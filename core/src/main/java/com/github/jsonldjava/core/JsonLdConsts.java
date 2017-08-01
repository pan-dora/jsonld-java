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

/**
 * URI Constants used in the JSON-LD parser.
 *
 * @author @tristan
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class JsonLdConsts {

    private JsonLdConsts() {
    }

    private static final String RDF_SYNTAX_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDF_SCHEMA_NS = "http://www.w3.org/2000/01/rdf-schema#";
    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

    public static final String XSD_ANYTYPE = XSD_NS + "anyType";
    static final String XSD_BOOLEAN = XSD_NS + "boolean";
    static final String XSD_DOUBLE = XSD_NS + "double";
    static final String XSD_INTEGER = XSD_NS + "integer";
    public static final String XSD_FLOAT = XSD_NS + "float";
    public static final String XSD_DECIMAL = XSD_NS + "decimal";
    public static final String XSD_ANYURI = XSD_NS + "anyURI";
    static final String XSD_STRING = XSD_NS + "string";

    static final String RDF_TYPE = RDF_SYNTAX_NS + "type";
    static final String RDF_FIRST = RDF_SYNTAX_NS + "first";
    static final String RDF_REST = RDF_SYNTAX_NS + "rest";
    static final String RDF_NIL = RDF_SYNTAX_NS + "nil";
    public static final String RDF_PLAIN_LITERAL = RDF_SYNTAX_NS + "PlainLiteral";
    public static final String RDF_XML_LITERAL = RDF_SYNTAX_NS + "XMLLiteral";
    static final String RDF_OBJECT = RDF_SYNTAX_NS + "object";
    static final String RDF_LANGSTRING = RDF_SYNTAX_NS + "langString";
    static final String RDF_LIST = RDF_SYNTAX_NS + "List";

    public static final String TEXT_TURTLE = "text/turtle";
    static final String APPLICATION_NQUADS = "application/n-quads"; // https://www
    // .w3.org/TR/n-quads/#sec-mediatype

    static final String FLATTENED = "flattened";
    static final String COMPACTED = "compacted";
    static final String EXPANDED = "expanded";

    static final String ID = "@id";
    static final String DEFAULT = "@default";
    static final String GRAPH = "@graph";
    static final String CONTEXT = "@context";
    static final String PRESERVE = "@preserve";
    static final String EXPLICIT = "@explicit";
    static final String OMIT_DEFAULT = "@omitDefault";
    static final String EMBED_CHILDREN = "@embedChildren";
    static final String EMBED = "@embed";
    static final String LIST = "@list";
    static final String LANGUAGE = "@language";
    static final String INDEX = "@index";
    static final String SET = "@set";
    static final String TYPE = "@type";
    static final String REVERSE = "@reverse";
    static final String VALUE = "@value";
    static final String NULL = "@null";
    static final String NONE = "@none";
    static final String CONTAINER = "@container";
    static final String BLANK_NODE_PREFIX = "_:";
    static final String VOCAB = "@vocab";
    static final String BASE = "@base";
}
