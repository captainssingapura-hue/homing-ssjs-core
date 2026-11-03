package hue.captains.singapura.js.homing.core;

public interface ImportsWriterResolver {
    /** The writer of the import statement for what is imported from {@code fromModule}. */
    <M extends EsModule<M>> SingleModuleImportWriter<M> resolve(M fromModule);
}
