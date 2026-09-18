package hue.captains.singapura.js.homing.design;

/**
 * A design on the colour plane only: its word for every pair whose target is
 * under {@link Target.Color}, and {@code null} for every other. Composed
 * under any physique it colours that physique — {@link Composed} — so a
 * deployment with three designs and five palettes offers fifteen looks, and
 * a palette is written once for all of them.
 *
 * <p>A whole design is also a palette, seen through
 * {@link Composed#paletteOf}; a palette written as one — a set of seeds and
 * a rule over the semantic branches — has no physique of its own and is
 * never listed as a theme, only as colours a theme may be worn in.</p>
 */
public interface Palette extends Design {

    @Override default String group() { return "Colours"; }
}
