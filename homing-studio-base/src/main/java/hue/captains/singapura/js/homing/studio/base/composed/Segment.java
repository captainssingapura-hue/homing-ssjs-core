package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * RFC 0019 — one element of a {@link ComposedDoc}'s body. Sealed sum;
 * each variant carries its own typed fields (pure ADT — no common base
 * beyond this marker; dispatch via exhaustive switch).
 *
 * <p>Phase 1 permitted two variants ({@link MarkdownSegment} +
 * {@link SvgSegment}); Phase 3 extends the permits list with
 * {@link TableSegment} (wraps {@code TableDoc}) and {@link ImageSegment}
 * (wraps {@code ImageDoc}) so RFC 0020's visual Docs flow inline:</p>
 *
 * <ul>
 *   <li>{@link MarkdownSegment} — prose body ({@code .mdad} once RFC
 *       0018 phases in; plain markdown for now).</li>
 *   <li>{@link SvgSegment} — proxy reference to a registered
 *       {@code SvgDoc} + optional per-appearance caption override.</li>
 *   <li>{@link TableSegment} — proxy reference to a registered
 *       {@code TableDoc} + optional per-appearance caption override.</li>
 *   <li>{@link ImageSegment} — proxy reference to a registered
 *       {@code ImageDoc} + optional per-appearance caption override.</li>
 * </ul>
 *
 * <p>Future visual kinds extend the permits list via the same
 * sealed-permits-with-exhaustive-dispatch pattern.</p>
 *
 * <p>Segments are independent — no cross-segment references (RFC 0019
 * §3.4). Citation works across segments because it targets Docs, not
 * segments.</p>
 *
 * @since RFC 0019 Phase 1 (extended Phase 3 with table + image variants)
 */
public sealed interface Segment extends ValueObject
        permits MarkdownSegment, TextSegment, CodeSegment,
                SvgSegment, TableSegment, ImageSegment,
                ComposedSegment,
                DocumentaryWidget {
}
