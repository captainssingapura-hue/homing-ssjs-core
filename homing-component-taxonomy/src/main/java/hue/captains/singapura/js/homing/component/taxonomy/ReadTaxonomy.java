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
 * Reads a taxonomy from the components a deployment declares, and the roles its catalogue files:
 * it reaches every kind through the parents, every component that plays a part through the
 * slots, every role a slot names and every branch of the role catalogue through the roles, and
 * refuses - with every problem it found at once - what the compiler cannot:
 *
 * <ul>
 *   <li>a kind, a component, a branch or a role with no parent, a chain of parents that comes
 *       back on itself, or a branch that is its own parent - a second root;</li>
 *   <li>a slot played by nothing or in no role, or a role a component names twice;</li>
 *   <li>a component that is a part of itself, however deep - whatever the cardinalities on the
 *       way, since a cycle is refused on the types;</li>
 *   <li>two nodes with one token, two roles with one name, or a role named as a node;</li>
 *   <li>a count that is no cardinality - thrown where it is said, and reported for the component
 *       that said it, so one component's bad count never hides another's problems.</li>
 * </ul>
 */
public record ReadTaxonomy() implements StatelessFunctionalObject {

    /** The components declared, and only the roles they name. */
    public Taxonomy read(Collection<? extends Component<?>> declared) { return read(declared, List.of()); }

    /** The components declared, and the roles a catalogue files - those no component names are kept, and reported. */
    public Taxonomy read(Collection<? extends Component<?>> declared, Collection<? extends Role<?>> catalogued) {
        var problems = new ArrayList<TaxonomyProblem>();
        var kinds = new LinkedHashSet<Kind<?>>();
        var components = new LinkedHashSet<Component<?>>();
        var parts = new ArrayList<Part<?, ?>>();
        var roles = new LinkedHashSet<Role<?>>();
        var plays = new LinkedHashMap<Component<?>, List<Component<?>>>();   // owner -> the components that play its parts

        var queue = new ArrayDeque<Component<?>>(declared);
        while (!queue.isEmpty()) {
            Component<?> c = queue.poll();
            if (!components.add(c)) continue;
            kinds.addAll(lineage(c, problems));
            var bases = new ArrayList<Component<?>>();
            var named = new HashSet<Role<?>>();
            for (Slot<?> slot : slotsOf(c, problems)) {
                if (slot == null) continue;
                Role<?> role = slot.role();
                if (role == null) {
                    problems.add(new TaxonomyProblem(Rule.NO_ROLE, Names.of(c) + " has a part played by " + Names.of(slot.base())
                            + " in no role"));
                    continue;
                }
                if (!named.add(role)) {
                    problems.add(new TaxonomyProblem(Rule.ROLE_TWICE, Names.of(c) + " names " + Names.of(role) + " twice"));
                    continue;
                }
                roles.add(role);
                Component<?> base = slot.base();
                if (base == null) {
                    problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.of(c) + "." + Names.of(role) + " is played by nothing"));
                    continue;
                }
                parts.add(new Part<Component<?>, Component<?>>(c, role, base, slot.cardinality()));
                bases.add(base);
                queue.add(base);
            }
            plays.put(c, bases);
        }
        for (Role<?> r : catalogued) if (r != null) roles.add(r);

        var branches = new LinkedHashSet<RoleBranch<?>>();
        for (Role<?> r : roles) branches.addAll(filing(r, problems));

        problems.addAll(compositionCycles(plays));
        var taxonomy = new Taxonomy(topDown(kinds), List.copyOf(components), parts, topDownBranches(branches), List.copyOf(roles));
        problems.addAll(tokensTwice(taxonomy));
        problems.addAll(roleNamesTwice(roles));
        problems.addAll(roleNamesANode(taxonomy));
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
        return taxonomy;
    }

    /** A component's slots; a count that is no cardinality is a problem of the component that said it. */
    private static List<Slot<?>> slotsOf(Component<?> c, List<TaxonomyProblem> problems) {
        try {
            List<Slot<?>> slots = c.parts();
            return slots == null ? List.of() : slots;
        } catch (BadCardinality e) {
            problems.add(new TaxonomyProblem(Rule.BAD_CARDINALITY, Names.of(c) + ": " + e.getMessage()));
            return List.of();
        }
    }

    /** The kinds above a component, nearest first; a missing parent or a cycle is a problem, and ends the walk. */
    private static List<Kind<?>> lineage(Component<?> c, List<TaxonomyProblem> problems) {
        var out = new ArrayList<Kind<?>>();
        Branch b = c.parent();
        if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(c) + " names no parent"));
        var seen = new HashSet<Branch>();
        while (b instanceof Kind<?> k) {
            if (!seen.add(k)) {
                problems.add(new TaxonomyProblem(Rule.PARENT_CYCLE, "the parents above " + Names.of(c) + " come back to " + Names.of(k)));
                break;
            }
            out.add(k);
            b = k.parent();
            if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(k) + " names no parent"));
        }
        return out;
    }

    /**
     * The branches above a role up to the root, nearest first, the root left out. A missing parent,
     * a chain that comes back on itself, or a branch that is its own parent - a second root - is a
     * problem, and ends the walk.
     */
    private static List<RoleBranch<?>> filing(Role<?> r, List<TaxonomyProblem> problems) {
        var out = new ArrayList<RoleBranch<?>>();
        RoleBranch<?> b = r.parent();
        if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(r) + " names no parent"));
        var seen = new HashSet<RoleBranch<?>>();
        while (b != null && !(b instanceof RoleRoot)) {
            if (!seen.add(b)) {
                problems.add(new TaxonomyProblem(Rule.PARENT_CYCLE, "the branches above " + Names.of(r) + " come back to " + Names.of(b)));
                break;
            }
            out.add(b);
            RoleBranch<?> up = b.parent();
            if (up == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(b) + " names no parent"));
            else if (up.equals(b)) {
                problems.add(new TaxonomyProblem(Rule.PARENT_CYCLE, Names.of(b) + " is its own parent: only the root is"));
                break;
            }
            b = up;
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

    /** Branches with their parents before them, each in the order first reached. */
    private static List<RoleBranch<?>> topDownBranches(Set<RoleBranch<?>> branches) {
        var out = new ArrayList<RoleBranch<?>>();
        for (RoleBranch<?> b : branches) placeBranch(b, branches, out, new HashSet<>());
        return out;
    }

    private static void placeBranch(RoleBranch<?> b, Set<RoleBranch<?>> branches, List<RoleBranch<?>> out, Set<RoleBranch<?>> walking) {
        if (out.contains(b) || !walking.add(b)) return;
        RoleBranch<?> p = b.parent();
        if (p != null && branches.contains(p)) placeBranch(p, branches, out, walking);
        out.add(b);
    }

    /** Every cycle of "plays a part in", each once, by the components it passes through. */
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
                    for (Component<?> c : cycle) names.add(Names.of(c));
                    names.add(Names.of(next));
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
                        + String.join(" and ", nodes.stream().map(Names::qualified).toList())));
        });
        return out;
    }

    /** Two roles, one name: one word means one thing. */
    private static List<TaxonomyProblem> roleNamesTwice(Collection<Role<?>> roles) {
        var byName = new LinkedHashMap<String, List<Role<?>>>();
        for (Role<?> r : roles) byName.computeIfAbsent(r.name().value(), x -> new ArrayList<>()).add(r);
        var out = new ArrayList<TaxonomyProblem>();
        byName.forEach((name, same) -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.ROLE_NAME_TWICE, "'" + name + "' is answered to by "
                        + String.join(" and ", same.stream().map(Names::qualified).toList())));
        });
        return out;
    }

    /** A role that shares a name with a kind or a component says what plays a part, not what it does. */
    private static List<TaxonomyProblem> roleNamesANode(Taxonomy t) {
        var nodes = new LinkedHashMap<String, Taxon>();
        nodes.put(Root.INSTANCE.name().value(), Root.INSTANCE);
        for (Kind<?> k : t.kinds()) nodes.putIfAbsent(k.name().value(), k);
        for (Component<?> c : t.components()) nodes.putIfAbsent(c.name().value(), c);
        var out = new ArrayList<TaxonomyProblem>();
        for (Role<?> r : t.roles()) {
            Taxon same = nodes.get(r.name().value());
            if (same != null)
                out.add(new TaxonomyProblem(Rule.ROLE_NAMES_A_NODE, "the role " + Names.qualified(r) + " is named as the "
                        + (same instanceof Component<?> ? "component " : "kind ") + Names.qualified(same)
                        + ": a role names what a part does, not what plays it"));
        }
        return out;
    }
}
