package hue.captains.singapura.js.homing.color.semantic.structural;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;

/** Secondary / muted text. */
public record TextMuted() implements SemanticColor, Leaf<Structural>, WithDesc {
    public static final TextMuted INSTANCE = new TextMuted();
    @Override public String name() { return "text-muted"; }
    @Override public Structural parent() { return Structural.INSTANCE; }
    @Override public Desc desc() { return Descs.of("Secondary / muted text."); }
}
