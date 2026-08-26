package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.PartyTree;
import hue.captains.singapura.js.homing.core.js.PartyTreeWriter;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RFC 0024 — abstract base for the framework's default shell. A
 * StandardMPA owns the document chrome and dispatches to one of its
 * registered {@link Widget}s by URL.
 *
 * <p>The shell's role:</p>
 *
 * <ol>
 *   <li>Build the root DomOpsParty branch tree — {@code header} at L1
 *       (chrome) and {@code main} at L1 (widget slot) — via
 *       {@link PartyTreeWriter}.</li>
 *   <li>Populate chrome via {@code StudioElements.Header()} (brand /
 *       breadcrumb / theme picker live inside that component);
 *       breadcrumb data comes from {@code /doc-refs?id=...}, brand from
 *       {@code /brand}.</li>
 *   <li>Read the URL {@code widget} query param, look up the matching
 *       Widget from {@link #widgets()}, and dispatch by calling its
 *       {@code mountInto(L2 branch, mainHost, params)} against a fresh
 *       L2 branch under main.</li>
 * </ol>
 *
 * <h2>Subclass contract</h2>
 *
 * <p>Concrete shells declare:</p>
 * <ul>
 *   <li>{@link #widgets()} — the set of Widgets this shell hosts.</li>
 *   <li>{@link #appMain()} — the typed {@code appMain} record.</li>
 *   <li>Optionally {@link #partyTree()} — override to add chrome
 *       variations beyond the default header.</li>
 * </ul>
 *
 * <pre>{@code
 * public final class MyStudioShell extends StandardMPA<MyStudioShell> {
 *     public static final MyStudioShell INSTANCE = new MyStudioShell();
 *     private MyStudioShell() {}
 *
 *     public record appMain() implements AppModule._AppMain<_None, MyStudioShell> {}
 *     @Override protected AppModule._AppMain<_None, MyStudioShell> appMain() { return new appMain(); }
 *
 *     @Override protected List<? extends Widget<?, ?>> widgets() {
 *         return List.of(SvgWidget.INSTANCE);
 *     }
 * }
 * }</pre>
 *
 * <h2>Params model</h2>
 *
 * <p>The shell declares no typed params ({@code _None.class}); it reads
 * {@code window.location.search} directly, extracts {@code widget} for
 * dispatch, and hands a plain object of the URL params to the matched
 * widget's {@code mountInto(branch, parent, params)}. The per-widget
 * typed Params record drives URL <i>generation</i> (link writing);
 * runtime URL <i>parsing</i> is uniform across widgets.</p>
 *
 * @param <M> self-type (CRTP)
 * @since RFC 0024 Phase P1a
 */
public abstract class StandardMPA<P extends AppModule._Param, M extends StandardMPA<P, M>>
        implements AppModule<P, M>, SelfContent {

    // -----------------------------------------------------------------------
    // Subclass contract.
    // -----------------------------------------------------------------------

    /** The typed appMain record this shell exports. */
    protected abstract AppModule._AppMain<P, M> appMain();

    /** The set of widgets this shell hosts. */
    protected abstract List<? extends Widget<?, ?>> widgets();

    /**
     * Whether the shell's {@code main} slot should fill the viewport
     * edge-to-edge — drop {@code max-width}, drop padding, become a flex
     * container — instead of the default centred-column reading chrome.
     *
     * <p>Default {@code false}: {@code .st-main} keeps its 1280px max-width
     * and the 36/32/64 padding suited for doc / catalogue / form pages.
     * Workspace-style apps (full-viewport interactive surfaces — see
     * {@link WorkspaceMPA}) override to {@code true} so a single
     * SplitPane / MultiTabPane / Modal-hosting widget fills the available
     * area cleanly.</p>
     *
     * <p>Lives on StandardMPA (not just WorkspaceMPA) so the chrome
     * bootstrap can read it via the same generated-JS path; WorkspaceMPA's
     * override is the canonical opt-in.</p>
     *
     * @since RFC 0025 L cleanup — extracts the 4-line incantation that
     *        every workspace-style demo widget was repeating in its
     *        {@code bodyJs()}.
     */
    protected boolean fullbleedMain() { return false; }

    /**
     * The root party tree. Default: three L1 siblings under root, both
     * symmetric chrome ({@code header}, {@code footer}) framework-owned,
     * plus the {@code main} widget slot shell-owned. Each branch holds
     * one wrapping host element the chrome-population code anchors its
     * contents into. Override to add chrome variations.
     *
     * <p>Header and footer are at the same structural level by design:
     * both are 中央秘书 / Central Secretariat, both reporting to
     * partyChief directly. The widget owns only {@code main}; chrome
     * (top AND bottom) is invariant across widget swaps.</p>
     */
    protected PartyTree partyTree() {
        return new PartyTree(List.of(
                PartyTree.FrameworkBranch.flat(
                        "header",
                        PartyTree.OwnerRef.PartyChief.INSTANCE,
                        List.of(new PartyTree.ElementDecl("headerHost", "div"))),
                new PartyTree.FrameworkBranch(
                        "main",
                        PartyTree.OwnerRef.ShellChief.INSTANCE,
                        List.of(new PartyTree.ElementDecl("mainHost", "div")),
                        List.of()),
                PartyTree.FrameworkBranch.flat(
                        "footer",
                        PartyTree.OwnerRef.PartyChief.INSTANCE,
                        List.of(new PartyTree.ElementDecl("footerHost", "div")))
        ));
    }

    // -----------------------------------------------------------------------
    // AppModule conventions.
    // -----------------------------------------------------------------------

    @Override public String simpleName() { return "standard-mpa"; }

    /** Default {@code _None}. Subclasses with typed URL Params (e.g.
     *  {@code SvgViewer} extending {@link SingleWidgetMPA}) override. */
    @Override @SuppressWarnings("unchecked")
    public Class<P> paramsType() { return (Class<P>) _None.class; }

    @Override public String title() { return "studio"; }

    // -----------------------------------------------------------------------
    // Framework wiring — final. Subclasses cannot override.
    // -----------------------------------------------------------------------

    @Override
    public final ImportsFor<M> imports() {
        var b = ImportsFor.<M>builder()
                .add(new ModuleImports<>(
                        List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(
                                new StudioElements.Header(),
                                new StudioElements.Footer()
                        ),
                        StudioElements.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(
                                new StudioStyles.st_root(),
                                new StudioStyles.st_main()
                        ),
                        StudioStyles.INSTANCE));
        for (Widget<?, ?> w : widgets()) {
            addWidgetImport(b, w);
        }
        return b.build();
    }

    /**
     * Adds one widget's {@code mountInto} export as an aliased import.
     * Every widget declares its mountInto record under the same simple
     * name ({@code mountInto}), so importing N widgets into one shell
     * would otherwise produce N duplicate-identifier JS errors. The
     * framework's import writer respects per-export aliases declared in
     * {@link ModuleImports#aliases()} — here we register {@code mountInto}
     * as {@code <WidgetSimpleClassName>_mountInto}. The dispatch JS
     * computes the same formula, so the two halves stay in sync.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addWidgetImport(ImportsFor._Builder builder, Widget<?, ?> w) {
        var mountIntoExports = w.exports().exports();
        var aliases = new HashMap<Class<? extends Exportable<?>>, String>();
        for (Exportable<?> exp : mountIntoExports) {
            aliases.put((Class<? extends Exportable<?>>) exp.getClass(),
                    mountIntoAlias(w));
        }
        builder.add(new ModuleImports(mountIntoExports, w, aliases));
    }

    /** Deterministic alias formula. */
    private static String mountIntoAlias(Widget<?, ?> w) {
        return w.getClass().getSimpleName() + "_mountInto";
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ExportsOf<M> exports() {
        return new ExportsOf<>((M) this, List.of(appMain()));
    }

    @Override
    public final List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        // RFC 0051 — params arrive as an argument when the app declares a
        // codec. Every MPA subclass shares this one generated body, so this is
        // the single edit that makes composed-viewer, doc-tree-viewer,
        // svg-viewer and the workspace shells addressable by path.
        lines.add("function appMain(rootElement, params, chrome) {");
        lines.add("    try {");
        lines.add("");
        lines.add("    // ── Root DOM container ──");
        lines.add("    var rootEl = document.createElement('div');");
        lines.add("    css.addClass(rootEl, st_root);");
        lines.add("    rootElement.replaceChildren(rootEl);");
        lines.add("");
        lines.add("    // ── PartyTree bootstrap (creates branches + host elements) ──");
        for (String line : new PartyTreeWriter(partyTree()).writeBootstrap()) {
            lines.add("    " + line);
        }
        lines.add("");
        lines.add("    // Anchor branch hosts into the DOM tree (header → main → footer).");
        lines.add("    rootEl.appendChild(headerHost);");
        lines.add("    css.addClass(mainHost, st_main);");
        if (fullbleedMain()) {
            lines.add("    // fullbleedMain() = true — neutralise .st-main's max-width + padding");
            lines.add("    // and turn the slot into a flex container so the widget's host can");
            lines.add("    // stretch via flex:1. WorkspaceMPA subclasses opt in. Replaces the");
            lines.add("    // 4-line incantation every workspace-style demo widget used to repeat.");
            lines.add("    mainHost.style.maxWidth = 'none';");
            lines.add("    mainHost.style.width    = '100%';");
            lines.add("    mainHost.style.padding  = '0';");
            lines.add("    mainHost.style.display  = 'flex';");
        }
        lines.add("    rootEl.appendChild(mainHost);");
        lines.add("    rootEl.appendChild(footerHost);");
        lines.add("");
        lines.add("    // ── Chrome population ──");
        lines.add("    // Brand-for-Header normalisation: BrandGetAction emits");
        lines.add("    // { label, logo, homeUrl }; Header's Brand expects { href, label, logo }.");
        lines.add("    function brandForHeader(b) {");
        lines.add("        return { href: (b && b.homeUrl) || '/', label: (b && b.label) || 'studio', logo: (b && b.logo) || '' };");
        lines.add("    }");
        lines.add("    var brandFallback = { label: 'studio', homeUrl: '/', logo: '' };");
        lines.add("    var resolvedTitle = " + jsString(title()) + ";");
        lines.add("    var resolvedBrand = brandFallback;");
        lines.add("    var resolvedCrumbs = [{ text: resolvedTitle }];");
        lines.add("    var headerEl = Header({ brand: brandForHeader(brandFallback), crumbs: resolvedCrumbs });");
        lines.add("    headerHost.appendChild(headerEl);");
        lines.add("    function refreshHeader() {");
        lines.add("        var newHeader = Header({ brand: brandForHeader(resolvedBrand), crumbs: resolvedCrumbs });");
        lines.add("        headerHost.replaceChild(newHeader, headerEl);");
        lines.add("        headerEl = newHeader;");
        lines.add("    }");
        lines.add("");
        lines.add("    fetch('/brand').then(function(r){ return r.ok ? r.json() : null; })");
        lines.add("        .then(function(b){ if (b) { resolvedBrand = b; refreshHeader(); } })");
        lines.add("        .catch(function(){});");
        lines.add("");
        lines.add("    // Footer — empty by default; studio shells override partyTree()");
        lines.add("    // or otherwise populate footerHost to put content here. The");
        lines.add("    // structural commitment is that footer EXISTS at L1 alongside");
        lines.add("    // header, symmetric chrome under partyChief.");
        lines.add("    var footerEl = Footer({ children: [] });");
        lines.add("    footerHost.appendChild(footerEl);");
        lines.add("");
        lines.add("    // ── URL → widget dispatch ──");
        lines.add("    var sp = new URLSearchParams(window.location.search);");
        // Single-widget convention: when a shell hosts exactly one widget,
        // omit ?widget= from URLs — the shell defaults to its sole widget.
        // This is what makes the "fake AppModule" pattern (SingleWidgetMPA
        // subclasses like SvgViewer / ComposedViewer) preserve their legacy
        // URL grammar: /app?app=svg-viewer&id=<uuid> still routes correctly.
        // Multi-widget shells (DemoStandardMPA) require explicit ?widget=.
        if (widgets().size() == 1) {
            lines.add("    var widgetName = (params && params.widget) || sp.get('widget') || '"
                    + widgets().get(0).simpleName() + "';");
        } else {
            lines.add("    var widgetName = (params && params.widget) || sp.get('widget');");
        }
        lines.add("    var widgetParams = {};");
        // RFC 0051 — the URL first, then the server's typed answer over the
        // top. Union rather than either/or, and deliberately so: a path URL
        // carries no query at all, so on that route the stamp is the only
        // source; on the flat route this shell forwards EVERY query key to its
        // widget (backlog B2), and taking only the stamped keys would silently
        // drop the ones the app's record does not name. Overlaying keeps the
        // pass-through intact while letting the typed value win where both
        // speak.
        lines.add("    sp.forEach(function(v, k){ widgetParams[k] = v; });");
        lines.add("    if (params) {");
        lines.add("        Object.keys(params).forEach(function(k){ widgetParams[k] = params[k]; });");
        lines.add("    }");
        lines.add("");
        lines.add("    // Un-stamped fallback — ONE case left after RFC 0051.");
        lines.add("    //   A leveled tree path (?treeId=..&l0=..&l1=.., RFC 0040):");
        lines.add("    //   fetch /open-refs?l0=.. — still the one endpoint that returns");
        lines.add("    //   a chain, because a tree path is a different addressing scheme");
        lines.add("    //   and D9 leaves it to that RFC.");
        lines.add("    //");
        lines.add("    //   The ?id=<doc-uuid> case is GONE. It fetched /doc-refs to");
        lines.add("    //   recover a title, and a crawl of both studios found 264 of 264");
        lines.add("    //   navigable pages already carry a stamped one.");
        // RFC 0051 — when the server stamped the trail, take it and fetch
        // nothing. The fallback below re-derives server-side knowledge over
        // the wire; the stamp is that knowledge, already here.
        lines.add("    if (chrome && chrome.crumbs && chrome.crumbs.length) {");
        lines.add("        resolvedCrumbs = chrome.crumbs.slice();");
        lines.add("        var stampedLeaf = resolvedCrumbs[resolvedCrumbs.length - 1];");
        lines.add("        if (stampedLeaf && stampedLeaf.text) {");
        lines.add("            resolvedTitle = stampedLeaf.text;");
        lines.add("            document.title = resolvedTitle + (resolvedBrand && resolvedBrand.label ? ' \u00b7 ' + resolvedBrand.label : '');");
        lines.add("        }");
        lines.add("        refreshHeader();");
        lines.add("    } else {");
        lines.add("    var crumbUrl;");
        lines.add("    if (widgetParams.treeId !== undefined || widgetParams.l0 !== undefined) {");
        lines.add("        var crumbPq = [];");
        lines.add("        if (widgetParams.treeId) crumbPq.push('treeId=' + encodeURIComponent(widgetParams.treeId));");
        lines.add("        for (var ci = 0; widgetParams['l' + ci] !== undefined; ci++) {");
        lines.add("            crumbPq.push('l' + ci + '=' + encodeURIComponent(widgetParams['l' + ci]));");
        lines.add("        }");
        lines.add("        crumbUrl = '/open-refs?' + crumbPq.join('&');");
        lines.add("    } else {");
        // RFC 0051 Phase 5 — /app-refs is retired. It existed only to answer
        // "what is the crumb for this app page", which the stamp now answers
        // without a round trip, so there is nothing left to ask. A page that
        // reaches here has no id, no tree path and no stamp — it is genuinely
        // unpositioned, and the honest chrome is the one it already has.
        lines.add("        crumbUrl = null;");
        lines.add("    }");
        lines.add("    if (crumbUrl) {");
        lines.add("    fetch(crumbUrl)");
        lines.add("        .then(function(r){ return r.ok ? r.json() : null; })");
        lines.add("        .then(function(info){");
        lines.add("            if (info && info.title) {");
        lines.add("                resolvedTitle = info.title;");
        lines.add("                var leaf = { text: resolvedTitle };");
        lines.add("                if (info.breadcrumbs && info.breadcrumbs.length > 0) {");
        lines.add("                    resolvedCrumbs = info.breadcrumbs.slice();");
        lines.add("                    resolvedCrumbs.push(leaf);");
        lines.add("                } else {");
        lines.add("                    resolvedCrumbs = [leaf];");
        lines.add("                }");
        lines.add("                refreshHeader();");
        lines.add("                document.title = info.title + (resolvedBrand && resolvedBrand.label ? ' \\u00b7 ' + resolvedBrand.label : '');");
        lines.add("            }");
        lines.add("        })");
        lines.add("        .catch(function(){});");
        lines.add("    }");   // closes: if (crumbUrl)
        lines.add("    }");   // closes: the un-stamped fallback branch
        lines.add("");
        lines.add("    if (!widgetName) {");
        lines.add("        var noWidgetEl = document.createElement('div');");
        lines.add("        noWidgetEl.textContent = 'No widget specified. Use ?widget=...';");
        lines.add("        noWidgetEl.style.cssText = 'padding:20px;color:#666;font-family:sans-serif;';");
        lines.add("        mainHost.appendChild(noWidgetEl);");
        lines.add("        return;");
        lines.add("    }");
        lines.add("");
        lines.add("    // Widget mount — fresh L2 widget branch under main, owned by a");
        lines.add("    // per-mount widgetChief. On SPA swap, dissolving the L2 branch");
        lines.add("    // releases the whole widget subtree.");
        lines.add("    var widgetBranch = main.createBranch(widgetName);");
        lines.add("    var widgetChief = Object.freeze({ toString: function(){ return 'widgetChief:' + widgetName; } });");
        lines.add("    widgetBranch.activate(widgetChief);");
        lines.add("");
        lines.add("    switch (widgetName) {");
        for (Widget<?, ?> w : widgets()) {
            lines.add("        case '" + w.simpleName() + "':");
            lines.add("            " + mountIntoAlias(w) + "(widgetBranch, mainHost, widgetParams);");
            lines.add("            break;");
        }
        lines.add("        default:");
        lines.add("            var unknownEl = document.createElement('div');");
        lines.add("            unknownEl.textContent = 'Unknown widget: ' + widgetName;");
        lines.add("            unknownEl.style.cssText = 'padding:20px;color:#900;font-family:sans-serif;';");
        lines.add("            mainHost.appendChild(unknownEl);");
        lines.add("    }");
        lines.add("");
        lines.add("    } catch (e) {");
        lines.add("        console.error('StandardMPA appMain failed:', e);");
        lines.add("        var errEl = document.createElement('pre');");
        lines.add("        errEl.style.cssText = 'padding:20px;color:#900;background:#fee;border:1px solid #c00;margin:20px;white-space:pre-wrap;font-family:monospace;';");
        lines.add("        errEl.textContent = 'StandardMPA error: ' + (e && e.message ? e.message : String(e)) + '\\n\\n' + (e && e.stack ? e.stack : '');");
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
