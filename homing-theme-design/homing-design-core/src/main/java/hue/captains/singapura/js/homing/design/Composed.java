package hue.captains.singapura.js.homing.design;

/**
 * A design composed of two on orthogonal planes: a <b>physique</b> — shape,
 * type, motion, depth, everything off the colour branch — and a
 * <b>palette</b> — the {@link Target.Color} branch and nothing else. The
 * sealed tree makes the cut exact: a pair is on one plane or the other by
 * its target, so the composed function is a switch with no overlap and no
 * gap, and any physique takes any palette.
 *
 * <p>The one place the planes touch is a physique word that needs a colour —
 * a shadow is "the ink, offset", a glow "the primary surface, blurred". A
 * physique says so by reference, {@link DesignClass#var()}, never by value;
 * the browser reads the palette's root binding, mode included, and the
 * deployment refuses a reference to a pair nobody required.</p>
 *
 * <p>Either side may be a whole design: {@link #physiqueOf} and
 * {@link #paletteOf} are the views that answer only their plane, so three
 * designs are already three physiques and three palettes.</p>
 *
 * @param physique the design answering every pair off the colour plane
 * @param palette  the palette answering every pair on it — also what {@link #palette()} names, as it should
 */
public record Composed(Design physique, Palette palette) implements Design {

    public static Composed of(Design physique, Design palette) { return new Composed(physiqueOf(physique), paletteOf(palette)); }

    /** The physique's own slug when the palette is its own — the whole design, not a cross. */
    public boolean isDiagonal() { return paletteOf(physique).id().equals(palette.id()); }

    @Override public Impl impl(DesignClass<?> pair) {
        return pair.onColourPlane() ? palette.impl(pair) : physique.impl(pair);
    }

    @Override public DesignId id()  { return DesignId.cross(physique.id(), palette.id()); }
    @Override public String label() { return physique.label() + " in " + palette.label(); }
    @Override public String group() { return "Composed"; }
    @Override public String inspiration() { return physique.label() + "'s physique under " + palette.label() + "'s colours."; }

    /** The design restricted to the physique plane: its word off the colour branch, none on it. */
    public static Design physiqueOf(Design d) {
        if (d instanceof Plane p) return p;
        if (d instanceof Colours c) return physiqueOf(c.whole);
        return new Plane(d);
    }

    /** The design restricted to the colour plane: its word on the colour branch, none off it. A palette already, as itself. */
    public static Palette paletteOf(Design d) {
        if (d instanceof Palette p) return p;
        if (d instanceof Plane p) return paletteOf(p.whole);
        return d.palette();
    }

    /** The physique plane of a whole design. */
    record Plane(Design whole) implements Design {
        @Override public Impl impl(DesignClass<?> pair) { return pair.onColourPlane() ? null : whole.impl(pair); }
        @Override public DesignId id()  { return whole.id(); }
        @Override public String label() { return whole.label(); }
        @Override public String group() { return whole.group(); }
        @Override public String inspiration() { return whole.inspiration(); }
    }

    /** The colour plane of a whole design: the design's own colours, as a palette any physique may wear — anchored to it, suiting no other by declaration. */
    record Colours(Design whole) implements Palette {
        @Override public Impl impl(DesignClass<?> pair) { return pair.onColourPlane() ? whole.impl(pair) : null; }
        @Override public DesignId id()  { return whole.id(); }
        @Override public DesignId anchor() { return whole.id(); }
        @Override public String label() { return whole.label(); }
        @Override public String inspiration() { return whole.inspiration(); }
    }
}
