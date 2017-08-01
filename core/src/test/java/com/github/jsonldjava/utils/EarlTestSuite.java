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

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EarlTestSuite {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String FILE_SEP = System.getProperty("file.separator");
    private final String cacheDir;
    private String etag;
    private List<Map<String, Object>> tests;

    public EarlTestSuite(String manifestURL) throws IOException {
        this(manifestURL, null, null);
    }

    /**
     * Loads an earl test suite.
     *
     * @param manifestURL the JsonLdUrl of the manifest file
     * @param cacheDir    the base directory to cache the files into
     * @param etag        the ETag field to load (useful if you know you have the latest
     *                    manifest cached and don't want to query the server for a new
     *                    one every time you run the tests).
     * @throws IOException IOException
     */
    @SuppressWarnings("unchecked")
    private EarlTestSuite(String manifestURL, String cacheDir, String etag) throws IOException {
        if (cacheDir == null) {
            cacheDir = getCacheDir(manifestURL);
        }
        this.cacheDir = cacheDir;
        new File(this.cacheDir).mkdirs();

        if (etag == null) {
            final java.net.URLConnection conn = new java.net.URL(manifestURL).openConnection();
            this.etag = conn.getHeaderField("ETag");
        } else {
            this.etag = etag;
        }
        final String manifestFile = getFile(manifestURL);

        Map<String, Object> manifest;
        if (manifestURL.endsWith(".ttl") || manifestURL.endsWith("nq")
                || manifestURL.endsWith("nt")) {
            try {
                Map<String, Object> rval = (Map<String, Object>) JsonLdProcessor
                        .fromRDF(manifestFile, new JsonLdOptions(manifestURL) {
                            {
                                this.format = "text/turtle";
                                this.useNamespaces = true;
                                this.outputForm = "compacted";
                            }
                        });

                final Map<String, Object> frame = new LinkedHashMap<String, Object>();
                frame.put("@context", rval.get("@context"));
                frame.put("@type",
                        "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#Manifest");
                // make manifest the base object, embeding any referenced items
                // (e.g. the test entries)
                rval = JsonLdProcessor.frame(rval, frame, new JsonLdOptions(manifestURL));
                // compact to remove the @graph label
                manifest = JsonLdProcessor.compact(rval, frame.get("@context"),
                        new JsonLdOptions(manifestURL));
                this.tests = (List<Map<String, Object>>) Obj.get(manifest, "mf:entries",
                        "@list");

            } catch (final JsonLdError e) {
                throw new RuntimeException(e);
            }
        } else if (manifestURL.endsWith(".jsonld") || manifestURL.endsWith(".json")) {
            final Object rval = JsonUtils.fromString(manifestFile);
            if (rval instanceof Map) {
                manifest = (Map<String, Object>) rval;
                this.tests = (List<Map<String, Object>>) Obj.get(manifest, "sequence");
            } else {
                throw new RuntimeException("expected JSON manifest file result to be an Object");
            }
        } else {
            throw new RuntimeException("unknown manifest file format");
        }
    }

    private String getFile(String url) throws IOException {
        final JsonLdUrl url_ = JsonLdUrl.parse(url);
        final String fn = this.cacheDir + url_.file + "." + this.etag;
        final File f = new File(fn);

        BufferedWriter fw = null;
        BufferedReader in;
        if (f.exists()) {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        } else {
            final java.net.URLConnection conn = new java.net.URL(url).openConnection();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            f.createNewFile();
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        }

        final StringWriter sw = new StringWriter();
        final char[] str = new char[1024];
        int read;
        while ((read = in.read(str)) >= 0) {
            sw.write(str, 0, read);
            if (fw != null) {
                fw.write(str, 0, read);
            }
        }

        in.close();
        if (fw != null) {
            fw.close();
        }

        return sw.toString();
    }

    private static String getCacheDir(String url) {
        final JsonLdUrl url_ = JsonLdUrl.parse(url);
        String dir = url_.path.substring(0, url_.path.lastIndexOf("/") + 1);
        if (!FILE_SEP.equals("/")) {
            dir = dir.replace("/", FILE_SEP);
        }
        return TEMP_DIR + dir;
    }

    public List<Map<String, Object>> getTests() {
        return tests;
    }

}
