package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.PartialModulePath;

public record SimplePrefixResolver(String prefix) implements ModuleNameResolver {
    @Override
    public PartialModulePath resolve(EsModule<?> module) {
        String path = prefix + module.getClass().getCanonicalName().replace(".", "/") + ".js";
        return new PartialModulePath(path);
    }
}
