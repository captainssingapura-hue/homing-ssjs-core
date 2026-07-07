package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L1;

/** Colour a human reads as meaning — felt (Affective) or learned (Symbolic). */
public record Expressive() implements L1<Colors>, ColorGroup {

    public static final Expressive INSTANCE = new Expressive();

    @Override public Colors parent() { return Colors.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Colour a human reads as meaning.");
    }
}
