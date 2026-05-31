package hue.captains.singapura.js.homing.workspace.shell;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Process-wide registry of every {@link WorkspaceSpec} a runtime knows
 * about. {@link GenericWorkspace} reads from it at request time to
 * serialize the registry into the chrome's body JS; the JS chooses
 * which spec to mount by the URL's {@code ?ws_kind=…} parameter.
 *
 * <p>Apps populate the registry at construction time — typically a
 * static initializer or a studio's bootstrap. Validation is at
 * {@link #register(WorkspaceSpec)}: kind non-blank, no duplicate kind,
 * widget entries non-empty.</p>
 *
 * <p>Singleton ({@link #INSTANCE}) per JVM. Synchronization is the
 * caller's responsibility — register at boot, read after.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public final class WorkspaceSpecRegistry {

    public static final WorkspaceSpecRegistry INSTANCE = new WorkspaceSpecRegistry();

    private final Map<String, WorkspaceSpec> byKind = new LinkedHashMap<>();

    private WorkspaceSpecRegistry() {}

    /** Register a spec. Throws on duplicate kind or invalid declarations. */
    public WorkspaceSpec register(WorkspaceSpec spec) {
        Objects.requireNonNull(spec, "spec");
        String kind = spec.kind();
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException(
                    spec.getClass().getName() + ".kind() must be a non-blank stable identifier");
        }
        if (byKind.containsKey(kind)) {
            throw new IllegalStateException(
                    "Duplicate WorkspaceSpec kind '" + kind + "' — already registered by "
                  + byKind.get(kind).getClass().getName());
        }
        if (spec.widgetEntries() == null || spec.widgetEntries().isEmpty()) {
            throw new IllegalStateException(
                    spec.getClass().getName() + ".widgetEntries() must be non-empty — "
                  + "a workspace with no widget types has nothing to host.");
        }
        byKind.put(kind, spec);
        return spec;
    }

    /** Resolve a spec by kind. */
    public Optional<WorkspaceSpec> get(String kind) {
        return Optional.ofNullable(byKind.get(kind));
    }

    /** All registered specs in registration order (the JSON wire shape preserves this order). */
    public Collection<WorkspaceSpec> all() { return byKind.values(); }

    /** Test-only — clears the registry. Production code never calls this. */
    void resetForTesting() { byKind.clear(); }
}
