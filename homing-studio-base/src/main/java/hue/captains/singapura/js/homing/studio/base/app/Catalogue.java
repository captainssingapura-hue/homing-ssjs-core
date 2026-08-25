package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A typed, ordered container of children — typed sub-catalogues and typed
 * leaf entries (Docs, Plans, Apps). Per the
 * <a href="../../../../../../../../../../docs/doctrines/CatalogueContainerDoc.md">
 * Catalogues as Containers</a> doctrine, a catalogue is structure only — it
 * carries no URLs, no presentation directives, no per-entry decoration.
 * Identity is the implementing Java class itself; presentation is the
 * renderer's responsibility.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/Rfc0005Doc.md">RFC 0005</a>
 * and the typed-levels refinement in
 * <a href="../../../../../../../../../../docs/rfcs/Rfc0005Ext2Doc.md">RFC 0005-ext2</a>,
 * a catalogue declaration is canonically a stateless record:</p>
 *
 * <pre>{@code
 * public record DoctrineCatalogue() implements L1_Catalogue<StudioCatalogue> {
 *     public static final DoctrineCatalogue INSTANCE = new DoctrineCatalogue();
 *
 *     @Override public StudioCatalogue parent() { return StudioCatalogue.INSTANCE; }
 *     @Override public String name()           { return "Doctrines"; }
 *     @Override public String summary()        { return "The rules that hold the design together."; }
 *
 *     // Sub-catalogues are typed by their L<N+1> level. Empty default
 *     // inherited from L1_Catalogue when a level has no further nesting.
 *
 *     @Override public List<Entry> leaves() {
 *         return List.of(
 *                 Entry.of(PureComponentViewsDoc.INSTANCE),
 *                 Entry.of(MethodsOverPropsDoc.INSTANCE),
 *                 Entry.of(ManagedDomOpsDoc.INSTANCE),
 *                 Entry.of(OwnedReferencesDoc.INSTANCE)
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p>Sub-catalogue children flow through {@link #subCatalogues()}; each level
 * interface narrows that method's return type to the next level down, so the
 * compiler refuses wrong-depth or wrong-parent-type children. Leaves flow
 * through {@link #leaves()}; the {@link Entry} sum is strictly for those
 * (Doc, Plan, App) and contains no catalogue variant.</p>
 *
 * <p>The class — {@code DoctrineCatalogue} — is the catalogue's identity.
 * Other artifacts reference this catalogue by importing the class and using
 * {@code DoctrineCatalogue.INSTANCE}. URLs are derived by the framework at
 * render time; no catalogue ever constructs a URL.</p>
 *
 * @since RFC 0005 (sub-catalogue / leaf split: RFC 0005-ext2)
 */
public sealed interface Catalogue<Self extends Catalogue<Self>>
        extends StatelessFunctionalObject
        permits L0_Catalogue,
                L1_Catalogue, L2_Catalogue, L3_Catalogue, L4_Catalogue,
                L5_Catalogue, L6_Catalogue, L7_Catalogue, L8_Catalogue {

    /**
     * Human-readable label identifying this catalogue. Used as the page heading and
     * in tile listings by parent catalogues.
     */
    String name();

    /**
     * RFC 0051 Law 2 — this catalogue's URL path segment, unique among its
     * siblings. Derived from the class minus a trailing {@code "Catalogue"}
     * ({@code DoctrineCatalogue} becomes {@code "doctrine"}), so rewording
     * {@link #name()} never moves a page.
     *
     * <p>Override when the derived segment is long or unhelpful; the boot
     * check only requires that siblings differ.</p>
     */
    default NodeName slug() {
        return NodeName.ofType(getClass(), "Catalogue");
    }

    default String summary() { return ""; }

    /**
     * RFC 0009 — text label flowing into the parent catalogue's card-tile
     * {@code category} JSON field. Default {@code "CATALOGUE"}.
     */
    default String badge() { return "CATALOGUE"; }

    /**
     * RFC 0009 — short glyph prefixed into the breadcrumb crumb. Default empty.
     */
    default String icon() { return ""; }

    /**
     * Typed sub-catalogue children — each level interface narrows the return
     * type to the next level down. RFC 0005-ext2.
     */
    default List<? extends Catalogue<?>> subCatalogues() { return List.of(); }

    /**
     * Leaf entries, typed by host (this catalogue's {@code Self}). RFC 0011
     * — an {@code Entry<Self>} can only be constructed for this catalogue,
     * so misplaced entries become compile errors.
     */
    default List<Entry<Self>> leaves() { return List.of(); }
}
