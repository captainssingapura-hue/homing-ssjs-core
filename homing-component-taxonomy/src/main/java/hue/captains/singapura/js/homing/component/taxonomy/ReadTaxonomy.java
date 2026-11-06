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
 * the types of state its tree files: it reaches every branch through the parents,
 * every component that plays a part through the slots, every role a slot or a role set names, every branch of
 * the role catalogue through the roles, every type of state a component has,
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
 *   <li>two nodes of the tree of states with one name; a type of state with fewer
 *       than two values, or one value twice; a design word for a type's rest, or for a value it
 *       lacks; two values answered by one design word;</li>
 *   <li>a family head with another between it and the root, and a component that says it heads a
 *       family ({@link FamilyHead});</li>
 *   <li>a leaf the document wears ({@link WornByTheDocument}) with anything an element's leaf has - parts,
 *       states, rooms, values, axes, a role set - or playing a part; a component that says the document
 *       wears it;</li>
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
 * Taxonomy#roles() roles}, and the {@link Taxonomy#states() states} tree. A taxonomy is read wherever it is served, and a running server needs no
 * enforcer.</p>
 *
 * <p>Nor does it read a meanings file. That every node means something, that every section is headed as a node,
 * and that no two nodes mean the same is held at build time too, by {@link MeaningsCheck} in the same tests; a
 * running server reads a node's meaning only when it is asked for one.</p>
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
        return read(declared, catalogued, List.of());
    }

    /**
     * The components declared, the roles a catalogue files, and the types of state the
     * tree files - each kept whether or not a component has it, so the tree is whole. Its
     * types come in the order given, then those only a component names.
     */
    public Taxonomy read(Collection<? extends Component<?>> declared, Collection<? extends Role<?>> catalogued,
                         Collection<? extends StateType<?>> stateTypes) {
        var problems = new ArrayList<TaxonomyProblem>();
        var reached = new Reached();
        for (Role<?> r : catalogued) if (r != null) reached.roles().add(r);
        reach(declared, reached, problems);
        var read = partsRead(reached, problems);

        // a role set's roles, as a slot's: what its component may be cast in is the taxonomy's, cast in or not yet (D55)
        for (Component<?> c : reached.components())
            if (c instanceof WithRoles<?> w && w.roleSet() != null)
                for (Role<?> r : orNone(w.roleSet().roles())) if (r != null) reached.roles().add(r);

        var roleBranches = new LinkedHashSet<RoleBranch>();
        for (Role<?> r : reached.roles()) roleBranches.addAll(filing(r, problems));

        var stateList = states(stateTypes, reached.components());
        var stateBranchList = stateBranches(stateList, problems);
        var branchList = byLevel(reached.branches(), ComponentBranch::level);
        var componentList = List.copyOf(reached.components());
        var roleBranchList = byLevel(roleBranches, RoleBranch::level);
        var roleList = List.copyOf(reached.roles());
        var checks = TaxonomyChecks.INSTANCE;
        problems.addAll(checks.compositionCycles(reached.plays()));
        problems.addAll(checks.tokensTwice(branchList, componentList, read));
        problems.addAll(new CatalogueNames().clashes(roleBranchList, roleList));
        problems.addAll(checks.roleNamesANode(branchList, componentList, roleList));
        problems.addAll(checks.extentsTwice(branchList, componentList));
        problems.addAll(LocalStyleBranchChecks.INSTANCE.problems(branchList, componentList, read));
        problems.addAll(checks.familyHeads(branchList, componentList));
        problems.addAll(checks.wornByTheDocument(componentList, read));
        problems.addAll(checks.castings(read, componentList));
        problems.addAll(TreeChecks.INSTANCE.clashes("states", StateNodes.INSTANCE.of(stateBranchList, stateList), StateNode::name));
        problems.addAll(TreeChecks.INSTANCE.badValues(stateList));
        problems.addAll(TreeChecks.INSTANCE.wordsTwice(stateList));
        problems.addAll(TreeChecks.INSTANCE.levels(stateList));
        problems.addAll(TreeChecks.INSTANCE.badChanges(stateList));
        problems.addAll(TreeChecks.INSTANCE.leafStates(componentList));
        if (!problems.isEmpty()) throw new RefusedTaxonomy(problems);
        return new Taxonomy(branchList, componentList, read, new RoleCatalogue(roleBranchList, roleList),
                new StateTree(stateBranchList, stateList));
    }

    /**
     * What is reached from the components declared: their branches, every component, the parts - each a Part, or a
     * branch's part to resolve once every component is read - the roles, and which components play an owner's parts.
     */
    private record Reached(Set<ComponentBranch> branches, Set<Component<?>> components, List<Object> parts,
                           Set<Role<?>> roles, Map<Component<?>, List<Component<?>>> plays) {

        /** Nothing reached yet. */
        Reached() { this(new LinkedHashSet<>(), new LinkedHashSet<>(), new ArrayList<>(), new LinkedHashSet<>(), new LinkedHashMap<>()); }
    }

    /** Every component reached from those declared, through the slots: each read, its slots held, its parts kept. */
    private void reach(Collection<? extends Component<?>> declared, Reached reached, List<TaxonomyProblem> problems) {
        var queue = new ArrayDeque<Component<?>>(declared);
        while (!queue.isEmpty()) {
            Component<?> c = queue.poll();
            if (!reached.components().add(c)) continue;
            reached.branches().addAll(lineage(c, problems));
            var bases = new ArrayList<Component<?>>();
            var named = new HashSet<Role<?>>();
            List<Slot<?>> slots = slotsOf(c, problems);
            kind(c, slots, problems);
            rooms(c, problems);
            values(c, problems);
            for (Slot<?> slot : slots) {
                if (slot == null) continue;
                Role<?> role = slot.role();
                if (role == null) {
                    problems.add(new TaxonomyProblem(Rule.NO_ROLE, Names.INSTANCE.of(c) + " has a part played by " + slot.players().say()
                            + " in no role"));
                    continue;
                }
                if (!named.add(role)) {
                    problems.add(new TaxonomyProblem(Rule.ROLE_TWICE, Names.INSTANCE.of(c) + " names " + Names.INSTANCE.of(role) + " twice"));
                    continue;
                }
                reached.roles().add(role);
                played(c, role, slot, reached.parts(), bases, queue, problems);
            }
            reached.plays().put(c, bases);
        }
    }

    /** A slot's players: its part kept, and the components that play it reached in their turn. */
    private void played(Component<?> c, Role<?> role, Slot<?> slot, List<Object> parts, List<Component<?>> bases,
                        ArrayDeque<Component<?>> queue, List<TaxonomyProblem> problems) {
        switch (slot.players()) {
            case Players.One one -> {
                if (one.component() == null) {
                    problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.INSTANCE.of(c) + "." + Names.INSTANCE.of(role) + " is played by nothing"));
                    return;
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
                    problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.INSTANCE.of(c) + "." + Names.INSTANCE.of(role) + " is played by nothing"));
                    return;
                }
                parts.add(new Pending(c, role, under.branch(), slot.cardinality()));
            }
        }
    }

    /** Every part: a branch's played by every component read under the branch - the instance's, a downstream's with the house's. */
    private List<Part<?, ?>> partsRead(Reached reached, List<TaxonomyProblem> problems) {
        var read = new ArrayList<Part<?, ?>>();
        for (Object o : reached.parts()) {
            if (o instanceof Part<?, ?> p) { read.add(p); continue; }
            Pending pending = (Pending) o;
            var under = new ArrayList<Component<?>>();
            for (Component<?> c : reached.components())
                for (ComponentBranch b = Levels.INSTANCE.parentOf(c); b != null; b = Levels.INSTANCE.parentOf(b))
                    if (b.equals(pending.branch())) { under.add(c); break; }
            if (under.isEmpty()) {
                problems.add(new TaxonomyProblem(Rule.NO_BASE, Names.INSTANCE.of(pending.owner()) + "." + Names.INSTANCE.of(pending.role())
                        + " is played by any " + Names.INSTANCE.of(pending.branch()) + ", and none is read"));
                continue;
            }
            read.add(new Part<Component<?>, Component<?>>(pending.owner(), pending.role(), under, pending.cardinality()));
            reached.plays().computeIfAbsent(pending.owner(), k -> new ArrayList<>()).addAll(under);
        }
        return read;
    }

    /** The types of state: those the tree files, then those only a component names. */
    private List<StateType<?>> states(Collection<? extends StateType<?>> stateTypes, Set<Component<?>> components) {
        var states = new LinkedHashSet<StateType<?>>();
        for (StateType<?> s : stateTypes) if (s != null) states.add(s);
        for (Component<?> c : components)
            for (StateType<?> s : orNone(c.states())) if (s != null) states.add(s);
        return List.copyOf(states);
    }

    /** The branches of the tree of states, reached through the types' parents; a missing parent is a problem. */
    private List<StateBranch> stateBranches(List<StateType<?>> states, List<TaxonomyProblem> problems) {
        var stateBranches = new LinkedHashSet<StateBranch>();
        for (StateType<?> s : states) {
            StateBranch b = s.parent();
            if (b == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.INSTANCE.of(s) + " names no parent"));
            else if (b instanceof L1_StateBranch<?> l1) {
                if (l1.parent() == null) problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.INSTANCE.of(l1) + " names no parent"));
                stateBranches.add(l1);
            }
        }
        return List.copyOf(stateBranches);
    }

    private <T> List<T> orNone(List<T> list) { return list == null ? List.of() : list; }

    /** A leaf declares no parts, a composite at least one, and no component says it is both (D53). */
    private void kind(Component<?> c, List<Slot<?>> slots, List<TaxonomyProblem> problems) {
        boolean leaf = c instanceof Leaf<?>, composite = c instanceof Composite<?>;
        if (leaf && composite) problems.add(new TaxonomyProblem(Rule.KIND_BADLY, Names.INSTANCE.of(c) + " says it is a leaf and a composite: a component is one"));
        else if (leaf && !slots.isEmpty()) problems.add(new TaxonomyProblem(Rule.KIND_BADLY, Names.INSTANCE.of(c) + " is a leaf, and declares parts: a component with parts is a composite"));
        else if (composite && slots.isEmpty()) problems.add(new TaxonomyProblem(Rule.KIND_BADLY, Names.INSTANCE.of(c) + " is a composite, and declares no parts: a component with none is a leaf"));
    }

    /** A component's rooms, each named once. */
    private void rooms(Component<?> c, List<TaxonomyProblem> problems) {
        var named = new HashSet<String>();
        for (Room room : c.rooms())
            if (!named.add(room.name())) problems.add(new TaxonomyProblem(Rule.ROOMS_BADLY, Names.INSTANCE.of(c) + " names its room " + room.name() + " twice"));
    }

    /** A component's values, each named once (a group's own are checked as it is made). */
    private void values(Component<?> c, List<TaxonomyProblem> problems) {
        var named = new HashSet<String>();
        for (FixedValue value : orNone(c.fixedValues()))
            if (!named.add(value.name())) problems.add(new TaxonomyProblem(Rule.VALUES_BADLY, Names.INSTANCE.of(c) + " names its value " + value.name() + " twice"));
    }

    /** A component's slots; a count that is no cardinality is a problem of the component that said it. */
    private List<Slot<?>> slotsOf(Component<?> c, List<TaxonomyProblem> problems) {
        try {
            List<Slot<?>> slots = c.parts();
            return slots == null ? List.of() : slots;
        } catch (BadCardinality e) {
            problems.add(new TaxonomyProblem(Rule.BAD_CARDINALITY, Names.INSTANCE.of(c) + ": " + e.getMessage()));
            return List.of();
        }
    }

    /** The branches above a component, nearest first, the root left out; a missing parent is a problem, and ends the walk. */
    private List<ComponentBranch> lineage(Component<?> c, List<TaxonomyProblem> problems) {
        var out = new ArrayList<ComponentBranch>();
        Taxon at = c;
        for (ComponentBranch b = Levels.INSTANCE.parentOf(at); !(b instanceof L0_ComponentBranch); b = Levels.INSTANCE.parentOf(at)) {
            if (b == null) {
                problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.INSTANCE.of(at) + " names no parent"));
                break;
            }
            out.add(b);
            at = b;
        }
        return out;
    }

    /** The branches above a role, nearest first, the root left out; a missing parent is a problem, and ends the walk. */
    private List<RoleBranch> filing(Role<?> r, List<TaxonomyProblem> problems) {
        var out = new ArrayList<RoleBranch>();
        RoleNode at = r;
        for (RoleBranch b = Levels.INSTANCE.parentOf(at); !(b instanceof L0_RoleBranch); b = Levels.INSTANCE.parentOf(at)) {
            if (b == null) {
                problems.add(new TaxonomyProblem(Rule.NO_PARENT, Names.INSTANCE.of(at) + " names no parent"));
                break;
            }
            out.add(b);
            at = b;
        }
        return out;
    }

    /** By level, root-most first, each level in the order first reached - so every parent comes before its children. */
    private <B> List<B> byLevel(Set<B> branches, java.util.function.ToIntFunction<B> level) {
        return branches.stream().sorted(Comparator.comparingInt(level)).toList();
    }

    /** A part played by any component under a branch, resolved once every component is read. */
    private record Pending(Component<?> owner, Role<?> role, ComponentBranch branch, Cardinality cardinality) {}
}
