package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.Theme;

/**
 * A design: a function from a design class to its fulfilment. Nothing else.
 * Asked for {@code (Danger, Color.Surface)} it answers a flat {@link Impl} —
 * bindings into the target's template, a whole body, or silence — or
 * {@code null} when it has no word for the pair. How it arrives at the answer
 * is its own: a map, a rule over the semantic's branch, a delegation to
 * another pair or to a base design it holds. The deployment sees only the
 * answer, and the sheet is that answer's rendering, so whatever a design
 * delegated to is flattened by the time anything else looks.
 *
 * <p>A design never names a component and never learns one exists; its
 * domain is design classes, and every design class is a semantic on a
 * target. It is a {@link Theme} — the framework's identity of "what a page
 * wears" — so a registry lists designs where it listed themes, the page's
 * {@code ?theme=} names one, and a picker shows its label.</p>
 */
public interface Design extends Theme {

    /** The fulfilment of a design class under this design; {@code null} when it has no word for it. */
    Impl impl(DesignClass<?> pair);
}
