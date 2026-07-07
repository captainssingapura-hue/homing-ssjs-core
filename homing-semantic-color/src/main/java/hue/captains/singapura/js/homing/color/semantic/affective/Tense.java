package hue.captains.singapura.js.homing.color.semantic.affective;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;

/** Low valence, high arousal — stressed, on edge (the felt root of "danger"). */
public record Tense() implements SemanticColor, Leaf<Affective>, WithDesc {

    public static final Tense INSTANCE = new Tense();

    @Override public String name() { return "tense"; }
    @Override public Affective parent() { return Affective.INSTANCE; }
    @Override public Desc desc() { return Descs.of("Low valence, high arousal — stressed, on edge."); }
}
