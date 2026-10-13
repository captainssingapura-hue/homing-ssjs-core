package hue.captains.singapura.js.homing.design.color;

/**
 * A colour scheme (RFC 0067, appendix Semantic Colours and Flavours): the name of a set of colours that a style may
 * use and a palette must give - a marker, and nothing else. Schemes are classes, so each is on top of exactly one:
 * {@link Base} is the base; a scheme on top of another extends it; a scheme from scratch implements ColorScheme alone
 * and is its own base, seeing nothing of the base. A style requires one scheme and a palette implements one, and a
 * style is worn only in a palette of exactly its scheme. A scheme is a type, never an instance: every scheme is an
 * abstract class. Schemes are added only when absolutely necessary.
 */
public interface ColorScheme {

    /** The base: the house's colours, and what every scheme on top of it sees. */
    abstract class Base implements ColorScheme {
        protected Base() {}
    }
}
