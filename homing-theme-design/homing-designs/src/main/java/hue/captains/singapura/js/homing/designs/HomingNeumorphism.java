package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Neumorphism — one material, moulded. A raised thing is pressed out of the
 * page, a selected one pressed into it: two shadows, the surface's own tone
 * turned toward the light and away from it, and no line drawn anywhere.
 * Every corner is rounded; the type is round and bold.
 *
 * <p>The fourth design, and the first written as a physique and a palette
 * from the start: {@link NeumorphismDesign} moulds whatever surface a palette
 * provides, and {@link SeedPalette#CLAY} is the classic grey-blue clay it is
 * worn in by default.</p>
 */
public record HomingNeumorphism() implements Design {

    public static final DesignId ID = new DesignId("neumorphism");
    public static final HomingNeumorphism INSTANCE = new HomingNeumorphism();

    /** Over Default: the mould where it has one, Default's for the rest; the clay on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return SeedPalette.CLAY.impl(pair);
        Impl own = NeumorphismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in the clay by default; the clay is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.CLAY; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Neumorphism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "One material, moulded — pressed out of the page or into it, never drawn on."; }
}
