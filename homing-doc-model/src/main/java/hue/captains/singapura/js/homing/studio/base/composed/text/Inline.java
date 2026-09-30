package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0018 / Phase 4 — sealed inline-token ADT for {@code TextSegment}
 * prose. Strictly additive over plain text: each variant earns its
 * keep via the existing-prose audit (Phase 4 design step).
 *
 * <p>Five variants only:</p>
 * <ul>
 *   <li>{@link Text}   — literal runs (no syntax)</li>
 *   <li>{@link Bold}   — {@code **bold**}</li>
 *   <li>{@link Italic} — {@code *italic*}</li>
 *   <li>{@link Code}   — {@code `inline code`} (contents are literal,
 *       no further parsing inside the backticks)</li>
 *   <li>{@link Ref}    — {@code [label](#ref:name)}; resolves to a
 *       declared {@code Reference} via the doc's references list</li>
 * </ul>
 *
 * <p>Nesting rules (strict): bold and italic may contain text, code,
 * and refs but <b>not</b> other bold/italic — that prevents the bold-vs-
 * italic ambiguity at {@code ***} and similar. Code is leaf only
 * (everything inside backticks is literal text).</p>
 *
 * @since RFC 0018 Phase 4
 */
public sealed interface Inline extends ValueObject
        permits Inline.Text, Inline.Bold, Inline.Italic, Inline.Code, Inline.Ref {

    /** Literal text run. */
    record Text(String text) implements Inline {
        public Text {
            Objects.requireNonNull(text, "Inline.Text.text");
        }
    }

    /** {@code **bold**} — wraps a list of leaf inlines (no nested bold/italic). */
    record Bold(List<Inline> inlines) implements Inline {
        public Bold {
            Objects.requireNonNull(inlines, "Inline.Bold.inlines");
            inlines = List.copyOf(inlines);
            for (Inline i : inlines) {
                if (i instanceof Bold || i instanceof Italic) {
                    throw new IllegalArgumentException(
                            "Inline.Bold may not nest bold/italic; got " + i.getClass().getSimpleName());
                }
            }
        }
    }

    /** {@code *italic*} — wraps a list of leaf inlines (no nested bold/italic). */
    record Italic(List<Inline> inlines) implements Inline {
        public Italic {
            Objects.requireNonNull(inlines, "Inline.Italic.inlines");
            inlines = List.copyOf(inlines);
            for (Inline i : inlines) {
                if (i instanceof Bold || i instanceof Italic) {
                    throw new IllegalArgumentException(
                            "Inline.Italic may not nest bold/italic; got " + i.getClass().getSimpleName());
                }
            }
        }
    }

    /** {@code `inline code`} — leaf; backtick contents are literal text. */
    record Code(String text) implements Inline {
        public Code {
            Objects.requireNonNull(text, "Inline.Code.text");
        }
    }

    /**
     * {@code [label](#ref:name)} — typed cross-reference. Resolves at
     * render time against the doc's {@code references()} list (RFC
     * 0004-ext1). Both fields are post-parse, with the {@code #ref:}
     * prefix stripped.
     */
    record Ref(String label, String anchor) implements Inline {
        public Ref {
            Objects.requireNonNull(label,  "Inline.Ref.label");
            Objects.requireNonNull(anchor, "Inline.Ref.anchor");
            if (anchor.isBlank()) {
                throw new IllegalArgumentException("Inline.Ref.anchor must not be blank");
            }
        }
    }
}
