package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L2;

/**
 * Felt, pre-cognitive colour — landmark feelings positioned on the valence x
 * arousal plane. A 2-D continuous nature; {@code Degree} applies along the
 * arousal axis. Its colours are the landmark-feeling leaves.
 */
public record Affective() implements L2<Expressive>, ColorGroup {

    public static final Affective INSTANCE = new Affective();

    @Override public Expressive parent() { return Expressive.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Felt, pre-cognitive colour — landmark feelings on the valence x arousal plane.");
    }
}
