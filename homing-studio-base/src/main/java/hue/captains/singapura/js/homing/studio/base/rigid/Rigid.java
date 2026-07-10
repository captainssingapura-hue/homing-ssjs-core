package hue.captains.singapura.js.homing.studio.base.rigid;

import hue.captains.singapura.js.homing.studio.base.SvgDoc;
import hue.captains.singapura.js.homing.studio.base.composed.CodeSegment;
import hue.captains.singapura.js.homing.studio.base.composed.ImageSegment;
import hue.captains.singapura.js.homing.studio.base.composed.MarkdownSegment;
import hue.captains.singapura.js.homing.studio.base.composed.RelationSegment;
import hue.captains.singapura.js.homing.studio.base.composed.RigidSegment;
import hue.captains.singapura.js.homing.studio.base.composed.SvgSegment;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegment;
import hue.captains.singapura.js.homing.studio.base.composed.text.Line;
import hue.captains.singapura.js.homing.studio.base.composed.text.Title;
import hue.captains.singapura.js.homing.studio.base.image.ImageDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0042 — the leveled tree-builder DSL for a {@link RigidDoc}. One builder
 * type per level ({@link L0}…{@link L6}), mirroring DomOpsParty's hardcoded
 * {@code L0…L18} level classes (RFC 0024/0040) — now for <i>authoring</i> the
 * content tree. Each level exposes only its legal moves, so the compiler
 * catches the silly mistakes and the source reads as the tree:
 *
 * <pre>{@code
 * RigidDoc DOC = RigidDoc.root(uuid, "Title", "summary", "DEMO")
 *     .l1("Section A")
 *         .text("lead-in prose")
 *         .l2("A.1")
 *             .code("x = 1", "java")
 *         .l2build()
 *     .l1build()
 *     .build();
 * }</pre>
 *
 * <ul>
 *   <li>{@code .lN(title)} opens a node one level down (the number IS the level).</li>
 *   <li>{@code text/markdown/code/relation/svg/image/segment} attach content to
 *       the <i>current</i> node and return the same builder, so content chains.</li>
 *   <li>{@code .lNbuild()} closes the current node and returns its parent; the
 *       chain must close level-by-level.</li>
 *   <li>{@code .build()} exists only on {@link L0} — an unclosed level can't
 *       reach it. There is no {@code .l7()} on {@link L6}: the depth cap is a
 *       fact of the type system, not a runtime check (this build caps at
 *       {@code L6}; the ladder extends to {@code L18} by the same template,
 *       a code-gen target per RFC 0042).</li>
 * </ul>
 *
 * @since homing-studio-base — RFC 0042 leveled tree-builder
 */
public final class Rigid {

    private Rigid() {}

    /**
     * Shared base — the content methods and node accumulation every level needs.
     * {@code SELF} is the concrete level type, so content methods return it and
     * the fluent chain stays at the current level.
     */
    public abstract static class Level<SELF extends Level<SELF>> {

        private final Title         title;
        private final List<RigidSegment> content  = new ArrayList<>();
        private final List<DocNode> children = new ArrayList<>();

        Level(String title) {
            // Wrap the authoring String into a typed Title at the boundary
            // (non-blank + ≤ Title.MAX_CHARS is enforced here).
            this.title = new Title(title);
        }

        abstract SELF self();

        /** Attach a {@code .mdad+} prose paragraph to the current node. */
        public SELF text(String body) { content.add(new TextSegment(body)); return self(); }

        /** Attach a CommonMark markdown block to the current node. */
        public SELF markdown(String body) { content.add(new MarkdownSegment(body)); return self(); }

        /** Attach a verbatim code listing (language for the {@code language-X} class). */
        public SELF code(String body, String language) { content.add(new CodeSegment(body, language)); return self(); }

        /** Attach a typed table (header row + body rows + optional caption). */
        public SELF relation(List<String> headers, List<List<String>> rows, String caption) {
            content.add(new RelationSegment(headers, rows, Line.optionalPlain(caption)));
            return self();
        }

        /** Attach a registered {@link SvgDoc} inline; caption falls through to the doc's title. */
        public SELF svg(SvgDoc<?> doc) { content.add(new SvgSegment(doc)); return self(); }

        /** Attach a registered {@link SvgDoc} inline with a per-appearance caption override. */
        public SELF svg(SvgDoc<?> doc, String caption) { content.add(new SvgSegment(doc, caption)); return self(); }

        /** Attach a registered {@link ImageDoc} inline; caption falls through to the doc's caption/alt. */
        public SELF image(ImageDoc doc) { content.add(new ImageSegment(doc)); return self(); }

        /** Attach a registered {@link ImageDoc} inline with a per-appearance caption override. */
        public SELF image(ImageDoc doc, String caption) { content.add(new ImageSegment(doc, caption)); return self(); }

        /** Attach any pre-built {@link RigidSegment} (the full constructor surface). */
        public SELF segment(RigidSegment s) { content.add(Objects.requireNonNull(s, "segment")); return self(); }

        void addChild(DocNode n) { children.add(n); }

        DocNode toNode() { return new DocNode(title, content, children); }
    }

    /** The document root (L0) — opens {@code l1} children and {@code build()}s the doc. */
    public static final class L0 extends Level<L0> {
        private final UUID   uuid;
        private final String summary;
        private final String category;
        L0(UUID uuid, String title, String summary, String category) {
            super(title);
            this.uuid     = Objects.requireNonNull(uuid, "RigidDoc.uuid");
            this.summary  = (summary  == null) ? "" : summary;
            this.category = (category == null) ? "DOC" : category;
        }
        @Override L0 self() { return this; }
        public L1 l1(String title) { return new L1(this, title); }
        public RigidDoc build() { return new RigidDoc(uuid, summary, category, toNode()); }
    }

    /** Level 1. */
    public static final class L1 extends Level<L1> {
        private final L0 parent;
        L1(L0 parent, String title) { super(title); this.parent = parent; }
        @Override L1 self() { return this; }
        public L2 l2(String title) { return new L2(this, title); }
        public L0 l1build() { parent.addChild(toNode()); return parent; }
    }

    /** Level 2. */
    public static final class L2 extends Level<L2> {
        private final L1 parent;
        L2(L1 parent, String title) { super(title); this.parent = parent; }
        @Override L2 self() { return this; }
        public L3 l3(String title) { return new L3(this, title); }
        public L1 l2build() { parent.addChild(toNode()); return parent; }
    }

    /** Level 3. */
    public static final class L3 extends Level<L3> {
        private final L2 parent;
        L3(L2 parent, String title) { super(title); this.parent = parent; }
        @Override L3 self() { return this; }
        public L4 l4(String title) { return new L4(this, title); }
        public L2 l3build() { parent.addChild(toNode()); return parent; }
    }

    /** Level 4. */
    public static final class L4 extends Level<L4> {
        private final L3 parent;
        L4(L3 parent, String title) { super(title); this.parent = parent; }
        @Override L4 self() { return this; }
        public L5 l5(String title) { return new L5(this, title); }
        public L3 l4build() { parent.addChild(toNode()); return parent; }
    }

    /** Level 5. */
    public static final class L5 extends Level<L5> {
        private final L4 parent;
        L5(L4 parent, String title) { super(title); this.parent = parent; }
        @Override L5 self() { return this; }
        public L6 l6(String title) { return new L6(this, title); }
        public L4 l5build() { parent.addChild(toNode()); return parent; }
    }

    /** Level 6 — the current depth cap (no {@code l7}; extends to L18 by template). */
    public static final class L6 extends Level<L6> {
        private final L5 parent;
        L6(L5 parent, String title) { super(title); this.parent = parent; }
        @Override L6 self() { return this; }
        public L5 l6build() { parent.addChild(toNode()); return parent; }
    }
}
