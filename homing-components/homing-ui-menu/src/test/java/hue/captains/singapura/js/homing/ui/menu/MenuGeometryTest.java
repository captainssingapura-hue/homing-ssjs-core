package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Where a menu goes and where its cursor goes, on numbers alone. */
class MenuGeometryTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/menu/MenuGeometryModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        js.eval("js", "var V = { w: 1000, h: 600 }, S = { w: 200, h: 150 }; function p(o) { return o.x + ',' + o.y; }");
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void place_atThePoint_flippedWhenItWouldRunOff_andKeptWithinTheMargin() {
        assertEquals("100,100", eval("p(MenuGeometry.place({ x: 100, y: 100 }, S, V))").asString(), "room enough: top-left at the point");
        assertEquals("700,100", eval("p(MenuGeometry.place({ x: 900, y: 100 }, S, V))").asString(), "off the right: flipped to the left of the point");
        assertEquals("100,350", eval("p(MenuGeometry.place({ x: 100, y: 500 }, S, V))").asString(), "off the bottom: flipped above the point");
        assertEquals("700,350", eval("p(MenuGeometry.place({ x: 900, y: 500 }, S, V))").asString(), "both");
        assertEquals("4,4", eval("p(MenuGeometry.place({ x: 100, y: 100 }, { w: 1200, h: 800 }, V))").asString(), "wider than the viewport: the margin, whatever else");
        assertEquals("796,446", eval("p(MenuGeometry.place({ x: 999, y: 599 }, S, V))").asString(), "flipped past the margin: shifted back to it");
    }

    @Test
    void beside_toTheRightOfTheRow_levelWithIt_flippedToTheLeft() {
        assertEquals("400,94", eval("p(MenuGeometry.beside({ left: 200, top: 100, right: 400, bottom: 130 }, S, V, 6))").asString(), "right of the frame, the first row level with the anchor: its inset above");
        assertEquals("700,94", eval("p(MenuGeometry.beside({ left: 900, top: 100, right: 1100, bottom: 130 }, S, V, 6))").asString(), "off the right: to the left of the frame");
        assertEquals("400,446", eval("p(MenuGeometry.beside({ left: 200, top: 580, right: 400, bottom: 610 }, S, V, 6))").asString(), "off the bottom: shifted up to the margin");
    }

    @Test
    void step_theNextEnabledRow_wrapping_fromTheEndsWhenFromNone() {
        eval("var E = [true, false, true, true];");
        assertEquals("2", eval("String(MenuGeometry.step(E, 0, 1))").asString(), "skips the disabled");
        assertEquals("0", eval("String(MenuGeometry.step(E, 3, 1))").asString(), "wraps");
        assertEquals("3", eval("String(MenuGeometry.step(E, 0, -1))").asString());
        assertEquals("0", eval("String(MenuGeometry.step(E, -1, 1))").asString(), "from none: the first");
        assertEquals("3", eval("String(MenuGeometry.step(E, -1, -1))").asString(), "from none, back: the last");
        assertEquals("-1", eval("String(MenuGeometry.step([false, false], -1, 1))").asString(), "none enabled");
        assertEquals("-1", eval("String(MenuGeometry.step([], -1, 1))").asString());
        assertEquals("1", eval("String(MenuGeometry.step([false, true, false], 1, 1))").asString(), "the only one: itself again");
    }
}
