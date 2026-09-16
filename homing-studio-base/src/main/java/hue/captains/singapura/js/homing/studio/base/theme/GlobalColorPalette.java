package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteClass;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.List;
import java.util.Set;

/**
 * RFC 0066 — the global palette as a node in the CSS dependency graph: one
 * group, one {@link PaletteClass}, and a {@link Provision} per theme.
 *
 * <p>Extracted from {@link StudioVars}, which stays as the typed names the
 * palette declares. What was three things — a list the studio kept, a map each
 * theme kept, and a sheet the server assembled from the map — is one class
 * that declares its tokens and eleven bodies that bind them.</p>
 *
 * <p><b>The prior.</b> 179 of the studio's 251 classes read a token from this
 * palette. Rather than 179 identical {@code dependsOn()} declarations, the
 * group is a {@link #prior() prior}: reachable from every class by definition,
 * loaded first, always. {@code dependsOn()} is kept for the edges that carry
 * information — a second palette that only some classes need, a class that
 * lays out inside another's.</p>
 *
 * <p><b>Served.</b> The client's first wave is still {@code /theme-vars}, and
 * that endpoint now renders this palette's provision for the requested theme;
 * the group is also served as itself at {@code /css-content?class=…&theme=…},
 * for when the client's plan names it directly. Episode 1 keeps the palette
 * PLAIN — the flat set, colours and scales together — so the organisation can
 * be finished first; Episode 2 re-declares it as a semantic tree.</p>
 */
public record GlobalColorPalette() implements CssGroup<GlobalColorPalette> {

    public static final GlobalColorPalette INSTANCE = new GlobalColorPalette();

    /** Everything leans on the palette; the palette leans on nothing. */
    @Override public boolean prior() { return true; }

    /** The one node: {@code :root}, provided per theme. */
    public record global_color_palette() implements PaletteClass<GlobalColorPalette> {
        @Override public Set<CssVar> declares() { return StudioVars.ALL; }
    }

    /**
     * A theme's body for the palette. One record per theme, registered in the
     * deployment's {@link hue.captains.singapura.js.homing.server.ThemeRegistry#palettes()};
     * {@link #values()} must bind every token {@link global_color_palette#declares()}
     * names and nothing outside it — {@code PaletteCompletenessTest} is the gate.
     */
    public interface Provision<TH extends Theme> extends PaletteProvision<GlobalColorPalette, TH> {
        @Override default GlobalColorPalette group() { return INSTANCE; }
    }

    @Override
    public List<CssClass<GlobalColorPalette>> cssClasses() {
        return List.of(new global_color_palette());
    }
}
