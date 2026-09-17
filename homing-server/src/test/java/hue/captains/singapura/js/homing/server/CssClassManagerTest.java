package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0064 — the manager with its three affiliates, against a shimmed
 * document (links are plain objects the test fires) and a shimmed steward.
 * Pins what a served group module gets from loadCss, what a switch does to
 * the page, A -> B -> A, a widget mounted after a switch, a failed switch,
 * and the manager following the store.
 */
class CssClassManagerTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/server/";

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
            globalThis.stored = null;
            globalThis.changeListeners = [];
            globalThis.PreferenceViewInstance = {
                resolve: function (name, fallback) { return globalThis.stored || fallback || null; },
                onChange: function (fn) { globalThis.changeListeners.push(fn); return function () {}; }
            };
            globalThis.live = function () { return links.filter(function (l) { return !l.removed; }).map(function (l) { return l.href + " " + l.media; }); };
            globalThis.fire = function (part) { for (const l of links) if (!l.fired && !l.removed && l.href.indexOf(part) >= 0) { l.fired = true; l.onload(); } };
            globalThis.fireAll = function (theme) { for (const l of links) if (!l.fired && !l.removed && l.href.indexOf("theme=" + theme) >= 0) { l.fired = true; l.onload(); } };
            globalThis.failOne = function (part) { for (const l of links) if (!l.fired && !l.removed && l.href.indexOf(part) >= 0) { l.fired = true; l.onerror(); return; } };
            // RFC 0066: the server writes the palette (a prior) into every subgraph and every non-prior node lists it.
            globalThis.SUB = { "Palette": { deps: [], prior: true }, "G": { deps: ["Palette", "D"] }, "D": { deps: ["Palette"] }, "Base": { deps: [], prior: true } };
            """;

    private Value css;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
        loadModule(DIR + "CssHandles.js");
        loadModule(DIR + "CssDependencyGraph.js");
        loadModule(DIR + "CssLoadProcedure.js");
        loadModule(DIR + "CssClassManager.js");
        css = global("CssClassManagerInstance");
    }

    private void tick() { js.eval("js", "undefined"); js.eval("js", "undefined"); }
    private void fire(String part) { js.eval("js", "fire('" + part + "'); undefined"); tick(); }
    private void fireAll(String theme) { for (int i = 0; i < 8; i++) { js.eval("js", "fireAll('" + theme + "'); undefined"); tick(); } }
    @SuppressWarnings("unchecked")
    private List<String> live() { return js.eval("js", "live()").as(List.class); }
    private String theme() { Value v = css.invokeMember("theme"); return v.isNull() ? null : v.asString(); }

    private void pageUnderDefault() {
        js.eval("js", "globalThis.done = 0; CssClassManagerInstance.loadCss('Base', 'default', { Palette: SUB.Palette, Base: SUB.Base }).then(() => globalThis.done++);"
                + "CssClassManagerInstance.loadCss('G', 'default', { Palette: SUB.Palette, G: SUB.G, D: SUB.D }).then(() => globalThis.done++); undefined");
        tick();
        fireAll("default");
        assertEquals(2, js.eval("js", "done").asInt());
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    void loadCss_bringsTheWholeTree_dependenciesFirst_thenAppliesAtOnce() {
        js.eval("js", "globalThis.done = false; CssClassManagerInstance.loadCss('G', 'default', { Palette: SUB.Palette, G: SUB.G, D: SUB.D }).then(() => globalThis.done = true); undefined");
        tick();
        assertEquals(List.of("/css-content?class=Palette&theme=default not all"), live(), "the prior alone until it lands");
        fire("class=Palette");
        assertEquals("/css-content?class=D&theme=default not all", live().get(1), "the dependency, by name, before the group");
        fire("class=D");
        assertEquals("/css-content?class=G&theme=default not all", live().get(2));
        assertTrue(live().stream().allMatch(s -> s.endsWith("not all")));
        fire("class=G");
        assertTrue(live().stream().allMatch(s -> s.endsWith(" all")), "applied in one pass");
        assertTrue(js.eval("js", "done").asBoolean());
        assertEquals("default", theme());
    }

    @Test
    void aStoredPick_decidesTheThemeOverTheFallback() {
        js.eval("js", "globalThis.stored = 'forest'; undefined");
        js.eval("js", "CssClassManagerInstance.loadCss('D', 'default', { Palette: SUB.Palette, D: SUB.D }); undefined");
        tick();
        assertEquals(List.of("/css-content?class=Palette&theme=forest not all"), live());
    }

    // ── Switching ─────────────────────────────────────────────────────────────

    @Test
    void switchTheme_loadsEverythingUnderTheNewTheme_thenRetiresTheOld() {
        pageUnderDefault();
        assertEquals(4, live().size());
        js.eval("js", "globalThis.applied = null; CssClassManagerInstance.onThemeApplied(function (c) { globalThis.applied = c.from + '>' + c.to; });"
                + "globalThis.sw = null; CssClassManagerInstance.switchTheme('forest').then(t => globalThis.sw = t); undefined");
        tick();
        assertEquals("/css-content?class=Palette&theme=forest not all", live().get(4), "the new theme's sheets arrive after the old");
        fire("class=Palette&theme=forest");
        assertEquals("/css-content?class=Base&theme=forest not all", live().get(5), "the prior first");
        fire("class=Base&theme=forest");
        fire("class=D&theme=forest");
        assertTrue(live().subList(0, 4).stream().allMatch(s -> s.endsWith(" all")), "the old theme is still authoritative");
        assertEquals(null, js.eval("js", "applied").isNull() ? null : "early");
        fire("class=G&theme=forest");
        assertEquals(List.of("/css-content?class=Palette&theme=forest all",
                             "/css-content?class=Base&theme=forest all", "/css-content?class=D&theme=forest all",
                             "/css-content?class=G&theme=forest all"), live(), "only the new theme remains, all applied");
        assertEquals("forest", theme());
        assertEquals("default>forest", js.eval("js", "applied").asString());
        assertEquals("forest", js.eval("js", "sw").asString());
    }

    @Test
    void aToBToA_loadsAAgain_afterB() {
        pageUnderDefault();
        js.eval("js", "CssClassManagerInstance.switchTheme('forest'); undefined"); tick(); fireAll("forest");
        assertEquals("forest", theme());
        js.eval("js", "CssClassManagerInstance.switchTheme('default'); undefined"); tick(); fireAll("default");
        assertEquals("default", theme());
        assertEquals(4, live().size());
        assertTrue(live().stream().allMatch(s -> s.contains("theme=default") && s.endsWith(" all")));
    }

    @Test
    void aWidgetMountedAfterTheSwitch_arrivesInTheNewTheme() {
        pageUnderDefault();
        js.eval("js", "CssClassManagerInstance.switchTheme('forest'); undefined"); tick(); fireAll("forest");
        js.eval("js", "CssClassManagerInstance.loadCss('W', 'default', { Palette: SUB.Palette, W: { deps: ['Palette', 'D'] }, D: SUB.D }); undefined");
        tick();
        assertEquals("/css-content?class=W&theme=forest not all", live().get(live().size() - 1),
                "the served module still says default; the page wears forest — and D is already there");
    }

    @Test
    void aFailedSwitch_leavesTheOldThemeWhole() {
        pageUnderDefault();
        js.eval("js", "globalThis.err = null; CssClassManagerInstance.switchTheme('forest').catch(e => globalThis.err = e.message); undefined");
        tick(); fire("class=Palette&theme=forest");
        js.eval("js", "failOne('class=Base&theme=forest'); undefined"); tick();
        assertTrue(js.eval("js", "err").asString().contains("Failed to load CSS"));
        assertEquals(4, live().size());
        assertTrue(live().stream().allMatch(s -> s.contains("theme=default") && s.endsWith(" all")));
        assertEquals("default", theme());
    }

    @Test
    void switchingToTheThemeWorn_isANoOp() {
        pageUnderDefault();
        js.eval("js", "CssClassManagerInstance.switchTheme('default'); undefined"); tick();
        assertEquals(4, live().size());
    }

    // ── Following the store ───────────────────────────────────────────────────

    @Test
    void anotherTabsPick_reachesThisPage_throughTheSteward() {
        pageUnderDefault();
        js.eval("js", "globalThis.stored = 'carbon'; changeListeners.forEach(fn => fn()); undefined");
        tick();
        assertEquals("/css-content?class=Palette&theme=carbon not all", live().get(4));
        fireAll("carbon");
        assertEquals("carbon", theme());
    }

    @Test
    void aChangeThatResolvesToTheThemeWorn_doesNothing() {
        pageUnderDefault();
        js.eval("js", "globalThis.stored = 'default'; changeListeners.forEach(fn => fn()); undefined");
        tick();
        assertEquals(4, live().size());
    }
}
