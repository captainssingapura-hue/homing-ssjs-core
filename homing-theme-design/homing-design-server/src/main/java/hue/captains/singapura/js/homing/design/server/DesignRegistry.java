package hue.captains.singapura.js.homing.design.server;

import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignExtension;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.List;

/**
 * A {@link ThemeRegistry} whose themes are designs. Lists the designs (a
 * {@link Design} is a {@link Theme}, so the page's {@code ?theme=} and the
 * picker see them as before), the extensions, and hands the server the one
 * renderer that serves the target groups.
 *
 * <p>During the migration a deployment still carries palettes and overrides
 * for the groups not yet moved onto design classes; they pass through here
 * unchanged and retire with the last such group.</p>
 */
public final class DesignRegistry implements ThemeRegistry {

    private final List<Design> designs;
    private final List<DesignExtension> extensions;
    private final List<PaletteProvision<?, ?>> palettes;
    private final List<CssGroupImpl<?, ?>> overrides;
    private final DesignCssRenderer renderer;

    public DesignRegistry(List<Design> designs, List<DesignExtension> extensions) {
        this(designs, extensions, List.of(), List.of());
    }

    public DesignRegistry(List<Design> designs, List<DesignExtension> extensions,
                          List<PaletteProvision<?, ?>> palettes, List<CssGroupImpl<?, ?>> overrides) {
        if (designs.isEmpty()) throw new IllegalArgumentException("a design registry lists at least one design");
        this.designs = List.copyOf(designs);
        this.extensions = List.copyOf(extensions);
        this.palettes = List.copyOf(palettes);
        this.overrides = List.copyOf(overrides);
        this.renderer = new DesignCssRenderer(this.designs, this.extensions);
    }

    public List<Design> designs() { return designs; }
    public List<DesignExtension> extensions() { return extensions; }

    @Override public List<Theme> themes() { return List.copyOf(designs); }
    @Override public List<PaletteProvision<?, ?>> palettes() { return palettes; }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return overrides; }
    @Override public List<CssRenderer> renderers() { return List.of(renderer); }
}
