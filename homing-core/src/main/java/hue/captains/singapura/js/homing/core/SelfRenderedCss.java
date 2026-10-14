package hue.captains.singapura.js.homing.core;

/**
 * Marker for a {@link DerivedCss} that renders its own sheet, per slug of what it varies with
 * ({@link DerivedCss#variesWith()}): a sheet generated from what a theme answers - a flattened sheet
 * of semantic classes, say - or a style's sheet per style, a palette's per palette. The server asks it
 * before any other renderer; the theme's owner fetches it again when what it varies with changes. A
 * sheet is whatever it says it is for the slug - an empty one for a theme it says nothing of - and one
 * that refuses a slug it does not know throws, which the server answers as not found.
 */
public interface SelfRenderedCss {
    /** The whole sheet under a slug: a theme's, a style's or a palette's, as the sheet varies. */
    String css(String slug);
}
