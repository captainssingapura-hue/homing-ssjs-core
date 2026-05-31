package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity of one workspace instance — UUID under the wrapper. Stable
 * across renames, exports, imports. Paired with {@link WorkspaceKind}
 * (the type-of-workspace) as the composite key for every per-instance
 * storage concern (event log rows, checkpoint rows, multi-workspace
 * catalogue entries).
 *
 * <p>Per the Names Are Types doctrine, no raw {@code String} or {@code
 * UUID} crosses a contract boundary as a workspace-instance reference.
 * The wrapper carries the type tag.</p>
 *
 * <p>Single-workspace (pre-RFC-0031) code uses {@link
 * #placeholderFor(WorkspaceKind)} — a deterministic per-kind UUID so the
 * storage shape never has to migrate when RFC 0031 multi-instance lands.
 * The placeholder collapses to a real per-instance UUID once the chrome
 * starts minting them.</p>
 *
 * @param id the underlying UUID
 * @since RFC 0035 P1 (forward-declared per RFC 0031 sketch)
 */
public record WorkspaceInstanceId(UUID id) {

    public WorkspaceInstanceId { Objects.requireNonNull(id, "WorkspaceInstanceId.id"); }

    public static WorkspaceInstanceId fresh() {
        return new WorkspaceInstanceId(UUID.randomUUID());
    }

    public static WorkspaceInstanceId parse(String s) {
        return new WorkspaceInstanceId(UUID.fromString(s));
    }

    /**
     * Deterministic placeholder for single-workspace use. Derives a UUID
     * from {@code "workspace:" + kind} via a stable hash so the same kind
     * always yields the same instance id pre-RFC-0031.
     */
    public static WorkspaceInstanceId placeholderFor(WorkspaceKind kind) {
        Objects.requireNonNull(kind, "kind");
        String seed = "workspace:" + kind.value();
        int hash = 0;
        for (int i = 0; i < seed.length(); i++) {
            hash = (hash << 5) - hash + seed.charAt(i);
        }
        long high = (((long) hash) << 32) | 0x7000_5000_9000L;
        long low  = (((long) hash) << 32) | 0x0001L;
        return new WorkspaceInstanceId(new UUID(high, low));
    }

    @Override public String toString() { return id.toString(); }
}
