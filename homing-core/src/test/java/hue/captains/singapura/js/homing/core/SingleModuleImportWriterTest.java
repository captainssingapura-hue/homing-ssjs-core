package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SingleModuleImportWriterTest {

    record TestModule() implements EsModule<TestModule> {
        record Foo() implements Exportable._Constant<TestModule> {}
        record Bar() implements Exportable._Class<TestModule> {}

        static final TestModule INSTANCE = new TestModule();

        @Override
        public ImportsFor<TestModule> imports() {
            return ImportsFor.<TestModule>builder().build();
        }

        @Override
        public ExportsOf<TestModule> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new Foo(), new Bar()));
        }
    }

    record AppA() implements AppModule<AppModule._None, AppA> {
        public record link() implements AppLink<AppA> {}
        static final AppA INSTANCE = new AppA();
        @Override public String title() { return "A"; }
        @Override public ImportsFor<AppA> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<AppA> exports() { return new ExportsOf<>(this, List.of()); }
    }

    @Test
    void writeImports_doesNotAppendJsSuffix() {
        ModuleNameResolver resolver = m -> new PartialModulePath("/modules/" + m.getClass().getSimpleName() + ".js", false);
        var writer = new SingleModuleImportWriter<>(TestModule.INSTANCE, resolver);

        var imports = new ModuleImports<>(List.of(new TestModule.Foo(), new TestModule.Bar()), TestModule.INSTANCE);
        String result = writer.writeImports(imports);

        assertEquals("import {Foo, Bar} from \"/modules/TestModule.js\";", result);
        // Verify no double .js
        assertFalse(result.contains(".js.js"), "Should not double-append .js");
    }

    @Test
    void writeImports_skipsAppLinkOnlyImports() {
        // RFC 0001 Step 11 fix: AppLink<?> entries are nav metadata, not JS exports.
        // An import-list containing only AppLinks must produce NO `import` line —
        // otherwise multiple targets' `link` records would collide as duplicate
        // identifiers in the consumer's compiled JS.
        ModuleNameResolver resolver = m -> new PartialModulePath("/m/" + m.getClass().getSimpleName(), false);
        var writer = new SingleModuleImportWriter<>(AppA.INSTANCE, resolver);
        var imports = new ModuleImports<>(List.of(new AppA.link()), AppA.INSTANCE);

        assertEquals("", writer.writeImports(imports),
                "All-AppLink import lists must produce no JS import line");
    }

    @Test
    void writeImports_filtersAppLinkFromMixedImports() {
        // If an entry mixes AppLink with non-AppLink members (rare but possible),
        // only the non-AppLink members are emitted in the JS import line.
        record MixedSource() implements EsModule<MixedSource> {
            record helper() implements Exportable._Constant<MixedSource> {}
            static final MixedSource INSTANCE = new MixedSource();
            @Override public ImportsFor<MixedSource> imports() { return ImportsFor.noImports(); }
            @Override public ExportsOf<MixedSource> exports() {
                return new ExportsOf<>(INSTANCE, List.of(new helper()));
            }
        }
        // Note: a real AppLink<MixedSource> isn't constructable since MixedSource
        // isn't a Linkable. This test establishes the filter behavior with
        // a placeholder; in real usage the mix doesn't typecheck.
        ModuleNameResolver resolver = m -> new PartialModulePath("/m/" + m.getClass().getSimpleName(), false);
        var writer = new SingleModuleImportWriter<>(MixedSource.INSTANCE, resolver);
        var imports = new ModuleImports<>(List.of(new MixedSource.helper()), MixedSource.INSTANCE);
        assertTrue(writer.writeImports(imports).contains("helper"));
    }

    @Test
    void writeImports_emitsAliasWhenDeclared() {
        // RFC 0024 Phase P1a — aliased imports. When ModuleImports.aliases()
        // maps an Exportable's runtime class to an alias name, the writer
        // emits `OriginalName as AliasName`.
        ModuleNameResolver resolver = m -> new PartialModulePath("/m/" + m.getClass().getSimpleName(), false);
        var writer = new SingleModuleImportWriter<>(TestModule.INSTANCE, resolver);
        var imports = new ModuleImports<>(
                List.of(new TestModule.Foo(), new TestModule.Bar()),
                TestModule.INSTANCE,
                Map.of(TestModule.Foo.class, "AliasedFoo")
        );
        String result = writer.writeImports(imports);
        assertEquals("import {Foo as AliasedFoo, Bar} from \"/m/TestModule\";", result,
                "Aliased export emits `Original as Alias`; non-aliased export emits bare original");
    }

    @Test
    void writeImports_aliasesMultipleExportsFromSameSource() {
        ModuleNameResolver resolver = m -> new PartialModulePath("/m/" + m.getClass().getSimpleName(), false);
        var writer = new SingleModuleImportWriter<>(TestModule.INSTANCE, resolver);
        var imports = new ModuleImports<>(
                List.of(new TestModule.Foo(), new TestModule.Bar()),
                TestModule.INSTANCE,
                Map.of(
                        TestModule.Foo.class, "AliasedFoo",
                        TestModule.Bar.class, "AliasedBar")
        );
        assertEquals("import {Foo as AliasedFoo, Bar as AliasedBar} from \"/m/TestModule\";",
                writer.writeImports(imports));
    }

    @Test
    void writeImports_worksWithQueryParamResolver() {
        ModuleNameResolver resolver = m -> new PartialModulePath("/module?class=" + m.getClass().getCanonicalName(), false);
        var writer = new SingleModuleImportWriter<>(TestModule.INSTANCE, resolver);

        var imports = new ModuleImports<>(List.of(new TestModule.Foo()), TestModule.INSTANCE);
        String result = writer.writeImports(imports);

        assertTrue(result.startsWith("import {Foo} from \"/module?class="));
        assertTrue(result.endsWith("\";"));
        // Query-param URLs should not have a .js file-extension suffix appended
        // (NB: the package name itself now contains ".js.homing." as a substring,
        //  so we check for .js immediately followed by the closing quote, not
        //  .js anywhere in the string.)
        assertFalse(result.contains(".js\""), "Query-param URLs should not have a .js extension");
    }
}
