package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExternalModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.PartialModulePath;

import java.util.Map;

/**
 * Resolves module names as query-parameter URLs.
 * <p>Generates paths like {@code /module?class=hue.captains.singapura.js.homing.demo.es.Alice}
 * — the class alone, the same for every importer.</p>
 *
 * <p>{@code libraryUrls} is what a deployment says of its {@link ExternalModule} proxies: where
 * each imports its library from, when not from the proxy's own default.</p>
 */
public record QueryParamResolver(String actionPath, Map<Class<? extends ExternalModule<?>>, String> libraryUrls)
        implements ModuleNameResolver {

    public QueryParamResolver {
        libraryUrls = Map.copyOf(libraryUrls);
        libraryUrls.forEach((proxy, url) -> {
            if (url.isBlank()) throw new IllegalArgumentException("library url must not be blank for " + proxy.getName());
        });
    }

    /** Every proxy on its own default. */
    public QueryParamResolver(String actionPath) {
        this(actionPath, Map.of());
    }

    public QueryParamResolver() {
        this("/module");
    }

    @Override
    public PartialModulePath resolve(EsModule<?> module) {
        String base = actionPath + "?class=" + module.getClass().getCanonicalName();
        return new PartialModulePath(base);
    }

    @Override
    public String libraryUrl(Class<? extends ExternalModule<?>> proxy, String defaultUrl) {
        return libraryUrls.getOrDefault(proxy, defaultUrl);
    }
}
