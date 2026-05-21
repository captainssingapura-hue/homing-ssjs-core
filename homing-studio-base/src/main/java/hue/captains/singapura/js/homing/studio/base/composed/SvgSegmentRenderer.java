package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * RFC 0024 Phase P1c — SVG segment renderer. Fetches the referenced
 * SvgDoc body and inlines it inside a captioned figure. Extracted from
 * the legacy renderer per Modest File Size.
 *
 * @since RFC 0024 Phase P1c
 */
public record SvgSegmentRenderer() implements DomModule<SvgSegmentRenderer> {

    public static final SvgSegmentRenderer INSTANCE = new SvgSegmentRenderer();

    public record renderSvgSegment() implements Exportable._Constant<SvgSegmentRenderer> {}

    @Override
    public ImportsFor<SvgSegmentRenderer> imports() {
        return ImportsFor.<SvgSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SvgSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderSvgSegment()));
    }
}
