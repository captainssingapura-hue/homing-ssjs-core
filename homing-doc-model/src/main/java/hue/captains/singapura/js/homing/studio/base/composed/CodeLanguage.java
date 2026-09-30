package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * The language a {@link TypedCodeSegment}'s listing is written in — a sealed
 * vocabulary rather than the free-form String {@link CodeSegment} carries.
 *
 * <h2>Why type it</h2>
 *
 * <p>A code listing's language started as decoration: it became
 * {@code class="language-X"} on the {@code <code>} element, a hint for a
 * highlighter the framework does not ship, and a typo cost nothing anyone would
 * notice. RFC 0059 Phase 2 changed that. {@code "mermaid"} is now <b>load-bearing</b>
 * — it is the difference between a diagram and a code listing — and a free-form
 * String is the wrong shape for a value a renderer branches on. Written
 * {@code "mermiad"}, the diagram silently stays a fence and nothing says why.</p>
 *
 * <p>The vocabulary is not invented. It is what the corpus actually writes,
 * counted across the four repositories: java (278), js/javascript (76),
 * mermaid (35), bash (19), css (9), xml (5), json (4), html (3), and one or two
 * each of md, svg, csv and text.</p>
 *
 * <h2>js and javascript were the same language, spelled twice</h2>
 *
 * <p>55 fences say {@code js} and 21 say {@code javascript}. Both mean the same
 * thing, and nothing could tell — which is the smaller version of the same
 * defect. {@link #JAVASCRIPT} has one tag, so a typed doc cannot spell it two
 * ways. Markdown docs keep whatever they wrote: {@code MarkdownDocNormalizer}
 * still emits an untyped {@link CodeSegment}, so this normalisation reaches only
 * what is authored through the type.</p>
 *
 * <h2>{@link #other(String)} is the escape hatch, and it is debt</h2>
 *
 * <p>A sealed set with no way out cannot be adopted progressively — a downstream
 * studio writing Kotlin would be unable to migrate at all. So {@code other("kotlin")}
 * exists, and it is exactly as type-safe as the String it wraps: not at all. If
 * you reach for it, the language probably deserves a constant here.</p>
 *
 * @since homing-studio-base — the typed half of RFC 0059 D15
 */
public sealed interface CodeLanguage extends ValueObject {

    /** The wire identifier — what reaches {@code class="language-X"}. */
    String tag();

    /** A named member of the vocabulary. */
    record Known(String tag) implements CodeLanguage {
        public Known {
            Objects.requireNonNull(tag, "CodeLanguage.tag");
        }
        @Override public String toString() { return tag; }
    }

    /**
     * A language outside the vocabulary. Deliberately awkward to reach — see the
     * class note; it takes an arbitrary String precisely because it gives up the
     * guarantee the rest of the type provides.
     */
    record Other(String tag) implements CodeLanguage {
        public Other {
            Objects.requireNonNull(tag, "CodeLanguage.Other.tag");
            if (tag.isBlank()) {
                throw new IllegalArgumentException(
                        "CodeLanguage.other: blank tag — use CodeLanguage.NONE for unspecified");
            }
        }
        @Override public String toString() { return tag; }
    }

    // ── The vocabulary ───────────────────────────────────────────────────────

    /** Unspecified — renders without a {@code language-X} class, as {@code CodeSegment("")} does. */
    CodeLanguage NONE       = new Known("");

    CodeLanguage JAVA       = new Known("java");
    /** Both {@code js} and {@code javascript} in the corpus; one tag here. */
    CodeLanguage JAVASCRIPT = new Known("javascript");
    /** The one language a renderer branches on — see {@code MermaidPlateModule}. */
    CodeLanguage MERMAID    = new Known("mermaid");
    CodeLanguage BASH       = new Known("bash");
    CodeLanguage CSS        = new Known("css");
    CodeLanguage HTML       = new Known("html");
    CodeLanguage XML        = new Known("xml");
    CodeLanguage JSON       = new Known("json");
    CodeLanguage MARKDOWN   = new Known("md");
    CodeLanguage SVG        = new Known("svg");
    CodeLanguage CSV        = new Known("csv");
    /** Verbatim output — console transcripts, file trees, fixtures. */
    CodeLanguage TEXT       = new Known("text");

    /** Every named language, in the order the vocabulary declares them. */
    List<CodeLanguage> ALL = List.of(
            NONE, JAVA, JAVASCRIPT, MERMAID, BASH, CSS, HTML, XML, JSON, MARKDOWN, SVG, CSV, TEXT);

    /** A language this vocabulary does not name. Debt — see the class note. */
    static CodeLanguage other(String tag) { return new Other(tag); }
}
