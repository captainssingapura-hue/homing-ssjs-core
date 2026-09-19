package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.server.AppMeta;
import hue.captains.singapura.js.homing.server.HomingActionRegistry;
import hue.captains.singapura.js.homing.server.QueryParamResolver;
import hue.captains.singapura.js.homing.server.ServedModules;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.SiteGetAction;
import hue.captains.singapura.js.homing.site.SiteHost;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.PostAction;
import hue.captains.singapura.tao.http.config.HostConfig;
import hue.captains.singapura.tao.http.vertx.VertxActionHost;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The standard MPA for one site: its brand, the themes it offers, the crates
 * it serves. From these, {@link #page} makes a JS app a page under the
 * chrome, and {@link #registry} puts the framework's routes beside the
 * site's own — {@code /module} for the JS, {@code /css-content} for the
 * sheets, {@code /themes} for the preferences menu, {@code /app} as the
 * flat address — with the site's catch-all last.
 *
 * <p>This is the one declaration a themed site makes. It knows nothing of
 * how the site routes: a page made here is a {@link hue.captains.singapura.js.homing.site.Placed}
 * navigable, and whatever router hands it out may tell it where it is.</p>
 */
public final class StandardMpa {

    private final Brand brand;
    private final ThemeRegistry themes;
    private final List<Crate> crates;
    private final ModuleNameResolver names;
    private final HomingActionRegistry framework;
    private final ThemesGetAction themesAction;

    private StandardMpa(Brand brand, ThemeRegistry themes, List<Crate> crates) {
        this.brand  = Objects.requireNonNull(brand, "StandardMpa.brand");
        this.themes = themes == null ? ThemeRegistry.EMPTY : themes;
        var all = new ArrayList<Crate>(crates);
        if (all.stream().noneMatch(c -> c == MpaCrate.INSTANCE)) all.add(MpaCrate.INSTANCE);
        priorsCrate(this.themes, ServedModules.of(all)).ifPresent(all::add);
        this.crates = List.copyOf(all);
        this.names  = new QueryParamResolver("/module");
        var apps = new ArrayList<AppModule<?, ?>>();
        for (EsModule<?> m : ServedModules.of(this.crates).byName().values()) {
            if (m instanceof AppModule<?, ?> a) apps.add(a);
        }
        this.framework = new HomingActionRegistry(names, new SimpleAppResolver(apps), ResourceReader.INSTANCE,
                                                  this.themes, new AppMeta(brand.label()), this.crates);
        this.themesAction = new ThemesGetAction(this.themes);
    }

    /**
     * The registry's priors — the palette groups every page leans on without
     * declaring them — must be served too, and a site should not have to know
     * which crate holds them. When any are not already among the served
     * modules, they are served from a crate of their own.
     */
    private static Optional<Crate> priorsCrate(ThemeRegistry themes, ServedModules served) {
        var missing = new ArrayList<CrateEntry>();
        for (CssGroup<?> prior : themes.priors()) {
            if (served.find(prior.getClass().getCanonicalName()).isEmpty()) missing.add(CrateEntry.of(prior));
        }
        if (missing.isEmpty()) return Optional.empty();
        var entries = List.copyOf(missing);
        return Optional.of(new Crate() {
            @Override public String name() { return "theme-priors"; }
            @Override public List<CrateEntry> entries() { return entries; }
        });
    }

    /** A site's MPA: the brand on the bar, the themes it offers, the crates whose modules it serves. */
    public static StandardMpa of(Brand brand, ThemeRegistry themes, Crate... crates) {
        return new StandardMpa(brand, themes, List.of(crates));
    }

    public Brand brand() { return brand; }

    public ThemeRegistry themes() { return themes; }

    /** The registry's default — first listed — when it lists any. */
    public Optional<String> defaultTheme() {
        return themes.themes().isEmpty() ? Optional.empty() : Optional.of(themes.themes().get(0).slug());
    }

    /** The address a served module is imported from, without the theme. */
    public String moduleUrl(EsModule<?> module) {
        return names.resolve(module).basePath();
    }

    // ── Pages ─────────────────────────────────────────────────────────────────

    /** {@code app} bound to {@code params}, as a page under the chrome. */
    public <P extends AppModule._Param, M extends AppModule<P, M>> AppPage<P, M> page(M app, P params) {
        return new AppPage<>(app, params, this);
    }

    /** A paramless app as a page under the chrome. */
    public <M extends AppModule<AppModule._None, M>> AppPage<AppModule._None, M> page(M app) {
        return new AppPage<>(app, AppModule._None.INSTANCE, this);
    }

    // ── Serving ───────────────────────────────────────────────────────────────

    /**
     * The framework's routes, then the site's catch-all. Insertion order is
     * the order the host mounts them, and {@code /*} must come last or it
     * would answer for {@code /module} too.
     */
    public ActionRegistry<RoutingContext> registry(Site site) {
        var siteActions = SiteHost.registry(site).getActions();
        var gets = new LinkedHashMap<String, GetAction<RoutingContext, ?, ?, ?>>();
        gets.putAll(framework.getActions());
        gets.put(ThemesGetAction.ROUTE, themesAction);
        siteActions.forEach((route, action) -> { if (!route.equals(SiteGetAction.ROUTE)) gets.put(route, action); });
        gets.put(SiteGetAction.ROUTE, siteActions.get(SiteGetAction.ROUTE));
        var posts = Map.copyOf(framework.postActions());
        return new ActionRegistry<>() {
            @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() { return gets; }
            @Override public Map<String, PostAction<RoutingContext, ?, ?, ?>> postActions() { return posts; }
        };
    }

    /** Hosts the site with its MPA over plain HTTP on {@code port}. */
    public Future<HttpServer> start(Site site, int port) {
        var host = new VertxActionHost(registry(site), HostConfig.http(port));
        return host.start()
                .onSuccess(server -> System.out.println(
                        site.name() + " listening on http://localhost:" + server.actualPort() + "/"))
                .onFailure(err -> System.err.println(
                        site.name() + " failed to start: " + err.getMessage()));
    }
}
