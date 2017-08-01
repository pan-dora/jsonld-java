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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Obj.
 *
 * @author @tristan
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class Obj {

    private Obj() {
    }

    /**
     * Helper function for creating maps and tuning them as necessary.
     *
     * @return A new {@link Map} instance.
     */
    public static Map<String, Object> newMap() {
        return new LinkedHashMap<>(4, 0.75f);
    }

    /**
     * Helper function for creating maps and tuning them as necessary.
     *
     * @param key   A key to add to the map on creation.
     * @param value A value to attach to the key in the new map.
     * @return A new {@link Map} instance.
     */
    public static Map<String, Object> newMap(String key, Object value) {
        final Map<String, Object> result = newMap();
        result.put(key, value);
        return result;
    }

    /**
     * Used to make getting values from maps embedded in maps embedded in maps
     * easier.
     *
     * <p>TODO: roll out the loops for efficiency
     *
     * @param map  The map to get a key from
     * @param keys The list of keys to attempt to get from the map. The first key
     *             found with a non-null value is returned, or if none are found,
     *             the original map is returned.
     * @return The key from the map, or the original map if none of the keys are
     * found.
     */
    @SuppressWarnings("unchecked")
    public static Object get(Map<String, Object> map, String... keys) {
        Map<String, Object> result = map;
        for (final String key : keys) {
            result = (Map<String, Object>) map.get(key);
            // make sure we don't crash if we get a null somewhere down the line
            if (result == null) {
                return null;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Object put(Object map, String key1, Object value) {
        ((Map<String, Object>) map).put(key1, value);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Object put(Object map, String key1, String key2, Object value) {
        ((Map<String, Object>) ((Map<String, Object>) map).get(key1)).put(key2, value);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Object put(Object map, String key1, String key2, String key3, Object value) {
        ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) map).get(key1))
                .get(key2)).put(key3, value);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Object put(Object map, String key1, String key2, String key3, String key4,
                             Object value) {
        ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String,
                Object>) map)
                .get(key1)).get(key2)).get(key3)).put(key4, value);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static boolean contains(Object map, String... keys) {
        for (final String key : keys) {
            map = ((Map<String, Object>) map).get(key);
            if (map == null) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static Object remove(Object map, String k1, String k2) {
        return ((Map<String, Object>) ((Map<String, Object>) map).get(k1)).remove(k2);
    }

    /**
     * A null-safe equals check using v1.equals(v2) if they are both not null.
     *
     * @param v1 The source object for the equals check.
     * @param v2 The object to be checked for equality using the first objects
     *           equals method.
     * @return True if the objects were both null. True if both objects were not
     * null and v1.equals(v2). False otherwise.
     */
    public static boolean equals(Object v1, Object v2) {
        return v1 == null ? v2 == null : v1.equals(v2);
    }
}
