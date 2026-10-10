package hue.captains.singapura.js.homing.core;

/**
 * Something a {@link CssClass}'s element wears beside it: a class token whose
 * rule is in a {@link DerivedCss derived sheet}, and nothing more. Two things
 * are needed of it - the token to put on the element, and the derived sheet
 * that must load - and nothing else: no body, no dependencies, no states. What
 * the token means and who fulfils it is not the wearer's business.
 *
 * <p>A component class names what it wears in {@link CssClass#wears()}; the
 * client manager adds the tokens with the class and removes them with it - a
 * name is the same in every theme - and {@link CssImportsFor#derivedOf} hands
 * each worn sheet to the theme's owner, which loads it and swaps it on a
 * switch.</p>
 */
public interface Wearable {

    /** The class token on the element. */
    String cssName();

    /** The derived sheet that carries this token's rule. */
    DerivedCss<?> sheet();
}
