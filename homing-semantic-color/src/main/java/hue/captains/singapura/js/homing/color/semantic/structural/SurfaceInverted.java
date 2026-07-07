package hue.captains.singapura.js.homing.color.semantic.structural;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;

/** An inverted (dark / header) surface. */
public record SurfaceInverted() implements SemanticColor, Leaf<Structural>, WithDesc {
    public static final SurfaceInverted INSTANCE = new SurfaceInverted();
    @Override public String name() { return "surface-inverted"; }
    @Override public Structural parent() { return Structural.INSTANCE; }
    @Override public Desc desc() { return Descs.of("An inverted (dark / header) surface."); }
}
