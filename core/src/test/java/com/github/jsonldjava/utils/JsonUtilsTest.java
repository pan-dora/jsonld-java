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

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.junit.Test;


public class JsonUtilsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void fromStringTest() {
        final String testString = "{\"seq\":3,\"id\":\"e48dfa735d9fad88db6b7cd696002df7\","
                + "\"changes\":[{\"rev\":\"2-6aebf275bc3f29b67695c727d448df8e\"}]}";
        final String testFailure = "{{{{{{{{{{{";
        Object obj = null;

        try {
            obj = JsonUtils.fromString(testString);
        } catch (final Exception e) {
            assertTrue(false);
        }

        assertTrue(((Map<String, Object>) obj).containsKey("seq"));
        assertTrue(((Map<String, Object>) obj).get("seq") instanceof Number);

        try {
            obj = JsonUtils.fromString(testFailure);
            assertTrue(false);
        } catch (final Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromJsonParser() throws Exception {
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory(jsonMapper);
        Reader testInputString = new StringReader("{}");
        JsonParser jp = jsonFactory.createParser(testInputString);
        JsonUtils.fromJsonParser(jp);
    }

    @Test
    public void trailingContent_1() throws IOException {
        trailingContent("{}");
    }

    @Test
    public void trailingContent_2() throws IOException {
        trailingContent("{}  \t  \r \n  \r\n   ");
    }

    @Test(expected = JsonParseException.class)
    public void trailingContent_3() throws IOException {
        trailingContent("{}x");
    }

    @Test(expected = JsonParseException.class)
    public void trailingContent_4() throws IOException {
        trailingContent("{}   x");
    }

    @Test(expected = JsonParseException.class)
    public void trailingContent_5() throws IOException {
        trailingContent("{} \"x\"");
    }

    @Test(expected = JsonParseException.class)
    public void trailingContent_6() throws IOException {
        trailingContent("{} {}");
    }

    @Test(expected = JsonParseException.class)
    public void trailingContent_7() throws IOException {
        trailingContent("{},{}");
    }

    @Test(expected = JsonParseException.class)
    public void trailingContent_8() throws IOException {
        trailingContent("{},[]");
    }

    private void trailingContent(String string) throws IOException {
        JsonUtils.fromString(string);
    }
}
