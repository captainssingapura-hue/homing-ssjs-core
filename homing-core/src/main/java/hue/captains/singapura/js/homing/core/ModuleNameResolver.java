package hue.captains.singapura.js.homing.core;

/**
 * Resolves how a module's name is written in a JavaScript import statement:
 * one {@link PartialModulePath} per module, whoever imports it, and where an
 * {@link ExternalModule} imports its 3rd-party library from.
 */
@FunctionalInterface
public interface ModuleNameResolver {
    /** How {@code module} is written in an import statement. */
    PartialModulePath resolve(EsModule<?> module);

    /**
     * The address {@code proxy} imports its 3rd-party library from: the one this
     * deployment gives it, else {@code defaultUrl}, the proxy's own.
     */
    default String libraryUrl(Class<? extends ExternalModule<?>> proxy, String defaultUrl) {
        return defaultUrl;
    }
}
