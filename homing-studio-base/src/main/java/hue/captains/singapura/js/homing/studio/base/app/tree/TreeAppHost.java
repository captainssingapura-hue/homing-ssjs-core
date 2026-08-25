package hue.captains.singapura.js.homing.studio.base.app.tree;

import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueHostRenderer;

import java.util.List;

/**
 * RFC 0016 — single shared {@link AppModule} that serves any registered
 * {@link ContentTree}. URL contract:
 *
 * <pre>/app?app=tree&id=&lt;tree-id&gt;[&path=&lt;branch-path&gt;]</pre>
 *
 * <p>Reuses {@link CatalogueHostRenderer} via its {@code apiUrl} prop:
 * the selfContent passes {@code apiUrl: "/tree?id=…&path=…"} so the
 * same renderer fetches the tree JSON instead of catalogue JSON. The
 * server pre-shapes the response to the catalogue JSON contract; the
 * renderer doesn't know which kind of source produced it.</p>
 *
 * @since RFC 0016
 */
@LegacyAppMain(reason = "Reuses CatalogueHostRenderer; migrates as CatalogueAppHost does.")
public record TreeAppHost() implements AppModule<TreeAppHost.Params, TreeAppHost>, SelfContent {

    record appMain() implements AppModule._AppMain<TreeAppHost.Params, TreeAppHost> {}
    public record link() implements AppLink<TreeAppHost> {}

    /**
     * @param id   ContentTree id (resolved via {@link TreeRegistry})
     * @param path optional slash-separated branch path; {@code null} / missing → root
     */
    public record Params(String id, String path) implements AppModule._Param {}

    public static final TreeAppHost INSTANCE = new TreeAppHost();

    /**
     * RFC 0051 — this app's params, read and written together.
     *
     * <p>{@code path} is optional: a tree opened at its root has none, so its
     * absence is a value rather than an error. {@code id} is required — a
     * tree host without a tree is not a page that can be rendered.</p>
     */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(java.util.Map<String, java.util.List<String>> query) {
            String id = QueryString.first(query, "id");
            if (id == null || id.isBlank()) return Decoded.missing("id");
            return Decoded.ok(new Params(id, QueryString.first(query, "path")));
        }

        @Override public java.util.Map<String, java.util.List<String>> to(Params params) {
            var out = QueryString.params();
            QueryString.put(out, "id", params.id());
            QueryString.put(out, "path", params.path());   // null is simply omitted
            return out;
        }
    };

    /** Canonical URL for a tree at a given path (or root if path is null/empty).
     *
     *  <p>Goes through {@link #CODEC} so the values are escaped: this built the
     *  query by concatenation, which produced a broken URL for any tree path
     *  containing a space or an {@code &} — silently, since nothing decoded it
     *  back to compare.</p> */
    public static String urlFor(String treeId, String path) {
        return "/app?app=" + INSTANCE.simpleName() + "&"
             + CODEC.toQueryString(new Params(treeId, (path == null || path.isEmpty()) ? null : path));
    }

    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }
    @Override public String simpleName() { return "tree"; }
    @Override public String title()      { return "tree"; }

    @Override
    public ImportsFor<TreeAppHost> imports() {
        return ImportsFor.<TreeAppHost>builder()
                .add(new ModuleImports<>(List.of(new CatalogueHostRenderer.renderCatalogueHost()),
                        CatalogueHostRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TreeAppHost> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        // The renderer takes apiUrl to override the default /catalogue endpoint.
        // We construct the /tree URL from the typed Params and hand it through.
        return List.of(
                "function appMain(rootElement) {",
                "    var apiUrl = '/tree?id=' + encodeURIComponent(params.id);",
                "    if (params.path) apiUrl += '&path=' + encodeURIComponent(params.path);",
                "    rootElement.replaceChildren(renderCatalogueHost({",
                "        catalogueId: params.id,",
                "        apiUrl:      apiUrl",
                "    }));",
                "}"
        );
    }
}
