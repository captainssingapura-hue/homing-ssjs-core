package hue.captains.singapura.js.homing.ui.dialog;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The dialog's keys through the party, over a fake pane and a fake steward:
 * on open it joins and claims by call, evicting whoever held; the content is
 * asked first, then Escape closes and Enter fires the primary action outside
 * a form control; on close it leaves and gives the keys back to who held
 * before, when still a member, and not when someone else took them; Modality
 * only makes the rest inert and captures nothing.
 */
class DialogKeysTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/ui/dialog/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, listeners: {}, inert: false, disabled: false,
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); }, remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; },
                contains: function (o) { for (var x = o; x; x = x.parentNode) if (x === this) return true; return false; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function () {},
                focus: function () { document.activeElement = this; log.push("focus:" + this.tag); },
                style: { setProperty: function () {}, removeProperty: function () {} } };
            return node;
        }
        var document = { body: el("body"), activeElement: null, contains: function (o) { return this.body.contains(o); } };
        var window = { innerWidth: 1000, innerHeight: 800 };
        var page = el("div"); document.body.appendChild(page); document.activeElement = page;
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); }, removeClass: function () {}, size: function () {}, extent: function () {} };
        ["dl_scrim", "dl_layer", "dl_float", "dl_actions"].forEach(function (c) { globalThis[c] = c; });
        // a pane that is a frame with a body; a button builder that makes a button element
        class FloatingPane { constructor(branch, o) { this.root = el("frame"); this.body = el("body"); this.root.appendChild(this.body); this._o = o; } setActive() {} title() { return this._o.title; } }
        class ButtonBuilder { label(l) { this._l = l; return this; } colour() { return this; } onClick(fn) { this._fn = fn; return this; } get tag() { return "button"; } build(e) { var fn = this._fn; e.addEventListener("click", fn); return { el: e, click: fn }; } }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} }; }
        var members = {}, kbLog = [], holder = null;
        var kb = { join: function (id, h) { members[id] = h; kbLog.push("join:" + id); return id; }, leave: function (id) { delete members[id]; if (holder === id) holder = null; kbLog.push("leave:" + id); },
                   claim: function (id) { if (!members[id]) throw new Error("no member " + id); kbLog.push("claim:" + id); holder = id; }, release: function () {}, holder: function () { return holder; }, has: function (id) { return !!members[id]; } };
        members.slider = {}; holder = "slider";   // the page's slider holds before the dialog opens
        var content = { onKeydown: function (ev) { log.push("content:" + ev.key); return ev.key === "ArrowUp"; }, focusEl: null };
        function open(o) {
            return new Dialog(fakeBranch("dialog"), Object.assign({ title: "T", keyboard: kb, content: function () { return content; },
                actions: [{ id: "ok", label: "OK", primary: true, onClick: function () { log.push("ok"); } }], onClose: function () { log.push("closed"); } }, o || {}));
        }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", "var console = { error: function (m) { throw new Error(m); } };");
        loadModule(DIR + "ModalityModule.js");
        loadModule(DIR + "DialogModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }
    private String kbLog() { return eval("kbLog.join(' ')").asString(); }

    @Test
    void onOpenItJoinsAndClaimsByCall_evictingWhoeverHeld_andNothingCapturesOnTheDocument() {
        eval("var d = open()");
        assertEquals("join:dialog claim:dialog", kbLog());
        assertEquals("dialog", eval("holder").asString());
        assertEquals("0", eval("String((d.el.listeners.keydown || []).length)").asString(), "no keydown listener on the frame");
        assertTrue(eval("page.inert").asBoolean(), "modal: the page behind is inert");
        assertEquals("false", eval("String(document.body.listeners.keydown != null)").asString(), "Modality captures nothing");
    }

    @Test
    void theContentIsAskedFirst_thenEscapeCloses_andEnterFiresThePrimaryOutsideAFormControl() {
        eval("var d = open(); log = []");
        assertTrue(eval("members.dialog.keyDown({ key: 'ArrowUp', target: d.bodyEl })").asBoolean(), "the content took it");
        assertFalse(eval("members.dialog.keyDown({ key: 'F5', target: d.bodyEl })").asBoolean(), "left by everyone");
        assertTrue(eval("members.dialog.keyDown({ key: 'Enter', target: d.bodyEl })").asBoolean());
        assertFalse(eval("members.dialog.keyDown({ key: 'Enter', target: el('input') })").asBoolean(), "Enter in a form control is the control's");
        assertEquals("content:ArrowUp content:F5 content:Enter ok content:Enter", log());
        eval("log = []");
        assertTrue(eval("members.dialog.keyDown({ key: 'Escape', target: d.bodyEl })").asBoolean());
        assertEquals("content:Escape dissolved:dialog focus:div closed", log(), "content asked first; then closed, once, the physical focus given back by Modality");
    }

    @Test
    void onCloseItLeavesAndGivesTheKeysBackToWhoHeldBefore() {
        eval("var d = open(); kbLog = []; d.close()");
        assertEquals("leave:dialog claim:slider", kbLog(), "the slider held before; it holds again");
        assertFalse(eval("page.inert").asBoolean(), "the page is live again");
    }

    @Test
    void noRestorationWhenTheKeysWereTakenMeanwhile_orTheHolderIsGone() {
        eval("var d = open(); members.other = {}; holder = 'other'; kbLog = []; d.close()");
        assertEquals("leave:dialog", kbLog(), "someone took the keys from the dialog: theirs to keep");
        eval("holder = 'slider'; var d2 = open(); delete members.slider; kbLog = []; d2.close()");
        assertEquals("leave:dialog", kbLog(), "the one who held before is gone: no claim for it");
    }

    @Test
    void withoutAStewardTheDialogHasNoKeys() {
        eval("kbLog = []; var d = open({ keyboard: null }); d.close()");
        assertEquals("", kbLog());
    }
}
