package com.github.jsonldjava.core;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JerseyApplicationTest extends JerseyTest{
    @Override
    public Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(RestService.class);
    }

    @Test
    public void testGet() {
        Response output = target("/test").request().get();
        assertEquals("should return status 200", 200, output.getStatus());
    }
}
