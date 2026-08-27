package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;
import hue.captains.singapura.tao.ontology.Immutable;

/**
 * Typed leaf entry inside a {@link Catalogue}. RFC 0011: parameterised by the
 * host catalogue's type {@code C}, so an {@code Entry<C>} can only appear in
 * {@code C}'s {@code leaves()} list. Misplaced entries become compile errors.
 *
 * <p>Three variants after RFC 0051 Phase 6:</p>
 *
 * <ul>
 *   <li>{@link OfLeaf} — a content leaf as a BINDING: the app that opens it,
 *       its typed params, and the doc it displays (or none, for a leaf that is
 *       purely an app).</li>
 *   <li>{@link OfStudio} — RFC 0011 cross-tree portal: a typed re-attachment
 *       of a source L0 catalogue via a {@link StudioProxy}. Structural —
 *       not content. The carve-out per the DocTree T4 ontology axiom.</li>
 *   <li>{@link OfIllustration} — in-place decoration; not addressable.</li>
 * </ul>
 *
 * <p><b>What Phase 6 removed.</b> {@code OfDoc} carried a Doc and nothing else,
 * so the framework had to ask the doc HOW it opens — through {@code url()} —
 * and a doc is the wrong thing to ask: the same {@code ComposedDoc} reasonably
 * opens in {@code composed-viewer} from a tile and in {@code doc-tree-viewer}
 * from a tree. With {@code OfDoc} went {@code AppDoc}, the wrapper a Navigable
 * had to become to be placeable at all, which paid for that with a uuid seeded
 * from its display framing — the defect that forced Law 1's second check.</p>
 *
 * <p>The generic factories — {@code Entry.of(host, …)} — take the host
 * catalogue as a type witness; the compiler infers {@code C} from the
 * {@code this} reference at the call site. The host argument is discarded
 * at runtime (the entry doesn't carry it back as data — it's known by
 * virtue of being in {@code host.leaves()}).</p>
 *
 * @param <C> the host catalogue's type
 * @since RFC 0005 (RFC 0005-ext1 OfPlan; RFC 0005-ext2 removed OfCatalogue;
 *        RFC 0011 typed by host + OfStudio; RFC 0015 Phase 6 collapsed OfApp /
 *        OfPlan into OfDoc; RFC 0051 Phase 6 replaced OfDoc with OfLeaf, the
 *        binding it should have been)
 */
public sealed interface Entry<C extends Catalogue<C>> extends Immutable {

    /**
     * RFC 0051 Phase 6 — a content leaf as the binding it always was: the app
     * that opens it, its typed params, and the content it displays.
     *
     * <p>{@link Navigable} already carries exactly the binding plus its tile
     * framing ({@code app, params, name, summary}), so nothing new was needed
     * to express this — only to stop routing around it.</p>
     *
     * <p>{@code content} is the Doc this leaf displays, or {@code null} for a
     * leaf that is purely an app (a themes page, a workspace). That nullability
     * IS the inversion: a leaf REFERENCES a doc rather than being one, so a
     * leaf without content is ordinary rather than a wrapper with a fabricated
     * identity — which is what {@code AppDoc} had to be.</p>
     *
     * @param nav     the app + its typed params, plus the tile's name/summary
     * @param content the doc displayed here, or null for a pure app leaf
     */
    record OfLeaf<C extends Catalogue<C>,
                  P extends AppModule._Param,
                  M extends AppModule<P, M>>
                 (Navigable<P, M> nav, Doc content, NodeName slug) implements Entry<C> {

        public OfLeaf {
            java.util.Objects.requireNonNull(nav,  "Entry.OfLeaf.nav");
            java.util.Objects.requireNonNull(slug, "Entry.OfLeaf.slug");
        }

    }

    /** RFC 0011 — a typed re-attachment of a source {@link L0_Catalogue} as a leaf
     *  in this catalogue's tree, via a {@link StudioProxy}. The proxy carries
     *  display fields plus a typed reference to the wrapped L0 INSTANCE;
     *  {@link CatalogueRegistry} uses it to augment breadcrumbs for any page
     *  reached via the source L0. */
    record OfStudio<C extends Catalogue<C>, S extends L0_Catalogue<S>>
                    (StudioProxy<S> proxy) implements Entry<C> {}

    /** A specialized in-place decoration leaf — markdown rendered above (or
     *  among) the catalogue's tile grid. Not addressable, not citable, not
     *  registered; exists only for the catalogue node it is placed in. */
    record OfIllustration<C extends Catalogue<C>>
                          (CatalogueIllustration illustration) implements Entry<C> {}

    // -----------------------------------------------------------------------
    // Convenience factories — `host` is a type witness for inference, discarded.
    // -----------------------------------------------------------------------

    /**
     * RFC 0051 Phase 6 — place {@code doc}, opened by {@code app} with
     * {@code params}. The placement NAMES the app, which is the whole point:
     * a doc does not know how it is viewed, and the same doc may be placed
     * twice under different viewers — two bindings, two positions, which the
     * path axiom permits because identity is {@code (app, params)}.
     *
     * <p>Tile framing comes from the doc — its title, summary and category —
     * so the emitted tile is unchanged from the {@code OfDoc} form.</p>
     */
    static <C extends Catalogue<C>,
            P extends AppModule._Param,
            M extends AppModule<P, M>>
           Entry<C> of(C host, M app, P params, Doc doc) {
        return new OfLeaf<>(
                new Navigable<>(app, params, doc.title(), doc.summary()),
                doc, DocSlugs.defaultFor(doc));
    }

    /**
     * Place {@code doc} under an explicitly named segment.
     *
     * <p>The form that makes the model true: a placement states where the doc
     * sits AND what that position is called. It is what fixes a collision the
     * document cannot see — four SvgDocs sharing the class-derived "svg" — and
     * it is how an authored name reaches the path without the doc minting one.</p>
     */
    static <C extends Catalogue<C>,
            P extends AppModule._Param,
            M extends AppModule<P, M>>
           Entry<C> of(C host, M app, P params, Doc doc, NodeName slug) {
        return new OfLeaf<>(
                new Navigable<>(app, params, doc.title(), doc.summary()), doc, slug);
    }

    /** Place an app: a leaf with a binding and no content. */
    static <C extends Catalogue<C>,
            P extends AppModule._Param,
            M extends AppModule<P, M>>
           Entry<C> of(C host, Navigable<P, M> nav) {
        // RFC 0051 Phase 6 — no wrapper. A Navigable IS the binding, so an app
        // leaf is one with no content, and AppDoc's whole reason for existing
        // goes: it was a Doc a navigable had to become in order to be
        // placeable, and it paid for that with a fabricated uuid seeded from
        // display framing — the defect that forced Law 1's second check.
        return new OfLeaf<>(nav, null, NodeName.conciseSlug(nav.name()));
    }

    /** Place a plan. */
    static <C extends Catalogue<C>, P extends Plan>
           Entry<C> of(C host, P plan) {
        // RFC 0051 Phase 6 — the app is named HERE rather than at each of the
        // placements, and that is not the doc-determines-its-viewer mistake in
        // disguise. A doc has a choice of viewers, so the choice belongs to
        // whoever places it. A Plan does not: PlanAppHost is the definition of
        // what hosts a plan, and its params are the plan's class because that
        // is what /plan is keyed by — which is also why PlanDoc's synthesised
        // uuid addresses nothing.
        var doc = new hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc(plan);
        var host_ = hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost.INSTANCE;
        return new OfLeaf<>(
                new Navigable<>(host_,
                        new hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost.Params(
                                plan.getClass().getName(), null),
                        doc.title(), doc.summary()),
                doc, DocSlugs.defaultFor(doc));
    }

    static <C extends Catalogue<C>, S extends L0_Catalogue<S>>
           Entry<C> of(C host, StudioProxy<S> proxy) {
        return new OfStudio<>(proxy);
    }

    /** Place a {@link CatalogueIllustration} as a decoration leaf in this catalogue. */
    static <C extends Catalogue<C>>
           Entry<C> of(C host, CatalogueIllustration illustration) {
        return new OfIllustration<>(illustration);
    }
}
