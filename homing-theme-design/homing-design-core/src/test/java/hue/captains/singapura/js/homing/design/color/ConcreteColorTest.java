package hue.captains.singapura.js.homing.design.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A concrete colour: checked when made, equal by its numbers, written one way. */
class ConcreteColorTest {

    @Test
    void sRgb_isReadFromEveryWrittenForm_andWrittenOneWay() {
        var navy = new ConcreteColor.Srgb(0x1f, 0x29, 0x37);
        for (String w : new String[]{"#1F2937", "#1f2937", " #1f2937 ", "rgb(31, 41, 55)", "rgb(31 41 55)", "rgba(31,41,55,1)", "rgb(31 41 55 / 100%)", "#1f2937ff"})
            assertEquals(navy, new ConcreteColor.Srgb(w), w);
        assertEquals("#1f2937", navy.css());
        assertEquals("#aabbcc", new ConcreteColor.Srgb("#abc").css());
        assertEquals("rgb(170 187 204 / 0.8)", new ConcreteColor.Srgb("#abcc").css());
    }

    @Test
    void sRgb_withAnAlpha_isWrittenWithASlash() {
        var tint = new ConcreteColor.Srgb("rgba(220, 38, 38, .10)");
        assertEquals(0.1, tint.alpha());
        assertEquals("rgb(220 38 38 / 0.1)", tint.css());
        assertEquals(tint, new ConcreteColor.Srgb("rgb(220 38 38 / 10%)"));
        assertEquals("transparent", new ConcreteColor.Srgb("transparent").css());
        assertEquals("transparent", new ConcreteColor.Srgb("rgba(0, 0, 0, 0)").css());
        assertEquals("rgb(255 255 255 / 0)", new ConcreteColor.Srgb(255, 255, 255, 0).css());
        assertEquals("rgb(0 0 0 / 0.502)", new ConcreteColor.Srgb("#00000080").css());
    }

    @Test
    void oklch_isReadFromItsWrittenForms_andWrittenOneWay() {
        var blue = new ConcreteColor.Oklch(0.62, 0.19, 259.8);
        for (String w : new String[]{"oklch(0.62 0.19 259.8)", "OKLCH(62% 0.19 259.8deg)", "oklch(.62 .19 259.8)", "oklch(0.62 0.19 259.8 / 1)"})
            assertEquals(blue, new ConcreteColor.Oklch(w), w);
        assertEquals("oklch(0.62 0.19 259.8)", blue.css());
        assertEquals(270, new ConcreteColor.Oklch("oklch(0.5 0.1 -90)").hue());
        assertEquals(0, new ConcreteColor.Oklch(0.5, 0.1, 720).hue());
        assertEquals("oklch(0.5 0 0 / 0.25)", new ConcreteColor.Oklch("oklch(50% 0 0 / 25%)").css());
    }

    @Test
    void theTwoSpaces_areNeverEqual() {
        assertNotEquals(new ConcreteColor.Srgb("#000000"), new ConcreteColor.Oklch("oklch(0 0 0)"));
    }

    @Test
    void whatIsNoColour_isRefusedWhenMade() {
        for (String w : new String[]{"", "red", "#12", "#12345", "#gggggg", "rgb(1, 2)", "rgb(256, 0, 0)", "rgb(1.5, 2, 3)", "rgb(1, 2, 3, 2)",
                "rgb(1, 2, 3 / 0.5)", "rgb(1 2 3 / 0.5 / 1)", "hsl(0 0% 0%)", "color-mix(in srgb, red, blue)", "var(--accent)", "currentColor"})
            assertThrows(IllegalArgumentException.class, () -> new ConcreteColor.Srgb(w), w);
        for (String w : new String[]{"oklch(1.2 0 0)", "oklch(0.5 -0.1 0)", "oklch(0.5 0.1)", "oklch(0.5 0.1 x)", "rgb(0 0 0)"})
            assertThrows(IllegalArgumentException.class, () -> new ConcreteColor.Oklch(w), w);
        assertThrows(IllegalArgumentException.class, () -> new ConcreteColor.Srgb(0, 0, 300));
        assertThrows(IllegalArgumentException.class, () -> new ConcreteColor.Srgb(0, 0, 0, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new ConcreteColor.Oklch(0.5, 0.1, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new ConcreteColor.Srgb(null));
    }

    @Test
    void aKnot_isAtADegreeFromMinusOneToOne() {
        record Feedback() implements L1_ColorBranch<ColorScheme.Base> {}
        record Calm() implements Palettable<ColorScheme.Base, Feedback> {}
        assertEquals(0.5, new ColorScale.Knot<ColorScheme.Base>(new Calm(), 0.5).degree());
        assertThrows(IllegalArgumentException.class, () -> new ColorScale.Knot<ColorScheme.Base>(new Calm(), 1.5));
        assertThrows(IllegalArgumentException.class, () -> new ColorScale.Knot<ColorScheme.Base>(new Calm(), Double.NaN));
        assertEquals("calm", new Calm().token());
    }
}
