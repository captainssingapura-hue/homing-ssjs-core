package hue.captains.singapura.js.homing.design;

import java.util.Set;

/**
 * A design on the colour plane only: its word for every pair whose target is
 * under {@link Target.Color}, and {@code null} for every other. Composed
 * under any physique it colours that physique — {@link Composed} — so a
 * deployment with three designs and five palettes offers fifteen looks, and
 * a palette is written once for all of them.
 *
 * <p>Written once, but not for all of them equally. A palette says which
 * design it was crafted for — its {@link #anchor}, the design it is the
 * default colours of — and which others it suits well enough to be offered
 * for, {@link #compatible}. Both by {@link DesignId}: a palette names designs
 * without holding them. Every cross stays resolvable — a page may wear any
 * pair by slug — but a picker offers a base only the colours that
 * {@link #fits} it. The judgement is the palette author's, not the
 * framework's: the deployment checks that a cross is complete, never that it
 * is handsome.</p>
 *
 * <p>A whole design is also a palette, seen through
 * {@link Composed#paletteOf}; its own colours are anchored to it and suit
 * nothing else by declaration. A palette written as one — a set of seeds and
 * a rule over the semantic branches — has no physique of its own and is
 * never listed as a theme, only as colours a theme may be worn in.</p>
 */
public interface Palette extends Design {

    /** The design this palette was crafted for: the one it is the default colours of. */
    DesignId anchor();

    /** The other designs it suits well enough to be offered for. Default: none. */
    default Set<DesignId> compatible() { return Set.of(); }

    /** Whether a picker offers this palette for a design: its anchor, or one it names compatible. */
    default boolean fits(Design d) { return anchor().equals(d.id()) || compatible().contains(d.id()); }

    @Override default String group() { return "Colours"; }
}
