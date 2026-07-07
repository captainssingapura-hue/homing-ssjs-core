package hue.captains.singapura.js.homing.color.semantic.structural;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;

/** A recessed / inset surface. */
public record SurfaceRecessed() implements SemanticColor, Leaf<Structural>, WithDesc {
    public static final SurfaceRecessed INSTANCE = new SurfaceRecessed();
    @Override public String name() { return "surface-recessed"; }
    @Override public Structural parent() { return Structural.INSTANCE; }
    @Override public Desc desc() { return Descs.of("A recessed / inset surface."); }
}
