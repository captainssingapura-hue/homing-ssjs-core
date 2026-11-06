package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.List;
import java.util.Objects;

/**
 * One segment of a served module's JS, as a list of lines. Kept line-oriented
 * so a {@link Finding} can point at a line and rules can scan cheaply. Value
 * object — immutable, no behaviour beyond joining.
 *
 * @param lines the segment's lines, in order (may be empty)
 */
public record JsSource(List<String> lines) implements Scalar {

    /** The empty segment (e.g. a consumer with no injected header, or no exports). */
    public static final JsSource EMPTY = new JsSource(List.of());

    public JsSource {
        Objects.requireNonNull(lines, "JsSource.lines");
        lines = List.copyOf(lines);
    }

    /** A segment from explicit lines. */
    public JsSource(String... lines) {
        this(List.of(lines));
    }

    /** The segment as a single string, lines rejoined with {@code '\n'}. */
    public String text() { return String.join("\n", lines); }

    /** Whether the segment has no lines. */
    public boolean isEmpty() { return lines.isEmpty(); }
}
