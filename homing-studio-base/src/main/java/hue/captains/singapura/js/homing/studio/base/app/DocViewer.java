package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0015 — abstract base for every Doc viewer. <b>Bakes the
 * "chrome composition is mandated" axiom (Viewer ontology V11) into the
 * type system.</b>
 *
 * <p>Splits a viewer into two layers:</p>
 * <ol>
 *   <li><b>Bare viewer (subclass-supplied)</b> — the kind-specific
 *       parts: {@link #bodyJs()} (JS that populates the main slot),
 *       {@link #bodyImports()} (any extra imports the body needs),
 *       {@link #appMain()} (the typed appMain record),
 *       {@link #simpleName()} / {@link #paramsType()} / {@link #title()}.</li>
 *   <li><b>Framework chrome (final here)</b> — Header with brand +
 *       breadcrumb + theme picker, the standard {@code st_root} /
 *       {@code st_main} layout, the audio runtime hookup (RFC 0007 binds
 *       automatically once the page is rendered through this base), the
 *       standard imports for Header + HrefManager + the framework's CSS
 *       primitives. Encoded in {@link #selfContent} and {@link #imports},
 *       both declared {@code final} so concrete subclasses cannot
 *       override them.</li>
 * </ol>
 *
 * <p>The result: every concrete {@code DocViewer} subclass automatically
 * gets the standard page chrome. There is no path to register a Doc
 * viewer that skips chrome (modulo bypassing this base entirely, which
 * is itself caught by a conformance test queued for follow-up).</p>
 *
 * <p>Concrete subclasses are regular Java classes (not records) because
 * Java disallows record-extends-class. The {@code INSTANCE} singleton
 * pattern + {@code final class} declaration recovers most of the
 * record discipline. The class still implements
 * {@link hue.captains.singapura.tao.ontology.StatelessFunctionalObject
 * StatelessFunctionalObject} transitively via {@link AppModule}.</p>
 *
 * <h3>Subclass template</h3>
 * <pre>{@code
 * public final class MyViewer extends DocViewer<MyViewer.Params, MyViewer> {
 *     public static final MyViewer INSTANCE = new MyViewer();
 *     private MyViewer() {}
 *
 *     public record Params(String id) implements AppModule._Param {}
 *     private record appMain() implements AppModule._AppMain<Params, MyViewer> {}
 *
 *     @Override public String simpleName() { return "my-viewer"; }
 *     @Override public Class<Params> paramsType() { return Params.class; }
 *     @Override public String title() { return "my viewer"; }
 *
 *     @Override protected AppModule._AppMain<Params, MyViewer> appMain() { return new appMain(); }
 *     @Override protected List<ModuleImports<? extends Importable>> bodyImports() { return List.of(); }
 *     @Override protected List<String> bodyJs() {
 *         return List.of(
 *             "    // body code — `main` and `params` are in scope",
 *             "    main.textContent = 'Hello, ' + params.id;"
 *         );
 *     }
 * }
 * }</pre>
 *
 * @param <P> the viewer's typed Params
 * @param <M> the concrete viewer subclass (CRTP self-bound)
 *
 * @since RFC 0015 Phase 5 + RFC 0016 (V11 axiom realisation)
 */
public abstract class DocViewer<P extends AppModule._Param, M extends DocViewer<P, M>>
        implements AppModule<P, M>, SelfContent {

    // -----------------------------------------------------------------------
    // Subclass contract — these stay abstract; each viewer supplies them.
    // -----------------------------------------------------------------------

    /** The typed appMain record this viewer exports. Implement as
     *  {@code new appMain()} where {@code appMain} is a nested record
     *  inside the concrete viewer. */
    protected abstract AppModule._AppMain<P, M> appMain();

    /** Any extra imports the body's JS needs (CSS classes beyond the
     *  framework chrome's defaults, additional JS components, etc.).
     *  Return an empty list when the body uses only framework primitives. */
    protected abstract List<ModuleImports<? extends Importable>> bodyImports();

    /** The body JS lines. These execute inside the framework's appMain
     *  function with {@code main} and {@code params} in scope. The body
     *  is responsible for populating {@code main}; the chrome (Header,
     *  root container, breadcrumb fetch, etc.) is already in place. */
    protected abstract List<String> bodyJs();

    // -----------------------------------------------------------------------
    // Framework chrome — final. Subclasses cannot override.
    // -----------------------------------------------------------------------

    @Override
    public final ImportsFor<M> imports() {
        var b = ImportsFor.<M>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioElements.Header(),
                        new StudioElements.Footer()
                ), StudioElements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE));
        for (ModuleImports<? extends Importable> extra : bodyImports()) {
            b.add(extra);
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ExportsOf<M> exports() {
        return new ExportsOf<>((M) this, List.of(appMain()));
    }

    @Override
    public final List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        // ---- Chrome open ----
        // RFC 0051 — params arrive as an argument for any subclass that
        // declares a codec, and the generated URL-reading const is suppressed
        // for exactly those. The body below refers to `params` throughout, so
        // this signature is what keeps it defined: adding a codec without this
        // turns the whole page into "params is not defined".
        lines.add("function appMain(rootElement, params, chrome) {");
        // Defensive try/catch — surfaces failures inline + to console so a
        // broken viewer doesn't render as an empty page (the failure mode that
        // motivated the chrome-in-type-system refactor).
        lines.add("    try {");
        lines.add("    // Brand-for-Header normalization: BrandGetAction emits { label, logo, homeUrl };");
        lines.add("    // the Header's Brand() expects { href, label, logo }. Map homeUrl \\u2192 href.");
        lines.add("    function brandForHeader(b) {");
        lines.add("        return { href: (b && b.homeUrl) || '/', label: (b && b.label) || 'studio', logo: (b && b.logo) || '' };");
        lines.add("    }");
        lines.add("    var brandFallback = { label: 'studio', homeUrl: '/', logo: '' };");
        lines.add("    var root = document.createElement('div');");
        lines.add("    css.addClass(root, st_root);");
        lines.add("    rootElement.replaceChildren(root);");
        lines.add("");
        lines.add("    var resolvedTitle = " + jsString(title()) + ";");
        lines.add("    var resolvedBrand = brandFallback;");
        // RFC 0051 Phase 5 — the title-only crumb is the starting point and,
        // for an unpositioned page, the finishing one. A positioned page
        // replaces it wholesale from the stamp a few lines below, before any
        // fetch, which is why this no longer pops in.
        lines.add("    var resolvedCrumbs = [{ text: resolvedTitle }];");
        lines.add("    var headerEl = Header({ brand: brandForHeader(brandFallback), crumbs: resolvedCrumbs });");
        lines.add("    root.appendChild(headerEl);");
        lines.add("");
        lines.add("    var main = document.createElement('div');");
        lines.add("    css.addClass(main, st_main);");
        lines.add("    root.appendChild(main);");
        lines.add("");
        lines.add("    function refreshHeader() {");
        lines.add("        var newHeader = Header({ brand: brandForHeader(resolvedBrand), crumbs: resolvedCrumbs });");
        lines.add("        root.replaceChild(newHeader, headerEl);");
        lines.add("        headerEl = newHeader;");
        lines.add("    }");
        lines.add("    fetch('/brand').then(function(r){ return r.ok ? r.json() : null; })");
        lines.add("        .then(function(b){ if (b) { resolvedBrand = b; refreshHeader(); } })");
        lines.add("        .catch(function(){});");
        // RFC 0051 — the stamped trail, and now the ONLY source of one. The
        // /doc-refs title fallback that used to sit here is gone: a crawl of
        // both studios found 264 of 264 navigable pages carry a stamp, so it
        // fired for nothing reachable, while costing every table, image and
        // svg page a fetch to re-ask for a title already in the page.
        lines.add("    if (chrome && chrome.crumbs && chrome.crumbs.length) {");
        lines.add("        resolvedCrumbs = chrome.crumbs.slice();");
        lines.add("        var stampedLeaf = resolvedCrumbs[resolvedCrumbs.length - 1];");
        lines.add("        if (stampedLeaf && stampedLeaf.text) {");
        lines.add("            resolvedTitle = stampedLeaf.text;");
        lines.add("            document.title = resolvedTitle + (resolvedBrand && resolvedBrand.label ? ' \\u00b7 ' + resolvedBrand.label : '');");
        lines.add("        }");
        lines.add("        refreshHeader();");
        lines.add("    }");
        lines.add("");
        lines.add("    // === Body (subclass-supplied, kind-specific) ===");
        // ---- Body (subclass) ----
        lines.addAll(bodyJs());
        // ---- Chrome close ----
        lines.add("");
        lines.add("    } catch (e) {");
        lines.add("        console.error('DocViewer appMain failed:', e);");
        lines.add("        var errEl = document.createElement('pre');");
        lines.add("        errEl.style.cssText = 'padding:20px;color:#900;background:#fee;border:1px solid #c00;margin:20px;white-space:pre-wrap;font-family:monospace;';");
        lines.add("        errEl.textContent = 'DocViewer error: ' + (e && e.message ? e.message : String(e)) + '\\n\\n' + (e && e.stack ? e.stack : '');");
        lines.add("        rootElement.replaceChildren(errEl);");
        lines.add("    }");
        lines.add("}");
        return lines;
    }

    private static String jsString(String s) {
        if (s == null) return "''";
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
