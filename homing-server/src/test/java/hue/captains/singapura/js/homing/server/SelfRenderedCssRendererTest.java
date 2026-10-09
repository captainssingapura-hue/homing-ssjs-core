package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A group that renders itself is its own renderer's, and its sheet is its answer for the slug. */
class SelfRenderedCssRendererTest {

    record Sheet() implements CssGroup<Sheet>, SelfRenderedCss {
        @Override public List<CssClass<Sheet>> cssClasses() { return List.of(); }
        @Override public String css(String themeSlug) { return "/* " + themeSlug + " */"; }
    }

    record Plain() implements CssGroup<Plain> {
        @Override public List<CssClass<Plain>> cssClasses() { return List.of(); }
    }

    @Test
    void aGroupThatRendersItself_isRenderedPerTheme_andVaries() {
        var r = SelfRenderedCssRenderer.INSTANCE;
        assertTrue(r.owns(new Sheet()));
        assertTrue(r.varies(new Sheet()));
        assertEquals(Optional.of("/* editorial */"), r.render(new Sheet(), "editorial"));
        assertFalse(r.owns(new Plain()));
        assertEquals(Optional.empty(), r.render(new Plain(), "editorial"));
    }
}
