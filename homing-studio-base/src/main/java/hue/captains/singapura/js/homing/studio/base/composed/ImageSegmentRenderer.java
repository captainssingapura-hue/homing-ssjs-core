package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewerRenderer;

import java.util.List;

/**
 * RFC 0024 Phase P1c — image segment renderer. Delegates to the shared
 * {@link ImageViewerRenderer}.
 *
 * @since RFC 0024 Phase P1c
 */
public record ImageSegmentRenderer() implements DomModule<ImageSegmentRenderer> {

    public static final ImageSegmentRenderer INSTANCE = new ImageSegmentRenderer();

    public record renderImageSegment() implements Exportable._Constant<ImageSegmentRenderer> {}

    @Override
    public ImportsFor<ImageSegmentRenderer> imports() {
        return ImportsFor.<ImageSegmentRenderer>builder()
                .add(new ModuleImports<>(List.of(new ImageViewerRenderer.renderImage()),
                        ImageViewerRenderer.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_section()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ImageSegmentRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderImageSegment()));
    }
}
