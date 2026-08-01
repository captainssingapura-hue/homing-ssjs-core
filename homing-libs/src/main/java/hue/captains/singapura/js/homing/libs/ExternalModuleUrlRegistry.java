package hue.captains.singapura.js.homing.libs;

import hue.captains.singapura.js.homing.core.ExternalModule;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide override table for {@link ExternalModule} proxy URLs — the
 * "configurable" half of a configurable 3rd-party module proxy.
 *
 * <p>An {@code ExternalModule} proxy (e.g. {@link MermaidProxyModule}) ships a
 * <em>default</em> remote URL it imports the real library from. When that
 * default is unreachable for a given deployment — blocked CDN, corporate proxy,
 * air-gap, a pinned internal mirror, or simply a newer version — the deployment
 * overrides it <b>once at boot</b>, before any page is served:</p>
 *
 * <pre>{@code
 * // in a downstream Fixtures constructor or a server main():
 * ExternalModuleUrlRegistry.INSTANCE.override(
 *         MermaidProxyModule.class,
 *         "https://my-mirror.internal/mermaid@11/mermaid.esm.min.mjs");
 * }</pre>
 *
 * <p>The proxy consults {@link #resolve(Class, String)} at serve time, so the
 * override takes effect for every subsequently served copy of that module with
 * no per-call wiring — mirroring the boot-time {@code register(...)} idiom used
 * by {@code WorkspaceSpecRegistry} / {@code ThemeRegistry}. Keyed by the proxy's
 * {@link Class} so the override is typed (no stringly-typed module id).</p>
 *
 * <p>Absent an override, {@code resolve} returns the proxy's own default — so
 * the table is empty and inert until a deployment opts in.</p>
 */
public final class ExternalModuleUrlRegistry {

    public static final ExternalModuleUrlRegistry INSTANCE = new ExternalModuleUrlRegistry();

    private final Map<Class<? extends ExternalModule<?>>, String> overrides = new ConcurrentHashMap<>();

    private ExternalModuleUrlRegistry() {}

    /**
     * Point {@code proxy} at {@code url} for the rest of this JVM's life,
     * replacing any prior override. Call once at boot, before serving.
     */
    public void override(Class<? extends ExternalModule<?>> proxy, String url) {
        Objects.requireNonNull(proxy, "proxy");
        Objects.requireNonNull(url, "url");
        if (url.isBlank()) {
            throw new IllegalArgumentException("override url must not be blank for " + proxy.getName());
        }
        overrides.put(proxy, url);
    }

    /**
     * The URL {@code proxy} should import from — the override if one was set,
     * otherwise {@code defaultUrl} (the proxy's own baked-in default).
     */
    public String resolve(Class<? extends ExternalModule<?>> proxy, String defaultUrl) {
        return overrides.getOrDefault(proxy, defaultUrl);
    }

    /** Drop any override for {@code proxy}, restoring its default. Mainly for tests. */
    public void reset(Class<? extends ExternalModule<?>> proxy) {
        overrides.remove(proxy);
    }
}
