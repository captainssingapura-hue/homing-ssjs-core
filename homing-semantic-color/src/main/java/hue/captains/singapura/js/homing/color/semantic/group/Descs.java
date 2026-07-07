package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.Line;
import com.hominglabs.rebar.core.desc.Summary;

import java.util.Arrays;

/** Sugar over rebar's {@code Desc / Summary / Line} — shared by group + leaf nodes. */
public final class Descs {
    private Descs() {}

    public static Desc of(String summary, String... lines) {
        return new Desc(new Summary(summary), Arrays.stream(lines).map(Line::new).toList());
    }
}
