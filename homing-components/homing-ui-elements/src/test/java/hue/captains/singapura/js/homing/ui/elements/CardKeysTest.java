package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The card's keys through the party: no keydown listener of its own; Enter
 * or Space on the card with an action is the action, by key(ev); a card
 * handed the steward joins and claims by the convention; a card without an
 * action takes nothing; dispose leaves.
 */
class CardKeysTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, listeners: {}, textContent: "", tabIndex: -1,
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); }, remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                contains: function (o) { for (var x = o; x; x = x.parentNode) if (x === this) return true; return false; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                click: function () { (this.listeners.click || []).forEach(function (fn) { fn({ target: node }); }); },
                style: { setProperty: function () {}, removeProperty: function () {} },
                listening: function (t) { return (this.listeners[t] || []).length; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); }, removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    size: function () {}, extent: function () {}, aspect: function () {} };
        var HrefManagerInstance = { set: function () {} };
        ["el_button", "el_button_primary", "el_button_secondary", "el_button_danger", "el_button_warning", "el_button_success", "el_button_plain", "el_button_on", "el_button_off",
         "el_card", "el_card_action", "el_card_head", "el_card_title", "el_card_badge", "el_card_body", "el_card_text", "el_card_foot", "el_card_link"].forEach(function (c) { globalThis[c] = c; });
        var members = {}, kbLog = [];
        var kb = { join: function (id, h) { members[id] = h; kbLog.push("join:" + id); return id; }, leave: function (id) { delete members[id]; kbLog.push("leave:" + id); },
                   claim: function (id) { kbLog.push("claim:" + id); }, release: function (id) { kbLog.push("release:" + id); } };
        var page = fakeBranch("page");
        var card = new CardBuilder().title("Act").onClick(function () { log.push("clicked"); }).keyboard(kb).build(page.createBranch("act"));
        var inner = new CardBuilder().title("In a pane").onClick(function () { log.push("inner"); }).build(page.createBranch("inner"));
        var plain = new CardBuilder().title("Plain").build(page.createBranch("plain"));
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(DIR + "component/keyboard/KeysModule.js");
        loadModule(DIR + "ui/elements/Elements.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void noKeydownOfItsOwn_enterOrSpaceOnTheCardIsTheAction() {
        assertEquals("0", eval("String(card.root.listening('keydown'))").asString());
        assertTrue(eval("card.key({ key: 'Enter', target: card.root })").asBoolean());
        assertTrue(eval("card.key({ key: ' ', target: card.root })").asBoolean());
        assertEquals("clicked clicked", log());
        assertFalse(eval("card.key({ key: 'Escape', target: card.root })").asBoolean(), "another key is left");
        assertFalse(eval("card.key({ key: 'Enter', target: card.body })").asBoolean(), "Enter on something inside the card is not the card's");
    }

    @Test
    void handedTheSteward_joinsAndClaimsByTheConvention_leavesOnDispose() {
        assertEquals("join:act", eval("kbLog.join(' ')").asString(), "the card with the steward joined as its branch's name; the others did not");
        assertEquals("1,1,1", eval("[card.root.listening('pointerdown'), card.root.listening('focusin'), card.root.listening('focusout')].join()").asString());
        eval("kbLog = []; members.act.keyDown({ key: 'Enter', target: card.root })");
        assertEquals("clicked", log(), "the member's handler is key()");
        eval("card.dispose()");
        assertEquals("leave:act", eval("kbLog.join(' ')").asString());
        assertEquals("0", eval("String(card.root.listening('pointerdown'))").asString());
    }

    @Test
    void aCardInsideAHolderIsBuiltWithout_andTakesKeysByCall() {
        assertEquals("0", eval("String(inner.root.listening('pointerdown'))").asString(), "no convention: its holder hands it the keys");
        assertTrue(eval("inner.key({ key: 'Enter', target: inner.root })").asBoolean());
        assertEquals("inner", log());
    }

    @Test
    void aCardWithoutAnActionTakesNothing() {
        assertFalse(eval("plain.key({ key: 'Enter', target: plain.root })").asBoolean());
        assertEquals("", eval("plain.root.getAttribute('role') || ''").asString());
    }
}
