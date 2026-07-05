package hue.captains.singapura.js.homing.realm.presentation;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.desc.Line;
import com.hominglabs.rebar.core.desc.Summary;

import java.util.Arrays;

/**
 * Small package-private convenience over rebar's {@code Desc / Summary / Line}
 * records — one call site per domain node instead of the three-record dance.
 * Not an abstraction over rebar; just sugar.
 */
final class Descs {
    private Descs() {}

    static Desc of(String summary, String... lines) {
        return new Desc(new Summary(summary), Arrays.stream(lines).map(Line::new).toList());
    }
}
