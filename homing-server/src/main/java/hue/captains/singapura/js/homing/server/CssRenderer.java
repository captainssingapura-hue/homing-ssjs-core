package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.DerivedCss;

import java.util.Optional;

/**
 * The seam between serving and design. {@code /css-content} asks each
 * registered renderer, in order, for a derived sheet under a theme slug; the
 * first that answers is served. A provided group is never asked about: it is
 * its declared bodies, the same in every theme (RFC 0064, D25). The server
 * never learns what a design is — a renderer does, on the design side, and
 * hands back CSS.
 *
 * <p>Registered through {@link ThemeRegistry#renderers()}, because the
 * registry is already what carries a deployment's theme knowledge into the
 * server.</p>
 */
public interface CssRenderer {

    /** The sheet derived under this slug, if this renderer owns it. */
    Optional<String> render(DerivedCss<?> sheet, String themeSlug);

    /** Whether this renderer derives the sheet at all. */
    boolean owns(DerivedCss<?> sheet);
}
