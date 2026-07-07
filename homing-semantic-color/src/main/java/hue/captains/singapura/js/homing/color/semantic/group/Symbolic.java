package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L2;

/**
 * Learned / cultural colour meaning — status (danger / success / warning),
 * auspicious, mourning. Discrete and culture-variable; a per-theme / per-culture
 * authored mapping, not derivable.
 */
public record Symbolic() implements L2<Expressive>, ColorGroup {

    public static final Symbolic INSTANCE = new Symbolic();

    @Override public Expressive parent() { return Expressive.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Learned / cultural colour meaning — status, auspicious, mourning.");
    }
}
