package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;

import java.util.Objects;

/**
 * A reference to one JS module to be verified — its fully-qualified class name.
 * The unit of a {@link ModuleRegistry} (RFC 0044's "the java proxies to be
 * verified"). Classification into a {@link JsModuleType} and rendering the
 * served artifact come later; a ref is just the module's identity.
 *
 * @param moduleClass the module's fully-qualified class name
 */
public record ModuleRef(String moduleClass) implements Comparable<ModuleRef> {

    public ModuleRef {
        Objects.requireNonNull(moduleClass, "ModuleRef.moduleClass");
        if (moduleClass.isBlank()) throw new IllegalArgumentException("ModuleRef.moduleClass must be non-blank");
    }

    /** The package portion of the class name ({@code ""} for the default package). */
    public String packageName() {
        int i = moduleClass.lastIndexOf('.');
        return i < 0 ? "" : moduleClass.substring(0, i);
    }

    /** The simple (unqualified) class name. */
    public String simpleName() {
        int i = moduleClass.lastIndexOf('.');
        return i < 0 ? moduleClass : moduleClass.substring(i + 1);
    }

    @Override public int compareTo(ModuleRef other) { return moduleClass.compareTo(other.moduleClass); }
    @Override public String toString() { return moduleClass; }
}
