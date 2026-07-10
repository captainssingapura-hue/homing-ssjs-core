package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0024 P1c — proxy reference to a registered {@link ComposedDoc}
 * embedded inline within another {@link ComposedDoc}. The structural
 * analog of {@link SvgSegment} / {@link TableSegment} / {@link ImageSegment}
 * for the recursive case: a composed doc can appear inside a composed doc.
 *
 * <p>The recursion is realized in the JS renderer: each
 * {@code ComposedSegment} mounts a fresh sub-branch under the parent
 * doc's widget branch and recursively invokes the ComposedWidget
 * orchestrator's mount function with the nested doc's id. The recursion
 * threads a {@code __renderingStack} parameter forward; cycles are
 * detected when the same doc id appears twice in the stack.</p>
 *
 * <p>Caption rendering follows the same three-way rule as SvgSegment:
 * explicit override, doc title fallback, no caption when both are blank.</p>
 *
 * <h2>Cycle detection</h2>
 *
 * <p>The Java side does not enforce acyclicity at construction time —
 * ComposedDocs are typically built from leaf docs upward, so cycles are
 * rare in code-defined trees, but a cycle could be assembled by an
 * orchestrator that re-references its own ancestors. The JS renderer
 * handles the runtime check (cheap, localized to the recursion site);
 * structurally inserting a check in the Java record would require
 * tracking the full transitive embedding graph per doc, which the doc
 * registry doesn't index today.</p>
 *
 * @param doc             the registered ComposedDoc this segment embeds
 * @param captionOverride optional caption specific to this appearance
 *
 * @since RFC 0024 Phase P1c — recursive composedDoc support
 */
public record ComposedSegment(ComposedDoc doc, Optional<Line.Plain> captionOverride) implements Segment {
    public ComposedSegment {
        Objects.requireNonNull(doc,             "ComposedSegment.doc");
        Objects.requireNonNull(captionOverride, "ComposedSegment.captionOverride (use Optional.empty)");
    }

    /** Convenience — no caption override; falls through to the doc's title. */
    public ComposedSegment(ComposedDoc doc) {
        this(doc, Optional.empty());
    }

    /** Convenience — caption from a raw string (blank becomes no override). */
    public ComposedSegment(ComposedDoc doc, String caption) {
        this(doc, Line.optionalPlain(caption));
    }

    /** The caption to render — explicit override, or doc.title() when blank. */
    public String resolvedCaption() {
        return captionOverride.map(Line.Plain::raw).orElse(doc.title());
    }
}
