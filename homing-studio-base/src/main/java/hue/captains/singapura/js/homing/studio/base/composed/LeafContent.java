package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * RFC 0041 — the polymorphic content payload a {@link ContentProvider} yields
 * for one node of a doc rigid tree. Content is <i>not</i> mandated to be one
 * shape: a node may carry a flat segment bundle, an external SVG addressed by
 * its own stable URL, a bare image, or a future kind — each binds through the
 * same {@code node -> ContentProvider -> LeafContent} seam.
 *
 * <p><b>Sealed on purpose.</b> The doc-tree serializer ({@link DocTreeJsonWriter})
 * dispatches over this type with an exhaustive switch, so a new leaf-content kind
 * cannot be added without the writer (and, in turn, the renderer) being forced to
 * handle it — <i>Make It Impossible</i>, not "remember to update the writer".</p>
 *
 * <p>{@link ComposedLeaf} (a flat {@link Segment} bundle) is the first and
 * currently only kind; RFC 0041 anticipates further kinds slotting in here.</p>
 *
 * @since homing-studio-base — RFC 0041 structure vs content
 */
public sealed interface LeafContent permits ComposedLeaf {
}
