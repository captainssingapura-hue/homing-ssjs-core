package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.server.DesignRegistry;
import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * The studio's theme registry: the framework's designs and colour palettes —
 * {@link HomingDesigns#DESIGNS} and {@link HomingDesigns#COLOURS}, from
 * {@code homing-designs} — every look wearable by slug, and on top of them the
 * legacy provisions only the studio still needs ({@code HomingEditorialLegacy}
 * and the rest, here). A site that wears design words alone takes
 * {@link HomingDesigns#REGISTRY} instead, and nothing of this.
 *
 * <p>Until the last group has moved, the legacy palettes and overrides pass
 * through beside the designs: a group not yet on design classes still reads
 * the palette's tokens and still takes an override.</p>
 */
public final class StudioThemeRegistry implements ThemeRegistry {

    public static final StudioThemeRegistry INSTANCE = new StudioThemeRegistry();

    // The framework's designs and colours (homing-designs), and on top of them
    // the legacy provisions the studio's groups still read.
    private static final DesignRegistry DESIGNS = new DesignRegistry(
            HomingDesigns.DESIGNS,
            HomingDesigns.COLOURS,
            List.of(),
            List.of(HomingEditorialLegacy.Palette.INSTANCE, HomingFlatMorphismLegacy.Palette.INSTANCE, HomingNeoBrutalismLegacy.Palette.INSTANCE, HomingNeoFuturismLegacy.Palette.INSTANCE, HomingNeumorphismLegacy.Palette.INSTANCE, HomingGlassmorphismLegacy.Palette.INSTANCE, HomingRetroFuturismLegacy.Palette.INSTANCE, HomingSketchyLegacy.Palette.INSTANCE,
                    HomingEditorialLegacy.Fonts.INSTANCE, HomingFlatMorphismLegacy.Fonts.INSTANCE, HomingNeoBrutalismLegacy.Fonts.INSTANCE, HomingNeoFuturismLegacy.Fonts.INSTANCE, HomingNeumorphismLegacy.Fonts.INSTANCE, HomingGlassmorphismLegacy.Fonts.INSTANCE, HomingRetroFuturismLegacy.Fonts.INSTANCE, HomingSketchyLegacy.Fonts.INSTANCE),
            List.of());

    private StudioThemeRegistry() {}

    @Override public List<Theme> themes()  { return DESIGNS.themes(); }
    @Override public List<Theme> bases()   { return DESIGNS.bases(); }
    @Override public List<Theme> colours() { return DESIGNS.colours(); }
    @Override public Theme dressed(Theme base, Theme colours) { return DESIGNS.dressed(base, colours); }
    @Override public boolean fits(Theme base, Theme colours) { return DESIGNS.fits(base, colours); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return DESIGNS.palettes(); }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return DESIGNS.overrides(); }
    @Override public List<CssRenderer> renderers(hue.captains.singapura.js.homing.server.ServedModules served) { return DESIGNS.renderers(served); }
}
