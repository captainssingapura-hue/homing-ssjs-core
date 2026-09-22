package hue.captains.singapura.js.homing.ui.focus;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The steward monitor over the real party and the real steward, on a fake
 * DOM: the lamp says active or dormant on what has the native focus; every
 * key the steward saw is a line, newest first, with where it went — the
 * holder and taken or left, native, no one, a Tab to its member, the
 * browser; the lines are capped; dispose stops listening.
 */
class StewardMonitorTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, textContent: "",
                get firstChild() { return this.children[0] || null; },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); }, remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function () {}, removeEventListener: function () {},
                has: function (c) { return classes.has(c); } };
            return node;
        }
        function fakeBranch(name) {
            var kids = {};
            return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; },
                     createBranch: function (n) { var b = fakeBranch(n); kids[n] = b; return b; }, dissolveBranch: function (n) { delete kids[n]; },
                     dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); }, removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, on) { if (on) e.classList.add(c); else e.classList.remove(c); } };
        ["sm_monitor", "sm_lamp", "sm_lamp_dormant", "sm_keys", "sm_key", "sm_key_name", "sm_key_route"].forEach(function (c) { globalThis[c] = c; });
        var focusListeners = 0;
        var document = { body: el("body"), activeElement: null, addEventListener: function () { focusListeners++; }, removeEventListener: function () { focusListeners--; } };
        document.activeElement = document.body;
        class Widget { constructor() {} keyDown(ev) { return ev.key === "ArrowUp"; } }
        var host = el("div");
        var monitor = new StewardMonitor(fakeBranch("monitor"), { host: host, keep: 3 });
        function key(k, target) { var ev = { key: k, target: target || document.body, defaultPrevented: false, preventDefault: function () { this.defaultPrevented = true; }, stopPropagation: function () {} }; KeyboardStewardInstance._onDown(ev); return ev; }
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
        loadModule(DIR + "ui/focus/StewardMonitorModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String lines() { return eval("monitor.lines().join(' | ')").asString(); }

    @Test
    void theLampSaysActiveOrDormantOnWhatHasTheFocus() {
        assertEquals("active — the keys are the holder's", eval("monitor.lamp()").asString());
        assertFalse(eval("monitor._lamp.has('sm_lamp_dormant')").asBoolean());
        eval("var sel = el('select'); sel.setAttribute('aria-label', 'which leaf'); document.activeElement = sel; monitor.refresh()");
        assertEquals("dormant on select “which leaf” — its keys are its own", eval("monitor.lamp()").asString());
        assertTrue(eval("monitor._lamp.has('sm_lamp_dormant')").asBoolean());
        eval("document.activeElement = document.body; monitor.refresh()");
        assertTrue(eval("monitor.lamp()").asString().startsWith("active"));
        assertEquals(2, eval("focusListeners").asInt(), "focusin and focusout on the document");
    }

    @Test
    void everyKeyIsALineWithWhereItWent_newestFirst_capped() {
        assertEquals("", lines());
        eval("key('ArrowUp')");
        assertEquals("ArrowUp → no one holds", lines());
        eval("var w = focusParty.root.join('widget', new Widget()); KeyboardStewardInstance.claim(w); key('ArrowUp'); key('Escape')");
        assertEquals("Escape → /widget · left | ArrowUp → /widget · taken | ArrowUp → no one holds", lines());
        eval("var input = el('input'); input.setAttribute('aria-label', 'search'); document.activeElement = input; key('x', input)");
        assertEquals("x → native: input “search” | Escape → /widget · left | ArrowUp → /widget · taken", lines(), "capped at three, newest first");
        eval("document.activeElement = document.body; key('Tab')");
        assertEquals("Tab → /widget (Tab)", eval("monitor.lines()[0]").asString(), "a Tab names the member it went to");
        eval("w.leave(); key('Tab')");
        assertEquals("Tab → the browser: no member to go to", eval("monitor.lines()[0]").asString());
        eval("key(' ')");
        assertEquals("Space → no one holds", eval("monitor.lines()[0]").asString(), "a space named");
    }

    @Test
    void disposeStopsListening() {
        eval("log = []; monitor.dispose(); key('ArrowUp')");
        assertEquals("0", eval("String(host.children.length)").asString(), "taken out of the host");
        assertEquals(0, eval("focusListeners").asInt(), "the document let go");
        assertTrue(eval("log.join(' ')").asString().endsWith("dissolved:monitor"));
        assertEquals(0, eval("KeyboardStewardInstance._traces.length").asInt(), "the trace let go");
        eval("focusParty.root.dissolve()");
    }
}
