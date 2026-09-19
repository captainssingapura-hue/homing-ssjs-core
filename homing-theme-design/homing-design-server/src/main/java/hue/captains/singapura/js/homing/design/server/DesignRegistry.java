package hue.captains.singapura.js.homing.design.server;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.design.Composed;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignExtension;
import hue.captains.singapura.js.homing.design.Palette;
import hue.captains.singapura.js.homing.server.CssRenderer;
import hue.captains.singapura.js.homing.server.ServedModules;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ThemeRegistry} whose themes are designs on two orthogonal planes.
 * Lists the designs — each a base worn in its own colours — and the colours
 * a base may be worn in instead: every design's own, and any {@link Palette}
 * written as colours alone. {@link #themes()} is the closure — the bases and
 * every cross — so the page's {@code ?theme=} and the renderer resolve any
 * slug a picker can compose; {@link #bases()} and {@link #colours()} are the
 * two axes a picker shows, {@link #dressed} joins them, and {@link #fits} says
 * which joins a picker offers — the palette's own declaration of what it was
 * crafted for and what else it suits.
 *
 * <p>Given what the deployment serves, hands the server the one renderer that
 * serves the target groups, cut to the pairs the served components wear.</p>
 *
 * <p>During the migration a deployment still carries palette provisions and
 * overrides for the groups not yet moved onto design classes; they pass
 * through here unchanged and retire with the last such group. A cross has no
 * provision, so such a group is unpainted under it until then.</p>
 */
public final class DesignRegistry implements ThemeRegistry {

    private final List<Design> designs;
    private final List<Palette> colours;
    private final List<DesignExtension> extensions;
    private final List<PaletteProvision<?, ?>> palettes;
    private final List<CssGroupImpl<?, ?>> overrides;
    /** Every wearable theme by slug: the designs, then each design under each other colour. */
    private final Map<String, Design> bySlug;

    public DesignRegistry(List<Design> designs, List<DesignExtension> extensions) {
        this(designs, List.of(), extensions, List.of(), List.of());
    }

    public DesignRegistry(List<Design> designs, List<DesignExtension> extensions,
                          List<PaletteProvision<?, ?>> palettes, List<CssGroupImpl<?, ?>> overrides) {
        this(designs, List.of(), extensions, palettes, overrides);
    }

    /**
     * @param designs the bases, each worn in its own colours by default; the first is the deployment's default
     * @param colours palettes written as colours alone — offered to every base beside the designs' own
     */
    public DesignRegistry(List<Design> designs, List<Palette> colours, List<DesignExtension> extensions,
                          List<PaletteProvision<?, ?>> palettes, List<CssGroupImpl<?, ?>> overrides) {
        if (designs.isEmpty()) throw new IllegalArgumentException("a design registry lists at least one design");
        this.designs = List.copyOf(designs);
        this.extensions = List.copyOf(extensions);
        this.palettes = List.copyOf(palettes);
        this.overrides = List.copyOf(overrides);
        var own = new ArrayList<Palette>();
        for (Design d : designs) own.add(Composed.paletteOf(d));
        own.addAll(colours);
        this.colours = List.copyOf(own);
        var all = new LinkedHashMap<String, Design>();
        for (Design d : designs) all.put(d.slug(), d);
        for (Design d : designs)
            for (Palette p : this.colours) {
                var cross = Composed.of(d, p);
                if (!cross.isDiagonal()) all.putIfAbsent(cross.slug(), cross);
            }
        this.bySlug = java.util.Collections.unmodifiableMap(all);   // in order: the default first, for the renderer's fallback
    }

    public List<Design> designs() { return designs; }
    public List<DesignExtension> extensions() { return extensions; }

    @Override public List<Theme> themes()  { return List.copyOf(bySlug.values()); }
    @Override public List<Theme> bases()   { return List.copyOf(designs); }
    @Override public List<Theme> colours() { return List.copyOf(colours); }
    @Override public Theme dressed(Theme base, Theme colours) {
        if (!(base instanceof Design d) || !(colours instanceof Design p)) return base;
        var cross = Composed.of(d, p);
        return cross.isDiagonal() ? base : bySlug.getOrDefault(cross.slug(), cross);
    }

    /** A base's own colours always; another palette when it says it fits the base. A theme that is not a design is offered everything. */
    @Override public boolean fits(Theme base, Theme colours) {
        if (!(base instanceof Design d) || !(colours instanceof Palette p)) return true;
        return Composed.paletteOf(d).id().equals(p.id()) || p.fits(d);
    }
    @Override public List<PaletteProvision<?, ?>> palettes() { return palettes; }
    @Override public List<CssGroupImpl<?, ?>> overrides() { return overrides; }

    @Override
    public List<CssRenderer> renderers(ServedModules served) {
        var groups = new ArrayList<CssGroup<?>>();
        for (var m : served.byName().values()) if (m instanceof CssGroup<?> g) groups.add(g);
        return List.of(new DesignCssRenderer(List.copyOf(bySlug.values()), extensions, Deployment.wornBy(groups), Deployment.scaledBy(groups)));
    }
}
