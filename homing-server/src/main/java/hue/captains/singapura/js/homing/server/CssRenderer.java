package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.Optional;

/**
 * The seam between serving and design. {@code /css-content} asks each
 * registered renderer, in order, for a group's sheet under a theme slug; the
 * first that answers is served, and a group no renderer claims is rendered
 * from its declared bodies as before. The server never learns what a design
 * is — a renderer does, on the design side, and hands back CSS.
 *
 * <p>Registered through {@link ThemeRegistry#renderers()}, because the
 * registry is already what carries a deployment's theme knowledge into the
 * server.</p>
 */
public interface CssRenderer {

    /** The sheet for this group under this slug, if this renderer owns the group. */
    Optional<String> render(CssGroup<?> group, String themeSlug);

    /** Whether this renderer renders the group at all. */
    boolean owns(CssGroup<?> group);

    /** Whether the group's sheet changes with the theme — a group that does not is loaded once, without a theme, and never re-fetched on a switch. */
    default boolean varies(CssGroup<?> group) { return true; }
}
