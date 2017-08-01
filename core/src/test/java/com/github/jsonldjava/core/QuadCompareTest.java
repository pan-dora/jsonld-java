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

import com.github.jsonldjava.core.RDFDataset.Quad;

import org.junit.Test;


public class QuadCompareTest {

    private Quad q = new Quad("http://example.com/s1", "http://example.com/p1",
            "http://example.com/o1", "http://example.com/g1");

    @Test
    public void compareToNull() throws Exception {
        assertNotEquals(0, q.compareTo(null));
    }

    @Test
    public void compareToSame() throws Exception {
        Quad q2 = new Quad("http://example.com/s1", "http://example.com/p1",
                "http://example.com/o1", "http://example.com/g1");
        assertEquals(0, q.compareTo(q2));
        // Should still compare equal, even if extra attributes are added
        q2.put("example", "value");
        assertEquals(0, q.compareTo(q2));
    }

    @Test
    public void compareToDifferentGraph() throws Exception {
        Quad q2 = new Quad("http://example.com/s1", "http://example.com/p1",
                "http://example.com/o1", "http://example.com/other");
        assertNotEquals(0, q.compareTo(q2));
    }

    @Test
    public void compareToDifferentSubject() throws Exception {
        Quad q2 = new Quad("http://example.com/other", "http://example.com/p1",
                "http://example.com/o1", "http://example.com/g1");
        assertNotEquals(0, q.compareTo(q2));
    }

    @Test
    public void compareToDifferentPredicate() throws Exception {
        Quad q2 = new Quad("http://example.com/s1", "http://example.com/other",
                "http://example.com/o1", "http://example.com/g1");
        assertNotEquals(0, q.compareTo(q2));
    }

    @Test
    public void compareToDifferentObject() throws Exception {
        Quad q2 = new Quad("http://example.com/s1", "http://example.com/p1",
                "http://example.com/other", "http://example.com/g1");
        assertNotEquals(0, q.compareTo(q2));
    }

    @Test
    public void compareToDifferentObjectType() throws Exception {
        Quad q2 = new Quad("http://example.com/s1", "http://example.com/p1",
                "http://example.com/other", null, null, // literal
                "http://example.com/g1");
        assertNotEquals(0, q.compareTo(q2));
    }

}
