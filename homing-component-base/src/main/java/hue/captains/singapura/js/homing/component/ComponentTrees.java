package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.NodeIdentities;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.RigidTrees;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The walk, the composition and the guard over component catalogues.
 *
 * <p><b>Composition is derived.</b> A site names its top-level crates; the
 * closure of their {@code requires()} is walked, every crate that is a
 * {@link ComponentVehicle} has its catalogue normalised standalone at L0
 * and grafted under the site's root by a pure level shift
 * ({@link RigidTrees#graftUnder}) — nothing is listed by hand, and a
 * component keeps its segment however it is reached. A site that wants a
 * different arrangement writes portals; none does yet.</p>
 *
 * <p><b>The guard</b> ({@link #validate}) holds the logical side to the
 * physical: every catalogue leaf names a component whose module the same
 * crate ships and exports; every component a served DOM module exports is
 * listed by exactly one catalogue; parents are coherent, siblings' segments
 * distinct, and the composed tree's identities disjoint.</p>
 */
public final class ComponentTrees {

    private ComponentTrees() {}

    /** A composed tree with what each vertex is. */
    public record Composition(NormalizedNode root, Map<NodeIdentity, ComponentDetails> details) {
        public Composition {
            Objects.requireNonNull(root, "root");
            details = Map.copyOf(details);
        }
        public ComponentDetails detailsOf(NodeIdentity id) { return details.get(id); }
    }

    // ── the closure ───────────────────────────────────────────────────────

    /** The crates reachable from the top-level ones through {@code requires()}, top-level first, each once. */
    public static List<Crate> closure(List<Crate> topLevel) {
        var out = new LinkedHashSet<Crate>();
        for (Crate c : topLevel) walk(c, out);
        return List.copyOf(out);
    }
    private static void walk(Crate c, Set<Crate> out) {
        if (!out.add(c)) return;
        for (Crate r : c.requires()) walk(r, out);
    }

    /** The vehicles in the closure, in closure order. */
    public static List<Crate> vehicles(List<Crate> topLevel) {
        return closure(topLevel).stream().filter(c -> c instanceof ComponentVehicle).toList();
    }

    // ── composition ───────────────────────────────────────────────────────

    /** The site's root with every vehicle's catalogue grafted under it, derived from the closure. */
    public static Composition compose(String rootName, List<Crate> topLevel) {
        var details = new LinkedHashMap<NodeIdentity, ComponentDetails>();
        var kids = new ArrayList<NormalizedNode>();
        int components = 0;
        for (Crate crate : vehicles(topLevel)) {
            NormalizedNode standalone = normalize(crate, details);
            kids.add(RigidTrees.graftUnder(standalone, TreeLevel.L0.INSTANCE));
            components += countComponents(standalone);
        }
        var id = new ComponentNodeIdentity.OfRoot(rootName);
        details.put(id, new ComponentDetails.OfComposition(rootName, kids.size(), components));
        return new Composition(new NormalizedNode(TreeLevel.L0.INSTANCE, NodeName.slug(rootName), id, label(rootName), kids), details);
    }

    /** A vehicle's catalogue as a standalone tree rooted at L0, its details recorded. */
    public static NormalizedNode normalize(Crate crate, Map<NodeIdentity, ComponentDetails> details) {
        C0_Components<?> root = ((ComponentVehicle) crate).components();
        var kids = new ArrayList<NormalizedNode>();
        for (ComponentCatalogue<?> sub : root.subCatalogues()) kids.add(normalize(sub, crate, TreeLevel.L1.INSTANCE, List.of(root.segment()), details));
        for (ComponentEntry<?> e : root.leaves()) kids.add(leaf(e, crate, TreeLevel.L1.INSTANCE, List.of(root.segment()), details));
        var id = new ComponentNodeIdentity.OfVehicle(crate.name());
        var node = new NormalizedNode(TreeLevel.L0.INSTANCE, root.segment(), id, label(root.name()), kids);
        details.put(id, new ComponentDetails.OfVehicle(crate.name(), root.name(), root.summary(), countComponents(node)));
        return node;
    }

    @SuppressWarnings("unchecked")
    private static NormalizedNode normalize(ComponentCatalogue<?> cat, Crate crate, TreeLevel at, List<NodeName> path, Map<NodeIdentity, ComponentDetails> details) {
        var here = new ArrayList<>(path); here.add(cat.segment());
        var kids = new ArrayList<NormalizedNode>();
        TreeLevel below = at.below().orElseThrow();
        for (ComponentCatalogue<?> sub : cat.subCatalogues()) kids.add(normalize(sub, crate, below, here, details));
        for (ComponentEntry<?> e : cat.leaves()) kids.add(leaf(e, crate, below, here, details));
        var id = new ComponentNodeIdentity.OfCatalogue((Class<? extends ComponentCatalogue<?>>) cat.getClass());
        var node = new NormalizedNode(at, cat.segment(), id, label(cat.name()), kids);
        details.put(id, new ComponentDetails.OfCatalogue(cat.name(), cat.summary(), countComponents(node)));
        return node;
    }

    @SuppressWarnings("unchecked")
    private static NormalizedNode leaf(ComponentEntry<?> e, Crate crate, TreeLevel at, List<NodeName> path, Map<NodeIdentity, ComponentDetails> details) {
        UiComponent<?> c = ((ComponentEntry.OfComponent<?, ?>) e).component();
        var id = new ComponentNodeIdentity.OfComponent((Class<? extends UiComponent<?>>) c.getClass());
        var words = new ArrayList<String>();
        for (NodeName n : path) words.add(n.value());
        words.add(c.segment().value());
        details.put(id, new ComponentDetails.OfComponent(c.label(), c.shape(), c instanceof ElementComponent<?> el ? el.tag() : "",
                c.summary(), moduleName(c), crate.name(), List.copyOf(words)));
        return NormalizedNode.leaf(at, c.segment(), id, label(c.label()));
    }

    private static String moduleName(UiComponent<?> c) {
        try { return c.module().getClass().getName(); } catch (IllegalStateException e) { return "?"; }
    }

    private static Map<DimensionKey, DimensionValue> label(String text) {
        return Map.of(DisplayLabel.INSTANCE, new NameValue(text));
    }

    private static int countComponents(NormalizedNode n) {
        if (n.identity() instanceof ComponentNodeIdentity.OfComponent) return 1;
        int sum = 0;
        for (NormalizedNode k : n.children()) sum += countComponents(k);
        return sum;
    }

    // ── the guard ─────────────────────────────────────────────────────────

    /** The problems with the closure's catalogues against its modules; empty when the logical side matches the physical. */
    public static List<String> validate(List<Crate> topLevel) {
        var problems = new ArrayList<String>();
        var closure = closure(topLevel);
        var listed = new LinkedHashMap<Class<?>, String>();   // component class → the vehicle that lists it
        for (Crate crate : closure) {
            if (!(crate instanceof ComponentVehicle v)) continue;
            C0_Components<?> root = v.components();
            if (root == null) { problems.add(crate.name() + ": a vehicle with no catalogue"); continue; }
            var shipped = new HashSet<Class<?>>();
            for (CrateEntry e : crate.entries()) shipped.add(e.module().getClass());
            checkNode(crate, root, null, shipped, listed, problems);
        }
        // coverage: every component a served DOM module exports is listed once
        for (Crate crate : closure) {
            for (CrateEntry e : crate.entries()) {
                EsModule<?> m = e.module();
                if (!(m instanceof DomModule<?>)) continue;
                for (var x : m.exports().exports()) {
                    if (!(x instanceof UiComponent<?>)) continue;
                    if (!listed.containsKey(x.getClass()))
                        problems.add(crate.name() + ": " + m.getClass().getSimpleName() + " exports the component " + x.getClass().getSimpleName()
                                + (crate instanceof ComponentVehicle ? " but its catalogue does not list it" : " but the crate ships no catalogue"));
                }
            }
        }
        var composed = compose("check", topLevel).root();
        NodeIdentities.duplicatesIn(composed).forEach((id, n) -> problems.add("identity carried by " + n + " vertices: " + id));
        return List.copyOf(problems);
    }

    private static void checkNode(Crate crate, ComponentCatalogue<?> node, ComponentCatalogue<?> parent, Set<Class<?>> shipped,
                                  Map<Class<?>, String> listed, List<String> problems) {
        String where = crate.name() + "/" + node.segment().value();
        Object named = node instanceof C1_Components<?, ?> c1 ? c1.parent() : node instanceof C2_Components<?, ?> c2 ? c2.parent() : null;
        if (parent != null && !Objects.equals(named, parent))
            problems.add(where + " is listed under " + parent.segment().value() + " but names " + (named == null ? "no parent" : ((ComponentCatalogue<?>) named).segment().value()));
        if (node.name() == null || node.name().isBlank()) problems.add(where + " has no name");
        var segments = new HashSet<String>();
        for (ComponentCatalogue<?> sub : node.subCatalogues()) {
            if (!segments.add(sub.segment().value())) problems.add(where + ": segment repeated among siblings: " + sub.segment().value());
            checkNode(crate, sub, node, shipped, listed, problems);
        }
        for (ComponentEntry<?> e : node.leaves()) {
            UiComponent<?> c = ((ComponentEntry.OfComponent<?, ?>) e).component();
            String leaf = where + "/" + c.segment().value();
            if (!segments.add(c.segment().value())) problems.add(where + ": segment repeated among siblings: " + c.segment().value());
            String before = listed.putIfAbsent(c.getClass(), crate.name());
            if (before != null) problems.add(leaf + " is listed already by " + before);
            EsModule<?> m;
            try { m = c.module(); } catch (IllegalStateException ex) { problems.add(leaf + ": " + ex.getMessage()); continue; }
            if (!shipped.contains(m.getClass())) problems.add(leaf + " is exported by " + m.getClass().getSimpleName() + ", which " + crate.name() + " does not ship");
            if (m.exports().exports().stream().noneMatch(x -> x.getClass() == c.getClass()))
                problems.add(leaf + ": " + m.getClass().getSimpleName() + " does not export it");
        }
    }
}
