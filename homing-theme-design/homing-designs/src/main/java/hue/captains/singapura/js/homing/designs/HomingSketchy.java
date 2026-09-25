package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Sketchy — drawn by hand, in marker, on paper: a 2px line around every box,
 * corners that wobble, handwriting for the body and a sketched capital for
 * headings, and nothing casting a shadow. {@link SketchyDesign} is the hand,
 * {@link SeedPalette#MARKER} the marker it draws with by default. After
 * Bootswatch's Sketchy.
 */
public record HomingSketchy() implements Design {

    public static final DesignId ID = new DesignId("sketchy");
    public static final HomingSketchy INSTANCE = new HomingSketchy();

    /** Over Default: the hand where it has one, Default's for the rest; the marker on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return SeedPalette.MARKER.impl(pair);
        Impl own = SketchyDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in the marker by default; the marker is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.MARKER; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Sketchy"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Drawn by hand in marker on paper — 2px lines, corners that wobble, nothing casts a shadow."; }
}
