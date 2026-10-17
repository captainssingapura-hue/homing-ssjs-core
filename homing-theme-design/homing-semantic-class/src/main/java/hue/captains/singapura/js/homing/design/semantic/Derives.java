package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;

/**
 * A style written over another: what it leaves unanswered all the way up the tree is its base's,
 * and {@link Answer.FromBase} takes the base's answer where it answers higher up itself. The base
 * requires the style's scheme or one below it, so every colour the base names is one the style's
 * palettes give; and the scheme is this style's own - a class cannot be a style of two schemes.
 *
 * @param <S> the scheme the style requires
 * @param <B> the base
 */
public interface Derives<S extends ColorScheme, B extends DesignStyle<? super S>> extends DesignStyle<S> {

    /** The style this one is written over. */
    B base();
}
