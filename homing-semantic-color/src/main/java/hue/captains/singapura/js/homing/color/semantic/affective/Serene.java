package hue.captains.singapura.js.homing.color.semantic.affective;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;

/** High valence, low arousal — calm and good (relaxed, at ease). */
public record Serene() implements SemanticColor, Leaf<Affective>, WithDesc {

    public static final Serene INSTANCE = new Serene();

    @Override public String name() { return "serene"; }
    @Override public Affective parent() { return Affective.INSTANCE; }
    @Override public Desc desc() { return Descs.of("High valence, low arousal — calm and good (relaxed)."); }
}
