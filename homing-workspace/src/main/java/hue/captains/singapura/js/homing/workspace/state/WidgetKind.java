package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Registry-key identity for a kind of widget — the string-shaped key the
 * workspace's widget registry uses to look up "how to construct one of
 * these." The wrapper that the {@code Names Are Types} doctrine demands —
 * no raw {@code String} crosses framework code as a widget-kind key.
 *
 * <p>Examples: {@code "MovingAnimalWidget"} (Java class simpleName),
 * {@code "spinning-animals"} (kebab-case), {@code "doc_view"} (snake_case).
 * Grammar is permissive — letters, digits, hyphen, underscore — to
 * accommodate widget naming conventions across the framework's host
 * languages. Validation is structural ("identifier-shaped"), not
 * stylistic.</p>
 *
 * @param value the underlying registry key
 * @since RFC 0029 cycle 1
 */
public record WidgetKind(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WidgetKind {
        Objects.requireNonNull(value, "WidgetKind.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "WidgetKind.value '" + value + "' — identifier-shaped required (letters, digits, hyphen, underscore)");
        }
    }

    public static WidgetKind of(String value) {
        return new WidgetKind(value);
    }

    @Override public String toString() { return value; }
}
