package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.tree.NodeName;

/**
 * A component, declared — not merely exported. The module system manages
 * JS code physically: a module, its imports, its exports, the crate that
 * ships it. A component is a logical thing the physical side only names:
 * a class a {@link DomModule} exports, of one of two shapes, with a place
 * in its vehicle's {@link ComponentCatalogue}. The export record of a
 * component implements this instead of the bare {@code _Constant} marker,
 * so the declaration sits where the export always sat, nested in its
 * module and named as the JS class:
 *
 * <pre>{@code
 * public record MultiTabPane() implements BranchComponent<MultiTabPaneModule> {
 *     @Override public String summary() { return "Tabs in a strip over one panel each; press selects, drag reorders."; }
 * }
 * }</pre>
 *
 * <p>The bound {@code M extends DomModule} is the earlier finding made a
 * type: only a DOM module exports a component. Builders, statics and data
 * stay {@code _Constant}. The physical home is read off the nesting —
 * {@link #module()} is the enclosing module's {@code INSTANCE} — so the
 * catalogue can hold every declaration to a served module that exports
 * it, and every served module's component to a catalogue.</p>
 *
 * <p>Named {@code UiComponent} because {@code Component} is the default
 * CSS cascade layer's name in the core, left as it is.</p>
 *
 * @param <M> the module that exports the class
 */
public sealed interface UiComponent<M extends DomModule<M>> extends Exportable._Constant<M> permits ElementComponent, BranchComponent {

    /** The shape: how a caller makes one. */
    enum Shape {
        /** Told which element to mint ({@code static TAG}), takes that element. */
        ELEMENT("element"),
        /** Takes the sub-branch its caller made for it and mints its own tree on it. */
        BRANCH("branch"),
        /** The branch shape with typed params: a host's widget. */
        WIDGET("widget");
        private final String tag;
        Shape(String tag) { this.tag = tag; }
        public String tag() { return tag; }
    }

    Shape shape();

    /** The class's name, which is the JS class's. */
    default String label() { return getClass().getSimpleName(); }

    /** One line on what it is, for a catalogue. */
    default String summary() { return ""; }

    /** The segment under its catalogue: the class name in kebab. */
    default NodeName segment() { return NodeName.ofType(getClass(), ""); }

    /**
     * The module that exports this class: the enclosing module record's
     * {@code INSTANCE}, since a declaration is nested in its module; a
     * declaration that is not overrides this.
     */
    @SuppressWarnings("unchecked")
    default M module() {
        Class<?> enclosing = getClass().getEnclosingClass();
        if (enclosing == null)
            throw new IllegalStateException(getClass().getName() + ": a component declaration is nested in its module, or overrides module()");
        try {
            return (M) enclosing.getField("INSTANCE").get(null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException(enclosing.getName() + ": no module INSTANCE to read for " + getClass().getSimpleName() + "; override module()", e);
        }
    }
}
