package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.theme.color.GlobalColorPalette;
import hue.captains.singapura.js.homing.theme.color.HomingVars;
import hue.captains.singapura.js.homing.theme.type.GlobalTypePalette;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;

import java.util.Map;

/**
 * Editorial — the house design, the one that ships with
 * {@code homing-studio-base} and works out of the box: a serif for the
 * headings, a grotesque for the body, hairline rules, corners just off
 * square, a whisper of shadow. Print-derived and quiet, which is what lets
 * every other design be written over it — {@link EditorialDesign} is the
 * physique they all fall back to for whatever they have no word of their own
 * for. Worn in {@link SeedPalette#HARBOUR} — navy and gold on a cool white
 * page — by default.
 *
 * <p>RFC 0066 — an identity record and one nested {@link Palette}: the
 * theme's body for the global palette, light and dark, for the groups not
 * yet on design classes. The structure every theme once re-shipped lives in
 * {@code StudioStyles} as agnostic classes; this theme overrides none of
 * them.</p>
 */
public record HomingEditorial() implements Design {

    public static final DesignId ID = new DesignId("editorial");
    public static final HomingEditorial INSTANCE = new HomingEditorial();

    /** The design as a function: the house physique off the colour plane, the house seeds on it; none for what neither has. */
    @Override public Impl impl(DesignClass<?> pair) {
        return pair.onColourPlane() ? SeedPalette.HARBOUR.impl(pair) : EditorialDesign.WORDS.get(pair);
    }

    /** Worn in Harbour by default — the first seed palette, in its own name. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.HARBOUR; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Editorial"; }
    @Override public String group() { return "Neutral"; }
    @Override public String inspiration() { return "The house design — serif headings, hairline rules, a whisper of shadow; ships with studio-base."; }

    // -------------------------------------------------------------------
    // Palette — the theme's body for GlobalColorPalette (RFC 0066). Served as the prior of every page.
    // Single semantic layer (--color-*, --space-*, --radius-*) — each role
    // gets a concrete value directly, with no intermediate primitive layer.
    // -------------------------------------------------------------------

    public record Palette() implements GlobalColorPalette.Provision<HomingEditorial> {
        public static final Palette INSTANCE = new Palette();
        @Override public HomingEditorial theme() { return HomingEditorial.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }
            @Override public Map<CssVar, String> darkValues() { return DARK; }

            /** The re-binding under prefers-color-scheme: dark — the colour roles only;
             *  the scales are the same job in both modes. */
            private static final Map<CssVar, String> DARK = Map.ofEntries(
                    Map.entry(HomingVars.COLOR_SURFACE,               "#0F1320"),
                    Map.entry(HomingVars.COLOR_SURFACE_RAISED,        "#1A1F36"),
                    Map.entry(HomingVars.COLOR_SURFACE_RECESSED,      "#232943"),
                    Map.entry(HomingVars.COLOR_SURFACE_INVERTED,      "#111936"),   // kept dark — header bg
                    Map.entry(HomingVars.COLOR_TEXT_PRIMARY,          "#E2E8F0"),
                    Map.entry(HomingVars.COLOR_TEXT_MUTED,            "#94A3B8"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,      "#E2E8F0"),
                    Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#B8C9F2"),
                    Map.entry(HomingVars.COLOR_TEXT_TITLE,            "#8FA3D8"),
                    Map.entry(HomingVars.COLOR_TEXT_LINK,             "#8FA3D8"),   // lifted navy
                    Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,       "#E0A833"),   // lifted amber-dk
                    Map.entry(HomingVars.COLOR_BORDER,                "#2D3454"),
                    Map.entry(HomingVars.COLOR_BORDER_EMPHASIS,       "#F4B942"),   // kept gold
                    Map.entry(HomingVars.COLOR_ACCENT,                "#F4B942"),
                    Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS,       "#E0A833"),   // lifted amber-dk
                    Map.entry(HomingVars.COLOR_ACCENT_ON,             "#111936")   // dark text on gold
            );

        // Semantic-only — no primitive layer. Each role has an independent
        // value per theme, eliminating the "primitive doing double duty"
        // class of bug. Light values here; the dark re-binding in DARK below.
        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces
                Map.entry(HomingVars.COLOR_SURFACE,          "#FAFBFD"),
                Map.entry(HomingVars.COLOR_SURFACE_RAISED,   "#FFFFFF"),
                Map.entry(HomingVars.COLOR_SURFACE_RECESSED, "#F1F4F9"),
                Map.entry(HomingVars.COLOR_SURFACE_INVERTED, "#111936"),

                // Text
                Map.entry(HomingVars.COLOR_TEXT_PRIMARY,           "#3B4A6B"),
                Map.entry(HomingVars.COLOR_TEXT_MUTED,             "#64748B"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED,       "#FFFFFF"),
                Map.entry(HomingVars.COLOR_TEXT_ON_INVERTED_MUTED, "#CADCFC"),
                Map.entry(HomingVars.COLOR_TEXT_TITLE,              "#1E2761"),   // title := link, unchanged
                Map.entry(HomingVars.COLOR_TEXT_LINK,              "#1E2761"),
                Map.entry(HomingVars.COLOR_TEXT_LINK_HOVER,        "#C8921E"),

                // Borders
                Map.entry(HomingVars.COLOR_BORDER,          "#E2E8F0"),
                Map.entry(HomingVars.COLOR_BORDER_EMPHASIS, "#F4B942"),

                // Accent
                Map.entry(HomingVars.COLOR_ACCENT,          "#F4B942"),
                Map.entry(HomingVars.COLOR_ACCENT_EMPHASIS, "#C8921E"),
                Map.entry(HomingVars.COLOR_ACCENT_ON,       "#111936"),

                // Spacing scale
                Map.entry(HomingVars.SPACE_1, "4px"),
                Map.entry(HomingVars.SPACE_2, "8px"),
                Map.entry(HomingVars.SPACE_3, "12px"),
                Map.entry(HomingVars.SPACE_4, "16px"),
                Map.entry(HomingVars.SPACE_5, "20px"),
                Map.entry(HomingVars.SPACE_6, "24px"),
                Map.entry(HomingVars.SPACE_7, "32px"),
                Map.entry(HomingVars.SPACE_8, "40px"),

                // Radius scale
                Map.entry(HomingVars.RADIUS_SM, "4px"),
                Map.entry(HomingVars.RADIUS_MD, "8px"),
                Map.entry(HomingVars.RADIUS_LG, "12px")
        );
    }
    /** The house faces — this theme has no typographic identity of its own. */
    public record Fonts() implements GlobalTypePalette.Provision<HomingEditorial> {
        public static final Fonts INSTANCE = new Fonts();
        @Override public HomingEditorial theme() { return HomingEditorial.INSTANCE; }
        @Override public Map<CssVar, String> values() { return StudioFonts.HOUSE; }
    }
}
