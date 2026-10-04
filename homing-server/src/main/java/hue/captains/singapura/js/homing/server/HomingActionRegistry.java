package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;

public class HomingActionRegistry implements ActionRegistry<RoutingContext> {

    private final AppHtmlGetAction appAction;
    private final EsModuleGetAction moduleAction;
    private final CssContentGetAction cssContentAction;

    /** Legacy constructor — only the {@code ?class=} contract is supported. */
    public HomingActionRegistry(ModuleNameResolver nameResolver) {
        this(nameResolver, null, ResourceReader.fromSystemProperty());
    }

    /**
     * Construct with a {@link SimpleAppResolver} (RFC 0001 Step 07) — supports both
     * the new {@code ?app=&lt;simpleName&gt;} and the legacy {@code ?class=&lt;canonical&gt;}
     * contracts on {@code /app}. Other endpoints retain {@code ?class=}.
     */
    public HomingActionRegistry(ModuleNameResolver nameResolver, SimpleAppResolver appResolver) {
        this(nameResolver, appResolver, ResourceReader.fromSystemProperty());
    }

    public HomingActionRegistry(ModuleNameResolver nameResolver, SimpleAppResolver appResolver, ResourceReader resourceReader) {
        this(nameResolver, appResolver, resourceReader, ThemeRegistry.EMPTY);
    }

    /** RFC 0002-ext1: construct with a populated ThemeRegistry — drives the
     *  theme-bundle endpoints AND the theme picker widget in the page bootstrap. */
    public HomingActionRegistry(ModuleNameResolver nameResolver, SimpleAppResolver appResolver,
                                ResourceReader resourceReader, ThemeRegistry themeRegistry) {
        this(nameResolver, appResolver, resourceReader, themeRegistry, AppMeta.DEFAULT);
    }

    /** Downstream-aware overload — accepts an {@link AppMeta} carrying the
     *  studio's brand label so {@link AppHtmlGetAction} can produce a
     *  brand-correct {@code <title>} server-side. */
    public HomingActionRegistry(ModuleNameResolver nameResolver, SimpleAppResolver appResolver,
                                ResourceReader resourceReader, ThemeRegistry themeRegistry,
                                AppMeta meta) {
        this(nameResolver, appResolver, resourceReader, themeRegistry, meta, null);
    }

    /** RFC 0044: {@code crates} are the deployment's crate roots; their closure is
     *  what {@code /module} and {@code /css-content} serve, by canonical name, and
     *  nothing else. {@code null} serves nothing by name (in-process rendering only). */
    public HomingActionRegistry(ModuleNameResolver nameResolver, SimpleAppResolver appResolver,
                                ResourceReader resourceReader, ThemeRegistry themeRegistry,
                                AppMeta meta, java.util.Collection<? extends Crate> crates) {
        if (themeRegistry == null) themeRegistry = ThemeRegistry.EMPTY;
        if (meta == null) meta = AppMeta.DEFAULT;
        this.appAction = new AppHtmlGetAction(nameResolver, appResolver, themeRegistry, meta);
        ServedModules served = crates == null ? ServedModules.NONE : ServedModules.of(crates);
        // A group varies with the theme when a design-side renderer owns it and says
        // so; every other group is served once, without a theme, and left alone by a
        // switch.
        var renderers = themeRegistry.renderers(served);
        java.util.function.Predicate<CssGroup<?>> varies = g -> renderers.stream().anyMatch(r -> r.owns(g) && r.varies(g));
        // The first theme listed is the default: what a sheet requested without
        // ?theme= is rendered under, and what each CSS group module hands the
        // client's manager as its fallback. RFC 0002 §3.6 still holds: no
        // file-based fallback.
        Theme defaultTheme = themeRegistry.themes().isEmpty() ? null : themeRegistry.themes().get(0);
        this.moduleAction = new EsModuleGetAction(nameResolver, resourceReader, served, varies,
                defaultTheme == null ? null : defaultTheme.slug());
        // Every group renders from its inline bodies; the design side's renderers are
        // asked first.
        this.cssContentAction = new CssContentGetAction(defaultTheme, served, renderers);
    }

    /** Backwards-compatible constructor for callers that don't yet use {@code SimpleAppResolver}. */
    public HomingActionRegistry(ModuleNameResolver nameResolver, ResourceReader resourceReader) {
        this(nameResolver, null, resourceReader);
    }

    /** RFC 0051 — the page renderer, so the catalogue-path route can render by
     *  delegation rather than growing a second one. */
    public AppHtmlGetAction appAction() { return appAction; }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() {
        return Map.of(
                "/app", appAction,
                "/module", moduleAction,
                "/css-content", cssContentAction
        );
    }

    @Override
    public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() {
        return Map.of();
    }
}
