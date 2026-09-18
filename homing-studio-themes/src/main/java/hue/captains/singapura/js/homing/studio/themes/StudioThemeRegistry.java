package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.server.DesignRegistry;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * The studio's three designs — {@link HomingDefault}, {@link HomingNeoBrutalism} and {@link HomingNeoFuturism}
 * over it — and two palettes written as colours alone, {@link SeedPalette#FOREST} and
 * {@link SeedPalette#SUNSET}, as the studio's theme registry: three bases, five colours, fifteen looks.
 * A design is a function over the design classes the studio's components wear;
 * Default is the house word, the other two call it for whatever they have no
 * word of their own for; a palette answers the colour plane alone.
 *
 * <p>Until the last group has moved, the legacy palettes and overrides pass
 * through beside the designs: a group not yet on design classes still reads
 * the palette's tokens and still takes an override.</p>
 */
public final class StudioThemeRegistry implements ThemeRegistry {

    public static final StudioThemeRegistry INSTANCE = new StudioThemeRegistry();

    private static final DesignRegistry DESIGNS = new DesignRegistry(
            List.of(HomingDefault.INSTANCE, HomingNeoBrutalism.INSTANCE, HomingNeoFuturism.INSTANCE),
            List.of(SeedPalette.FOREST, SeedPalette.SUNSET),
            List.of(),
            List.of(HomingDefault.Palette.INSTANCE, HomingNeoBrutalism.Palette.INSTANCE, HomingNeoFuturism.Palette.INSTANCE,
                    HomingDefault.Fonts.INSTANCE, HomingNeoBrutalism.Fonts.INSTANCE, HomingNeoFuturism.Fonts.INSTANCE),
            List.of());

    private StudioThemeRegistry() {}

    @Override public List<Theme> themes()  { return DESIGNS.themes(); }
    @Override public List<Theme> bases()   { return DESIGNS.bases(); }
    @Override public List<Theme> colours() { return DESIGNS.colours(); }
    @Override public Theme dressed(Theme base, Theme colours) { return DESIGNS.dressed(base, colours); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return DESIGNS.palettes(); }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return DESIGNS.overrides(); }
    @Override public List<CssRenderer> renderers(hue.captains.singapura.js.homing.server.ServedModules served) { return DESIGNS.renderers(served); }
}
