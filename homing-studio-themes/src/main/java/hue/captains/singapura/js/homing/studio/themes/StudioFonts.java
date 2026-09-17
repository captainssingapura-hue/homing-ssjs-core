package hue.captains.singapura.js.homing.studio.themes;

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

    public static final String BODY    = "\"Calibri\", \"Segoe UI\", system-ui, sans-serif";
    public static final String DISPLAY = "\"Georgia\", serif";
    public static final String MONO    = "\"Consolas\", \"Courier New\", monospace";

    public static final Map<CssVar, String> HOUSE = Map.of(
            HomingFonts.FONT_BODY,    BODY,
            HomingFonts.FONT_DISPLAY, DISPLAY,
            HomingFonts.FONT_MONO,    MONO);

    private StudioFonts() {}
}
