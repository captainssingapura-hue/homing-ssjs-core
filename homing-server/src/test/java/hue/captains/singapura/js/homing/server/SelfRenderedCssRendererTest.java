package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A derived sheet that renders itself is its own renderer's, and its sheet is its answer for the slug. */
class SelfRenderedCssRendererTest {

    record Sheet() implements DerivedCss<Sheet>, SelfRenderedCss {
        @Override public String css(String themeSlug) { return "/* " + themeSlug + " */"; }
    }

    record Plain() implements DerivedCss<Plain> {}

    @Test
    void aSheetThatRendersItself_isRenderedPerTheme() {
        var r = SelfRenderedCssRenderer.INSTANCE;
        assertTrue(r.owns(new Sheet()));
        assertEquals(Optional.of("/* editorial */"), r.render(new Sheet(), "editorial"));
        assertFalse(r.owns(new Plain()), "a derived sheet another renderer derives");
        assertEquals(Optional.empty(), r.render(new Plain(), "editorial"));
    }
}
