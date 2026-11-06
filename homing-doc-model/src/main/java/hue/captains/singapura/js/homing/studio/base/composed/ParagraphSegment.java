package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.List;
import java.util.Objects;

/**
 * A plain paragraph — a list of {@link Line.Plain} lines rendered as one regular,
 * <b>unformatted</b> paragraph: the lines flow together into a single {@code <p>}
 * (no inline markup, no per-line breaks). The where-the-wrap-falls is an authoring
 * detail; the rendered paragraph reads the same.
 *
 * <p>The strict, directly-constructed successor to a prose {@link MarkdownSegment}
 * / {@link TextSegment} for the common "just a paragraph" case — built from the
 * {@link Line} building blocks, so every line is capped at {@value Line#MAX_CHARS}
 * chars. First of the strict block-segment family that lets the free-form prose
 * segments retire.</p>
 *
 * @param lines the paragraph's plain lines, in order; at least one
 */
public record ParagraphSegment(List<Line.Plain> lines) implements Listable {

    public ParagraphSegment {
        Objects.requireNonNull(lines, "ParagraphSegment.lines");
        lines = List.copyOf(lines);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("ParagraphSegment.lines must have at least one line");
        }
    }

    /** The flowing paragraph text — the lines joined by a single space. */
    public String text() {
        var sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(lines.get(i).raw());
        }
        return sb.toString();
    }

    /**
     * Convenience — author from a single prose string, word-wrapped into
     * {@link Line.Plain} lines at the {@value Line#MAX_CHARS}-char cap. Whitespace
     * runs collapse to single spaces; an over-long single word is hard-split.
     */
    public ParagraphSegment(String text) {
        this(LineWrapper.INSTANCE.wrap(text));
    }
}
