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
                fire: function (t, ev) { var e = ev || {}; if (!e.preventDefault) e.preventDefault = function () { e.defaulted = true; }; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); return e; },
                has: function (c) { return classes.has(c); }, attr: function (k) { return attrs[k]; }, prop: function (k) { return props[k]; }, listening: function (t) { return (this.listeners[t] || []).length; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function (o) { this.owner = String(o); } }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); }, size: function (e, s) { e.size = s; }, extent: function () {}, aspect: function () {} };
        var el_slider = "el_slider", el_slider_label = "el_slider_label", el_slider_rail = "el_slider_rail", el_slider_track = "el_slider_track", el_slider_fill = "el_slider_fill",
            el_slider_detent = "el_slider_detent", el_slider_knob = "el_slider_knob", el_slider_held = "el_slider_held", el_slider_readout = "el_slider_readout", el_slider_off = "el_slider_off";
        var page = fakeBranch("page");
        var s = new SliderBuilder().label("The tabs' size").axis().format(function (v) { return v.toFixed(1); })
                    .onInput(function (v) { log.push("in:" + v); }).onChange(function (v) { log.push("change:" + v); }).build(page.createBranch("size"));
        var rail = s.root.children[1], knob = rail.children[rail.children.length - 1], detent = rail.children[1], readout = s.root.children[2];
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        // the icon component reads ICONS as it loads: the wardrobe's names first, as the served module would import them
        js.eval("js", "var ic_base = 'ic_base', ICONS = { grip: 'ic_grip', size: 'ic_size', aspect: 'ic_aspect', extent: 'ic_extent', level: 'ic_level' };");
        loadModule("/homing/js/hue/captains/singapura/js/homing/ui/icons/IconModule.js");
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
        assertTrue(eval("knob.children[0].has('ic_base') && knob.children[0].has('ic_grip')").asBoolean(), "a grip on the knob unless the slider says what it sets");
        assertEquals("grip", eval("s.icon()").asString());
        eval("s.icon('size');");
        assertTrue(eval("knob.children[0].has('ic_size') && !knob.children[0].has('ic_grip')").asBoolean());
        eval("s.icon(null);");
        assertTrue(eval("s.icon() === null && !knob.children[0].has('ic_size')").asBoolean(), "a bare knob");
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
    void theKeysOnTheKnob_stepShiftHomeEndAndPages_eachAChange() {
        eval("s.value(0); log = [];");
        eval("knob.fire('keydown', { key: 'ArrowRight' }); knob.fire('keydown', { key: 'ArrowUp' }); knob.fire('keydown', { key: 'ArrowLeft', shiftKey: true });");
        assertEquals("in:0.1 change:0.1 in:0.2 change:0.2 in:-0.8 change:-0.8", log(), "a step, a step, ten steps back");
        eval("log = []; knob.fire('keydown', { key: 'End' }); knob.fire('keydown', { key: 'Home' }); knob.fire('keydown', { key: 'PageUp' }); knob.fire('keydown', { key: 'PageDown' });");
        assertEquals("in:1 change:1 in:-1 change:-1 in:0 change:0 in:-1 change:-1", log());
        var e = eval("knob.fire('keydown', { key: 'Tab' })");
        assertFalse(e.hasMember("defaulted"), "a key that is not the slider's passes");
        eval("log = []; knob.fire('keydown', { key: 'ArrowLeft' });");
        assertEquals("", log(), "at the bottom already: nothing");
    }

    @Test
    void offIsInert_theSizeReachesEveryPart_disposeDissolves() {
        eval("s.setOn(false); log = [];");
        assertTrue(eval("s.root.has('el_slider_off') && knob.attr('aria-disabled') === 'true' && knob.attr('tabindex') === '-1'").asBoolean());
        eval("rail.fire('pointerdown', { button: 0, pointerId: 1, clientX: 300 }); knob.fire('keydown', { key: 'End' });");
        assertEquals("", log(), "off: neither the hand nor the keys");
        eval("s.setOn(true); s.size(0.5);");
        assertEquals("0.5", eval("String(s.root.size) + '/' + String(knob.size) + '/' + String(readout.size) + '/' + String(detent.size)").asString().split("/")[0]);
        assertEquals("0.5,0.5,0.5,0.5,0.5", eval("[s.root.size, knob.size, readout.size, detent.size, knob.children[0].size].join(',')").asString(), "the size is an element's own, so the slider carries it to every part, the mark too");
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
