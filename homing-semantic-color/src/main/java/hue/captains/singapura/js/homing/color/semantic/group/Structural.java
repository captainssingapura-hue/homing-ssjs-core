package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L1;

/** Colour that organizes the UI surface — hierarchy and legibility (surface / text / border). */
public record Structural() implements L1<Colors>, ColorGroup {

    public static final Structural INSTANCE = new Structural();

    @Override public Colors parent() { return Colors.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Colour that organizes the UI surface — hierarchy and legibility.");
    }
}
