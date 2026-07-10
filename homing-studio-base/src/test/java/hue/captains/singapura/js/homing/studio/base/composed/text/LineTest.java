package hue.captains.singapura.js.homing.studio.base.composed.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pins the {@link Line} length caps — the whole reason the type exists is to
 * move the {@value Line#MAX_CHARS}-char constraint into the type system.
 */
class LineTest {

    private static String repeat(int n) { return "a".repeat(n); }

    @Test
    @DisplayName("Plain: accepts a line at the cap and reports its length")
    void plainAtCap() {
        var line = new Line.Plain(repeat(Line.MAX_CHARS));
        assertEquals(Line.MAX_CHARS, line.effectiveLength());
    }

    @Test
    @DisplayName("Plain: one char over the cap throws")
    void plainOverCap() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> new Line.Plain(repeat(Line.MAX_CHARS + 1)));
        assertEquals(true, e.getMessage().contains("exceeds " + Line.MAX_CHARS));
    }

    @Test
    @DisplayName("Plain: null raw throws NPE naming the field")
    void plainNull() {
        var e = assertThrows(NullPointerException.class, () -> new Line.Plain(null));
        assertEquals(true, e.getMessage().contains("Line.Plain.raw"));
    }

    @Test
    @DisplayName("Articulated: effective length currently counts raw (marks not yet stripped)")
    void articulatedEffectiveIsRawForNow() {
        var line = new Line.Articulated(repeat(Line.MAX_CHARS));
        assertEquals(Line.MAX_CHARS, line.effectiveLength());
    }

    @Test
    @DisplayName("Articulated: one char over the effective cap throws")
    void articulatedOverCap() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> new Line.Articulated(repeat(Line.MAX_CHARS + 1)));
        assertEquals(true, e.getMessage().contains("exceeds " + Line.MAX_CHARS));
    }
}
