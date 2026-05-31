package hue.captains.singapura.js.homing.workspace.events.contract;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Typed name of an event kind — PascalCase, used as the event-log's
 * vocabulary key. Mirrors the grammar of {@link
 * hue.captains.singapura.js.homing.workspace.state.WorkspaceKind} for
 * consistency: same shape across every framework identifier that names a
 * type-of-thing.
 *
 * <p>Per the Names Are Types doctrine, no raw {@code String} crosses a
 * contract boundary as an event name. The wrapper carries the grammar
 * invariant.</p>
 *
 * <p>The sealed payload hierarchy {@link WorkspaceEventPayload} provides
 * the typed counterpart — every variant declares its own EventName via a
 * {@code static EventName NAME = new EventName("VariantName")} so the
 * vocabulary is centralised and typo-safe at authoring time.</p>
 *
 * @param value the underlying PascalCase identifier
 * @since RFC 0035 P1
 */
public record EventName(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Z][A-Za-z0-9]*");

    public EventName {
        Objects.requireNonNull(value, "EventName.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "EventName.value '" + value + "' — PascalCase required");
        }
    }

    public static EventName of(String value) { return new EventName(value); }

    @Override public String toString() { return value; }
}
