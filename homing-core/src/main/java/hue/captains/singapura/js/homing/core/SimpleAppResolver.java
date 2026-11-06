package hue.captains.singapura.js.homing.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Boot-time registry of {@link Linkable}s reachable from a set of entry apps.
 *
 * <p>The resolver walks {@link AppLink} import edges from the entry apps to
 * discover the transitive closure of reachable {@link AppModule}s and
 * {@link ProxyApp}s. The walker follows {@code AppLink<?>} edges
 * <i>only</i> — code-level imports (CSS, SVG, plain {@code Exportable._Constant})
 * do not trigger registration.</p>
 *
 * <p>Cycles are tolerated via a visited set. Simple-name collisions in the
 * closure throw {@link IllegalStateException} at construction.</p>
 *
 * <p>The resolver maintains separate maps for AppModules and ProxyApps so the
 * server side can dispatch {@code /app?app=...} only to the AppModule entries
 * (proxies are URL builders, not navigation targets — they 404 if requested
 * directly).</p>
 *
 * <p>Introduced in RFC 0001 Step 04.</p>
 */
public final class SimpleAppResolver {

    private final Map<String, AppModule<?, ?>> appsByName;
    private final Map<Class<?>, AppModule<?, ?>> appsByClass;
    private final Map<String, ProxyApp<?, ?>>   proxiesByName;
    private final Map<Class<?>, ProxyApp<?, ?>> proxiesByClass;

    /**
     * Build the registry from a list of entry apps. Walks {@link AppLink}
     * imports transitively to discover every reachable Linkable.
     *
     * @throws IllegalStateException on simple-name collision in the closure
     */
    public SimpleAppResolver(List<? extends AppModule<?, ?>> entryApps) {
        var apps    = new LinkedHashMap<Class<?>, AppModule<?, ?>>();
        var proxies = new LinkedHashMap<Class<?>, ProxyApp<?, ?>>();
        for (var entry : entryApps) {
            collect(entry, apps, proxies);
        }

        this.appsByName     = indexByName(apps.values(),    "app");
        this.appsByClass    = Map.copyOf(apps);
        this.proxiesByName  = indexByName(proxies.values(), "proxy");
        this.proxiesByClass = Map.copyOf(proxies);

        // Cross-kind name collision check: app and proxy share the simple-name namespace.
        for (var name : appsByName.keySet()) {
            if (proxiesByName.containsKey(name)) {
                throw new IllegalStateException(
                        "Simple-name collision across kinds: '" + name + "' is both an app ("
                      + appsByName.get(name).getClass().getName() + ") and a proxy ("
                      + proxiesByName.get(name).getClass().getName() + ")");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Lookups
    // -----------------------------------------------------------------------

    /** Resolve any Linkable (app or proxy) by simple-name. */
    public Linkable resolve(String simpleName) {
        var app = appsByName.get(simpleName);
        if (app != null) return app;
        return proxiesByName.get(simpleName);
    }

    /** Resolve only an AppModule by simple-name. Returns null for proxy names or unknown. */
    public AppModule<?, ?> resolveApp(String simpleName) {
        return appsByName.get(simpleName);
    }

    /** Resolve only a ProxyApp by simple-name. Returns null for app names or unknown. */
    public ProxyApp<?, ?> resolveProxy(String simpleName) {
        return proxiesByName.get(simpleName);
    }

    /** Resolve any Linkable by class. */
    public Linkable resolveByClass(Class<?> cls) {
        var app = appsByClass.get(cls);
        if (app != null) return app;
        return proxiesByClass.get(cls);
    }

    /** All AppModules in the closure. */
    public Collection<AppModule<?, ?>> apps() {
        return Collections.unmodifiableCollection(appsByClass.values());
    }

    /** All ProxyApps in the closure. */
    public Collection<ProxyApp<?, ?>> proxies() {
        return Collections.unmodifiableCollection(proxiesByClass.values());
    }

    /** All Linkables (apps + proxies) in the closure. */
    public Collection<Linkable> all() {
        var out = new ArrayList<Linkable>(appsByClass.size() + proxiesByClass.size());
        out.addAll(appsByClass.values());
        out.addAll(proxiesByClass.values());
        return Collections.unmodifiableList(out);
    }

    // -----------------------------------------------------------------------
    // The walker
    // -----------------------------------------------------------------------

    private void collect(
            AppModule<?, ?> app,
            Map<Class<?>, AppModule<?, ?>> apps,
            Map<Class<?>, ProxyApp<?, ?>> proxies) {
        if (apps.containsKey(app.getClass())) return; // visited
        apps.put(app.getClass(), app);

        // Walk imports following only AppLink edges.
        for (var entry : app.imports().getAllImports().entrySet()) {
            boolean importsLink = entry.getValue().allImports().stream()
                    .anyMatch(e -> e instanceof AppLink<?>);
            if (!importsLink) continue;

            Importable from = entry.getKey();
            switch (from) {
                case AppModule<?, ?> nextApp -> collect(nextApp, apps, proxies);
                case ProxyApp<?, ?> proxy -> proxies.putIfAbsent(proxy.getClass(), proxy);
                case EsModule<?> ignored -> {
                    // An AppLink import keyed on a non-Linkable EsModule is a
                    // misuse (you can only AppLink to a Linkable). Silently
                    // ignore here; the writer (Step 05) will emit a warning.
                }
            }
        }
    }

    private <L extends Linkable> Map<String, L> indexByName(
            Collection<L> linkables, String kind) {
        var byName = new LinkedHashMap<String, L>();
        for (var l : linkables) {
            String name = l.simpleName();
            if (name == null || name.isBlank()) {
                throw new IllegalStateException(
                        kind + " " + l.getClass().getName() + " has null/blank simpleName()");
            }
            var prev = byName.put(name, l);
            if (prev != null && prev != l) {
                throw new IllegalStateException(
                        "Simple-name collision: '" + name + "' is used by both "
                      + prev.getClass().getName() + " and " + l.getClass().getName());
            }
        }
        return Map.copyOf(byName);
    }
}
