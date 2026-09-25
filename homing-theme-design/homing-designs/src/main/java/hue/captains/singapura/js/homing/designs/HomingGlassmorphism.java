package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Glassmorphism — plates of frosted glass over an aurora. Every raised plate
 * and the masthead blur what is behind them, rimmed by a hairline of light,
 * on a soft wide shadow, corners well rounded; a selected thing is a clearer
 * pane lit along its top edge. {@link GlassmorphismDesign} is the frosting,
 * {@link FrostPalette} what the glass is made of and what shows through it.
 */
public record HomingGlassmorphism() implements Design {

    public static final DesignId ID = new DesignId("glassmorphism");
    public static final HomingGlassmorphism INSTANCE = new HomingGlassmorphism();

    /** Over Default: the frosting where it has one, Default's for the rest; Frost on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return FrostPalette.INSTANCE.impl(pair);
        Impl own = GlassmorphismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in Frost by default; Frost is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return FrostPalette.INSTANCE; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Glassmorphism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Plates of frosted glass over an aurora — blurred, rimmed in light, lifted on soft shadow."; }
}
