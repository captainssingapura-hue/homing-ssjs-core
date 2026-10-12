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
 * RFC 0064, D25 — the theme's owner: the derived sheets and the switch, against a shimmed document
 * (links are plain objects the test fires) and a shimmed steward. Pins what a module that needs a
 * derived sheet gets from load, what a switch does to the page, A -> B -> A, a sheet asked for
 * after a switch, a failed switch, and the owner following the store.
 */
class ThemeSheetsTest extends JsModuleTestBase {

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
            """;

    private Value themes;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
        loadModule(DIR + "CssDependencyGraph.js");
        loadModule(DIR + "CssLoadProcedure.js");
        loadModule(DIR + "CssLinks.js");
        loadModule(DIR + "ThemeSheets.js");
        themes = global("ThemeSheetsInstance");
    }

    private void tick() { js.eval("js", "undefined"); js.eval("js", "undefined"); }
    private void fire(String part) { js.eval("js", "fire('" + part + "'); undefined"); tick(); }
    private void fireAll(String theme) { for (int i = 0; i < 4; i++) { js.eval("js", "fireAll('" + theme + "'); undefined"); tick(); } }
    @SuppressWarnings("unchecked")
    private List<String> live() { return js.eval("js", "live()").as(List.class); }
    private String theme() { Value v = themes.invokeMember("theme"); return v.isNull() ? null : v.asString(); }

    /** Two derived sheets the page's modules import - a lexicon's, a look's - under the default. */
    private void pageUnderDefault() {
        js.eval("js", "globalThis.done = 0; ThemeSheetsInstance.load(['Words'], 'default').then(() => globalThis.done++);"
                + "ThemeSheetsInstance.load(['Look'], 'default').then(() => globalThis.done++); undefined");
        tick();
        fireAll("default");
        assertEquals(2, js.eval("js", "done").asInt());
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    void load_bringsTheSheetsUnderTheThemeWorn_appliedAtOnce() {
        js.eval("js", "globalThis.done = false; ThemeSheetsInstance.load(['Words', 'Look'], 'default').then(() => globalThis.done = true); undefined");
        tick();
        assertEquals(List.of("/css-content?class=Words&theme=default not all", "/css-content?class=Look&theme=default not all"), live());
        fire("class=Words");
        assertTrue(live().stream().allMatch(s -> s.endsWith("not all")), "nothing applied until everything has landed");
        fire("class=Look");
        assertTrue(live().stream().allMatch(s -> s.endsWith(" all")), "applied in one pass");
        assertTrue(js.eval("js", "done").asBoolean());
        assertEquals("default", theme());
    }

    @Test
    void aStoredPick_decidesTheThemeOverTheFallback() {
        js.eval("js", "globalThis.stored = 'forest'; ThemeSheetsInstance.load(['Words'], 'default'); undefined");
        tick();
        assertEquals(List.of("/css-content?class=Words&theme=forest not all"), live());
    }

    // ── Switching ─────────────────────────────────────────────────────────────

    @Test
    void switchTheme_loadsEverythingUnderTheNewTheme_thenRetiresTheOld() {
        pageUnderDefault();
        assertEquals(2, live().size());
        js.eval("js", "globalThis.applied = null; ThemeSheetsInstance.onThemeApplied(function (c) { globalThis.applied = c.from + '>' + c.to; });"
                + "globalThis.sw = null; ThemeSheetsInstance.switchTheme('forest').then(t => globalThis.sw = t); undefined");
        tick();
        assertEquals(List.of("/css-content?class=Words&theme=forest not all", "/css-content?class=Look&theme=forest not all"), live().subList(2, 4),
                "the new theme's sheets arrive after the old");
        fire("class=Words&theme=forest");
        assertTrue(live().subList(0, 2).stream().allMatch(s -> s.endsWith(" all")), "the old theme is still authoritative");
        assertTrue(js.eval("js", "applied").isNull());
        fire("class=Look&theme=forest");
        assertEquals(List.of("/css-content?class=Words&theme=forest all", "/css-content?class=Look&theme=forest all"), live(),
                "only the new theme remains, all applied");
        assertEquals("forest", theme());
        assertEquals("default>forest", js.eval("js", "applied").asString());
        assertEquals("forest", js.eval("js", "sw").asString());
    }

    @Test
    void aToBToA_loadsAAgain_afterB() {
        pageUnderDefault();
        js.eval("js", "ThemeSheetsInstance.switchTheme('forest'); undefined"); tick(); fireAll("forest");
        assertEquals("forest", theme());
        js.eval("js", "ThemeSheetsInstance.switchTheme('default'); undefined"); tick(); fireAll("default");
        assertEquals("default", theme());
        assertEquals(2, live().size());
        assertTrue(live().stream().allMatch(s -> s.contains("theme=default") && s.endsWith(" all")));
    }

    @Test
    void aSheetAskedForAfterTheSwitch_arrivesInTheNewTheme() {
        pageUnderDefault();
        js.eval("js", "ThemeSheetsInstance.switchTheme('forest'); undefined"); tick(); fireAll("forest");
        js.eval("js", "ThemeSheetsInstance.load(['Glyphs'], 'default'); undefined");
        tick();
        assertEquals("/css-content?class=Glyphs&theme=forest not all", live().get(live().size() - 1),
                "the served module still says default; the page wears forest");
    }

    @Test
    void aFailedSwitch_leavesTheOldThemeWhole() {
        pageUnderDefault();
        js.eval("js", "globalThis.err = null; ThemeSheetsInstance.switchTheme('forest').catch(e => globalThis.err = e.message); undefined");
        tick(); fire("class=Words&theme=forest");
        js.eval("js", "failOne('class=Look&theme=forest'); undefined"); tick();
        assertTrue(js.eval("js", "err").asString().contains("Failed to load CSS"));
        assertEquals(2, live().size());
        assertTrue(live().stream().allMatch(s -> s.contains("theme=default") && s.endsWith(" all")));
        assertEquals("default", theme());
    }

    @Test
    void switchingToTheThemeWorn_isANoOp() {
        pageUnderDefault();
        js.eval("js", "ThemeSheetsInstance.switchTheme('default'); undefined"); tick();
        assertEquals(2, live().size());
    }

    // ── Following the store ───────────────────────────────────────────────────

    @Test
    void anotherTabsPick_reachesThisPage_throughTheSteward() {
        pageUnderDefault();
        js.eval("js", "globalThis.stored = 'carbon'; changeListeners.forEach(fn => fn()); undefined");
        tick();
        assertEquals("/css-content?class=Words&theme=carbon not all", live().get(2));
        fireAll("carbon");
        assertEquals("carbon", theme());
    }

    @Test
    void aPickForgotten_goesBackToThePagesDefault() {
        pageUnderDefault();
        js.eval("js", "globalThis.stored = 'carbon'; changeListeners.forEach(fn => fn()); undefined");
        tick();
        fireAll("carbon");
        assertEquals("carbon", theme());
        js.eval("js", "globalThis.stored = null; changeListeners.forEach(fn => fn()); undefined");
        tick();
        assertTrue(live().stream().anyMatch(l -> l.contains("theme=default not all")), "the default arrives again: " + live());
        fireAll("default");
        assertEquals("default", theme());
    }

    @Test
    void aChangeThatResolvesToTheThemeWorn_doesNothing() {
        pageUnderDefault();
        js.eval("js", "globalThis.stored = 'default'; changeListeners.forEach(fn => fn()); undefined");
        tick();
        assertEquals(2, live().size());
    }
}
