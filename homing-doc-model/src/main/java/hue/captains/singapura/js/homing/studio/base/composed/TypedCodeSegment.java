package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.Objects;
import java.util.Optional;

/**
 * A code listing whose language is a {@link CodeLanguage} rather than a String —
 * {@link CodeSegment}, mirrored field for field, with the second argument typed.
 *
 * <pre>{@code
 * new CodeSegment(src, "java")                    // the String form
 * new TypedCodeSegment(src, CodeLanguage.JAVA)    // the typed form
 * }</pre>
 *
 * <h2>It is a mirror on purpose</h2>
 *
 * <p>Same fields, same order, same wire shape: both serialise to
 * {@code {"kind":"code", …, "language":"java", "body":…}}, so a renderer cannot
 * tell them apart and no client code changed to make this exist. That is what
 * makes migration a per-call-site decision rather than a flag day — swap the one
 * argument when you touch a doc, leave the rest, and nothing observable moves.
 * {@code TypedCodeSegmentTest} pins that equivalence.</p>
 *
 * <h2>Why the two do not merge</h2>
 *
 * <p>{@code CodeSegment} keeps its String because {@code MarkdownDocNormalizer}
 * produces it from a fence's info string — whatever an author typed after the
 * backticks, which is unbounded input and genuinely a String at that boundary.
 * Hand-authored docs are the opposite case: the author is writing Java, the set
 * of languages is known, and the language now decides whether the segment draws
 * a diagram. The two segments exist because the two sources really are different.</p>
 *
 * @param body     the source text, rendered verbatim
 * @param language the typed language; {@link CodeLanguage#NONE} for unspecified
 * @param title    optional segment title; contributes to the TOC as a level-2
 *                 entry when present
 *
 * @since homing-studio-base — the typed half of RFC 0059 D15
 */
public record TypedCodeSegment(String body, CodeLanguage language, Optional<String> title)
        implements Listable {

    public TypedCodeSegment {
        Objects.requireNonNull(body,     "TypedCodeSegment.body");
        Objects.requireNonNull(language, "TypedCodeSegment.language (use CodeLanguage.NONE)");
        Objects.requireNonNull(title,    "TypedCodeSegment.title (use Optional.empty)");
    }

    /** Convenience — no title. */
    public TypedCodeSegment(String body, CodeLanguage language) {
        this(body, language, Optional.empty());
    }

    /** Convenience — unspecified language, no title. */
    public TypedCodeSegment(String body) {
        this(body, CodeLanguage.NONE, Optional.empty());
    }

    /** The String form this segment is equivalent to — the migration's other side. */
    public CodeSegment asUntyped() {
        return new CodeSegment(body, language.tag(), title);
    }
}
