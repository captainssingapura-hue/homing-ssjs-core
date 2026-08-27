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
 * <p>Sealed to two variants after RFC 0015 Phase 6:</p>
 *
 * <ul>
 *   <li>{@link OfDoc} — a content leaf bearing any {@link Doc} subtype
 *       (prose Doc, PlanDoc, AppDoc, ProxyDoc, or downstream Doc kinds).</li>
 *   <li>{@link OfStudio} — RFC 0011 cross-tree portal: a typed re-attachment
 *       of a source L0 catalogue via a {@link StudioProxy}. Structural —
 *       not content. The carve-out per the DocTree T4 ontology axiom.</li>
 * </ul>
 *
 * <p>The previous {@code OfApp} and {@code OfPlan} variants are gone (RFC 0015
 * Phase 6). Plans and Navigables are now wrapped at the factory boundary into
 * {@link hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc PlanDoc}
 * and {@link AppDoc}, both Doc subtypes carried by {@code OfDoc}. The factory
 * signatures preserve the old call shapes — {@code Entry.of(this, somePlan)}
 * and {@code Entry.of(this, someNavigable)} keep compiling and produce the
 * Doc-wrapped form transparently.</p>
 *
 * <p>The generic factories — {@code Entry.of(host, target)} — take the host
 * catalogue as a type witness; the compiler infers {@code C} from the
 * {@code this} reference at the call site. The host argument is discarded
 * at runtime (the entry doesn't carry it back as data — it's known by
 * virtue of being in {@code host.leaves()}).</p>
 *
 * @param <C> the host catalogue's type
 * @since RFC 0005 (RFC 0005-ext1 OfPlan; RFC 0005-ext2 removed OfCatalogue;
 *        RFC 0011 typed by host + OfStudio variant for cross-tree composition;
 *        RFC 0015 Phase 6 collapsed OfApp / OfPlan into OfDoc-wrapped Doc subtypes)
 */
public sealed interface Entry<C extends Catalogue<C>> extends Immutable {

    /** A content leaf bearing any {@link Doc} — prose, plan, app, proxy, …
     *
     *  <p><b>RFC 0051 Phase 6 — being replaced by {@link OfLeaf}.</b> This
     *  variant records only WHAT is placed, never HOW it opens, so the framework
     *  had to ask the doc — through {@code url()} — and a doc is the wrong thing
     *  to ask, because the same {@code ComposedDoc} reasonably opens in
     *  {@code composed-viewer} from a tile and in {@code doc-tree-viewer} from a
     *  tree. Both remain during the migration; new placements use
     *  {@code OfLeaf}.</p> */
    record OfDoc<C extends Catalogue<C>, D extends Doc>
                 (D doc) implements Entry<C> {}

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
                 (Navigable<P, M> nav, Doc content) implements Entry<C> {

        public OfLeaf {
            java.util.Objects.requireNonNull(nav, "Entry.OfLeaf.nav");
        }

        /**
         * This leaf's path segment. RFC 0051 Phase 6 — the slug belongs to the
         * PLACEMENT, which is what finally makes it fixable: every
         * {@code SvgDoc} inherits the class-derived slug {@code "svg"}, so four
         * demo animals derived one colliding path while the slug was the
         * document's to state.
         *
         * <p>Reproduces today's rule exactly, so no path moves: a doc leaf
         * takes the doc's slug, an app leaf takes its tile name — the same
         * two rules {@code Doc.slug()} and {@code AppDoc.slug()} apply now.</p>
         */
        public NodeName slug() {
            return content != null
                    ? content.slug()
                    : NodeName.conciseSlug(nav.name());
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
    // RFC 0015 Phase 6: Plan and Navigable factories transparently wrap into
    // PlanDoc / AppDoc and emit OfDoc; the call-site syntax is unchanged.
    // -----------------------------------------------------------------------

    static <C extends Catalogue<C>, D extends Doc>
           Entry<C> of(C host, D doc) {
        return new OfDoc<>(doc);
    }

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
                new Navigable<>(app, params, doc.title(), doc.summary()), doc);
    }

    /**
     * RFC 0015 Phase 3b — wraps the Navigable in an {@link AppDoc} and emits
     * {@link OfDoc}. Phase 6 removed the standalone {@code OfApp} variant.
     */
    static <C extends Catalogue<C>,
            P extends AppModule._Param,
            M extends AppModule<P, M>>
           Entry<C> of(C host, Navigable<P, M> nav) {
        return new OfDoc<>(new AppDoc<>(nav));
    }

    /**
     * RFC 0015 Phase 3b — wraps the Plan in a
     * {@link hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc PlanDoc}
     * and emits {@link OfDoc}. Phase 6 removed the standalone {@code OfPlan} variant.
     */
    static <C extends Catalogue<C>, P extends Plan>
           Entry<C> of(C host, P plan) {
        return new OfDoc<>(new hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc(plan));
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
