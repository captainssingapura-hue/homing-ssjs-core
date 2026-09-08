package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeWidget;
import hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetMPA;

/**
 * The reader for any markdown-bodied Doc. Downstream studios register
 * {@code DocReader.INSTANCE} alongside their other apps; no per-studio subclass.
 *
 * <p>URL: {@code /app?app=doc-reader&doc=<uuid>}.</p>
 *
 * <h2>RFC 0059 Phase 3 — one markdown path</h2>
 *
 * <p>This app used to own a pipeline of its own: fetch the raw bytes from
 * {@code /doc?id=}, hand the whole document to {@code marked}, then sweep the
 * resulting DOM for headings to build a TOC and for {@code language-mermaid}
 * fences to draw diagrams. That renderer was 471 lines, over the effective-line
 * cap, and carried some forty baselined conformance findings. More to the point,
 * every improvement to document rendering had to be made twice — once here and
 * once in the rigid readers — and in practice was not: mermaid reached one
 * reader, the scroll-synced TOC reached the other.</p>
 *
 * <p>It is now a {@link SingleWidgetMPA} over {@link DocTreeWidget}, the same
 * widget the {@code doc-tree-viewer} hosts. The document is normalised on the
 * server by {@code MarkdownDocNormalizer} and served from {@code /doc-tree?id=},
 * so a markdown doc and a RigidDoc arrive as the same tree and render through
 * the same code. A feature now lands once.</p>
 *
 * <p><b>Nothing about this app's identity moved.</b> Same {@code simpleName},
 * same {@code ?doc=<uuid>} grammar, same {@link Params}, same {@link #CODEC},
 * same {@code AppLink} — which matters, because 168 catalogue entries across
 * four repositories name {@code DocReader.INSTANCE} and address it with
 * {@code doc}. The widget accepts that locator under its own name; see
 * {@code DocTreeWidget}'s locator block.</p>
 *
 * <p>What the reader gained: the tree TOC with keyboard navigation and
 * scroll-sync, mermaid, deep (h4–h6) headings, and section anchors that survive
 * an export with no JavaScript. What it kept: the References section and the
 * category chip, lifted into {@code DocRefsModule} so the rigid readers have
 * them too, and the floating Export HTML pill, which the widget already had.</p>
 *
 * @since homing-studio-base — RFC 0059 Phase 3 (was a bespoke marked renderer)
 */
public final class DocReader extends SingleWidgetMPA<DocReader.Params, DocReader> {

    public static final DocReader INSTANCE = new DocReader();

    private DocReader() {}

    public record appMain() implements AppModule._AppMain<Params, DocReader> {}

    public record link() implements AppLink<DocReader> {}

    /**
     * Query parameter — the wire identity of the Doc to render. The string is the
     * textual form of a {@link java.util.UUID}; parsing to a typed UUID happens at
     * the boundary that consumes it ({@code DocTreeGetAction.Query}). Kept as
     * String here because the Params record drives JS-side codegen, and the JS
     * side forwards the string into {@code /doc-tree?id=<string>}.
     */
    public record Params(String doc) implements AppModule._Param {}

    /**
     * RFC 0051 - the doc id, read and written together. Most catalogue leaves
     * open through this app, so without a codec every /cat path ending at a
     * doc renders "(no document)": a path URL has no query string for the
     * client-side parse to read.
     */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(java.util.Map<String, java.util.List<String>> query) {
            String doc = QueryString.first(query, "doc");
            if (doc == null || doc.isBlank()) return Decoded.missing("doc");
            return Decoded.ok(new Params(doc));
        }

        @Override public java.util.Map<String, java.util.List<String>> to(Params params) {
            return QueryString.of("doc", params.doc());
        }
    };

    @Override public String simpleName() { return "doc-reader"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    /** Generic page-kind label. {@code AppHtmlGetAction} appends the downstream
     *  studio's brand label from {@code AppMeta}, producing {@code "doc · <brand>"}.
     *  Once the tree loads, the widget replaces this with the doc's own title. */
    @Override public String title() { return "doc"; }

    @Override
    protected AppModule._AppMain<Params, DocReader> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return DocTreeWidget.INSTANCE;
    }
}
