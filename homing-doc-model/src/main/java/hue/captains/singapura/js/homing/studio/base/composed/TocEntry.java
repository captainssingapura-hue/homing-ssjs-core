package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * RFC 0019 — one entry in a {@link ComposedDoc}'s server-rendered
 * table of contents.
 *
 * <p>Built at serialisation time by walking the segment list: each
 * non-prose segment contributes a level-2 entry from its caption; each
 * MarkdownSegment additionally contributes level-1..level-4 entries
 * extracted from its body's heading lines.</p>
 *
 * @param level  heading depth (1–4 typical)
 * @param text   the heading text
 * @param anchor target anchor id (segment slug for visuals; heading slug for markdown)
 *
 * @since RFC 0019 Phase 1
 */
public record TocEntry(int level, String text, String anchor) implements ValueObject {
    public TocEntry {
        Objects.requireNonNull(text,   "TocEntry.text");
        Objects.requireNonNull(anchor, "TocEntry.anchor");
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("TocEntry.level must be 1-6; got " + level);
        }
    }
}
