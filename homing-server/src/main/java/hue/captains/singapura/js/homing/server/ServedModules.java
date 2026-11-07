package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.tao.ontology.Immutable;

import java.util.Collection;
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
public record ServedModules(Map<String, EsModule<?>> byName) implements Immutable {

    public ServedModules { byName = Map.copyOf(byName); }

    /** Serves nothing by name. {@code render(module)} still works on an instance. */
    public static final ServedModules NONE = new ServedModules(Map.of());

    /**
     * The modules of {@code roots} and of every crate they require, by canonical name; the first declared keeps a name.
     */
    public ServedModules(Collection<? extends Crate> roots) {
        this(CrateModules.INSTANCE.byName(roots));
    }

    /** The module of this canonical name, if a crate declared it. */
    public Optional<EsModule<?>> find(String canonicalName) {
        return Optional.ofNullable(byName.get(canonicalName));
    }

    /** How many modules are served. */
    public int size() { return byName.size(); }
}
