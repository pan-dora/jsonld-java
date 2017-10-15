package com.github.jsonldjava.core;

import com.github.jsonldjava.utils.JsonUtils;
import org.junit.Test;

public class PersistentContextTest {

    @Test
    public void testCompaction() throws Exception {

        final Object in = JsonUtils
                .fromInputStream(getClass().getResourceAsStream("/custom/contexttest-0004.jsonld"));
        JsonLdOptions opts = new JsonLdOptions();
        opts.format = JsonLdConsts.APPLICATION_NQUADS;
        opts.persistContext = true;
        final Object compacted = JsonLdProcessor.toRDF(in, opts);

        // System.out.println("\n\nAfter compact:");
        System.out.println(compacted);
    }

}

