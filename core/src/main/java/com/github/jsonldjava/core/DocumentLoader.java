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

package com.github.jsonldjava.core;

import com.github.jsonldjava.utils.JsonUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * DocumentLoader.
 *
 * @author @tristan
 * @author Peter Ansell p_ansell@yahoo.com
 * @author @ryankenney
 */
public class DocumentLoader {

    private Map<String, Object> m_injectedDocs = new HashMap<>();

    /**
     * Identifies a system property that can be set to "true" in order to
     * disallow remote context loading.
     */
    static final String DISALLOW_REMOTE_CONTEXT_LOADING = "com.github.jsonldjava"
            + ".disallowRemoteContextLoading";

    DocumentLoader addInjectedDoc(String url, String doc) throws JsonLdError {
        try {
            m_injectedDocs.put(url, JsonUtils.fromString(doc));
            return this;
        } catch (final Exception e) {
            throw new JsonLdError(JsonLdError.Error.LOADING_INJECTED_CONTEXT_FAILED, url, e);
        }
    }

    public RemoteDocument loadDocument(String url) throws JsonLdError {
        final RemoteDocument doc = new RemoteDocument(url, null);

        if (m_injectedDocs.containsKey(url)) {
            try {
                doc.setDocument(m_injectedDocs.get(url));
            } catch (final Exception e) {
                throw new JsonLdError(JsonLdError.Error.LOADING_INJECTED_CONTEXT_FAILED, url, e);
            }
            return doc;
        }

        final String disallowRemote = System
                .getProperty(DocumentLoader.DISALLOW_REMOTE_CONTEXT_LOADING);
        if ("true".equalsIgnoreCase(disallowRemote)) {
            throw new JsonLdError(JsonLdError.Error.LOADING_REMOTE_CONTEXT_FAILED, "Remote "
                    + "context loading has been "
                    + "disallowed (url was " + url + ")");
        }

        try {
            doc.setDocument(JsonUtils.fromURL(new URL(url), getHttpClient()));
        } catch (final Exception e) {
            throw new JsonLdError(JsonLdError.Error.LOADING_REMOTE_CONTEXT_FAILED, url, e);
        }
        return doc;
    }

    /**
     * An HTTP Accept header that prefers JSONLD.
     *
     * @deprecated Use {@link JsonUtils#ACCEPT_HEADER} instead.
     */
    @Deprecated
    public static final String ACCEPT_HEADER = JsonUtils.ACCEPT_HEADER;

    private volatile CloseableHttpClient httpClient;

    CloseableHttpClient getHttpClient() {
        CloseableHttpClient result = httpClient;
        if (result == null) {
            synchronized (DocumentLoader.class) {
                result = httpClient;
                if (result == null) {
                    result = httpClient = JsonUtils.getDefaultHttpClient();
                }
            }
        }
        return result;
    }

    void setHttpClient(CloseableHttpClient nextHttpClient) {
        httpClient = nextHttpClient;
    }
}
