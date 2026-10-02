package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.site.mpa.Mpa;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A vertex of the catalogue tree: a named place with sub-catalogues under it
 * and leaves in it. The tree's shape is in the types — a catalogue is one of
 * {@link L0_Catalogue} through {@link L8_Catalogue}, a level-{@code n}
 * catalogue's parent is a level-{@code n-1} catalogue and its sub-catalogues
 * are level {@code n+1}, so a vertex cannot be declared at a depth its type
 * does not allow, and the root is the one kind with no parent.
 *
 * <p>Both directions are declared: a parent lists its sub-catalogues, in the
 * order they are shown, and every sub-catalogue names its parent. They must
 * agree, and {@link CatalogueTree} refuses a tree in which they do not.
 * Stating the edge twice is what lets a catalogue be read on its own — the
 * type says where it sits — while the parent keeps the say over order.</p>
 *
 * <p>Trees compose by {@link #grafts()}: a catalogue places another tree's
 * root under itself, and that edge is declared once, by the host, so a tree
 * written by a module that knows no site can be grafted wherever a site
 * wants it. The types say where a vertex sits within its own tree; the
 * composed tree says where it sits in the site.</p>
 *
 * <p>A catalogue is a stateless singleton, identified by its class. Its slug
 * — the path segment it answers to — derives from the class name, so
 * {@code RfcsCatalogue} is {@code rfcs}; override {@link #slug()} to lock
 * the address independently of the name.</p>
 *
 * @param <Self> the implementing type, so a leaf can be typed to its host
 */
public sealed interface Catalogue<Self extends Catalogue<Self>>
        extends StatelessFunctionalObject
        permits L0_Catalogue,
                L1_Catalogue, L2_Catalogue, L3_Catalogue, L4_Catalogue,
                L5_Catalogue, L6_Catalogue, L7_Catalogue, L8_Catalogue {

    /** The display name. */
    String name();

    /** The path segment; from the class name minus {@code Catalogue} unless overridden. */
    default NodeName slug() {
        return NodeName.ofType(getClass(), "Catalogue");
    }

    default String summary() { return ""; }

    /** A short upper-case tag for a listing to show; {@code CATALOGUE} by default. */
    default String badge() { return "CATALOGUE"; }

    /** An emoji or glyph for a listing to show; empty for none. */
    default String icon() { return ""; }

    /** The catalogues under this one, in display order. Each level narrows the type. */
    default List<? extends Catalogue<?>> subCatalogues() { return List.of(); }

    /**
     * Whole trees placed under this one - other trees' roots, each written on
     * its own and grafted here, in display order after the sub-catalogues. The
     * edge is declared here alone: a grafted root names no parent, and so can
     * be grafted wherever a site wants it.
     */
    default List<Graft<Self>> grafts() { return List.of(); }

    /** The pages in this catalogue, in display order - pages that need no site to be made. */
    default List<Leaf<Self>> leaves() { return List.of(); }

    /**
     * The pages in this catalogue, made with the MPA of the site serving it -
     * {@code mpa.page(app, params)} - so that every page of a site, whichever
     * tree placed it, wears the site's one chrome, themes and preferences. A
     * catalogue stays a stateless singleton that knows no site: the site reads
     * its tree with its MPA ({@link CatalogueTree#of(L0_Catalogue, Mpa)}) and
     * the MPA is handed in here. Unless overridden, the pages that need no
     * site: {@link #leaves()}.
     */
    default List<Leaf<Self>> leaves(Mpa mpa) { return leaves(); }
}
