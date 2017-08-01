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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.github.jsonldjava.utils.JsonUtils;

import java.net.URL;

import org.junit.Test;

public class ArrayContextToRDFTest {
    @Test
    public void toRdfWithNamespace() throws Exception {

        final URL contextUrl = getClass().getResource("/custom/contexttest-0001.jsonld");
        assertNotNull(contextUrl);
        final Object context = JsonUtils.fromURL(contextUrl, JsonUtils.getDefaultHttpClient());
        assertNotNull(context);

        final URL arrayContextUrl = getClass().getResource("/custom/array-context.jsonld");
        assertNotNull(arrayContextUrl);
        final Object arrayContext = JsonUtils.fromURL(arrayContextUrl,
                JsonUtils.getDefaultHttpClient());
        assertNotNull(arrayContext);
        final JsonLdOptions options = new JsonLdOptions();
        options.useNamespaces = true;
        // Fake document loader that always returns the imported context
        // from classpath
        final DocumentLoader documentLoader = new DocumentLoader() {
            @Override
            public RemoteDocument loadDocument(String url) throws JsonLdError {
                return new RemoteDocument("http://nonexisting.example.com/context", context);
            }
        };
        options.setDocumentLoader(documentLoader);
        final RDFDataset rdf = (RDFDataset) JsonLdProcessor.toRDF(arrayContext, options);
        // System.out.println(rdf.getNamespaces());
        assertEquals("http://example.org/", rdf.getNamespace("ex"));
        assertEquals("http://example.com/2/", rdf.getNamespace("ex2"));
        // Only 'proper' prefixes returned
        assertFalse(rdf.getNamespaces().containsKey("term1"));

    }
}
