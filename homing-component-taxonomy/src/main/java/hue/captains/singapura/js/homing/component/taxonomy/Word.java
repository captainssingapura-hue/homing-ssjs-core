package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A word of the lexicon, a leaf: what a mark means, never what it looks like - {@code Close} closes
 * a tab, a pane and a dialog alike; {@code Disclose} says there is more beneath. A component shows a
 * word, and the style worn writes it - a symbol of its own, in the ink the mark stands on - so the
 * same word is drawn once per style and looks the same wherever it is shown. A shared, stateless
 * singleton filed under a branch, the root's included. Which word a mark shows is the mark's value,
 * never its kind: an icon that shows another word is the same icon.
 *
 * <pre>{@code
 * public record Close() implements Word<Acting> {
 *     public static final Close INSTANCE = new Close();
 *     @Override public Acting parent() { return Acting.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface Word<P extends WordBranch> extends WordNode {
    /** The branch it is filed under. */
    P parent();

    /** Its token, the name a component shows it by: {@code close}. */
    default String token() { return name().value(); }
}
