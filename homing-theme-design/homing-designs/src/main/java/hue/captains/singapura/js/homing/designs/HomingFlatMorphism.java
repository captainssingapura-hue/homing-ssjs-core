package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Flat-morphism — flat sheets, stacked. Every surface is a cut sheet of card
 * lying on a ground, and the only thing that says how high it lies is the
 * hard offset it casts: no blur, one direction for the whole page. Flat
 * design's honesty about surfaces, with one mechanic for depth rather than
 * none.
 *
 * <p>{@link FlatMorphismDesign} is the mechanic — the unit, the cut corner,
 * the two registers answered in two planes — and {@link SeedPalette#CARDSTOCK}
 * is the paper it is cut from, with the cast colours that a hard shadow has
 * to be given rather than compute.</p>
 */
public record HomingFlatMorphism() implements Design {

    public static final DesignId ID = new DesignId("flat-morphism");
    public static final HomingFlatMorphism INSTANCE = new HomingFlatMorphism();

    /** Over Default: the sheet where it has one, Default's for the rest; cardstock on the colour plane. */
    @Override public Impl impl(DesignClass<?> pair) {
        if (pair.onColourPlane()) {
            Impl own = FlatMorphismDesign.WORDS.get(pair);
            return own != null ? own : SeedPalette.CARDSTOCK.impl(pair);
        }
        Impl own = FlatMorphismDesign.WORDS.get(pair);
        return own != null ? own : HomingEditorial.INSTANCE.impl(pair);
    }

    /** Worn in cardstock by default; the paper is a palette in its own name, so any physique may wear it. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.CARDSTOCK; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Flat-Morphism"; }
    @Override public String group() { return "Neutral"; }
    @Override public String inspiration() { return "Cut sheets of card on a table — one lamp, hard offsets, no blur anywhere."; }
}
