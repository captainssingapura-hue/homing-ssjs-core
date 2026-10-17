package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0067, appendix Two Classes per Leaf - an element wears its leaf's class, one leaf's alone, and a state's class
 * while the state holds; every wrong name refused where it is written. The leaves are described as Java generates them.
 */
class StyledLeavesTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/server/";

    private static final String SHIMS = """
            globalThis.element = function () {
                const set = [];
                return { classList: {
                    add: function (c) { if (set.indexOf(c) < 0) set.push(c); },
                    remove: function (c) { const i = set.indexOf(c); if (i >= 0) set.splice(i, 1); },
                    contains: function (c) { return set.indexOf(c) >= 0; },
                    [Symbol.iterator]: function () { return set.slice()[Symbol.iterator](); }
                }, classes: function () { return set.slice(); } };
            };
            globalThis.tab = Object.freeze({ token: "tab", styled: "styled-tab", states: Object.freeze({
                enabled: Object.freeze({ "true": null, "false": "styled-tab--disabled" }),
                selected: Object.freeze({ "false": null, "true": "styled-tab--selected" }),
                keys: Object.freeze({ away: null, candidate: "styled-tab--candidate", held: "styled-tab--held" }) }) });
            globalThis.lead = Object.freeze({ token: "strip-lead", styled: "styled-strip-lead", states: Object.freeze({
                selected: Object.freeze({ "false": null, "true": "styled-strip-lead--selected" }) }) });
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
        for (String m : new String[]{"CssHandles.js", "CssDependencyGraph.js", "CssLoadProcedure.js", "CssLinks.js", "CssClassManager.js", "StyledLeaves.js"})
            loadModule(DIR + m);
    }

    @SuppressWarnings("unchecked")
    private List<String> classes(String script) { return js.eval("js", script + "; el.classes()").as(List.class); }

    private String refused(String script) {
        return assertThrows(PolyglotException.class, () -> js.eval("js", script)).getMessage();
    }

    @Test
    void anElement_wearsItsLeafsClass_andAStatesWhileItHolds() {
        assertEquals(List.of("styled-tab", "styled-tab--selected"),
                classes("globalThis.el = element(); StyledLeaf.wear(el, tab); StyledLeaf.hold(el, tab, 'selected', true)"));
        assertEquals(List.of("styled-tab"), classes("StyledLeaf.hold(el, tab, 'selected', false)"), "the rest wears no class");
        assertEquals(List.of("styled-tab", "styled-tab--held"),
                classes("StyledLeaf.hold(el, tab, 'keys', 'candidate'); StyledLeaf.hold(el, tab, 'keys', 'held')"),
                "a state's one value at a time: candidate taken off for held");
    }

    @Test
    void aPartsElement_wearsItsPartsClassesAlone() {
        assertEquals(List.of("styled-strip-lead", "styled-strip-lead--selected"),
                classes("globalThis.el = element(); StyledLeaf.wear(el, lead); StyledLeaf.hold(el, lead, 'selected', true)"));
        assertTrue(refused("StyledLeaf.wear(el, tab)").contains(
                "the element wears styled-strip-lead, and tab is another leaf - an element wears one leaf's classes, a part's its part's alone"));
    }

    @Test
    void everyWrongName_isRefusedWhereItIsWritten() {
        js.eval("js", "globalThis.el = element(); StyledLeaf.wear(el, tab); undefined");
        assertTrue(refused("StyledLeaf.hold(el, tab, 'hovered', true)").contains(
                "tab lists no state hovered; its states are enabled, selected, keys (a pseudo state is the browser's)"));
        assertTrue(refused("StyledLeaf.hold(el, tab, 'keys', 'lost')").contains("keys has no value lost; its values are away, candidate, held"));
        assertTrue(refused("StyledLeaf.hold(element(), tab, 'selected', true)").contains("wear the leaf before holding its states"));
        assertTrue(refused("StyledLeaf.wear(el, { styled: 'styled-x' })").contains("a leaf is described by its token, its class and its states"));
    }
}
