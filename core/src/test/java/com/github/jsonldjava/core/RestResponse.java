package com.github.jsonldjava.core;

        import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RestResponse {

    private Integer id;
    private String message;

    /**
     * Default no-args constructor needed for jaxb
     */
    public RestResponse() {
    }

    public RestResponse(Integer id, String message) {
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
