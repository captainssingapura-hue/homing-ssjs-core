package hue.captains.singapura.js.homing.workspace.shell;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0058 — the deployment's {@link WorkspaceGroup}s by id, and the one index
 * the group model needs that the flat {@link WorkspaceSpecRegistry} cannot give:
 * <b>kind → group</b>. It is what the {@code ws_kind} legacy form consults to
 * find its group, and what {@code /goto} consults to answer for a kind.
 *
 * <p>Registration is the first half of law 4: a kind may belong to <em>one</em>
 * group, and a second group claiming it is refused with both groups named. The
 * second half — a group is placed exactly once in the catalogue tree — needs the
 * tree and lives in {@link WorkspaceGroups}.</p>
 *
 * <p>Like the spec registry, a process-wide singleton written at boot and read
 * after; a studio's fixtures register their groups right after their specs.</p>
 */
public final class WorkspaceGroupRegistry {

    public static final WorkspaceGroupRegistry INSTANCE = new WorkspaceGroupRegistry();

    private final Map<String, WorkspaceGroup> byId   = new LinkedHashMap<>();
    private final Map<String, WorkspaceGroup> byKind = new LinkedHashMap<>();

    private WorkspaceGroupRegistry() {}

    public WorkspaceGroup register(WorkspaceGroup group) {
        Objects.requireNonNull(group, "group");
        WorkspaceGroup prior = byId.get(group.id());
        if (prior != null) {
            if (prior.equals(group)) return prior;   // idempotent re-registration of the same value
            throw new IllegalStateException("Duplicate WorkspaceGroup id '" + group.id()
                    + "' — already registered holding " + prior.kinds());
        }
        for (String kind : group.kinds()) {
            WorkspaceGroup other = byKind.get(kind);
            if (other != null) {
                throw new IllegalStateException("Kind '" + kind + "' belongs to group '" + other.id()
                        + "' already; group '" + group.id() + "' cannot claim it too — a kind belongs to exactly one group");
            }
        }
        byId.put(group.id(), group);
        for (String kind : group.kinds()) byKind.put(kind, group);
        return group;
    }

    public Optional<WorkspaceGroup> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /** The group holding {@code kind}, when any does. */
    public Optional<WorkspaceGroup> groupOf(String kind) {
        return Optional.ofNullable(byKind.get(kind));
    }

    /** All registered groups, in registration order. */
    public Collection<WorkspaceGroup> all() { return byId.values(); }

    void resetForTesting() { byId.clear(); byKind.clear(); }
}
