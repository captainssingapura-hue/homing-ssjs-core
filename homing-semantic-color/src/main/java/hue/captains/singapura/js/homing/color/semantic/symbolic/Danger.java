package hue.captains.singapura.js.homing.color.semantic.symbolic;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Symbolic;

/** Learned signal — harm / error / stop. (Felt root: Affective "tense".) */
public record Danger() implements SemanticColor, Leaf<Symbolic>, WithDesc {

    public static final Danger INSTANCE = new Danger();

    @Override public String name() { return "danger"; }
    @Override public Symbolic parent() { return Symbolic.INSTANCE; }
    @Override public Desc desc() { return Descs.of("Learned signal — harm / error / stop."); }
}
