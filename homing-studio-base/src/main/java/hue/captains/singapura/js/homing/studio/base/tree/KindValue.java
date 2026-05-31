package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.tree.DimensionValue;

/**
 * Studio-supplied {@link DimensionValue} for the substrate's {@code Kind}
 * key — carries a node's type discriminator. For a catalogue branch this is
 * {@code "catalogue"}; for a leaf doc it is the doc's {@code kind()}
 * ({@code "doc"}, {@code "composed"}, {@code "svg"}, …). The detail view's
 * widget registry dispatches on this value, so it doubles as the polymorphic
 * content selector.
 *
 * @since homing-tree-views v1 — studio adapter boundary
 */
public record KindValue(String text) implements DimensionValue {

    /** The Kind value for a catalogue (branch) node. */
    public static final KindValue CATALOGUE = new KindValue("catalogue");

    public KindValue {
        if (text == null) throw new IllegalArgumentException("text");
    }
    @Override public String tag() { return "kind"; }
    @Override public String displayText() { return text; }
}
