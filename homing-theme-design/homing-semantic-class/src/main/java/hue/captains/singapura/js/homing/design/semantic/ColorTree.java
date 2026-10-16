package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A scheme's colour tree as read (RFC 0067, appendix Semantic Colours and Flavours): the normalized tree its reader
 * overlays, the base's layer first and its own last, each node with its scheme and its parent. A palettable colour is
 * resolved by the selected palette, then down the palettes it sits on, the first answer winning. Made by
 * {@link ReadColors}, which refuses a tree that cannot be read.
 */
public final class ColorTree {

    /** A node as read: its scheme, and its parent - none at the top. */
    record Read(ColorNode<?, ?, ?> node, Class<?> scheme, ColorNode<?, ?, ?> parent) {}

    private final Map<Class<?>, Read> read;

    ColorTree(Map<Class<?>, Read> read) { this.read = Collections.unmodifiableMap(new LinkedHashMap<>(read)); }

    /** Every node, the base's layer first, each layer in the order its reader lists it. */
    public List<ColorNode<?, ?, ?>> nodes() { return read.values().stream().<ColorNode<?, ?, ?>>map(Read::node).toList(); }

    /** Whether the tree has this node. */
    public boolean has(ColorNode<?, ?, ?> node) { return read.containsKey(node.getClass()); }

    /** The scheme a node is in. */
    public Class<?> scheme(ColorNode<?, ?, ?> node) { return of(node).scheme(); }

    /** A node's parent; none for a branch at the top. */
    public Optional<ColorNode<?, ?, ?>> parent(ColorNode<?, ?, ?> node) { return Optional.ofNullable(of(node).parent()); }

    /** The scheme a style requires; none when its type argument is no scheme. */
    public Optional<Class<?>> scheme(DesignStyle<?> style) { return Schemes.INSTANCE.of(style); }

    /** Whether a scheme sees a node: one of its own scheme's, or of a scheme below it. */
    public boolean sees(Class<?> scheme, ColorNode<?, ?, ?> node) { return Schemes.INSTANCE.sees(scheme, of(node).scheme()); }

    /** A scheme as messages say it: {@code the base}, or its name. */
    public String schemeName(Class<?> scheme) { return Schemes.INSTANCE.name(scheme); }


    /** The concrete colour a palettable colour is under a palette, or one below it; none when nothing answers it. */
    public Optional<ConcreteColor> resolve(ColorPalette<?> selected, Palettable<?, ?> color) {
        return resolution(selected, color).map(Resolved::color);
    }

    /**
     * A palettable colour resolved, and who gave it: the selected palette itself, or a palette below it.
     *
     * @param color the concrete colour
     * @param from  who gave it, as a sheet says it - {@code paper, below neon} - empty when the selected palette gave it itself
     */
    public record Resolved(ConcreteColor color, String from) {}

    /** A palettable colour resolved down the palettes, saying who gave it; none when nothing answers it. */
    public Optional<Resolved> resolution(ColorPalette<?> selected, Palettable<?, ?> color) {
        of(color);   // refused when the tree has it not
        Optional<ConcreteColor> found = selected.color(color);
        if (found.isPresent()) return Optional.of(new Resolved(found.get(), ""));
        var passed = new ArrayList<ColorPalette<?>>(List.of(selected));
        for (Optional<? extends ColorPalette<?>> p = selected.below(); p.isPresent(); p = p.get().below()) {
            if (passed.contains(p.get())) throw new IllegalArgumentException(selected.token() + "'s chain comes back to " + p.get().token());
            passed.add(p.get());
            found = p.get().color(color);
            if (found.isPresent()) return Optional.of(new Resolved(found.get(), p.get().token() + ", below " + selected.token()));
        }
        return Optional.empty();
    }

    /** The palettable colours a palette's scheme sees - its own scheme's and every scheme's below it - in the tree's order. */
    public List<Palettable<?, ?>> palettables(ColorPalette<?> palette) {
        Class<?> scheme = Schemes.INSTANCE.of(palette).orElseThrow(() -> new IllegalArgumentException(palette.token() + " implements no scheme"));
        var out = new ArrayList<Palettable<?, ?>>();
        for (Read r : read.values())
            if (r.node() instanceof Palettable<?, ?> p && Schemes.INSTANCE.sees(scheme, r.scheme())) out.add(p);
        return List.copyOf(out);
    }

    /**
     * What is wrong with a palette over this tree: a palette in no scheme, one sitting on a palette not of the scheme
     * below its own - or on nothing when its scheme is on top of another - and every palettable colour its scheme can
     * see that nothing answers. None when it is whole.
     */
    public List<String> check(ColorPalette<?> palette) {
        var out = new ArrayList<String>(chainProblems(palette));
        if (!out.isEmpty()) return List.copyOf(out);
        Class<?> scheme = Schemes.INSTANCE.of(palette).orElseThrow();
        for (Read r : read.values())
            if (r.node() instanceof Palettable<?, ?> p && Schemes.INSTANCE.sees(scheme, r.scheme()) && resolve(palette, p).isEmpty())
                out.add(palette.token() + " leaves " + p.token() + " unanswered");
        return List.copyOf(out);
    }

    /** A palette's chain, refused when it is no chain down the schemes. */
    void requireChain(ColorPalette<?> palette) {
        List<String> problems = chainProblems(palette);
        if (!problems.isEmpty()) throw new IllegalArgumentException(String.join("; ", problems));
    }

    private static List<String> chainProblems(ColorPalette<?> palette) {
        var schemes = Schemes.INSTANCE;
        var out = new ArrayList<String>();
        ColorPalette<?> next = palette;
        while (true) {
            ColorPalette<?> at = next;
            Optional<Class<?>> scheme = schemes.of(at);
            if (scheme.isEmpty()) {
                out.add(at.token() + " implements no scheme: its type argument is an abstract class, a scheme from scratch or one on top of another");
                return out;
            }
            Optional<Class<?>> belowScheme = schemes.below(scheme.get());
            Optional<? extends ColorPalette<?>> below = at.below();
            if (below.isEmpty()) {
                belowScheme.ifPresent(b -> out.add(at.token() + " sits on nothing, and " + schemes.name(scheme.get()) + " is on top of "
                        + schemes.name(b) + ": it sits on a palette of " + schemes.name(b)));
                return out;
            }
            Optional<Class<?>> under = schemes.of(below.get());
            if (belowScheme.isEmpty() || under.isEmpty() || under.get() != belowScheme.get()) {
                out.add(at.token() + " sits on " + below.get().token() + ", " + under.map(u -> "of " + schemes.name(u)).orElse("of no scheme") + ": "
                        + belowScheme.map(b -> "a palette of " + schemes.name(scheme.get()) + " sits on one of " + schemes.name(b))
                                     .orElse(schemes.name(scheme.get()) + " is on top of no scheme, so its palettes sit on nothing"));
                return out;
            }
            next = below.get();
        }
    }

    private Read of(ColorNode<?, ?, ?> node) {
        Read r = read.get(node.getClass());
        if (r == null) throw new IllegalArgumentException(node.token() + " is in no colour tree read: its scheme's layer, or one below it, lists it");
        return r;
    }
}
