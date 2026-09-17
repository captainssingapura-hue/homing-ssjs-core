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
 * declares dependencies; RFC 0066: and a prior that is not a palette. All are
 * declaration errors, and this resolver runs
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
            // push() stacks at the head; the walk's order is the reverse.
            for (var it = path.descendingIterator(); it.hasNext(); ) names.add(it.next().getSimpleName());
            names.add(current.getClass().getSimpleName());
            throw new IllegalStateException("CSS dependency cycle: " + String.join(" -> ", names));
        }
        List<CssGroup<?>> deps = CssImportsFor.dependenciesOf(current);
        if (current.prior()) {
            if (!deps.isEmpty()) {
                throw new IllegalStateException(
                        "CssGroup " + current.getClass().getSimpleName() + " is a prior and may not declare dependencies");
            }
            // RFC 0066 — a prior is the claim "everything leans on me without
            // saying so". Only a palette may make it: the global palette is the
            // one implicit node of a deployment. A drawn group that wants to be
            // loaded first is depended on, not prior.
            boolean holdsPalette = false;
            for (CssClass<?> c : current.cssClasses()) if (c instanceof PaletteClass<?>) { holdsPalette = true; break; }
            if (!holdsPalette) {
                throw new IllegalStateException(
                        "CssGroup " + current.getClass().getSimpleName()
                        + " is a prior but holds no PaletteClass: only a palette is prior; a group"
                        + " others lean on is named in their dependsOn()");
            }
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
