package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Composed;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.server.DesignRegistry;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * The studio's three designs — {@link HomingDefault}, {@link HomingNeoBrutalism} and {@link HomingNeoFuturism}
 * over it — as the studio's theme registry. The nine other themes are retired
 * with the palette-and-override contract they were written in; a design is a
 * function over the design classes the studio's components wear; Default is the
 * house word, the other two call it for whatever they have no word of their own for.
 *
 * <p>Until the last group has moved, the legacy palettes and overrides pass
 * through beside the designs: a group not yet on design classes still reads
 * the palette's tokens and still takes an override.</p>
 */
public final class StudioThemeRegistry implements ThemeRegistry {

    public static final StudioThemeRegistry INSTANCE = new StudioThemeRegistry();

    private static final DesignRegistry DESIGNS = new DesignRegistry(
            withCompositions(List.of(HomingDefault.INSTANCE, HomingNeoBrutalism.INSTANCE, HomingNeoFuturism.INSTANCE)),
            List.of(),
            List.of(HomingDefault.Palette.INSTANCE, HomingNeoBrutalism.Palette.INSTANCE, HomingNeoFuturism.Palette.INSTANCE,
                    HomingDefault.Fonts.INSTANCE, HomingNeoBrutalism.Fonts.INSTANCE, HomingNeoFuturism.Fonts.INSTANCE),
            List.of());

    /**
     * The three, then every cross of one physique with another palette — the
     * planes are orthogonal, so any physique takes any palette; the diagonal
     * is the three themselves and is not repeated.
     */
    static List<Design> withCompositions(List<Design> whole) {
        var out = new java.util.ArrayList<Design>(whole);
        for (Design physique : whole)
            for (Design palette : whole)
                if (physique != palette) out.add(Composed.of(physique, palette));
        return List.copyOf(out);
    }

    private StudioThemeRegistry() {}

    @Override public List<Theme> themes() { return DESIGNS.themes(); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return DESIGNS.palettes(); }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return DESIGNS.overrides(); }
    @Override public List<CssRenderer> renderers(hue.captains.singapura.js.homing.server.ServedModules served) { return DESIGNS.renderers(served); }
}
