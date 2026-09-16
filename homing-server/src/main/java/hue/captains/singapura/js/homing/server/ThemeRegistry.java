package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.PaletteProvision;

import java.util.List;

/**
 * RFC 0002-ext1 Phase 09 — registry of per-theme artifacts.
 *
 * <p>Holds three lists: {@link Theme} identities, {@link PaletteProvision} singletons
 * (one per theme — RFC 0066: the theme's body for the global palette), {@link ThemeGlobals}
 * singletons (one per theme; may be empty).
 * {@code ThemeGlobalsGetAction} consults it for {@code /theme-globals?theme=Y}; the
 * palette is served as a group by {@code CssContentGetAction}, and {@link #palette()}
 * names it as the prior the module action writes into every subgraph.
 *
 * <p>Each deployment provides its own {@code ThemeRegistry} implementation,
 * typically as a record holding its themes + palettes + globals. The default
 * empty registry is used by deployments that haven't migrated to the new
 * theme-bundle model — those still use the legacy {@code CssGroupImpl} path
 * via {@code CssContentGetAction}.</p>
 */
public interface ThemeRegistry {

    /** All themes registered for this deployment. */
    List<Theme> themes();

    /** RFC 0066 — every theme's provision of the global palette, one per theme. */
    List<PaletteProvision<?, ?>> palettes();

    /** All theme-globals singletons registered for this deployment. */
    List<ThemeGlobals<?>> globals();

    /** Empty registry — no themes registered. Used as the default until a
     *  deployment provides its own. */
    ThemeRegistry EMPTY = new ThemeRegistry() {
        @Override public List<Theme>              themes()    { return List.of(); }
        @Override public List<PaletteProvision<?, ?>> palettes() { return List.of(); }
        @Override public List<ThemeGlobals<?>>    globals()   { return List.of(); }
    };

    /** Look up the {@link PaletteProvision} for a theme by slug.
     *  Returns {@code null} if not registered. */
    default PaletteProvision<?, ?> paletteForSlug(String slug) {
        if (slug == null) return null;
        for (var v : palettes()) {
            if (slug.equals(v.theme().slug())) return v;
        }
        return null;
    }

    /**
     * RFC 0066 — the palette group the provisions fill: the PRIOR every group
     * on every page leans on without declaring it. The server writes it into
     * each served group's dependency subgraph, so the client loads it first by
     * the ordinary plan rather than by a special node. Derived from the
     * provisions (they all fill one group — the completeness gate checks it);
     * {@code null} when the deployment registers none.
     */
    default CssGroup<?> palette() {
        var all = palettes();
        return all.isEmpty() ? null : all.get(0).group();
    }

    /** Look up the {@link ThemeGlobals} singleton for a theme by slug.
     *  Returns {@code null} if not registered. */
    default ThemeGlobals<?> globalsForSlug(String slug) {
        if (slug == null) return null;
        for (var g : globals()) {
            if (slug.equals(g.theme().slug())) return g;
        }
        return null;
    }
}
