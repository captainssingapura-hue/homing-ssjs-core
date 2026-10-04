package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.PartialModulePath;

/**
 * Resolves module names as query-parameter URLs.
 * <p>Generates paths like {@code /module?class=hue.captains.singapura.js.homing.demo.es.Alice}
 * — the class alone, the same for every importer.</p>
 */
public record QueryParamResolver(String actionPath) implements ModuleNameResolver {

    public QueryParamResolver() {
        this("/module");
    }

    @Override
    public PartialModulePath resolve(EsModule<?> module) {
        String base = actionPath + "?class=" + module.getClass().getCanonicalName();
        return new PartialModulePath(base);
    }
}
