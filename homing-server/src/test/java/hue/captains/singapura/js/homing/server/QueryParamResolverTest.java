package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryParamResolverTest {

    record TestModule() implements EsModule<TestModule> {
        static final TestModule INSTANCE = new TestModule();

        @Override
        public ImportsFor<TestModule> imports() {
            return ImportsFor.<TestModule>builder().build();
        }

        @Override
        public ExportsOf<TestModule> exports() {
            return new ExportsOf<>(INSTANCE, List.of());
        }
    }

    @Test
    void resolve_usesDefaultActionPath() {
        var resolver = new QueryParamResolver();
        var result = resolver.resolve(TestModule.INSTANCE);

        assertTrue(result.basePath().startsWith("/module?class="));
        assertTrue(result.basePath().contains(TestModule.class.getCanonicalName()));
        // Resolver should not append a .js file extension to the basePath.
        // (The package name itself contains ".js.homing." now, so check for
        //  a trailing .js — i.e. .js at the end of the string — not anywhere.)
        assertFalse(result.basePath().endsWith(".js"), "Query param resolver should not append .js extension");
    }

    @Test
    void resolve_isTheClassAlone() {
        // One URL per module, whoever imports it: no theme, no locale, so a
        // dynamic import(url) and a static import name the same instance.
        var result = new QueryParamResolver().resolve(TestModule.INSTANCE);
        assertEquals("/module?class=" + TestModule.class.getCanonicalName(), result.basePath());
    }

    @Test
    void resolve_usesCustomActionPath() {
        var resolver = new QueryParamResolver("/api/esmodule");
        var result = resolver.resolve(TestModule.INSTANCE);

        assertTrue(result.basePath().startsWith("/api/esmodule?class="));
    }

    @Test
    void resolve_generatesValidImportPath() {
        var resolver = new QueryParamResolver();
        var importWriter = new SingleModuleImportWriter<>(TestModule.INSTANCE, resolver);

        record Foo() implements Exportable._Constant<TestModule> {}

        var imports = new ModuleImports<>(List.of(new Foo()), TestModule.INSTANCE);
        String result = importWriter.writeImports(imports);

        // The import writer no longer appends .js, so the query-param URL is clean.
        // (Match the closing-quote form, since the package name itself contains
        //  ".js.homing." as a substring after the rename.)
        assertTrue(result.contains("from \"/module?class="));
        assertFalse(result.contains(".js\""), "Import from query-param URL should not have a .js extension");
    }
}
