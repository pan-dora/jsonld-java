package com.github.jsonldjava.core;

import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;

public class NoGraphTest {

    @Test
    public void testCompact0001() throws IOException, JsonLdError {
        final Object in = JsonUtils
                .fromInputStream(getClass().getResourceAsStream("/custom/compacted-nograph-0001-in.jsonld"));

        final Object context = JsonUtils
                .fromInputStream(getClass().getResourceAsStream("/custom/compacted-nograph-0001-context.jsonld"));

        final JsonLdOptions opts = new JsonLdOptions();
        opts.setEmbed("@always");
        opts.setUseGraphKeyword(false);
        final Map<String, Object> compacted = JsonLdProcessor.compact(in, context, opts);

        final Object out = JsonUtils
                .fromInputStream(getClass().getResourceAsStream("/custom/compacted-nograph-0001-out.jsonld"));
        //assertEquals(out, compacted);
        System.out.println(JsonUtils.toPrettyString(out));
        System.out.println(JsonUtils.toPrettyString(compacted));
    }
}
