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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.jsonldjava.core.JsonLdApi;
import com.github.jsonldjava.core.JsonLdProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;


/**
 * Functions used to make loading, parsing, and serializing JSON easy using
 * Jackson.
 *
 * @author tristan
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * An HTTP Accept header that prefers JSONLD.
     */
    public static final String ACCEPT_HEADER = "application/ld+json, application/json;q=0.9, "
            + "application/javascript;"
            + "q=0.5, text/javascript;q=0.5, text/plain;q=0.2, */*;q=0.1";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final JsonFactory JSON_FACTORY = new JsonFactory(JSON_MAPPER);
    private static volatile CloseableHttpClient DEFAULT_HTTP_CLIENT;

    static {
        // Disable default Jackson behaviour to close
        // InputStreams/Readers/OutputStreams/Writers
        JSON_FACTORY.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        // Disable string retention features that may work for most JSON where
        // the field names are in limited supply, but does not work for JSON-LD
        // where a wide range of URIs are used for subjects and predicates
        JSON_FACTORY.disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        JSON_FACTORY.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
    }

    /**
     * Parses a JSON-LD document from the given {@link InputStream} to an object
     * that can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.<br>
     * Uses UTF-8 as the character encoding when decoding the InputStream.
     *
     * @param input The JSON-LD document in an InputStream.
     * @return A JSON Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    public static Object fromInputStream(InputStream input) throws IOException {
        // no readers from inputstreams w.o. encoding!!
        return fromInputStream(input, "UTF-8");
    }

    /**
     * Parses a JSON-LD document from the given {@link InputStream} to an object
     * that can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.
     *
     * @param input The JSON-LD document in an InputStream.
     * @param enc   The character encoding to use when interpreting the characters
     *              in the InputStream.
     * @return A JSON Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    private static Object fromInputStream(InputStream input, String enc) throws IOException {
        try (InputStreamReader in = new InputStreamReader(input, enc);
             BufferedReader reader = new BufferedReader(in)) {
            return fromReader(reader);
        }
    }

    /**
     * Parses a JSON-LD document from the given {@link Reader} to an object that
     * can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.
     *
     * @param reader The JSON-LD document in a Reader.
     * @return A JSON Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    public static Object fromReader(Reader reader) throws IOException {
        final JsonParser jp = JSON_FACTORY.createParser(reader);
        return fromJsonParser(jp);
    }

    /**
     * Parses a JSON-LD document from the given {@link JsonParser} to an object that
     * can be used as input for the {@link JsonLdApi} and
     * {@link JsonLdProcessor} methods.
     *
     * @param jp The JSON-LD document in a {@link JsonParser}.
     * @return A JSON Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    static Object fromJsonParser(JsonParser jp) throws IOException {
        Object rval;
        final JsonToken initialToken = jp.nextToken();

        if (initialToken == JsonToken.START_ARRAY) {
            rval = jp.readValueAs(List.class);
        } else if (initialToken == JsonToken.START_OBJECT) {
            rval = jp.readValueAs(Map.class);
        } else if (initialToken == JsonToken.VALUE_STRING) {
            rval = jp.readValueAs(String.class);
        } else if (initialToken == JsonToken.VALUE_FALSE || initialToken == JsonToken.VALUE_TRUE) {
            rval = jp.readValueAs(Boolean.class);
        } else if (initialToken == JsonToken.VALUE_NUMBER_FLOAT
                || initialToken == JsonToken.VALUE_NUMBER_INT) {
            rval = jp.readValueAs(Number.class);
        } else if (initialToken == JsonToken.VALUE_NULL) {
            rval = null;
        } else {
            throw new JsonParseException(jp,
                    "document doesn't start with a valid json element : " + initialToken,
                    jp.getCurrentLocation());
        }

        JsonToken t;
        try {
            t = jp.nextToken();
        } catch (final JsonParseException ex) {
            throw new JsonParseException(jp,
                    "Document contains more content after json-ld element - (possible mismatched "
                            + "{}?)",
                    jp.getCurrentLocation());
        }
        if (t != null) {
            throw new JsonParseException(jp,
                    "Document contains possible json content after the json-ld element - "
                            + "(possible mismatched {}?)",
                    jp.getCurrentLocation());
        }
        return rval;
    }

    /**
     * Parses a JSON-LD document from a string to an object that can be used as
     * input for the {@link JsonLdApi} and {@link JsonLdProcessor} methods.
     *
     * @param jsonString The JSON-LD document as a string.
     * @return A JSON Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    public static Object fromString(String jsonString) throws IOException {
        return fromReader(new StringReader(jsonString));
    }

    /**
     * Writes the given JSON-LD Object out to a String, using indentation and
     * new lines to improve readability.
     *
     * @param jsonObject The JSON-LD Object to serialize.
     * @return A JSON document serialised to a String.
     * @throws JsonGenerationException If there is a JSON error during serialization.
     * @throws IOException             If there is an IO error during serialization.
     */
    public static String toPrettyString(Object jsonObject)
            throws JsonGenerationException, IOException {
        final StringWriter sw = new StringWriter();
        writePrettyPrint(sw, jsonObject);
        return sw.toString();
    }

    /**
     * Writes the given JSON-LD Object out to a String.
     *
     * @param jsonObject The JSON-LD Object to serialize.
     * @return A JSON document serialised to a String.
     * @throws JsonGenerationException If there is a JSON error during serialization.
     * @throws IOException             If there is an IO error during serialization.
     */
    public static String toString(Object jsonObject) throws JsonGenerationException, IOException {
        final StringWriter sw = new StringWriter();
        write(sw, jsonObject);
        return sw.toString();
    }

    /**
     * Writes the given JSON-LD Object out to the given Writer.
     *
     * @param writer     The writer that is to receive the serialized JSON-LD object.
     * @param jsonObject The JSON-LD Object to serialize.
     * @throws JsonGenerationException If there is a JSON error during serialization.
     * @throws IOException             If there is an IO error during serialization.
     */
    private static void write(Writer writer, Object jsonObject)
            throws IOException {
        final JsonGenerator jw = JSON_FACTORY.createGenerator(writer);
        jw.writeObject(jsonObject);
    }

    /**
     * Writes the given JSON-LD Object out to the given Writer, using
     * indentation and new lines to improve readability.
     *
     * @param writer     The writer that is to receive the serialized JSON-LD object.
     * @param jsonObject The JSON-LD Object to serialize.
     * @throws JsonGenerationException If there is a JSON error during serialization.
     * @throws IOException             If there is an IO error during serialization.
     */
    public static void writePrettyPrint(Writer writer, Object jsonObject)
            throws JsonGenerationException, IOException {
        final JsonGenerator jw = JSON_FACTORY.createGenerator(writer);
        jw.useDefaultPrettyPrinter();
        jw.writeObject(jsonObject);
    }

    /**
     * Parses a JSON-LD document, from the contents of the JSON resource
     * resolved from the JsonLdUrl, to an object that can be used as input for
     * the {@link JsonLdApi} and {@link JsonLdProcessor} methods.
     *
     * @param url        The JsonLdUrl to resolve
     * @param httpClient The {@link CloseableHttpClient} to use to resolve the URL.
     * @return A JSON Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    public static Object fromURL(java.net.URL url, CloseableHttpClient httpClient)
            throws IOException {
        final String protocol = url.getProtocol();
        // We can only use the Apache HTTPClient for HTTP/HTTPS, so use the
        // native java client for the others
        InputStream in = null;
        try {
            if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
                // Can't use the HTTP client for those!
                // Fallback to Java's built-in JsonLdUrl handler. No need for
                // Accept headers as it's likely to be file: or jar:
                in = url.openStream();
            } else {
                final HttpUriRequest request = new HttpGet(url.toExternalForm());
                // We prefer application/ld+json, but fallback to
                // application/json
                // or whatever is available
                request.addHeader("Accept", ACCEPT_HEADER);

                final CloseableHttpResponse response = httpClient.execute(request);
                final int status = response.getStatusLine().getStatusCode();
                if (status != 200 && status != 203) {
                    throw new IOException("Can't retrieve " + url + ", status code: " + status);
                }
                in = response.getEntity().getContent();
            }
            return fromInputStream(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Fallback method directly using the {@link java.net.HttpURLConnection}
     * class for cases where servers do not interoperate correctly with Apache
     * HTTPClient.
     *
     * @param url The URL to access.
     * @return The result, after conversion from JSON to a Java Object.
     * @throws JsonParseException If there was a JSON related error during parsing.
     * @throws IOException        If there was an IO error during parsing.
     */
    public static Object fromURLJavaNet(java.net.URL url) throws IOException {
        final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.addRequestProperty("Accept", ACCEPT_HEADER);

        final StringWriter output = new StringWriter();
        try (InputStream directStream = urlConn.getInputStream()) {
            IOUtils.copy(directStream, output, Charset.forName("UTF-8"));
        } finally {
            output.flush();
        }
        return JsonUtils.fromReader(new StringReader(output.toString()));
    }

    public static CloseableHttpClient getDefaultHttpClient() {
        CloseableHttpClient result = DEFAULT_HTTP_CLIENT;
        if (result == null) {
            synchronized (JsonUtils.class) {
                result = DEFAULT_HTTP_CLIENT;
                if (result == null) {
                    result = DEFAULT_HTTP_CLIENT = JsonUtils.createDefaultHttpClient();
                }
            }
        }
        return result;
    }

    private static CloseableHttpClient createDefaultHttpClient() {
        // Common CacheConfig for both the JarCacheStorage and the underlying
        // BasicHttpCacheStorage
        final CacheConfig cacheConfig = CacheConfig.custom().setMaxCacheEntries(1000)
                .setMaxObjectSize(1024 * 128).build();

        return CachingHttpClientBuilder.create()
                // allow caching
                .setCacheConfig(cacheConfig)
                // Wrap the local JarCacheStorage around a BasicHttpCacheStorage
                .setHttpCacheStorage(new JarCacheStorage(null, cacheConfig,
                        new BasicHttpCacheStorage(cacheConfig)))
                // Support compressed data
                // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/httpagent
                // .html#d5e1238
                .addInterceptorFirst(new RequestAcceptEncoding())
                .addInterceptorFirst(new ResponseContentEncoding())
                .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
                // use system defaults for proxy etc.
                .useSystemProperties().build();
    }
}
