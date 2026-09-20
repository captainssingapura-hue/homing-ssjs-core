package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The strip alone with {@code floating}, over a fake DOM that knows its
 * rectangles: a chip pulled off the row stays the strip's own and floats
 * under the hand, the row closing behind it; let go, it is afloat where
 * it is; pressed again and brought back onto the strip, it is seated and
 * lands as any chip does.
 */
class TabStripTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/panes/";

    // three chips of 80 at a pitch of 80 on a strip 30 tall, the chips on its bottom edge
    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, children: [], parentNode: null, listeners: {}, textContent: "", rect: { left: 0, top: 0, width: 0, height: 0 },
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
                setPointerCapture: function () {}, releasePointerCapture: function () {},
                fire: function (t, ev) { var e = ev || {}; e.type = t; e.stopPropagation = e.stopPropagation || function () {}; e.preventDefault = e.preventDefault || function () {}; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); },
                has: function (c) { return classes.has(c); }, prop: function (k) { return props[k]; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { return el(tag); }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () {}, activate: function () {} }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); } };
        var mtp_strip = "mtp_strip", mtp_strip_loose = "mtp_strip_loose", mtp_chip = "mtp_chip", mtp_chip_label = "mtp_chip_label", mtp_chip_seated = "mtp_chip_seated",
            mtp_chip_dragging = "mtp_chip_dragging", mtp_chip_shifted = "mtp_chip_shifted", mtp_chip_floating = "mtp_chip_floating", mtp_chip_afloat = "mtp_chip_afloat",
            mtp_chip_close = "mtp_chip_close", mtp_drop_mark = "mtp_drop_mark", mtp_strip_tail = "mtp_strip_tail", mtp_add = "mtp_add", mtp_add_off = "mtp_add_off", mtp_pill = "mtp_pill";
        var strip = new TabStrip(fakeBranch("strip"), { floating: true,
            onDrop: function (c, dest) { log.push("drop:" + c.children[0].textContent + "@" + dest); },
            onFloat: function (c, e, grab) { log.push("float:" + c.children[0].textContent + " grab " + grab.x + "," + grab.y); },
            onLand: function (c, at) { log.push("land:" + c.children[0].textContent + " at " + at.x + "," + at.y); },
            onDragOut: function () { log.push("dragout"); } });
        strip.el.rect = { left: 0, top: 0, width: 400, height: 30, right: 400, bottom: 30 };
        var chips = ["A", "B", "C"].map(function (n, i) {
            var c = strip.chip({ id: n.toLowerCase(), title: n }, { onSelect: function () { log.push("select:" + n); }, onClose: function () {} });
            c.rect = { left: 40 + 80 * i, top: 0, width: 80, height: 30 };
            return c;
        });
        strip.arrange(chips);
        var B = chips[1];
        function vars(c) { return ["--mtp-drag-x", "--mtp-drag-y", "--mtp-float-x", "--mtp-float-y", "--mtp-shift-x"].map(function (k) { return c.prop(k) == null ? "-" : c.prop(k); }).join(" "); }
        function state(c) { return ["mtp_chip_seated", "mtp_chip_dragging", "mtp_chip_shifted", "mtp_chip_floating", "mtp_chip_afloat"].filter(function (k) { return c.has(k); }).join("+") || "none"; }
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
    void aChipPulledOffTheRowFloats_theRowClosingBehindIt_andIsLeftWhereTheHandLetsGo() {
        assertEquals("mtp_chip_seated", eval("state(B)").asString(), "seated to begin with");
        // press B 20 in and 10 down; a move along, then straight down until 25 of its 30 are off the strip
        eval("B.fire('pointerdown', { button: 0, pointerId: 1, clientX: 140, clientY: 10, target: B }); B.fire('pointermove', { clientX: 146, clientY: 10 });");
        assertEquals("mtp_chip_dragging", eval("state(B)").asString(), "in the hand: not seated");
        assertTrue(eval("strip.el.has('mtp_strip_loose')").asBoolean());
        eval("B.fire('pointermove', { clientX: 150, clientY: 25 });");
        assertEquals("10px 15px - - -", eval("vars(B)").asString(), "half off: still in the row, under the hand");
        eval("log.length = 0; B.fire('pointermove', { clientX: 150, clientY: 35 });");
        assertEquals("float:B grab 20,10", log(), "more than two thirds off: afloat, the strip's own still; no hand-off");
        assertEquals("mtp_chip_dragging+mtp_chip_floating", eval("state(B)").asString());
        assertEquals("- - 130px 25px -", eval("vars(B)").asString(), "where it was, in the strip's frame: 120 + 10 across, 25 down");
        assertTrue(eval("strip.floating(B) && !strip.floating(chips[0])").asBoolean());
        eval("B.fire('pointermove', { clientX: 300, clientY: 200 });");
        assertEquals("- - 280px 190px -", eval("vars(B)").asString(), "free under the hand, at the grab");
        eval("log.length = 0; B.fire('pointerup', { clientX: 300, clientY: 200 });");
        assertEquals("land:B at 280,190", log());
        assertEquals("mtp_chip_floating+mtp_chip_afloat", eval("state(B)").asString(), "afloat where it was let go");
        assertTrue(eval("strip.el.has('mtp_strip_loose')").asBoolean(), "the strip clips nothing while a chip is afloat");
        assertEquals("A,C", eval("strip._seated().map(function (c) { return c.children[0].textContent; }).join(',')").asString(), "the row closed behind it");
    }

    @Test
    void aChipAfloatPressedAgainIsInTheHand_andBroughtBackOntoTheStripIsSeatedAndLands() {
        eval("B.fire('pointerdown', { button: 0, pointerId: 1, clientX: 140, clientY: 10, target: B }); B.fire('pointermove', { clientX: 146, clientY: 10 }); B.fire('pointermove', { clientX: 150, clientY: 35 }); B.fire('pointerup', { clientX: 300, clientY: 200 });");
        eval("B.rect = { left: 280, top: 190, width: 80, height: 30 }; log.length = 0;");
        // press the floating chip 10 in and 5 down, carry it up until more than half of it is on the strip, over A's slot
        eval("B.fire('pointerdown', { button: 0, pointerId: 2, clientX: 290, clientY: 195, target: B }); B.fire('pointermove', { clientX: 296, clientY: 195 });");
        assertEquals("mtp_chip_dragging+mtp_chip_floating", eval("state(B)").asString(), "in the hand, still afloat");
        eval("B.fire('pointermove', { clientX: 60, clientY: 30 });");
        assertEquals("mtp_chip_dragging+mtp_chip_floating", eval("state(B)").asString(), "25 down of 30: only a sixth on, still afloat");
        eval("B.rect = { left: 120, top: 0, width: 80, height: 30 }; B.fire('pointermove', { clientX: 60, clientY: 15 });");
        assertEquals("mtp_chip_dragging", eval("state(B)").asString(), "10 down: two thirds on — seated again, in the hand");
        assertEquals("A,B,C", eval("strip._seated().map(function (c) { return c.children[0].textContent; }).join(',')").asString(), "back in the row at its place in the order");
        assertEquals("-70px 10px - - -", eval("vars(B)").asString(), "under the hand at the grab, over A's slot");
        assertEquals("80px", eval("chips[0].prop('--mtp-shift-x')").asString(), "A steps aside");
        eval("B.fire('pointerup', { clientX: 60, clientY: 15 });");
        assertEquals("select:B drop:B@0", log(), "landed on A's slot");
        assertEquals("mtp_chip_seated", eval("state(B)").asString());
        assertTrue(eval("!strip.el.has('mtp_strip_loose')").asBoolean(), "nothing loose: the strip clips again");
    }

    @Test
    void seat_putsAFloatingChipBackInTheRow_andWithoutFloatingTheHandOffIsAsBefore() {
        eval("B.fire('pointerdown', { button: 0, pointerId: 1, clientX: 140, clientY: 10, target: B }); B.fire('pointermove', { clientX: 146, clientY: 10 }); B.fire('pointermove', { clientX: 150, clientY: 35 }); B.fire('pointerup', { clientX: 300, clientY: 200 });");
        eval("strip.seat(B)");
        assertEquals("mtp_chip_seated", eval("state(B)").asString());
        assertEquals("A,B,C", eval("strip._seated().map(function (c) { return c.children[0].textContent; }).join(',')").asString());
        assertEquals("- - - - -", eval("vars(B)").asString());
        // a strip that does not float hands the chip off instead
        eval("var plain = new TabStrip(fakeBranch('plain'), { onDrop: function () {}, onDragOut: function (c, e, grab) { log.push('dragout:' + grab.x + ',' + grab.y); } });"
           + "plain.el.rect = strip.el.rect; var P = plain.chip({ id: 'p', title: 'P' }, { onSelect: function () {}, onClose: function () {} }); P.rect = { left: 40, top: 0, width: 80, height: 30 }; plain.arrange([P]); log.length = 0;"
           + "P.fire('pointerdown', { button: 0, pointerId: 3, clientX: 60, clientY: 10, target: P }); P.fire('pointermove', { clientX: 66, clientY: 10 }); P.fire('pointermove', { clientX: 66, clientY: 35 });");
        assertEquals("dragout:20,10", log());
        assertEquals("mtp_chip_seated", eval("state(P)").asString(), "handed off: the strip's part is over, the chip seated as far as it is concerned");
    }
}
