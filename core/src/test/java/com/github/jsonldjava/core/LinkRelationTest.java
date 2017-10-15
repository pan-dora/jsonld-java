package com.github.jsonldjava.core;

import static com.github.jsonldjava.core.JsonLdConsts.APPLICATION_JSON;
import static com.github.jsonldjava.core.JsonLdConsts.APPLICATION_JSONLD;
import static com.github.jsonldjava.utils.HttpClient5Exchange.getDefaultHttpClient;
import static com.github.jsonldjava.utils.HttpClient5Exchange.processExchange;
import static junit.framework.TestCase.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.sync.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public class LinkRelationTest {
    private final DocumentLoader documentLoader = new DocumentLoader();
    private static String JSONLD_LINK_HEADER =
            "<https://schema.org/>; rel=\"http://www" +
                    ".w3.org/ns/json-ld#context\"; type=\"application/ld+json\"";
    private InputStream content;
    private HttpEntity entity;
    private CloseableHttpClient client = getDefaultHttpClient();
    private ClassicHttpRequest request;
    private ClassicHttpResponse response;
    private BasicHeader contentType;
    private BasicHeader link;
    private Header[] headers;

    @Before
    public void setup() throws Exception {
        contentType = Mockito.mock(BasicHeader.class);
        PowerMockito.whenNew(BasicHeader.class)
                .withArguments(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON).thenReturn(contentType);
        link = Mockito.mock(BasicHeader.class);
        PowerMockito.whenNew(BasicHeader.class).withArguments("Link", JSONLD_LINK_HEADER)
                .thenReturn(link);

        content = Mockito.mock(InputStream.class);
        entity = Mockito.mock(HttpEntity.class);
        response = Mockito.mock(ClassicHttpResponse.class);
        //request = Mockito.mock(ClassicHttpRequest.class);
        //client = Mockito.mock(CloseableHttpClient.class, Mockito.CALLS_REAL_METHODS);
        URL url = new URL("http://schema.org");
        request = new HttpGet(url.toExternalForm());
        request.addHeader(HttpHeaders.ACCEPT, APPLICATION_JSONLD);
    }

    @Test
    public void testProcessExchange() throws Exception {
        Header ct = new BasicHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        Header lk = new BasicHeader("Link", JSONLD_LINK_HEADER);
        Header[] headers = {ct, lk};
        Mockito.when(response.getAllHeaders()).thenReturn(headers);
        Mockito.when(response.getCode()).thenReturn(200);
        Object obj = processExchange(client, request, response);
        assertTrue(obj instanceof Map);
    }
}

