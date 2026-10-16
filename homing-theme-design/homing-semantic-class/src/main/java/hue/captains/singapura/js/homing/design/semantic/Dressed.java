package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorScheme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A theme as resolution walks it: a style worn in a palette of its scheme, over a colour tree (RFC 0067, appendix
 * Semantic Colours and Flavours) - derived, never declared. Every target is the style's: its answers are read with its
 * base chain, a layer each, the worn style first, and the palette says only what each palettable colour the style names
 * is. The scheme is the type argument both share, so a style and a palette of different schemes do not compile together;
 * where the two are chosen at run time, {@link Wear} pairs them, and refuses what the compiler would.
 *
 * <p>Refused as it is built: a style or a palette in no scheme, a palette that is no chain down the schemes, a base
 * chain that comes back on itself, a class answered twice by one style, and a {@link Answer.FromBase} where there is
 * no base.</p>
 */
public final class Dressed {

    private final ColorTree colors;
    private final DesignStyle<?> style;
    private final ColorPalette<?> palette;
    private final Layer layer;

    /** A style worn in a palette of its scheme. */
    public <S extends ColorScheme> Dressed(ColorTree colors, DesignStyle<S> style, ColorPalette<S> palette) {
        this(colors, style, palette, true);
    }

    /** A style and a palette whose schemes {@link Wear} has found the same. */
    Dressed(ColorTree colors, DesignStyle<?> style, ColorPalette<?> palette, boolean paired) {
        this.colors = Objects.requireNonNull(colors, "Dressed.colors");
        this.style = Objects.requireNonNull(style, "Dressed.style");
        this.palette = Objects.requireNonNull(palette, "Dressed.palette");
        if (Schemes.INSTANCE.of(style).isEmpty())
            throw new IllegalArgumentException(style.token() + " says no scheme: a style says the scheme it requires");
        colors.requireChain(palette);
        this.layer = layerOf(style, new LinkedHashSet<>());
    }

    public ColorTree colors() { return colors; }
    public DesignStyle<?> style() { return style; }
    public ColorPalette<?> palette() { return palette; }

    /** The worn style's layer, over its base chain: what every target is asked of. */
    Layer layer() { return layer; }

    /** Every layer the theme answers from: the worn style down its base chain. */
    List<Layer> layers() { return layer.chain(); }

    /** The style's tokens, the worn one first, down its base chain. */
    public List<String> chain() { return layer.chain().stream().map(Layer::token).toList(); }

    /**
     * What the theme answers that the taxonomy never asks: a node it has not, or a casting no walk
     * passes through - a component in a role no slot casts it in. Each would answer nothing.
     */
    public List<String> check(Taxonomy taxonomy) {
        var asked = new HashSet<Casting>();
        for (Casting c : taxonomy.castings()) asked.addAll(c.lineage(taxonomy));
        var out = new ArrayList<String>();
        var tokens = SemanticTokens.INSTANCE;
        for (Layer l : layer.chain())
            for (Layer.Key k : l.answers().keySet()) {
                Casting c = k.node();
                ComponentNode n = c.component();
                if (!(n instanceof Root) && !taxonomy.nodes().contains(n))
                    out.add(l.token() + ": " + tokens.casting(c) + " " + k.target().token() + " - the taxonomy has no " + n.token());
                else if (!asked.contains(c))
                    out.add(l.token() + ": " + tokens.casting(c) + " " + k.target().token() + " - no casting the taxonomy has walks through it");
            }
        return List.copyOf(out);
    }

    private static Layer layerOf(DesignStyle<?> style, Set<Class<?>> seen) {
        if (!seen.add(style.getClass()))
            throw new IllegalArgumentException("the base chain of " + seen.iterator().next().getSimpleName() + " comes back to " + style.token());
        Layer base = null;
        if (style instanceof Derives<?, ?> d) {
            DesignStyle<?> b = Objects.requireNonNull(d.base(), style.token() + ".base()");
            Optional<ColorScheme> mine = Schemes.INSTANCE.of(style), its = Schemes.INSTANCE.of(b);
            if (its.isEmpty() || !Schemes.INSTANCE.sees(mine.orElseThrow(), its.get()))
                throw new IllegalArgumentException(style.token() + " is written over " + b.token() + ", whose scheme it cannot see");
            base = layerOf(b, seen);
        }
        return Layer.index(style, base);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Dressed d && d.colors == colors && d.style.equals(style) && d.palette.equals(palette);
    }
    @Override public int hashCode() { return Objects.hash(style, palette); }

    /** {@code sketchy in harbour}. */
    @Override public String toString() { return style.token() + " in " + palette.token(); }
}
