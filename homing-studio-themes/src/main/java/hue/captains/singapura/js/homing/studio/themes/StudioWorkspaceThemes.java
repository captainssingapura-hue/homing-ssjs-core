package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * The studio's registry for a deployment with the workspace mounted. Once a
 * theme's word on the workspace was a second file of overrides that this
 * registry appended; now a design binds the classes the workspace's elements
 * wear, and there is nothing to append. Kept as the name the starter and the
 * downstream studios registered; it is {@link StudioThemeRegistry}.
 */
public final class StudioWorkspaceThemes implements ThemeRegistry {

    public static final StudioWorkspaceThemes INSTANCE = new StudioWorkspaceThemes();

    private StudioWorkspaceThemes() {}

    @Override public List<Theme> themes() { return StudioThemeRegistry.INSTANCE.themes(); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return StudioThemeRegistry.INSTANCE.palettes(); }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return StudioThemeRegistry.INSTANCE.overrides(); }
    @Override public List<CssRenderer> renderers(hue.captains.singapura.js.homing.server.ServedModules served) { return StudioThemeRegistry.INSTANCE.renderers(served); }
}
