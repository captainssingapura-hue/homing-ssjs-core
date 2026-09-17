package hue.captains.singapura.js.homing.theme.type;

import hue.captains.singapura.js.homing.core.CssVar;

import java.util.Set;

/**
 * The vocabulary of the global type palette — the semantic faces a component
 * sets type in, named for what the text is doing, never for a typeface.
 * Every declared body that names a face names one of these; a theme binds
 * the stacks. RFC 0066: the fourteen overrides Turbo C needed to change one
 * face were the measure of this vocabulary's absence.
 *
 * <p>Three roles, and no more until a fourth is measured: the studio's 251
 * classes set exactly these — Calibri once (the page), Georgia thirteen times
 * (titles, labels, headings), a monospace stack four times (code).</p>
 */
public final class HomingFonts {

    /** Running text — the page's default, prose, controls. */
    public static final CssVar FONT_BODY    = new CssVar("--font-body");
    /** Titles, labels, kickers, headings — the face that carries the identity. */
    public static final CssVar FONT_DISPLAY = new CssVar("--font-display");
    /** Code, identifiers, anything that must align. */
    public static final CssVar FONT_MONO    = new CssVar("--font-mono");

    public static final Set<CssVar> ALL = Set.of(FONT_BODY, FONT_DISPLAY, FONT_MONO);

    private HomingFonts() {}
}
