package hue.captains.singapura.js.homing.design.semantic;

/**
 * A style written over another: what it leaves unanswered all the way up the tree is its base's,
 * and {@link Answer.FromBase} takes the base's answer where it answers higher up itself.
 *
 * @param <B> the base
 */
public interface Derives<B extends DesignStyle> {

    /** The style this one is written over. */
    B base();
}
