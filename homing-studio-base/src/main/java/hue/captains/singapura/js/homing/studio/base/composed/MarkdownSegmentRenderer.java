package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.libs.MarkedJs;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0024 Phase P1c — markdown segment renderer. Parses the segment's
 * body via marked.js; assigns H1-H4 anchors matching the server's TOC
 * pattern ({@code seg-N-hM}). Extracted from the legacy renderer per
 * Modest File Size.
 *
 * @since RFC 0024 Phase P1c
 */
public record MarkdownSegmentRenderer() implements DomModule<MarkdownSegmentRenderer> {

    public static final MarkdownSegmentRenderer INSTANCE = new MarkdownSegmentRenderer();

    public record renderMarkdownSegment() implements Exportable._Constant<MarkdownSegmentRenderer> {}

    @Override
    public ImportsFor<MarkdownSegmentRenderer> imports() {
        return ImportsFor.<MarkdownSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_doc()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MarkdownSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderMarkdownSegment()));
    }
}
