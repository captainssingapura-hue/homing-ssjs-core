package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ImportsWriterResolver;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SingleModuleImportWriter;

public record SimpleImportsWriterResolver(ModuleNameResolver nameResolver) implements ImportsWriterResolver {

    @Override
    public <M extends EsModule<M>> SingleModuleImportWriter<M> resolve(M fromModule) {
        return new SingleModuleImportWriter<>(fromModule, nameResolver);
    }
}
