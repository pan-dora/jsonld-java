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
import static org.junit.Assert.assertTrue;

import com.github.jsonldjava.utils.JsonUtils;

import java.net.URL;

import org.junit.Test;

public class LongestPrefixTest {
    @Test
    public void toRdfWithNamespace() throws Exception {

        final URL contextUrl = getClass().getResource("/custom/contexttest-0003.jsonld");
        assertNotNull(contextUrl);
        final Object context = JsonUtils.fromURL(contextUrl, JsonUtils.getDefaultHttpClient());
        assertNotNull(context);

        final JsonLdOptions options = new JsonLdOptions();
        options.useNamespaces = true;
        final RDFDataset rdf = (RDFDataset) JsonLdProcessor.toRDF(context, options);
        // System.out.println(rdf.getNamespaces());
        assertEquals("http://vocab.getty.edu/aat/", rdf.getNamespace("aat"));
        assertEquals("http://vocab.getty.edu/aat/rev/", rdf.getNamespace("aat_rev"));
    }

    @Test
    public void fromRdfWithNamespaceLexicographicallyShortestChosen() throws Exception {

        final RDFDataset inputRdf = new RDFDataset();
        inputRdf.setNamespace("aat", "http://vocab.getty.edu/aat/");
        inputRdf.setNamespace("aat_rev", "http://vocab.getty.edu/aat/rev/");

        inputRdf.addTriple("http://vocab.getty.edu/aat/rev/5001065997", JsonLdConsts.RDF_TYPE,
                "http://vocab.getty.edu/aat/datatype");

        final JsonLdOptions options = new JsonLdOptions();
        options.useNamespaces = true;

        final Object fromRDF = JsonLdProcessor.compact(new JsonLdApi(options).fromRDF(inputRdf),
                inputRdf.getContext(), options);

        final RDFDataset rdf = (RDFDataset) JsonLdProcessor.toRDF(fromRDF, options);
        // System.out.println(rdf.getNamespaces());
        assertEquals("http://vocab.getty.edu/aat/", rdf.getNamespace("aat"));
        assertEquals("http://vocab.getty.edu/aat/rev/", rdf.getNamespace("aat_rev"));

        final String toJSONLD = JsonUtils.toPrettyString(fromRDF);
        // System.out.println(toJSONLD);

        assertTrue("The lexicographically shortest URI was not chosen",
                toJSONLD.contains("aat:rev/"));
    }

    @Test
    public void fromRdfWithNamespaceLexicographicallyShortestChosen2() throws Exception {

        final RDFDataset inputRdf = new RDFDataset();
        inputRdf.setNamespace("aat", "http://vocab.getty.edu/aat/");
        inputRdf.setNamespace("aatrev", "http://vocab.getty.edu/aat/rev/");

        inputRdf.addTriple("http://vocab.getty.edu/aat/rev/5001065997", JsonLdConsts.RDF_TYPE,
                "http://vocab.getty.edu/aat/datatype");

        final JsonLdOptions options = new JsonLdOptions();
        options.useNamespaces = true;

        final Object fromRDF = JsonLdProcessor.compact(new JsonLdApi(options).fromRDF(inputRdf),
                inputRdf.getContext(), options);

        final RDFDataset rdf = (RDFDataset) JsonLdProcessor.toRDF(fromRDF, options);
        // System.out.println(rdf.getNamespaces());
        assertEquals("http://vocab.getty.edu/aat/", rdf.getNamespace("aat"));
        assertEquals("http://vocab.getty.edu/aat/rev/", rdf.getNamespace("aatrev"));

        final String toJSONLD = JsonUtils.toPrettyString(fromRDF);
        // System.out.println(toJSONLD);

        assertFalse("The lexicographically shortest URI was not chosen",
                toJSONLD.contains("aat:rev/"));
    }

    @Test
    public void prefixUsedToShortenPredicate() throws Exception {
        final RDFDataset inputRdf = new RDFDataset();
        inputRdf.setNamespace("ex", "http://www.a.com/foo/");
        inputRdf.addTriple("http://www.a.com/foo/s", "http://www.a.com/foo/p",
                "http://www.a.com/foo/o");
        assertEquals("http://www.a.com/foo/", inputRdf.getNamespace("ex"));

        final JsonLdOptions options = new JsonLdOptions();
        options.useNamespaces = true;

        final Object fromRDF = JsonLdProcessor.compact(new JsonLdApi(options).fromRDF(inputRdf),
                inputRdf.getContext(), options);
        final String toJSONLD = JsonUtils.toPrettyString(fromRDF);
        // System.out.println(toJSONLD);

        assertFalse("The lexicographically shortest URI was not chosen",
                toJSONLD.contains("http://www.a.com/foo/p"));
    }

}
