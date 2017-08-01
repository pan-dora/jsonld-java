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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.junit.After;
import org.junit.Test;

public class JarCacheTest {

    @Test
    public void cacheHit() throws Exception {
        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();
        final JarCacheStorage storage = new JarCacheStorage(null, cacheConfig);
        final HttpClient httpClient = createTestHttpClient(cacheConfig, storage);
        final HttpGet get = new HttpGet("http://nonexisting.example.com/context");
        final HttpResponse resp = httpClient.execute(get);

        assertEquals("application/ld+json", resp.getEntity().getContentType().getValue());
        final String str = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
        assertTrue(str.contains("ex:datatype"));
    }

    @Test(expected = IOException.class)
    public void cacheMiss() throws Exception {
        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();
        final JarCacheStorage storage = new JarCacheStorage(null, cacheConfig);
        final HttpClient httpClient = createTestHttpClient(cacheConfig, storage);
        final HttpGet get = new HttpGet("http://nonexisting.example.com/notfound");
        // Should throw an IOException as the DNS name
        // nonexisting.example.com does not exist
        httpClient.execute(get);
    }

    @Test
    public void doubleLoad() throws Exception {
        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();
        final JarCacheStorage storage = new JarCacheStorage(null, cacheConfig);
        final HttpClient httpClient = createTestHttpClient(cacheConfig, storage);
        final HttpGet get = new HttpGet("http://nonexisting.example.com/context");
        HttpResponse resp = httpClient.execute(get);
        resp = httpClient.execute(get);
        // Ensure second load through the cached jarcache list works
        assertEquals("application/ld+json", resp.getEntity().getContentType().getValue());
    }

    @Test
    public void customClassPath() throws Exception {
        final URL nestedJar = getClass().getResource("/nested.jar");
        final ClassLoader cl = new URLClassLoader(new URL[]{nestedJar});
        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();
        final JarCacheStorage storage = new JarCacheStorage(cl, cacheConfig);
        final HttpClient httpClient = createTestHttpClient(cacheConfig, storage);
        final HttpGet get = new HttpGet("http://nonexisting.example.com/nested/hello");
        final HttpResponse resp = httpClient.execute(get);

        assertEquals("application/json", resp.getEntity().getContentType().getValue());
        final String str = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
        assertEquals("{ \"Hello\": \"World!\" }", str.trim());
    }

    @Test
    public void contextClassLoader() throws Exception {
        final URL nestedJar = getClass().getResource("/nested.jar");
        assertNotNull(nestedJar);
        final ClassLoader cl = new URLClassLoader(new URL[]{nestedJar});

        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();
        final JarCacheStorage storage = new JarCacheStorage(cl, cacheConfig);
        Thread.currentThread().setContextClassLoader(cl);

        final HttpClient httpClient = createTestHttpClient(cacheConfig, storage);
        final HttpGet get = new HttpGet("http://nonexisting.example.com/nested/hello");
        final HttpResponse resp = httpClient.execute(get);

        assertEquals("application/json", resp.getEntity().getContentType().getValue());
        final String str = IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
        assertEquals("{ \"Hello\": \"World!\" }", str.trim());
    }

    @After
    public void setContextClassLoader() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    }

    @Test
    public void systemClassLoader() throws Exception {
        final URL nestedJar = getClass().getResource("/nested.jar");
        assertNotNull(nestedJar);
        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();
        final JarCacheStorage storage = new JarCacheStorage(null, cacheConfig);

        final HttpClient httpClient = createTestHttpClient(cacheConfig, storage);
        final HttpGet get = new HttpGet("http://nonexisting.example.com/context");
        final HttpResponse resp = httpClient.execute(get);
        assertEquals("application/ld+json", resp.getEntity().getContentType().getValue());
    }

    private static CloseableHttpClient createTestHttpClient(CacheConfig cacheConfig,
                                                            JarCacheStorage jarCacheConfig) {
        final CloseableHttpClient result = CachingHttpClientBuilder.create()
                // allow caching
                .setCacheConfig(cacheConfig)
                // Set the JarCacheStorage instance as the HttpCache
                .setHttpCacheStorage(jarCacheConfig)
                // Support compressed data
                // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/httpagent
                // .html#d5e1238
                .addInterceptorFirst(new RequestAcceptEncoding())
                .addInterceptorFirst(new ResponseContentEncoding())
                .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
                // use system defaults for proxy etc.
                .useSystemProperties().build();

        return result;
    }
}
