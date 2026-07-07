package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L1;

/** Colour recognized as a brand or entity — the theme's signature. */
public record Identity() implements L1<Colors>, ColorGroup {

    public static final Identity INSTANCE = new Identity();

    @Override public Colors parent() { return Colors.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Colour recognized as a brand or entity — the theme's signature.");
    }
}
