package hue.captains.singapura.js.homing.core;

import java.util.stream.Collectors;

public record SingleModuleImportWriter<M extends EsModule<M>>(
        M module, ModuleNameResolver resolver, String theme, String locale
) {

    public SingleModuleImportWriter(M module, ModuleNameResolver resolver) {
        this(module, resolver, null, null);
    }

    public String writeImports(ModuleImports<M> imports) {
        // RFC 0001 Step 11 fix: AppLink<?> entries are pure Java metadata for
        // nav generation — they don't correspond to JS-side exports. Multiple
        // targets all use the inner record name `link`, so emitting them as
        // `import { link } from "..."` would produce duplicate-identifier errors
        // in the browser. Filter them out here.
        var emittable = imports.allImports().stream()
                .filter(x -> !(x instanceof AppLink<?>))
                .toList();
        if (emittable.isEmpty()) return "";

        final String moduleName = resolver.resolve(imports.from())
                .withTheme(theme).withLocale(locale).basePath();

        // RFC 0024 Phase P1a — aliasing support. If the import declaration
        // carries an entry in ModuleImports.aliases() keyed by the export's
        // runtime class, emit `OriginalName as AliasName`. Otherwise emit
        // the bare original name (the historical behaviour, preserved for
        // every non-aliased import).
        var aliases = imports.aliases();
        return "import {"
                + emittable.stream().map(x -> {
                    String original = x.getClass().getSimpleName();
                    String alias = aliases.get(x.getClass());
                    return alias == null ? original : original + " as " + alias;
                }).collect(Collectors.joining(", "))
                + "} from \"" + moduleName + "\";";
    }
}
