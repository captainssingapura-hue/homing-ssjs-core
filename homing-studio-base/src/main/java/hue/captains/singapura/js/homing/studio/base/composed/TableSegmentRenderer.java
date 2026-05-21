package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.table.TableViewerRenderer;

import java.util.List;

/**
 * RFC 0024 Phase P1c — table segment renderer. Delegates to the shared
 * {@link TableViewerRenderer} so the standalone TableViewer and the
 * inline segment share rendering.
 *
 * @since RFC 0024 Phase P1c
 */
public record TableSegmentRenderer() implements DomModule<TableSegmentRenderer> {

    public static final TableSegmentRenderer INSTANCE = new TableSegmentRenderer();

    public record renderTableSegment() implements Exportable._Constant<TableSegmentRenderer> {}

    @Override
    public ImportsFor<TableSegmentRenderer> imports() {
        return ImportsFor.<TableSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(new TableViewerRenderer.renderTable()),
                        TableViewerRenderer.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TableSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderTableSegment()));
    }
}
