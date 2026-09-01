package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.tree.CatalogueNormalizer;
import hue.captains.singapura.js.homing.studio.base.tree.CatalogueTree;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /catalogue?id=<class-fqn>} — serves a {@link Catalogue}'s resolved data
 * as JSON for the {@code CatalogueAppHost}'s renderer to consume.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/Rfc0005Doc.md">RFC 0005</a>,
 * the catalogue's structural data is augmented server-side with all derived display
 * data: per-entry display fields (title/summary/url for Doc; name/summary/url for
 * sub-Catalogue and App entries), the breadcrumb chain (from the registry's parent
 * index), and the brand label + home URL. The renderer receives one fully-resolved
 * payload and emits HTML — no client-side resolution needed.</p>
 *
 * <p>Response shape:</p>
 * <pre>{@code
 * {
 *   "name":    "...",
 *   "summary": "...",
 *   "brand":   { "label": "...", "homeUrl": "..." },
 *   "breadcrumbs": [ { "name": "...", "url": "..." }, ... ],   // root → leaf
 *   "entries": [
 *     { "kind": "doc",       "title": "...", "summary": "...", "category": "...", "url": "/app?app=doc-reader&doc=<uuid>" },
 *     { "kind": "catalogue", "name":  "...", "summary": "...", "category": "CATALOGUE", "url": "/app?app=catalogue&id=<fqn>" },
 *     { "kind": "app",       "name":  "...", "summary": "...", "category": "APP",       "url": "/app?app=<simpleName>"       },
 *     { "kind": "plan",      "name":  "...", "summary": "...", "category": "...", "url": "/app?app=plan&id=<fqn>"           }
 *   ]
 * }
 * }</pre>
 *
 * @since RFC 0005
 */
public class CatalogueGetAction
        implements GetAction<RoutingContext, CatalogueGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    /**
     * @param id      registered catalogue's class FQN
     * @param context optional scoping tag. The flat route still carries it, but
     *                nothing reads it: it selected an augmentation slot, and the
     *                augmentation mechanism is gone. It was never part of a
     *                catalogue's identity either — RFC 0053 excludes it
     *                deliberately, because it selects framing, not subject.
     */
    public record Query(String id, String context) implements Param._QueryString {}

    private final CatalogueRegistry registry;

    /** The substrate's own writer — the tree payload costs no bespoke serialisation. */
    private static final TreeNodeJsonWriter TREE_WRITER = new TreeNodeJsonWriter();

    public CatalogueGetAction(CatalogueRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(
                ctx.request().getParam("id"),
                ctx.request().getParam("context"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        String fqn = query.id();
        if (fqn == null || fqn.isBlank()) {
            return CompletableFuture.failedFuture(
                    notFound("id", "Required query parameter 'id' was not provided"));
        }
        Class<?> raw;
        try {
            raw = Class.forName(fqn);
        } catch (ClassNotFoundException e) {
            return CompletableFuture.failedFuture(notFound(fqn, "Class not found"));
        }
        if (!Catalogue.class.isAssignableFrom(raw)) {
            return CompletableFuture.failedFuture(notFound(fqn, "Class is not a Catalogue"));
        }
        @SuppressWarnings("unchecked")
        Class<? extends Catalogue<?>> cls = (Class<? extends Catalogue<?>>) raw;
        Catalogue<?> catalogue = registry.resolve(cls);
        if (catalogue == null) {
            return CompletableFuture.failedFuture(notFound(fqn, "Catalogue not registered"));
        }
        try {
            String body = serialize(catalogue);
            return CompletableFuture.completedFuture(new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(fqn,
                    "Failed to serialise catalogue: " + e.getMessage()));
        }
    }

    String serialize(Catalogue<?> c) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"name\":")   .append(jstr(c.name())).append(',');
        sb.append("\"summary\":").append(jstr(c.summary())).append(',');

        // Brand — `logo` is the resolved SVG markup string (server-side read of
        // the typed SvgRef), or empty when no logo is configured. The renderer's
        // Brand() component falls back to the dot for empty strings.
        StudioBrand brand = registry.brand();
        String logoSvg = (brand.logo() != null) ? brand.logo().resolve().orElse("") : "";
        sb.append("\"brand\":{")
          .append("\"label\":")  .append(jstr(brand.label())).append(',')
          .append("\"logo\":")   .append(jstr(logoSvg)).append(',')
          .append("\"homeUrl\":").append(jstr(homeUrl()))
          .append("},");

        // Breadcrumbs (root → leaf). RFC 0009: prefix the visible text with
        // each catalogue's icon() glyph when non-empty.
        sb.append("\"breadcrumbs\":[");
        @SuppressWarnings("unchecked")
        Class<? extends Catalogue<?>> cClass = (Class<? extends Catalogue<?>>) c.getClass();
        List<Catalogue<?>> crumbs = registry.breadcrumbs(cClass);
        boolean firstCrumb = true;
        for (Catalogue<?> ck : crumbs) {
            if (!firstCrumb) sb.append(',');
            firstCrumb = false;
            String url = (ck.getClass() == c.getClass()) ? "" : pathUrl(ck);
            sb.append("{\"name\":").append(jstr(crumbTextOf(ck)))
              .append(",\"url\":") .append(jstr(url))
              .append('}');
        }
        sb.append("],");

        // RFC 0053 — the same catalogue as a TREE, from the normalized layer.
        // The listing draws this, and it is now the ONLY entry payload: the tile
        // grid it replaced has been retired, so the tree is the single derivation
        // of what a catalogue contains rather than the second of two.
        //
        // Normalizing THIS catalogue rather than slicing the forest keeps the
        // action free of any re-rooting: a Catalogue already knows its own
        // subCatalogues() and leaves(), so the subtree comes from the same
        // producer the boot gate checks, not from a second traversal. That is
        // the whole point of not reaching for CatalogueTreeGetAction, which
        // re-roots by slug match on the LEGACY adapter.
        //
        // treeBase is this catalogue's own path. A row's namePath is relative to
        // the subtree root, so base + '/' + namePath is the authentic URL — the
        // identity the parity walk reports 223/223 agreement on.
        sb.append("\"treeBase\":").append(jstr(pathUrl(c))).append(',');
        // Both halves of the same walk: the structure, and the rows its identities
        // resolve to. The writer is handed the projection, never the details.
        CatalogueTree ct = CatalogueNormalizer.INSTANCE.toCatalogueTree(c);
        sb.append("\"tree\":")
          .append(TREE_WRITER.write(ct.structure(), ct.rowDisplay()));

        sb.append("}");
        return sb.toString();
    }

    private static String catalogueUrl(String fqn) {
        return CatalogueAppHost.urlFor(fqn);
    }

    /**
     * RFC 0051 — the address of a catalogue node, as a path.
     *
     * <p>Every tile and crumb this action emits goes through here, so
     * catalogue navigation shows the authentic address rather than the flat
     * {@code (app, args)} form. The path comes from
     * {@link CatalogueRegistry#pathOf}, which derives it from the same
     * breadcrumb walk the crumb trail is built from — the URL and the crumb
     * cannot disagree because they are one derivation.</p>
     */
    /** RFC 0051 - the brand home link is the tree root, which is /cat. */
    private String homeUrl() {
        Catalogue<?> root = registry.root();
        return root == null ? catalogueUrl(registry.brand().homeApp().getName()) : pathUrl(root);
    }

    private String pathUrl(Catalogue<?> node) {
        CataloguePath path = registry.pathOf(node);
        return path == null ? catalogueUrl(node.getClass().getName()) : path.toUrl();
    }

    /**
     * The address of a leaf, as a path — falling back to the doc's own flat
     * URL when it has no position.
     *
     * <p>The fallback is not dead code: Law 1 gives a doc AT MOST one
     * position, not necessarily one. Docs harvested from content trees are
     * reachable and viewable without sitting in the catalogue tree, and they
     * keep their flat address.</p>
     */
    private String pathUrl(hue.captains.singapura.js.homing.studio.base.Doc doc) {
        CataloguePath path = registry.pathOf(doc);
        return path == null ? DocViewers.addressOf(doc).flat() : path.toUrl();
    }

    /**
     * The address of a bound leaf. RFC 0051 Phase 6 — the flat fallback is
     * minted from the leaf's OWN binding rather than from a doc's opinion of
     * how it opens, which is the difference the phase is for. An app leaf with
     * no content has no doc to ask at all, and needs none.
     */
    private String pathUrl(Entry.OfLeaf<?, ?, ?> leaf) {
        // Ask by BINDING, not by content. A content-less leaf — an app tile —
        // has no doc to look up, and asking by doc is what silently dropped
        // thirteen app tiles back to flat URLs when AppDoc stopped lending
        // them a Doc identity.
        CataloguePath path = registry.pathOf(leaf.nav());
        return path == null ? leaf.nav().url() : path.toUrl();
    }

    /** RFC 0009: breadcrumb crumb text — icon glyph prefix + name. */
    static String crumbTextOf(Catalogue<?> c) {
        String icon = c.icon();
        return (icon == null || icon.isEmpty()) ? c.name() : icon + " " + c.name();
    }

    // RFC 0015 Phase 6: docReaderUrl / appUrl / planUrl helpers are removed —
    // URLs now come from doc.url() polymorphism on each Doc subtype, and the
    // serializer no longer constructs per-kind URLs locally.

    private static String jstr(String v) {
        if (v == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
