package hue.captains.singapura.js.homing.design.color;

/**
 * A colour scheme (RFC 0067, appendix Semantic Colours and Flavours): the name of a set of colours that a style may
 * use and a palette must give - a marker, and nothing else: it knows no reader and no palette. A scheme lives in one jar
 * with its colours, which a tree reader of its own reads (SchemeTreeReader, named {@code <Name>Colours} beside
 * {@code <Name>Scheme}, found by the instance's finder): its layer, closed, over the layers of the schemes below it. Schemes are
 * classes, so each is on top of exactly one: a scheme from scratch implements ColorScheme alone and is a base - the
 * house's is {@code BaseScheme}, beside the house's colours; a scheme on top of another extends it, sees what it has,
 * and adds colours of its own - a scheme adding none means nothing. A style requires one scheme and a palette
 * implements one, and a style is worn only in a palette of exactly its scheme. A scheme is a type, never an instance:
 * every scheme is an abstract class. Schemes are added only when absolutely necessary; typically two layers - a
 * style's own over the base.
 */
public interface ColorScheme {
}
