package hue.captains.singapura.js.homing.core;

/**
 * Resolves how a module's name is written in a JavaScript import statement:
 * one {@link PartialModulePath} per module, whoever imports it.
 */
@FunctionalInterface
public interface ModuleNameResolver {
    /** How {@code module} is written in an import statement. */
    PartialModulePath resolve(EsModule<?> module);
}
