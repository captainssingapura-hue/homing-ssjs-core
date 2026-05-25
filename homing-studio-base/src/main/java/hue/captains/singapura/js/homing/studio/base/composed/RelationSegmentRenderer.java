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
 * RFC 0019 (RelationSegment) — typed relation table segment renderer.
 *
 * <p>Renders a {@link RelationSegment}'s {@code headers + rows} payload as
 * a styled {@code <table>} inside a section wrapper. Cell and header values
 * may contain inline markdown; the renderer applies {@code marked.parseInline()}
 * so bold, italic, inline code, and {@code [label](#ref:name)} cross-references
 * all render without any extra processing.</p>
 *
 * <p>CSS tokens reused from the existing table rendering vocabulary:
 * {@code st_table}, {@code st_thead}, {@code st_th}, {@code st_td}.</p>
 *
 * <p>Extracted as its own module per the Modest File Size doctrine.</p>
 *
 * @since RFC 0019 (RelationSegment addition)
 */
public record RelationSegmentRenderer() implements DomModule<RelationSegmentRenderer> {

    public static final RelationSegmentRenderer INSTANCE = new RelationSegmentRenderer();

    public record renderRelationSegment()
            implements Exportable._Constant<RelationSegmentRenderer> {}

    @Override
    public ImportsFor<RelationSegmentRenderer> imports() {
        return ImportsFor.<RelationSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_table(),
                        new StudioStyles.st_thead(),
                        new StudioStyles.st_th(),
                        new StudioStyles.st_td()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RelationSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderRelationSegment()));
    }
}
