package hue.captains.singapura.js.homing.designs;

import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.Palette;
import hue.captains.singapura.js.homing.design.server.DesignRegistry;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * The framework's designs, as a site hands them to its MPA:
 * {@code StandardMpa.of(brand, HomingDesigns.REGISTRY, crates…)}.
 *
 * <p>Eight bases — {@link HomingEditorial}, the house design, first and so the
 * default, and seven written over it — and two palettes written as colours
 * alone, offered to every base beside the designs' own: every look wearable by
 * slug. A design is a function over the design classes a component wears, so
 * a site whose components wear design words needs nothing else.</p>
 *
 * <p>Nothing legacy rides here: no global palette provision ({@code --color-*},
 * the legacy fonts), no per-class override. Those are the studio's, for its
 * groups not yet on design classes, and the studio's registry adds them on top
 * of these same lists.</p>
 */
public final class HomingDesigns {

    /** The bases, the house design first: it is the default. */
    public static final List<Design> DESIGNS = List.of(
            HomingEditorial.INSTANCE, HomingFlatMorphism.INSTANCE, HomingNeoBrutalism.INSTANCE,
            HomingNeoFuturism.INSTANCE, HomingNeumorphism.INSTANCE, HomingGlassmorphism.INSTANCE,
            HomingRetroFuturism.INSTANCE, HomingSketchy.INSTANCE);

    /** Palettes written as colours alone, offered to every base. */
    public static final List<Palette> COLOURS = List.of(SeedPalette.FOREST, SeedPalette.SUNSET);

    /** The designs and the colours, and nothing else. */
    public static final ThemeRegistry REGISTRY = new DesignRegistry(DESIGNS, COLOURS, List.of(), List.of(), List.of());

    private HomingDesigns() {}
}
