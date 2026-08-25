package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record EsModuleWriter<M extends EsModule<M>>(M module, ContentProvider<M> contentProvider, ModuleNameResolver nameResolver, ExportWriter exportWriter, ImportsWriterResolver importsWriterResolver) {
    public List<String> writeModule(){
        var allImports = module.imports().getAllImports();
        // RFC 0001 Step 06: only AppModules carry a Params type.
        Class<?> paramsType = (module instanceof AppModule<?, ?> app) ? app.paramsType() : Void.class;

        // RFC 0051 — an app that declares a ParamCodec is HANDED its params by
        // the server, so it must not also derive them from window.location.
        // Emitting both would leave two answers in one module: a module-level
        // const built from the URL and an appMain argument built from the
        // server's typed value. They agree today and would stop agreeing the
        // moment a redirect, a default or a coercion differed — and the one
        // the app actually used would depend on nothing more than whether its
        // appMain declared a second parameter.
        if (module instanceof AppModule<?, ?> app
                && app.paramCodec() != ParamCodec.None.INSTANCE) {
            paramsType = Void.class;
        }
        final Class<?> paramsTypeToWrite = paramsType;
        return Stream.of(
                // ES module imports — only for EsModule sources (Linkable sources go to nav).
                // SingleModuleImportWriter filters out AppLink<?> members; if all members of an
                // entry are AppLinks (the common case for nav-only imports), it returns "".
                allImports.entrySet().stream()
                        .filter(e -> e.getKey() instanceof EsModule<?>)
                        .map(e -> {
                            @SuppressWarnings({"unchecked", "rawtypes"})
                            var writer = importsWriterResolver.resolve((EsModule) e.getKey());
                            return writer.writeImports(e.getValue());
                        })
                        .filter(line -> !line.isEmpty()),
                // RFC 0001 Step 05: typed nav const for any AppLink<?> imports.
                new NavWriter(allImports).write().stream(),
                // RFC 0001 Step 06: typed params const if this AppModule declares one.
                new ParamsWriter(paramsTypeToWrite).write().stream(),
                // User's JS content.
                contentProvider.content().stream(),
                // Export statement.
                exportWriter.writeExports(module.exports()).stream()
        ).flatMap(Function.identity()).toList();
    }
}
