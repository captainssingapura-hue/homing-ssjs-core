package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0024 Phase P1c — documentary widget segment renderer. Dynamic-imports
 * the wrapped AppModule's JS module and calls its {@code appMain(host, params)}.
 * Per the P1c design decision, embedded widgets keep the legacy
 * {@code appMain(host, params)} shape for now (option a); migration to the
 * new {@code mountInto(branch, parent, params)} contract is opportunistic.
 *
 * @since RFC 0024 Phase P1c
 */
public record DocumentaryWidgetSegmentRenderer()
        implements DomModule<DocumentaryWidgetSegmentRenderer> {

    public static final DocumentaryWidgetSegmentRenderer INSTANCE = new DocumentaryWidgetSegmentRenderer();

    public record renderDocumentaryWidgetSegment()
            implements Exportable._Constant<DocumentaryWidgetSegmentRenderer> {}

    @Override
    public ImportsFor<DocumentaryWidgetSegmentRenderer> imports() {
        return ImportsFor.<DocumentaryWidgetSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DocumentaryWidgetSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderDocumentaryWidgetSegment()));
    }
}
