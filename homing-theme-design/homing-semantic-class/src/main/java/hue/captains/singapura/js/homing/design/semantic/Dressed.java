package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A theme as resolution walks it: a style worn in a palette (RFC 0067) - derived, never declared.
 * Colour is the palette's, every other target the style's: the style's answers are read with its
 * base chain, a layer each, the worn style first. Refused as it is built: a style that answers
 * colour, a base chain that comes back on itself, a class answered twice by one identity, and a
 * {@link Answer.FromBase} where there is no base.
 */
public final class Dressed {

    private final DesignStyle style;
    private final ColorPalette palette;
    private final Layer styleLayer;
    private final Layer paletteLayer;

    public Dressed(DesignStyle style, ColorPalette palette) {
        this.style = Objects.requireNonNull(style, "Dressed.style");
        this.palette = Objects.requireNonNull(palette, "Dressed.palette");
        this.styleLayer = layerOf(style, new LinkedHashSet<>());
        this.paletteLayer = Layer.index(palette.token(), palette.implementations(), null);
    }

    public DesignStyle style() { return style; }
    public ColorPalette palette() { return palette; }

    /** The layer a target is answered in: the palette's for colour, the worn style's for the rest. */
    Layer layerFor(Target target) { return target instanceof Target.Color ? paletteLayer : styleLayer; }

    /** Every layer the theme answers from: the worn style down its base chain, then the palette. */
    List<Layer> layers() {
        var out = new ArrayList<Layer>(styleLayer.chain());
        out.add(paletteLayer);
        return List.copyOf(out);
    }

    /** The style's tokens, the worn one first, down its base chain. */
    public List<String> chain() { return styleLayer.chain().stream().map(Layer::token).toList(); }

    /**
     * What the theme answers that the taxonomy never asks: a node it has not, or a casting no walk
     * passes through - a component in a role no slot casts it in. Each would answer nothing.
     */
    public List<String> check(Taxonomy taxonomy) {
        var asked = new java.util.HashSet<Casting>();
        for (Casting c : taxonomy.castings()) asked.addAll(c.lineage(taxonomy));
        var out = new ArrayList<String>();
        var tokens = SemanticTokens.INSTANCE;
        for (Layer layer : List.of(styleLayer, paletteLayer))
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

    private static Layer layerOf(DesignStyle style, Set<Class<?>> seen) {
        if (!seen.add(style.getClass()))
            throw new IllegalArgumentException("the base chain of " + seen.iterator().next().getSimpleName() + " comes back to " + style.token());
        Layer base = null;
        if (style instanceof Derives<?> d) base = layerOf(Objects.requireNonNull(d.base(), style.token() + ".base()"), seen);
        for (Implementation<?> i : style.implementations())
            if (i.target() instanceof Target.Color)
                throw new IllegalArgumentException(style.token() + ": " + SemanticTokens.INSTANCE.casting(i.node()) + " " + i.target().token()
                        + " - a style never answers colour: that is its palette's");
        return Layer.index(style.token(), style.implementations(), base);
    }

    @Override public boolean equals(Object o) { return o instanceof Dressed d && d.style.equals(style) && d.palette.equals(palette); }
    @Override public int hashCode() { return Objects.hash(style, palette); }

    /** {@code sketchy in harbour}. */
    @Override public String toString() { return style.token() + " in " + palette.token(); }
}
