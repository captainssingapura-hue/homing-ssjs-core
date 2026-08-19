package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
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
    private final ThemeVarsGetAction themeVarsAction;
    private final ThemeGlobalsGetAction themeGlobalsAction;

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

    /** RFC 0044: {@code servable} is the registered crate closure's module classes —
     *  when non‑null, {@code /module} refuses to serve a class outside it (a served
     *  module must be crated). {@code null} keeps the legacy permissive behaviour. */
    public HomingActionRegistry(ModuleNameResolver nameResolver, SimpleAppResolver appResolver,
                                ResourceReader resourceReader, ThemeRegistry themeRegistry,
                                AppMeta meta, java.util.Set<String> servable) {
        if (themeRegistry == null) themeRegistry = ThemeRegistry.EMPTY;
        if (meta == null) meta = AppMeta.DEFAULT;
        this.appAction = new AppHtmlGetAction(nameResolver, appResolver, themeRegistry, meta);
        this.moduleAction = new EsModuleGetAction(nameResolver, resourceReader, servable);
        // Base registry serves a typed-only CssContentGetAction with no impls
        // and no default theme — every /css-content request 404s unless an
        // outer registry (e.g. StudioActionRegistry) overrides this route with
        // its own typed-impl-aware action. RFC 0002 §3.6 (hard cut, no file-based fallback).
        this.cssContentAction = new CssContentGetAction(List.of(), null);
        this.themeVarsAction    = new ThemeVarsGetAction(themeRegistry, null);
        this.themeGlobalsAction = new ThemeGlobalsGetAction(themeRegistry, null);
    }

    /** Backwards-compatible constructor for callers that don't yet use {@code SimpleAppResolver}. */
    public HomingActionRegistry(ModuleNameResolver nameResolver, ResourceReader resourceReader) {
        this(nameResolver, null, resourceReader);
    }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() {
        return Map.of(
                "/app", appAction,
                "/module", moduleAction,
                "/css-content", cssContentAction,
                "/theme-vars", themeVarsAction,
                "/theme-globals", themeGlobalsAction
        );
    }

    @Override
    public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() {
        return Map.of();
    }
}
