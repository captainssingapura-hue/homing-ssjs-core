package hue.captains.singapura.js.homing.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Declare all imports for a given EsModule.
 *
 * <p>The map is keyed on {@link Importable} (RFC 0001 Step 04) so that
 * both EsModule sources (emit JS imports) and {@link Linkable} sources
 * (specifically {@link ProxyApp}, emit nav entries only) coexist in
 * one container.</p>
 *
 * @param <M> the importing module — always an EsModule
 */
public final class ImportsFor<M extends EsModule>{

    private final Map<Importable, ModuleImports<?>> allImports;

    private ImportsFor(Map<Importable, ModuleImports<?>> allImports) {
        this.allImports = allImports;
    }

    /** The imports of a module that imports nothing. */
    public ImportsFor() {
        this(Map.of());
    }

    /** What is imported from each source, by source, in {@link ImportOrder}. */
    public Map<Importable, ModuleImports<?>> getAllImports() {
        return allImports;
    }

    /** Whether any import is a link to an app. */
    public boolean importsAnyAppLink() {
        return allImports.values().stream()
                .anyMatch(mi -> mi.allImports().stream().anyMatch(e -> e instanceof AppLink<?>));
    }

    /** The distinct managers its imports inject. */
    public java.util.List<ManagerInjector> managerInjectors() {
        return allImports.keySet().stream()
                .filter(target -> target instanceof ManagerInjector)
                .map(target -> (ManagerInjector) target)
                .distinct()
                .toList();
    }

    /**
     * Builder utility to ensure the map is constructed properly.
     * @param <M> the importing module
     */
    public static final class _Builder<M extends EsModule> {
        private final HashMap<Importable, ModuleImports<?>> tempMap = new HashMap<>();

        /** Adds what is imported from one source, in place of anything added for that source before. */
        public <M2 extends Importable> _Builder<M> add(ModuleImports<M2> importsFromThisModule){
            this.tempMap.put(Objects.requireNonNull(importsFromThisModule.from(), "ModuleImports.from"), importsFromThisModule);
            return this;
        }

        /** The imports added so far, in {@link ImportOrder} whatever order they were added in. */
        public ImportsFor<M> build(){
            var ordered = new LinkedHashMap<Importable, ModuleImports<?>>();
            this.tempMap.keySet().stream().sorted(ImportOrder.INSTANCE).forEach(source -> ordered.put(source, this.tempMap.get(source)));
            return new ImportsFor<>(Collections.unmodifiableMap(ordered));
        }

    }
}
