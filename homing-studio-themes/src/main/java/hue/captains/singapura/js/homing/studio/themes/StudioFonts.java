package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.designs.HouseFonts;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.type.HomingFonts;

import java.util.Map;

/**
 * RFC 0066 — the house stacks for the three faces, bound by every studio theme
 * that has no typographic identity of its own (seven of eleven). The others
 * bind their own: Letterpress a serif body, Turbo C and Retro 90s monospace
 * throughout, Brutalist a grotesque body and a black display.
 *
 * <p>One map, shared — the first palette provider reused across themes, and
 * the reason a provider is a thing distinct from a theme.</p>
 */
public final class StudioFonts {

    // The stacks are the designs' (homing-designs); the legacy type palette provides the same ones.
    public static final String BODY    = HouseFonts.BODY;
    public static final String DISPLAY = HouseFonts.DISPLAY;
    public static final String MONO    = HouseFonts.MONO;

    public static final Map<CssVar, String> HOUSE = Map.of(
            HomingFonts.FONT_BODY,    BODY,
            HomingFonts.FONT_DISPLAY, DISPLAY,
            HomingFonts.FONT_MONO,    MONO);

    private StudioFonts() {}
}
