package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeJsonWriter;

import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0042 — a rigid-tree document: {@code ComposedDoc}'s successor. Where a
 * {@code ComposedDoc} is a flat list of segments, a {@code RigidDoc} is a tree
 * of titled {@link DocNode}s authored through the leveled builder ({@link Rigid}),
 * so it nests — and therefore folds (RFC 0039 + the in-sync TOC fold) at every
 * level, not just as a whole.
 *
 * <p>It is a {@link Doc} of kind {@code "composed"}, so the Navigator's Open and
 * the {@code SingleWidgetWorkspace} route it to the same {@code doc-tree-widget}
 * a {@code ComposedDoc} uses; the difference is purely the structure the
 * normalizer emits ({@link RigidDocNormalizer}). {@code ComposedDoc} is
 * untouched — the two coexist (RFC 0041 discipline).</p>
 *
 * <p>Build one with the leveled DSL:</p>
 * <pre>{@code
 * RigidDoc.root(uuid, "Title", "summary", "DEMO")
 *     .l1("Section").text("…").l1build()
 *     .build();
 * }</pre>
 *
 * @since homing-studio-base — RFC 0042 leveled tree-builder
 */
public final class RigidDoc implements Doc {

    private final UUID    uuid;
    private final String  summary;
    private final String  category;
    private final DocNode root;

    RigidDoc(UUID uuid, String summary, String category, DocNode root) {
        this.uuid     = Objects.requireNonNull(uuid, "RigidDoc.uuid");
        this.summary  = (summary  == null) ? "" : summary;
        this.category = (category == null) ? "DOC" : category;
        this.root     = Objects.requireNonNull(root, "RigidDoc.root");
    }

    /** Open the leveled builder at the document root (L0). */
    public static Rigid.L0 root(UUID uuid, String title, String summary, String category) {
        return new Rigid.L0(uuid, title, summary, category);
    }

    /** The root structure node (its title is the doc title). */
    public DocNode root() { return root; }

    // ── Doc protocol ──────────────────────────────────────────────────────
    @Override public UUID    uuid()        { return uuid; }
    @Override public DocId   id()          { return new DocId.ByUuid(uuid); }
    @Override public String  title()       { return root.title(); }
    @Override public String  summary()     { return summary; }
    @Override public String  category()    { return category; }
    @Override public String  kind()        { return "composed"; }   // reuses the doc-tree route
    @Override public String  url()         { return "/app?app=doc-tree-viewer&id=" + uuid; }
    @Override public String  contentType() { return "application/json; charset=utf-8"; }
    @Override public String  fileExtension() { return ""; }

    /**
     * The doc rigid-tree payload ({@code {structure, content}}) — the same shape
     * {@code /doc-tree} serves. A {@code RigidDoc} skips the legacy flat
     * {@code /doc} viewer entirely; this keeps {@code contents()} a sensible,
     * parse-clean byte source for any generic caller.
     */
    @Override public String contents() {
        return DocTreeJsonWriter.INSTANCE.write(RigidDocNormalizer.INSTANCE.toDocTree(this));
    }
}
