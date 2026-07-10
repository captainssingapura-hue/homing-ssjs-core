package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * Renders a node's optional <b>caption</b> — the highlighted header
 * ({@code <h2 class="st_section_title">}) drawn above the node's body.
 *
 * <p>Studio-level: it owns the {@code st_section_title} style, so the substrate
 * doc-tree renderer (homing-core-js) stays style-free — the widget injects this
 * as the {@code renderCaption} callback, exactly as it injects {@code renderContent}
 * for segments.</p>
 */
public record CaptionRenderer() implements DomModule<CaptionRenderer> {

    public static final CaptionRenderer INSTANCE = new CaptionRenderer();

    public record renderCaption() implements Exportable._Constant<CaptionRenderer> {}

    @Override
    public ImportsFor<CaptionRenderer> imports() {
        return ImportsFor.<CaptionRenderer>builder()
                .add(new ModuleImports<>(List.of(new StudioStyles.st_section_title()),
                        StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CaptionRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderCaption()));
    }
}
