package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.List;

/**
 * Shared concrete AppModule for reading any markdown doc by path. Downstream
 * studios just register {@code DocReader.INSTANCE} alongside their other apps;
 * no per-studio subclass needed.
 *
 * <p>URL: {@code /app?app=doc-reader&doc=<uuid>}. The reader fetches the bytes via
 * {@code /doc?id=<uuid>} (served by {@code DocGetAction} from this same module against
 * the studio's {@link hue.captains.singapura.js.homing.studio.base.DocRegistry}), renders
 * with marked.js, and builds a heading TOC sidebar.</p>
 *
 * <p>The auto-generated body uses the studio brand by default. Downstream
 * that wants a custom brand can subclass and override {@link #brandLabel()};
 * the typical case is just to use the shared instance.</p>
 */
@LegacyAppMain(reason = "Markdown body + References renderer; already informs the DocViewer chrome pattern (DocViewer.java back-ported this body in 0.0.111). Clean migration candidate.")
public record DocReader() implements AppModule<DocReader.Params, DocReader>, SelfContent {

    record appMain() implements AppModule._AppMain<DocReader.Params, DocReader> {}

    public record link() implements AppLink<DocReader> {}

    /**
     * Query parameter — the wire identity of the Doc to render. The string is the textual
     * form of a {@link java.util.UUID}; server-side parsing to a typed UUID happens in
     * {@code DocGetAction.Query} (the boundary that actually consumes it). Kept as String
     * here because the Params record drives JS-side codegen, and the JS side just forwards
     * the string into {@code /doc?id=<string>}.
     */
    public record Params(String doc) implements AppModule._Param {}

    public static final DocReader INSTANCE = new DocReader();

    @Override public Class<Params> paramsType() { return Params.class; }

    /** Generic page-kind label. {@code AppHtmlGetAction} appends the downstream
     *  studio's brand label from {@code AppMeta}, producing {@code "doc · <brand>"}.
     *  Once the doc loads, {@code DocReaderRenderer} replaces this with the
     *  doc's actual title. */
    @Override public String title() { return "doc"; }

    /** Brand label shown in the header. Override in a subclass for custom branding. */
    public String brandLabel() { return "Homing · studio"; }

    /**
     * URL the brand link navigates to. Default {@code "/"} (the framework's root
     * redirect lands the user on the studio's home app). Downstream studios with a
     * specific catalogue can override:
     *
     * <pre>{@code
     * @Override public String homeUrl() { return CatalogueAppHost.urlFor(MyHomeCatalogue.class); }
     * }</pre>
     */
    public String homeUrl() { return "/"; }

    @Override
    public ImportsFor<DocReader> imports() {
        return ImportsFor.<DocReader>builder()
                .add(new ModuleImports<>(List.of(new DocReaderRenderer.renderDocReader()),
                        DocReaderRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DocReader> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        // The brand (label + logo + homeUrl) comes from /brand — populated
        // from StudioBrand at boot. This module no longer hardcodes its own
        // brandLabel() / homeUrl() — those defaults exist as a back-compat
        // safety net for the /brand action when no StudioBrand is registered.
        //
        // RFC 0005-ext2: the breadcrumb chain is supplied by /doc-refs (typed
        // catalogue chain — root → ... → containing catalogue). We pass an
        // empty crumbsAbove here; the renderer overrides it when info.breadcrumbs
        // arrives. Studios with no catalogues registered get no chain (legacy
        // behaviour — the brand link in the header is the only nav).
        return List.of(
                "function appMain(rootElement) {",
                "    fetch(\"/brand\").then(function(r) { return r.json(); }).then(function(brand) {",
                "        rootElement.replaceChildren(renderDocReader({",
                "            docId:       params.doc,",
                "            brand:       { href: brand.homeUrl, label: brand.label, logo: brand.logo },",
                "            crumbsAbove: []",
                "        }));",
                "    });",
                "}"
        );
    }
}
