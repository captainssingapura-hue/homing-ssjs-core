package hue.captains.singapura.js.homing.color.semantic.affective;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;

/** High valence, high arousal — happy and lively. */
public record Cheerful() implements SemanticColor, Leaf<Affective>, WithDesc {

    public static final Cheerful INSTANCE = new Cheerful();

    @Override public String name() { return "cheerful"; }
    @Override public Affective parent() { return Affective.INSTANCE; }
    @Override public Desc desc() { return Descs.of("High valence, high arousal — happy and lively."); }
}
