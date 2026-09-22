package hue.captains.singapura.js.homing.ui.focus;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The monitor over the real party and the real steward, on a fake DOM: one
 * row per node, indented by depth; the holder's row lit and named; a change
 * of structure or of holder redraws; a holder outside the tree is named
 * below it; dispose stops listening.
 */
class FocusMonitorTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, textContent: "",
                get firstChild() { return this.children[0] || null; },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); }, remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function () {}, removeEventListener: function () {},
                style: { setProperty: function (k, v) { props[k] = v; }, getPropertyValue: function (k) { return props[k] || ""; } },
                has: function (c) { return classes.has(c); }, prop: function (k) { return props[k]; },
                text: function () { return this.children.map(function (c) { return c.textContent; }).filter(Boolean).join(" "); } };
            return node;
        }
        function fakeBranch(name) {
            var kids = {};
            return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; },
                     createBranch: function (n) { var b = fakeBranch(n); kids[n] = b; return b; }, dissolveBranch: function (n) { delete kids[n]; log.push("dissolved:" + n); },
                     dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); }, removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); } };
        ["fm_tree", "fm_row", "fm_row_holder", "fm_kind", "fm_name", "fm_component", "fm_outside"].forEach(function (c) { globalThis[c] = c; });
        class Dock { constructor() {} }
        class Widget { constructor() {} keyDown(ev) { return true; } }
        var host = el("div");
        var monitor = new FocusMonitor(fakeBranch("monitor"), { host: host });
        function rows() { return monitor.root.children.filter(function (r) { return r.has("fm_row"); }).map(function (r) { return r.prop("--fm-depth") + ":" + r.text() + (r.has("fm_row_holder") ? "*" : ""); }); }
        function outside() { var o = monitor.root.children.filter(function (r) { return r.has("fm_outside"); }); return o.length ? o[0].textContent : null; }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", "var console = { error: function (m) { throw new Error(m); } };");
        loadModule(DIR + "component/party/PartyModule.js");
        loadModule(DIR + "component/keyboard/FocusPartyModule.js");
        loadModule(DIR + "component/keyboard/KeyboardSecretaryModule.js");
        loadModule(DIR + "component/keyboard/KeyboardEventsModule.js");
        loadModule(DIR + "component/keyboard/KeyboardStewardModule.js");
        loadModule(DIR + "ui/focus/FocusMonitorModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String rows() { return eval("rows().join(' | ')").asString(); }

    @Test
    void anEmptyTreeIsTheRootAlone_andNoOneHoldsTheKeys() {
        assertEquals("0:root no one holds the keys", rows());
        assertTrue(eval("outside() === null").asBoolean());
        assertTrue(eval("monitor.holderRow() === null").asBoolean());
    }

    @Test
    void everyNodeIsARowAtItsDepth_andTheHoldersRowIsLit() {
        eval("var dock = focusParty.root.createBranch('dock', new Dock()); var w = dock.join('widget', new Widget()); var c = focusParty.root.join('card', new Widget());");
        assertEquals("0:root no one holds the keys | 1:branch dock Dock | 2:leaf widget Widget | 1:leaf card Widget", rows(), "redrawn on every join");
        eval("KeyboardStewardInstance.claim(w)");
        assertEquals("0:root | 1:branch dock Dock | 2:leaf widget Widget* | 1:leaf card Widget", rows(), "the holder's row lit, the root's caption gone");
        assertEquals("widget", eval("monitor.holderRow().children[1].textContent").asString());
        assertEquals("true", eval("monitor.holderRow().getAttribute('aria-selected')").asString());
        eval("KeyboardStewardInstance.claim(c)");
        assertEquals("0:root | 1:branch dock Dock | 2:leaf widget Widget | 1:leaf card Widget*", rows(), "the keys moved: the row moved with them");
        eval("focusParty.root.createBranch('desk', new Dock()).adopt(w)");
        assertEquals("0:root | 1:branch dock Dock | 1:leaf card Widget* | 1:branch desk Dock | 2:leaf widget Widget", rows(), "a move redraws");
        eval("c.leave()");
        assertEquals("0:root no one holds the keys | 1:branch dock Dock | 1:branch desk Dock | 2:leaf widget Widget", rows(), "the holder left: no one holds, and the row is gone");
    }

    @Test
    void aHolderOutsideTheTreeIsNamedBelowIt() {
        eval("KeyboardStewardInstance.join('legacy/slider', {}); KeyboardStewardInstance.claim('legacy/slider')");
        assertEquals("0:root", rows());
        assertEquals("held outside the tree: legacy/slider", eval("outside()").asString());
        eval("KeyboardStewardInstance.leave('legacy/slider')");
        assertTrue(eval("outside() === null").asBoolean());
    }

    @Test
    void disposeStopsListening() {
        eval("log = []; monitor.dispose(); focusParty.root.join('late', new Widget())");
        assertEquals("0", eval("String(host.children.length)").asString(), "the tree taken out of the host");
        assertTrue(eval("log.join(' ')").asString().endsWith("dissolved:monitor"));
        eval("focusParty.root.dissolve()");
    }
}
