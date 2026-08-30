package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.List;

/**
 * Single shared {@link AppModule} that serves any registered {@link Catalogue}.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/Rfc0005Doc.md">RFC 0005</a>
 * D1, catalogues are served through one {@code CatalogueAppHost} rather than one
 * AppModule per catalogue. URL contract:</p>
 *
 * <pre>/app?app=catalogue&id=&lt;class-fqn&gt;</pre>
 *
 * <p>The selfContent emits a JS body that fetches the resolved catalogue payload
 * from {@link CatalogueGetAction} ({@code /catalogue?id=<id>}) and invokes
 * {@link CatalogueHostRenderer}. Server-side resolution + client-side fetch =
 * one AppHost serving every registered catalogue.</p>
 *
 * @since RFC 0005
 */
@LegacyAppMain(reason = "Studio chrome / catalogue tile-grid; migration tracked as RFC 0024 successor work to the canonical spike.")
public record CatalogueAppHost() implements AppModule<CatalogueAppHost.Params, CatalogueAppHost>, SelfContent {

    record appMain() implements AppModule._AppMain<CatalogueAppHost.Params, CatalogueAppHost> {}

    public record link() implements AppLink<CatalogueAppHost> {}

    /**
     * @param id      class FQN of the {@link Catalogue} to render (server
     *                resolves via {@link CatalogueRegistry})
     * @param context optional framework-managed scoping tag — forwarded to
     *                {@link CatalogueGetAction} so the same catalogue class
     *                can render context-scoped variants (RFC 0014 per-studio
     *                diagnostics). {@code null} / missing = the catalogue's
     *                unscoped page.
     */
    public record Params(String id, String context) implements AppModule._Param {}

    public static final CatalogueAppHost INSTANCE = new CatalogueAppHost();

    /**
     * The typed identity of a catalogue: this host, opened on that catalogue's
     * class (RFC 0053). Global, because the class is — it owes nothing to where
     * the catalogue sits, so the identity survives being grafted under a
     * different parent.
     *
     * <p>{@code context} is left null deliberately. It is a REFINEMENT — it
     * selects framing, not subject — and identity is the pair that determines
     * the position, nothing finer and nothing coarser.</p>
     *
     * <p>Lives here because this host is what a catalogue is opened by, so it
     * is the one place that should know how a catalogue is named. Two places
     * minting this key would be exactly the duplication RFC 0053 removes.</p>
     */
    public static NavKey identityFor(Catalogue<?> catalogue) {
        return identityFor(catalogue.getClass().getName());
    }

    /** As {@link #identityFor(Catalogue)}, from the class name a flat URL carries. */
    public static NavKey identityFor(String catalogueFqn) {
        return new NavKey(CatalogueAppHost.class, new Params(catalogueFqn, null));
    }

    /**
     * Build the canonical URL serving the given {@link Catalogue}. Used by any consumer
     * (other AppModules, downstream apps) that needs to link to a catalogue without
     * hand-building the path.
     */
    public static String urlFor(Class<? extends Catalogue<?>> catalogueClass) {
        return urlFor(catalogueClass.getName(), null);
    }

    /** As above, for a caller holding the FQN as a string rather than the class. */
    public static String urlFor(String catalogueFqn) {
        return urlFor(catalogueFqn, null);
    }

    /** As above, carrying the {@code context} refinement. */
    public static String urlFor(String catalogueFqn, String context) {
        return AppUrl.flat(INSTANCE, new Params(catalogueFqn, context));
    }

    /**
     * RFC 0051 — the catalogue page's params, read and written together.
     *
     * <p>This app is what a {@code /cat/...} path renders, and a path URL
     * carries no query string at all — so without a codec the page would have
     * nothing to read and would render "no catalogue specified". Stamping is
     * not an optimisation here; it is what makes the path route possible.</p>
     *
     * <p>{@code context} is optional: it selects a diagnostics variant of the
     * same catalogue, and its absence is the ordinary case.</p>
     */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(java.util.Map<String, java.util.List<String>> query) {
            String id = QueryString.first(query, "id");
            if (id == null || id.isBlank()) return Decoded.missing("id");
            return Decoded.ok(new Params(id, QueryString.first(query, "context")));
        }

        @Override public java.util.Map<String, java.util.List<String>> to(Params params) {
            var out = QueryString.params();
            QueryString.put(out, "id", params.id());
            QueryString.put(out, "context", params.context());
            return out;
        }
    };

    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override public String simpleName() { return "catalogue"; }

    /** Page-kind label. {@code AppHtmlGetAction} appends the downstream brand;
     *  the renderer refines it to {@code "<catalogue-name> · <brand>"} on load. */
    @Override public String title() { return "catalogue"; }

    @Override
    public ImportsFor<CatalogueAppHost> imports() {
        return ImportsFor.<CatalogueAppHost>builder()
                .add(new ModuleImports<>(List.of(new CatalogueHostRenderer.renderCatalogueHost()),
                        CatalogueHostRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CatalogueAppHost> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                // RFC 0051 - params arrive from the server. Required here, not
                // merely preferred: a /cat path URL has no query string for a
                // client-side parse to read.
                // RFC 0051 — chrome is handed in, never built. The renderer
                // takes these crumbs whole; it has no business composing a
                // statement about where this page sits.
                "function appMain(rootElement, params, chrome) {",
                "    rootElement.replaceChildren(renderCatalogueHost({",
                "        catalogueId: params.id,",
                "        context:     params.context,",
                "        crumbs:      chrome && chrome.crumbs",
                "    }));",
                "}"
        );
    }
}
