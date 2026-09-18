package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The modules a deployment serves, pre-registered by canonical name from its
 * crates. The one place a request's {@code ?class=} is turned into a module
 * — by lookup, never by {@code Class.forName}: a name that no crate declared
 * is a name that cannot be served, and a nested type's canonical name
 * ({@code Target.Color.Surface}) resolves the same as a top-level one's,
 * because the map was built from the instances the crates hold.
 *
 * <p>Built once at boot from the crate roots and their {@code requires()}
 * closure; the canonical name is what the framework emits in every import
 * path and every CSS subgraph, so the two sides of the wire agree by
 * construction.</p>
 */
public record ServedModules(Map<String, EsModule<?>> byName) {

    public ServedModules { byName = Map.copyOf(byName); }

    /** Serves nothing by name. {@code render(module)} still works on an instance. */
    public static final ServedModules NONE = new ServedModules(Map.of());

    public static ServedModules of(Collection<? extends Crate> roots) {
        var byName = new LinkedHashMap<String, EsModule<?>>();
        var seen = new LinkedHashMap<String, Crate>();
        Deque<Crate> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            Crate c = queue.removeFirst();
            if (seen.putIfAbsent(c.name(), c) != null) continue;
            for (CrateEntry e : c.entries()) byName.putIfAbsent(e.module().getClass().getCanonicalName(), e.module());
            queue.addAll(c.requires());
        }
        return new ServedModules(byName);
    }

    public Optional<EsModule<?>> find(String canonicalName) {
        return Optional.ofNullable(byName.get(canonicalName));
    }

    public int size() { return byName.size(); }
}
