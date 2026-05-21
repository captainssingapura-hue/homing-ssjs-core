package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Map;

/**
 * Imports from a single source.
 *
 * <p>The source is typed as {@link Importable} — typically an
 * {@link EsModule} (the historical case, emits a JS {@code import} statement)
 * or a {@link Linkable} (specifically {@link ProxyApp}, contributes a
 * {@code nav} entry but no JS import). The type parameter is bounded
 * to {@code Importable} as of RFC 0001 Step 04.</p>
 *
 * <h2>Aliased imports (RFC 0024 Phase P1a)</h2>
 *
 * <p>The optional {@code aliases} map carries per-export rename
 * instructions: when an entry's key matches an {@link Exportable}'s
 * runtime class in {@code allImports}, the writer emits
 * {@code import { OriginalName as AliasName } from "...";}. This makes
 * multi-widget shells viable — every Widget declares the same JS-side
 * identifier {@code mountInto}, so the shell would hit a duplicate-import
 * error without aliases. The shell builds its imports with
 * {@code aliases = Map.of(SvgWidget.mountInto.class, "SvgWidget_mountInto",
 *                          ComposedWidget.mountInto.class, "ComposedWidget_mountInto",
 *                          ...)}.</p>
 *
 * <p>Backwards-compatible: the no-aliases constructor preserves every
 * existing call site. Aliases default to an empty map.</p>
 *
 * @param allImports objects to be imported from {@code from}
 * @param from       the source — an EsModule or a Linkable
 * @param aliases    optional per-export rename map — keys are the runtime
 *                   classes of entries in {@code allImports}; values are
 *                   the JS-side alias to emit. Defaults to empty.
 * @param <M>        the source type
 */
public record ModuleImports<M extends Importable>(
        List<? extends Exportable<M>> allImports,
        M from,
        Map<Class<? extends Exportable<?>>, String> aliases
) {
    /** Canonical constructor — defensive copy of aliases. */
    public ModuleImports {
        aliases = Map.copyOf(aliases);
    }

    /** Convenience: import without aliases. The backwards-compatible
     *  shape — every existing call site keeps working unchanged. */
    public ModuleImports(List<? extends Exportable<M>> allImports, M from) {
        this(allImports, from, Map.of());
    }
}
