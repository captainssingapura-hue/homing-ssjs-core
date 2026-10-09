package hue.captains.singapura.js.homing.core;

/**
 * Marker for a {@link CssGroup} that renders its own sheet, per theme, instead of rendering its
 * classes' declared bodies: a sheet generated from what a theme answers - a flattened sheet of
 * semantic classes, say. The server asks it before any other renderer, and its sheet varies with
 * the theme, so a theme switch fetches it again.
 *
 * <p>The group still declares the classes a module imports it by; their bodies are not rendered.
 * A sheet is whatever the group says it is for the slug - an empty one for a theme it says nothing
 * of.</p>
 */
public interface SelfRenderedCss {
    /** The whole sheet under a theme. */
    String css(String themeSlug);
}
