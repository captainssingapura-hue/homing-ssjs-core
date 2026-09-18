package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.server.DesignRegistry;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * The studio's two designs — {@link HomingDefault} and {@link HomingNeoBrutalism}
 * over it — as the studio's theme registry. The nine other themes are retired
 * with the palette-and-override contract they were written in; a design is a
 * set of providers over the design classes the studio's components wear, and
 * the two that remain are being rewritten onto that, group by group.
 *
 * <p>Until the last group has moved, the legacy palettes and overrides pass
 * through beside the designs: a group not yet on design classes still reads
 * the palette's tokens and still takes an override.</p>
 */
public final class StudioThemeRegistry implements ThemeRegistry {

    public static final StudioThemeRegistry INSTANCE = new StudioThemeRegistry();

    private static final DesignRegistry DESIGNS = new DesignRegistry(
            List.of(HomingDefault.INSTANCE, HomingNeoBrutalism.INSTANCE),
            List.of(),
            List.of(HomingDefault.Palette.INSTANCE, HomingNeoBrutalism.Palette.INSTANCE,
                    HomingDefault.Fonts.INSTANCE, HomingNeoBrutalism.Fonts.INSTANCE),
            List.of());

    private StudioThemeRegistry() {}

    @Override public List<Theme> themes() { return DESIGNS.themes(); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return DESIGNS.palettes(); }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return DESIGNS.overrides(); }
    @Override public List<CssRenderer> renderers(hue.captains.singapura.js.homing.server.ServedModules served) { return DESIGNS.renderers(served); }
}
