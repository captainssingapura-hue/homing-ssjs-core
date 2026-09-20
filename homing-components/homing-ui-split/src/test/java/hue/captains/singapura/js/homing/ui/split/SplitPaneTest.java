package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The splitter against a shimmed DOM: the tree is rendered as rows and
 * columns with a divider between neighbours, each child carries its share,
 * the leaves are hosts by slot id, a drag re-shares two neighbours within
 * the minimum and reports once on release, a cancel restores, setRatios
 * normalises and reports, and a bad layout is refused before anything is
 * built.
 */
class SplitPaneTest extends JsModuleTestBase {

    private static final String EVENTS = "/homing/js/hue/captains/singapura/js/homing/ui/split/SplitEventsModule.js";
    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/split/SplitPaneModule.js";

    // Elements with children, classes, attributes, custom properties and a
    // measurable size along either axis; a branch; the css manager; the class names.
    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            return { tag: tag, children: [], parentNode: null, listeners: {}, size: { width: 1000, height: 600 },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); }, toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                style: { setProperty: function (k, v) { props[k] = v; }, getPropertyValue: function (k) { return props[k] || ""; } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                getBoundingClientRect: function () { return { width: this.size.width, height: this.size.height, left: 0, top: 0 }; },
                setPointerCapture: function () {}, releasePointerCapture: function () {},
                fire: function (t, ev) { (this.listeners[t] || []).slice().forEach(function (fn) { fn(ev); }); },
                has: function (c) { return classes.has(c); }, cls: function () { return Array.from(classes).join(" "); } };
        }
        function fakeBranch(name) {
            return { name: name, dissolved: [], createElement: function (n, tag) { return el(tag); },
                     createBranch: function (n) { return fakeBranch(n); }, dissolveBranch: function (n) { this.dissolved.push(n); }, dissolve: function () { this.dissolved.push(name); }, activate: function () {} };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); },
                    extent: function (e, t) { if (t == null) e.style.setProperty("--extent", ""); else e.style.setProperty("--extent", String(t)); } };
        var sp_root = "sp_root", sp_split = "sp_split", sp_split_h = "sp_split_h", sp_split_v = "sp_split_v", sp_child = "sp_child",
            sp_child_h = "sp_child_h", sp_child_v = "sp_child_v", sp_leaf = "sp_leaf", sp_divider = "sp_divider", sp_divider_h = "sp_divider_h",
            sp_divider_v = "sp_divider_v", sp_divider_lit = "sp_divider_lit";
        function lit(d) { return (d.has("sp_divider_lit") ? "lit@" + d.style.getPropertyValue("--extent") : "unlit"); }
        var console = { error: function (m) { log.push("error:" + m); } };
        var host = el("div"), branch = fakeBranch("page");
        var LAYOUT = { kind: "split", orientation: "horizontal", children: [
            { pane: { kind: "leaf", slotId: "nav" }, ratio: 1 },
            { pane: { kind: "split", orientation: "vertical", children: [
                { pane: { kind: "leaf", slotId: "demo" }, ratio: 3 },
                { pane: { kind: "leaf", slotId: "explain" }, ratio: 1 } ] }, ratio: 3 } ] };
        var split = new SplitPane(branch.createBranch("split"), { host: host, layout: LAYOUT, minPanePx: 100,
            onEvent: function (ev) { log.push(ev.kind + ":" + ev.path + ":" + ev.ratios.map(function (r) { return r.toFixed(2); }).join(",")); } });
        var rootSplit = split.el.children[0];
        function shares(s) { return s.children.filter(function (c) { return c.has("sp_child"); }).map(function (c) { return (+c.style.getPropertyValue("--sp-ratio")).toFixed(2); }).join(","); }
        function shape(s) { return s.children.map(function (c) { return c.has("sp_divider") ? "|" : c.has("sp_child") ? (c.children[0].has("sp_leaf") ? c.children[0].getAttribute("data-slot") : "(" + shape(c.children[0]) + ")") : "?"; }).join(" "); }
        function divider(s, i) { return s.children.filter(function (c) { return c.has("sp_divider"); })[i]; }
        function drag(d, from, to, end) { d.fire("pointerdown", { button: 0, clientX: from.x, clientY: from.y, pointerId: 1 }); d.fire("pointermove", { clientX: to.x, clientY: to.y }); d.fire(end || "pointerup", { type: end || "pointerup" }); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(EVENTS);
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void theTreeIsRenderedWithADividerBetweenNeighboursAndEachChildsShare() {
        assertEquals("nav | (demo | explain)", eval("shape(rootSplit)").asString());
        assertTrue(eval("rootSplit.has('sp_split_h') && rootSplit.children[2].children[0].has('sp_split_v')").asBoolean());
        assertEquals("0.25,0.75", eval("shares(rootSplit)").asString(), "ratios are normalised");
        assertEquals("0.75,0.25", eval("shares(rootSplit.children[2].children[0])").asString());
        assertTrue(eval("divider(rootSplit, 0).has('sp_divider_h') && divider(rootSplit.children[2].children[0], 0).has('sp_divider_v')").asBoolean());
        assertEquals("vertical", eval("divider(rootSplit, 0).getAttribute('aria-orientation')").asString(), "a divider between side-by-side panes is a vertical separator");
        assertEquals("100px", eval("split.el.style.getPropertyValue('--sp-min')").asString());
        assertEquals("nav,demo,explain", eval("split.slots().join(',')").asString());
        assertEquals("demo", eval("split.slot('demo').getAttribute('data-slot')").asString());
        assertTrue(eval("split.slot('nope') === null").asBoolean());
    }

    @Test
    void theLayoutReadsBackWithTheSharesAsTheyAre() {
        assertEquals("{\"kind\":\"split\",\"orientation\":\"horizontal\",\"children\":[{\"pane\":{\"kind\":\"leaf\",\"slotId\":\"nav\"},\"ratio\":0.25},"
                + "{\"pane\":{\"kind\":\"split\",\"orientation\":\"vertical\",\"children\":[{\"pane\":{\"kind\":\"leaf\",\"slotId\":\"demo\"},\"ratio\":0.75},"
                + "{\"pane\":{\"kind\":\"leaf\",\"slotId\":\"explain\"},\"ratio\":0.25}]},\"ratio\":0.75}]}",
                eval("JSON.stringify(split.layout())").asString());
    }

    @Test
    void aDragReSharesTwoNeighboursAndReportsOnceOnRelease() {
        // the root split is 1000px wide; moving the divider 100px right gives nav 10% more
        eval("drag(divider(rootSplit, 0), { x: 250, y: 0 }, { x: 350, y: 0 })");
        assertEquals("0.35,0.65", eval("shares(rootSplit)").asString());
        assertEquals("RatioChanged::0.35,0.65", log());
        assertEquals(0.35, eval("split.layout().children[0].ratio").asDouble(), 1e-9);
        // the nested one is 600px tall; 60px up gives the demo 10% less
        eval("log = []; var inner = rootSplit.children[2].children[0]; inner.size = { width: 750, height: 600 }; drag(divider(inner, 0), { x: 0, y: 450 }, { x: 0, y: 390 })");
        assertEquals("0.65,0.35", eval("shares(inner)").asString());
        assertEquals("RatioChanged:1:0.65,0.35", log());
    }

    @Test
    void aDragIsHeldAtTheMinimumAndACancelRestores() {
        eval("drag(divider(rootSplit, 0), { x: 250, y: 0 }, { x: -500, y: 0 })");
        assertEquals("0.10,0.90", eval("shares(rootSplit)").asString(), "100px of 1000 is the floor");
        eval("log = []; drag(divider(rootSplit, 0), { x: 100, y: 0 }, { x: 900, y: 0 })");
        assertEquals("0.90,0.10", eval("shares(rootSplit)").asString(), "and the ceiling");
        eval("log = []; drag(divider(rootSplit, 0), { x: 900, y: 0 }, { x: 500, y: 0 }, 'pointercancel')");
        assertEquals("0.90,0.10", eval("shares(rootSplit)").asString(), "a cancel puts the shares back");
        assertEquals("", log(), "and says nothing");
        eval("var d = divider(rootSplit, 0); d.fire('pointerdown', { button: 0, clientX: 900, clientY: 0, pointerId: 1 })");
        assertEquals("lit@1", eval("lit(d)").asString(), "the handle wears the primary surface at full while held");
        eval("d.fire('pointerup', { type: 'pointerup' })");
        assertEquals("unlit", eval("lit(d)").asString(), "and nothing of its own again");
        assertEquals("", log(), "a press without a move is not a change");
    }

    @Test
    void theHandleWearsThePrimarySurfaceByExtentAsItIsHoveredAndHeld() {
        eval("var d = divider(rootSplit, 0)");
        assertEquals("unlit", eval("lit(d)").asString(), "nothing of its own at rest");
        eval("d.fire('pointerenter', {})");
        assertEquals("lit@0.4", eval("lit(d)").asString(), "part of the way while hovered");
        eval("d.fire('pointerdown', { button: 0, clientX: 250, clientY: 0, pointerId: 1 })");
        assertEquals("lit@1", eval("lit(d)").asString(), "at full while held");
        eval("d.fire('pointerleave', {})");
        assertEquals("lit@1", eval("lit(d)").asString(), "held is held, wherever the pointer goes");
        eval("d.fire('pointerup', { type: 'pointerup' })");
        assertEquals("unlit", eval("lit(d)").asString(), "released off the handle: rest");
        eval("d.fire('pointerenter', {}); d.fire('pointerdown', { button: 0, clientX: 250, clientY: 0, pointerId: 1 }); d.fire('pointerup', { type: 'pointerup' })");
        assertEquals("lit@0.4", eval("lit(d)").asString(), "released on the handle: hover");
    }

    @Test
    void setRatiosNormalisesAppliesAndReports() {
        eval("split.setRatios('1', [1, 3])");
        assertEquals("0.25,0.75", eval("shares(rootSplit.children[2].children[0])").asString());
        assertEquals("RatioChanged:1:0.25,0.75", log());
        assertTrue(assertThrows(PolyglotException.class, () -> eval("split.setRatios('2', [1, 1])")).getMessage().contains("no split at '2'"));
        assertTrue(assertThrows(PolyglotException.class, () -> eval("split.setRatios('', [1, 1, 1])")).getMessage().contains("2 ratios expected"));
        assertTrue(assertThrows(PolyglotException.class, () -> eval("split.setRatios('', [1, 0])")).getMessage().contains("positive"));
    }

    @Test
    void aBadLayoutIsRefusedBeforeAnythingIsBuilt() {
        for (String bad : new String[] {
                "{ kind: 'box' }",
                "{ kind: 'leaf', slotId: '' }",
                "{ kind: 'split', orientation: 'diagonal', children: [] }",
                "{ kind: 'split', orientation: 'horizontal', children: [ { pane: { kind: 'leaf', slotId: 'a' } } ] }",
                "{ kind: 'split', orientation: 'horizontal', children: [ { pane: { kind: 'leaf', slotId: 'a' } }, { pane: { kind: 'leaf', slotId: 'a' } } ] }",
                "{ kind: 'split', orientation: 'horizontal', children: [ { pane: { kind: 'leaf', slotId: 'a' }, ratio: -1 }, { pane: { kind: 'leaf', slotId: 'b' } } ] }" }) {
            var h = "var h2 = el('div'); new SplitPane(branch.createBranch('s'), { host: h2, layout: " + bad + " })";
            var ex = assertThrows(PolyglotException.class, () -> eval(h), bad);
            assertTrue(ex.getMessage().startsWith("Error: [SplitPane] "), ex.getMessage());
            assertEquals(0, eval("h2.children.length").asInt(), "nothing was attached");
        }
    }

    @Test
    void disposeTakesTheRootOutAndDissolvesTheBranch() {
        eval("split.dispose()");
        assertEquals(0, eval("host.children.length").asInt());
        assertEquals("split", eval("split._branch.dissolved[0]").asString(), "the branch it was given is dissolved");
        assertEquals("", eval("split.slots().join(',')").asString());
    }
}
