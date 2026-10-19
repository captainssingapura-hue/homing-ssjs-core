package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads a taxonomy from the components a deployment declares, the roles its catalogue files, and
 * the types of state and of event its trees file: it reaches every branch through the parents,
 * every component that plays a part through the slots, every role a slot or a role set names, every branch of
 * the role catalogue through the roles, every type of state a component has and of event it offers,
 * and their branches through their parents, and refuses - with every problem it found at once -
 * what the compiler cannot:
 *
 * <ul>
 *   <li>a branch, a component or a role with no parent;</li>
 *   <li>a slot played by nothing or in no role, or a role a component names twice;</li>
 *   <li>a component that is a part of itself, however deep - whatever the cardinalities on the
 *       way, since a cycle is refused on the types;</li>
 *   <li>two nodes with one token; two nodes of the role catalogue - branches or roles - with one
 *       name, which the catalogue itself refuses to be constructed with; or a role named as a node;</li>
 *   <li>a count that is no cardinality - thrown where it is said, and reported for the component
 *       that said it, so one component's bad count never hides another's problems.</li>
 *   <li>an axis declared twice along one lineage;</li>
 *   <li>a local-style branch holding a component that is not virtual, a virtual leaf with parts, or a
 *       branch; a part played by a virtual leaf;</li>
 *   <li>two nodes of the tree of states, or of events, with one name; a type of state with fewer
 *       than two values, or one value twice; a design word for a type's rest, or for a value it
 *       lacks; two values answered by one design word;</li>
 *   <li>a node of any of the four trees with no meaning, a meanings file with a section astray, or
 *       two nodes that mean the same;</li>
 *   <li>a family head with another between it and the root, and a component that says it heads a
 *       family ({@link FamilyHead});</li>
 *   <li>a component cast in a role with a look of its own that its role set does not allow, or that has no
 *       role set ({@link WithRoles}); a role set with no roles, a role twice, or one that only places a part
 *       (D55).</li>
 * </ul>
 *
 * <p>What the levels already make impossible it never checks: a chain of parents that comes back
 * on itself, a second root, a tree deeper than level 8.</p>
 *
 * <p>Nor does it judge whether a node holds state. That every node is a stateless functional
 * object is held at build time, by the tests of whoever declares the nodes, with jOntology's
 * enforcer over what was read - its {@link Taxonomy#branches() branches}, {@link
 * Taxonomy#components() components}, {@link Taxonomy#roleBranches() role branches}, {@link
 * Taxonomy#roles() roles}, and the {@link Taxonomy#states() states} and {@link Taxonomy#events()
 * events} trees. A taxonomy is read wherever it is served, and a running server needs no
 * enforcer.</p>
 */
public record ReadTaxonomy() implements StatelessFunctionalObject {

    /** The components declared, and only the roles they name. */
    public Taxonomy read(Collection<? extends Component<?>> declared) { return read(declared, List.of()); }

    /**
     * The components declared, and the roles a catalogue files - those no component names are kept, and
     * reported. The roles come in the catalogue's order, then those only a slot names, as the components
     * come in the order declared, then those only a part reaches.
     */
    public Taxonomy read(Collection<? extends Component<?>> declared, Collection<? extends Role<?>> catalogued) {
        return read(declared, catalogued, List.of(), List.of());
    }

    /**
     * The components declared, the roles a catalogue files, and the types of state and of event the
     * trees file - each kept whether or not a component has it, so the trees are whole. Each tree's
     * types come in the order given, then those only a component names.
     */
    public Taxonomy read(Collection<? extends Component<?>> declared, Collection<? extends Role<?>> catalogued,
                         Collection<? extends StateType<?>> stateTypes, Collection<? extends EventType<?>> eventTypes) {
        var problems = new ArrayList<TaxonomyProblem>();
        var branches = new LinkedHashSet<ComponentBranch>();
        var components = new LinkedHashSet<Component<?>>();
        var parts = new ArrayList<Object>();   // each a Part, or a branch's part to resolve once every component is read
        var roles = new LinkedHashSet<Role<?>>();
        var plays = new LinkedHashMap<Component<?>, List<Component<?>>>();   // owner -> the components that play its parts
        for (Role<?> r : catalogued) if (r != null) roles.add(r);

        var queue = new ArrayDeque<Component<?>>(declared);
        while (!queue.isEmpty()) {
            Component<?> c = queue.poll();
            if (!components.add(c)) continue;
            branches.addAll(lineage(c, problems));
            var bases = new ArrayList<Component<?>>();
            var named = new HashSet<Role<?>>();
            List<Slot<?>> slots = slotsOf(c, problems);
            kind(c, slots, problems);
            for (Slot<?> slot : slots) {
                if (slot == null) continue;
                Role<?> role = slot.role();
                if (role == null) {
                    problems.add(new TaxonomyProblem(Rule.NO_ROLE, Names.of(c) + " has a part played by " + slot.players().say()
                            + " in no role"));
                    continue;
                }
                if (!named.add(role)) {
                    problems.add(new TaxonomyProblem(Rule.ROLE_TWICE, Names.of(c) + " names " + Names.of(role) + " twice"));
                    continue;
                }
                roles.add(role);
                switch (slot.players()) {
                    case Players.One one -> {
                        if (one.component() == null) {
                            problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.of(c) + "." + Names.of(role) + " is played by nothing"));
                            continue;
                        }
                        parts.add(new Part<Component<?>, Component<?>>(c, role, List.of(one.component()), slot.cardinality()));
                        bases.add(one.component());
                        queue.add(one.component());
                    }
                    case Players.AnyOf some -> {
                        parts.add(new Part<Component<?>, Component<?>>(c, role, some.components(), slot.cardinality()));
                        bases.addAll(some.components());
                        queue.addAll(some.components());
                    }
                    case Players.Under under -> {
                        if (under.branch() == null) {
                            problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.of(c) + "." + Names.of(role) + " is played by nothing"));
                            continue;
                        }
                        parts.add(new Pending(c, role, under.branch(), slot.cardinality()));
                    }
                }
            }
            plays.put(c, bases);
        }

        // a branch's part: played by every component read under the branch - the instance's, a downstream's with the house's
        var read = new ArrayList<Part<?, ?>>();
        for (Object o : parts) {
            if (o instanceof Part<?, ?> p) { read.add(p); continue; }
            Pending pending = (Pending) o;
            var under = new ArrayList<Component<?>>();
            for (Component<?> c : components)
                for (ComponentBranch b = Levels.parentOf(c); b != null; b = Levels.parentOf(b))
                    if (b.equals(pending.branch())) { under.add(c); break; }
            if (under.isEmpty()) {
                problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.of(pending.owner()) + "." + Names.of(pending.role())
                        + " is played by any " + Names.of(pending.branch()) + ", and none is read"));
                continue;
            }
            read.add(new Part<Component<?>, Component<?>>(pending.owner(), pending.role(), under, pending.cardinality()));
            plays.computeIfAbsent(pending.owner(), k -> new ArrayList<>()).addAll(under);
        }

        // a role set's roles, as a slot's: what its component may be cast in is the taxonomy's, cast in or not yet (D55)
        for (Component<?> c : components)
            if (c instanceof WithRoles<?> w && w.roleSet() != null)
                for (Role<?> r : orNone(w.roleSet().roles())) if (r != null) roles.add(r);

        var roleBranches = new LinkedHashSet<RoleBranch>();
        for (Role<?> r : roles) roleBranches.addAll(filing(r, problems));

        var states = new LinkedHashSet<StateType<?>>();
        var events = new LinkedHashSet<EventType<?>>();
        for (StateType<?> s : stateTypes) if (s != null) states.add(s);
        for (EventType<?> e : eventTypes) if (e != null) events.add(e);
        for (Component<?> c : components) {
            for (StateType<?> s : orNone(c.states())) if (s != null) states.add(s);
            for (EventType<?> e : orNone(c.events())) if (e != null) events.add(e);
        }
        var stateBranches = new LinkedHashSet<StateBranch>();
        for (StateType<?> s : states) {
            StateBranch b = s.parent();
            if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(s) + " names no parent"));
            else if (b instanceof L1_StateBranch<?> l1) {
                if (l1.parent() == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(l1) + " names no parent"));
                stateBranches.add(l1);
            }
        }
        var eventBranches = new LinkedHashSet<EventBranch>();
        for (EventType<?> e : events) {
            EventBranch b = e.parent();
            if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(e) + " names no parent"));
            else if (b instanceof L1_EventBranch<?> l1) {
                if (l1.parent() == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(l1) + " names no parent"));
                eventBranches.add(l1);
            }
        }
        var stateBranchList = List.copyOf(stateBranches);
        var stateList = List.copyOf(states);
        var eventBranchList = List.copyOf(eventBranches);
        var eventList = List.copyOf(events);

        var branchList = byLevel(branches, ComponentBranch::level);
        var componentList = List.copyOf(components);
        var roleBranchList = byLevel(roleBranches, RoleBranch::level);
        var roleList = List.copyOf(roles);
        problems.addAll(compositionCycles(plays));
        problems.addAll(tokensTwice(branchList, componentList, read));
        problems.addAll(new CatalogueNames().clashes(roleBranchList, roleList));
        problems.addAll(roleNamesANode(branchList, componentList, roleList));
        problems.addAll(extentsTwice(branchList, componentList));
        problems.addAll(LocalStyleBranchChecks.INSTANCE.problems(branchList, componentList, read));
        problems.addAll(familyHeads(branchList, componentList));
        problems.addAll(castings(read, componentList));
        problems.addAll(TreeChecks.INSTANCE.clashes("states", StateTree.nodes(stateBranchList, stateList), StateNode::name));
        problems.addAll(TreeChecks.INSTANCE.badValues(stateList));
        problems.addAll(TreeChecks.INSTANCE.wordsTwice(stateList));
        problems.addAll(TreeChecks.INSTANCE.levels(stateList));
        problems.addAll(TreeChecks.INSTANCE.leafStates(componentList));
        problems.addAll(TreeChecks.INSTANCE.clashes("events", EventTree.nodes(eventBranchList, eventList), EventNode::name));
        var meant = meant(branchList, componentList, roleBranchList, roleList);
        if (!stateList.isEmpty()) meant.addAll(StateTree.nodes(stateBranchList, stateList));
        if (!eventList.isEmpty()) meant.addAll(EventTree.nodes(eventBranchList, eventList));
        problems.addAll(Meanings.INSTANCE.problems(meant));
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
        return new Taxonomy(branchList, componentList, read, new RoleCatalogue(roleBranchList, roleList),
                new StateTree(stateBranchList, stateList), new EventTree(eventBranchList, eventList));
    }

    /** Every node of the first two trees that must mean something: each tree's root when anything of the tree is read, its branches, its leaves. */
    private static List<Object> meant(List<ComponentBranch> branches, List<Component<?>> components,
                                      List<RoleBranch> roleBranches, List<Role<?>> roles) {
        var out = new ArrayList<Object>();
        if (!components.isEmpty()) out.add(Root.INSTANCE);
        out.addAll(branches);
        out.addAll(components);
        if (!roles.isEmpty()) out.add(RoleRoot.INSTANCE);
        out.addAll(roleBranches);
        out.addAll(roles);
        return out;
    }

    /** An axis declared twice by one node, or declared by a node and a branch above it: each pair named. */
    private static List<TaxonomyProblem> extentsTwice(List<ComponentBranch> branches, List<Component<?>> components) {
        var nodes = new ArrayList<Taxon>();
        nodes.addAll(branches);
        nodes.addAll(components);
        var out = new ArrayList<TaxonomyProblem>();
        for (Taxon t : nodes) {
            var own = new LinkedHashSet<ExtentAxis>();
            for (ExtentAxis a : axes(t))
                if (!own.add(a)) out.add(new TaxonomyProblem(Rule.EXTENT_TWICE, Names.of(t) + " declares " + a + " twice"));
            for (ComponentBranch b = Levels.parentOf(t); b != null; b = Levels.parentOf(b))
                for (ExtentAxis a : own)
                    if (axes(b).contains(a))
                        out.add(new TaxonomyProblem(Rule.EXTENT_TWICE, Names.of(t) + " declares " + a + ", which "
                                + Names.of(b) + " above it already does: every node under a branch has its axes"));
        }
        return out;
    }

    /** A family head under another, each pair named; a component that says it heads a family. */
    private static List<TaxonomyProblem> familyHeads(List<ComponentBranch> branches, List<Component<?>> components) {
        var out = new ArrayList<TaxonomyProblem>();
        for (ComponentBranch b : branches) {
            if (!(b instanceof FamilyHead)) continue;
            for (ComponentBranch above = Levels.parentOf(b); above != null; above = Levels.parentOf(above))
                if (above instanceof FamilyHead)
                    out.add(new TaxonomyProblem(Rule.FAMILY_HEAD_NESTED, Names.of(b) + " heads a family under " + Names.of(above)
                            + ", which heads one too: a family is headed at one level, no head between a head and the root"));
        }
        for (Component<?> c : components)
            if (c instanceof FamilyHead)
                out.add(new TaxonomyProblem(Rule.FAMILY_HEAD_NOT_A_BRANCH, Names.of(c) + " is a component, and says it heads a family: "
                        + "only a branch does"));
        return out;
    }

    private static <T> List<T> orNone(List<T> list) { return list == null ? List.of() : list; }

    private static List<ExtentAxis> axes(Taxon t) {
        List<ExtentAxis> a = t.extents();
        return a == null ? List.of() : a;
    }

    /** A leaf declares no parts, a composite at least one, and no component says it is both (D53). */
    private static void kind(Component<?> c, List<Slot<?>> slots, List<TaxonomyProblem> problems) {
        boolean leaf = c instanceof Leaf<?>, composite = c instanceof Composite<?>;
        if (leaf && composite) problems.add(new TaxonomyProblem(Rule.KIND_BADLY, Names.of(c) + " says it is a leaf and a composite: a component is one"));
        else if (leaf && !slots.isEmpty()) problems.add(new TaxonomyProblem(Rule.KIND_BADLY, Names.of(c) + " is a leaf, and declares parts: a component with parts is a composite"));
        else if (composite && slots.isEmpty()) problems.add(new TaxonomyProblem(Rule.KIND_BADLY, Names.of(c) + " is a composite, and declares no parts: a component with none is a leaf"));
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

    /** The branches above a component, nearest first, the root left out; a missing parent is a problem, and ends the walk. */
    private static List<ComponentBranch> lineage(Component<?> c, List<TaxonomyProblem> problems) {
        var out = new ArrayList<ComponentBranch>();
        Taxon at = c;
        for (ComponentBranch b = Levels.parentOf(at); !(b instanceof L0_ComponentBranch); b = Levels.parentOf(at)) {
            if (b == null) {
                problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(at) + " names no parent"));
                break;
            }
            out.add(b);
            at = b;
        }
        return out;
    }

    /** The branches above a role, nearest first, the root left out; a missing parent is a problem, and ends the walk. */
    private static List<RoleBranch> filing(Role<?> r, List<TaxonomyProblem> problems) {
        var out = new ArrayList<RoleBranch>();
        RoleNode at = r;
        for (RoleBranch b = Levels.parentOf(at); !(b instanceof L0_RoleBranch); b = Levels.parentOf(at)) {
            if (b == null) {
                problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.of(at) + " names no parent"));
                break;
            }
            out.add(b);
            at = b;
        }
        return out;
    }

    /** By level, root-most first, each level in the order first reached - so every parent comes before its children. */
    private static <B> List<B> byLevel(Set<B> branches, java.util.function.ToIntFunction<B> level) {
        return branches.stream().sorted(Comparator.comparingInt(level)).toList();
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
    private static List<TaxonomyProblem> tokensTwice(List<ComponentBranch> branches, List<Component<?>> components, List<Part<?, ?>> parts) {
        var nodes = new ArrayList<ComponentNode>();
        nodes.add(Root.INSTANCE);
        nodes.addAll(branches);
        nodes.addAll(components);
        nodes.addAll(parts);
        var byToken = new LinkedHashMap<String, List<ComponentNode>>();
        for (ComponentNode n : nodes) byToken.computeIfAbsent(n.token(), x -> new ArrayList<>()).add(n);
        var out = new ArrayList<TaxonomyProblem>();
        byToken.forEach((token, same) -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.TOKEN_TWICE, "'" + token + "' is derived by "
                        + String.join(" and ", same.stream().map(Names::qualified).toList())));
        });
        return out;
    }

    /** A role that shares a name with a branch or a component says what plays a part, not what it does. */
    private static List<TaxonomyProblem> roleNamesANode(List<ComponentBranch> branches, List<Component<?>> components, List<Role<?>> roles) {
        var nodes = new LinkedHashMap<String, Taxon>();
        nodes.put(Root.INSTANCE.name().value(), Root.INSTANCE);
        for (ComponentBranch b : branches) nodes.putIfAbsent(b.name().value(), b);
        for (Component<?> c : components) nodes.putIfAbsent(c.name().value(), c);
        var out = new ArrayList<TaxonomyProblem>();
        for (Role<?> r : roles) {
            Taxon same = nodes.get(r.name().value());
            if (same != null)
                out.add(new TaxonomyProblem(Rule.ROLE_NAMES_A_NODE, "the role " + Names.qualified(r) + " is named as the "
                        + (same instanceof Component<?> ? "component " : "branch ") + Names.qualified(same)
                        + ": a role names what a part does, not what plays it"));
        }
        return out;
    }

    /**
     * A component cast in a role with a look of its own only in one its role set allows (D55) - so its castings are its
     * own, and never an owner's to add - and each role set well made: some roles, each once, none that only places a
     * part.
     */
    private static List<TaxonomyProblem> castings(List<Part<?, ?>> parts, List<Component<?>> components) {
        var out = new ArrayList<TaxonomyProblem>();
        for (Component<?> c : components) {
            if (!(c instanceof WithRoles<?> w)) continue;
            RoleSet set = w.roleSet();
            if (set == null) { out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, Names.of(c) + " says it is cast in roles, and gives no role set")); continue; }
            String named = Names.of(c) + "'s role set, " + set.getClass().getSimpleName() + ",";
            List<Role<?>> in = orNone(set.roles());
            if (in.isEmpty()) out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " has no roles: a component cast in none is no WithRoles"));
            var seen = new HashSet<Role<?>>();
            for (Role<?> r : in) {
                if (r == null) out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " names no role"));
                else if (!seen.add(r)) out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " names " + Names.of(r) + " twice"));
                else if (new Casting(c, r).functional())
                    out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " names " + Names.of(r) + ", a role that only places a part: no casting"));
            }
        }
        for (Part<?, ?> p : parts)
            for (Component<?> b : p.bases()) {
                if (new Casting(b, p.role()).functional() || b instanceof VirtualComponent<?>) continue;   // a virtual leaf plays no part at all
                if (!(b instanceof WithRoles<?> w) || w.roleSet() == null)
                    out.add(new TaxonomyProblem(Rule.ROLE_NOT_ALLOWED, Names.of(p.owner()) + " casts " + Names.of(b) + " as " + Names.of(p.role())
                            + ", a role with a look of its own, and " + Names.of(b) + " has no role set: a component says the roles it may be cast in (WithRoles)"));
                else if (!orNone(w.roleSet().roles()).contains(p.role()))
                    out.add(new TaxonomyProblem(Rule.ROLE_NOT_ALLOWED, Names.of(p.owner()) + " casts " + Names.of(b) + " as " + Names.of(p.role())
                            + ", a role its set, " + w.roleSet().getClass().getSimpleName() + ", does not allow"));
            }
        return out;
    }

    /** A part played by any component under a branch, resolved once every component is read. */
    private record Pending(Component<?> owner, Role<?> role, ComponentBranch branch, Cardinality cardinality) {}
}
