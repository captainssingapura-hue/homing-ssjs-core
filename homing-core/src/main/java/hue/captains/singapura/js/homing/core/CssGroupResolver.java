package hue.captains.singapura.js.homing.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

/**
 * Transitively resolves CSS dependencies for a list of {@link CssGroup}s.
 * Returns a flat list in dependency order (dependencies before dependents, no duplicates).
 *
 * <p>RFC 0064: the walk refuses a cycle with the names on it, and a prior that
 * declares dependencies — both are declaration errors, and this resolver runs
 * when a group module is served, so either fails loudly on first use rather
 * than recursing forever or loading in an order nobody intended.</p>
 */
public final class CssGroupResolver {

    private CssGroupResolver() {}

    public static List<CssGroup<?>> resolve(List<CssGroup<?>> roots) {
        SequencedSet<CssGroup<?>> resolved = new LinkedHashSet<>();
        Deque<Class<?>> path = new ArrayDeque<>();
        for (CssGroup<?> root : roots) {
            walk(root, resolved, path);
        }
        return new ArrayList<>(resolved);
    }

    private static void walk(CssGroup<?> current, SequencedSet<CssGroup<?>> resolved, Deque<Class<?>> path) {
        if (contains(resolved, current)) {
            return;
        }
        if (path.contains(current.getClass())) {
            List<String> names = new ArrayList<>();
            for (Class<?> c : path) names.add(c.getSimpleName());
            names.add(current.getClass().getSimpleName());
            throw new IllegalStateException("CSS dependency cycle: " + String.join(" -> ", names));
        }
        List<CssGroup<?>> deps = current.cssImports().imports();
        if (current.prior() && !deps.isEmpty()) {
            throw new IllegalStateException(
                    "CssGroup " + current.getClass().getSimpleName() + " is a prior and may not declare dependencies");
        }
        path.push(current.getClass());
        for (CssGroup<?> dep : deps) {
            walk(dep, resolved, path);
        }
        path.pop();
        resolved.add(current);
    }

    /** Groups are singletons per class; two instances of one class are the same node. */
    private static boolean contains(SequencedSet<CssGroup<?>> set, CssGroup<?> g) {
        for (CssGroup<?> have : set) if (have.getClass() == g.getClass()) return true;
        return false;
    }
}
