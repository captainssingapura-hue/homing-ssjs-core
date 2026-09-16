package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.HomingVars;

import java.util.Set;

/**
 * The studio's former name for the vocabulary — RFC 0066 moved it to the
 * theme design core as {@link HomingVars}, where an app that is not a studio
 * can reach it. Every constant here IS the {@code HomingVars} one (same
 * {@link CssVar} instance), so a downstream that still says
 * {@code StudioVars.COLOR_ACCENT} binds the same token and passes the same
 * completeness gate. Migrate at leisure; this forwarder goes with the next
 * minor release.
 *
 * @deprecated use {@link HomingVars}
 */
@Deprecated(since = "0.8.4", forRemoval = true)
public final class StudioVars {

    public static final CssVar COLOR_SURFACE          = HomingVars.COLOR_SURFACE;
    public static final CssVar COLOR_SURFACE_RAISED   = HomingVars.COLOR_SURFACE_RAISED;
    public static final CssVar COLOR_SURFACE_RECESSED = HomingVars.COLOR_SURFACE_RECESSED;
    public static final CssVar COLOR_SURFACE_INVERTED = HomingVars.COLOR_SURFACE_INVERTED;

    public static final CssVar COLOR_TEXT_PRIMARY           = HomingVars.COLOR_TEXT_PRIMARY;
    public static final CssVar COLOR_TEXT_MUTED             = HomingVars.COLOR_TEXT_MUTED;
    public static final CssVar COLOR_TEXT_ON_INVERTED       = HomingVars.COLOR_TEXT_ON_INVERTED;
    public static final CssVar COLOR_TEXT_ON_INVERTED_MUTED = HomingVars.COLOR_TEXT_ON_INVERTED_MUTED;
    public static final CssVar COLOR_TEXT_LINK              = HomingVars.COLOR_TEXT_LINK;
    public static final CssVar COLOR_TEXT_LINK_HOVER        = HomingVars.COLOR_TEXT_LINK_HOVER;
    public static final CssVar COLOR_TEXT_TITLE             = HomingVars.COLOR_TEXT_TITLE;

    public static final CssVar COLOR_BORDER          = HomingVars.COLOR_BORDER;
    public static final CssVar COLOR_BORDER_EMPHASIS = HomingVars.COLOR_BORDER_EMPHASIS;

    public static final CssVar COLOR_ACCENT          = HomingVars.COLOR_ACCENT;
    public static final CssVar COLOR_ACCENT_EMPHASIS = HomingVars.COLOR_ACCENT_EMPHASIS;
    public static final CssVar COLOR_ACCENT_ON       = HomingVars.COLOR_ACCENT_ON;

    public static final CssVar SPACE_1 = HomingVars.SPACE_1;
    public static final CssVar SPACE_2 = HomingVars.SPACE_2;
    public static final CssVar SPACE_3 = HomingVars.SPACE_3;
    public static final CssVar SPACE_4 = HomingVars.SPACE_4;
    public static final CssVar SPACE_5 = HomingVars.SPACE_5;
    public static final CssVar SPACE_6 = HomingVars.SPACE_6;
    public static final CssVar SPACE_7 = HomingVars.SPACE_7;
    public static final CssVar SPACE_8 = HomingVars.SPACE_8;

    public static final CssVar RADIUS_SM = HomingVars.RADIUS_SM;
    public static final CssVar RADIUS_MD = HomingVars.RADIUS_MD;
    public static final CssVar RADIUS_LG = HomingVars.RADIUS_LG;

    public static final Set<CssVar> ALL = HomingVars.ALL;

    private StudioVars() {}
}
