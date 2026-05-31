package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable, workspace-scoped identity for a leaf pane in the SplitPane tree.
 * The wrapper that the {@code Names Are Types} doctrine demands — no raw
 * {@code String} crosses framework code as a pane identifier.
 *
 * <p>Grammar: letters, digits, hyphen, underscore. Restricts paneIds to
 * forms that round-trip cleanly through URL fragments, JSON keys, and
 * CSS selectors without escaping.</p>
 *
 * @param value the underlying identifier; non-blank, format-restricted
 * @since RFC 0029 cycle 1
 */
public record PaneId(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public PaneId {
        Objects.requireNonNull(value, "PaneId.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "PaneId.value '" + value + "' — only letters, digits, hyphen, underscore allowed");
        }
    }

    /** Convenience constructor. */
    public static PaneId of(String value) {
        return new PaneId(value);
    }

    @Override public String toString() { return value; }
}
