package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;
import hue.captains.singapura.js.homing.studio.base.NoOwnContentException;
import hue.captains.singapura.js.homing.studio.base.Reference;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

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
public final class RigidDoc implements Doc, hue.captains.singapura.js.homing.studio.base.AuthoredName {

    private final UUID     uuid;
    private final String   summary;
    private final String   category;
    private final DocNode  root;
    private final NodeName slug;
    private final Supplier<List<Reference>> references;

    RigidDoc(UUID uuid, String summary, String category, DocNode root) {
        this(uuid, summary, category, root, null, List::of);
    }

    private RigidDoc(UUID uuid, String summary, String category, DocNode root, NodeName slug, Supplier<List<Reference>> references) {
        this.uuid     = Objects.requireNonNull(uuid, "RigidDoc.uuid");
        this.summary  = (summary  == null) ? "" : summary;
        this.category = (category == null) ? "DOC" : category;
        this.root     = Objects.requireNonNull(root, "RigidDoc.root");
        this.slug     = slug;
        this.references = Objects.requireNonNull(references, "RigidDoc.references");
    }

    /**
     * RFC 0051 — this doc with an authored path segment. A wither rather than
     * a wrapper, for the same reason as {@code ComposedDoc.withSlug}: the
     * viewers dispatch on the concrete type, so identity has to be attached
     * without changing what the value is.
     */
    public RigidDoc withSlug(NodeName authored) {
        Objects.requireNonNull(authored, "RigidDoc.withSlug(authored)");
        return new RigidDoc(uuid, summary, category, root, authored, references);
    }

    /**
     * The same doc, declaring the references it cites - each a name, and what it names. Declared
     * lazily: the docs named are read when the references are asked for, never when this doc is
     * made. So two docs that name each other can both be made; an eager list would read the other's
     * constant while it is still being made, and hold null.
     */
    public RigidDoc withReferences(Supplier<List<Reference>> references) {
        Objects.requireNonNull(references, "RigidDoc.withReferences(references)");
        return new RigidDoc(uuid, summary, category, root, slug, references);
    }

    /** Open the leveled builder at the document root (L0). */
    public static Rigid.L0 root(UUID uuid, String title, String summary, String category) {
        return new Rigid.L0(uuid, title, summary, category);
    }

    /**
     * Build a {@code RigidDoc} directly from a pre-assembled {@link DocNode} tree.
     * The leveled DSL ({@link Rigid}) is a convenience over this; a caller that
     * already has an arbitrary tree (e.g. transformed from another source) builds
     * the {@link DocNode}s itself and wraps them here. The doc title is the root
     * node's title.
     */
    public static RigidDoc fromNode(UUID uuid, String summary, String category, DocNode root) {
        return new RigidDoc(uuid, summary, category, root);
    }

    /** The root structure node (its title is the doc title). */
    public DocNode root() { return root; }

    // ── Doc protocol ──────────────────────────────────────────────────────
    @Override public UUID    uuid()        { return uuid; }
    @Override public DocId   id()          { return new DocId.ByUuid(uuid); }
    @Override public String  title()       { return root.title().text(); }
    /** RFC 0051 Law 2 — the authored segment when one was supplied, else the
     *  title: every RigidDoc shares this class, so the class-derived default
     *  would give them all the segment "rigid". */
    /** The author-chosen name, or null. RFC 0051 Phase 6 — the doc carries the
     *  NAME; DocSlugs decides how a name becomes a path segment. */
    @Override public NodeName authoredSlug() { return slug; }
    @Override public String  summary()     { return summary; }
    @Override public String  category()    { return category; }
    /** The references it declares ({@link #withReferences}), read now: none, unless it declares some. */
    @Override public List<Reference> references() { return List.copyOf(references.get()); }
    @Override public String  kind()        { return "composed"; }   // reuses the doc-tree route
    @Override public String  contentType() { throw new NoOwnContentException(this); }
    @Override public String  fileExtension() { return ""; }

    /** A rigid doc's data is its tree, not text: content is made from it (the studio's {@code LegacyDocWire}, via the doc-tree writer). */
    @Override public String contents() { throw new NoOwnContentException(this); }
}
