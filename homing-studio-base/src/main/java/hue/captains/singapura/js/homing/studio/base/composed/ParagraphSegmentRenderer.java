package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * Inline renderer for {@link ParagraphSegment} — the plain lines joined into one
 * regular, unformatted {@code <p>}. Pure inline content (no fetch), so it needs
 * only the shared section style.
 */
public record ParagraphSegmentRenderer() implements DomModule<ParagraphSegmentRenderer> {

    public static final ParagraphSegmentRenderer INSTANCE = new ParagraphSegmentRenderer();

    public record renderParagraphSegment() implements Exportable._Constant<ParagraphSegmentRenderer> {}

    @Override
    public ImportsFor<ParagraphSegmentRenderer> imports() {
        return ImportsFor.<ParagraphSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ParagraphSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderParagraphSegment()));
    }
}
