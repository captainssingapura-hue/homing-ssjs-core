package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.Root;

/**
 * Root of the ColorGroup taxonomy — colours classified by their nature (what
 * kind of job the colour does).
 */
public record Colors() implements Root, ColorGroup {

    public static final Colors INSTANCE = new Colors();

    @Override public Desc desc() {
        return Descs.of("Root of the colour-group taxonomy — colours classified by their nature.");
    }
}
