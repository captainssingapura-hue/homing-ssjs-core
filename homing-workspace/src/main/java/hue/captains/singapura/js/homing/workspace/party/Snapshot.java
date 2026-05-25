package hue.captains.singapura.js.homing.workspace.party;

import java.util.Map;
import java.util.Objects;

/**
 * The inspection surface of an Agent (Secretary or Actor). Surfaced to
 * dev-tools via the runtime's walk-the-tree accessor.
 *
 * <p>For Secretaries, {@link #keyFacts()} typically reflects the full
 * coordination state (the state value is usually small enough to expose
 * directly). For Actors, {@code keyFacts} returns curated logical state
 * — never the raw UI state, which is large and uninteresting to a
 * messaging-layer debugger.</p>
 *
 * @param label short identifier — typically the Agent's class simple name
 * @param keyFacts inspection-friendly key-value pairs; immutable copy
 * @since RFC 0028 cycle 1
 */
public record Snapshot(String label, Map<String, Object> keyFacts) {

    public Snapshot {
        Objects.requireNonNull(label,    "Snapshot.label");
        Objects.requireNonNull(keyFacts, "Snapshot.keyFacts");
        if (label.isBlank()) {
            throw new IllegalArgumentException("Snapshot.label must not be blank");
        }
        keyFacts = Map.copyOf(keyFacts);
    }

    /** Convenience: label only, no key facts. */
    public static Snapshot of(String label) {
        return new Snapshot(label, Map.of());
    }
}
