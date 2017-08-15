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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UniqueNamer.
 *
 * @author Tristan King
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class UniqueNamer {
    private final String prefix;
    private int counter;
    private Map<String, String> existing;

    /**
     * Creates a new UniqueNamer. A UniqueNamer issues unique names, keeping
     * track of any previously issued names.
     *
     * @param prefix the prefix to use ('&lt;prefix&gt;&lt;counter&gt;').
     */
    UniqueNamer(String prefix) {
        this.prefix = prefix;
        this.counter = 0;
        this.existing = new LinkedHashMap<String, String>();
    }

    /**
     * Copies this UniqueNamer.
     *
     * @return a copy of this UniqueNamer.
     */
    @Override
    @SuppressWarnings("unchecked")
    public UniqueNamer clone() {
        final UniqueNamer copy = new UniqueNamer(this.prefix);
        copy.counter = this.counter;
        copy.existing = (Map<String, String>) JsonLdUtils.clone(this.existing);
        return copy;
    }

    /**
     * Gets the new name for the given old name, where if no old name is given a
     * new name will be generated.
     *
     * @param oldName the old name to get the new name for.
     * @return the new name.
     */
    String getName(String oldName) {
        if (oldName != null && this.existing.containsKey(oldName)) {
            return this.existing.get(oldName);
        }

        final String name = this.prefix + this.counter;
        this.counter++;

        if (oldName != null) {
            this.existing.put(oldName, name);
        }

        return name;
    }

    public String getName() {
        return getName(null);
    }

    Boolean isNamed(String oldName) {
        return this.existing.containsKey(oldName);
    }

    Map<String, String> existing() {
        return existing;
    }
}