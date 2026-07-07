package hue.captains.singapura.js.homing.color.semantic.structural;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;

/** A raised panel / card surface. */
public record SurfaceRaised() implements SemanticColor, Leaf<Structural>, WithDesc {
    public static final SurfaceRaised INSTANCE = new SurfaceRaised();
    @Override public String name() { return "surface-raised"; }
    @Override public Structural parent() { return Structural.INSTANCE; }
    @Override public Desc desc() { return Descs.of("A raised panel / card surface."); }
}
