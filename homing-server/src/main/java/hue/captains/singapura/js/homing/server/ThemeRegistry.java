package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Theme;

import java.util.List;

/**
 * What a deployment says about its themes: the {@link Theme} identities a page
 * may wear, how a picker composes them, and the renderers that serve the sheets
 * that vary with them. A theme is a design (RFC 0066 Episode 3); there is no
 * global palette, no provision and no per-class override any more (RFC 0067 retired
 * them with the global palette).
 *
 * <p>Each deployment provides its own implementation. The first theme listed
 * is the default a page is served under.</p>
 */
public interface ThemeRegistry {

    /** All themes registered for this deployment; the first is the default. Every slug a page may wear, crosses included. */
    List<Theme> themes();

    /**
     * The themes a picker lists — each a base with its own colours. A theme's
     * crosses with other colours are reached through {@link #dressed}, not
     * listed. Default: every theme.
     */
    default List<Theme> bases() { return themes(); }

    /**
     * The colours a base may be worn in, beside its own: a registry whose
     * themes are designs on two orthogonal planes offers every design's own
     * colours and any palette written as colours alone. Default: none — a
     * theme is worn in its own colours only.
     */
    default List<Theme> colours() { return List.of(); }

    /**
     * A base worn in some colours: the base itself when the colours are its
     * own, else the composed theme — the one {@link #themes()} lists under the
     * composed slug. Default: the base.
     */
    default Theme dressed(Theme base, Theme colours) { return base; }

    /**
     * Whether a picker offers a base in some colours. Its own colours, always;
     * another palette when it says it suits the base — crafted for it, or
     * named compatible by its author. Advisory: every pair {@link #dressed}
     * answers stays wearable by slug, this only decides what is put in front
     * of a user unasked. Default: everything offered.
     */
    default boolean fits(Theme base, Theme colours) { return true; }

    /**
     * The design side's renderers — asked first by {@code /css-content} for any group,
     * before the declared-body rendering — given what the deployment serves, since
     * what a sheet must carry is decided by what the served components wear.
     * Empty for a registry with no design side.
     */
    default List<CssRenderer> renderers(ServedModules served) { return List.of(); }

    /** Empty registry — no themes registered. */
    ThemeRegistry EMPTY = new ThemeRegistry() {
        @Override public List<Theme> themes() { return List.of(); }
    };

}
