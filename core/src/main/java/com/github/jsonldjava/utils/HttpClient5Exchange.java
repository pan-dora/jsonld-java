package com.github.jsonldjava.utils;

import static com.github.jsonldjava.core.JsonLdConsts.*;
import static com.github.jsonldjava.utils.JsonUtils.fromInputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdApi;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.sync.methods.HttpGet;
import org.apache.hc.client5.http.sync.methods.HttpHead;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.jackrabbit.webdav.util.LinkHeaderFieldParser;

public class HttpClient5Exchange {
    private static volatile CloseableHttpClient DEFAULT_HTTP_CLIENT;

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
    public static Object getRemoteDocument(java.net.URL url, CloseableHttpClient httpClient)
            throws IOException, JsonLdError {
        final String protocol = url.getProtocol();
        if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
            return fromInputStream(url.openStream());
        } else {
            final ClassicHttpRequest headRequest = new HttpHead(url.toExternalForm());
            final CloseableHttpResponse headResponse = httpClient.execute(headRequest);
            final int status = headResponse.getCode();
            if (status != 200 && status != 203) {
                throw new IOException("Can't retrieve " + url + ", status code: " + status);
            }
            final ClassicHttpRequest getRequest = new HttpGet(url.toExternalForm());
            //request.addHeader("Accept", ACCEPT_HEADER);
            return processExchange(httpClient, getRequest, headResponse);
        }
    }

    public static Object processExchange(CloseableHttpClient httpClient,
                                         ClassicHttpRequest getRequest,
                                         ClassicHttpResponse headResponse)
            throws IOException, JsonLdError {
        //6.8 <https://www.w3.org/TR/json-ld/#interpreting-json-as-json-ld>
        try {
            Header[] headers = headResponse.getAllHeaders();
            Set<String> headerValues = parseTokenOrCodedUrlheaderField(headers);
            //read headers for "application/json" Content-Type OR a Link @context relation.
            //If Link JSONLD relation is present and client only Accepts application/ld+json
            //Content-Type, 1.0 specification says ignore, we allow it to be read.
            //Content-Type will only be application/json if explicitly Accepted or client does
            // not have a preference, and it is the API default.
            Set<String> relations = headerValues.stream()
                    .filter(link -> link.contains(JSONLD_CONTEXT_RELATION) ||
                            link.contains(APPLICATION_JSON)).collect(Collectors.toSet());
            if (relations.size() > 0) {
                List<String> relationsList = new ArrayList<>(headerValues);
                //use parser to get relations from link header String
                LinkHeaderFieldParser lhfp = new LinkHeaderFieldParser(relationsList);
                if (lhfp.getFirstTargetForRelation(JSONLD_CONTEXT_RELATION) != null) {
                    String context;
                    context = lhfp.getFirstTargetForRelation(JSONLD_CONTEXT_RELATION);
                    //got context from headers, proceed with GET
                    final CloseableHttpResponse response = httpClient.execute(getRequest);
                    Map<Object, Object> map = new HashMap<>();
                    InputStream is = response.getEntity().getContent();
                    //map has context string as key, document as value
                    map.put(context, fromInputStream(is));
                    //TODO check for profile, and if present, put key/value in map
                    return map;
                }
            } else if (headerValues.contains(APPLICATION_JSONLD)) {
                //typical response
                final CloseableHttpResponse response = httpClient.execute(getRequest);
                return fromInputStream(response.getEntity().getContent());
            }
        } catch (final Exception e) {
            throw new JsonLdError(JsonLdError.Error.UNKNOWN_FORMAT, e);
        }
        return null;
    }

    private static Set<String> parseTokenOrCodedUrlheaderField(Header[] headers) {
        if (headers == null) {
            return Collections.emptySet();
        } else {
            Set<String> result = new HashSet<>();
            int var5 = headers.length;

            for (Header h : headers) {
                String s;
                for (Iterator var8 = tokenizeList(h.getValue()).iterator(); var8.hasNext();
                     result.add(s.trim())) {
                    s = (String) var8.next();
                    if (s.startsWith("<") && s.endsWith(">")) {
                        s = s.substring(1, s.length() - 1);
                    }
                }
            }
            return Collections.unmodifiableSet(result);
        }
    }

    private static List<String> tokenizeList(String list) {
        String[] split = list.split(",");
        if (split.length == 1) {
            return Collections.singletonList(split[0].trim());
        } else {
            List<String> result = new ArrayList<>();
            String inCodedUrl;

            for (String t : split) {
                inCodedUrl = t.trim();
                result.add(inCodedUrl);
            }
            return Collections.unmodifiableList(result);
        }
    }

    public static CloseableHttpClient getDefaultHttpClient() {
        CloseableHttpClient result = DEFAULT_HTTP_CLIENT;
        if (result == null) {
            synchronized (JsonUtils.class) {
                result = DEFAULT_HTTP_CLIENT;
                if (result == null) {
                    result = DEFAULT_HTTP_CLIENT = createDefaultHttpClient();
                }
            }
        }
        return result;
    }

    private static CloseableHttpClient createDefaultHttpClient() {
        return HttpClientBuilder.create().build();
    }
}
