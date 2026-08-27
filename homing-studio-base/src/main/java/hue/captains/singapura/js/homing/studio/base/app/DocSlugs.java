package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocV2;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc;

/**
 * RFC 0051 Phase 6 — how a doc's default path segment is derived.
 *
 * <p>A segment is where a thing SITS, so it is the placement's to state, not
 * the document's. {@link Entry.OfLeaf} carries one; this supplies the default
 * a placement gets when it does not name its own.</p>
 *
 * <p>The four rules below are today's rules, moved rather than changed. That
 * is deliberate and it is the whole safety argument: every existing URL in
 * both studios is one of these strings, so re-deriving them differently — from
 * the title, say, which reads better — would silently rewrite the lot.</p>
 *
 * <p>Consulted at CONSTRUCTION, when a leaf is built, never per request.</p>
 *
 * @since RFC 0051 Phase 6
 */
public final class DocSlugs {

    private DocSlugs() {}

    /** The segment {@code doc} gets when its placement does not name one. */
    public static NodeName defaultFor(Doc doc) {
        java.util.Objects.requireNonNull(doc, "doc");

        // A RigidDoc may carry an AUTHORED name — the author saying "call this
        // rfc0015-inventory". That is the doc's own datum; turning it into a
        // path segment is the framework's job, which is the split this class
        // exists to make.
        if (doc instanceof RigidDoc rd && rd.authoredSlug() != null) {
            return rd.authoredSlug();
        }
        // A ComposedDoc carries its own too, as a record component defaulting
        // to conciseSlug(title) — which is why two of them can sit in one
        // catalogue at all. Missing this was caught by Law 2 within seconds:
        // both container doctrines fell back to the class-derived "composed",
        // since their INSTANCE fields are plain ComposedDocs rather than
        // distinct classes.
        if (doc instanceof hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc cd
                && cd.slug() != null) {
            return cd.slug();
        }
        if (doc instanceof RigidDoc)   return NodeName.conciseSlug(doc.title());
        if (doc instanceof RigidDocV2) return NodeName.conciseSlug(doc.title());
        // A plan is identified by its class, and its segment follows — the same
        // reason /plan is keyed by class rather than by PlanDoc's uuid.
        if (doc instanceof PlanDoc pd) return NodeName.ofType(pd.plan().getClass(), "Plan");
        // The common case: one class, one leaf. DemoIntroDoc -> "demo-intro".
        return NodeName.ofType(doc.getClass(), "Doc");
    }
}
