package hue.captains.singapura.js.homing.design.color;

import hue.captains.singapura.tao.ontology.Stateless;

import java.util.Optional;

/**
 * A colour scheme (RFC 0067, appendix Semantic Colours and Flavours, D49): the name of a set of colours that a style
 * may use and a palette must give - a stateless object, one of each, its {@code INSTANCE}, and nothing more: it knows
 * no palette, and no declaration of its colours. It says the scheme it is on top of ({@link #below()}), as its class
 * extends that scheme's class: a scheme from scratch implements ColorScheme alone and is a base - the house's is
 * {@code BaseScheme}, beside the house's colours; a scheme on top of another extends it, sees what it has, and adds
 * colours of its own - a scheme adding none means nothing. Its colours are declared, leaf by leaf, in its own jar
 * (SchemeDeclaration), and an instance registers the declarations explicitly - nothing is read off a type at run time.
 * A style requires one scheme and a palette implements one, each saying which, and a style is worn only in a palette of
 * exactly its scheme. Schemes are added only when absolutely necessary; typically two layers - a style's own over the
 * base.
 */
public interface ColorScheme extends Stateless {

    /** The scheme it is on top of, whose colours it sees: none for a scheme from scratch - a base. */
    default Optional<ColorScheme> below() { return Optional.empty(); }

    /** Its name, as messages say it: {@code BrutalismScheme}. */
    default String name() { return getClass().getSimpleName(); }
}
