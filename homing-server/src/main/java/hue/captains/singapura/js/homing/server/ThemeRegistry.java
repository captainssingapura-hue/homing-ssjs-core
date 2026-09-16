package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0066 — what a deployment says about its themes: the {@link Theme}
 * identities, each one's {@link PaletteProvision} of the global palette, and
 * the {@link CssGroupImpl}s carrying their per-class overrides. That is the
 * whole of a theme; there is no globals sheet any more (RFC 0066 retired it:
 * structure is agnostic classes, the dark binding is on the provision, the
 * overlay is overrides).
 *
 * <p>Each deployment provides its own implementation. The first theme listed
 * is the default a page is served under. {@link #palette()} — derived — is the
 * prior the module action writes into every served subgraph.</p>
 */
public interface ThemeRegistry {

    /** All themes registered for this deployment; the first is the default. */
    List<Theme> themes();

    /** Every theme's provision of the global palette, one per theme. */
    List<PaletteProvision<?, ?>> palettes();

    /**
     * The per-class overrides: an impl per (group, theme) a theme has something
     * to say about. Optional — a palette-only theme contributes none. Provisions
     * are impls too, but are listed by {@link #palettes()}; {@link #impls()}
     * joins the two.
     */
    default List<CssGroupImpl<?, ?>> overrides() { return List.of(); }

    /** Empty registry — no themes registered. */
    ThemeRegistry EMPTY = new ThemeRegistry() {
        @Override public List<Theme>                 themes()   { return List.of(); }
        @Override public List<PaletteProvision<?, ?>> palettes() { return List.of(); }
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
     * The palette group the provisions fill: the PRIOR every group on every
     * page leans on without declaring it. Derived from the provisions (they all
     * fill one group — the completeness gate checks it); {@code null} when the
     * deployment registers none.
     */
    default CssGroup<?> palette() {
        var all = palettes();
        return all.isEmpty() ? null : all.get(0).group();
    }

    /** Provisions and overrides together — what the CSS action resolves impls from. */
    default List<CssGroupImpl<?, ?>> impls() {
        var all = new ArrayList<CssGroupImpl<?, ?>>(palettes());
        all.addAll(overrides());
        return List.copyOf(all);
    }
}
