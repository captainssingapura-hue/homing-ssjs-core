package hue.captains.singapura.js.homing.color.semantic.structural;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;

/**
 * An emphasized rule. Structurally a border, but in every shipped theme its
 * value is the brand hue — the one place Identity leaks into a Structural slot.
 */
public record BorderEmphasis() implements SemanticColor, Leaf<Structural>, WithDesc {
    public static final BorderEmphasis INSTANCE = new BorderEmphasis();
    @Override public String name() { return "border-emphasis"; }
    @Override public Structural parent() { return Structural.INSTANCE; }
    @Override public Desc desc() { return Descs.of("An emphasized rule — often carries the brand hue."); }
}
