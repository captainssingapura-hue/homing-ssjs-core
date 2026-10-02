package hue.captains.singapura.js.homing.studio.base.composed;

import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0019 — prose segment of a {@link ComposedDoc}. Body is markdown
 * (plain in the Phase 1 PoC; {@code .mdad} once RFC 0018 phases in and
 * the conformance scanner enforces the slim subset).
 *
 * <p>Optional title — when present, contributes to the doc's
 * server-rendered TOC as a level-2 entry above the segment. When absent,
 * the segment's TOC contribution is the heading hierarchy parsed out of
 * its body.</p>
 *
 * <p>Unlike visual segments (SvgSegment, etc.), the markdown body is
 * intrinsic to the composed doc — not a proxy to a separately-registered
 * ProseDoc. Markdown is the connective tissue between visual segments;
 * each MarkdownSegment is specific to its containing doc.</p>
 *
 * @param body  the markdown text
 * @param title optional segment title; contributes to TOC when present
 *
 * @since RFC 0019 Phase 1
 * @deprecated Signpost only (no removal yet): the raw-CommonMark escape hatch is
 *     being superseded by strict, directly-constructed block segments built on
 *     {@link hue.captains.singapura.js.homing.studio.base.composed.text.Line} —
 *     as {@link SimpleListSegment} already is for lists. Prefer the typed building
 *     blocks (and {@link RelationSegment} over a GFM table). Still fully functional.
 */
@Deprecated
public record MarkdownSegment(String body, Optional<String> title) implements Listable {
    public MarkdownSegment {
        Objects.requireNonNull(body,  "MarkdownSegment.body");
        Objects.requireNonNull(title, "MarkdownSegment.title (use Optional.empty)");
    }

    /** Convenience — no title, body only. */
    public MarkdownSegment(String body) {
        this(body, Optional.empty());
    }
}
