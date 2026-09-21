package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mirror over a fake DOM: it draws the layout it is told from the
 * geometry at its scale, and nothing it measured; the cursor goes to the
 * first cell, moves by the arrows to the cell beside by the workspace's rule
 * and stops at an outer edge, goes where the pointer presses, follows a
 * re-arrangement that removed its cell, and says every move as data.
 */
class SplitGridMirrorTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/splitgrid/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            return { tag: tag, children: [], parentNode: null, listeners: {}, tabIndex: -1,
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); }, toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                style: { setProperty: function (k, v) { props[k] = v; }, getPropertyValue: function (k) { return props[k] || ""; } },
                get firstChild() { return this.children[0] || null; },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                focus: function () { log.push("focus"); },
                fire: function (t, ev) { var e = ev || {}; e.preventDefault = function () {}; e.stopPropagation = function () {}; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); },
                has: function (c) { return classes.has(c); }, prop: function (k) { return props[k]; } };
        }
        function fakeBranch(name) {
            return { name: name, createElement: function (n, tag) { return el(tag); },
                     createBranch: function (n) { return fakeBranch(n); }, dissolve: function () {}, activate: function () {} };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); } };
        var sgm_root = "sgm_root", sgm_cell = "sgm_cell", sgm_cell_current = "sgm_cell_current";
        var console = { error: function (m) { log.push("error:" + m); } };
        var host = el("div"), branch = fakeBranch("page");
        var LAYOUT = { kind: "split", orientation: "horizontal", children: [
            { node: { kind: "cell", id: "nav" }, ratio: 1 },
            { node: { kind: "split", orientation: "vertical", children: [
                { node: { kind: "cell", id: "demo" }, ratio: 3 },
                { node: { kind: "cell", id: "explain" }, ratio: 1 } ] }, ratio: 3 } ] };
        var mirror = new SplitGridMirror(branch.createBranch("mirror"), { host: host, scale: 0.5, onEvent: function (ev) { log.push(ev.kind === "CursorMoved" ? "cursor:" + ev.cellId + "/" + ev.by : ev.kind); } });
        mirror.reflect(LAYOUT, { w: 1007, h: 607 });
        function boxes() { return mirror.el.children.map(function (b) { return b.getAttribute("data-cell") + (b.has("sgm_cell_current") ? "*" : "") + "@" + [b.prop("--sgm-x"), b.prop("--sgm-y"), b.prop("--sgm-w"), b.prop("--sgm-h")].join(","); }).join(" "); }
        function key(k) { return mirror.key({ key: k }); }   // from whoever holds the keys for the mirror: no keydown listener of its own
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeysModule.js");
        loadModule(P + "SplitGridEventsModule.js");
        loadModule(P + "SplitGridTreeModule.js");
        loadModule(P + "SplitGridGeometryModule.js");
        loadModule(P + "SplitGridMirrorModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void itDrawsTheLayoutFromTheGeometry_atItsScale_withTheCursorOnTheFirstCell() {
        assertEquals("504px x 304px", eval("mirror.el.prop('--sgm-w') + ' x ' + mirror.el.prop('--sgm-h')").asString());
        assertEquals("nav*@0px,0px,125px,304px demo@129px,0px,375px,225px explain@129px,229px,375px,75px", eval("boxes()").asString());
        assertEquals("cursor:nav/call", log());
        assertEquals("0", eval("String(mirror.el.tabIndex)").asString(), "focusable");
        eval("mirror.scale(0.25);");
        assertEquals("252px x 152px", eval("mirror.el.prop('--sgm-w') + ' x ' + mirror.el.prop('--sgm-h')").asString());
        assertEquals("nav*@0px,0px,63px,152px demo@64px,0px,188px,113px explain@64px,114px,188px,38px", eval("boxes()").asString(), "redrawn at the new scale, the cursor kept");
    }

    @Test
    void theArrowsMoveTheCursorByTheWorkspacesRule_andStopAtAnEdge() {
        assertEquals("0", eval("String((mirror.el.listeners.keydown || []).length)").asString(), "the keys come through the party");
        assertEquals("true,true,true,true,true", eval("log.length = 0; [key('ArrowRight'), key('ArrowDown'), key('ArrowDown'), key('ArrowLeft'), key('ArrowUp')].join()").asString(), "an arrow is taken, at an edge too");
        assertFalse(eval("key('Enter')").asBoolean(), "another key is left");
        assertEquals("cursor:demo/right cursor:explain/down cursor:nav/left", log(), "down at the bottom and up at the top move nothing and say nothing");
        assertEquals("nav", eval("mirror.cursor()").asString());
        assertTrue(eval("mirror.el.children[0].has('sgm_cell_current') && mirror.el.children[0].getAttribute('aria-current') === 'true' && !mirror.el.children[1].has('sgm_cell_current')").asBoolean());
    }

    @Test
    void aPressPutsTheCursorThere_andAReflectThatLostTheCellMovesItToTheFirst() {
        eval("log.length = 0; mirror.el.children[2].fire('pointerdown', { button: 0 });");
        assertEquals("focus cursor:explain/pointer", log());
        eval("log.length = 0; mirror.reflect({ kind: 'split', orientation: 'horizontal', children: [ { node: { kind: 'cell', id: 'nav' } }, { node: { kind: 'cell', id: 'demo' } } ] }, { w: 1007, h: 607 });");
        assertEquals("cursor:nav/call", log());
        assertEquals("nav*@0px,0px,250px,304px demo@254px,0px,250px,304px", eval("boxes()").asString());
        eval("log.length = 0; mirror.cursor('demo'); mirror.cursor('demo');");
        assertEquals("cursor:demo/call", log(), "a call moves it once");
    }
}
