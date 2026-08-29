package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record EsModuleWriter<M extends EsModule<M>>(M module, ContentProvider<M> contentProvider, ModuleNameResolver nameResolver, ExportWriter exportWriter, ImportsWriterResolver importsWriterResolver) {
    public List<String> writeModule(){
        var allImports = module.imports().getAllImports();
        // RFC 0051 — NO params const is generated. An app is HANDED its params
        // by the server, stamped into the page and passed to appMain. This used
        // to also emit a module-level const built from window.location.search,
        // leaving two answers in one module that agreed only for as long as no
        // default, redirect or coercion differed — and which one an app used
        // depended on nothing more than whether its appMain happened to declare
        // a second parameter. ParamsWriter is deleted rather than bypassed.
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
                // User's JS content.
                contentProvider.content().stream(),
                // Export statement.
                exportWriter.writeExports(module.exports()).stream()
        ).flatMap(Function.identity()).toList();
    }
}
