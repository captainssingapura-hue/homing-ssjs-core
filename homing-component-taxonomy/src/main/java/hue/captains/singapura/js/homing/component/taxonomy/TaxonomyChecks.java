package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What a taxonomy refuses, seen only once it is read whole: an axis declared twice along a lineage, a family head under
 * another, a leaf the document wears with anything an element's leaf has, a component that is a part of itself, two nodes
 * with one token, a role named as a node, and a casting no role set allows. Checked where a taxonomy is read, each check
 * saying every problem it finds, so a read reports them all at once.
 */
record TaxonomyChecks() implements StatelessFunctionalObject {

    static final TaxonomyChecks INSTANCE = new TaxonomyChecks();

    /** An axis declared twice by one node, or declared by a node and a branch above it: each pair named. */
    List<TaxonomyProblem> extentsTwice(List<ComponentBranch> branches, List<Component<?>> components) {
        var nodes = new ArrayList<Taxon>();
        nodes.addAll(branches);
        nodes.addAll(components);
        var out = new ArrayList<TaxonomyProblem>();
        for (Taxon t : nodes) {
            var own = new LinkedHashSet<ExtentAxis>();
            for (ExtentAxis a : axes(t))
                if (!own.add(a)) out.add(new TaxonomyProblem(Rule.EXTENT_TWICE, Names.INSTANCE.of(t) + " declares " + a + " twice"));
            for (ComponentBranch b = Levels.INSTANCE.parentOf(t); b != null; b = Levels.INSTANCE.parentOf(b))
                for (ExtentAxis a : own)
                    if (axes(b).contains(a))
                        out.add(new TaxonomyProblem(Rule.EXTENT_TWICE, Names.INSTANCE.of(t) + " declares " + a + ", which "
                                + Names.INSTANCE.of(b) + " above it already does: every node under a branch has its axes"));
        }
        return out;
    }

    /** A family head under another, each pair named; a component that says it heads a family. */
    List<TaxonomyProblem> familyHeads(List<ComponentBranch> branches, List<Component<?>> components) {
        var out = new ArrayList<TaxonomyProblem>();
        for (ComponentBranch b : branches) {
            if (!(b instanceof FamilyHead)) continue;
            for (ComponentBranch above = Levels.INSTANCE.parentOf(b); above != null; above = Levels.INSTANCE.parentOf(above))
                if (above instanceof FamilyHead)
                    out.add(new TaxonomyProblem(Rule.FAMILY_HEAD_NESTED, Names.INSTANCE.of(b) + " heads a family under " + Names.INSTANCE.of(above)
                            + ", which heads one too: a family is headed at one level, no head between a head and the root"));
        }
        for (Component<?> c : components)
            if (c instanceof FamilyHead)
                out.add(new TaxonomyProblem(Rule.FAMILY_HEAD_NOT_A_BRANCH, Names.INSTANCE.of(c) + " is a component, and says it heads a family: "
                        + "only a branch does"));
        return out;
    }

    /**
     * A leaf the document wears ({@link WornByTheDocument}) with anything an element's leaf has - its one problem naming
     * each; one that plays a part, each part named; and a component that says the document wears it, which only a branch says.
     */
    List<TaxonomyProblem> wornByTheDocument(List<Component<?>> components, List<Part<?, ?>> parts) {
        var out = new ArrayList<TaxonomyProblem>();
        for (Component<?> c : components) {
            if (c instanceof WornByTheDocument)
                out.add(new TaxonomyProblem(Rule.WORN_BY_THE_DOCUMENT_NOT_A_BRANCH, Names.INSTANCE.of(c) + " is a component, and says the document wears it: "
                        + "only a branch does, for every leaf under it"));
            if (!underTheDocument(c)) continue;
            var has = new ArrayList<String>();
            if (c instanceof Composite<?>) has.add("parts");
            if (!orNone(c.states()).isEmpty()) has.add("states");
            if (!c.rooms().isEmpty()) has.add("rooms");
            if (!orNone(c.fixedValues()).isEmpty()) has.add("values");
            if (!axes(c).isEmpty()) has.add("axes");
            if (c instanceof WithRoles<?>) has.add("a role set");
            if (!has.isEmpty())
                out.add(new TaxonomyProblem(Rule.DOCUMENT_LEAF_BADLY, Names.INSTANCE.of(c) + " is worn by the document, and has " + String.join(", ", has)
                        + ": the document's leaf is a style's rule on its root, and nothing else - no element, no state, no part"));
        }
        for (Part<?, ?> p : parts)
            for (Component<?> player : p.bases())
                if (underTheDocument(player))
                    out.add(new TaxonomyProblem(Rule.DOCUMENT_LEAF_PLAYS_A_PART, Names.INSTANCE.of(player) + " plays " + Names.INSTANCE.of(p.owner()) + "." + Names.INSTANCE.of(p.role())
                            + ", and the document wears it: a part is an element of its owner's, and the document's leaf has none"));
        return out;
    }

    /** Whether a branch above a node says the document wears its leaves. */
    private boolean underTheDocument(Taxon t) {
        for (ComponentBranch b = Levels.INSTANCE.parentOf(t); b != null; b = Levels.INSTANCE.parentOf(b))
            if (b instanceof WornByTheDocument) return true;
        return false;
    }

    private <T> List<T> orNone(List<T> list) { return list == null ? List.of() : list; }

    private List<ExtentAxis> axes(Taxon t) {
        List<ExtentAxis> a = t.extents();
        return a == null ? List.of() : a;
    }

    /** Every cycle of "plays a part in", each once, by the components it passes through. */
    List<TaxonomyProblem> compositionCycles(Map<Component<?>, List<Component<?>>> plays) {
        var out = new ArrayList<TaxonomyProblem>();
        var reported = new HashSet<Set<Component<?>>>();
        for (Component<?> start : plays.keySet()) {
            var path = new ArrayList<Component<?>>();
            path.add(start);
            walk(start, plays, path, reported, out);
        }
        return out;
    }

    private void walk(Component<?> at, Map<Component<?>, List<Component<?>>> plays, List<Component<?>> path,
                             Set<Set<Component<?>>> reported, List<TaxonomyProblem> out) {
        for (Component<?> next : plays.getOrDefault(at, List.of())) {
            int i = path.indexOf(next);
            if (i >= 0) {
                var cycle = new ArrayList<>(path.subList(i, path.size()));
                if (reported.add(new HashSet<>(cycle))) {
                    var names = new ArrayList<String>();
                    for (Component<?> c : cycle) names.add(Names.INSTANCE.of(c));
                    names.add(Names.INSTANCE.of(next));
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
    List<TaxonomyProblem> tokensTwice(List<ComponentBranch> branches, List<Component<?>> components, List<Part<?, ?>> parts) {
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
                        + String.join(" and ", same.stream().map(Names.INSTANCE::qualified).toList())));
        });
        return out;
    }

    /** A role that shares a name with a branch or a component says what plays a part, not what it does. */
    List<TaxonomyProblem> roleNamesANode(List<ComponentBranch> branches, List<Component<?>> components, List<Role<?>> roles) {
        var nodes = new LinkedHashMap<String, Taxon>();
        nodes.put(Root.INSTANCE.name().value(), Root.INSTANCE);
        for (ComponentBranch b : branches) nodes.putIfAbsent(b.name().value(), b);
        for (Component<?> c : components) nodes.putIfAbsent(c.name().value(), c);
        var out = new ArrayList<TaxonomyProblem>();
        for (Role<?> r : roles) {
            Taxon same = nodes.get(r.name().value());
            if (same != null)
                out.add(new TaxonomyProblem(Rule.ROLE_NAMES_A_NODE, "the role " + Names.INSTANCE.qualified(r) + " is named as the "
                        + (same instanceof Component<?> ? "component " : "branch ") + Names.INSTANCE.qualified(same)
                        + ": a role names what a part does, not what plays it"));
        }
        return out;
    }

    /**
     * A component cast in a role with a look of its own only in one its role set allows (D55) - so its castings are its
     * own, and never an owner's to add - and each role set well made: some roles, each once, none that only places a
     * part.
     */
    List<TaxonomyProblem> castings(List<Part<?, ?>> parts, List<Component<?>> components) {
        var out = new ArrayList<TaxonomyProblem>();
        for (Component<?> c : components) {
            if (!(c instanceof WithRoles<?> w)) continue;
            RoleSet set = w.roleSet();
            if (set == null) { out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, Names.INSTANCE.of(c) + " says it is cast in roles, and gives no role set")); continue; }
            String named = Names.INSTANCE.of(c) + "'s role set, " + set.getClass().getSimpleName() + ",";
            List<Role<?>> in = orNone(set.roles());
            if (in.isEmpty()) out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " has no roles: a component cast in none is no WithRoles"));
            var seen = new HashSet<Role<?>>();
            for (Role<?> r : in) {
                if (r == null) out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " names no role"));
                else if (!seen.add(r)) out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " names " + Names.INSTANCE.of(r) + " twice"));
                else if (new Casting(c, r).functional())
                    out.add(new TaxonomyProblem(Rule.ROLE_SET_BADLY, named + " names " + Names.INSTANCE.of(r) + ", a role that only places a part: no casting"));
            }
        }
        for (Part<?, ?> p : parts)
            for (Component<?> b : p.bases()) {
                if (b instanceof LayoutStyling<?>) {
                    out.add(new TaxonomyProblem(Rule.LAYOUT_STYLING_PLAYS_A_PART, Names.INSTANCE.of(p.owner()) + " casts " + Names.INSTANCE.of(b) + " as " + Names.INSTANCE.of(p.role())
                            + ": the layouts' styling owns parts for their design, and is no part of anything"));
                    continue;
                }
                if (new Casting(b, p.role()).functional() || b instanceof VirtualComponent<?>) continue;   // a virtual leaf plays no part at all
                if (!(b instanceof WithRoles<?> w) || w.roleSet() == null)
                    out.add(new TaxonomyProblem(Rule.ROLE_NOT_ALLOWED, Names.INSTANCE.of(p.owner()) + " casts " + Names.INSTANCE.of(b) + " as " + Names.INSTANCE.of(p.role())
                            + ", a role with a look of its own, and " + Names.INSTANCE.of(b) + " has no role set: a component says the roles it may be cast in (WithRoles)"));
                else if (!orNone(w.roleSet().roles()).contains(p.role()))
                    out.add(new TaxonomyProblem(Rule.ROLE_NOT_ALLOWED, Names.INSTANCE.of(p.owner()) + " casts " + Names.INSTANCE.of(b) + " as " + Names.INSTANCE.of(p.role())
                            + ", a role its set, " + w.roleSet().getClass().getSimpleName() + ", does not allow"));
            }
        return out;
    }
}
