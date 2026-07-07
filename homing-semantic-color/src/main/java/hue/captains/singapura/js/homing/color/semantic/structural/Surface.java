package hue.captains.singapura.js.homing.color.semantic.structural;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;

/** The base page surface. */
public record Surface() implements SemanticColor, Leaf<Structural>, WithDesc {
    public static final Surface INSTANCE = new Surface();
    @Override public String name() { return "surface"; }
    @Override public Structural parent() { return Structural.INSTANCE; }
    @Override public Desc desc() { return Descs.of("The base page surface."); }
}
