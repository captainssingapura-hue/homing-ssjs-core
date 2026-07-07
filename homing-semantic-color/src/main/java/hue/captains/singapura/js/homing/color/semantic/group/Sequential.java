package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L2;

/** Ordered magnitude — a ramp (heatmap). A scalar nature; {@code Degree} applies. */
public record Sequential() implements L2<Encoding>, ColorGroup {

    public static final Sequential INSTANCE = new Sequential();

    @Override public Encoding parent() { return Encoding.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Ordered magnitude — a ramp (heatmap). Degree applies.");
    }
}
