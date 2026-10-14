package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorBranch;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Ink;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads a colour tree from the nodes its owner declares and the nodes each extension grafts (RFC 0067, appendix
 * Semantic Colours and Flavours), and refuses - with every problem it found at once - what the compiler cannot:
 *
 * <ul>
 *   <li>a node that is not a stateless record, or whose scheme, parent or kind cannot be read off its declaration -
 *       one written raw, one in the marker {@code ColorScheme} itself;</li>
 *   <li>a node whose parent the tree has not, or one its scheme cannot see;</li>
 *   <li>two nodes with one name, or one node listed twice;</li>
 *   <li>a scale of fewer than three knots, its knots not strictly rising from -1 to 1, or a knot of a colour the tree has
 *       not; an interpolated colour at a degree outside -1 to 1, or of a scale the tree has not; a fixed colour that gives
 *       none;</li>
 *   <li>an extension grafting palettable colours whose base sub-palette is missing, or leaves one unanswered.</li>
 * </ul>
 *
 * <p>What the types already make impossible it never checks: a node of two kinds, under two parents, at two levels; a
 * chain of parents that comes back; a knot that is interpolated or a scale.</p>
 */
public record ReadColors() implements StatelessFunctionalObject {

    public static final ReadColors INSTANCE = new ReadColors();

    /** The tree of a source: its own nodes, then those of every extension on the classpath grafting onto it. */
    public ColorTree read(Class<? extends ColorSource> source, Collection<? extends ColorNode<?, ?, ?>> own) {
        return read(own, ColorExtensions.INSTANCE.of(source));
    }

    /** The tree of these nodes and these extensions' grafts. */
    public ColorTree read(Collection<? extends ColorNode<?, ?, ?>> own, Collection<? extends ColorExtension> extensions) {
        var problems = new ArrayList<ColorProblem>();
        var schemes = Schemes.INSTANCE;

        // 1. every node listed, by whom: the owner's, then each extension's
        record Listing(ColorExtension by, Collection<? extends ColorNode<?, ?, ?>> nodes) {}
        var lists = new ArrayList<Listing>(List.of(new Listing(null, own)));
        for (ColorExtension e : extensions) lists.add(new Listing(e, e.nodes()));
        var listed = new LinkedHashMap<ColorNode<?, ?, ?>, ColorExtension>();
        var byName = new LinkedHashMap<String, ColorNode<?, ?, ?>>();
        for (Listing list : lists)
            for (ColorNode<?, ?, ?> n : list.nodes()) {
                ColorNode<?, ?, ?> before = byName.putIfAbsent(n.token(), n);
                if (before != null) {
                    problems.add(new ColorProblem(Rule.NAME_TWICE, before.getClass() == n.getClass()
                            ? n.token() + " is listed twice" + by(listed.get(before)) + " and" + by(list.by())
                            : "'" + n.token() + "' names " + before.getClass().getName() + " and " + n.getClass().getName()));
                    continue;
                }
                listed.put(n, list.by());
            }

        // 2. each node a stateless record, in a scheme, under a parent the tree has and its scheme can see
        var byClass = new LinkedHashMap<Class<?>, ColorNode<?, ?, ?>>();
        for (ColorNode<?, ?, ?> n : listed.keySet()) byClass.put(n.getClass(), n);
        var read = new LinkedHashMap<Class<?>, ColorTree.Read>();
        listed.forEach((n, extension) -> {
            Class<?> c = n.getClass();
            if (!c.isRecord() || c.getRecordComponents().length > 0)
                problems.add(new ColorProblem(Rule.NOT_STATELESS, n.token() + " is " + (c.isRecord() ? "a record with components" : "no record")
                        + ": a node is a stateless record, its type its meaning"));
            Optional<Class<?>> scheme = schemes.of(n);
            if (scheme.isEmpty()) {
                problems.add(new ColorProblem(Rule.SCHEME_ASTRAY, n.token() + " is in no scheme: its scheme is an abstract class, the base,"
                        + " one on top of it, or one from scratch - never ColorScheme itself, nor a variable"));
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
                            + ", which the tree has not: its owner or an extension lists it"));
                    return;
                }
                Optional<Class<?>> parentScheme = schemes.of(parent);
                if (parentScheme.isPresent() && !schemes.sees(scheme.get(), parentScheme.get()))
                    problems.add(new ColorProblem(Rule.SCHEME_ASTRAY, n.token() + ", in " + schemes.name(scheme.get()) + ", hangs under "
                            + parent.token() + ", in " + schemes.name(parentScheme.get()) + ", which it cannot see"));
            }
            read.put(c, new ColorTree.Read(n, scheme.get(), parent, extension));
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
                case Ink<?, ?> k -> {
                    double share = k.share();
                    if (!(share > 0 && share <= 1))
                        problems.add(new ColorProblem(Rule.INK_BADLY, k.token() + " takes " + share + " of the ink: a share is more than none and at most the ink whole, 1"));
                }
                case Fixed<?, ?> f -> {
                    ConcreteColor color;
                    try { color = f.color(); } catch (RuntimeException e) { color = null; }
                    if (color == null) problems.add(new ColorProblem(Rule.FIXED_BADLY, f.token() + " gives no colour: a fixed colour is given at its declaration"));
                }
                default -> { }
            }
        }

        // 4. each extension's last word, on every palettable colour it grafts
        var graftedBy = new LinkedHashMap<ColorExtension, List<Palettable<?, ?>>>();
        for (ColorTree.Read r : read.values())
            if (r.extension() != null && r.node() instanceof Palettable<?, ?> p) graftedBy.computeIfAbsent(r.extension(), e -> new ArrayList<>()).add(p);
        graftedBy.forEach((e, mine) -> {
            Optional<SubPalette> last = e.baseSubPalette();
            String who = e.getClass().getSimpleName();
            if (last.isEmpty()) {
                problems.add(new ColorProblem(Rule.LAST_WORD_MISSING, who + " grafts " + mine.size() + " palettable "
                        + (mine.size() == 1 ? "colour" : "colours") + " and has no base sub-palette: its last word answers every one"));
                return;
            }
            for (Palettable<?, ?> p : mine)
                if (last.get().color(p).isEmpty())
                    problems.add(new ColorProblem(Rule.LAST_WORD_MISSING, who + "'s base sub-palette leaves " + p.token() + " unanswered"));
        });

        if (!problems.isEmpty()) throw new RefusedColors(problems);
        return new ColorTree(read);
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

    private static String by(ColorExtension e) { return e == null ? " by the tree's owner" : " by " + e.getClass().getSimpleName(); }
}
