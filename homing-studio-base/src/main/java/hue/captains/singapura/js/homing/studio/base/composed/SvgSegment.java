package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.SvgDoc;
import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0019 — proxy reference to a registered {@link SvgDoc} embedded
 * inline within a {@link ComposedDoc}.
 *
 * <p>The canonical artifact is the SvgDoc — registered, addressable,
 * citable, themable (per RFC 0017). This segment wraps it with an
 * optional per-appearance caption override; the SVG itself isn't
 * duplicated, the segment is a thin reference.</p>
 *
 * <p>Generalises the {@code ProxyDoc} pattern (RFC 0015 §2.5) to inline
 * visual appearances: same canonical content, fresh framing per
 * appearance, no duplication. The same SvgDoc may appear inline in
 * multiple ComposedDocs, each with its own caption.</p>
 *
 * <p>Caption rendering:</p>
 * <ul>
 *   <li>{@code captionOverride.present} → use the override</li>
 *   <li>{@code captionOverride.empty} and {@code doc.title()} non-blank → use the doc's title</li>
 *   <li>otherwise → no caption rendered</li>
 * </ul>
 *
 * @param doc             the registered SvgDoc this segment references
 * @param captionOverride optional caption specific to this appearance
 *
 * @since RFC 0019 Phase 1
 */
public record SvgSegment(SvgDoc<?> doc, Optional<Line.Plain> captionOverride) implements Listable {
    public SvgSegment {
        Objects.requireNonNull(doc,             "SvgSegment.doc");
        Objects.requireNonNull(captionOverride, "SvgSegment.captionOverride (use Optional.empty)");
    }

    /** Convenience — no caption override; falls through to the SvgDoc's title. */
    public SvgSegment(SvgDoc<?> doc) {
        this(doc, Optional.empty());
    }

    /** Convenience — caption from a raw string (blank becomes no override). */
    public SvgSegment(SvgDoc<?> doc, String caption) {
        this(doc, Line.optionalPlain(caption));
    }

    /** The caption to render — explicit override, or doc.title() when blank, or empty. */
    public String resolvedCaption() {
        return captionOverride.map(Line.Plain::raw).orElse(doc.title());
    }
}
