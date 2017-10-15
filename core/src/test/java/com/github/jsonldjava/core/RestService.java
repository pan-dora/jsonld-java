package com.github.jsonldjava.core;

import static com.github.jsonldjava.core.JsonLdConsts.APPLICATION_JSON;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@Path("/test")
public class RestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
          return Response.ok().header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                    .header("Link", "<http://json-ld.org/contexts/person.jsonld>; " +
                            "rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"")
                    .build();
    }

    @HEAD
    @Produces(MediaType.APPLICATION_JSON)
    public Response head() {
        return Response.ok().header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .header("Link", "<http://json-ld.org/contexts/person.jsonld>; " +
                        "rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json\"")
                .build();
    }
}
