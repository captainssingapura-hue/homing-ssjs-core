package hue.captains.singapura.js.homing.ui.focus;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The steward monitor over the real steward, on a fake DOM: the lamp says
 * active or dormant on what has the native focus, redrawn on the steward's
 * events and the native focus moving; dispose stops listening.
 */
class StewardMonitorTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, textContent: "",
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); }, remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function () {}, removeEventListener: function () {},
                has: function (c) { return classes.has(c); } };
            return node;
        }
        function fakeBranch(name) {
            return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; },
                     dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); }, removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, on) { if (on) e.classList.add(c); else e.classList.remove(c); } };
        ["sm_lamp", "sm_lamp_dormant"].forEach(function (c) { globalThis[c] = c; });
        var focusListeners = 0;
        var document = { body: el("body"), activeElement: null, addEventListener: function () { focusListeners++; }, removeEventListener: function () { focusListeners--; } };
        document.activeElement = document.body;
        class Widget { constructor() {} keyDown(ev) { return ev.key === "ArrowUp"; } }
        var host = el("div");
        var monitor = new StewardMonitor(fakeBranch("monitor"), { host: host });
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", "var console = { error: function (m) { throw new Error(m); } };");
        loadModule(DIR + "component/party/PartyModule.js");
        loadModule(DIR + "component/keyboard/FocusPartyModule.js");
        loadModule(DIR + "component/keyboard/KeyboardSecretaryModule.js");
        loadModule(DIR + "component/keyboard/KeyboardEventsModule.js");
        loadModule(DIR + "component/keyboard/KeyboardWalkModule.js");
        loadModule(DIR + "component/keyboard/KeyboardStewardModule.js");
        loadModule(DIR + "ui/focus/StewardMonitorModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void theLampSaysActiveOrDormantOnWhatHasTheFocus() {
        assertEquals("active — the keys are the holder's", eval("monitor.lamp()").asString());
        assertFalse(eval("monitor.root.has('sm_lamp_dormant')").asBoolean());
        assertEquals("1", eval("String(host.children.length)").asString(), "the lamp alone");
        eval("var sel = el('select'); sel.setAttribute('aria-label', 'which leaf'); document.activeElement = sel; monitor.refresh()");
        assertEquals("dormant on select “which leaf” — its keys are its own", eval("monitor.lamp()").asString());
        assertTrue(eval("monitor.root.has('sm_lamp_dormant')").asBoolean());
        eval("var btn = el('button'); document.activeElement = btn; monitor.refresh()");
        assertEquals("dormant on button — its keys are its own", eval("monitor.lamp()").asString(), "no name: the tag alone");
        eval("document.activeElement = document.body; monitor.refresh()");
        assertTrue(eval("monitor.lamp()").asString().startsWith("active"));
        assertEquals(2, eval("focusListeners").asInt(), "focusin and focusout on the document");
        eval("var w = focusParty.root.join('widget', new Widget()); KeyboardStewardInstance.claim(w); w.leave()");
        assertTrue(eval("monitor.lamp()").asString().startsWith("active"), "redrawn on the steward's events, still active");
    }

    /** A walk on: the lamp names the member the keys are offered to, and is active again when the offer is off. */
    @Test
    void theLampNamesTheMemberAWalkOffersTheKeysTo() {
        eval("var a = focusParty.root.join('alpha', new Widget()), b = focusParty.root.join('beta', new Widget())");
        eval("KeyboardStewardInstance.offer(b)");
        assertEquals("active — the keys are offered to “beta”", eval("monitor.lamp()").asString());
        assertFalse(eval("monitor.root.has('sm_lamp_dormant')").asBoolean(), "a walk is the keyboard's: the steward is active");
        eval("document.activeElement = el('input'); monitor.refresh()");
        assertTrue(eval("monitor.lamp()").asString().startsWith("dormant"), "what is focused comes first");
        eval("document.activeElement = document.body; KeyboardStewardInstance.withdraw()");
        assertEquals("active — the keys are the holder's", eval("monitor.lamp()").asString());
        eval("a.leave(); b.leave()");
    }

    @Test
    void disposeStopsListening() {
        eval("log = []; monitor.dispose()");
        assertEquals("0", eval("String(host.children.length)").asString(), "taken out of the host");
        assertEquals(0, eval("focusListeners").asInt(), "the document let go");
        assertTrue(eval("log.join(' ')").asString().endsWith("dissolved:monitor"));
        eval("focusParty.root.dissolve()");
    }
}
