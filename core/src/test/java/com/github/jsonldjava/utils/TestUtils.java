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
/**
 *
 */

package com.github.jsonldjava.utils;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

/**
 * TestUtils.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class TestUtils {

    public static InputStream copyResourceToFileStream(File testDir, String resource)
            throws Exception {
        return new FileInputStream(copyResourceToFile(testDir, resource));
    }

    public static String copyResourceToFile(File testDir, String resource) throws Exception {
        String filename = resource;
        String directory = "";
        if (resource.contains("/")) {
            filename = resource.substring(resource.lastIndexOf('/'));
            directory = resource.substring(0, resource.lastIndexOf('/'));
        }
        final File nextDirectory = new File(testDir, directory);
        nextDirectory.mkdirs();
        final File nextFile = new File(nextDirectory, filename);
        nextFile.createNewFile();

        final InputStream inputStream = TestUtils.class.getResourceAsStream(resource);
        assertNotNull("Missing test resource: " + resource, inputStream);

        IOUtils.copy(inputStream, new FileOutputStream(nextFile));
        return nextFile.getAbsolutePath();
    }

    public static String join(Collection<String> list, String delim) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delim);
        }
        return builder.toString();
    }

    private TestUtils() {
    }

}
