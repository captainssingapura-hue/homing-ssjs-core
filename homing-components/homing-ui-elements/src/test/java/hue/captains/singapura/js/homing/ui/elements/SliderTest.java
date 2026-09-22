package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The slider over a fake DOM: the number it keeps — clamped, on the step,
 * resting at a detent within the hand's bite — the hand on the rail, the
 * keys on the knob, the two events, the aria, the size on every part.
 */
class SliderTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/elements/SliderModule.js";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, listeners: {}, textContent: "", size: undefined,
                rect: { left: 100, top: 0, width: 200, height: 24, right: 300, bottom: 24 },
                style: { setProperty: function (k, v) { props[k] = v; }, removeProperty: function (k) { delete props[k]; }, getPropertyValue: function (k) { return props[k] == null ? "" : props[k]; } },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); }, toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; }, removeAttribute: function (k) { delete attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                getBoundingClientRect: function () { return this.rect; },
                setPointerCapture: function (id) { log.push("capture:" + id); },
                focus: function () { log.push("focus:" + this.tag); },
                fire: function (t, ev) { var e = ev || {}; e.target = e.target || this; if (!e.preventDefault) e.preventDefault = function () { e.defaulted = true; }; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); return e; },
                has: function (c) { return classes.has(c); }, attr: function (k) { return attrs[k]; }, prop: function (k) { return props[k]; }, listening: function (t) { return (this.listeners[t] || []).length; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function (o) { this.owner = String(o); } }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); }, size: function (e, s) { e.size = s; }, extent: function () {}, aspect: function () {} };
        var el_slider = "el_slider", el_slider_label = "el_slider_label", el_slider_rail = "el_slider_rail", el_slider_track = "el_slider_track", el_slider_fill = "el_slider_fill",
            el_slider_detent = "el_slider_detent", el_slider_knob = "el_slider_knob", el_slider_face = "el_slider_face", el_slider_mark = "el_slider_mark", el_slider_held = "el_slider_held", el_slider_face_held = "el_slider_face_held", el_slider_knob_current = "el_slider_knob_current", el_slider_readout = "el_slider_readout", el_slider_off = "el_slider_off",
            el_slider_vertical = "el_slider_vertical", el_slider_rail_vertical = "el_slider_rail_vertical", el_slider_rail_ticked = "el_slider_rail_ticked", el_slider_track_vertical = "el_slider_track_vertical",
            el_slider_fill_vertical = "el_slider_fill_vertical", el_slider_detent_vertical = "el_slider_detent_vertical", el_slider_cap = "el_slider_cap", el_slider_cap_face = "el_slider_cap_face", el_slider_cap_mark = "el_slider_cap_mark",
            el_slider_tick = "el_slider_tick", el_slider_tick_vertical = "el_slider_tick_vertical", el_slider_tick_line = "el_slider_tick_line", el_slider_tick_line_vertical = "el_slider_tick_line_vertical", el_slider_tick_label = "el_slider_tick_label";
        var page = fakeBranch("page");
        var s = new SliderBuilder().label("The tabs' size").axis().format(function (v) { return v.toFixed(1); })
                    .onInput(function (v) { log.push("in:" + v); }).onChange(function (v) { log.push("change:" + v); }).build(page.createBranch("size"));
        var rail = s.root.children[1], knob = rail.children[rail.children.length - 1], face = knob.children[0], mark = knob.children[1], detent = rail.children[1], readout = s.root.children[2];
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        // the icon component reads ICONS as it loads: the wardrobe's names first, as the served module would import them
        js.eval("js", "var ic_base = 'ic_base', ICONS = { grip: 'ic_grip', size: 'ic_size', aspect: 'ic_aspect', extent: 'ic_extent', level: 'ic_level' };");
        loadModule("/homing/js/hue/captains/singapura/js/homing/ui/icons/IconModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeysModule.js");
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void anAxis_restsAtNought_theFillFromTheDetent_theAriaAndTheReadoutSaid() {
        assertEquals("0", eval("String(s.value())").asString());
        assertEquals("50.000%", eval("rail.prop('--sl-value')").asString());
        assertEquals("50.000%", eval("rail.prop('--sl-rest')").asString());
        assertEquals("0.000%", eval("rail.prop('--sl-span')").asString(), "at the detent, no fill");
        assertEquals("slider", eval("knob.attr('role')").asString());
        assertEquals("-1,1,0,0.0,The tabs' size", eval("[knob.attr('aria-valuemin'), knob.attr('aria-valuemax'), knob.attr('aria-valuenow'), knob.attr('aria-valuetext'), knob.attr('aria-label')].join(',')").asString());
        assertEquals("0.0", eval("readout.textContent").asString());
        assertTrue(eval("detent.has('el_slider_detent') && knob.has('el_slider_knob')").asBoolean());
        assertTrue(eval("mark.has('ic_base') && mark.has('ic_grip')").asBoolean(), "a grip on the knob unless the slider says what it sets");
        assertEquals("grip", eval("s.icon()").asString());
        eval("s.icon('size');");
        assertTrue(eval("mark.has('ic_size') && !mark.has('ic_grip')").asBoolean());
        eval("s.icon(null);");
        assertTrue(eval("s.icon() === null && !mark.has('ic_size')").asBoolean(), "a bare knob");
        assertEquals("aspect", eval("new SliderBuilder().axis().icon('aspect').build(page.createBranch('a')).icon()").asString());
        eval("s.value(-0.7);");
        assertEquals("15.000%,35.000%", eval("rail.prop('--sl-from') + ',' + rail.prop('--sl-span')").asString(), "a value below the detent fills leftwards from it");
        assertEquals("-0.7", eval("String(s.value())").asString());
        eval("s.value(2); log = [];");
        assertEquals("1", eval("String(s.value())").asString(), "clamped");
        assertEquals("", log(), "a set by call says nothing: it is the caller's own doing");
    }

    @Test
    void theHandOnTheRail_jumpsGrabsAndRests_inputLive_changeOnRelease() {
        // the rail is 200px from 100: a press at 250 is three quarters along → 0.5 on the axis
        eval("log = []; var down = rail.fire('pointerdown', { button: 0, pointerId: 3, clientX: 250 });");
        assertEquals("capture:3 in:0.5 focus:div", log(), "captured at the press, the value set live, the knob focused");
        assertTrue(eval("down.defaulted && knob.has('el_slider_held')").asBoolean());
        assertEquals("75.000%", eval("rail.prop('--sl-value')").asString());
        eval("log = []; rail.fire('pointermove', { clientX: 206 });");
        assertEquals("in:0", log(), "206 is 0.06 on the axis: within the detent's bite of six tenths of a step, so it rests at nought");
        eval("log = []; rail.fire('pointermove', { clientX: 214 });");
        assertEquals("in:0.1", log(), "0.14 is past the bite: on the step");
        eval("log = []; rail.fire('pointermove', { clientX: 214 });");
        assertEquals("", log(), "the same value again is nothing");
        eval("log = []; rail.fire('pointerup', {});");
        assertEquals("change:0.1", log(), "released: the change, once, since the value moved from the press");
        assertFalse(eval("knob.has('el_slider_held')").asBoolean());
        assertEquals("0", eval("String(rail.listening('pointermove'))").asString(), "the hand's listeners are gone");
        eval("log = []; rail.fire('pointerdown', { button: 0, pointerId: 4, clientX: 220 }); rail.fire('pointermove', { clientX: 220 }); rail.fire('pointerup', {});");
        assertEquals("capture:4 in:0.2 focus:div change:0.2", log());
        eval("log = []; rail.fire('pointerdown', { button: 2, pointerId: 5, clientX: 100 });");
        assertEquals("", log(), "a secondary button is nothing");
    }

    @Test
    void theKeys_stepShiftHomeEndAndPages_eachAChange_takenOrLeft() {
        assertEquals("0", eval("String(knob.listening('keydown'))").asString(), "no keydown listener of the slider's own: the keys come through the party");
        eval("s.value(0); log = [];");
        assertEquals("true,true,true", eval("[s.key({ key: 'ArrowRight' }), s.key({ key: 'ArrowUp' }), s.key({ key: 'ArrowLeft', shiftKey: true })].join()").asString());
        assertEquals("in:0.1 change:0.1 in:0.2 change:0.2 in:-0.8 change:-0.8", log(), "a step, a step, ten steps back");
        eval("log = []; s.key({ key: 'End' }); s.key({ key: 'Home' }); s.key({ key: 'PageUp' }); s.key({ key: 'PageDown' });");
        assertEquals("in:1 change:1 in:-1 change:-1 in:0 change:0 in:-1 change:-1", log());
        assertFalse(eval("s.key({ key: 'Tab' })").asBoolean(), "a key that is not the slider's is left");
        eval("log = [];");
        assertTrue(eval("s.key({ key: 'ArrowLeft' })").asBoolean(), "taken, though at the bottom already");
        assertEquals("", log(), "and nothing moved");
        eval("s.setOn(false)");
        assertFalse(eval("s.key({ key: 'ArrowRight' })").asBoolean(), "off: left");
    }

    /** Handed the page's steward, the slider is a member: the convention on its root, the keys through the member's handler, and it leaves on dispose. */
    @Test
    void withTheSteward_joinsClaimsByTheConventionAndLeaves() {
        eval("""
            var members = {}, kbLog = [];
            var kb = { enroll: function (root, id) { root._kb = id; return function () { delete root._kb; }; }, memberAt: function (el) { for (var x = el; x; x = x.parentNode) if (x._kb) return x._kb; return null; }, join: function (id, h) { members[id] = h; kbLog.push("join:" + id); return id; }, leave: function (id) { delete members[id]; kbLog.push("leave:" + id); },
                       claim: function (id) { kbLog.push("claim:" + id); }, release: function (id) { kbLog.push("release:" + id); } };
            var k = new SliderBuilder().axis().onChange(function (v) { log.push("k:" + v); }).keyboard(kb).build(page.createBranch("keyed"));
            var k2 = new SliderBuilder().axis().keyboard(kb, "other").build(page.createBranch("keyed2"));
            """);
        assertEquals("join:keyed join:other", eval("kbLog.join(' ')").asString(), "joined as the branch's name, or as said");
        assertEquals("1,0,0", eval("[k.root.listening('pointerdown'), k.root.listening('focusin'), k.root.listening('focusout')].join()").asString(), "the convention on the root: one press");
        eval("kbLog = []; k.root.fire('pointerdown', {}); k.root.fire('focusout', { relatedTarget: null });");
        assertEquals("claim:keyed", eval("kbLog.join(' ')").asString(), "a press claims; the focus leaving releases nothing");
        eval("log = [];");
        assertTrue(eval("members.keyed.keyDown({ key: 'ArrowUp' })").asBoolean(), "the member's handler is the slider's key()");
        assertEquals("k:0.1", log());
        eval("kbLog = []; k.dispose();");
        assertEquals("leave:keyed", eval("kbLog.join(' ')").asString());
        assertEquals("0", eval("String(k.root.listening('pointerdown'))").asString(), "the convention removed");
    }

    /**
     * Stood up as a fader: the value rises from the bottom, the knob is the cap,
     * a scale of ticks sits beside the track, each at its height with its caption.
     */
    @Test
    void stoodUp_theValueRisesFromTheBottom_theKnobIsTheCap_theTicksAtTheirHeights() {
        eval("""
            var f = new SliderBuilder().label("vocals").vertical().range(-60, 10, 1).detent(0).value(0).icon("level")
                        .ticks([{ at: 10, label: "+10" }, { at: 0, label: "0" }, { at: -20, label: "-20" }, { at: -60, label: "-inf" }, { at: 99, label: "off the scale" }])
                        .format(function (v) { return v + " dB"; }).onInput(function (v) { log.push("in:" + v); }).build(page.createBranch("vocals"));
            var vr = f.root.children[1]; vr.rect = { left: 100, top: 0, width: 24, height: 200, right: 124, bottom: 200 };
            var vk = vr.children[vr.children.length - 1];
            var vticks = vr.children.filter(function (c) { return c.has("el_slider_tick"); });
            function ins() { return log.filter(function (l) { return l.indexOf("in:") === 0; }).join(" "); }
            """);
        assertTrue(eval("f.root.has('el_slider_vertical') && vr.has('el_slider_rail_vertical') && vr.has('el_slider_rail_ticked')").asBoolean());
        assertTrue(eval("vk.has('el_slider_cap') && !vk.has('el_slider_knob') && vk.children[0].has('el_slider_cap_face') && vk.children[1].has('el_slider_cap_mark') && vk.children[1].has('ic_level')").asBoolean(), "the cap, its face, its mark");
        assertEquals("vertical", eval("vk.attr('aria-orientation')").asString());
        assertEquals("4", eval("String(vticks.length)").asString(), "a tick per value on the scale; one off it is dropped");
        assertEquals("100.000%,85.714%,57.143%,0.000%", eval("vticks.map(function (t) { return t.prop('--sl-tick'); }).join(',')").asString(), "each at its fraction of the length");
        assertTrue(eval("vticks[0].has('el_slider_tick_vertical') && vticks[0].children[0].has('el_slider_tick_line_vertical') && vticks[0].children[1].textContent === '+10'").asBoolean());
        assertEquals("85.714%", eval("vr.prop('--sl-value')").asString(), "at nought: six sevenths up");
        // the rail is 200 tall from 0: a press at y=50 is three quarters up, which is -60 + 0.75 * 70 = -7.5, so -7 on the step
        eval("log = []; vr.fire('pointerdown', { button: 0, pointerId: 9, clientX: 112, clientY: 50 });");
        assertEquals("in:-7", eval("ins()").asString(), "from the bottom, on the step");
        eval("log = []; vr.fire('pointermove', { clientX: 112, clientY: 200 }); vr.fire('pointerup', {});");
        assertEquals("in:-60", eval("ins()").asString(), "the bottom is the least");
        eval("f.size(1);");
        assertEquals("1", eval("String(vticks[0].size)").asString(), "the ticks take the size with the rest");
    }

    @Test
    void offIsInert_theSizeReachesEveryPart_disposeDissolves() {
        eval("s.setOn(false); log = [];");
        assertTrue(eval("s.root.has('el_slider_off') && knob.attr('aria-disabled') === 'true' && knob.attr('tabindex') === '-1'").asBoolean());
        eval("rail.fire('pointerdown', { button: 0, pointerId: 1, clientX: 300 }); knob.fire('keydown', { key: 'End' });");
        assertEquals("", log(), "off: neither the hand nor the keys");
        eval("s.setOn(true); s.size(0.5);");
        assertEquals("0.5", eval("String(s.root.size) + '/' + String(knob.size) + '/' + String(readout.size) + '/' + String(detent.size)").asString().split("/")[0]);
        assertEquals("0.5,0.5,0.5,0.5,0.5", eval("[s.root.size, knob.size, readout.size, detent.size, mark.size].join(',')").asString(), "the size is an element's own, so the slider carries it to every part, the mark too");
        eval("s.size(0);");
        assertTrue(eval("s.root.size === null && knob.size === null").asBoolean(), "nought is the design's: no var");
        eval("s.label('Aspect'); s.labelWidth('9em');");
        assertEquals("Aspect", eval("knob.attr('aria-label')").asString());
        assertEquals("9em", eval("s.root.children[0].prop('--sl-label')").asString(), "one label width, so stacked rails align");
        eval("s.labelWidth(null);");
        assertTrue(eval("s.root.children[0].prop('--sl-label') === undefined").asBoolean());
        eval("s.dispose();");
        assertEquals("dissolved:size", log());
        var plain = eval("new SliderBuilder().label('Volume').range(0, 100, 5).value(42).build(page.createBranch('vol'))");
        assertEquals("40", eval("String(new SliderBuilder().range(0, 100, 5).value(42).build(page.createBranch('v2')).value())").asString(), "on the step");
        assertTrue(eval("new SliderBuilder().range(0, 100, 5).value(42).build(page.createBranch('v3')).root.children[1].children.length === 2").asBoolean(), "no detent on a plain range: track and knob only");
        var ex = assertThrows(PolyglotException.class, () -> eval("new SliderBuilder().range(5, 5).build(page.createBranch('bad'))"));
        assertTrue(ex.getMessage().contains("max must be above min"), ex.getMessage());
        assertThrows(PolyglotException.class, () -> eval("new SliderBuilder().build(null)"));
    }
}
