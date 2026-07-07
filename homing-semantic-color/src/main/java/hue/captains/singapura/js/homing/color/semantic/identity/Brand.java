package hue.captains.singapura.js.homing.color.semantic.identity;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.WithDesc;
import com.hominglabs.rebar.core.level.Leaf;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Descs;
import hue.captains.singapura.js.homing.color.semantic.group.Identity;

/** The theme's signature hue (maps to StudioVars {@code accent}). */
public record Brand() implements SemanticColor, Leaf<Identity>, WithDesc {

    public static final Brand INSTANCE = new Brand();

    @Override public String name() { return "brand"; }
    @Override public Identity parent() { return Identity.INSTANCE; }
    @Override public Desc desc() { return Descs.of("The theme's signature hue."); }
}
