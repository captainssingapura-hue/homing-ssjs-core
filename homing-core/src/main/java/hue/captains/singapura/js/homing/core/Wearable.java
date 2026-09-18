package hue.captains.singapura.js.homing.core;

/**
 * Something a {@link CssClass}'s element wears beside it: a class token that
 * belongs to a group, and nothing more. The graph needs two things of it —
 * the token to put on the element, and the group whose sheet must load — and
 * asks for nothing else: no body, no dependencies, no states. What the token
 * means and who fulfils it is not the graph's business.
 *
 * <p>A component class names what it wears in {@link CssClass#wears()}; the
 * client manager adds the tokens with the class and removes them with it;
 * {@link CssImportsFor} counts each worn group as a dependency of the
 * wearer's.</p>
 */
public interface Wearable {

    /** The class token on the element. */
    String cssName();

    /** The group whose sheet carries this token's rule. */
    CssGroup<?> group();
}
