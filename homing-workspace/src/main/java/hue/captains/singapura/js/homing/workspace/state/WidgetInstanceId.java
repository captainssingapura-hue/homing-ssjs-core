package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable, per-instance identity for a widget. Assigned at widget
 * construction time and threaded through the widget's lifecycle —
 * survives drag operations, persistence round-trips, and is the natural
 * key for undo/redo (future) and Party Actor identity (RFC 0028).
 *
 * <p>Backed by a {@link UUID} for unambiguous global identity. The wrapper
 * is what the {@code Names Are Types} doctrine demands — no raw
 * {@code String} or {@code UUID} crosses framework code as a widget
 * instance identifier.</p>
 *
 * @param id the underlying UUID; never null
 * @since RFC 0029 cycle 1
 */
public record WidgetInstanceId(UUID id) {

    public WidgetInstanceId {
        Objects.requireNonNull(id, "WidgetInstanceId.id");
    }

    /** Fresh, random instance id — the construction-time path. */
    public static WidgetInstanceId fresh() {
        return new WidgetInstanceId(UUID.randomUUID());
    }

    /** Parse from canonical UUID string form — the deserialisation path. */
    public static WidgetInstanceId parse(String s) {
        Objects.requireNonNull(s, "WidgetInstanceId.parse: s");
        return new WidgetInstanceId(UUID.fromString(s));
    }

    @Override public String toString() { return id.toString(); }
}
