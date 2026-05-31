package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.tree.DimensionValue;

/**
 * Studio-supplied {@link DimensionValue} for the substrate's {@code Category}
 * key — wraps a catalogue badge or a doc's {@code category()} string at the
 * adapter boundary. The substrate owns the <i>key</i> (Category); studio-base
 * owns this <i>value</i> shape. No raw strings escape: the adapter wraps the
 * native String here, once, at the seam.
 *
 * @since homing-tree-views v1 — studio adapter boundary
 */
public record CategoryValue(String text) implements DimensionValue {
    public CategoryValue {
        if (text == null) throw new IllegalArgumentException("text");
    }
    @Override public String tag() { return "category"; }
    @Override public String displayText() { return text; }
}
