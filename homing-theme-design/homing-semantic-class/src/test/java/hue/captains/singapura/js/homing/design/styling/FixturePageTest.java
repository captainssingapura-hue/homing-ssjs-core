package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import hue.captains.singapura.js.homing.server.CssContentGetAction;
import hue.captains.singapura.js.homing.server.CssQuery;
import hue.captains.singapura.js.homing.server.DerivedCssContentProvider;
import hue.captains.singapura.js.homing.server.QueryParamResolver;
import hue.captains.singapura.js.homing.server.SelfRenderedCssRenderer;
import hue.captains.singapura.js.homing.server.ServedModules;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A fixture page (RFC 0067, appendix Two Classes per Leaf, phase 4): a styling's two sheets served as the server serves
 * them - a style's, the same in every palette, and a palette's, the same under every style - each loaded by the theme's
 * owner from the module the server writes for it, and each swapped on its own when its pick changes. Every link the
 * owner appends is fetched from the server's own action; an element wears its leaf's classes as Java names them, and
 * the sheet served rules exactly those.
 */
class FixturePageTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/server/";

    static final Styling STYLING = new Styling(StyledTaxonomy.INSTANCE.read(), ResolvingColours.INSTANCE.read(),
            List.of(Plain.INSTANCE, Ink.INSTANCE), List.of(ResolvingColours.Paper.INSTANCE, ResolvingColours.Night.INSTANCE));

    /** The page's style-aware sheet: a style's, by ?style=. */
    record PageStyle() implements DerivedCss<PageStyle>, SelfRenderedCss {
        static final PageStyle INSTANCE = new PageStyle();
        @Override public VariesWith variesWith() { return VariesWith.STYLE; }
        @Override public Optional<String> fallback() { return Optional.of(STYLING.defaultStyle().token()); }
        @Override public String css(String style) { return STYLING.styleSheet(style); }
    }

    /** The page's palette sheet: a palette's, by ?palette=. */
    record PagePalette() implements DerivedCss<PagePalette>, SelfRenderedCss {
        static final PagePalette INSTANCE = new PagePalette();
        @Override public VariesWith variesWith() { return VariesWith.PALETTE; }
        @Override public Optional<String> fallback() { return Optional.of(STYLING.defaultPalette().token()); }
        @Override public String css(String palette) { return STYLING.paletteSheet(palette); }
    }

    record Page() implements Crate {
        @Override public String name() { return "page"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(PageStyle.INSTANCE), CrateEntry.of(PagePalette.INSTANCE)); }
    }

    private static final CssContentGetAction SERVER = new CssContentGetAction(null, ServedModules.of(List.of(new Page())),
            List.of(SelfRenderedCssRenderer.INSTANCE));

    private static final String SHIMS = """
            globalThis.links = [];
            globalThis.document = {
                head: { appendChild: function (l) { globalThis.links.push(l); } },
                createElement: function (tag) {
                    return { tag: tag, media: "", removed: false, onload: null, onerror: null,
                             remove: function () { this.removed = true; } };
                }
            };
            globalThis.console = { error: function (m, e) { globalThis.lastError = String(e && e.message || m); } };
            globalThis.picked = {};
            globalThis.changeListeners = [];
            globalThis.PreferenceViewInstance = {
                resolve: function (name, fallback) { return globalThis.picked[name] || fallback || null; },
                onChange: function (fn) { globalThis.changeListeners.push(fn); return function () {}; }
            };
            globalThis.pending = function () { return links.filter(l => !l.fired && !l.removed).map(l => l.href); };
            globalThis.land = function (href) { for (const l of links) if (!l.fired && !l.removed && l.href === href) { l.fired = true; l.onload(); } };
            globalThis.live = function () { return links.filter(l => !l.removed).map(l => l.href + " " + l.media); };
            globalThis.element = function () {
                const set = [];
                return { classList: {
                    add: function (c) { if (set.indexOf(c) < 0) set.push(c); },
                    remove: function (c) { const i = set.indexOf(c); if (i >= 0) set.splice(i, 1); },
                    contains: function (c) { return set.indexOf(c) >= 0; },
                    [Symbol.iterator]: function () { return set.slice()[Symbol.iterator](); }
                }, classes: function () { return set.slice(); } };
            };
            """;

    /** What the server answered for each link, as the browser would have fetched it. */
    private final Map<String, String> served = new LinkedHashMap<>();

    @BeforeEach
    void open() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
        for (String m : List.of("CssHandles.js", "CssDependencyGraph.js", "CssLoadProcedure.js", "CssLinks.js", "CssClassManager.js", "ThemeSheets.js",
                "StyledLeaves.js")) loadModule(DIR + m);
        js.eval("js", "globalThis._themes = ThemeSheetsInstance; globalThis.leaves = (" + LeafClasses.INSTANCE.script(STYLING.taxonomy()) + "); undefined");
        // The two modules the page imports, as the server writes them: each hands its sheet to the owner.
        var resolver = new QueryParamResolver("/module");
        for (DerivedCss<?> sheet : List.<DerivedCss<?>>of(PageStyle.INSTANCE, PagePalette.INSTANCE))
            js.eval("js", load(sheet, resolver).replaceFirst("^await ", "") + " undefined");
        fetchAndLand();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String load(DerivedCss<?> sheet, QueryParamResolver resolver) {
        return new DerivedCssContentProvider(sheet, null, resolver).content().get(1).toString();
    }

    private void tick() { js.eval("js", "undefined"); js.eval("js", "undefined"); }

    /** Every link waiting, fetched from the server and landed, until none waits. */
    @SuppressWarnings("unchecked")
    private List<String> fetchAndLand() {
        var fetched = new java.util.ArrayList<String>();
        for (int round = 0; round < 6; round++) {
            tick();
            for (String href : (List<String>) js.eval("js", "pending()").as(List.class)) {
                served.put(href, fetch(href));
                fetched.add(href);
                js.eval("js", "land(" + quoted(href) + "); undefined");
            }
        }
        return fetched;
    }

    private static String fetch(String href) {
        Map<String, String> q = new HashMap<>();
        for (String pair : href.substring(href.indexOf('?') + 1).split("&")) {
            int eq = pair.indexOf('=');
            q.put(pair.substring(0, eq), URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        try {
            return SERVER.execute(new CssQuery(q.get("class"), q.get("theme"), q.get("style"), q.get("palette")), null).get().body();
        } catch (Exception e) {
            throw new AssertionError(href + " was not served", e);
        }
    }

    private static String quoted(String s) { return "\"" + s.replace("\"", "\\\"") + "\""; }

    private static String href(Class<?> sheet, String varies, String slug) { return "/css-content?class=" + sheet.getCanonicalName() + "&" + varies + "=" + slug; }

    private void pick(String varies, String slug) {
        js.eval("js", "globalThis.picked[" + quoted(varies) + "] = " + quoted(slug) + "; changeListeners.forEach(fn => fn()); undefined");
    }

    @SuppressWarnings("unchecked")
    private List<String> live() { return js.eval("js", "live()").as(List.class); }

    @Test
    void thePageWears_theDefaultStyle_inTheDefaultPalette_eachItsOwnSheet() {
        String style = href(PageStyle.class, "style", "plain"), palette = href(PagePalette.class, "palette", "paper");
        assertEquals(List.of(style + " all", palette + " all"), live());
        assertTrue(served.get(style).contains("/* tab: plain's own */"), served.get(style));
        assertTrue(served.get(palette).contains("    --fp-page: #ffffff;\n"), served.get(palette));
    }

    @Test
    void theTwoSheetsFit_everyPaletteVariableTheStyleNames_definedByThePalette() {
        var named = Pattern.compile("var\\((--fp-[a-z0-9-]+)\\)").matcher(served.get(href(PageStyle.class, "style", "plain")));
        String palette = served.get(href(PagePalette.class, "palette", "paper"));
        int count = 0;
        while (named.find()) {
            count++;
            assertTrue(palette.contains("    " + named.group(1) + ": "), named.group(1) + " is named by the style and not defined by the palette");
        }
        assertTrue(count > 5, "the style names the palette's colours: " + count);
    }

    @Test
    void pickingAStyle_swapsTheStylesSheetAlone() {
        pick("style", "ink");
        String ink = href(PageStyle.class, "style", "ink");
        assertEquals(List.of(ink), fetchAndLand(), "the style's sheet fetched again, and nothing of the palette's");
        assertTrue(served.get(ink).contains("/* tab: ink's own */"), served.get(ink));
        assertEquals(List.of(href(PagePalette.class, "palette", "paper") + " all", ink + " all"), live(), "plain's sheet gone, paper's untouched");
    }

    @Test
    void pickingAPalette_swapsThePalettesSheetAlone() {
        pick("palette", "night");
        String night = href(PagePalette.class, "palette", "night");
        assertEquals(List.of(night), fetchAndLand(), "the palette's sheet fetched again, and nothing of the style's");
        assertTrue(served.get(night).contains("    --fp-copy: oklch(0.93 0.01 260);\n"), served.get(night));
        assertEquals(List.of(href(PageStyle.class, "style", "plain") + " all", night + " all"), live(), "paper's sheet gone, plain's untouched");
    }

    @Test
    @SuppressWarnings("unchecked")
    void anElementWearsItsLeafsClasses_andTheSheetServedRulesThem() {
        String sheet = served.get(href(PageStyle.class, "style", "plain"));
        List<String> tab = js.eval("js", "globalThis.el = element(); StyledLeaf.wear(el, leaves.tab); StyledLeaf.hold(el, leaves.tab, 'selected', true); el.classes()")
                .as(List.class);
        assertEquals(List.of("styled-tab", "styled-tab--selected"), tab);
        assertTrue(sheet.contains(".styled-tab {") && sheet.contains("&.styled-tab--selected {"), "the tab's class and its state's, ruled");
        List<String> lead = js.eval("js", "globalThis.el2 = element(); StyledLeaf.wear(el2, leaves['tab_lead']); StyledLeaf.hold(el2, leaves['tab_lead'], 'selected', true); el2.classes()")
                .as(List.class);
        assertEquals(List.of("styled-tab_lead", "styled-tab_lead--selected"), lead, "the tab cast as Lead wears its casting's classes alone, none of the tab's on its own");
        assertTrue(sheet.contains(".styled-tab_lead {") && sheet.contains("&.styled-tab_lead--selected {"));
    }
}
