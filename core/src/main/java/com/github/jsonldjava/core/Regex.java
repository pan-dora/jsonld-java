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

import java.util.regex.Pattern;

public class Regex {
    static final Pattern TRICKY_UTF_CHARS = Pattern.compile(
            // ("1.7".equals(System.getProperty("java.specification.version")) ?
            // "[\\x{10000}-\\x{EFFFF}]" :
            "[\uD800\uDC00-\uDB7F\uDFFF]" // this seems to work with jdk1.6
    );
    // for ttl
    static final Pattern PN_CHARS_BASE = Pattern.compile(
            "[a-zA-Z]|[\\u00C0-\\u00D6]|[\\u00D8-\\u00F6]|[\\u00F8-\\u02FF]|[\\u0370-\\u037D"
                    + "]|[\\u037F-\\u1FFF]|"
                    + "[\\u200C-\\u200D]|[\\u2070-\\u218F]|[\\u2C00-\\u2FEF]|[\\u3001-\\uD7FF"
                    + "]|[\\uF900-\\uFDCF]|[\\uFDF0-\\uFFFD]|"
                    + TRICKY_UTF_CHARS);
    static final Pattern PN_CHARS_U = Pattern.compile(PN_CHARS_BASE + "|[_]");
    static final Pattern PN_CHARS = Pattern
            .compile(PN_CHARS_U + "|[-0-9]|[\\u00B7]|[\\u0300-\\u036F]|[\\u203F-\\u2040]");
    static final Pattern PN_PREFIX = Pattern.compile(
            "(?:(?:" + PN_CHARS_BASE + ")(?:(?:" + PN_CHARS + "|[\\.])*(?:" + PN_CHARS + "))?)");
    static final Pattern HEX = Pattern.compile("[0-9A-Fa-f]");
    static final Pattern PN_LOCAL_ESC = Pattern
            .compile("[\\\\][_~\\.\\-!$&'\\(\\)*+,;=/?#@%]");
    static final Pattern PERCENT = Pattern.compile("%" + HEX + HEX);
    static final Pattern PLX = Pattern.compile(PERCENT + "|" + PN_LOCAL_ESC);
    static final Pattern PN_LOCAL = Pattern
            .compile("((?:" + PN_CHARS_U + "|[:]|[0-9]|" + PLX + ")(?:(?:" + PN_CHARS + "|[.]|[:]|"
                    + PLX + ")*(?:" + PN_CHARS + "|[:]|" + PLX + "))?)");
    static final Pattern PNAME_NS = Pattern.compile("((?:" + PN_PREFIX + ")?):");
    static final Pattern PNAME_LN = Pattern.compile("" + PNAME_NS + PN_LOCAL);
    static final Pattern UCHAR = Pattern.compile("\\u005Cu" + HEX + HEX + HEX + HEX
            + "|\\u005CU" + HEX + HEX + HEX + HEX + HEX + HEX + HEX + HEX);
    static final Pattern ECHAR = Pattern.compile("\\u005C[tbnrf\\u005C\"']");
    static final Pattern IRIREF = Pattern
            .compile("(?:<((?:[^\\x00-\\x20<>\"{}|\\^`\\\\]|" + UCHAR + ")*)>)");
    static final Pattern BLANK_NODE_LABEL = Pattern.compile("(?:_:((?:" + PN_CHARS_U
            + "|[0-9])(?:(?:" + PN_CHARS + "|[\\.])*(?:" + PN_CHARS + "))?))");
    private static final Pattern WS = Pattern.compile("[ \t\r\n]");
    public static final Pattern WS_0_N = Pattern.compile(WS + "*");
    public static final Pattern WS_0_1 = Pattern.compile(WS + "?");
    public static final Pattern WS_1_N = Pattern.compile(WS + "+");
    static final Pattern STRING_LITERAL_QUOTE = Pattern.compile(
            "\"(?:[^\\u0022\\u005C\\u000A\\u000D]|(?:" + ECHAR + ")|(?:" + UCHAR + "))*\"");
    static final Pattern STRING_LITERAL_SINGLE_QUOTE = Pattern
            .compile("'(?:[^\\u0027\\u005C\\u000A\\u000D]|(?:" + ECHAR + ")|(?:" + UCHAR + "))*'");
    static final Pattern STRING_LITERAL_LONG_SINGLE_QUOTE = Pattern
            .compile("'''(?:(?:(?:'|'')?[^'\\\\])|" + ECHAR + "|" + UCHAR + ")*'''");
    static final Pattern STRING_LITERAL_LONG_QUOTE = Pattern
            .compile("\"\"\"(?:(?:(?:\"|\"\")?[^\\\"\\\\])|" + ECHAR + "|" + UCHAR + ")*\"\"\"");
    static final Pattern LANGTAG = Pattern.compile("(?:@([a-zA-Z]+(?:-[a-zA-Z0-9]+)*))");
    public static final Pattern INTEGER = Pattern.compile("[+-]?[0-9]+");
    public static final Pattern DECIMAL = Pattern.compile("[+-]?[0-9]*\\.[0-9]+");
    private static final Pattern EXPONENT = Pattern.compile("[eE][+-]?[0-9]+");
    public static final Pattern DOUBLE = Pattern.compile("[+-]?(?:(?:[0-9]+\\.[0-9]*" + EXPONENT
            + ")|(?:\\.[0-9]+" + EXPONENT + ")|(?:[0-9]+" + EXPONENT + "))");
}
