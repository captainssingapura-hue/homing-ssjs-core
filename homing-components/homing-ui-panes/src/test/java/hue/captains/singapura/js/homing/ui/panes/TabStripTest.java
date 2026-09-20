package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The strip alone over a fake DOM that knows its rectangles: the drag is a
 * rail — the chip follows the hand along the row at the press's offset,
 * within the slots, never off the rail however the hand wanders; the
 * chips it passes step aside and step back; let go, it lands on the slot
 * it is nearest and the strip says so; every step is taken back at once.
 */
class TabStripTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/panes/";

    // three chips of 80 at a pitch of 80 on a strip 30 tall; the second is pinned in one test
    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, children: [], parentNode: null, listeners: {}, textContent: "", rect: { left: 0, top: 0, width: 0, height: 0 }, cancelled: 0,
                style: { setProperty: function (k, v) { props[k] = v; }, removeProperty: function (k) { delete props[k]; }, getPropertyValue: function (k) { return props[k] == null ? "" : props[k]; } },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); }, toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                insertBefore: function (c, ref) { if (c.parentNode) c.parentNode.removeChild(c); var i = ref ? this.children.indexOf(ref) : -1; if (i < 0) this.children.push(c); else this.children.splice(i, 0, c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                contains: function (c) { return c === this; },
                getBoundingClientRect: function () { return this.rect; },
                getAnimations: function () { var self = this; return [{ cancel: function () { self.cancelled++; } }]; },
                animate: function (frames, opts) { log.push("settle:" + this.children[0].textContent + ":" + frames[0].translate + "->" + frames[1].translate + "/" + opts.duration); },
                setPointerCapture: function () {}, releasePointerCapture: function () {},
                fire: function (t, ev) { var e = ev || {}; e.type = t; e.stopPropagation = e.stopPropagation || function () {}; e.preventDefault = e.preventDefault || function () {}; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); },
                has: function (c) { return classes.has(c); }, prop: function (k) { return props[k]; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { return el(tag); }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () {}, activate: function () {} }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); } };
        var getComputedStyle = function () { return { transitionDuration: "0.16s, 0.16s" }; };
        var mtp_strip = "mtp_strip", mtp_strip_loose = "mtp_strip_loose", mtp_chip = "mtp_chip", mtp_chip_label = "mtp_chip_label", mtp_chip_seated = "mtp_chip_seated",
            mtp_chip_dragging = "mtp_chip_dragging", mtp_chip_shifted = "mtp_chip_shifted",
            mtp_chip_close = "mtp_chip_close", mtp_drop_mark = "mtp_drop_mark", mtp_strip_tail = "mtp_strip_tail", mtp_add = "mtp_add", mtp_add_off = "mtp_add_off", mtp_pill = "mtp_pill";
        var strip = new TabStrip(fakeBranch("strip"), { onDrop: function (c, dest) { log.push("drop:" + c.children[0].textContent + "@" + dest); } });
        strip.el.rect = { left: 0, top: 0, width: 400, height: 30 };
        function make(names, pinned) {
            return names.map(function (n, i) {
                var c = strip.chip({ id: n.toLowerCase(), title: n, pinned: pinned && i === 0 }, { onSelect: function () { log.push("select:" + n); }, onClose: function () {} });
                c.rect = { left: 40 + 80 * i, top: 0, width: 80, height: 30 };
                return c;
            });
        }
        var chips = make(["A", "B", "C"], false);
        strip.arrange(chips);
        var A = chips[0], B = chips[1], C = chips[2];
        function x(c) { return c.prop("--mtp-drag-x") == null ? "-" : c.prop("--mtp-drag-x"); }
        function shifts() { return chips.map(function (c) { return c.has("mtp_chip_shifted") ? c.prop("--mtp-shift-x") : "."; }).join(" "); }
        function state(c) { return ["mtp_chip_seated", "mtp_chip_dragging", "mtp_chip_shifted"].filter(function (k) { return c.has(k); }).join("+") || "none"; }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(P + "TabDragModule.js");
        loadModule(P + "TabHandModule.js");
        loadModule(P + "TabStripModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void theChipFollowsTheHandAlongTheRail_neverOffIt_theOthersSteppingAsideLive() {
        assertEquals("mtp_chip_seated", eval("state(A)").asString());
        // press A 20 in; 6 along starts the drag; the hand wanders 200 down and 50 up: only x matters
        eval("A.fire('pointerdown', { button: 0, pointerId: 1, clientX: 60, clientY: 10, target: A }); A.fire('pointermove', { clientX: 66, clientY: 10 });");
        assertEquals("mtp_chip_dragging", eval("state(A)").asString(), "in the hand: the design's word for a thing dragged");
        assertTrue(eval("strip.el.has('mtp_strip_loose')").asBoolean());
        eval("A.fire('pointermove', { clientX: 90, clientY: 210 });");
        assertEquals("30px", eval("x(A)").asString(), "30 along, the 200 down nothing");
        assertEquals(". . .", eval("shifts()").asString(), "under half a pitch: nobody moves");
        eval("A.fire('pointermove', { clientX: 110, clientY: -40 });");
        assertEquals("50px", eval("x(A)").asString());
        assertEquals(". -80px .", eval("shifts()").asString(), "past half a pitch: B steps one pitch left, live");
        eval("A.fire('pointermove', { clientX: 900, clientY: 10 });");
        assertEquals("160px", eval("x(A)").asString(), "kept within the rail: the last slot");
        assertEquals(". -80px -80px", eval("shifts()").asString());
        eval("A.fire('pointermove', { clientX: 125, clientY: 10 });");
        assertEquals("65px", eval("x(A)").asString());
        assertEquals(". -80px .", eval("shifts()").asString(), "back under two: C steps back");
    }

    @Test
    void letGo_theChipSettlesOntoItsSlot_everyStepTakenBackAtOnce_andTheStripSaysWhereItLanded() {
        eval("A.fire('pointerdown', { button: 0, pointerId: 1, clientX: 60, clientY: 10, target: A }); A.fire('pointermove', { clientX: 66, clientY: 10 }); A.fire('pointermove', { clientX: 150, clientY: 10 }); log.length = 0;");
        eval("A.fire('pointerup', { clientX: 150, clientY: 10 });");
        assertEquals("drop:A@1 settle:A:10px 0->0 0/160", log(), "landed on B's slot, then the last leg: from 10 past the slot onto it, in the design's 160ms");
        assertEquals("mtp_chip_seated", eval("state(A)").asString());
        assertEquals("-", eval("x(A)").asString());
        assertEquals(". . .", eval("shifts()").asString(), "B's step taken back");
        assertEquals("1", eval("String(B.cancelled)").asString(), "and not eased: B is where the arrangement puts it, not sliding there");
        assertTrue(eval("!strip.el.has('mtp_strip_loose')").asBoolean());
        // a drag that goes nowhere says nothing, and a cancelled one neither
        eval("log.length = 0; A.fire('pointerdown', { button: 0, pointerId: 2, clientX: 60, clientY: 10, target: A }); A.fire('pointermove', { clientX: 66, clientY: 10 }); A.fire('pointerup', { clientX: 66, clientY: 10 });");
        assertEquals("select:A settle:A:6px 0->0 0/160", log(), "its own slot: no drop, the settle only");
        eval("log.length = 0; A.fire('pointerdown', { button: 0, pointerId: 3, clientX: 60, clientY: 10, target: A }); A.fire('pointermove', { clientX: 150, clientY: 10 }); A.fire('pointercancel', {});");
        assertEquals("select:A settle:A:90px 0->0 0/160", log(), "cancelled: back onto its own slot, no drop");
        assertEquals(". . .", eval("shifts()").asString());
    }

    @Test
    void aPinnedChipIsNeitherDraggedNorPassed() {
        eval("chips = make(['P', 'Q', 'R'], true); strip.arrange(chips); var Pc = chips[0], Q = chips[1]; log.length = 0;");
        eval("Pc.fire('pointerdown', { button: 0, pointerId: 1, clientX: 60, clientY: 10, target: Pc }); Pc.fire('pointermove', { clientX: 200, clientY: 10 }); Pc.fire('pointerup', { clientX: 200, clientY: 10 });");
        assertEquals("select:P", log(), "pinned: selected, never dragged");
        eval("log.length = 0; Q.fire('pointerdown', { button: 0, pointerId: 2, clientX: 140, clientY: 10, target: Q }); Q.fire('pointermove', { clientX: 134, clientY: 10 }); Q.fire('pointermove', { clientX: 0, clientY: 10 });");
        assertEquals("0px", eval("x(Q)").asString(), "never before the pinned one: held at its own slot");
        assertEquals(". . .", eval("shifts()").asString());
        eval("Q.fire('pointerup', { clientX: 0, clientY: 10 });");
        assertEquals("select:Q", log(), "on its own slot already: nothing to settle");
    }
}
