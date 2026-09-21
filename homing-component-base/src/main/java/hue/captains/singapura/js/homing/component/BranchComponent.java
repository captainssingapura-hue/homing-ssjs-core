package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.DomModule;

/**
 * A branch component: it takes the sub-branch its caller made for it and
 * mints its own tree on it — a card, a pane, a menu, a desk; {@code root}
 * is what the caller appends and {@code dispose()} dissolves the branch.
 * A {@link Widget} is this shape with typed params.
 *
 * @param <M> the module that exports the class
 */
public non-sealed interface BranchComponent<M extends DomModule<M>> extends UiComponent<M> {
    @Override default Shape shape() { return Shape.BRANCH; }
}
