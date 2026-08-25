package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import hue.captains.singapura.js.homing.core.util.SimpleImportsWriterResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EsModuleWriterTest {

    record Source() implements EsModule<Source> {
        static final Source INSTANCE = new Source();
        record Greet() implements Exportable._Constant<Source> {}

        @Override public ImportsFor<Source> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Source> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new Greet()));
        }
    }

    record Consumer() implements EsModule<Consumer> {
        static final Consumer INSTANCE = new Consumer();
        record main() implements Exportable._Constant<Consumer> {}

        @Override public ImportsFor<Consumer> imports() {
            return ImportsFor.<Consumer>builder()
                    .add(new ModuleImports<>(List.of(new Source.Greet()), Source.INSTANCE))
                    .build();
        }

        @Override public ExportsOf<Consumer> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new main()));
        }
    }

    record NoExports() implements EsModule<NoExports> {
        static final NoExports INSTANCE = new NoExports();
        @Override public ImportsFor<NoExports> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<NoExports> exports() { return new ExportsOf<>(INSTANCE, List.of()); }
    }

    private final ModuleNameResolver resolver = m -> new PartialModulePath("/mod?class=" + m.getClass().getCanonicalName(), false);
    private final SimpleImportsWriterResolver importsResolver = new SimpleImportsWriterResolver(resolver);

    @Test
    void writeModule_noImportsNoExports() {
        ContentProvider<NoExports> content = () -> List.of("console.log('hello');");
        var writer = new EsModuleWriter<>(NoExports.INSTANCE, content, resolver, ExportWriter.INSTANCE, importsResolver);

        var lines = writer.writeModule();
        assertEquals(List.of("console.log('hello');"), lines);
    }

    @Test
    void writeModule_withImportsContentAndExports() {
        ContentProvider<Consumer> content = () -> List.of("const main = () => Greet;");
        var writer = new EsModuleWriter<>(Consumer.INSTANCE, content, resolver, ExportWriter.INSTANCE, importsResolver);

        var lines = writer.writeModule();

        assertEquals(3, lines.size());
        assertTrue(lines.get(0).startsWith("import {Greet} from"));
        assertEquals("const main = () => Greet;", lines.get(1));
        assertEquals("export {main};", lines.get(2));
    }

    @Test
    void writeModule_contentOnly() {
        ContentProvider<Source> content = () -> List.of("const Greet = 'hello';");
        var writer = new EsModuleWriter<>(Source.INSTANCE, content, resolver, ExportWriter.INSTANCE, importsResolver);

        var lines = writer.writeModule();
        assertEquals(2, lines.size());
        assertEquals("const Greet = 'hello';", lines.get(0));
        assertEquals("export {Greet};", lines.get(1));
    }

    @Test
    void writeModule_multiLineContent() {
        ContentProvider<Source> content = () -> List.of(
                "const Greet = () => {",
                "  return 'hi';",
                "};"
        );
        var writer = new EsModuleWriter<>(Source.INSTANCE, content, resolver, ExportWriter.INSTANCE, importsResolver);

        var lines = writer.writeModule();
        assertEquals(4, lines.size());
        assertEquals("export {Greet};", lines.getLast());
    }

    // ----- RFC 0051: a coded app is handed its params, not left to parse -----

    record Coded(String id) implements AppModule._Param {}

    /** An app with a ParamCodec — the server stamps its params into the page. */
    record CodedApp() implements AppModule<Coded, CodedApp> {
        static final CodedApp INSTANCE = new CodedApp();
        record appMain() implements AppModule._AppMain<Coded, CodedApp> {}
        @Override public String simpleName() { return "coded"; }
        @Override public String title()      { return "Coded"; }
        @Override public Class<Coded> paramsType() { return Coded.class; }
        @Override public ParamCodec<Coded> paramCodec() {
            return new ParamCodec<>() {
                @Override public Decoded<Coded> from(java.util.Map<String, List<String>> q) {
                    return Decoded.ok(new Coded(QueryString.first(q, "id")));
                }
                @Override public java.util.Map<String, List<String>> to(Coded p) {
                    return QueryString.of("id", p.id());
                }
            };
        }
        @Override public ImportsFor<CodedApp> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<CodedApp> exports() {
            return new ExportsOf<>(INSTANCE, List.<Exportable<CodedApp>>of(new appMain()));
        }
    }

    /** Same params, no codec — keeps deriving them from the URL. */
    record UncodedApp() implements AppModule<Coded, UncodedApp> {
        static final UncodedApp INSTANCE = new UncodedApp();
        record appMain() implements AppModule._AppMain<Coded, UncodedApp> {}
        @Override public String simpleName() { return "uncoded"; }
        @Override public String title()      { return "Uncoded"; }
        @Override public Class<Coded> paramsType() { return Coded.class; }
        @Override public ImportsFor<UncodedApp> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<UncodedApp> exports() {
            return new ExportsOf<>(INSTANCE, List.<Exportable<UncodedApp>>of(new appMain()));
        }
    }

    @Test
    void codedApp_doesNotAlsoDeriveParamsFromTheUrl() {
        ContentProvider<CodedApp> content = () -> List.of("function appMain(el, params) {}");
        var lines = new EsModuleWriter<>(CodedApp.INSTANCE, content, resolver,
                ExportWriter.INSTANCE, importsResolver).writeModule();

        // Two answers in one module is the failure being prevented: a const
        // built from window.location and an argument built from the server's
        // typed value, with which one wins decided by whether appMain happened
        // to declare a second parameter.
        assertTrue(lines.stream().noneMatch(l -> l.contains("const params")),
                "coded app still derives params from the URL: " + lines);
        assertTrue(lines.stream().noneMatch(l -> l.contains("URLSearchParams")), lines.toString());
    }

    @Test
    void uncodedApp_keepsDerivingParamsFromTheUrl() {
        // The migration is per-app; an app without a codec is untouched.
        ContentProvider<UncodedApp> content = () -> List.of("function appMain(el) {}");
        var lines = new EsModuleWriter<>(UncodedApp.INSTANCE, content, resolver,
                ExportWriter.INSTANCE, importsResolver).writeModule();

        assertTrue(lines.stream().anyMatch(l -> l.contains("const params")), lines.toString());
        assertTrue(lines.stream().anyMatch(l -> l.contains("URLSearchParams")), lines.toString());
    }
}
