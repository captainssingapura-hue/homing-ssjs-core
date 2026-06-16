package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeWidget;
import hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetMPA;

/**
 * RFC 0039 — the rigid-tree ComposedDoc viewer app. A {@link SingleWidgetMPA}
 * "fake AppModule" hosting {@link DocTreeWidget}, exactly as {@code SvgViewer}
 * hosts {@code SvgWidget}.
 *
 * <pre>/app?app=doc-tree-viewer&id=&lt;composed-doc-uuid&gt;</pre>
 *
 * <p>Parallel to the legacy {@code ComposedViewer} (which hosts
 * {@code ComposedWidget} over {@code /doc?id=}); this one renders the same
 * ComposedDoc through the rigid-tree pipeline (TOC = {@code TreeRenderer},
 * body = per-node content, selection navigates). Both coexist until RFC 0039's
 * migration flips the corpus over.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
public final class DocTreeViewer extends SingleWidgetMPA<DocTreeViewer.Params, DocTreeViewer> {

    public static final DocTreeViewer INSTANCE = new DocTreeViewer();

    private DocTreeViewer() {}

    /** @param id UUID of the ComposedDoc to render. */
    public record Params(String id) implements AppModule._Param {}

    public record appMain() implements AppModule._AppMain<Params, DocTreeViewer> {}
    public record link() implements AppLink<DocTreeViewer> {}

    @Override public String simpleName() { return "doc-tree-viewer"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "doc"; }

    @Override
    protected AppModule._AppMain<Params, DocTreeViewer> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return DocTreeWidget.INSTANCE;
    }
}
