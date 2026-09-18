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
 * @param palette  the design answering every pair on it
 */
public record Composed(Design physique, Design palette) implements Design {

    public static Composed of(Design physique, Design palette) { return new Composed(physiqueOf(physique), paletteOf(palette)); }

    @Override public Impl impl(DesignClass<?> pair) {
        return pair.onColourPlane() ? palette.impl(pair) : physique.impl(pair);
    }

    @Override public String slug()  { return physique.slug() + "_" + palette.slug(); }
    @Override public String label() { return physique.label() + " in " + palette.label(); }
    @Override public String group() { return "Composed"; }
    @Override public String inspiration() { return physique.label() + "'s physique under " + palette.label() + "'s colours."; }

    /** The design restricted to the physique plane: its word off the colour branch, none on it. */
    public static Design physiqueOf(Design d) {
        if (d instanceof Plane p) return p.colour ? physiqueOf(p.whole) : p;
        return new Plane(d, false);
    }

    /** The design restricted to the colour plane: its word on the colour branch, none off it. */
    public static Design paletteOf(Design d) {
        if (d instanceof Plane p) return p.colour ? p : paletteOf(p.whole);
        return new Plane(d, true);
    }

    /** One plane of a whole design. */
    record Plane(Design whole, boolean colour) implements Design {
        @Override public Impl impl(DesignClass<?> pair) { return pair.onColourPlane() == colour ? whole.impl(pair) : null; }
        @Override public String slug()  { return whole.slug(); }
        @Override public String label() { return whole.label(); }
        @Override public String group() { return whole.group(); }
        @Override public String inspiration() { return whole.inspiration(); }
    }
}
