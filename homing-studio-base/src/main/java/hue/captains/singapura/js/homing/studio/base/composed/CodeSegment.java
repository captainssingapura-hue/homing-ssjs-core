package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0019 §2.1 future kind — typed code listing segment.
 *
 * <p>Renders the body verbatim inside a {@code <pre><code>} block. The
 * framework's default theme already styles {@code .st-doc pre} and
 * {@code .st-doc pre code} (surface-inverted background, monospace font,
 * horizontal overflow scroll), so a CodeSegment dropped into a
 * {@link ComposedDoc} inherits the chrome's code-block look without
 * per-segment CSS.</p>
 *
 * <p>The {@code language} field is a free-form identifier (e.g. {@code
 * "java"}, {@code "javascript"}, {@code "bash"}, {@code "text"}). It's
 * emitted as {@code class="language-X"} on the {@code <code>} element —
 * the convention syntax-highlighters expect. The framework doesn't ship a
 * highlighter; downstream that wants one can either add a
 * {@code BundledExternalModule} for prism / highlight.js or rely on the
 * unhighlighted monospace presentation (which is already legible against
 * the inverted surface).</p>
 *
 * <p>Use this segment when the code listing is the segment — a tutorial
 * step, a configuration sample, an API surface dump. For inline code
 * fragments inside prose, use the {@code ``code``} inline grammar in a
 * {@link TextSegment} or {@link MarkdownSegment} instead.</p>
 *
 * <p>Per the no-cross-segment-references invariant (§3.4), a CodeSegment
 * can't reference values from neighbouring segments. Self-contained.</p>
 *
 * @param body     the source-code text, rendered verbatim
 * @param language language identifier (lowercase by convention); may be
 *                 empty for unspecified — the segment still renders, just
 *                 without a {@code language-X} class
 * @param title    optional segment title; contributes to the TOC as a
 *                 level-2 entry when present
 *
 * @since RFC 0019 §2.1 — future kind landed
 */
public record CodeSegment(String body, String language, Optional<String> title) implements Segment {

    public CodeSegment {
        Objects.requireNonNull(body,     "CodeSegment.body");
        Objects.requireNonNull(language, "CodeSegment.language (use \"\" for unspecified)");
        Objects.requireNonNull(title,    "CodeSegment.title (use Optional.empty)");
    }

    /** Convenience — no title. */
    public CodeSegment(String body, String language) {
        this(body, language, Optional.empty());
    }

    /** Convenience — no language, no title. */
    public CodeSegment(String body) {
        this(body, "", Optional.empty());
    }
}
