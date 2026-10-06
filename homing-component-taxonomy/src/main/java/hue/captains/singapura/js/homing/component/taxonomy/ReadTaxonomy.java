package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads a taxonomy from the components declared, and refuses it with every problem at once.
 *
 * <p>Only components need declaring. Their kinds are reached through their parents, since a
 * parent never lists its children; the components that play their roles are reached through
 * the roles, and theirs in turn. Each role becomes a part, its owner appended.</p>
 *
 * <p>Refused: a kind or component with no parent; a chain of parents that comes back on
 * itself; a role listed by a component it is not nested in, or listed twice; a role with no
 * component to play it; a component that is, through roles, a part of itself; two nodes that
 * derive one token. That only a leaf is concrete needs no check: only a {@link Branch} can be
 * named as a parent, and a {@link Component} is not one.</p>
 */
public record ReadTaxonomy() implements StatelessFunctionalObject {

    public Taxonomy read(Collection<? extends Component<?>> declared) {
        var problems = new ArrayList<TaxonomyProblem>();
        var kinds = new LinkedHashSet<Kind<?>>();
        var components = new LinkedHashSet<Component<?>>();
        var parts = new ArrayList<Part<?, ?>>();
        var plays = new LinkedHashMap<Component<?>, List<Component<?>>>();   // owner -> the components that play its roles

        var queue = new ArrayDeque<Component<?>>(declared);
        while (!queue.isEmpty()) {
            Component<?> c = queue.poll();
            if (!components.add(c)) continue;
            kinds.addAll(lineage(c, problems));
            var bases = new ArrayList<Component<?>>();
            var listed = new HashSet<Role<?>>();
            for (Role<?> role : c.roles()) {
                if (role == null) continue;
                if (!listed.add(role)) {
                    problems.add(new TaxonomyProblem(Rule.ROLE_TWICE, name(c) + " lists " + name(role) + " twice"));
                    continue;
                }
                if (role.getClass().getEnclosingClass() != c.getClass()) {
                    problems.add(new TaxonomyProblem(Rule.ROLE_NOT_ITS_OWN, name(c) + " lists " + name(role)
                            + ", which is not nested in it: only a component declares its roles"));
                    continue;
                }
                Component<?> base = role.base();
                if (base == null) {
                    problems.add(new TaxonomyProblem(Rule.NO_BASE, name(role) + " names no component to play it"));
                    continue;
                }
                parts.add(part(c, role));
                bases.add(base);
                queue.add(base);
            }
            plays.put(c, bases);
        }

        problems.addAll(compositionCycles(plays));
        var taxonomy = new Taxonomy(topDown(kinds), List.copyOf(components), parts);
        problems.addAll(tokensTwice(taxonomy));
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
        return taxonomy;
    }

    /** The kinds above a component, nearest first; a missing parent or a cycle is a problem, and ends the walk. */
    private static List<Kind<?>> lineage(Component<?> c, List<TaxonomyProblem> problems) {
        var out = new ArrayList<Kind<?>>();
        Branch b = c.parent();
        if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, name(c) + " names no parent"));
        var seen = new HashSet<Branch>();
        while (b instanceof Kind<?> k) {
            if (!seen.add(k)) {
                problems.add(new TaxonomyProblem(Rule.PARENT_CYCLE, "the parents above " + name(c) + " come back to " + name(k)));
                break;
            }
            out.add(k);
            b = k.parent();
            if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, name(k) + " names no parent"));
        }
        return out;
    }

    /** Kinds with their parents before them, each in the order first reached. */
    private static List<Kind<?>> topDown(Set<Kind<?>> kinds) {
        var out = new ArrayList<Kind<?>>();
        for (Kind<?> k : kinds) place(k, kinds, out, new HashSet<>());
        return out;
    }

    private static void place(Kind<?> k, Set<Kind<?>> kinds, List<Kind<?>> out, Set<Kind<?>> walking) {
        if (out.contains(k) || !walking.add(k)) return;
        if (k.parent() instanceof Kind<?> p && kinds.contains(p)) place(p, kinds, out, walking);
        out.add(k);
    }

    /** Every cycle of "plays a role in", each once, by the components it passes through. */
    private static List<TaxonomyProblem> compositionCycles(Map<Component<?>, List<Component<?>>> plays) {
        var out = new ArrayList<TaxonomyProblem>();
        var reported = new HashSet<Set<Component<?>>>();
        for (Component<?> start : plays.keySet()) {
            var path = new ArrayList<Component<?>>();
            path.add(start);
            walk(start, plays, path, reported, out);
        }
        return out;
    }

    private static void walk(Component<?> at, Map<Component<?>, List<Component<?>>> plays, List<Component<?>> path,
                             Set<Set<Component<?>>> reported, List<TaxonomyProblem> out) {
        for (Component<?> next : plays.getOrDefault(at, List.of())) {
            int i = path.indexOf(next);
            if (i >= 0) {
                var cycle = new ArrayList<>(path.subList(i, path.size()));
                if (reported.add(new HashSet<>(cycle))) {
                    var names = new ArrayList<String>();
                    for (Component<?> c : cycle) names.add(name(c));
                    names.add(name(next));
                    out.add(new TaxonomyProblem(Rule.COMPOSITION_CYCLE, "a part of itself: " + String.join(" → ", names)));
                }
                continue;
            }
            path.add(next);
            walk(next, plays, path, reported, out);
            path.remove(path.size() - 1);
        }
    }

    /** Two nodes, one token: each pair named. */
    private static List<TaxonomyProblem> tokensTwice(Taxonomy t) {
        var byToken = new LinkedHashMap<String, List<ComponentNode>>();
        for (ComponentNode n : t.nodes()) byToken.computeIfAbsent(t.token(n), x -> new ArrayList<>()).add(n);
        var out = new ArrayList<TaxonomyProblem>();
        byToken.forEach((token, nodes) -> {
            if (nodes.size() > 1)
                out.add(new TaxonomyProblem(Rule.TOKEN_TWICE, "'" + token + "' is derived by "
                        + String.join(" and ", nodes.stream().map(ReadTaxonomy::qualified).toList())));
        });
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Part<?, ?> part(Component<?> owner, Role<?> role) {
        return new Part(owner, role.base(), role);
    }

    /** A node as a reader would say it: {@code PlainButton}, a role {@code Dialog.Ok}. */
    private static String name(Object o) {
        if (o instanceof Part<?, ?> p) return name(p.role());
        Class<?> c = o.getClass();
        return o instanceof Role<?> && c.getEnclosingClass() != null
                ? c.getEnclosingClass().getSimpleName() + "." + c.getSimpleName()
                : c.getSimpleName();
    }

    /** A node's type without its package, where simple names are not enough: {@code Here$Badge}. */
    private static String qualified(Object o) {
        if (o instanceof Part<?, ?> p) return qualified(p.belongsTo()) + "." + p.name().value();
        String n = o.getClass().getName();
        return n.substring(n.lastIndexOf('.') + 1);
    }
}
