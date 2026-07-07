package hue.captains.singapura.js.homing.color.semantic.symbolic;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Symbolic;

/** Learned signal — a good, complete outcome. */
public record Success() implements SemanticColor, Leaf<Symbolic>, WithDesc {

    public static final Success INSTANCE = new Success();

    @Override public String name() { return "success"; }
    @Override public Symbolic parent() { return Symbolic.INSTANCE; }
    @Override public Desc desc() { return Descs.of("Learned signal — a good, complete outcome."); }
}
