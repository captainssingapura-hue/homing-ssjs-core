package hue.captains.singapura.js.homing.studio.base;

/**
 * Cross-reference from one {@link Doc} to another. Renders in the References section as a
 * card with the target's title (linked to the doc-reader URL via UUID) and summary.
 *
 * @param name   short anchor key cited in markdown as {@code #ref:<name>}
 * @param target the typed Doc this reference points at
 * @since RFC 0004-ext1
 */
public record DocReference(String name, Doc target) implements Reference {}
