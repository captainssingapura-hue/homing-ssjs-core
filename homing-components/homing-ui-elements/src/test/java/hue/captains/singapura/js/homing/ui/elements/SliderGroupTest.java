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
 * The group over a fake DOM and a fake steward: one member for all its
 * sliders, the first current; Tab and Shift+Tab walk them, wrapping, and
 * put the focus on the knob; Escape gives the keys back and is left; the
 * rest goes to the current slider; the header's press keeps the focus on
 * the current knob; the focus arriving on a knob makes its slider current;
 * held and current are worn; dispose leaves and disposes the sliders.
 */
class SliderGroupTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), children: [], parentNode: null, listeners: {}, textContent: "",
                rect: { left: 100, top: 0, width: 200, height: 24, right: 300, bottom: 24 },
                style: { setProperty: function (k, v) { props[k] = v; }, removeProperty: function (k) { delete props[k]; }, getPropertyValue: function (k) { return props[k] == null ? "" : props[k]; } },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); }, toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                contains: function (o) { for (var x = o; x; x = x.parentNode) if (x === this) return true; return false; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; }, removeAttribute: function (k) { delete attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                getBoundingClientRect: function () { return this.rect; },
                setPointerCapture: function () {},
                focus: function () { log.push("focus:" + this.name); },
                fire: function (t, ev) { var e = ev || {}; e.target = e.target || this; if (!e.preventDefault) e.preventDefault = function () { e.defaulted = true; }; (this.listeners[t] || []).slice().forEach(function (fn) { fn(e); }); return e; },
                has: function (c) { return classes.has(c); }, attr: function (k) { return attrs[k]; }, listening: function (t) { return (this.listeners[t] || []).length; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = name + "/" + n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function (o) { this.owner = String(o); } }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); }, size: function () {}, extent: function () {}, aspect: function () {} };
        ["el_slider", "el_slider_label", "el_slider_rail", "el_slider_track", "el_slider_fill", "el_slider_detent", "el_slider_knob", "el_slider_face", "el_slider_mark", "el_slider_held", "el_slider_face_held",
         "el_slider_knob_current", "el_slider_readout", "el_slider_off", "el_slider_vertical", "el_slider_rail_vertical", "el_slider_rail_ticked", "el_slider_track_vertical", "el_slider_fill_vertical",
         "el_slider_detent_vertical", "el_slider_cap", "el_slider_cap_face", "el_slider_cap_mark", "el_slider_tick", "el_slider_tick_vertical", "el_slider_tick_line", "el_slider_tick_line_vertical", "el_slider_tick_label",
         "el_slider_group", "el_slider_group_held", "el_slider_group_header", "el_slider_group_title", "el_slider_group_body", "el_slider_group_body_across"
        ].forEach(function (c) { globalThis[c] = c; });
        // a steward that only records: the member's handlers are kept so the test can call them as the steward would
        var members = {}, kbLog = [];
        var kb = { join: function (id, h) { members[id] = h; kbLog.push("join:" + id); return id; }, leave: function (id) { delete members[id]; kbLog.push("leave:" + id); },
                   claim: function (id) { kbLog.push("claim:" + id); }, release: function (id) { kbLog.push("release:" + id); } };
        var page = fakeBranch("page");
        var g = new SliderGroupBuilder().title("Mixer").keyboard(kb).across().build(page.createBranch("mixer"));
        var a = g.add("a", new SliderBuilder().label("A").axis().onChange(function (v) { log.push("a:" + v); }));
        var b = g.add("b", new SliderBuilder().label("B").axis().onChange(function (v) { log.push("b:" + v); }));
        var c = g.add("c", new SliderBuilder().label("C").axis().onChange(function (v) { log.push("c:" + v); }));
        function knobOf(s) { var rail = s.root.children[1]; return rail.children[rail.children.length - 1]; }
        var header = g.root.children[0], body = g.root.children[1];
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", "var ic_base = 'ic_base', ICONS = { grip: 'ic_grip', size: 'ic_size', aspect: 'ic_aspect', extent: 'ic_extent', level: 'ic_level' };");
        loadModule(DIR + "ui/icons/IconModule.js");
        loadModule(DIR + "component/keyboard/KeysModule.js");
        loadModule(DIR + "ui/elements/SliderModule.js");
        loadModule(DIR + "ui/elements/SliderGroupModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }
    private String kbLog() { return eval("kbLog.join(' ')").asString(); }
    private String currentLabel() { return eval("g.current().label()").asString(); }

    @Test
    void oneMemberForAllTheSliders_theFirstCurrent_theSlidersInside() {
        assertEquals("join:mixer", kbLog(), "the group joined as its branch's name; the sliders did not");
        assertEquals("A", currentLabel());
        assertEquals("true,false,false", eval("[a, b, c].map(function (s) { return knobOf(s).has('el_slider_knob_current'); }).join()").asString());
        assertEquals("3", eval("String(body.children.length)").asString(), "the sliders' roots in the body");
        assertEquals("A,B,C", eval("g.sliders().map(function (s) { return s.label(); }).join()").asString());
        assertEquals("Mixer", eval("header.children[0].textContent").asString());
        assertTrue(eval("body.has('el_slider_group_body_across')").asBoolean());
        assertEquals("group", eval("g.root.attr('role')").asString());
        assertEquals("0", eval("String(knobOf(a).listening('keydown'))").asString(), "no keydown listener anywhere: the keys come through the party");
    }

    @Test
    void tabWalksTheSlidersWrappingAndPutsTheFocusOnTheKnob() {
        eval("log = []");
        assertTrue(eval("members.mixer.keyDown({ key: 'Tab' })").asBoolean(), "taken");
        assertEquals("B", currentLabel());
        assertEquals("focus:b/knob", log(), "the group's choice: the physical focus to the current knob");
        eval("members.mixer.keyDown({ key: 'Tab' }); members.mixer.keyDown({ key: 'Tab' })");
        assertEquals("A", currentLabel(), "wrapped");
        eval("members.mixer.keyDown({ key: 'Tab', shiftKey: true })");
        assertEquals("C", currentLabel(), "and back, wrapping");
        assertEquals("false,false,true", eval("[a, b, c].map(function (s) { return knobOf(s).has('el_slider_knob_current'); }).join()").asString(), "the ring moved");
    }

    @Test
    void theRestGoesToTheCurrentSlider() {
        eval("log = []; members.mixer.keyDown({ key: 'Tab' })");
        eval("log = []");
        assertTrue(eval("members.mixer.keyDown({ key: 'ArrowUp' })").asBoolean());
        assertTrue(eval("members.mixer.keyDown({ key: 'ArrowDown', shiftKey: true })").asBoolean());
        assertEquals("b:0.1 b:-0.9", log(), "the current slider's, not the first's");
        assertEquals("0", eval("String(a.value())").asString());
        assertFalse(eval("members.mixer.keyDown({ key: 'F5' })").asBoolean(), "a key no slider takes is left");
    }

    @Test
    void escapeGivesTheKeysBackAndIsLeft() {
        eval("kbLog = []");
        assertFalse(eval("members.mixer.keyDown({ key: 'Escape' })").asBoolean());
        assertEquals("release:mixer", kbLog());
        assertEquals("A", currentLabel(), "the current stays");
    }

    @Test
    void heldIsWornWhileTheGroupHoldsTheKeys() {
        assertFalse(eval("g.root.has('el_slider_group_held')").asBoolean());
        eval("members.mixer.granted()");
        assertTrue(eval("g.root.has('el_slider_group_held')").asBoolean());
        eval("members.mixer.taken('other')");
        assertFalse(eval("g.root.has('el_slider_group_held')").asBoolean());
    }

    @Test
    void theHeaderPressKeepsTheDefaultOffAndFocusesTheCurrentKnob_theConventionClaims() {
        assertEquals("1,2,1", eval("[g.root.listening('pointerdown'), g.root.listening('focusin'), g.root.listening('focusout')].join()").asString(), "the convention on the root, and the group's own focusin for the current");
        eval("log = []; kbLog = []; g.current(2)");
        var e = eval("header.fire('pointerdown', {})");
        assertTrue(e.getMember("defaulted").asBoolean(), "the press does not blur");
        assertEquals("focus:c/knob", log());
        eval("g.root.fire('pointerdown', {})");   // the browser runs the root's capture listener for a press in the header; here, fired on the root
        assertEquals("claim:mixer", kbLog());
    }

    @Test
    void theFocusArrivingOnAKnobMakesItsSliderCurrent() {
        eval("g.root.fire('focusin', { target: knobOf(b) })");
        assertEquals("B", currentLabel());
        eval("g.root.fire('focusin', { target: header })");
        assertEquals("B", currentLabel(), "the focus elsewhere in the group changes nothing");
    }

    @Test
    void currentByIndexOrSliderAndOnlyOfTheGroup() {
        eval("g.current(c)");
        assertEquals("C", currentLabel());
        eval("g.current(1)");
        assertEquals("B", currentLabel());
        assertThrows(PolyglotException.class, () -> eval("g.current(new SliderBuilder().axis().build(page.createBranch('x')))"));
        assertThrows(PolyglotException.class, () -> eval("g.add('d', {})"), "add wants a builder");
    }

    @Test
    void disposeLeavesAndDisposesTheSliders() {
        eval("log = []; kbLog = []; g.dispose()");
        assertEquals("leave:mixer", kbLog());
        assertEquals("dissolved:a dissolved:b dissolved:c dissolved:mixer", log());
        assertEquals("0", eval("String(g.root.listening('pointerdown'))").asString(), "the convention removed");
        assertEquals("0", eval("String(g.sliders().length)").asString());
    }
}
