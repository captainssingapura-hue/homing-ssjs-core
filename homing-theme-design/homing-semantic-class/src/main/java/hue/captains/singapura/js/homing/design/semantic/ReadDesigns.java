package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.DesignProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Reads the designs of a taxonomy's design branches (RFC 0067, appendix Virtual Leaves and Designs): the
 * concrete leaves gathered by the design each names, each design judged complete over its branch and closed
 * at every leaf's owned targets, in every theme given - and refuses, with every problem it found at once,
 * what the compiler cannot:
 *
 * <ul>
 *   <li>a concrete leaf whose leaf or design cannot be read off its declaration, or that says nothing of
 *       how it is drawn;</li>
 *   <li>two designs with one name;</li>
 *   <li>a design with two concrete leaves of one leaf, or none of a leaf of its branch;</li>
 *   <li>a virtual leaf that owns no target, one twice, or one no sheet carries;</li>
 *   <li>in a theme: drawings that cannot be made, a target drawn that its leaf does not own, one drawn
 *       twice, one owned and left undrawn - nothing falls through to the theme - a drawing taken from
 *       astray, one taken from a component the theme cannot give it from: a virtual leaf, one the
 *       taxonomy has not, one it leaves unanswered at rest - and an animation effect that takes a property
 *       another target the leaf owns writes.</li>
 * </ul>
 *
 * <p>A concrete leaf of a leaf this taxonomy has not is another taxonomy's, and is passed over. The
 * pairings the types hold - a leaf locked to its branch, a concrete leaf to one design of the same branch
 * - it never checks.</p>
 */
public record ReadDesigns() implements StatelessFunctionalObject {

    public static final ReadDesigns INSTANCE = new ReadDesigns();

    /** The designs every extension on the classpath of this class loader adds to the taxonomy's branches. */
    public List<DesignRead> read(Taxonomy taxonomy, Collection<Dressed> themes, ClassLoader loader) {
        return read(taxonomy, themes, DesignExtensions.INSTANCE.concretes(loader));
    }

    /** The designs these concrete leaves make of the taxonomy's branches, checked in every theme given. */
    public List<DesignRead> read(Taxonomy taxonomy, Collection<Dressed> themes, Collection<? extends ConcreteComponent<?, ?, ?>> concretes) {
        var problems = new ArrayList<DesignProblem>();
        var has = new HashSet<>(taxonomy.components());

        // 1. each concrete leaf's leaf and design, read off its declaration
        var byDesign = new LinkedHashMap<Design<?, ?>, List<ConcreteComponent<?, ?, ?>>>();
        for (ConcreteComponent<?, ?, ?> c : concretes) {
            VirtualComponent<?> leaf;
            Design<?, ?> design;
            try {
                leaf = c.virtual();
                design = c.design();
            } catch (IllegalStateException e) {
                problems.add(new DesignProblem(Rule.UNREADABLE, e.getMessage()));
                continue;
            }
            if (!has.contains(leaf)) continue;   // another taxonomy's branch: not this one's to read
            if (!(c instanceof Drawn))
                problems.add(new DesignProblem(Rule.NOT_DRAWN, ResolveDrawing.name(c) + " says nothing of how it is drawn: it implements Drawn"));
            byDesign.computeIfAbsent(design, d -> new ArrayList<>()).add(c);
        }

        // 2. one design, one name
        var byName = new LinkedHashMap<String, List<Design<?, ?>>>();
        for (Design<?, ?> d : byDesign.keySet()) byName.computeIfAbsent(d.token(), n -> new ArrayList<>()).add(d);
        byName.forEach((name, same) -> {
            if (same.size() > 1)
                problems.add(new DesignProblem(Rule.NAME_TWICE, "'" + name + "' names " + same.size() + " designs: "
                        + String.join(" and ", same.stream().map(d -> d.getClass().getName()).toList())));
        });

        // 2b. each design in a scheme, as a style is (D33)
        for (Design<?, ?> d : byDesign.keySet())
            if (Schemes.INSTANCE.of(d).isEmpty())
                problems.add(new DesignProblem(Rule.NO_SCHEME, d.token() + " is in no scheme: its first type argument is an abstract class - "
                        + "the base, one on top of it, or one from scratch"));

        // 3. each design complete over its branch, and what each leaf owns
        var owned = new HashMap<VirtualComponent<?>, List<Target>>();
        var out = new ArrayList<DesignRead>();
        byDesign.forEach((design, mine) -> {
            ComponentBranch branch = mine.get(0).virtual().parent();   // one branch for all: the types hold it
            var byLeaf = new LinkedHashMap<VirtualComponent<?>, ConcreteComponent<?, ?, ?>>();
            for (ConcreteComponent<?, ?, ?> c : mine) {
                ConcreteComponent<?, ?, ?> before = byLeaf.putIfAbsent(c.virtual(), c);
                if (before != null)
                    problems.add(new DesignProblem(Rule.LEAF_TWICE, design.token() + " draws " + c.virtual().token() + " twice: "
                            + ResolveDrawing.name(before) + " and " + ResolveDrawing.name(c)));
            }
            var ordered = new ArrayList<ConcreteComponent<?, ?, ?>>();
            for (VirtualComponent<?> leaf : leaves(taxonomy, branch)) {
                owned.computeIfAbsent(leaf, l -> owns(l, problems));
                ConcreteComponent<?, ?, ?> c = byLeaf.get(leaf);
                if (c == null)
                    problems.add(new DesignProblem(Rule.LEAF_MISSING, design.token() + " draws no " + leaf.token() + ", a leaf of "
                            + branch.token() + ": a design is complete over its branch"));
                else ordered.add(c);
            }
            out.add(new DesignRead(design, branch, ordered, taxonomy));
        });

        // 4. in every theme: every owned target drawn once, nothing else drawn, each redirect landing
        for (Dressed theme : themes)
            for (DesignRead d : out) {
                Class<?> designs = Schemes.INSTANCE.of(d.design()).orElse(null), wears = Schemes.INSTANCE.of(theme.style()).orElse(null);
                if (designs == null || wears == null) continue;   // refused above, or by the theme
                if (!Schemes.INSTANCE.sees(wears, designs)) {
                    problems.add(new DesignProblem(Rule.SCHEME_UNSEEN, d.token() + " is " + Schemes.INSTANCE.name(designs) + "'s, which " + name(theme)
                            + "'s scheme, " + Schemes.INSTANCE.name(wears) + ", does not see: a design is drawn only in a theme whose scheme sees its own"));
                    continue;
                }
                    for (ConcreteComponent<?, ?, ?> c : d.concretes()) {
                        if (!(c instanceof Drawn drawn)) continue;   // refused above
                        List<Target> mineOwned = owned.getOrDefault(c.virtual(), List.of());
                        List<Drawing<?>> drawings;
                        try {
                            drawings = drawn.drawings(theme);
                        } catch (RuntimeException e) {
                            problems.add(new DesignProblem(Rule.DRAWING_REFUSED, ResolveDrawing.name(c) + " in " + name(theme) + ": " + e.getMessage()));
                            continue;
                        }
                        var seen = new HashSet<Target>();
                        var mine = new HashMap<Target, Drawing<?>>();
                        for (Drawing<?> dr : drawings) {
                            mine.putIfAbsent(dr.target(), dr);
                            if (!mineOwned.contains(dr.target()))
                                problems.add(new DesignProblem(Rule.NOT_OWNED, ResolveDrawing.name(c) + " in " + name(theme) + " draws "
                                        + dr.target().token() + ", which " + c.virtual().token() + " does not own"));
                            else if (!seen.add(dr.target()))
                                problems.add(new DesignProblem(Rule.DRAWN_TWICE, ResolveDrawing.name(c) + " in " + name(theme) + " draws "
                                        + dr.target().token() + " twice"));
                        }
                        for (Target t : mineOwned) {
                            if (!seen.contains(t)) {
                                problems.add(new DesignProblem(Rule.UNDRAWN, ResolveDrawing.name(c) + " in " + name(theme) + " leaves "
                                        + t.token() + " undrawn: draw it, say none, or take another's"));
                                continue;
                            }
                            ResolveDrawing.Resolved drawing;
                            if (mine.get(t) instanceof Drawing.Of<?> of) {
                                try {
                                    drawing = ResolveDrawing.INSTANCE.taken(taxonomy, of, theme, ResolveDrawing.name(c), c);
                                } catch (IllegalArgumentException e) {
                                    problems.add(new DesignProblem(Rule.OF_ASTRAY, "in " + name(theme) + ": " + e.getMessage()));
                                    continue;
                                }
                            } else {
                                try {
                                    drawing = ResolveDrawing.INSTANCE.resolve(taxonomy, c, t, theme);
                                } catch (IllegalArgumentException e) {
                                    problems.add(new DesignProblem(Rule.AS_ASTRAY, "in " + name(theme) + ": " + e.getMessage()));
                                    continue;
                                }
                            }
                            if (drawing.effect() != null) takesOwned(c, drawing.effect(), mineOwned, theme, problems);
                        }
                    }
            }

        if (!problems.isEmpty()) throw new RefusedDesigns(problems);
        return List.copyOf(out);
    }

    /** An effect takes what it needs: a property another target the leaf owns writes is refused, by name. */
    private static void takesOwned(ConcreteComponent<?, ?, ?> c, AnimationEffect effect, List<Target> owned, Dressed theme, List<DesignProblem> problems) {
        var takes = Effects.INSTANCE.takes(effect);
        for (Target other : owned) {
            if (other instanceof Target.Animation) continue;
            for (String p : other.properties())
                if (takes.contains(p))
                    problems.add(new DesignProblem(Rule.EFFECT_TAKES_OWNED, ResolveDrawing.name(c) + " in " + name(theme) + " wears "
                            + effect.token() + ", which takes " + p + " - and " + c.virtual().token() + " owns " + other.token()
                            + ", which writes it: what an effect takes, nothing else there may write"));
        }
    }

    /** The leaves of a design branch, in the taxonomy's order. */
    static List<VirtualComponent<?>> leaves(Taxonomy taxonomy, ComponentBranch branch) {
        var out = new ArrayList<VirtualComponent<?>>();
        for (var n : taxonomy.children(branch)) if (n instanceof VirtualComponent<?> v) out.add(v);
        return List.copyOf(out);
    }

    /** The targets a virtual leaf owns, or none: what a design's concrete leaves of it answer. */
    static List<Target> owned(VirtualComponent<?> leaf) {
        return leaf instanceof Owns o && o.owns() != null ? List.copyOf(o.owns()) : List.of();
    }

    private static List<Target> owns(VirtualComponent<?> leaf, List<DesignProblem> problems) {
        List<Target> targets = owned(leaf);
        if (targets.isEmpty()) {
            problems.add(new DesignProblem(Rule.OWNS_BADLY, leaf.token() + " owns no target: a virtual leaf says what its concrete leaves answer, with Owns"));
            return targets;
        }
        var seen = new HashSet<Target>();
        for (Target t : targets) {
            if (!seen.add(t)) problems.add(new DesignProblem(Rule.OWNS_BADLY, leaf.token() + " owns " + t.token() + " twice"));
            if (t.carrier() != Carrier.CSS)
                problems.add(new DesignProblem(Rule.OWNS_BADLY, leaf.token() + " owns " + t.token() + ", which no sheet carries"));
        }
        return targets;
    }

    private static String name(Dressed theme) { return theme.toString(); }
}
