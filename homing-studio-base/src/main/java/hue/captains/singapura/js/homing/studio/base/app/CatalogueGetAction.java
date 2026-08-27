package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import hue.captains.singapura.js.homing.studio.base.DocContent;
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
     * @param context optional scoping tag — when present, picks a different
     *                augmentation slot in the {@link CatalogueAugmentation}
     *                map so the same catalogue class can project multiple
     *                framework-managed variants (e.g. per-studio diagnostics)
     */
    public record Query(String id, String context) implements Param._QueryString {}

    private final CatalogueRegistry registry;
    /**
     * Per-(class, context) augmentation. Reserved for framework-managed
     * injections (e.g. RFC 0014 diagnostics tile pyramid). Empty for normal
     * serving — back-compat constructor passes {@code Map.of()}.
     */
    private final Map<CatalogueAugmentation.AugKey, CatalogueAugmentation> augmentations;

    public CatalogueGetAction(CatalogueRegistry registry) {
        this(registry, Map.of());
    }

    public CatalogueGetAction(CatalogueRegistry registry,
                              Map<CatalogueAugmentation.AugKey, CatalogueAugmentation> augmentations) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.augmentations = Map.copyOf(Objects.requireNonNull(augmentations, "augmentations"));
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
            String body = serialize(catalogue, query.context());
            return CompletableFuture.completedFuture(new DocContent(body, "application/json; charset=utf-8"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(notFound(fqn,
                    "Failed to serialise catalogue: " + e.getMessage()));
        }
    }

    /** Back-compat overload used by tests that don't exercise the context scoping. */
    String serialize(Catalogue<?> c) { return serialize(c, null); }

    String serialize(Catalogue<?> c, String context) {
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

        // Entries — RFC 0005-ext2 Option A: typed sub-catalogues first
        // (rendered as catalogue cards), then leaves (Doc / App / Plan).
        // Within each group the catalogue's declared order is preserved.
        // RFC 0014: when an augmentation has replace=true, the typed entries
        // are suppressed and only the synthetic ones render. Used to project
        // per-context variants of a catalogue (e.g. per-studio diagnostics).
        @SuppressWarnings("unchecked")
        Class<? extends Catalogue<?>> cKey = (Class<? extends Catalogue<?>>) c.getClass();
        CatalogueAugmentation aug = augmentations.get(new CatalogueAugmentation.AugKey(cKey, context));
        boolean suppressTyped = aug != null && aug.replace();

        sb.append("\"entries\":[");
        boolean firstEntry = true;

        // ---- Sub-catalogues ----
        if (!suppressTyped) for (Catalogue<?> child : c.subCatalogues()) {
            if (!firstEntry) sb.append(',');
            firstEntry = false;
            // RFC 0009: per-instance badge (default "CATALOGUE", may be
            // overridden to "STUDIO" / "DOCTRINE" / etc.). The icon glyph is
            // a navigation aid in breadcrumbs only — the card's category text
            // is plain. Renderers pick a CSS badge class from the category.
            sb.append("{\"kind\":\"catalogue\",")
              .append("\"name\":")    .append(jstr(child.name())).append(',')
              .append("\"summary\":") .append(jstr(child.summary())).append(',')
              .append("\"category\":").append(jstr(child.badge())).append(',')
              .append("\"url\":")     .append(jstr(pathUrl(child)))
              .append('}');
        }

        // ---- Leaves ----
        if (!suppressTyped) for (Entry<?> e : c.leaves()) {
            if (!firstEntry) sb.append(',');
            firstEntry = false;
            switch (e) {
                // RFC 0051 Phase 6 — a bound leaf. Its tile reads exactly what
                // the OfDoc tile reads, from the same places, so the payload is
                // unchanged; what differs is where the URL comes from. The doc
                // is no longer asked how it opens — the placement already said.
                case Entry.OfLeaf<?, ?, ?> leaf -> {
                    Doc content = leaf.content();
                    String kind = (content != null) ? content.kind() : "app";
                    String titleKey = "doc".equals(kind) ? "\"title\"" : "\"name\"";
                    String name     = (content != null) ? content.title()    : leaf.nav().name();
                    String summary  = (content != null) ? content.summary()  : leaf.nav().summary();
                    String category = (content != null) ? content.category() : "APP";
                    sb.append('{')
                      .append("\"kind\":")    .append(jstr(kind)).append(',')
                      .append(titleKey)       .append(':').append(jstr(name)).append(',')
                      .append("\"summary\":") .append(jstr(summary)).append(',')
                      .append("\"category\":").append(jstr(category)).append(',')
                      .append("\"url\":")     .append(jstr(pathUrl(leaf)))
                      .append('}');
                }
                case Entry.OfDoc<?, ?>(Doc d) -> {
                    // RFC 0015 Phase 3b — dispatch via Doc's typed kind() + url().
                    // Field-key asymmetry preserved: frontend renderer uses entry.title
                    // for kind="doc"; entry.name for everything else (plan / app /
                    // studio / catalogue). Phase 6 will unify the schema once the
                    // frontend dispatch is updated.
                    String kind = d.kind();
                    String titleKey = "doc".equals(kind) ? "\"title\"" : "\"name\"";
                    sb.append('{')
                      .append("\"kind\":")    .append(jstr(kind)).append(',')
                      .append(titleKey)       .append(':').append(jstr(d.title())).append(',')
                      .append("\"summary\":") .append(jstr(d.summary())).append(',')
                      .append("\"category\":").append(jstr(d.category())).append(',')
                      .append("\"url\":")     .append(jstr(pathUrl(d)))
                      .append('}');
                }
                // RFC 0015 Phase 6: OfApp / OfPlan branches removed. Plans
                // and Navigables now flow through OfDoc(PlanDoc/AppDoc) above,
                // where doc.kind() emits "plan" / "app" and doc.url() emits
                // the right URL. The two cases collapse into one.
                case Entry.OfIllustration<?>(CatalogueIllustration illustration) -> {
                    // Specialized in-place decoration — markdown rendered as a
                    // hero block by the frontend. No URL, no addressing, no
                    // catalogue-tile shape; the renderer treats kind="illustration"
                    // as a special entry that doesn't go through the Card path.
                    sb.append("{\"kind\":\"illustration\",")
                      .append("\"body\":").append(jstr(illustration.body()))
                      .append('}');
                }
                case Entry.OfStudio<?, ?>(StudioProxy<?> proxy) -> {
                    // RFC 0011: studio-kind card. URL points at the wrapped
                    // source L0's own catalogue page; the proxy's display
                    // fields (name / summary / badge / icon) render the tile.
                    sb.append("{\"kind\":\"studio\",")
                      .append("\"name\":")    .append(jstr(proxy.icon().isEmpty()
                                                      ? proxy.name()
                                                      : proxy.icon() + " " + proxy.name())).append(',')
                      .append("\"summary\":") .append(jstr(proxy.summary())).append(',')
                      .append("\"category\":").append(jstr(proxy.badge())).append(',')
                      .append("\"url\":")     .append(jstr(pathUrl(proxy.source())))
                      .append('}');
                }
            }
        }

        // ---- Synthetic entries (framework augmentation; e.g. RFC 0014 diagnostics injection).
        // Appended last so the studio's declared tiles stay where the studio put them
        // (when replace=false). When replace=true, the typed entries above were
        // suppressed and these are the only entries rendered.
        if (aug != null) {
            for (SyntheticEntry s : aug.entries()) {
                if (!firstEntry) sb.append(',');
                firstEntry = false;
                sb.append('{')
                  .append("\"kind\":")    .append(jstr(s.kind())).append(',')
                  .append("\"name\":")    .append(jstr(s.name())).append(',')
                  .append("\"summary\":") .append(jstr(s.summary())).append(',')
                  .append("\"category\":").append(jstr(s.category())).append(',')
                  .append("\"url\":")     .append(jstr(s.url()))
                  .append('}');
            }
        }

        sb.append("]}");
        return sb.toString();
    }

    private static String catalogueUrl(String fqn) {
        return "/app?app=catalogue&id=" + fqn;
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
        return path == null ? doc.url() : path.toUrl();
    }

    /**
     * The address of a bound leaf. RFC 0051 Phase 6 — the flat fallback is
     * minted from the leaf's OWN binding rather than from a doc's opinion of
     * how it opens, which is the difference the phase is for. An app leaf with
     * no content has no doc to ask at all, and needs none.
     */
    private String pathUrl(Entry.OfLeaf<?, ?, ?> leaf) {
        Doc content = leaf.content();
        CataloguePath path = (content != null) ? registry.pathOf(content) : null;
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
