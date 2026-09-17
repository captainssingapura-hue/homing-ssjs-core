package hue.captains.singapura.js.homing.theme.type;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteClass;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;

import java.util.List;
import java.util.Set;

/**
 * RFC 0066 — the global type palette as a node in the CSS dependency graph:
 * one group, one {@link PaletteClass} declaring {@link HomingFonts}, and a
 * {@link Provision} per theme binding the three stacks.
 *
 * <p>A prior, like the colour palette: every component sets type in the same
 * semantic faces, so every class leans on this node by definition and the
 * server writes it into every served subgraph. The registry's priors are
 * exactly its palettes; a theme is complete for a deployment when it provides
 * every palette the deployment reaches.</p>
 */
public record GlobalTypePalette() implements CssGroup<GlobalTypePalette> {

    public static final GlobalTypePalette INSTANCE = new GlobalTypePalette();

    @Override public boolean prior() { return true; }

    /** The one node: {@code :root}, provided per theme. */
    public record global_type_palette() implements PaletteClass<GlobalTypePalette> {
        @Override public Set<CssVar> declares() { return HomingFonts.ALL; }
    }

    /** A theme's stacks for the three faces. */
    public interface Provision<TH extends Theme> extends PaletteProvision<GlobalTypePalette, TH> {
        @Override default GlobalTypePalette group() { return INSTANCE; }
    }

    @Override
    public List<CssClass<GlobalTypePalette>> cssClasses() {
        return List.of(new global_type_palette());
    }
}
