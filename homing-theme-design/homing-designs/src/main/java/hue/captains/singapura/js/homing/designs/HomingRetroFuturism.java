package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Retro-futurism — the future as 1984 drew it. Everything is outlined in neon
 * and glows; a title is wide, upright, capitalised and haloed; a selected
 * thing burns brighter. {@link RetroFuturismDesign} is the neon,
 * {@link SynthwavePalette} which neon, and the sun and the grid behind it.
 */
public record HomingRetroFuturism() implements Design {

    public static final DesignId ID = new DesignId("retro-futurism");
    public static final HomingRetroFuturism INSTANCE = new HomingRetroFuturism();

    /** Over Default: the neon where it has one, Default's for the rest; Synthwave on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) return SynthwavePalette.INSTANCE.impl(pair);
        Impl own = RetroFuturismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in Synthwave by default; a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SynthwavePalette.INSTANCE; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Retro-Futurism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "A magenta sun over a cyan grid — everything outlined in neon and glowing."; }
}
