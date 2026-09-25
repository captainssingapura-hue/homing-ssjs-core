package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Neo-Futurism — cool glass over deep space: near-white plates in light,
 * near-black in dark, every edge a filament of one electric cyan, the
 * primary action a solid bar of it. Violet is the second voice, magenta the
 * alarm. A wide geometric display set light and tracked; a slow decelerating
 * ease that glows rather than snaps.
 *
 * <p>The third design, and the first written on the design classes from the
 * start: no override ever existed for it, so it is exactly the sum of its
 * words in {@link NeoFuturismDesign} over {@link HomingEditorial}'s.</p>
 *
 * <p>Dark mode is the native register — the light mode is the same design
 * seen in daylight, the glow kept but dimmed to what a white page can carry.
 * The masthead stays space in both; only the filament beneath it divides it
 * from the page.</p>
 */
public record HomingNeoFuturism() implements Design {

    public static final DesignId ID = new DesignId("neo-futurism");
    public static final HomingNeoFuturism INSTANCE = new HomingNeoFuturism();

    /** Over Default: its own word where it has one, Default's for the rest — the base is a plain call. */
    @Override public Impl impl(DesignClass<?> pair) {
        Impl own = NeoFuturismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Neo-Futurism"; }
    @Override public String group() { return "Expressive"; }
    @Override public String inspiration() { return "Cool glass over deep space — one electric cyan, every edge a filament."; }
}
