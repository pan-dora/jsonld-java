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
 * The JsonLdOptions type as specified in the
 * <a href="http://www.w3.org/TR/json-ld-api/#the-jsonldoptions-type">JSON-LD-
 * API specification</a>.
 *
 * @author tristan
 */
public class JsonLdOptions {

    static final boolean DEFAULT_COMPACT_ARRAYS = true;

    /**
     * Constructs an instance of JsonLdOptions using an empty base.
     */
    public JsonLdOptions() {
        this("");
    }

    /**
     * Constructs an instance of JsonLdOptions using the given base.
     *
     * @param base The base IRI for the document.
     */
    public JsonLdOptions(String base) {
        this.setBase(base);
    }

    // Base options : http://www.w3.org/TR/json-ld-api/#idl-def-JsonLdOptions

    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-base
     */
    private String base = null;

    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-compactArrays
     */
    private Boolean compactArrays = DEFAULT_COMPACT_ARRAYS;
    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-expandContext
     */
    private Object expandContext = null;
    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-processingMode
     */
    private String processingMode = "json-ld-1.0";
    /**
     * http://www.w3.org/TR/json-ld-api/#widl-JsonLdOptions-documentLoader
     */
    private DocumentLoader documentLoader = new DocumentLoader();

    // Frame options : http://json-ld.org/spec/latest/json-ld-framing/

    private Boolean embed = null;
    private Boolean explicit = null;
    private Boolean omitDefault = null;

    // RDF conversion options :
    // http://www.w3.org/TR/json-ld-api/#serialize-rdf-as-json-ld-algorithm

    private Boolean useRdfType = false;
    private Boolean useNativeTypes = false;
    private boolean produceGeneralizedRdf = false;

    public Boolean getEmbed() {
        return embed;
    }

    public void setEmbed(Boolean embed) {
        this.embed = embed;
    }

    public Boolean getExplicit() {
        return explicit;
    }

    public void setExplicit(Boolean explicit) {
        this.explicit = explicit;
    }

    public Boolean getOmitDefault() {
        return omitDefault;
    }

    public void setOmitDefault(Boolean omitDefault) {
        this.omitDefault = omitDefault;
    }

    public Boolean getCompactArrays() {
        return compactArrays;
    }

    public void setCompactArrays(Boolean compactArrays) {
        this.compactArrays = compactArrays;
    }

    public Object getExpandContext() {
        return expandContext;
    }

    public void setExpandContext(Object expandContext) {
        this.expandContext = expandContext;
    }

    public String getProcessingMode() {
        return processingMode;
    }

    public void setProcessingMode(String processingMode) {
        this.processingMode = processingMode;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Boolean getUseRdfType() {
        return useRdfType;
    }

    public void setUseRdfType(Boolean useRdfType) {
        this.useRdfType = useRdfType;
    }

    public Boolean getUseNativeTypes() {
        return useNativeTypes;
    }

    public void setUseNativeTypes(Boolean useNativeTypes) {
        this.useNativeTypes = useNativeTypes;
    }

    public boolean getProduceGeneralizedRdf() {
        return this.produceGeneralizedRdf;
    }

    public void setProduceGeneralizedRdf(Boolean produceGeneralizedRdf) {
        this.produceGeneralizedRdf = produceGeneralizedRdf;
    }

    public DocumentLoader getDocumentLoader() {
        return documentLoader;
    }

    public void setDocumentLoader(DocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    // TODO: THE FOLLOWING ONLY EXIST SO I DON'T HAVE TO DELETE A LOT OF CODE,
    // REMOVE IT WHEN DONE
    public String format = null;
    protected Boolean useNamespaces = false;
    protected String outputForm = null;
}
