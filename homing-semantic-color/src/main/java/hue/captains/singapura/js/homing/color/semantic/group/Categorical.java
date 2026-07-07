package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L2;

/** Unordered, maximally-distinct colours (tags, chart series). No {@code Degree}. */
public record Categorical() implements L2<Encoding>, ColorGroup {

    public static final Categorical INSTANCE = new Categorical();

    @Override public Encoding parent() { return Encoding.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Unordered, maximally-distinct colours — tags, chart series.");
    }
}
