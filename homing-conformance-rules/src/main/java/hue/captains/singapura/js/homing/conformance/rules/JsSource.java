package hue.captains.singapura.js.homing.conformance.rules;

import java.util.List;
import java.util.Objects;

/**
 * One segment of a served module's JS, as a list of lines. Kept line-oriented
 * so a {@link Finding} can point at a line and rules can scan cheaply. Value
 * object — immutable, no behaviour beyond joining.
 *
 * @param lines the segment's lines, in order (may be empty)
 */
public record JsSource(List<String> lines) {

    /** The empty segment (e.g. a consumer with no injected header, or no exports). */
    public static final JsSource EMPTY = new JsSource(List.of());

    public JsSource {
        Objects.requireNonNull(lines, "JsSource.lines");
        lines = List.copyOf(lines);
    }

    /** A segment from raw text, split on newlines. */
    public static JsSource ofText(String text) {
        Objects.requireNonNull(text, "text");
        return text.isEmpty() ? EMPTY : new JsSource(List.of(text.split("\n", -1)));
    }

    /** A segment from explicit lines. */
    public static JsSource of(String... lines) {
        return new JsSource(List.of(lines));
    }

    /** The segment as a single string, lines rejoined with {@code '\n'}. */
    public String text() { return String.join("\n", lines); }

    public boolean isEmpty() { return lines.isEmpty(); }
}
