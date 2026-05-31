package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.persistence.WidgetParamsCodecRegistryModule;

import java.util.List;

/**
 * {@code CodecRegistrar} — Phase 3 of the workspace-shell chrome.
 * Two-step registration of widget Params codecs:
 *
 * <ol>
 *   <li><b>Defaults (sync):</b> every spec {@code entry} gets the
 *       substrate's identity codec ({@code transformTo}/{@code transformFrom}
 *       both return {@code {}}). Covers widgets whose Params are
 *       {@code Widget._None} or otherwise round-trip-trivial — the
 *       majority.</li>
 *   <li><b>Custom (async overlay):</b> each entry in
 *       {@code spec.widgetCodecs} ({@link WidgetCodecRef}) is
 *       dynamic-imported and registered under its
 *       {@code widgetKind}, overriding the identity codec for that
 *       kind. Tested in isolation: the synchronous
 *       {@code _registerDefaults} + {@code _registerCustom} cores are
 *       directly callable.</li>
 * </ol>
 *
 * <p>Replaces the inline registration loop V1 carries at chrome lines
 * 244–297. Must run BEFORE persistence attach (Phase 4) — saved-state
 * round-trip dispatches by widget kind through this registry, so an
 * unregistered kind marks restore failed.</p>
 *
 * <p>Explicit Substrate doctrine: instance methods on INSTANCE
 * singleton; no static methods; {@code registerAll} returns
 * {@code Promise<void>} explicitly (the I/O point is the dynamic
 * imports inside).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 3
 */
public record CodecRegistrarModule() implements DomModule<CodecRegistrarModule> {

    public static final CodecRegistrarModule INSTANCE = new CodecRegistrarModule();

    /** The {@code CodecRegistrar} JS class. */
    public record CodecRegistrar() implements Exportable._Class<CodecRegistrarModule> {}

    @Override
    public ImportsFor<CodecRegistrarModule> imports() {
        return ImportsFor.<CodecRegistrarModule>builder()
                .add(new ModuleImports<>(
                        List.of(new WidgetParamsCodecRegistryModule.WidgetParamsCodecRegistry()),
                        WidgetParamsCodecRegistryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CodecRegistrarModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CodecRegistrar()));
    }
}
