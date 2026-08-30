package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * RFC 0053 — serves {@link CatalogueTreeParity} as JSON at
 * {@code GET /catalogue-parity}, for the TreeView listing to render.
 *
 * <p>Read-only and derived entirely from structures already built at boot, so it
 * observes the running studio without being able to disturb it. The normalized
 * tree it walks feeds display endpoints only; {@code /cat} resolution continues to
 * run through {@code CatalogueRegistry.childIndex} untouched.</p>
 *
 * @since RFC 0053
 */
public final class CatalogueTreeParityGetAction
        implements GetAction<RoutingContext, EmptyParam.NoQuery, EmptyParam.NoHeaders, DocContent> {

    private final CatalogueRegistry registry;

    public CatalogueTreeParityGetAction(CatalogueRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, EmptyParam.NoQuery> queryStrMarshaller() {
        return ctx -> new EmptyParam.NoQuery();
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(EmptyParam.NoQuery query, EmptyParam.NoHeaders headers) {
        try {
            CatalogueTreeParity.Report report =
                    CatalogueTreeParity.of(registry.root(), registry);
            return CompletableFuture.completedFuture(
                    new DocContent(write(report), "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(
                    "catalogue-parity", "Failed to build the parity report: " + e.getMessage()));
        }
    }

    private static String write(CatalogueTreeParity.Report r) {
        var out = new StringBuilder(1024);
        out.append('{');
        out.append("\"total\":").append(r.total());
        out.append(",\"agree\":").append(r.agree());
        out.append(",\"differ\":").append(r.differ());
        out.append(",\"unplaced\":").append(r.unplaced());
        out.append(",\"byIdentity\":").append(r.byIdentity());
        out.append(",\"byStructure\":").append(r.byStructure());

        // The canonical TreeNode payload the generic TreeRenderer consumes — level,
        // segment, the resolved row, children, and nothing bespoke. Written by the
        // substrate own writer, so the interactive tree costs no per-tree-kind code.
        out.append(",\"tree\":")
           .append(new TreeNodeJsonWriter().write(r.tree(), r.catalogue().rowDisplay()));

        // The parity verdicts, joined to the rendered rows by NAME-PATH: the
        // '/'-joined segment chain the renderer now rebuilds while it draws. That
        // key survives a sibling being inserted or reordered, where the ordinal
        // path it replaced would have silently addressed a different node. The
        // ordinal path still travels for callers not yet migrated off it.
        out.append(",\"rows\":[");
        boolean first = true;
        for (CatalogueTreeParity.Row row : r.rows()) {
            if (!first) out.append(',');
            first = false;
            out.append('{');
            out.append("\"path\":[");
            for (int i = 0; i < row.indexPath().size(); i++) {
                if (i > 0) out.append(',');
                out.append(row.indexPath().get(i));
            }
            out.append(']');
            out.append(",\"namePath\":");  quote(namePathOf(row), out);
            out.append(",\"label\":");     quote(row.label(), out);
            out.append(",\"segment\":");   quote(row.segment().value(), out);
            out.append(",\"derived\":");   quote(row.derived() == null ? "" : row.derived().toUrl(), out);
            out.append(",\"authentic\":"); quote(row.authenticUrl() == null ? "" : row.authenticUrl(), out);
            out.append(",\"status\":");    quote(row.status().name(), out);
            out.append(",\"via\":");       quote(row.via().name(), out);
            out.append('}');
        }
        out.append("]}");
        return out.toString();
    }

    /**
     * A row's name-path — its derived segments, {@code '/'}-joined, {@code ""} at
     * the root. The same chain the client rebuilds from each node's segment while
     * it draws, which is what lets the two sides join without either shipping an
     * ordinal. For a catalogue vertex it is also exactly the URL after
     * {@code /cat}, because {@code CataloguePath} excludes the root's own segment.
     */
    private static String namePathOf(CatalogueTreeParity.Row row) {
        if (row.derived() == null) return "";
        var sb = new StringBuilder();
        for (var seg : row.derived().segments()) {
            if (sb.length() > 0) sb.append('/');
            sb.append(seg.value());
        }
        return sb.toString();
    }

    private static void quote(String s, StringBuilder out) {
        out.append('"');
        String v = (s == null) ? "" : s;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append('"');
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
