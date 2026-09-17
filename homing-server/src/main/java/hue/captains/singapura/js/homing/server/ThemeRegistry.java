package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0066 — what a deployment says about its themes: the {@link Theme}
 * identities, each one's {@link PaletteProvision}s of the global palettes
 * (colour, type, and whatever vocabularies follow), and the
 * {@link CssGroupImpl}s carrying their per-class overrides. That is the whole
 * of a theme; there is no globals sheet any more (RFC 0066 retired it:
 * structure is agnostic classes, the dark binding is on the provision, the
 * overlay is overrides).
 *
 * <p>Each deployment provides its own implementation. The first theme listed
 * is the default a page is served under. {@link #priors()} — derived — are the
 * palette groups the module action writes into every served subgraph.</p>
 */
public interface ThemeRegistry {

    /** All themes registered for this deployment; the first is the default. */
    List<Theme> themes();

    /** Every theme's provisions of the global palettes — one per theme per palette group. */
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

    /**
     * Look up a theme's provision of one palette group by slug — the colour
     * palette for the picker's swatches, say. Returns {@code null} if the theme
     * has none for that group.
     */
    default PaletteProvision<?, ?> paletteForSlug(String slug, CssGroup<?> palette) {
        if (slug == null || palette == null) return null;
        for (var v : palettes()) {
            if (slug.equals(v.theme().slug()) && v.group().getClass() == palette.getClass()) return v;
        }
        return null;
    }

    /**
     * The palette groups the provisions fill — colour, type, and whatever
     * vocabularies follow — each a PRIOR every group on every page leans on
     * without declaring it. Derived from the provisions, first-mention order,
     * one entry per group; the server writes them all into every served
     * subgraph. Empty when the deployment registers none.
     */
    default List<CssGroup<?>> priors() {
        var out = new ArrayList<CssGroup<?>>();
        for (var p : palettes()) {
            boolean seen = false;
            for (var have : out) if (have.getClass() == p.group().getClass()) { seen = true; break; }
            if (!seen) out.add(p.group());
        }
        return List.copyOf(out);
    }

    /** Provisions and overrides together — what the CSS action resolves impls from. */
    default List<CssGroupImpl<?, ?>> impls() {
        var all = new ArrayList<CssGroupImpl<?, ?>>(palettes());
        all.addAll(overrides());
        return List.copyOf(all);
    }
}
