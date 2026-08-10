package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RFC 0044 — the transitive {@code requires} closure of a set of root crates:
 * every crate reachable from the roots, deduplicated by name, roots first. This
 * is how a studio turns its top-level (owned) crates into the full dependency
 * graph — including external (required-but-not-owned) crates.
 */
public final class CrateClosure {

    private CrateClosure() {}

    /** All crates reachable from {@code roots} via {@link Crate#requires()} (roots included). */
    public static List<Crate> of(Collection<? extends Crate> roots) {
        Map<String, Crate> byName = new LinkedHashMap<>();
        Deque<Crate> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            Crate c = queue.removeFirst();
            if (byName.putIfAbsent(c.name(), c) != null) continue;
            queue.addAll(c.requires());
        }
        return List.copyOf(byName.values());
    }
}
