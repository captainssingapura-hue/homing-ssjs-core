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
 * RFC 0064, D25 — the CSS manager handles what does not vary. Against a shimmed document (links
 * are plain objects the test fires): a provided group's tree loads dependencies first and applies
 * at once, with no theme on any sheet; a group loaded twice is fetched once; and the manager has
 * no theme and no switch - it follows no store, so a theme change reaches nothing here.
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
            globalThis.live = function () { return links.filter(function (l) { return !l.removed; }).map(function (l) { return l.href + " " + l.media; }); };
            globalThis.fire = function (part) { for (const l of links) if (!l.fired && !l.removed && l.href.indexOf(part) >= 0) { l.fired = true; l.onload(); } };
            // Palette, which G and D lean on, and Base, have no dependencies - so they load first.
            globalThis.SUB = { "Palette": { deps: [] }, "G": { deps: ["Palette", "D"] }, "D": { deps: ["Palette"] }, "Base": { deps: [] } };
            """;

    private Value css;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
        loadModule(DIR + "CssHandles.js");
        loadModule(DIR + "CssDependencyGraph.js");
        loadModule(DIR + "CssLoadProcedure.js");
        loadModule(DIR + "CssLinks.js");
        loadModule(DIR + "CssClassManager.js");
        css = global("CssClassManagerInstance");
    }

    private void tick() { js.eval("js", "undefined"); js.eval("js", "undefined"); }
    private void fire(String part) { js.eval("js", "fire('" + part + "'); undefined"); tick(); }
    @SuppressWarnings("unchecked")
    private List<String> live() { return js.eval("js", "live()").as(List.class); }

    @Test
    void loadCss_bringsTheWholeTree_dependenciesFirst_thenAppliesAtOnce_withNoTheme() {
        js.eval("js", "globalThis.done = false; CssClassManagerInstance.loadCss('G', { Palette: SUB.Palette, G: SUB.G, D: SUB.D }).then(() => globalThis.done = true); undefined");
        tick();
        assertEquals(List.of("/css-content?class=Palette not all"), live(), "what nothing else waits on first, alone until it lands - and no theme");
        fire("class=Palette");
        assertEquals("/css-content?class=D not all", live().get(1), "the dependency, by name, before the group");
        fire("class=D");
        assertEquals("/css-content?class=G not all", live().get(2));
        assertTrue(live().stream().allMatch(s -> s.endsWith("not all")));
        fire("class=G");
        assertTrue(live().stream().allMatch(s -> s.endsWith(" all")), "applied in one pass");
        assertTrue(js.eval("js", "done").asBoolean());
    }

    @Test
    void aSheetAlreadyLoaded_isFetchedOnce() {
        js.eval("js", "CssClassManagerInstance.loadCss('G', { Palette: SUB.Palette, G: SUB.G, D: SUB.D }); undefined");
        tick(); fire("class=Palette"); fire("class=D"); fire("class=G");
        js.eval("js", "CssClassManagerInstance.loadCss('D', { Palette: SUB.Palette, D: SUB.D }); undefined");
        tick();
        assertEquals(3, live().size(), "D and its palette are on the page already: " + live());
    }

    @Test
    void theManagerHasNoTheme_andNoSwitch() {
        assertTrue(js.eval("js", "CssClassManagerInstance.switchTheme === undefined && CssClassManagerInstance.theme === undefined"
                + " && CssClassManagerInstance.onThemeApplied === undefined").asBoolean(), "the theme's owner is ThemeSheets");
    }
}
