package hue.captains.singapura.js.homing.studio.base.app.tree;

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

    /** Canonical URL for a tree at a given path (or root if path is null/empty). */
    public static String urlFor(String treeId, String path) {
        String base = "/app?app=" + INSTANCE.simpleName() + "&id=" + treeId;
        return (path == null || path.isEmpty()) ? base : base + "&path=" + path;
    }

    @Override public Class<Params> paramsType() { return Params.class; }
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
