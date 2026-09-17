package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0066 — the studio's themes with their word on the workspace added: the
 * same themes and palettes as {@link StudioThemeRegistry}, plus the impls that
 * override the workspace's classes. A theme's overrides for a product live in
 * a module that sees both the theme and the product; this registry is where
 * the studio+workspace bundle joins them, and the starter's fixtures serve it.
 */
public final class StudioWorkspaceThemes implements ThemeRegistry {

    public static final StudioWorkspaceThemes INSTANCE = new StudioWorkspaceThemes();

    private StudioWorkspaceThemes() {}

    @Override public List<Theme> themes() { return StudioThemeRegistry.INSTANCE.themes(); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return StudioThemeRegistry.INSTANCE.palettes(); }

    @Override public List<hue.captains.singapura.js.homing.server.CssRenderer> renderers() { return StudioThemeRegistry.INSTANCE.renderers(); }
    @Override public List<CssGroupImpl<?, ?>> overrides() {
        var all = new ArrayList<CssGroupImpl<?, ?>>(StudioThemeRegistry.INSTANCE.overrides());
        all.add(BrutalistWorkspace.Switcher.INSTANCE);
        all.add(BrutalistWorkspace.Graph.INSTANCE);
        all.add(BrutalistWorkspace.Monitor.INSTANCE);
        return List.copyOf(all);
    }
}
