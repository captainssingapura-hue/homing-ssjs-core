package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L1;

/** Colour that distinguishes or quantifies data; meaning is assigned externally. */
public record Encoding() implements L1<Colors>, ColorGroup {

    public static final Encoding INSTANCE = new Encoding();

    @Override public Colors parent() { return Colors.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Colour that distinguishes or quantifies data; meaning assigned externally.");
    }
}
