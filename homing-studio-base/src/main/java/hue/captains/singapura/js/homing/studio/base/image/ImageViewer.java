package hue.captains.singapura.js.homing.studio.base.image;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.app.DocViewer;

import java.util.List;

/**
 * RFC 0020 — viewer AppModule for {@link ImageDoc}. URL contract:
 *
 * <pre>/app?app=image-viewer&id=&lt;imagedoc-uuid&gt;</pre>
 *
 * <p>Extends {@link DocViewer} so the framework's standard chrome
 * (Header + brand + breadcrumb + theme picker) is composed automatically
 * (V11 axiom). The body delegates to {@link ImageViewerRenderer}'s
 * {@code renderImage} JS function.</p>
 *
 * @since RFC 0020
 */
@LegacyAppMain(reason = "Chrome via DocViewer; tiny body (60 JS lines). Clean migration candidate.")
public final class ImageViewer extends DocViewer<ImageViewer.Params, ImageViewer> {

    public static final ImageViewer INSTANCE = new ImageViewer();

    private ImageViewer() {}

    /** @param id UUID of the ImageDoc to render. */
    public record Params(String id) implements AppModule._Param {}

    private record appMain() implements AppModule._AppMain<Params, ImageViewer> {}
    public record link() implements AppLink<ImageViewer> {}

    @Override public String simpleName() { return "image-viewer"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "image"; }

    @Override
    protected AppModule._AppMain<Params, ImageViewer> appMain() {
        return new appMain();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(
                        List.of(new ImageViewerRenderer.renderImage()),
                        ImageViewerRenderer.INSTANCE)
        );
    }

    @Override
    protected List<String> bodyJs() {
        return List.of(
                "    main.style.cssText = 'display:flex;align-items:center;justify-content:center;min-height:60vh;padding:40px;';",
                "    renderImage({ docId: params.id, host: main });"
        );
    }
}
