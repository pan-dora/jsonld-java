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

import com.github.jsonldjava.utils.Obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JsonLdUtils.
 *
 * @author @tristan
 * @author Peter Ansell p_ansell@yahoo.com
 */
class JsonLdUtils {

    private JsonLdUtils() {
    }

    private static final int MAX_CONTEXT_URLS = 10;

    /**
     * Returns whether or not the given value is a keyword (or a keyword alias).
     *
     * @param key the value to check.
     * @return true if the value is a keyword, false if not.
     */
    static boolean isKeyword(Object key) {
        return isString(key) && ("@base".equals(key) || "@context".equals(key) || "@container"
                .equals(key)
                || "@default".equals(key) || "@embed".equals(key) || "@explicit".equals(key)
                || "@graph".equals(key)
                || "@id".equals(key) || "@index".equals(key) || "@language".equals(key) || "@list"
                .equals(key)
                || "@omitDefault".equals(key) || "@reverse".equals(key) || "@preserve".equals(key)
                || "@set".equals(key)
                || "@type".equals(key) || "@value".equals(key) || "@vocab".equals(key));
    }

    @SuppressWarnings("unchecked")
    private static Boolean deepCompare(Object v1, Object v2, Boolean listOrderMatters) {
        if (v1 == null) {
            return v2 == null;
        } else if (v2 == null) {
            return v1 == null;
        } else if (v1 instanceof Map && v2 instanceof Map) {
            final Map<String, Object> m1 = (Map<String, Object>) v1;
            final Map<String, Object> m2 = (Map<String, Object>) v2;
            if (m1.size() != m2.size()) {
                return false;
            }
            for (final String key : m1.keySet()) {
                if (!m2.containsKey(key)
                        || !deepCompare(m1.get(key), m2.get(key), listOrderMatters)) {
                    return false;
                }
            }
            return true;
        } else if (v1 instanceof List && v2 instanceof List) {
            final List<Object> l1 = (List<Object>) v1;
            final List<Object> l2 = (List<Object>) v2;
            if (l1.size() != l2.size()) {

                return false;
            }
            // used to mark members of l2 that we have already matched to avoid
            // matching the same item twice for lists that have duplicates
            final boolean alreadyMatched[] = new boolean[l2.size()];
            for (int i = 0; i < l1.size(); i++) {
                final Object o1 = l1.get(i);
                Boolean gotmatch = false;
                if (listOrderMatters) {
                    gotmatch = deepCompare(o1, l2.get(i), listOrderMatters);
                } else {
                    for (int j = 0; j < l2.size(); j++) {
                        if (!alreadyMatched[j] && deepCompare(o1, l2.get(j), listOrderMatters)) {
                            alreadyMatched[j] = true;
                            gotmatch = true;
                            break;
                        }
                    }
                }
                if (!gotmatch) {
                    return false;
                }
            }
            return true;
        } else {
            return v1.equals(v2);
        }
    }

    static Boolean deepCompare(Object v1, Object v2) {
        return deepCompare(v1, v2, false);
    }

    private static boolean deepContains(List<Object> values, Object value) {
        for (final Object item : values) {
            if (deepCompare(item, value, false)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    static void mergeValue(Map<String, Object> obj, String key, Object value) {
        if (obj == null) {
            return;
        }
        List<Object> values = (List<Object>) obj.get(key);
        if (values == null) {
            values = new ArrayList<>();
            obj.put(key, values);
        }
        if ("@list".equals(key)
                || (value instanceof Map && ((Map<String, Object>) value).containsKey("@list"))
                || !deepContains(values, value)) {
            values.add(value);
        }
    }

    @SuppressWarnings("unchecked")
    static void laxMergeValue(Map<String, Object> obj, String key, Object value) {
        if (obj == null) {
            return;
        }
        List<Object> values = (List<Object>) obj.get(key);
        if (values == null) {
            values = new ArrayList<>();
            obj.put(key, values);
        }
        // if ("@list".equals(key)
        // || (value instanceof Map && ((Map<String, Object>)
        // value).containsKey("@list"))
        // || !deepContains(values, value)
        // ) {
        values.add(value);
        // }
    }

    static boolean isAbsoluteIri(String value) {
        // TODO: this is a bit simplistic!
        return value.contains(":");
    }

    /**
     * Returns true if the given value is a subject with properties.
     *
     * @param v the value to check.
     * @return true if the value is a subject with properties, false if not.
     */
    @SuppressWarnings("unchecked")
    static boolean isNode(Object v) {
        // Note: A value is a subject if all of these hold true:
        // 1. It is an Object.
        // 2. It is not a @value, @set, or @list.
        // 3. It has more than 1 key OR any existing key is not @id.
        return v instanceof Map && !(((Map) v).containsKey("@value") || ((Map) v).containsKey(
                "@set") || ((Map) v)
                .containsKey("@list")) && (((Map<String, Object>) v).size() > 1 || !((Map) v)
                .containsKey("@id"));
    }

    /**
     * Returns true if the given value is a subject reference.
     *
     * @param v the value to check.
     * @return true if the value is a subject reference, false if not.
     */
    @SuppressWarnings("unchecked")
    static boolean isNodeReference(Object v) {
        // Note: A value is a subject reference if all of these hold true:
        // 1. It is an Object.
        // 2. It has a single key: @id.
        return (v instanceof Map && ((Map<String, Object>) v).size() == 1
                && ((Map<String, Object>) v).containsKey("@id"));
    }

    // TODO: fix this test
    static boolean isRelativeIri(String value) {
        return !(isKeyword(value) || isAbsoluteIri(value));
    }

    /**
     * Removes the @preserve keywords as the last step of the framing algorithm.
     *
     * @param ctx the active context used to compact the input.
     * @param input the framed, compacted output.
     * @param opts the compaction options used.
     * @return the resulting output.
     * @throws JsonLdError JsonLdError
     */
    @SuppressWarnings("unchecked")
    static Object removePreserve(Context ctx, Object input, JsonLdOptions opts) throws JsonLdError {
        // recurse through arrays
        if (isArray(input)) {
            final List<Object> output = new ArrayList<>();
            for (final Object i : (List<Object>) input) {
                final Object result = removePreserve(ctx, i, opts);
                // drop nulls from arrays
                if (result != null) {
                    output.add(result);
                }
            }
            input = output;
        } else if (isObject(input)) {
            // remove @preserve
            if (((Map<String, Object>) input).containsKey("@preserve")) {
                if ("@null".equals(((Map<String, Object>) input).get("@preserve"))) {
                    return null;
                }
                return ((Map<String, Object>) input).get("@preserve");
            }

            // skip @values
            if (isValue(input)) {
                return input;
            }

            // recurse through @lists
            if (isList(input)) {
                ((Map<String, Object>) input).put("@list",
                        removePreserve(ctx, ((Map<String, Object>) input).get("@list"), opts));
                return input;
            }

            // recurse through properties
            for (final String prop : ((Map<String, Object>) input).keySet()) {
                Object result = removePreserve(ctx, ((Map<String, Object>) input).get(prop), opts);
                final String container = ctx.getContainer(prop);
                if (opts.getCompactArrays() && isArray(result)
                        && ((List<Object>) result).size() == 1 && container == null) {
                    result = ((List<Object>) result).get(0);
                }
                ((Map<String, Object>) input).put(prop, result);
            }
        }
        return input;
    }

    /**
     * Compares two strings first based on length and then lexicographically.
     *
     * @param a the first string.
     * @param b the second string.
     * @return -1 if a < b, 1 if a > b, 0 if a == b.
     */
    static int compareShortestLeast(String a, String b) {
        if (a.length() < b.length()) {
            return -1;
        } else if (b.length() < a.length()) {
            return 1;
        }
        return Integer.signum(a.compareTo(b));
    }

    /**
     * Compares two JSON-LD values for equality. Two JSON-LD values will be
     * considered equal if:
     *
     * <p>1. They are both primitives of the same type and value. 2. They are
     * both @values with the same @value, @type, and @language, OR 3. They both
     * have @ids they are the same.
     *
     * @param v1 the first value.
     * @param v2 the second value.
     * @return true if v1 and v2 are considered equal, false if not.
     */
    @SuppressWarnings("unchecked")
    static boolean compareValues(Object v1, Object v2) {
        return v1.equals(v2) || isValue(v1) && isValue(v2) && Obj.equals(((Map<String, Object>)
                v1).get("@value"), (
                (Map<String, Object>) v2).get("@value")) && Obj.equals(((Map<String, Object>) v1)
                .get("@type"), (
                (Map<String, Object>) v2).get("@type")) && Obj.equals(((Map<String, Object>) v1)
                .get(
                "@language"), ((Map<String, Object>) v2).get("@language")) && Obj.equals((
                (Map<String, Object>) v1)
                .get("@index"), ((Map<String, Object>) v2).get("@index")) || (v1 instanceof Map
                && ((Map<String,
                Object>) v1).containsKey("@id")) && (v2 instanceof Map && ((Map<String, Object>)
                v2).containsKey(
                "@id")) && ((Map<String, Object>) v1).get("@id").equals(((Map<String, Object>)
                v2).get("@id"));

    }

    /**
     * Returns true if the given value is a blank node.
     *
     * @param v the value to check.
     * @return true if the value is a blank node, false if not.
     */
    static boolean isBlankNode(Object v) {
        // Note: A value is a blank node if all of these hold true:
        // 1. It is an Object.
        // 2. If it has an @id key its value begins with '_:'.
        // 3. It has no keys OR is not a @value, @set, or @list.
        if (v instanceof Map) {
            if (((Map) v).containsKey("@id")) {
                return ((String) ((Map) v).get("@id")).startsWith("_:");
            } else {
                return ((Map) v).size() == 0 || !(((Map) v).containsKey("@value")
                        || ((Map) v).containsKey("@set") || ((Map) v).containsKey("@list"));
            }
        }
        return false;
    }

    static Object clone(Object value) {
        // throws
        // CloneNotSupportedException {
        Object rval = null;
        if (value instanceof Cloneable) {
            try {
                rval = value.getClass().getMethod("clone").invoke(value);
            } catch (final Exception e) {
                rval = e;
            }
        }
        if (rval == null || rval instanceof Exception) {
            // the object wasn't cloneable, or an error occured
            if (value == null || value instanceof String || value instanceof Number
                    || value instanceof Boolean) {
                // strings numbers and booleans are immutable
                rval = value;
            } else {
                // TODO: making this throw runtime exception so it doesn't have
                // to be caught
                // because simply it should never fail in the case of JSON-LD
                // and means that
                // the input JSON-LD is invalid
                throw new RuntimeException(new CloneNotSupportedException(
                        (rval != null ? ((Exception) rval).getMessage() : "")));
            }
        }
        return rval;
    }

    /**
     * Returns true if the given value is a JSON-LD Array.
     *
     * @param v the value to check.
     * @return boolean
     */
    private static Boolean isArray(Object v) {
        return (v instanceof List);
    }

    /**
     * Returns true if the given value is a JSON-LD List.
     *
     * @param v the value to check.
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    static Boolean isList(Object v) {
        return (v instanceof Map && ((Map<String, Object>) v).containsKey("@list"));
    }

    /**
     * Returns true if the given value is a JSON-LD Object.
     *
     * @param v the value to check.
     * @return boolean
     */
    static Boolean isObject(Object v) {
        return (v instanceof Map);
    }

    /**
     * Returns true if the given value is a JSON-LD value.
     *
     * @param v the value to check.
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    static Boolean isValue(Object v) {
        return (v instanceof Map && ((Map<String, Object>) v).containsKey("@value"));
    }

    /**
     * Returns true if the given value is a JSON-LD string.
     *
     * @param v the value to check.
     * @return boolean
     */
    static Boolean isString(Object v) {
        // TODO: should this return true for arrays of strings as well?
        return (v instanceof String);
    }
}