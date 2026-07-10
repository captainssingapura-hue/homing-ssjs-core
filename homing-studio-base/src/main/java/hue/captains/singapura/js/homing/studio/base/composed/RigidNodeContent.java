package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The content a provider yields for one rigid-tree node — an optional
 * {@code caption} (a highlighted header) plus an ordered bundle of
 * {@link RigidSegment}s.
 *
 * <p><b>Not a {@link Segment}.</b> "A list of segments" <i>is</i> a node's
 * content, not a segment — a segment that held a bundle would be doing the node's
 * job. This is the former {@code SimpleListSegment}, demoted from a Segment to
 * the node-content wrapper the content provider returns.</p>
 *
 * <p>The {@code caption} is content-level metadata (rendered as the highlighted
 * header above the body), distinct from the node's <i>title</i> (the structural
 * heading). It's a single {@link Line.Plain} — captions carry no further
 * formatting.</p>
 *
 * <p>Provider-layer only: a caller assembles these into the existing
 * {@code DocNode} / {@code RigidDoc} (the node's {@code caption} and
 * {@code content} come from this wrapper's {@link #caption()} / {@link #segments()}).</p>
 *
 * @param caption  optional highlighted header for the node; {@code empty} for none
 * @param segments the node's content segments, in order; may be empty (a pure branch)
 */
public record RigidNodeContent(Optional<Line.Plain> caption, List<RigidSegment> segments) {

    public RigidNodeContent {
        Objects.requireNonNull(caption,  "RigidNodeContent.caption (use Optional.empty)");
        Objects.requireNonNull(segments, "RigidNodeContent.segments");
        segments = List.copyOf(segments);
    }

    /** Convenience — no caption. */
    public RigidNodeContent(List<RigidSegment> segments) {
        this(Optional.empty(), segments);
    }

    /** Convenience — no caption, from varargs. */
    public static RigidNodeContent of(RigidSegment... segments) {
        return new RigidNodeContent(List.of(segments));
    }

    /** Convenience — with a caption (blank/null → none), from varargs. */
    public static RigidNodeContent captioned(String caption, RigidSegment... segments) {
        return new RigidNodeContent(Line.optionalPlain(caption), List.of(segments));
    }
}
