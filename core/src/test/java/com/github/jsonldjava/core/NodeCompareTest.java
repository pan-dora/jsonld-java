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
import static org.junit.Assert.assertNotEquals;

import com.github.jsonldjava.core.RDFDataset.BlankNode;
import com.github.jsonldjava.core.RDFDataset.IRI;
import com.github.jsonldjava.core.RDFDataset.Literal;
import com.github.jsonldjava.core.RDFDataset.Node;
import com.github.jsonldjava.core.RDFDataset.Quad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class NodeCompareTest {

    /**
     * While this order might not particularly make sense (RDF is unordered),
     * this is at least documented. Feel free to move things around below if the
     * underlying .compareTo() changes.
     */
    @Test
    public void ordered() throws Exception {
        List<Node> expected = Arrays.asList(

                new Literal("1", JsonLdConsts.XSD_INTEGER, null),
                new Literal("10", JsonLdConsts.XSD_INTEGER, null),
                new Literal("2", JsonLdConsts.XSD_INTEGER, null), // still ordered by string value

                new Literal("a", JsonLdConsts.RDF_LANGSTRING, "en"),
                new Literal("a", JsonLdConsts.RDF_LANGSTRING, "fr"),
                new Literal("a", null, null), // equivalent to xsd:string
                new Literal("b", JsonLdConsts.XSD_STRING, null),
                new Literal("false", JsonLdConsts.XSD_BOOLEAN, null),
                new Literal("true", JsonLdConsts.XSD_BOOLEAN, null),

                new Literal("x", JsonLdConsts.XSD_STRING, null),

                new Literal("z", JsonLdConsts.RDF_LANGSTRING, "en"),
                new Literal("z", JsonLdConsts.RDF_LANGSTRING, "fr"),
                new Literal("z", null, null),

                new BlankNode("a"),
                new BlankNode("f"),
                new BlankNode("z"),

                new IRI("http://example.com/ex1"),
                new IRI("http://example.com/ex2"),
                new IRI("http://example.org/ex"),
                new IRI("https://example.net/")
        );

        List<Node> shuffled = new ArrayList<>(expected);
        Random rand = new Random(1337); // fixed seed
        Collections.shuffle(shuffled, rand);
        //System.out.println("Shuffled:");
        //shuffled.stream().forEach(System.out::println);
        assertNotEquals(expected, shuffled);

        Collections.sort(shuffled);
        //System.out.println("Now sorted:");
        //sorted.stream().forEach(System.out::println);
        // Not so useful output from this
        //        assertEquals(expected, sorted);
        // so we'll instead do:
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Wrong sort order at position " + i,
                    expected.get(i), shuffled.get(i));
        }
    }

    @Test
    public void literalSameValue() throws Exception {
        Literal l1 = new Literal("Same", null, null);
        Literal l2 = new Literal("Same", null, null);
        assertEquals(l1, l2);
        assertEquals(0, l1.compareTo(l2));
    }

    @Test
    public void literalDifferentValue() throws Exception {
        Literal l1 = new Literal("Same", null, null);
        Literal l2 = new Literal("Different", null, null);
        assertNotEquals(l1, l2);
        assertNotEquals(0, l1.compareTo(l2));
    }

    @Test
    public void literalSameValueSameLang() throws Exception {
        Literal l1 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, "en");
        Literal l2 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, "en");
        assertEquals(l1, l2);
        assertEquals(0, l1.compareTo(l2));
    }

    @Test
    public void literalDifferentValueSameLang() throws Exception {
        Literal l1 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, "en");
        Literal l2 = new Literal("Different", JsonLdConsts.RDF_LANGSTRING, "en");
        assertNotEquals(l1, l2);
        assertNotEquals(0, l1.compareTo(l2));
    }

    @Test
    public void literalSameValueDifferentLang() throws Exception {
        Literal l1 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, "en");
        Literal l2 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, "no");
        assertNotEquals(l1, l2);
        assertNotEquals(0, l1.compareTo(l2));
    }

    @Test
    public void literalSameValueLangNull() throws Exception {
        Literal l1 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, "en");
        Literal l2 = new Literal("Same", JsonLdConsts.RDF_LANGSTRING, null);
        assertNotEquals(l1, l2);
        assertNotEquals(0, l1.compareTo(l2));
        assertNotEquals(0, l2.compareTo(l1));
    }


    @Test
    public void literalSameValueSameType() throws Exception {
        Literal l1 = new Literal("1", JsonLdConsts.XSD_INTEGER, null);
        Literal l2 = new Literal("1", JsonLdConsts.XSD_INTEGER, null);
        assertEquals(l1, l2);
        assertEquals(0, l1.compareTo(l2));
    }

    @Test
    public void literalSameValueSameTypeNull() throws Exception {
        Literal l1 = new Literal("1", JsonLdConsts.XSD_STRING, null);
        Literal l2 = new Literal("1", null, null);
        assertEquals(l1, l2);
        assertEquals(0, l1.compareTo(l2));
    }


    @Test
    public void literalSameValueDifferentType() throws Exception {
        Literal l1 = new Literal("1", JsonLdConsts.XSD_INTEGER, null);
        Literal l2 = new Literal("1", JsonLdConsts.XSD_STRING, null);
        assertNotEquals(l1, l2);
        assertNotEquals(0, l1.compareTo(l2));
    }


    @Test
    public void literalsInDataset() throws Exception {
        RDFDataset dataset = new RDFDataset();
        dataset.addQuad("http://example.com/p", "http://example.com/p", "Same", null, null,
                "http://example.com/g1");
        dataset.addQuad("http://example.com/p", "http://example.com/p", "Different", null, null,
                "http://example.com/g1");
        List<Quad> quads = dataset.getQuads("http://example.com/g1");
        Quad q1 = quads.get(0);
        Quad q2 = quads.get(1);
        assertNotEquals(q1, q2);
        assertNotEquals(0, q1.compareTo(q2));
        assertNotEquals(0, q1.getObject().compareTo(q2.getObject()));
    }

    @Test
    public void iriDifferentLiteral() throws Exception {
        Node iri = new IRI("http://example.com/");
        Node literal = new Literal("http://example.com/", null, null);
        assertNotEquals(iri, literal);
        assertNotEquals(0, iri.compareTo(literal));
        assertNotEquals(0, literal.compareTo(iri));
    }

    @Test
    public void iriDifferentNull() throws Exception {
        Node iri = new IRI("http://example.com/");
        assertNotEquals(0, iri.compareTo(null));
    }

    @Test
    public void literalDifferentNull() throws Exception {
        Node literal = new Literal("hello", null, null);
        assertNotEquals(0, literal.compareTo(null));
    }

    @Test
    public void iriDifferentIri() throws Exception {
        Node iri = new IRI("http://example.com/");
        Node other = new IRI("http://example.com/other");
        assertNotEquals(iri, other);
        assertNotEquals(0, iri.compareTo(other));
    }

    @Test
    public void iriSameIri() throws Exception {
        Node iri = new IRI("http://example.com/same");
        Node same = new IRI("http://example.com/same");
        assertEquals(iri, same);
        assertEquals(0, iri.compareTo(same));
    }

    @Test
    public void iriDifferentBlankNode() throws Exception {
        // We'll use a relative IRI to avoid :-issues
        Node iri = new IRI("b1");
        Node bnode = new BlankNode("b1");
        assertNotEquals(iri, bnode);
        assertNotEquals(bnode, iri);
        assertNotEquals(0, iri.compareTo(bnode));
        assertNotEquals(0, bnode.compareTo(iri));
    }

    @Test
    public void literalDifferentBlankNode() throws Exception {
        // We'll use a relative IRI to avoid :-issues
        Node literal = new Literal("b1", null, null);
        Node bnode = new BlankNode("b1");
        assertNotEquals(literal, bnode);
        assertNotEquals(bnode, literal);
        assertNotEquals(0, literal.compareTo(bnode));
        assertNotEquals(0, bnode.compareTo(literal));

    }


}
