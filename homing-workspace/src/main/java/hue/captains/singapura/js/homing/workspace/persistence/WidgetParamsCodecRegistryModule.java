package hue.captains.singapura.js.homing.workspace.persistence;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The JS-side polymorphism boundary for {@code Widget._Param} hierarchy
 * codecs. Each widget kind registers its Params codec under its
 * {@code WidgetKind} value; the {@code WidgetInstance} codec dispatches
 * by kind discriminator on encode + decode paths.
 *
 * <h2>Why this lives in {@code homing-workspace}</h2>
 *
 * <p>The registry is referenced from inside the generated codec module
 * ({@code WorkspaceStateCodec.transformTo} dispatches via it) AND from
 * the hand-written persistence module (the facade plumbs it through
 * {@code attach({paramsCodecs:...})}). Both modules need it in their JS
 * scope. Placing it here — in the base module both depend on — avoids
 * a Maven dependency cycle (codecs ↔ persistence) and keeps the
 * registry available everywhere it's needed.</p>
 *
 * @since RFC 0034 P2 — module-scope cleanup
 */
public record WidgetParamsCodecRegistryModule()
        implements DomModule<WidgetParamsCodecRegistryModule> {

    public static final WidgetParamsCodecRegistryModule INSTANCE =
            new WidgetParamsCodecRegistryModule();

    public record WidgetParamsCodecRegistry()
            implements Exportable._Class<WidgetParamsCodecRegistryModule> {}

    @Override
    public ImportsFor<WidgetParamsCodecRegistryModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WidgetParamsCodecRegistryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WidgetParamsCodecRegistry()));
    }
}
