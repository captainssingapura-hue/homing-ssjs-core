package hue.captains.singapura.js.homing.design;

/**
 * Text — the kinds of writing on a page. Each is set in a face, a weight, a
 * scale and a treatment, and inked; {@code Link} is also decorated and
 * eases; {@code Code} also sits on a surface with a corner and an inset.
 */
public interface Text extends Semantic {

    record Body() implements Text {}

    record Heading() implements Text {}

    record Display() implements Text {}

    record Caption() implements Text {}

    record Label() implements Text {}

    record Code() implements Text {}

    /** The eyebrow: a short tracked label above or beside a title, set small and loud. */
    record Kicker() implements Text {}

    /**
     * Rendered prose — markdown, with no class on any element inside it. The one
     * place a design writes a body with element selectors nested: the headings,
     * links, code and tables of a document are the design's to set, per element,
     * inside the one class the document wears.
     */
    record Prose() implements Text {}

    /** The lede: the paragraph under a title, set a little larger and a little quieter. */
    record Lede() implements Text {}

    /** A figure that is read at a glance: a percentage, a count, a glyph — set large in the display face. */
    record Numeral() implements Text {}

    record Link() implements Text {}
}
