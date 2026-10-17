package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Reads local styles (RFC 0067, appendix Local Styles): each as the module that registers it lists it - a concrete
 * leaf for every leaf of its branch - judged complete over its branch and closed at every leaf's owned targets, in every
 * style given - and refuses, with every problem it found at once, what the compiler cannot:
 *
 * <ul>
 *   <li>a concrete leaf whose leaf or local style cannot be read off its declaration, one that names another local style
 *       than the one listing it, or one that says nothing of how it is drawn;</li>
 *   <li>two local styles with one name, or one given twice; one in no scheme;</li>
 *   <li>a local style of a branch, or a concrete leaf of a leaf, the taxonomy has not - its branch is registered with
 *       it;</li>
 *   <li>a local style with two concrete leaves of one leaf, or none of a leaf of its branch;</li>
 *   <li>a virtual leaf that owns no target, one twice, or one no sheet carries;</li>
 *   <li>in a style: a local style its scheme does not see; drawings that cannot be made, a target drawn that its leaf
 *       does not own, one drawn twice, one owned and left undrawn - nothing falls through to the style - a drawing taken
 *       from astray, and an animation effect that takes a property another target the leaf owns writes.</li>
 * </ul>
 *
 * <p>The pairings the types hold - a leaf locked to its branch, a concrete leaf to one local style of the same branch -
 * it never checks.</p>
 */
public record ReadLocalStyles() implements StatelessFunctionalObject {

    public static final ReadLocalStyles INSTANCE = new ReadLocalStyles();

    /** The local styles, each over the taxonomy, its drawings checked in every style given - none, to read them alone. */
    public List<LocalStyleRead> read(Taxonomy taxonomy, Collection<InStyle> styles, Collection<? extends LocalStyle<?, ?>> localStyles) {
        var problems = new ArrayList<LocalStyleProblem>();
        var out = new ArrayList<LocalStyleRead>();

        // 1. each local style once, in its scheme, its concrete leaves its own, of its branch's leaves
        var given = new HashSet<Class<?>>();
        var byName = new LinkedHashMap<String, List<LocalStyle<?, ?>>>();
        for (LocalStyle<?, ?> ls : localStyles) {
            if (!given.add(ls.getClass())) {
                problems.add(new LocalStyleProblem(Rule.NAME_TWICE, ls.token() + " is given twice"));
                continue;
            }
            byName.computeIfAbsent(ls.token(), n -> new ArrayList<>()).add(ls);
            if (Schemes.INSTANCE.of(ls).isEmpty())
                problems.add(new LocalStyleProblem(Rule.NO_SCHEME, ls.token() + " says no scheme: a local style says the scheme it is in"));
            LocalStyleRead read = listed(taxonomy, ls, problems);
            if (read != null) out.add(read);
        }
        byName.forEach((name, same) -> {
            if (same.size() > 1)
                problems.add(new LocalStyleProblem(Rule.NAME_TWICE, "'" + name + "' names " + same.size() + " local styles: "
                        + String.join(" and ", same.stream().map(ls -> ls.getClass().getName()).toList())));
        });

        // 2. what each leaf owns, once a leaf
        var owned = new HashMap<VirtualComponent<?>, List<Target>>();
        for (LocalStyleRead read : out)
            for (VirtualComponent<?> leaf : leaves(taxonomy, read.branch())) owned.computeIfAbsent(leaf, l -> owns(read, l, problems));

        // 3. in every style: every owned target drawn once, nothing else drawn, each redirect landing
        for (InStyle style : styles)
            for (LocalStyleRead read : out) {
                ColorScheme mine = Schemes.INSTANCE.of(read.localStyle()).orElse(null), wears = Schemes.INSTANCE.of(style.style()).orElse(null);
                if (mine == null || wears == null) continue;   // refused above, or by the styling
                if (!Schemes.INSTANCE.sees(wears, mine)) {
                    problems.add(new LocalStyleProblem(Rule.SCHEME_UNSEEN, read.token() + " is " + Schemes.INSTANCE.name(mine) + "'s, which " + style
                            + "'s scheme, " + Schemes.INSTANCE.name(wears) + ", does not see: a local style is drawn only in a style whose scheme sees its own"));
                    continue;
                }
                for (ConcreteComponent<?, ?, ?> c : read.concretes()) drawnIn(taxonomy, c, owned.getOrDefault(c.virtual(), List.of()), style, problems);
            }

        if (!problems.isEmpty()) throw new RefusedLocalStyles(problems);
        return List.copyOf(out);
    }

    /** A local style as it lists itself: its concrete leaves in its branch's order - or null, where it cannot be read so. */
    private static LocalStyleRead listed(Taxonomy taxonomy, LocalStyle<?, ?> ls, List<LocalStyleProblem> problems) {
        List<? extends ConcreteComponent<?, ?, ?>> listed = ls.concretes();
        if (listed == null || listed.isEmpty()) {
            problems.add(new LocalStyleProblem(Rule.LEAF_MISSING, ls.token() + " lists no concrete leaf: a local style draws every leaf of its branch"));
            return null;
        }
        var byLeaf = new LinkedHashMap<VirtualComponent<?>, ConcreteComponent<?, ?, ?>>();
        ComponentBranch branch = null;
        for (ConcreteComponent<?, ?, ?> c : listed) {
            VirtualComponent<?> leaf;
            LocalStyle<?, ?> names;
            try {
                leaf = c.virtual();
                names = c.localStyle();
            } catch (IllegalStateException e) {
                problems.add(new LocalStyleProblem(Rule.UNREADABLE, ls.token() + " lists " + e.getMessage()));
                continue;
            }
            if (names.getClass() != ls.getClass())
                problems.add(new LocalStyleProblem(Rule.NAMES_ANOTHER, ls.token() + " lists " + ResolveDrawing.name(c) + ", a concrete leaf of "
                        + names.token() + ": a local style lists its own"));
            if (!(c instanceof Drawn))
                problems.add(new LocalStyleProblem(Rule.NOT_DRAWN, ResolveDrawing.name(c) + " says nothing of how it is drawn: it implements Drawn"));
            if (branch == null) branch = leaf.parent();   // one branch for all: the types hold it
            ConcreteComponent<?, ?, ?> before = byLeaf.putIfAbsent(leaf, c);
            if (before != null)
                problems.add(new LocalStyleProblem(Rule.LEAF_TWICE, ls.token() + " draws " + leaf.token() + " twice: "
                        + ResolveDrawing.name(before) + " and " + ResolveDrawing.name(c)));
        }
        if (branch == null) return null;   // no concrete leaf could be read: refused above
        if (!taxonomy.nodes().contains(branch)) {
            problems.add(new LocalStyleProblem(Rule.NOT_IN_TAXONOMY, ls.token() + " styles " + branch.token()
                    + ", which the taxonomy has not: the module that registers a local style brings its branch"));
            return null;
        }
        List<VirtualComponent<?>> leaves = leaves(taxonomy, branch);
        for (VirtualComponent<?> leaf : byLeaf.keySet())
            if (!leaves.contains(leaf))
                problems.add(new LocalStyleProblem(Rule.NOT_IN_TAXONOMY, ls.token() + " draws " + leaf.token() + ", a leaf of " + branch.token()
                        + " the taxonomy has not"));
        var ordered = new ArrayList<ConcreteComponent<?, ?, ?>>();
        for (VirtualComponent<?> leaf : leaves) {
            ConcreteComponent<?, ?, ?> c = byLeaf.get(leaf);
            if (c == null)
                problems.add(new LocalStyleProblem(Rule.LEAF_MISSING, ls.token() + " draws no " + leaf.token() + ", a leaf of "
                        + branch.token() + ": a local style is complete over its branch"));
            else ordered.add(c);
        }
        return new LocalStyleRead(ls, branch, ordered, taxonomy);
    }

    /** A concrete leaf's drawings in a style: every target its leaf owns drawn once, nothing else, each redirect landing. */
    private static void drawnIn(Taxonomy taxonomy, ConcreteComponent<?, ?, ?> c, List<Target> owned, InStyle style, List<LocalStyleProblem> problems) {
        if (!(c instanceof Drawn drawn)) return;   // refused above
        List<Drawing<?>> drawings;
        try {
            drawings = drawn.drawings(style.style());
        } catch (RuntimeException e) {
            problems.add(new LocalStyleProblem(Rule.DRAWING_REFUSED, ResolveDrawing.name(c) + " in " + style + ": " + e.getMessage()));
            return;
        }
        var seen = new HashSet<Target>();
        for (Drawing<?> dr : drawings) {
            if (!owned.contains(dr.target()))
                problems.add(new LocalStyleProblem(Rule.NOT_OWNED, ResolveDrawing.name(c) + " in " + style + " draws "
                        + dr.target().token() + ", which " + c.virtual().token() + " does not own"));
            else if (!seen.add(dr.target()))
                problems.add(new LocalStyleProblem(Rule.DRAWN_TWICE, ResolveDrawing.name(c) + " in " + style + " draws "
                        + dr.target().token() + " twice"));
        }
        for (Target t : owned) {
            if (!seen.contains(t)) {
                problems.add(new LocalStyleProblem(Rule.UNDRAWN, ResolveDrawing.name(c) + " in " + style + " leaves "
                        + t.token() + " undrawn: draw it, say none, or take another's"));
                continue;
            }
            ResolveDrawing.Resolved drawing;
            try {
                drawing = ResolveDrawing.INSTANCE.resolve(taxonomy, c, t, style);
            } catch (IllegalArgumentException e) {
                problems.add(new LocalStyleProblem(Rule.AS_ASTRAY, "in " + style + ": " + e.getMessage()));
                continue;
            }
            if (drawing.effect() != null) takesOwned(c, drawing.effect(), owned, style, problems);
        }
    }

    /** An effect takes what it needs: a property another target the leaf owns writes is refused, by name. */
    private static void takesOwned(ConcreteComponent<?, ?, ?> c, AnimationEffect effect, List<Target> owned, InStyle style, List<LocalStyleProblem> problems) {
        var takes = Effects.INSTANCE.takes(effect);
        for (Target other : owned) {
            if (other instanceof Target.Animation) continue;
            for (String p : other.properties())
                if (takes.contains(p))
                    problems.add(new LocalStyleProblem(Rule.EFFECT_TAKES_OWNED, ResolveDrawing.name(c) + " in " + style + " wears "
                            + effect.token() + ", which takes " + p + " - and " + c.virtual().token() + " owns " + other.token()
                            + ", which writes it: what an effect takes, nothing else there may write"));
        }
    }

    /** The leaves of a local-style branch, in the taxonomy's order. */
    private static List<VirtualComponent<?>> leaves(Taxonomy taxonomy, ComponentBranch branch) {
        var out = new ArrayList<VirtualComponent<?>>();
        for (var n : taxonomy.children(branch)) if (n instanceof VirtualComponent<?> v) out.add(v);
        return List.copyOf(out);
    }

    /** What a leaf owns, held to the rules: a target at least, each once, each one a sheet carries. */
    private static List<Target> owns(LocalStyleRead read, VirtualComponent<?> leaf, List<LocalStyleProblem> problems) {
        List<Target> targets = read.owns(leaf);
        if (targets.isEmpty()) {
            problems.add(new LocalStyleProblem(Rule.OWNS_BADLY, leaf.token() + " owns no target: a virtual leaf says what its concrete leaves answer, with Owns"));
            return targets;
        }
        var seen = new HashSet<Target>();
        for (Target t : targets) {
            if (!seen.add(t)) problems.add(new LocalStyleProblem(Rule.OWNS_BADLY, leaf.token() + " owns " + t.token() + " twice"));
            if (t.carrier() != Carrier.CSS)
                problems.add(new LocalStyleProblem(Rule.OWNS_BADLY, leaf.token() + " owns " + t.token() + ", which no sheet carries"));
        }
        return targets;
    }
}
