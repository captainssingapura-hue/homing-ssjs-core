package hue.captains.singapura.js.homing.libs;

import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ExternalModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.List;

/**
 * Configurable 3rd-party proxy for <a href="https://mermaid.js.org/">Mermaid</a>
 * (diagrams-as-text). Unlike the bundled libs ({@code ToneJs}, {@code ThreeJs}),
 * this is an {@link ExternalModule}: its served body imports the <em>real</em>
 * Mermaid ESM from a remote URL <b>at runtime</b> and re-exports a small typed
 * surface. Consumers import this proxy same-origin
 * ({@code /module?class=…MermaidProxyModule}); only this one module reaches the
 * network, so the CDN dependency is isolated to a single, replaceable seam.
 *
 * <h2>The "no runtime CDN" doctrine, and why this is the exception</h2>
 * <p>{@code CdnFreeConformanceTest} bans runtime CDN imports so the app stays
 * self-contained. The documented, sanctioned exception is exactly this: an
 * {@code ExternalModule} with a hand-emitted wrapper. Because the body is
 * generated programmatically (via {@link SelfContent}) rather than living in a
 * {@code .js} resource file, it isn't scanned by that test — but the intent is
 * explicit here: this proxy is <em>meant</em> to reach a CDN, and its URL is
 * overridable precisely so a deployment that can't reach the default can point
 * it at a mirror or a bundled copy.</p>
 *
 * <h2>Configuring the URL</h2>
 * <p>The default is {@link #DEFAULT_URL}. Override it once at boot via
 * {@link ExternalModuleUrlRegistry}:</p>
 * <pre>{@code
 * ExternalModuleUrlRegistry.INSTANCE.override(
 *         MermaidProxyModule.class,
 *         "https://my-mirror.internal/mermaid@11/mermaid.esm.min.mjs");
 * }</pre>
 *
 * <h2>Exports</h2>
 * <ul>
 *   <li>{@code mermaid} — the initialised Mermaid API object (auto-render off).</li>
 *   <li>{@code renderMermaid(id, code)} — {@code async (id, code) -> svgString};
 *       renders one diagram and returns its SVG markup.</li>
 * </ul>
 */
public record MermaidProxyModule() implements ExternalModule<MermaidProxyModule>, SelfContent {

    public static final MermaidProxyModule INSTANCE = new MermaidProxyModule();

    /** Default Mermaid ESM entry point — a pinned major on jsDelivr. */
    public static final String DEFAULT_URL =
            "https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs";

    /** The initialised Mermaid API object. */
    public record mermaid() implements Exportable._Constant<MermaidProxyModule> {}

    /** {@code async (id, code) -> svgString}. */
    public record renderMermaid() implements Exportable._Constant<MermaidProxyModule> {}

    @Override
    public ExportsOf<MermaidProxyModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new mermaid(), new renderMermaid()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        String url = ExternalModuleUrlRegistry.INSTANCE.resolve(MermaidProxyModule.class, DEFAULT_URL);
        return List.of(
                "// Runtime-CDN proxy (see MermaidProxyModule.java). URL is deployment-overridable.",
                "import mermaidLib from \"" + url + "\";",
                "const mermaid = mermaidLib;",
                "mermaid.initialize({ startOnLoad: false });",
                "const renderMermaid = async (id, code) => {",
                "    const { svg } = await mermaid.render(id, code);",
                "    return svg;",
                "};"
        );
    }
}
