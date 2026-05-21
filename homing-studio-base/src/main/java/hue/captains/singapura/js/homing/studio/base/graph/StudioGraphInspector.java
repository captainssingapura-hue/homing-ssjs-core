package hue.captains.singapura.js.homing.studio.base.graph;

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
 * RFC 0014 — diagnostic AppModule that renders the live {@link StudioGraph}
 * as HTML on the front-end. URL: {@code /app?app=studio-graph}.
 *
 * <p>Conditionally wired by {@code Bootstrap.compose()} only when
 * {@code RuntimeParams.diagnosticsEnabled()} is true. When disabled, both
 * this app and its backing {@code StudioGraphMarkdownAction} are absent
 * from the running server.</p>
 *
 * <p>Optional query: {@code &root=<class-FQN>} to dump a specific subtree
 * rather than the Bootstrap root.</p>
 *
 * <p>Rendering pipeline: app shell loads → JS fetches {@code /graph-md} →
 * markdown is parsed with the bundled marked.js → HTML is inserted into the
 * page. No fetched data is held server-side after the request — the graph is
 * built once at boot and reused.</p>
 */
@LegacyAppMain(reason = "Diagnostics viewer; framework-author scope. Low pressure; may move to homing-diagnostics module per RFC 0023 D7.")
public record StudioGraphInspector()
        implements AppModule<StudioGraphInspector.Params, StudioGraphInspector>, SelfContent {

    record appMain() implements AppModule._AppMain<StudioGraphInspector.Params, StudioGraphInspector> {}

    public record link() implements AppLink<StudioGraphInspector> {}

    /**
     * Typed query parameters.
     *
     * @param root optional vertex class FQN — narrows the dump to a subtree;
     *             empty / missing → defaults to the Bootstrap root. (Stays
     *             {@code String} because the v1 {@code ParamsWriter} forbids
     *             nested records inside a {@code Params} record — a typed
     *             wrapper isn't legal here yet.)
     * @param view optional render mode — typed enum so the compiler enforces
     *             the closed set and the frontend coerces via {@code _enum(...)}.
     *             {@code null} → {@link StudioGraphView#TREE}.
     */
    public record Params(String root, StudioGraphView view) implements AppModule._Param {}

    public static final StudioGraphInspector INSTANCE = new StudioGraphInspector();

    @Override public Class<Params> paramsType() { return Params.class; }

    @Override public String simpleName() { return "studio-graph"; }
    @Override public String title()      { return "studio graph"; }

    @Override
    public ImportsFor<StudioGraphInspector> imports() {
        return ImportsFor.<StudioGraphInspector>builder()
                .add(new ModuleImports<>(
                        List.of(new StudioGraphInspectorRenderer.renderStudioGraphInspector()),
                        StudioGraphInspectorRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StudioGraphInspector> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                "function appMain(rootElement) {",
                "    rootElement.replaceChildren(renderStudioGraphInspector());",
                "}"
        );
    }
}
