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
 * JsonLdTripleCallback.
 *
 * @author Tristan
 *
 * <p>TODO: in the JSONLD RDF API the callback we're representing here is
 * QuadCallback which takes a list of quads (subject, predicat, object,
 * graph). for the moment i'm just going to use the dataset provided by
 * toRDF but this should probably change in the future
 */
public interface JsonLdTripleCallback {

    /**
     * Construct output based on internal RDF dataset format.
     *
     * @param dataset The format of the dataset is a Map with the following
     * structure: { GRAPH_1: [ TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ],
     * GRAPH_2: [ TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ], ... GRAPH_N: [
     * TRIPLE_1, TRIPLE_2, ..., TRIPLE_N ] }
     *
     * <p>GRAPH: Is the graph name/IRI. if no graph is present for a
     * triple, it will be listed under the "@default" graph TRIPLE:
     * Is a map with the following structure: { "subject" : SUBJECT
     * "predicate" : PREDICATE "object" : OBJECT }
     *
     * <p>Each of the values in the triple map are also maps with the
     * following key-value pairs: "value" : The value of the node.
     * "subject" can be an IRI or blank node id. "predicate" should
     * only ever be an IRI "object" can be and IRI or blank node id,
     * or a literal value (represented as a string) "type" : "IRI" if
     * the value is an IRI or "blank node" if the value is a blank
     * node. "object" can also be "literal" in the case of literals.
     * The value of "object" can also contain the following optional
     * key-value pairs: "language" : the language value of a string
     * literal "datatype" : the datatype of the literal. (if not set
     * will default to XSD:string, if set to null, null will be
     * used).
     * @return the resulting RDF object in the desired format
     */
    Object call(RDFDataset dataset);
}
