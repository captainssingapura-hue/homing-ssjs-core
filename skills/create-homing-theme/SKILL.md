---
name: create-homing-theme
description: Use this skill when the user wants to add a new theme (palette + optional textures/typography) to a homing-studio-base studio. Triggers — "add theme", "create homing theme", "new color scheme", "design a theme for X". Skip if the user wants to redesign a SINGLE component (use direct CSS edit or component override) — themes are about cohesive whole-system palette swaps.
---

# Create a Homing Theme

A Homing theme is **two static records in one Java file**: `Palette` (the theme's body for the global palette — its per-token values, RFC 0066) and `Globals` (the dark-mode override + structural CSS reuse + optional theme-specific CSS layer for textures, fonts, etc.). Plus three lines added to a registry. Total ~120 LoC for a basic theme; ~180 LoC for an elaborate one with texture / serif body / custom dividers.

## Mental model

The framework's CSS is **structurally identical across themes**. Every page renders through the same builders (`Card`, `ListItem`, `Header`, etc.) using the same CSS classes (`st-card`, `st-list-item`, …). Each class body references **semantic tokens** — `var(--color-surface)`, `var(--color-text-link)`, `var(--space-4)`, `var(--radius-md)`. A theme's only job is to bind those tokens to concrete values.

Three tiers of theme complexity:

| Tier | Adds | Examples |
|---|---|---|
| **Basic** | Just the `Palette` map (every token `GlobalColorPalette` declares) + dark-mode `Globals` | Default, Forest, Sunset, Forbidden City |
| **Identity-charged** | Override `RADIUS_*` to `0px` for geometric look, custom accent emphasis colour for impact | Bauhaus |
| **Layered** | Texture (SVG noise data: URI), body-font override, custom rules — appended *after* the shared structural CSS | Letterpress |

Pick the tier first, then build only what you need.

## The 14 tokens you must populate

Found in `StudioVars` (`homing-studio-base/.../theme/StudioVars.java`). **Every theme must bind every token** or rendering breaks for the missing one.

| Token | Role | Light example | Dark example |
|---|---|---|---|
| `COLOR_SURFACE` | page background | `#FAFBFD` | `#0F1320` |
| `COLOR_SURFACE_RAISED` | cards / panels | `#FFFFFF` | `#1A1F36` |
| `COLOR_SURFACE_RECESSED` | inset panels / code | `#F1F4F9` | `#232943` |
| `COLOR_SURFACE_INVERTED` | header band | `#111936` | `#111936` (kept) |
| `COLOR_TEXT_PRIMARY` | body text | `#3B4A6B` | `#E2E8F0` |
| `COLOR_TEXT_MUTED` | secondary text | `#64748B` | `#94A3B8` |
| `COLOR_TEXT_ON_INVERTED` | text on dark band | `#FFFFFF` | `#E2E8F0` |
| `COLOR_TEXT_ON_INVERTED_MUTED` | secondary text on dark | `#CADCFC` | `#B8C9F2` |
| `COLOR_TEXT_LINK` | link colour | `#1E2761` | `#8FA3D8` |
| `COLOR_TEXT_LINK_HOVER` | link hover | `#C8921E` | `#E0A833` |
| `COLOR_BORDER` | hairline borders | `#E2E8F0` | `#2D3454` |
| `COLOR_BORDER_EMPHASIS` | card left-edge / section underline | `#F4B942` | `#F4B942` (often kept) |
| `COLOR_ACCENT` | accent fills (brand dot, bullets, badges) | `#F4B942` | `#F4B942` |
| `COLOR_ACCENT_EMPHASIS` | accent hover / pressed | `#C8921E` | `#E0A833` |
| `COLOR_ACCENT_ON` | text on accent fill | `#111936` | `#111936` |

Plus the spacing scale (`SPACE_1..8`) and radius scale (`RADIUS_SM/MD/LG`) — typically copied verbatim from `HomingDefault`. Override `RADIUS_*` to `0px` for a geometric / Bauhaus look.

## Step-by-step

### 1. Decide the palette

Lock these decisions before touching code:

- **Mood / inspiration** — name and a one-sentence description. Helps justify colour choices.
- **Page surface** — light or dark? Warm or cool?
- **Inverted band** (header) — typically a deep version of the brand accent or a neutral dark.
- **Accent + emphasis** — the two-step accent for static + interactive states.
- **Link colour** — must be readable on `--color-surface` AND distinct from `--color-text-primary`.
- **Whether dark-mode is a tonal flip or a different identity** — flip is easier (most themes); different identity is more work but possible.

Sanity-check by **writing the palette out as a contrast table**. The two combinations that bite:
- `text-primary` on `surface` — must hit at least 4.5:1 for body text.
- `text-on-inverted` on `surface-inverted` — same bar.

### 2. Create the Java file

Mirror an existing theme. **Pattern to copy** depends on the tier:

- **Basic tier**: copy `HomingForest.java` — palette only.
- **Identity-charged tier**: copy `HomingBauhaus.java` — palette + radius overrides.
- **Layered tier**: copy `HomingLetterpress.java` — palette + texture + font override + custom CSS rules.

File location: `homing-studio-base/src/main/java/hue/captains/singapura/js/homing/studio/base/theme/Homing<Name>.java` (or your downstream's equivalent path if you ship themes from a downstream module).

Skeleton:

```java
package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.PaletteProvision;

import java.util.Map;

public record Homing<Name>() implements Theme {

    public static final Homing<Name> INSTANCE = new Homing<Name>();

    @Override public String slug()  { return "<slug>"; }   // URL: ?theme=<slug>
    @Override public String label() { return "<Label>"; }  // shown in picker

    public record Palette() implements GlobalColorPalette.Provision<Homing<Name>> {   // RFC 0066
        public static final Palette INSTANCE = new Palette();
        @Override public Homing<Name> theme() { return Homing<Name>.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                Map.entry(StudioVars.COLOR_SURFACE,                "#..."),
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,         "#..."),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED,       "#..."),
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED,       "#..."),
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#..."),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#..."),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#..."),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#..."),
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#..."),
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#..."),
                Map.entry(StudioVars.COLOR_BORDER,                 "#..."),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS,        "#..."),
                Map.entry(StudioVars.COLOR_ACCENT,                 "#..."),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS,        "#..."),
                Map.entry(StudioVars.COLOR_ACCENT_ON,              "#..."),

                Map.entry(StudioVars.SPACE_1, "4px"),  Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"), Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"), Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"), Map.entry(StudioVars.SPACE_8, "40px"),
                Map.entry(StudioVars.RADIUS_SM, "4px"),
                Map.entry(StudioVars.RADIUS_MD, "8px"),
                Map.entry(StudioVars.RADIUS_LG, "12px")
        );
    }

    public record Globals() implements ThemeGlobals<Homing<Name>> {
        public static final Globals INSTANCE = new Globals();
        @Override public Homing<Name> theme() { return Homing<Name>.INSTANCE; }
        @Override public String css() { return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS; }

        /** Re-bind every token for prefers-color-scheme: dark. */
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: light dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #...;
                        --color-surface-raised:    #...;
                        --color-surface-recessed:  #...;
                        --color-surface-inverted:  #...;

                        --color-text-primary:            #...;
                        --color-text-muted:              #...;
                        --color-text-on-inverted:        #...;
                        --color-text-on-inverted-muted:  #...;
                        --color-text-link:               #...;
                        --color-text-link-hover:         #...;

                        --color-border:           #...;
                        --color-border-emphasis:  #...;

                        --color-accent:           #...;
                        --color-accent-emphasis:  #...;
                        --color-accent-on:        #...;
                    }
                }
                """;
    }
}
```

Replace `Homing<Name>` with the actual class name (no angle brackets).

### 3. Register in the theme registry

Add three lines to `StudioThemeRegistry.java` (or your downstream's equivalent):

```java
@Override public List<Theme> themes() {
    return List.of(
            HomingDefault.INSTANCE,
            // … existing themes
            Homing<Name>.INSTANCE          // ← new
    );
}

@Override public List<PaletteProvision<?, ?>> palettes() {
    return List.of(
            HomingDefault.Palette.INSTANCE,
            // … existing
            Homing<Name>.Palette.INSTANCE     // ← new
    );
}

@Override public List<ThemeGlobals<?>> globals() {
    return List.of(
            HomingDefault.Globals.INSTANCE,
            // … existing
            Homing<Name>.Globals.INSTANCE  // ← new
    );
}
```

That's it. Theme is now reachable.

### 4. Verify

- `mvn install` should pass.
- Restart the studio.
- Visit `/app?app=themes` — your theme appears as a swatch row.
- Click the activator or use `?theme=<slug>` — the chrome retints.
- The header band uses `--color-surface-inverted`.
- The picker (top-right of header) lists your theme.

## Tier-3 elaboration (textures, fonts, custom rules)

If your theme needs more than palette swap — e.g. a paper texture, serif body, custom dividers — append a third CSS string after the shared structural cascade:

```java
@Override public String css() {
    // Order: dark-overrides, then shared structural CSS, then YOUR overlay.
    // Putting your overlay last lets you re-install background-image after the
    // shared `background: var(--color-surface)` shorthand cleared it.
    return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS + EXTRA_OVERRIDE;
}

private static final String EXTRA_OVERRIDE = """
        /* Body font override — falls back gracefully across OSes. */
        html, body {
            font-family: "Iowan Old Style", "Charter", "Georgia", "Cambria", "Times New Roman", serif;
        }
        /* Inline SVG-noise paper grain — no extra HTTP, no extra asset. */
        html, body {
            background-image:
                url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><filter id='n'><feTurbulence type='fractalNoise' baseFrequency='0.92' numOctaves='2' stitchTiles='stitch'/><feColorMatrix values='0 0 0 0 0.18  0 0 0 0 0.15  0 0 0 0 0.12  0 0 0 0.10 0'/></filter><rect width='100%' height='100%' filter='url(%23n)'/></svg>");
            background-repeat: repeat;
        }
        @media (prefers-color-scheme: dark) {
            html, body {
                background-image:
                    url("data:image/svg+xml;utf8,<svg ...feColorMatrix flipped for dark...>");
            }
        }
        /* Editorial-style double-rule divider beneath section titles. */
        .st-section-title {
            border-bottom-width: 3px;
            border-bottom-style: double;
        }
        """;
```

**Texture cookbook** — `feColorMatrix values` is the per-channel tint of the noise. The 5×5 matrix maps `(R, G, B, A, 1) → output`. For grain, set the `R/G/B` rows to constants (the tint) and the `A` row's last column to opacity:

```
0 0 0 0 R       <- output red = R
0 0 0 0 G       <- output green = G
0 0 0 0 B       <- output blue = B
0 0 0 ALPHA 0   <- output alpha = source-alpha * ALPHA
```

For a parchment grain (light mode): `R=0.18 G=0.15 B=0.12 ALPHA=0.10` (dark warm specks at low opacity).
For an inked-paper grain (dark mode): `R=0.94 G=0.91 B=0.84 ALPHA=0.06` (cream specks at lower opacity).

`baseFrequency` controls grain size — higher = finer, lower = coarser. `0.85–0.95` is paper-grain territory.

**Why the overlay must come after structural** — the structural rule has `background: var(--color-surface)` which is a shorthand. Shorthands reset all longhands (including `background-image`) to initial. Setting `background-image` longhand later re-installs the texture without losing the colour.

## What to never do

- **Skip a token.** Missing one breaks the cascade for that role; the css falls back to `unset` which is empty. Bind every one of the 15 colour tokens.
- **Use `var(--color-...)` inside SVG `fill=` attributes.** CSS vars don't resolve in XML attributes. If you want theme-aware SVG colours, use `style="fill: var(--color-...)"` (style attr is a CSS context).
- **Hardcode `rgba()` values in your theme's globals.** Use `color-mix(in srgb, var(--color-text-link) 12%, transparent)` instead — keeps the theme cohesive.
- **Author CSS in the studio downstream.** Themes belong in `homing-studio-base` (or a downstream theme module) — they need to live alongside the structural cascade they're customising.
- **Re-use another theme's slug.** Slug is the URL contract; each theme owns one slug forever.

## Reference themes to copy from

| Theme | File | When to copy |
|---|---|---|
| `HomingDefault`     | the canonical "warm gold + navy" baseline | for any modern corporate-clean look |
| `HomingForest`      | greens + honey | for organic / earth-toned themes |
| `HomingSunset`      | corals + ambers | for warm dusk themes |
| `HomingBauhaus`     | black + yellow + Itten blue, no rounded corners | for geometric / modernist themes |
| `HomingForbiddenCity` | imperial vermilion + gold + parchment | for historical / saturated warm themes |
| `HomingLetterpress` | parchment + brick red + serif body + paper grain | for editorial / textured themes — copy this for the layered tier |
