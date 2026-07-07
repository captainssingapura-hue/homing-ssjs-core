package hue.captains.singapura.js.homing.color.semantic.affective;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;

/** The centre — even, unmarked (mid valence, mid arousal). */
public record Neutral() implements SemanticColor, Leaf<Affective>, WithDesc {

    public static final Neutral INSTANCE = new Neutral();

    @Override public String name() { return "neutral"; }
    @Override public Affective parent() { return Affective.INSTANCE; }
    @Override public Desc desc() { return Descs.of("The centre — even, unmarked."); }
}
