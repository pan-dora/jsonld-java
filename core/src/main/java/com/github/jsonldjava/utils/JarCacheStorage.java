/*
 * Copyright (c) 2012, Deutsche Forschungszentrum für Künstliche Intelligenz GmbH
 * Copyright (c) 2012-2017, JSONLD-Java contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.jsonldjava.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.client.cache.HeaderConstants;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.cache.HttpCacheUpdateCallback;
import org.apache.http.client.cache.HttpCacheUpdateException;
import org.apache.http.client.cache.Resource;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JarCacheStorage.
 *
 * @author Stian Soiland-Reyes
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class JarCacheStorage implements HttpCacheStorage {

    private static final String JARCACHE_JSON = "jarcache.json";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CacheConfig cacheConfig;

    private ClassLoader classLoader;

    /**
     * All live caching that is not found locally is delegated to this
     * implementation.
     */
    private final HttpCacheStorage delegate;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Map from uri of jarcache.json (e.g. jar://blab.jar!jarcache.json) to a
     * SoftReference to its content as JsonNode.
     *
     * @see #getJarCache(URL)
     */
    private final ConcurrentMap<URI, SoftReference<JsonNode>> jarCaches = new ConcurrentHashMap<>();

    private ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    JarCacheStorage(ClassLoader classLoader, CacheConfig cacheConfig) {
        this(classLoader, cacheConfig, new BasicHttpCacheStorage(cacheConfig));
    }

    public JarCacheStorage(ClassLoader classLoader, CacheConfig cacheConfig,
                           HttpCacheStorage delegate) {
        setClassLoader(classLoader);
        this.cacheConfig = cacheConfig;
        this.delegate = delegate;
    }

    @Override
    public void putEntry(String key, HttpCacheEntry entry) throws IOException {
        delegate.putEntry(key, entry);
    }

    @Override
    public HttpCacheEntry getEntry(String key) throws IOException {
        log.trace("Requesting " + key);
        URI requestedUri;
        try {
            requestedUri = new URI(key);
        } catch (final URISyntaxException e) {
            return null;
        }
        if ((requestedUri.getScheme().equals("http") && requestedUri.getPort() == 80)
                || (requestedUri.getScheme().equals("https") && requestedUri.getPort() == 443)) {
            // Strip away default http ports
            try {
                requestedUri = new URI(requestedUri.getScheme(), requestedUri.getHost(),
                        requestedUri.getPath(), requestedUri.getFragment());
            } catch (final URISyntaxException ignored) {
            }
        }

        final Enumeration<URL> jarcaches = getResources();
        while (jarcaches.hasMoreElements()) {
            final URL url = jarcaches.nextElement();

            final JsonNode tree = getJarCache(url);
            // TODO: Cache tree per URL
            for (final JsonNode node : tree) {
                final URI uri = URI.create(node.get("Content-Location").asText());
                if (uri.equals(requestedUri)) {
                    return cacheEntry(requestedUri, url, node);

                }
            }
        }
        // If we didn't find it in our cache, then attempt to find it in the
        // chained delegate
        return delegate.getEntry(key);
    }

    private Enumeration<URL> getResources() throws IOException {
        final ClassLoader cl = getClassLoader();
        if (cl != null) {
            return cl.getResources(JARCACHE_JSON);
        } else {
            return ClassLoader.getSystemResources(JARCACHE_JSON);
        }
    }

    private JsonNode getJarCache(URL url) throws IOException {

        URI uri;
        try {
            uri = url.toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Invalid jarCache URI " + url, e);
        }

        // Check if we have one from before - we'll use SoftReference so that
        // the maps reference is not counted for garbage collection purposes
        final SoftReference<JsonNode> jarCacheRef = jarCaches.get(uri);
        if (jarCacheRef != null) {
            final JsonNode jarCache = jarCacheRef.get();
            if (jarCache != null) {
                return jarCache;
            } else {
                jarCaches.remove(uri);
            }
        }

        // Only parse again if the optimistic get failed
        final JsonNode tree = mapper.readTree(url);
        // Use putIfAbsent to ensure concurrent reads do not return different
        // JsonNode objects, for memory management purposes
        final SoftReference<JsonNode> putIfAbsent = jarCaches.putIfAbsent(uri,
                new SoftReference<>(tree));
        if (putIfAbsent != null) {
            final JsonNode returnValue = putIfAbsent.get();
            if (returnValue != null) {
                return returnValue;
            } else {
                // Force update the reference if the existing reference had
                // been garbage collected
                jarCaches.put(uri, new SoftReference<>(tree));
            }
        }
        return tree;
    }

    private HttpCacheEntry cacheEntry(URI requestedUri, URL baseURL, JsonNode cacheNode)
            throws IOException {
        final URL classpath = new URL(baseURL, cacheNode.get("X-Classpath").asText());
        log.debug("Cache hit for " + requestedUri);
        log.trace("{}", cacheNode);

        final List<Header> responseHeaders = new ArrayList<>();
        if (!cacheNode.has(HTTP.DATE_HEADER)) {
            responseHeaders
                    .add(new BasicHeader(HTTP.DATE_HEADER, DateUtils.formatDate(new Date())));
        }
        if (!cacheNode.has(HeaderConstants.CACHE_CONTROL)) {
            responseHeaders.add(new BasicHeader(HeaderConstants.CACHE_CONTROL,
                    HeaderConstants.CACHE_CONTROL_MAX_AGE + "=" + Integer.MAX_VALUE));
        }
        final Resource resource = new JarCacheResource(classpath);
        final Iterator<String> fieldNames = cacheNode.fieldNames();
        while (fieldNames.hasNext()) {
            final String headerName = fieldNames.next();
            final JsonNode header = cacheNode.get(headerName);
            // TODO: Support multiple headers with []
            responseHeaders.add(new BasicHeader(headerName, header.asText()));
        }

        return new HttpCacheEntry(new Date(), new Date(),
                new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"),
                responseHeaders.toArray(new Header[0]), resource);
    }

    @Override
    public void removeEntry(String key) throws IOException {
        delegate.removeEntry(key);
    }

    @Override
    public void updateEntry(String key, HttpCacheUpdateCallback callback)
            throws IOException, HttpCacheUpdateException {
        delegate.updateEntry(key, callback);
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

}
