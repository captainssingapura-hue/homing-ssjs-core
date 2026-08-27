package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppAddress;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.ProxyDoc;
import hue.captains.singapura.js.homing.studio.base.SvgDoc;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedViewer;
import hue.captains.singapura.js.homing.studio.base.image.ImageDoc;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewer;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocV2;
import hue.captains.singapura.js.homing.studio.base.table.TableDoc;
import hue.captains.singapura.js.homing.studio.base.table.TableViewer;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc;

/**
 * RFC 0051 Phase 6 — which app opens a given Doc, as the structured
 * {@link AppAddress} pair.
 *
 * <p>This is the DEFAULT designation only. A catalogue entry names the app it
 * places a doc under, because that is a placement decision: the same
 * {@code ComposedDoc} reasonably opens in {@code composed-viewer} from a tile
 * and in {@code doc-tree-viewer} from a tree, and under {@code (app, args)}
 * identity those are two navigables, two positions — which Law 1 permits,
 * correctly, because they are two different beings. This table answers only
 * "and if the placement does not say?".</p>
 *
 * <h3>Why not keyed on {@code kind()}</h3>
 *
 * <p>Because {@code kind() -> viewer} is not a function, and the codebase
 * proves it: {@code ComposedDoc}, {@code RigidDoc} and {@code RigidDocV2} all
 * answer {@code "composed"}, while the first opens in {@code composed-viewer}
 * and the other two in {@code doc-tree-viewer}. RFC 0015's {@code
 * ContentViewer} registry is kind-keyed and has no entry for
 * {@code doc-tree-viewer} at all, so consuming it would have routed every
 * RigidDoc to the wrong viewer. That is why it was built and never used.</p>
 *
 * <p>Keyed on the Doc's TYPE instead, which is a function by construction, and
 * ordered most-specific-first so subtype relationships resolve correctly.</p>
 *
 * @since RFC 0051 Phase 6
 */
public final class DocViewers {

    private DocViewers() {}

    /**
     * The default address for {@code doc} — exactly what {@code doc.url()}
     * returns today, structured rather than serialised.
     *
     * <p>Asserted byte-identical to {@code doc.url()} over every registered doc
     * in both studios by {@code DocAddressLaw}; that gate is what lets the
     * consumers switch over without the flat-address index shifting under
     * them, since the index is KEYED on those strings.</p>
     */
    public static AppAddress addressOf(Doc doc) {
        java.util.Objects.requireNonNull(doc, "doc");
        String uuid = doc.uuid().toString();

        // Order matters: RigidDocV2 and RigidDoc before the composed default,
        // and ProxyDoc before the prose default (it addresses its OWN uuid,
        // never its target's).
        if (doc instanceof RigidDocV2) {
            return AppAddress.of("doc-tree-viewer", DocTreeViewer.CODEC,
                    new DocTreeViewer.Params(uuid));
        }
        if (doc instanceof RigidDoc) {
            return AppAddress.of("doc-tree-viewer", DocTreeViewer.CODEC,
                    new DocTreeViewer.Params(uuid));
        }
        if (doc instanceof ComposedDoc) {
            return AppAddress.of("composed-viewer", ComposedViewer.CODEC,
                    new ComposedViewer.Params(uuid));
        }
        if (doc instanceof SvgDoc<?>) {
            return AppAddress.of("svg-viewer", SvgViewer.CODEC,
                    new SvgViewer.Params(uuid));
        }
        if (doc instanceof TableDoc) {
            return AppAddress.of("table-viewer", TableViewer.CODEC,
                    new TableViewer.Params(uuid));
        }
        if (doc instanceof ImageDoc) {
            return AppAddress.of("image-viewer", ImageViewer.CODEC,
                    new ImageViewer.Params(uuid));
        }
        if (doc instanceof PlanDoc pd) {
            // A plan is identified by its CLASS, not by the uuid PlanDoc
            // synthesises to satisfy Doc — which nothing addresses. One of the
            // negation's exhibits.
            return AppAddress.of("plan", PlanAppHost.CODEC,
                    new PlanAppHost.Params(pd.plan().getClass().getName(), null));
        }
        if (doc instanceof ProxyDoc) {
            return AppAddress.of("doc-reader", DocReader.CODEC,
                    new DocReader.Params(uuid));
        }
        return AppAddress.of("doc-reader", DocReader.CODEC, new DocReader.Params(uuid));
    }

    /** Parse a flat URL back into the pair. Used only for the AppDoc branch. */
    private static AppAddress fromFlat(String flatUrl) {
        var args = new java.util.LinkedHashMap<>(QueryString.parse(flatUrl));
        String app = QueryString.first(args, "app");
        args.remove("app");
        return new AppAddress(app, args);
    }
}
