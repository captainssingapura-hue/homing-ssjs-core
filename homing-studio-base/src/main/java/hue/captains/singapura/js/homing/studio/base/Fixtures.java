package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import hue.captains.singapura.tao.ontology.Immutable;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

/**
 * RFC 0012 — the harness wrapping. Apps, actions, theme registry, default
 * theme, brand resolution, and node chrome that surround the studios. This
 * is the downstream extensibility seam: a custom harness implements its
 * own {@code Fixtures<S>} (or extends {@link DefaultFixtures}) to add apps,
 * change chrome, or override the brand.
 *
 * <p>Bound by {@code S} so harness apps can reference the studio set. The
 * Fixtures arrives at the {@link Bootstrap} already-initialised with
 * whatever it needs from downstream; the bootstrap reads it through the
 * interface.</p>
 *
 * @param <S> the studio type at the umbrella's leaves; usually {@code Studio<?>}
 */
public interface Fixtures<S extends Studio<?>> extends Immutable {

    /** The studio tree being served. */
    Umbrella<S> umbrella();

    /**
     * Apps the harness contributes on top of each studio's own apps. The
     * framework's default set — {@code CatalogueAppHost}, {@code PlanAppHost},
     * {@code DocReader}, {@code ThemesIntro} — ships with {@link DefaultFixtures}.
     */
    java.util.List<AppModule<?, ?>> harnessApps();

    /** Raw GET actions the harness contributes. Empty by default. */
    default Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() {
        return Map.of();
    }

    /**
     * RFC 0044 — the crate roots this studio serves. Their closure is what
     * {@code /module} and {@code /css-content} resolve a name against, by lookup
     * and nothing else: a module no crate declared cannot be served. The default
     * is what studio-base alone ships — its own crate and the two palettes it
     * leans on; a starter or a downstream studio overrides with its closure.
     */
    default java.util.List<hue.captains.singapura.js.homing.core.Crate> crates() {
        return java.util.List.of(StudioBaseCrate.INSTANCE,
                hue.captains.singapura.js.homing.theme.color.ThemeColorCrate.INSTANCE,
                hue.captains.singapura.js.homing.theme.type.ThemeTypeCrate.INSTANCE);
    }

    /** Raw POST actions the harness contributes. Empty by default. */
    default Map<String, PostAction<RoutingContext, ?, ?, ?>> harnessPostActions() {
        return Map.of();
    }
    /** RFC 0066 — the themes the harness installs. studio-base ships none: the
     *  eleven live one layer up in {@code homing-studio-themes} and the starter
     *  installs them; a bespoke Fixtures installs its own. Default: none. */
    default ThemeRegistry themeRegistry() { return ThemeRegistry.EMPTY; }

    /** The theme a request without {@code ?theme=} is served under: the first the
     *  registry lists, or none when the registry is empty. */
    default Theme defaultTheme() {
        var themes = themeRegistry().themes();
        return themes.isEmpty() ? null : themes.get(0);
    }

    /**
     * Brand to install. Default: the first studio's
     * {@link Studio#standaloneBrand()}. Override when a custom umbrella
     * wants its own brand (multi-studio deploys typically do).
     */
    default StudioBrand brand() {
        var studios = umbrella().studios();
        if (studios.isEmpty()) return null;
        return studios.get(0).standaloneBrand();
    }

    /**
     * Visual chrome for a node in the umbrella tree. Applied by the framework
     * when rendering breadcrumbs / TOCs / landing tiles. Default in
     * {@link DefaultFixtures} switches on {@code Solo} vs {@code Group}.
     */
    NodeChrome chromeFor(Umbrella<S> node);

    /** Display chrome for one Umbrella node — a badge label and a glyph icon. */
    record NodeChrome(String badge, String icon) {}
}
