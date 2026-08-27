package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppAddress;
import hue.captains.singapura.js.homing.core.AppModule;
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
     * The default address for {@code doc} — the encoded rendering of
     * {@link #navOf(Doc)}, for callers that need an href.
     */
    public static AppAddress addressOf(Doc doc) {
        return addressOfNav(navOf(doc));
    }

    /**
     * RFC 0051 Phase 6 — the same designation as {@link #addressOf(Doc)}, but
     * as the typed pair it was built from rather than the string it was
     * encoded into.
     *
     * <p>Every branch below already CONSTRUCTS typed params and then hands
     * them to a codec. The encode was never the answer, only the shape the
     * flat index happened to want. The addressing index is keyed on the typed
     * pair now, so callers that consult it take this, and only callers that
     * emit an href take {@link #addressOf(Doc)}.</p>
     *
     * <p>Name and summary come from the doc, which is what a navigable for it
     * would say — this is a Doc's default binding, stated in full.</p>
     */
    public static Navigable<?, ?> navOf(Doc doc) {
        java.util.Objects.requireNonNull(doc, "doc");
        String uuid = doc.uuid().toString();

        // Order matters: RigidDocV2 and RigidDoc before the composed default,
        // and ProxyDoc before the prose default (it addresses its OWN uuid,
        // never its target's).
        if (doc instanceof RigidDocV2) {
            return bind(DocTreeViewer.INSTANCE, new DocTreeViewer.Params(uuid), doc);
        }
        if (doc instanceof RigidDoc) {
            return bind(DocTreeViewer.INSTANCE, new DocTreeViewer.Params(uuid), doc);
        }
        if (doc instanceof ComposedDoc) {
            return bind(ComposedViewer.INSTANCE, new ComposedViewer.Params(uuid), doc);
        }
        if (doc instanceof SvgDoc<?>) {
            return bind(SvgViewer.INSTANCE, new SvgViewer.Params(uuid), doc);
        }
        if (doc instanceof TableDoc) {
            return bind(TableViewer.INSTANCE, new TableViewer.Params(uuid), doc);
        }
        if (doc instanceof ImageDoc) {
            return bind(ImageViewer.INSTANCE, new ImageViewer.Params(uuid), doc);
        }
        if (doc instanceof PlanDoc pd) {
            // A plan is identified by its CLASS, not by the uuid PlanDoc
            // synthesises to satisfy Doc — which nothing addresses. One of the
            // negation's exhibits.
            return bind(PlanAppHost.INSTANCE,
                    new PlanAppHost.Params(pd.plan().getClass().getName(), null), doc);
        }
        if (doc instanceof ProxyDoc) {
            return bind(DocReader.INSTANCE, new DocReader.Params(uuid), doc);
        }
        return bind(DocReader.INSTANCE, new DocReader.Params(uuid), doc);
    }

    /**
     * Bind an app to params, naming the pair from the doc. Generic so the
     * app's {@code P} and the params must agree at the call site — the check
     * a string address could never make.
     */
    private static <P extends AppModule._Param, M extends AppModule<P, M>>
            Navigable<P, M> bind(M app, P params, Doc doc) {
        String name = (doc.title() == null || doc.title().isBlank()) ? "doc" : doc.title();
        return new Navigable<>(app, params, name, doc.summary());
    }

    /** Encode a binding for an outbound href — the one legitimate direction. */
    private static <P extends AppModule._Param> AppAddress addressOfNav(Navigable<P, ?> nav) {
        return new AppAddress(nav.app().simpleName(), nav.app().paramCodec().to(nav.params()));
    }
}
