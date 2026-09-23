package hue.captains.singapura.js.homing.ui.docking;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The protocol over a fake DOM with rectangles: a chip pulled off a strip
 * floats under the same hand, widget and all, and the dock has lost it; a
 * floating pane dragged over a dock is offered — the dock lit, the mark
 * where the tab would land — and dropped there becomes its tab; released
 * elsewhere it stays afloat. Every step is data on one sink, in order.
 */
class DockingTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/";

    // Elements that know their children, classes, attributes, inline
    // properties and a rectangle the test sets; a party branch; a css
    // manager over classList; the typed class names as strings.
    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, children: [], parentNode: null, listeners: {}, textContent: "", rect: { left: 0, top: 0, right: 0, bottom: 0, width: 0, height: 0 },
                style: { setProperty: function (k, v) { props[k] = v; }, removeProperty: function (k) { delete props[k]; }, getPropertyValue: function (k) { return props[k] == null ? "" : props[k]; } },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); },
                             toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                insertBefore: function (c, ref) { if (c.parentNode) c.parentNode.removeChild(c); var i = ref ? this.children.indexOf(ref) : -1; if (i < 0) this.children.push(c); else this.children.splice(i, 0, c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                removeAttribute: function (k) { delete attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                contains: function (c) { return c === this; },
                getBoundingClientRect: function () { return this.rect; },
                setPointerCapture: function () { log.push("capture:" + this.tag); }, releasePointerCapture: function () {},
                fire: function (t, ev) { var e = ev || {}; e.type = t; e.stopPropagation = e.stopPropagation || function () {}; e.preventDefault = e.preventDefault || function () {}; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); },
                has: function (c) { return classes.has(c); },
                prop: function (k) { return props[k]; } };
            return node;
        }
        function fakeBranch(name) {
            return { name: name,
                createElement: function (n, tag) { return el(tag); },
                createBranch: function (n) { return fakeBranch(n); },
                dissolve: function () {},
                activate: function (owner) { this.owner = String(owner); } };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); },
                    hasClass: function (e, c) { return e.classList.contains(c); },
                    extent: function (e, t) { if (t == null) e.style.removeProperty("--extent"); else e.style.setProperty("--extent", String(t)); },
                    size: function (e, s) { if (s == null) e.style.removeProperty("--size"); else e.style.setProperty("--size", String(s)); } };
        var fp_desk = "fp_desk", fp_desk_layer = "fp_desk_layer", fp_frame = "fp_frame", fp_hoverable = "fp_hoverable", fp_held = "fp_held", fp_active = "fp_active",
            fp_head = "fp_head", fp_head_held = "fp_head_held", fp_title = "fp_title", fp_close = "fp_close", fp_body = "fp_body", fp_grip = "fp_grip";
        var mtp_pane = "mtp_pane", mtp_strip = "mtp_strip", mtp_strip_loose = "mtp_strip_loose", mtp_chip = "mtp_chip", mtp_chip_label = "mtp_chip_label", mtp_chip_mark = "mtp_chip_mark", mtp_chip_mark_on = "mtp_chip_mark_on", mtp_chip_lifted = "mtp_chip_lifted", mtp_chip_dragging = "mtp_chip_dragging", mtp_chip_shifted = "mtp_chip_shifted",
            mtp_chip_seated = "mtp_chip_seated",
            mtp_chip_close = "mtp_chip_close", mtp_drop_mark = "mtp_drop_mark", mtp_strip_tail = "mtp_strip_tail", mtp_add = "mtp_add", mtp_add_off = "mtp_add_off",
            mtp_pill = "mtp_pill", mtp_content = "mtp_content", mtp_tab_content = "mtp_tab_content", mtp_tab_content_hidden = "mtp_tab_content_hidden",
            mtp_empty = "mtp_empty", mtp_dock_target = "mtp_dock_target";
        var console = { error: function (m, e) { log.push("error:" + m); } };
        var page = fakeBranch("page");
        var host = el("div");
        // a widget by the law: a member of the dock the tab is first added to, with activate(); afloat, a widget of the desk's
        function widget(key, into) { var w = { root: el("w-" + key), activate: function () { log.push(key + ":activate"); }, dispose: function () { log.push(key + ":disposed"); if (w.focus.in) w.focus.leave(); } }; w.focus = (into || A.focus).join(key, w); return w; }
        var sink = function (ev) {
            switch (ev.kind) {
                case "Opened": log.push("opened:" + ev.id); break;
                case "Released": log.push("released:" + ev.id); break;
                case "Closed": log.push("closed:" + ev.id); break;
                case "Raised": log.push("raised:" + ev.id); break;
                case "Moved": log.push("moved:" + ev.id); break;
                case "Docked": log.push("docked:" + ev.tabId + "@" + ev.slotId + "#" + ev.index); break;
                case "Undocked": log.push("undocked:" + ev.tabId + "<" + ev.slotId); break;
                case "TabAttached": log.push("attached:" + ev.slotId + ":" + ev.tab.id + "@" + ev.atIndex); break;
                case "TabActivated": log.push("active:" + ev.slotId + ":" + ev.tabId); break;
                case "TabAdded": log.push("added:" + ev.slotId + ":" + ev.tab.id); break;
                default: log.push(ev.kind);
            } };
        var docking = new Docking(page.createBranch("docking"), { host: host, onEvent: sink });
        docking.desk.root.rect = { left: 0, top: 0, right: 800, bottom: 600, width: 800, height: 600 };
        docking.desk.root.clientWidth = 800; docking.desk.root.clientHeight = 600;
        function dock(slotId, rect) {
            var h = el("div");
            var d = new MultiTabPane(page.createBranch(slotId), { host: h, slotId: slotId, onEvent: sink });
            d.el.rect = rect;
            d.el.children[0].rect = { left: rect.left, top: rect.top, right: rect.right, bottom: rect.top + 30, width: rect.right - rect.left, height: 30 };
            docking.addDock(d);
            return d;
        }
        var A = dock("a", { left: 0, top: 0, right: 400, bottom: 300 });
        var B = dock("b", { left: 400, top: 0, right: 800, bottom: 300 });
        var afloat = focusParty.root.createBranch("afloat", {});   // where a widget opened on the desk joins, until the desk holds a branch of its own
        A.addTab({ id: "t1", title: "One", widget: widget("w1") });
        A.addTab({ id: "t2", title: "Two", widget: widget("w2") });
        log.length = 0;
        function chips(d) { return d.el.children[0].children.filter(function (c) { return c.has("mtp_chip"); }); }
        chips(A).forEach(function (c, i) { c.rect = { left: 40 + 80 * i, top: 0, right: 120 + 80 * i, bottom: 30, width: 80, height: 30 }; });
        function chipOf(d, title) { return chips(d).find(function (c) { return c.children[0].textContent === title; }); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/party/PartyModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/FocusPartyModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardSecretaryModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardEventsModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardWalkModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardShortcutsModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardStewardModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeysModule.js");
        loadModule(P + "floating/FloatEventsModule.js");
        loadModule(P + "floating/FloatingPaneModule.js");
        loadModule(P + "floating/DeskModule.js");
        loadModule(P + "panes/PaneEventsModule.js");
        loadModule(P + "panes/TabDragModule.js");
        loadModule(P + "panes/TabHandModule.js");
        loadModule(P + "panes/TabStripModule.js");
        loadModule(P + "panes/PaneKeysModule.js");
        loadModule(P + "panes/PaneMenusModule.js");
        loadModule(P + "panes/MultiTabPaneModule.js");
        loadModule(P + "docking/DockEventsModule.js");
        loadModule(P + "docking/DockingModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void aDragAlongTheStripReorders_andNeverLeavesTheRail() {
        // press on Two (its slot 120..200, One's 40..120), 20px in; wander far to the right, far above and below the strip, then back over One's slot
        eval("var c = chipOf(A, 'Two'); c.fire('pointerdown', { button: 0, pointerId: 7, clientX: 140, clientY: 15, target: c });"
           + "c.fire('pointermove', { clientX: 146, clientY: 15 }); c.fire('pointermove', { clientX: 900, clientY: 15 });");
        assertEquals("0px", eval("c.prop('--mtp-drag-x')").asString(), "far right: kept within the row, on its own slot");
        assertTrue(eval("c.has('mtp_chip_dragging') && !c.has('mtp_chip_seated') && A.el.children[0].has('mtp_strip_loose')").asBoolean(), "in the hand: the design's word for a thing dragged, not for a thing seated");
        eval("c.fire('pointermove', { clientX: 110, clientY: -200 }); c.fire('pointermove', { clientX: 110, clientY: 400 });");
        assertEquals("-30px", eval("c.prop('--mtp-drag-x')").asString(), "far above, far below: the rail; part way to One's slot, still nearest its own");
        assertFalse(eval("chipOf(A, 'One').has('mtp_chip_shifted')").asBoolean());
        eval("c.fire('pointermove', { clientX: 50, clientY: 20 });");
        assertEquals("-80px", eval("c.prop('--mtp-drag-x')").asString(), "over One's slot, kept at the row's start");
        assertEquals("80px", eval("chipOf(A, 'One').prop('--mtp-shift-x')").asString(), "One steps aside, one pitch right, live");
        eval("c.fire('pointerup', { clientX: 50, clientY: 20 });");
        assertEquals("active:a:t2 capture:div TabMoved", log(), "the press activates, then a move and nothing else: no float opened, nothing undocked");
        assertEquals("t2,t1", eval("A.tabs().join(',')").asString(), "the drag reordered the dock");
        assertTrue(eval("!c.has('mtp_chip_dragging') && c.has('mtp_chip_seated') && !A.el.children[0].has('mtp_strip_loose') && c.prop('--mtp-drag-x') == null && !chipOf(A, 'One').has('mtp_chip_shifted') && chipOf(A, 'One').prop('--mtp-shift-x') == null").asBoolean(), "let go, nothing of the drag remains; seated again");
        assertEquals("0", eval("docking.desk.panes().length + ''").asString());
    }

    @Test
    void aTabUndockedByCallFloatsUnderTheHandAtTheGrab() {
        // a holder undocks by call with the pointer event and the grab — the press's offset within the chip — as the strip will once it pulls a tab off
        eval("docking.undock(A, { id: 't2' }, { clientX: 160, clientY: 90, pointerId: 7 }, { x: 20, y: 15 });");
        assertEquals("opened:t2 raised:t2 undocked:t2<a capture:header", log(), "detached — the active tab stays, so nothing else is activated; the desk opens it, then the undock is said; the hand is taken over");
        assertEquals("t1", eval("A.tabs().join(',')").asString(), "the dock lost the tab");
        assertEquals("w-w2", eval("docking.desk.pane('t2').body.children[0].tag").asString(), "the widget travelled, root and all");
        assertTrue(eval("docking.desk.pane('t2').root.has('fp_held')").asBoolean(), "the float is in the hand");
        assertEquals("140,75", eval("var b = docking.desk.pane('t2').bounds(); b.x + ',' + b.y").asString(), "the frame sits under the hand at the grab: the press's 20 and 15 within the chip");
    }

    @Test
    void aTabUndockedAtAPoint_aMenusPick_floatsWithNoHand() {
        eval("docking.undockAt(A, { id: 't2' }, { x: 160, y: 90 });");
        assertEquals("opened:t2 raised:t2 undocked:t2<a", log(), "the desk opens it, the undock is said, and no hand is taken");
        assertEquals("t1", eval("A.tabs().join(',')").asString(), "the dock lost the tab");
        assertEquals("w-w2", eval("docking.desk.pane('t2').body.children[0].tag").asString(), "the widget travelled");
        assertFalse(eval("docking.desk.pane('t2').root.has('fp_held')").asBoolean(), "not in any hand");
        assertEquals("100,76", eval("var b = docking.desk.pane('t2').bounds(); b.x + ',' + b.y").asString(), "its head at the point, the grip's offset in: 160-60, 90-14");
        eval("docking.undockAt(A, { id: 't1' }, { x: -50, y: -50 });");
        assertEquals("0,0", eval("var c = docking.desk.pane('t1').bounds(); c.x + ',' + c.y").asString(), "kept within the desk");
    }

    @Test
    void aFloatDraggedOverADockIsOffered_andDroppedThereBecomesItsTab() {
        eval("docking.desk.open({ id: 'f', title: 'Float', widget: widget('wf', afloat), x: 500, y: 400 }); log.length = 0;");
        eval("var head = docking.desk.pane('f').root.children[0]; head.fire('pointerdown', { button: 0, pointerId: 3, clientX: 520, clientY: 410, target: head });");
        // over B's strip: offered, B lit; over its content: not a landing
        eval("head.fire('pointermove', { clientX: 600, clientY: 20 });");
        assertTrue(eval("B.el.has('mtp_dock_target')").asBoolean(), "B is lit while the tab is offered");
        assertFalse(eval("A.el.has('mtp_dock_target')").asBoolean());
        eval("head.fire('pointermove', { clientX: 600, clientY: 200 });");
        assertFalse(eval("B.el.has('mtp_dock_target')").asBoolean(), "content is not a landing: docks may tile a box");
        // over A's strip, left of every chip: offered at 0 with the mark first
        eval("head.fire('pointermove', { clientX: 10, clientY: 10 });");
        assertTrue(eval("A.el.has('mtp_dock_target')").asBoolean());
        assertFalse(eval("B.el.has('mtp_dock_target')").asBoolean(), "the offer moved");
        assertEquals("mtp_drop_mark", eval("A.el.children[0].children[0].has('mtp_drop_mark') ? 'mtp_drop_mark' : A.el.children[0].children[0].tag").asString(), "the mark is first in the strip");
        eval("log.length = 0; head.fire('pointerup', { clientX: 10, clientY: 10 });");
        assertEquals("released:f attached:a:f@0 docked:f@a#0", log());
        assertEquals("f,t1,t2", eval("A.tabs().join(',')").asString());
        assertFalse(eval("docking.desk.has('f')").asBoolean());
        assertFalse(eval("A.el.has('mtp_dock_target')").asBoolean(), "the offer is over");
        assertEquals("w-wf", eval("A.contentElOf('f').children[0].tag").asString(), "the widget is in the dock's panel");
        assertEquals("a", eval("A.widgetOf('f').focus.in.name").asString(), "and its membership adopted into the dock's branch");
    }

    @Test
    void aFloatLetGoOffEveryDockStaysAfloat() {
        eval("docking.desk.open({ id: 'f', title: 'Float', widget: widget('wf', afloat), x: 500, y: 400 }); log.length = 0;");
        eval("var head = docking.desk.pane('f').root.children[0]; head.fire('pointerdown', { button: 0, pointerId: 3, clientX: 520, clientY: 410, target: head });"
           + "head.fire('pointermove', { clientX: 600, clientY: 20 }); head.fire('pointermove', { clientX: 600, clientY: 500 }); head.fire('pointerup', { clientX: 600, clientY: 500 });");
        assertEquals("capture:header moved:f", log());
        assertTrue(eval("docking.desk.has('f')").asBoolean());
        assertFalse(eval("B.el.has('mtp_dock_target')").asBoolean(), "the offer was withdrawn when the pointer left");
    }
}
