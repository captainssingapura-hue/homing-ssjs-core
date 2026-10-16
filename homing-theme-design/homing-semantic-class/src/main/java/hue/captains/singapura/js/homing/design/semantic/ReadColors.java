package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorBranch;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.semantic.ColorProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Reads a scheme's colour tree (RFC 0067, appendix Semantic Colours and Flavours): the normalized tree its reader
 * ({@link SchemeTreeReader}) overlays, layer by layer from the base up to its own, and refuses - with every problem it
 * found at once - what the compiler cannot:
 *
 * <ul>
 *   <li>a layer that is no scheme's, one whose scheme lives in another jar than its reader, one that adds no colour - a
 *       scheme without colours of its own means nothing - one that holds another scheme's, or one that sits on a layer
 *       other than its scheme's below, or on itself;</li>
 *   <li>a node that is not a stateless record, or whose scheme, parent or kind cannot be read off its declaration -
 *       one written raw, one in the marker {@code ColorScheme} itself;</li>
 *   <li>a node whose parent the tree has not, or one its scheme cannot see;</li>
 *   <li>two nodes with one name, or one node listed twice;</li>
 *   <li>a scale of fewer than three knots, its knots not strictly rising from -1 to 1, or a knot of a colour the tree has
 *       not; an interpolated colour at a degree outside -1 to 1, or of a scale the tree has not; a fixed colour that gives
 *       none.</li>
 * </ul>
 *
 * <p>What the types already make impossible it never checks: a node of two kinds, under two parents, at two levels; a
 * chain of parents that comes back; a knot that is interpolated or a scale; a node hung under a parent its scheme
 * cannot see.</p>
 */
public record ReadColors() implements StatelessFunctionalObject {

    public static final ReadColors INSTANCE = new ReadColors();

    /** A scheme's normalized tree: its reader's layers, the base first, each its scheme's alone. */
    public ColorTree read(SchemeTreeReader<?> reader) {
        var problems = new ArrayList<ColorProblem>();
        var schemes = Schemes.INSTANCE;
        var layers = new ArrayList<SchemeTreeReader<?>>();
        var passed = new HashSet<Class<?>>();
        for (SchemeTreeReader<?> at = reader; at != null; at = at.below().orElse(null)) {
            if (!passed.add(at.getClass())) {
                problems.add(new ColorProblem(Rule.LAYER_ASTRAY, at.getClass().getSimpleName() + " sits, in the end, on itself"));
                break;
            }
            layers.add(0, at);
        }
        var nodes = new ArrayList<ColorNode<?, ?, ?>>();
        Class<?> under = null;
        for (SchemeTreeReader<?> layer : layers) {
            String who = layer.getClass().getSimpleName();
            Optional<Class<?>> scheme = schemes.of(layer);
            if (scheme.isEmpty()) {
                problems.add(new ColorProblem(Rule.LAYER_ASTRAY, who + " reads no scheme: its type argument is an abstract class, "
                        + "a scheme from scratch or one on top of another - never ColorScheme itself, nor a variable"));
                return refuse(problems);
            }
            Optional<Class<?>> below = schemes.below(scheme.get());
            if (!Objects.equals(below.orElse(null), under))
                problems.add(new ColorProblem(Rule.LAYER_ASTRAY, who + " reads " + schemes.name(scheme.get()) + ", which is "
                        + below.map(b -> "on top of " + schemes.name(b)).orElse("from scratch") + ", and sits on "
                        + (under == null ? "nothing" : schemes.name(under) + "'s layer")));
            if (!Objects.equals(home(layer.getClass()), home(scheme.get())))
                problems.add(new ColorProblem(Rule.LAYER_ELSEWHERE, who + " reads " + schemes.name(scheme.get()) + ", which lives in another jar: "
                        + "a scheme and its colours live in one jar"));
            List<? extends ColorNode<?, ?, ?>> own = layer.layer();
            if (own == null || own.isEmpty())
                problems.add(new ColorProblem(Rule.LAYER_EMPTY, who + " adds no colour to " + schemes.name(scheme.get())
                        + ": a scheme without colours of its own means nothing"));
            else for (ColorNode<?, ?, ?> n : own) {
                Optional<Class<?>> its = schemes.of(n);
                if (its.isPresent() && its.get() != scheme.get())
                    problems.add(new ColorProblem(Rule.LAYER_ASTRAY, n.token() + " is " + schemes.name(its.get()) + "'s, in "
                            + schemes.name(scheme.get()) + "'s layer: a layer holds its scheme's colours alone"));
                nodes.add(n);
            }
            under = scheme.get();
        }
        return read(nodes, problems);
    }

    /** The tree of these nodes, as one layer: for what reads a tree's nodes alone. */
    ColorTree read(Collection<? extends ColorNode<?, ?, ?>> nodes) {
        return read(nodes, new ArrayList<>());
    }

    private ColorTree read(Collection<? extends ColorNode<?, ?, ?>> nodes, List<ColorProblem> problems) {
        var schemes = Schemes.INSTANCE;

        // 1. every node listed once, each name once
        var listed = new ArrayList<ColorNode<?, ?, ?>>();
        var byName = new LinkedHashMap<String, ColorNode<?, ?, ?>>();
        for (ColorNode<?, ?, ?> n : nodes) {
            ColorNode<?, ?, ?> before = byName.putIfAbsent(n.token(), n);
            if (before != null) {
                problems.add(new ColorProblem(Rule.NAME_TWICE, before.getClass() == n.getClass()
                        ? n.token() + " is listed twice"
                        : "'" + n.token() + "' names " + before.getClass().getName() + " and " + n.getClass().getName()));
                continue;
            }
            listed.add(n);
        }

        // 2. each node a stateless record, in a scheme, under a parent the tree has and its scheme can see
        var byClass = new LinkedHashMap<Class<?>, ColorNode<?, ?, ?>>();
        for (ColorNode<?, ?, ?> n : listed) byClass.put(n.getClass(), n);
        var read = new LinkedHashMap<Class<?>, ColorTree.Read>();
        listed.forEach(n -> {
            Class<?> c = n.getClass();
            if (!c.isRecord() || c.getRecordComponents().length > 0)
                problems.add(new ColorProblem(Rule.NOT_STATELESS, n.token() + " is " + (c.isRecord() ? "a record with components" : "no record")
                        + ": a node is a stateless record, its type its meaning"));
            Optional<Class<?>> scheme = schemes.of(n);
            if (scheme.isEmpty()) {
                problems.add(new ColorProblem(Rule.SCHEME_ASTRAY, n.token() + " is in no scheme: its scheme is an abstract class, a scheme from scratch"
                        + " or one on top of another - never ColorScheme itself, nor a variable"));
                return;
            }
            Class<?> parentClass = TypeArguments.INSTANCE.at(c, ColorNode.class, 1);
            if (parentClass == null) {
                problems.add(new ColorProblem(Rule.UNREADABLE, n.token() + "'s parent cannot be read off its declaration: it is written raw"));
                return;
            }
            ColorNode<?, ?, ?> parent = null;
            if (parentClass != ColorBranch.Top.class) {
                parent = byClass.get(parentClass);
                if (parent == null) {
                    problems.add(new ColorProblem(Rule.PARENT_MISSING, n.token() + " hangs under " + parentClass.getSimpleName()
                            + ", which the tree has not: its layer, or one below it, lists it"));
                    return;
                }
                Optional<Class<?>> parentScheme = schemes.of(parent);
                if (parentScheme.isPresent() && !schemes.sees(scheme.get(), parentScheme.get()))
                    problems.add(new ColorProblem(Rule.SCHEME_ASTRAY, n.token() + ", in " + schemes.name(scheme.get()) + ", hangs under "
                            + parent.token() + ", in " + schemes.name(parentScheme.get()) + ", which it cannot see"));
            }
            read.put(c, new ColorTree.Read(n, scheme.get(), parent));
        });

        // 3. each leaf whole: a scale's knots, an interpolated colour's scale and degree, a fixed colour's colour
        for (ColorTree.Read r : read.values()) {
            switch (r.node()) {
                case ColorScale<?, ?> s -> scale(s, r, read, problems);
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
                default -> { }
            }
        }


        if (!problems.isEmpty()) throw new RefusedColors(problems);
        return new ColorTree(read);
    }

    private static ColorTree refuse(List<ColorProblem> problems) { throw new RefusedColors(problems); }

    /** Where a class was loaded from: its jar, or its classes' directory. */
    private static String home(Class<?> c) {
        CodeSource source = c.getProtectionDomain() == null ? null : c.getProtectionDomain().getCodeSource();
        return source == null || source.getLocation() == null ? null : source.getLocation().toString();
    }

    private static void scale(ColorScale<?, ?> s, ColorTree.Read r, Map<Class<?>, ColorTree.Read> read, List<ColorProblem> problems) {
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
