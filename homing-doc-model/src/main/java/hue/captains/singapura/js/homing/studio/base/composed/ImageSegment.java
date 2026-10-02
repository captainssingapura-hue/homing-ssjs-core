package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;
import hue.captains.singapura.js.homing.studio.base.image.ImageDoc;

import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0019 Phase 3 + RFC 0020 — proxy reference to a registered
 * {@link ImageDoc} embedded inline within a {@link ComposedDoc}.
 *
 * <p>Mirrors {@link SvgSegment} / {@link TableSegment}: the canonical
 * artifact is the ImageDoc — registered, addressable, citable. This
 * segment wraps it with an optional per-appearance caption override; the
 * image bytes themselves aren't duplicated, the segment is a thin
 * reference.</p>
 *
 * <p>Caption rendering:</p>
 * <ul>
 *   <li>{@code captionOverride.present} → use the override</li>
 *   <li>{@code captionOverride.empty} → fall back to {@code doc.summary()}
 *       (ImageDoc.summary returns its caption field) if non-blank,
 *       else {@code doc.alt()}.</li>
 * </ul>
 *
 * @param doc             the registered ImageDoc this segment references
 * @param captionOverride optional caption specific to this appearance
 *
 * @since RFC 0019 Phase 3
 */
public record ImageSegment(ImageDoc doc, Optional<Line.Plain> captionOverride) implements Listable {
    public ImageSegment {
        Objects.requireNonNull(doc,             "ImageSegment.doc");
        Objects.requireNonNull(captionOverride, "ImageSegment.captionOverride (use Optional.empty)");
    }

    /** Convenience — no caption override; falls through to the ImageDoc's caption/alt. */
    public ImageSegment(ImageDoc doc) {
        this(doc, Optional.empty());
    }

    /** Convenience — caption from a raw string (blank becomes no override). */
    public ImageSegment(ImageDoc doc, String caption) {
        this(doc, Line.optionalPlain(caption));
    }

    /** The caption to render — explicit override, or doc.summary() (its caption), or alt. */
    public String resolvedCaption() {
        return captionOverride.map(Line.Plain::raw).orElseGet(() -> {
            String cap = doc.summary();
            return (cap == null || cap.isBlank()) ? doc.alt() : cap;
        });
    }
}
