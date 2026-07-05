package hue.captains.singapura.js.homing.realm.plan;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.Line;
import com.hominglabs.rebar.core.desc.Summary;

import java.util.Arrays;

/** Package-private sugar over rebar's {@code Desc / Summary / Line}. */
final class Descs {
    private Descs() {}

    static Desc of(String summary, String... lines) {
        return new Desc(new Summary(summary), Arrays.stream(lines).map(Line::new).toList());
    }
}
