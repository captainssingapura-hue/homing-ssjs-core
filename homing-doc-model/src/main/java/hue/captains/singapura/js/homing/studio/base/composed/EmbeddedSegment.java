package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * A segment whose content is outside the document model: something interactive,
 * embedded in a {@link ComposedDoc} and shown under a caption. The model knows only
 * that it is there and what it is called; what it is, and how it runs, belongs to the
 * layer that implements it - {@code DocumentaryWidget}, an ES-module app embedded by
 * the studio, is the one there is.
 *
 * <p>Open, where the rest of {@link Segment} is sealed: the model cannot name what it
 * does not depend on. Valid in a flat {@link ComposedDoc} only, never in a rigid tree.</p>
 */
public non-sealed interface EmbeddedSegment extends Segment {

    /** The caption shown above the embed - an explicit one, or the embed's own title. */
    String resolvedCaption();
}
