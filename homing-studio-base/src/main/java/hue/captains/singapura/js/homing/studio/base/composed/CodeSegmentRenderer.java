package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0024 Phase P1c — code segment renderer. Verbatim code listing
 * inside {@code <pre><code>}. Theme styles {@code .st-doc pre} +
 * {@code .st-doc pre code} (surface-inverted background, monospace,
 * horizontal overflow) — the segment puts the article inside an
 * {@code st-doc}-classed wrapper so those rules apply.
 *
 * <p>Extracted from the legacy {@code ComposedViewerRenderer.js} per the
 * Modest File Size doctrine.</p>
 *
 * @since RFC 0024 Phase P1c
 */
public record CodeSegmentRenderer() implements DomModule<CodeSegmentRenderer> {

    public static final CodeSegmentRenderer INSTANCE = new CodeSegmentRenderer();

    public record renderCodeSegment() implements Exportable._Constant<CodeSegmentRenderer> {}

    @Override
    public ImportsFor<CodeSegmentRenderer> imports() {
        return ImportsFor.<CodeSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_doc()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CodeSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderCodeSegment()));
    }
}
