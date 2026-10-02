package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.DomModule;

/**
 * An element component: it tells its caller which element to mint —
 * {@code static TAG} in JS, {@link #tag()} here, the same word — and takes
 * that element in its constructor: a button, a chip, an icon's span.
 *
 * @param <M> the module that exports the class
 */
public non-sealed interface ElementComponent<M extends DomModule<M>> extends UiComponent<M> {

    /** The tag the caller mints: {@code "button"}, {@code "span"} … */
    String tag();

    @Override default Shape shape() { return Shape.ELEMENT; }
}
