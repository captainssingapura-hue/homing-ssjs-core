package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * RFC 0039 — the per-node content seam for a doc rigid tree. A structure node
 * (RFC 0040 {@code NormalizedNode}) carries <b>no</b> body; content is a
 * separate projection from the node's position: {@code node -> ContentProvider},
 * and the provider yields the node's polymorphic content.
 *
 * <p>This is the doc-scale twin of the abstraction already in use elsewhere:
 * the catalogue leaf's provider is its {@code Doc} ({@code doc.contents()});
 * leveled Open resolves a path to a doc's bytes. Here the provider yields a
 * {@link Segment} — the polymorphic content unit (Markdown / Text / Code /
 * Table / Svg / Image / Widget). A {@link ComposedSegment} never flows through
 * a provider: nesting is <i>structure</i> (a graft), not content, so a provider
 * always yields a non-{@code ComposedSegment} segment.</p>
 *
 * <p>Delivery is inline-mostly (RFC 0039): the server resolves each provider
 * and bakes the content into one payload. The single exception is immutable
 * external binary resources (pictures, external SVG files), which a provider
 * surfaces as a descriptor plus the resource's own stable URL rather than
 * inlined bytes.</p>
 *
 * @since homing-studio-base — RFC 0039 rigid-tree doc
 */
@FunctionalInterface
public interface ContentProvider {

    /**
     * The content this node renders — a polymorphic {@link LeafContent}
     * (RFC 0041), not mandated to any one shape. Today the only kind is a
     * {@link ComposedLeaf} (a flat segment bundle): a {@code ComposedDoc} node
     * yields a singleton bundle; a {@code RigidDoc} (RFC 0042) leaf yields its
     * whole bundle. Future kinds (an external SVG / image addressed by its own
     * stable URL) bind here unchanged. Where the payload is a bundle, no element
     * is ever a {@link ComposedSegment} (nesting is structure, a graft — not
     * content).
     */
    LeafContent content();
}
