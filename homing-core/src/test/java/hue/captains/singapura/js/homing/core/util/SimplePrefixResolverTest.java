package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimplePrefixResolverTest {

    record DummyModule() implements EsModule<DummyModule> {
        @Override
        public ImportsFor<DummyModule> imports() {
            return ImportsFor.<DummyModule>builder().build();
        }

        @Override
        public ExportsOf<DummyModule> exports() {
            return new ExportsOf<>(new DummyModule(), List.of());
        }
    }

    @Test
    void resolve_includesJsSuffix() {
        var resolver = new SimplePrefixResolver("/test/");
        var result = resolver.resolve(new DummyModule());

        assertTrue(result.basePath().startsWith("/test/"));
        assertTrue(result.basePath().endsWith(".js"), "SimplePrefixResolver should append .js");
    }

    @Test
    void resolve_convertsDotsToSlashes() {
        var resolver = new SimplePrefixResolver("/assets/");
        var result = resolver.resolve(new DummyModule());

        String expected = "/assets/" +
                DummyModule.class.getCanonicalName().replace(".", "/") + ".js";
        assertEquals(expected, result.basePath());
    }
}
