package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorBranch;
import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.semantic.ColorProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads a scheme's colour tree (RFC 0067, appendix Semantic Colours and Flavours, D49): the layers of its declarations,
 * the base's first and its own last, each the leaves its declaration lists and the branches above them - each branch
 * coming in with the first leaf under it, as a component's branch comes in with it. Nothing is read off a type at run
 * time: a node says its parent, a declaration its scheme, a scheme the one below it. It refuses, with every problem it
 * found at once:
 *
 * <ul>
 *   <li>layers that are not their schemes' own chain - a first layer of a scheme on top of another, a layer whose scheme
 *       is not on top of the one below it, a scheme read twice - or a layer that adds no colour: a scheme without
 *       colours of its own means nothing;</li>
 *   <li>a node that gives no parent; two nodes with one name, or one node listed twice;</li>
 *   <li>a scale of fewer than three knots, its knots not strictly rising from -1 to 1, or a knot of a colour the tree has
 *       not; an interpolated colour at a degree outside -1 to 1, or of a scale the tree has not; a fixed colour that gives
 *       none.</li>
 * </ul>
 *
 * <p>What only a node's declaration could tell - a node that is no stateless record, a layer in another jar than its
 * scheme, a branch of a scheme other than the layer it came in with - is checked when its jar is built
 * ({@link CheckScheme}), never at run time. What the types already make impossible it never checks: a node of two
 * kinds, under two parents, at two levels; a chain of parents that comes back; a knot that is interpolated or a scale;
 * a layer holding another scheme's leaves; a node hung under a parent its scheme cannot see.</p>
 */
public record ReadColors() implements StatelessFunctionalObject {

    public static final ReadColors INSTANCE = new ReadColors();

    /** A scheme's normalized tree: its layers' declarations, the base's first, each its scheme's alone. */
    public ColorTree read(List<? extends SchemeDeclaration<?>> layers) {
        var problems = new ArrayList<ColorProblem>();
        var read = new LinkedHashMap<Class<?>, ColorTree.Read>();
        var byName = new LinkedHashMap<String, ColorNode<?, ?, ?>>();
        if (layers.isEmpty()) problems.add(new ColorProblem(Rule.LAYER_EMPTY, "no layer is read: a tree is its scheme's layers"));
        var passed = new HashSet<Class<?>>();
        ColorScheme under = null;
        for (SchemeDeclaration<?> layer : layers) {
            String who = layer.getClass().getSimpleName();
            ColorScheme scheme = layer.scheme();
            if (scheme == null) {
                problems.add(new ColorProblem(Rule.LAYER_ASTRAY, who + " declares no scheme"));
                throw new RefusedColors(problems);
            }
            if (!passed.add(scheme.getClass())) {
                problems.add(new ColorProblem(Rule.LAYER_ASTRAY, who + " declares " + scheme.name() + ", whose layer is read already: "
                        + "a scheme's layer is read once"));
                break;
            }
            Optional<ColorScheme> below = scheme.below();
            boolean sits = below.isEmpty() ? under == null : under != null && Schemes.INSTANCE.same(below.get(), under);
            if (!sits)
                problems.add(new ColorProblem(Rule.LAYER_ASTRAY, who + " declares " + scheme.name() + ", which is "
                        + below.map(b -> "on top of " + b.name()).orElse("from scratch") + ", and sits on "
                        + (under == null ? "nothing" : under.name() + "'s layer")));
            List<? extends ColorLeaf<?, ?, ?>> leaves = layer.leaves();
            if (leaves == null || leaves.isEmpty())
                problems.add(new ColorProblem(Rule.LAYER_EMPTY, who + " adds no colour to " + scheme.name()
                        + ": a scheme without colours of its own means nothing"));
            else for (ColorLeaf<?, ?, ?> leaf : leaves) add(leaf, scheme, read, byName, problems);
            under = scheme;
        }
        return whole(read, problems);
    }

    /** The tree of these nodes, as one layer of a scheme: for what reads a tree's nodes alone. */
    ColorTree read(ColorScheme scheme, Collection<? extends ColorNode<?, ?, ?>> nodes) {
        var problems = new ArrayList<ColorProblem>();
        var read = new LinkedHashMap<Class<?>, ColorTree.Read>();
        var byName = new LinkedHashMap<String, ColorNode<?, ?, ?>>();
        for (ColorNode<?, ?, ?> n : nodes) add(n, scheme, read, byName, problems);
        return whole(read, problems);
    }

    /**
     * A node into the tree, in a layer's scheme: the branches above it first, from the top down, each the tree has not
     * yet coming in with it, each under the parent it says; a node listed twice, or a name naming two, refused.
     */
    private static void add(ColorNode<?, ?, ?> node, ColorScheme scheme, Map<Class<?>, ColorTree.Read> read,
                            Map<String, ColorNode<?, ?, ?>> byName, List<ColorProblem> problems) {
        var chain = new ArrayList<ColorNode<?, ?, ?>>();
        for (ColorNode<?, ?, ?> at = node; at != null; ) {
            chain.add(0, at);
            Object parent = at.parent();
            if (parent == null) {
                problems.add(new ColorProblem(Rule.UNREADABLE, at.token() + " gives no parent: a node says the branch it hangs under"));
                return;
            }
            at = parent instanceof ColorNode<?, ?, ?> p ? p : null;   // the top: no parent
        }
        ColorNode<?, ?, ?> parent = null;
        for (int i = 0; i < chain.size(); i++) {
            ColorNode<?, ?, ?> n = chain.get(i);
            ColorTree.Read before = read.get(n.getClass());
            if (before != null) {
                if (i == chain.size() - 1) problems.add(new ColorProblem(Rule.NAME_TWICE, n.token() + " is listed twice"));
                parent = before.node();
                continue;
            }
            ColorNode<?, ?, ?> named = byName.putIfAbsent(n.token(), n);
            if (named != null) {
                problems.add(new ColorProblem(Rule.NAME_TWICE, "'" + n.token() + "' names " + named.getClass().getName() + " and " + n.getClass().getName()));
                return;
            }
            read.put(n.getClass(), new ColorTree.Read(n, scheme, parent));
            parent = n;
        }
    }

    /** Each leaf whole - a scale's knots, an interpolated colour's scale and degree, a share, a fixed colour's colour - or refused. */
    private static ColorTree whole(Map<Class<?>, ColorTree.Read> read, List<ColorProblem> problems) {
        for (ColorTree.Read r : read.values()) {
            switch (r.node()) {
                case ColorScale<?, ?> s -> scale(s, read, problems);
                case Interpolated<?, ?> i -> {
                    double d = i.degree();
                    if (!(d >= -1 && d <= 1))
                        problems.add(new ColorProblem(Rule.INTERPOLATED_BADLY, i.token() + " is at " + d + ": a degree is from -1 to 1"));
                    ColorScale<?, ?> s = i.scale();
                    if (s == null || !read.containsKey(s.getClass()))
                        problems.add(new ColorProblem(Rule.INTERPOLATED_BADLY, i.token() + " is drawn from " + (s == null ? "no scale" : s.token() + ", which the tree has not")));
                }
                case CurrentColor<?, ?> k -> {
                    double share = k.share();
                    if (!(share > 0 && share <= 1))
                        problems.add(new ColorProblem(Rule.SHARE_BADLY, k.token() + " takes " + share + " of the colour it is given: a share is more than none and at most the whole of it, 1"));
                }
                case Fixed<?, ?> f -> {
                    ConcreteColor color;
                    try { color = f.color(); } catch (RuntimeException e) { color = null; }
                    if (color == null) problems.add(new ColorProblem(Rule.FIXED_BADLY, f.token() + " gives no colour: a fixed colour is given at its declaration"));
                }
                case ColorBranch<?, ?> b -> { }
                default -> { }
            }
        }
        if (!problems.isEmpty()) throw new RefusedColors(problems);
        return new ColorTree(read);
    }

    private static void scale(ColorScale<?, ?> s, Map<Class<?>, ColorTree.Read> read, List<ColorProblem> problems) {
        List<? extends ColorScale.Knot<?>> knots;
        try { knots = s.knots(); } catch (RuntimeException e) {
            problems.add(new ColorProblem(Rule.SCALE_BADLY, s.token() + "'s knots cannot be made: " + e.getMessage()));
            return;
        }
        if (knots == null || knots.size() < 3) {
            problems.add(new ColorProblem(Rule.SCALE_BADLY, s.token() + " has " + (knots == null ? "no" : knots.size()) + " knots: a scale has three or more"));
            return;
        }
        if (knots.get(0).degree() != -1 || knots.get(knots.size() - 1).degree() != 1)
            problems.add(new ColorProblem(Rule.SCALE_BADLY, s.token() + "'s knots run from " + knots.get(0).degree() + " to "
                    + knots.get(knots.size() - 1).degree() + ": the first is at -1 and the last at 1, so every degree has a colour"));
        for (int i = 1; i < knots.size(); i++)
            if (!(knots.get(i).degree() > knots.get(i - 1).degree()))
                problems.add(new ColorProblem(Rule.SCALE_BADLY, s.token() + "'s knot at " + knots.get(i).degree() + " follows one at "
                        + knots.get(i - 1).degree() + ": its knots rise strictly"));
        for (ColorScale.Knot<?> k : knots)
            if (!read.containsKey(k.color().getClass()))
                problems.add(new ColorProblem(Rule.SCALE_BADLY, s.token() + " has a knot of " + k.color().token() + ", which the tree has not"));
    }
}
