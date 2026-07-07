package hue.captains.singapura.js.homing.color.semantic.symbolic;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Symbolic;

/** Learned signal — neutral, noteworthy information. */
public record Info() implements SemanticColor, Leaf<Symbolic>, WithDesc {

    public static final Info INSTANCE = new Info();

    @Override public String name() { return "info"; }
    @Override public Symbolic parent() { return Symbolic.INSTANCE; }
    @Override public Desc desc() { return Descs.of("Learned signal — neutral, noteworthy information."); }
}
