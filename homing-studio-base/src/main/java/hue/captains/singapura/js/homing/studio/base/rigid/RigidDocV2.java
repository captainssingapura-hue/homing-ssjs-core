package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2JsonWriter;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2Source;
import hue.captains.singapura.js.homing.studio.base.composed.RigidNodeContent;
import hue.captains.singapura.js.homing.studio.base.composed.graph.RigidNode;
import hue.captains.singapura.js.homing.studio.base.composed.graph.RigidNodeNormalizer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The name-path successor to {@link RigidDoc} (RFC 0039): a {@link Doc} whose
 * structure and content come from a "bring your own tree" {@link TreeGraph} —
 * a flat set of nodes + edges the {@link TreeGraphNormalizer} assembles and
 * validates into a {@link DocTreeV2}. Content is addressed by <b>name-path</b>
 * (a stable nodeName-chain), not the fragile child-index path {@code RigidDoc}
 * serves.
 *
 * <p>The tree is built <b>lazily</b> (first request, cached) via a supplier, so a
 * doc that mirrors a source it also belongs to can defer the walk to request time
 * — dodging a class-init cycle and always reflecting the live source. It routes to
 * the same {@code doc-tree-viewer} as {@code RigidDoc} (kind {@code "composed"}),
 * and the endpoints recognise it through {@link DocTreeV2Source}.</p>
 *
 * @since homing-studio-base — RFC 0039 name-path doc (RigidDocV2)
 */
public final class RigidDocV2 implements Doc, DocTreeV2Source {

    private final UUID id;
    private final String title;
    private final String summary;
    private final String category;
    private final Supplier<DocTreeV2> builder;

    private volatile DocTreeV2 cached;

    public RigidDocV2(UUID id, String title, String summary, String category,
                      Supplier<DocTreeV2> builder) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.summary = summary == null ? "" : summary;
        this.category = category == null ? "" : category;
        this.builder = Objects.requireNonNull(builder, "builder");
    }

    /**
     * Build a {@code RigidDocV2} from a supplier of parent-pointer {@link RigidNode}s
     * plus the single {@code content} provider that resolves each node's body from
     * its {@code source}. The normalizer assembles + validates the flat node list at
     * first request; the {@code <T>} source type is captured here and never escapes.
     */
    public static <T> RigidDocV2 fromNodes(UUID id, String title, String summary, String category,
                                           Supplier<List<RigidNode<T>>> nodes,
                                           Function<T, RigidNodeContent> content) {
        Objects.requireNonNull(nodes, "nodes");
        Objects.requireNonNull(content, "content provider");
        return new RigidDocV2(id, title, summary, category,
                () -> RigidNodeNormalizer.INSTANCE.toDocTree(nodes.get(), content));
    }

    @Override
    public DocTreeV2 toDocTreeV2() {
        DocTreeV2 t = cached;
        if (t == null) {
            t = builder.get();
            cached = t;
        }
        return t;
    }

    // ── Doc protocol ──────────────────────────────────────────────────────────
    @Override public UUID   uuid()        { return id; }
    @Override public String title()       { return title; }
    @Override public String summary()     { return summary; }
    @Override public String category()    { return category; }
    @Override public String kind()        { return "composed"; }   // reuses the doc-tree route
    @Override public String url()         { return "/app?app=doc-tree-viewer&id=" + id; }
    @Override public String contentType() { return "application/json; charset=utf-8"; }
    @Override public String fileExtension() { return ""; }

    @Override public String contents() {
        return DocTreeV2JsonWriter.INSTANCE.write(toDocTreeV2(), id.toString());
    }
}
