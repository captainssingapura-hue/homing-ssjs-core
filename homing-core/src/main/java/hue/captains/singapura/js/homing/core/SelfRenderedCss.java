package hue.captains.singapura.js.homing.core;

/**
 * Marker for a {@link DerivedCss} that renders its own sheet, per theme: a sheet generated from
 * what a theme answers - a flattened sheet of semantic classes, say. The server asks it before any
 * other renderer; its sheet varies with the theme, so the theme's owner fetches it again on a
 * switch. A sheet is whatever it says it is for the slug - an empty one for a theme it says nothing
 * of.
 */
public interface SelfRenderedCss {
    /** The whole sheet under a theme. */
    String css(String themeSlug);
}
