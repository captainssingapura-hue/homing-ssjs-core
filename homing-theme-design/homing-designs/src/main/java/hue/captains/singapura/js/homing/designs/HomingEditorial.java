package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;


/**
 * Editorial — the house design, the default of the framework's designs
 * ({@link HomingDesigns}), and what works out of the box: a serif for the
 * headings, a grotesque for the body, hairline rules, corners just off
 * square, a whisper of shadow. Print-derived and quiet, which is what lets
 * every other design be written over it — {@link EditorialDesign} is the
 * physique they all fall back to for whatever they have no word of their own
 * for. Worn in {@link SeedPalette#HARBOUR} — navy and gold on a cool white
 * page — by default.
 *
 * <p>RFC 0066 — an identity record: its words are {@link EditorialDesign}'s,
 * its colours {@link SeedPalette#HARBOUR}'s. The legacy global-palette values
 * the studio's older groups still read are the studio's, kept beside its
 * registry ({@code HomingEditorialLegacy}, in {@code homing-studio-themes}).</p>
 */
public record HomingEditorial() implements Design {

    public static final DesignId ID = new DesignId("editorial");
    public static final HomingEditorial INSTANCE = new HomingEditorial();

    /** The design as a function: the house physique off the colour plane, the house seeds on it; none for what neither has. */
    @Override public Impl impl(DesignClass<?> pair) {
        return pair.onColourPlane() ? SeedPalette.HARBOUR.impl(pair) : EditorialDesign.WORDS.get(pair);
    }

    /** Worn in Harbour by default — the first seed palette, in its own name. */
    @Override public hue.captains.singapura.js.homing.design.Palette palette() { return SeedPalette.HARBOUR; }

    @Override public DesignId id() { return ID; }
    @Override public String label() { return "Editorial"; }
    @Override public String group() { return "Neutral"; }
    @Override public String inspiration() { return "The house design — serif headings, hairline rules, a whisper of shadow."; }
}
