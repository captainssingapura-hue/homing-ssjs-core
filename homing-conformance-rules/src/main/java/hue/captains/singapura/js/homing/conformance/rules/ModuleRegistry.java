package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The set of JS modules a conformance studio (or the engine) works over —
 * RFC 0044's passed-in "registry of the java proxies to be verified". The
 * studio base renders this; it does not discover it. A concrete studio builds
 * one (e.g. via {@link ModuleEnumerator} scanning the classpath, or a
 * hand-maintained list) and injects it.
 *
 * @param modules the module refs, de-duplicated and sorted by class name
 */
public record ModuleRegistry(List<ModuleRef> modules) {

    public ModuleRegistry {
        Objects.requireNonNull(modules, "ModuleRegistry.modules");
        // de-dup + sort for a stable, presentable order.
        modules = List.copyOf(new TreeSet<>(modules));
    }

    /** Build from raw class names. */
    public static ModuleRegistry ofClassNames(Iterable<String> classNames) {
        var refs = new TreeSet<ModuleRef>();
        for (String cn : classNames) refs.add(new ModuleRef(cn));
        return new ModuleRegistry(List.copyOf(refs));
    }

    public int size() { return modules.size(); }
    public boolean isEmpty() { return modules.isEmpty(); }

    /** The modules grouped by package name, packages in sorted order. */
    public SortedMap<String, List<ModuleRef>> byPackage() {
        var out = new TreeMap<String, List<ModuleRef>>();
        for (ModuleRef m : modules) {
            out.computeIfAbsent(m.packageName(), k -> new java.util.ArrayList<>()).add(m);
        }
        out.replaceAll((k, v) -> List.copyOf(v));
        return out;
    }
}
