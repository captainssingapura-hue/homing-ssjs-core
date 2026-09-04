package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;

import java.util.Set;

/**
 * RFC 0002-ext1 — typed CSS variable vocabulary for the studio.
 *
 * <p>Semantic-only: every variable here is a role-named token that component
 * bodies reference via {@link CssVar#ref()}. Concrete values are provided
 * per-theme by each {@link hue.captains.singapura.js.homing.core.ThemeVariables}
 * implementation (e.g. {@link HomingDefault.Vars}, {@link HomingForest.Vars},
 * {@link HomingSunset.Vars}). The previous primitive layer ({@code --st-*}
 * brand colors) was retired — having two layers caused a "primitive doing
 * double duty" class of bug, where the same primitive served two semantic
 * roles whose dark-mode requirements diverged. Each role now owns its
 * value independently, per theme, per mode.</p>
 */
public final class StudioVars {

    // -------------------------------------------------------------------
    // Semantic vocabulary — the public contract component bodies reference.
    // -------------------------------------------------------------------

    // Surface roles
    public static final CssVar COLOR_SURFACE          = new CssVar("--color-surface");
    public static final CssVar COLOR_SURFACE_RAISED   = new CssVar("--color-surface-raised");
    public static final CssVar COLOR_SURFACE_RECESSED = new CssVar("--color-surface-recessed");
    public static final CssVar COLOR_SURFACE_INVERTED = new CssVar("--color-surface-inverted");

    // Text roles
    public static final CssVar COLOR_TEXT_PRIMARY            = new CssVar("--color-text-primary");
    public static final CssVar COLOR_TEXT_MUTED              = new CssVar("--color-text-muted");
    public static final CssVar COLOR_TEXT_ON_INVERTED        = new CssVar("--color-text-on-inverted");
    public static final CssVar COLOR_TEXT_ON_INVERTED_MUTED  = new CssVar("--color-text-on-inverted-muted");
    public static final CssVar COLOR_TEXT_LINK               = new CssVar("--color-text-link");
    public static final CssVar COLOR_TEXT_LINK_HOVER         = new CssVar("--color-text-link-hover");

    /** The page title. Separate from the link colour it used to borrow: a title
     *  is not a link, and the two only agreed by accident while every theme was
     *  light-primary. On a dark ground a link colour is lifted for legibility at
     *  13px, which at 44px is a shout. */
    public static final CssVar COLOR_TEXT_TITLE              = new CssVar("--color-text-title");

    // Border roles
    public static final CssVar COLOR_BORDER          = new CssVar("--color-border");
    public static final CssVar COLOR_BORDER_EMPHASIS = new CssVar("--color-border-emphasis");

    // Accent roles
    public static final CssVar COLOR_ACCENT          = new CssVar("--color-accent");
    public static final CssVar COLOR_ACCENT_EMPHASIS = new CssVar("--color-accent-emphasis");
    public static final CssVar COLOR_ACCENT_ON       = new CssVar("--color-accent-on");

    // Spacing scale
    public static final CssVar SPACE_1 = new CssVar("--space-1");
    public static final CssVar SPACE_2 = new CssVar("--space-2");
    public static final CssVar SPACE_3 = new CssVar("--space-3");
    public static final CssVar SPACE_4 = new CssVar("--space-4");
    public static final CssVar SPACE_5 = new CssVar("--space-5");
    public static final CssVar SPACE_6 = new CssVar("--space-6");
    public static final CssVar SPACE_7 = new CssVar("--space-7");
    public static final CssVar SPACE_8 = new CssVar("--space-8");

    // Radius scale
    public static final CssVar RADIUS_SM = new CssVar("--radius-sm");
    public static final CssVar RADIUS_MD = new CssVar("--radius-md");
    public static final CssVar RADIUS_LG = new CssVar("--radius-lg");

    /** Full vocabulary — useful for conformance tests and iteration. */
    public static final Set<CssVar> ALL = Set.of(
            COLOR_SURFACE, COLOR_SURFACE_RAISED, COLOR_SURFACE_RECESSED, COLOR_SURFACE_INVERTED,
            COLOR_TEXT_PRIMARY, COLOR_TEXT_MUTED, COLOR_TEXT_ON_INVERTED, COLOR_TEXT_ON_INVERTED_MUTED,
            COLOR_TEXT_LINK, COLOR_TEXT_LINK_HOVER,
            COLOR_TEXT_TITLE,
            COLOR_BORDER, COLOR_BORDER_EMPHASIS,
            COLOR_ACCENT, COLOR_ACCENT_EMPHASIS, COLOR_ACCENT_ON,
            SPACE_1, SPACE_2, SPACE_3, SPACE_4, SPACE_5, SPACE_6, SPACE_7, SPACE_8,
            RADIUS_SM, RADIUS_MD, RADIUS_LG
    );

    private StudioVars() {}
}
