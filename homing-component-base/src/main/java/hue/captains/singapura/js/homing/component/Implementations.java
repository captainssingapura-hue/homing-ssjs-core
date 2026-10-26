package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Impl;
import hue.captains.singapura.js.homing.component.taxonomy.ImplFor;
import hue.captains.singapura.js.homing.component.taxonomy.ImplForBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ImplForLeavesOf;
import hue.captains.singapura.js.homing.component.taxonomy.Part;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The implementations against the taxonomy: every component's code anchored at a node of the tree,
 * and composed as its leaf declares. Read over a closure of crates, graded on the top level's own
 * modules:
 *
 * <ul>
 *   <li><b>Anchored.</b> A module that exports a component implements its leaf ({@link ImplFor}),
 *       or every leaf of a branch ({@link ImplForLeavesOf}); a module that implements a branch's base
 *       ({@link ImplForBranch}) exports no component.</li>
 *   <li><b>Declared.</b> The anchor is a node of the taxonomy: a leaf among its components, a
 *       branch among its branches. And an element's: a leaf the document wears ({@link
 *       hue.captains.singapura.js.homing.component.taxonomy.WornByTheDocument}) is a style's rule
 *       on the document's root, and no module implements it.</li>
 *   <li><b>Once.</b> One leaf, one module; one branch, one module - a leaf under a branch one
 *       module realizes whole is that module's alone.</li>
 *   <li><b>One class.</b> The module exports one thing: the component named as its leaf, the base
 *       class named as its branch, or the component named as the branch whose every leaf it
 *       realizes. A leaf's module may export its companion beside it: the factory that makes its
 *       element, a constant named for it.</li>
 *   <li><b>Alike but for the look.</b> The leaves one class realizes hold no parts: their behaviour
 *       is the class's, their look their own (D52).</li>
 *   <li><b>In the lineage.</b> A leaf's module imports a branch's base only when the leaf is under
 *       the branch.</li>
 *   <li><b>Composed as declared.</b> The leaves a leaf's module imports are its parts' - no more,
 *       no fewer. Every part is a component of its own, implemented by a module of the closure: a
 *       part no module implements is a problem of its owner's, never realized by its owner inline -
 *       an owner never makes its part's element (RFC 0071), and a widget draws only components. A
 *       part's own parts the owner may also import, to make them and hand them in: a button holding
 *       an icon need not know which icon, its owner does. A module that realizes every leaf of a branch
 *       stands for each leaf its importer's parts call for - one a slot names or any of the branch -
 *       and is imported only for one. A module that implements nothing is code its importers run,
 *       so the leaves it imports are theirs.</li>
 * </ul>
 */
public record Implementations() {

    public static final Implementations INSTANCE = new Implementations();

    /** The problems with the top level's implementations against the taxonomy; empty when every one is anchored and composed as declared. */
    public List<String> validate(Taxonomy taxonomy, List<Crate> topLevel) {
        var problems = new ArrayList<String>();
        var leaves = new LinkedHashMap<Component<?>, EsModule<?>>();         // a leaf → the module that implements it, over the closure
        var branches = new LinkedHashMap<ComponentBranch, EsModule<?>>();    // a branch → the module of its base
        var whole = new LinkedHashMap<ComponentBranch, EsModule<?>>();       // a branch → the module whose one class realizes its every leaf
        for (Crate crate : ComponentTrees.INSTANCE.closure(topLevel))
            for (CrateEntry e : crate.entries())
                index(e.module(), leaves, branches, whole, problems);
        for (var e : leaves.entrySet()) {
            EsModule<?> all = wholeOf(taxonomy, e.getKey(), whole);
            if (all != null)
                problems.add(simple(e.getKey()) + " is implemented twice: by " + all.getClass().getSimpleName() + ", every leaf of its branch, and " + e.getValue().getClass().getSimpleName());
        }

        var own = new ArrayList<EsModule<?>>();
        for (Crate crate : topLevel) for (CrateEntry e : crate.entries()) own.add(e.module());
        var components = Set.copyOf(taxonomy.components());
        var tree = Set.copyOf(taxonomy.branches());
        for (EsModule<?> m : own) {
            String name = m.getClass().getSimpleName();
            List<? extends Exportable<?>> exports = m.exports().exports();
            // a widget is no component (owner, 20 Oct), nor a layout (21 Oct): neither is anchored at a leaf
            boolean exportsComponent = exports.stream().anyMatch(x -> x instanceof UiComponent<?> c && c.shape() != UiComponent.Shape.WIDGET && c.shape() != UiComponent.Shape.LAYOUT);
            switch (m) {
                case ImplFor<?> impl -> {
                    Component<?> leaf = anchor(impl, name, problems);
                    if (leaf == null) continue;
                    if (!components.contains(leaf)) { problems.add(name + " implements " + simple(leaf) + ", which the taxonomy does not declare"); continue; }
                    if (taxonomy.wornByTheDocument(leaf)) {
                        problems.add(name + " implements " + simple(leaf) + ", which the document wears: its look is a style's rule on the document's root, and no module's");
                        continue;
                    }
                    if (!asItsLeafSays(exports, leaf))
                        problems.add(name + " implements " + simple(leaf) + " but exports " + said(exports)
                                + ": one module, one class - the component, named as its leaf - and at most its companion, named for it");
                    composition(taxonomy, m, leaf, leaves, whole, problems);
                }
                case ImplForBranch<?> impl -> {
                    ComponentBranch branch = anchor(impl, name, problems);
                    if (branch == null) continue;
                    if (!tree.contains(branch)) { problems.add(name + " implements the branch " + simple(branch) + ", which the taxonomy does not declare"); continue; }
                    if (exportsComponent)
                        problems.add(name + " implements the branch " + simple(branch) + " but exports a component: a branch is never realized, only its leaves are");
                    else if (exports.size() != 1 || !(exports.get(0) instanceof Exportable._Class<?>) || !simple(exports.get(0)).equals(simple(branch)))
                        problems.add(name + " implements the branch " + simple(branch) + " but exports " + said(exports) + ": one module, one class - the base, named as its branch");
                }
                case ImplForLeavesOf<?> impl -> {
                    ComponentBranch branch = anchor(impl, name, problems);
                    if (branch == null) continue;
                    if (!tree.contains(branch)) { problems.add(name + " implements every leaf of " + simple(branch) + ", a branch the taxonomy does not declare"); continue; }
                    if (exports.size() != 1 || !(exports.get(0) instanceof UiComponent<?>) || !simple(exports.get(0)).equals(simple(branch)))
                        problems.add(name + " implements every leaf of " + simple(branch) + " but exports " + said(exports)
                                + ": one module, one class - the component, named as the branch, made with the leaf it realizes");
                    for (Component<?> c : taxonomy.components())
                        if (taxonomy.fallback(c).contains(branch) && !taxonomy.partsOf(c).isEmpty())
                            problems.add(name + " implements every leaf of " + simple(branch) + ", but " + simple(c)
                                    + " holds parts: the leaves of one class are alike but for their look");
                }
                default -> {
                    if (exportsComponent)
                        problems.add(name + " exports the component " + said(exports.stream().filter(x -> x instanceof UiComponent<?>).toList())
                                + " but is anchored nowhere: a component's module implements its leaf (ImplFor)");
                }
            }
        }
        return List.copyOf(problems);
    }

    /** The leaves and branches implemented over the closure, each once: a branch's base, and a branch realized whole, apart. */
    private static void index(EsModule<?> m, Map<Component<?>, EsModule<?>> leaves, Map<ComponentBranch, EsModule<?>> branches,
                              Map<ComponentBranch, EsModule<?>> whole, List<String> problems) {
        if (!(m instanceof Impl impl)) return;
        Taxon anchor;
        try { anchor = impl.anchor(); } catch (IllegalStateException e) { return; }   // reported where it is graded
        EsModule<?> before = switch (anchor) {
            case Component<?> c -> leaves.putIfAbsent(c, m);
            case ComponentBranch b -> impl instanceof ImplForLeavesOf<?> ? whole.putIfAbsent(b, m) : branches.putIfAbsent(b, m);
        };
        if (before != null && before != m)
            problems.add(simple(anchor) + " is implemented twice: by " + before.getClass().getSimpleName() + " and " + m.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Taxon> T anchor(Impl impl, String name, List<String> problems) {
        try { return (T) impl.anchor(); }
        catch (IllegalStateException e) { problems.add(name + ": " + e.getMessage()); return null; }
    }

    /** The leaves a leaf's module imports against those its parts call for, and the bases it imports against its lineage. */
    private static void composition(Taxonomy taxonomy, EsModule<?> m, Component<?> leaf,
                                    Map<Component<?>, EsModule<?>> leaves, Map<ComponentBranch, EsModule<?>> whole, List<String> problems) {
        String who = m.getClass().getSimpleName() + " (" + simple(leaf) + ")";
        var expected = new LinkedHashMap<Component<?>, String>();   // a leaf → the part that calls for it
        expect(taxonomy, leaf, who, leaves, whole, expected, problems);
        expected.remove(leaf);   // a component holding more of itself imports nothing for it
        var handed = new HashSet<Component<?>>();   // the leaves its parts hold, at any depth: the owner may make them and hand them in
        held(taxonomy, leaf, new HashSet<>(List.of(leaf)), handed);
        var imported = new LinkedHashMap<Component<?>, String>();   // a leaf → the module it came from, and through which helper
        var wholes = new LinkedHashMap<ComponentBranch, String>();   // a branch realized whole → the module it came from, and through which helper
        var lineage = taxonomy.fallback(leaf);
        for (var key : m.imports().getAllImports().keySet()) {
            if (!(key instanceof EsModule<?> dep) || dep == m) continue;
            if (!(dep instanceof Impl impl)) { helped(dep, dep, new HashSet<>(List.of(m, dep)), imported, wholes); continue; }
            Taxon anchor;
            try { anchor = impl.anchor(); } catch (IllegalStateException e) { continue; }
            if (impl instanceof ImplForLeavesOf<?> && anchor instanceof ComponentBranch b) { wholes.putIfAbsent(b, dep.getClass().getSimpleName()); continue; }
            switch (anchor) {
                case Component<?> c -> imported.put(c, dep.getClass().getSimpleName());
                case ComponentBranch b -> {
                    if (!lineage.contains(b))
                        problems.add(who + " imports the base of " + simple(b) + ", a branch it is not under");
                }
            }
        }
        for (var w : wholes.entrySet()) {   // it stands for each leaf of the branch its parts call for or hold
            var called = new ArrayList<Component<?>>();
            for (var c : expected.keySet()) if (taxonomy.fallback(c).contains(w.getKey())) called.add(c);
            for (var c : handed) if (taxonomy.fallback(c).contains(w.getKey()) && !called.contains(c)) called.add(c);
            if (called.isEmpty()) problems.add(who + " imports every leaf of " + simple(w.getKey()) + " (" + w.getValue() + "), but none of its parts holds one");
            for (var c : called) imported.putIfAbsent(c, w.getValue());
        }
        for (var e : imported.entrySet())
            if (!expected.containsKey(e.getKey()) && !handed.contains(e.getKey()))
                problems.add(who + " imports " + simple(e.getKey()) + " (" + e.getValue() + "), which none of its parts declares");
        for (var e : expected.entrySet())
            if (!imported.containsKey(e.getKey()))
                problems.add(who + " never imports " + simple(e.getKey()) + ", which its part " + e.getValue() + " calls for");
    }

    /**
     * The leaves a module that implements nothing imports, and the branches realized whole, through any such modules it
     * imports in turn: its importer's, since it is the importer's code.
     */
    private static void helped(EsModule<?> helper, EsModule<?> via, Set<EsModule<?>> seen, Map<Component<?>, String> out,
                               Map<ComponentBranch, String> wholes) {
        for (var key : helper.imports().getAllImports().keySet()) {
            if (!(key instanceof EsModule<?> dep) || !seen.add(dep)) continue;
            if (!(dep instanceof Impl impl)) { helped(dep, via, seen, out, wholes); continue; }
            Taxon anchor;
            try { anchor = impl.anchor(); } catch (IllegalStateException e) { continue; }
            String said = dep.getClass().getSimpleName() + ", through " + via.getClass().getSimpleName();
            if (impl instanceof ImplForLeavesOf<?> && anchor instanceof ComponentBranch b) wholes.putIfAbsent(b, said);
            else if (anchor instanceof Component<?> c) out.putIfAbsent(c, said);
        }
    }

    /** Every leaf a component's parts hold, at any depth, realized or not. */
    private static void held(Taxonomy taxonomy, Component<?> owner, Set<Component<?>> seen, Set<Component<?>> out) {
        for (Part<?, ?> p : taxonomy.partsOf(owner))
            for (Component<?> base : p.bases())
                if (seen.add(base)) { out.add(base); held(taxonomy, base, seen, out); }
    }

    /** The leaves a component's parts call for, each implemented by a module of the closure; a part nothing implements, a problem. */
    private static void expect(Taxonomy taxonomy, Component<?> owner, String who, Map<Component<?>, EsModule<?>> leaves,
                               Map<ComponentBranch, EsModule<?>> whole, Map<Component<?>, String> out, List<String> problems) {
        for (Part<?, ?> p : taxonomy.partsOf(owner)) {
            String said = p.role().name().value();
            for (Component<?> base : p.bases()) {
                if (leaves.containsKey(base) || wholeOf(taxonomy, base, whole) != null) out.putIfAbsent(base, said);
                else if (!base.equals(owner))
                    problems.add(who + ": its part " + said + " is " + simple(base) + ", which no module implements - a part is a component of its own, never realized by its owner inline");
            }
        }
    }

    /** The module whose one class realizes every leaf of a branch the leaf is under, or null. */
    private static EsModule<?> wholeOf(Taxonomy taxonomy, Component<?> leaf, Map<ComponentBranch, EsModule<?>> whole) {
        for (var t : taxonomy.fallback(leaf))
            if (t instanceof ComponentBranch b && whole.containsKey(b)) return whole.get(b);
        return null;
    }

    /**
     * The component named as its leaf, alone or with its companion: the factory that makes the
     * element it is realized on, exported beside it as a constant named for it -
     * {@code PlainButtonFactory} for {@code PlainButton}.
     */
    private static boolean asItsLeafSays(List<? extends Exportable<?>> exports, Component<?> leaf) {
        if (exports.isEmpty() || exports.size() > 2) return false;
        if (!(exports.get(0) instanceof UiComponent<?>) || !simple(exports.get(0)).equals(simple(leaf))) return false;
        return exports.size() == 1
                || exports.get(1) instanceof Exportable._Constant<?> && simple(exports.get(1)).equals(simple(leaf) + "Factory");
    }

    private static String simple(Object o) { return o.getClass().getSimpleName(); }

    private static String said(List<? extends Exportable<?>> exports) {
        return exports.isEmpty() ? "nothing" : String.join(", ", exports.stream().map(Implementations::simple).toList());
    }
}
