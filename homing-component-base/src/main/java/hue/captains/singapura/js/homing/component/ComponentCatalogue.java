package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A catalogue of components: the typed tree a delivery vehicle — a crate —
 * ships beside its modules, saying what it delivers as components rather
 * than as files. The shape is the studio catalogue's at a third of its
 * height: a stateless record per node with its {@code INSTANCE}, a
 * {@code parent()} that names the node above, sub-catalogues typed to the
 * next level down ({@link C0_Components} → {@link C1_Components} →
 * {@link C2_Components}, the last with no next), and leaves typed by their
 * host so a misplaced entry is a compile error. Identity is the class;
 * the segment is the class's name in kebab without its {@code Components}
 * suffix, so rewording {@link #name()} never moves a node.
 *
 * <p>Vehicles compose as studios do, and without writing it: a site's
 * catalogue is its own root with each required crate's catalogue grafted
 * under it, derived from the crate closure by {@link ComponentTrees}. A
 * component keeps its segment however it is reached.</p>
 *
 * @param <Self> the concrete catalogue's own type
 */
public sealed interface ComponentCatalogue<Self extends ComponentCatalogue<Self>>
        extends StatelessFunctionalObject permits C0_Components, C1_Components, C2_Components {

    /** What the node is called. */
    String name();

    /** One line on what is under it. */
    default String summary() { return ""; }

    /** The segment under the parent: the class without {@code Components}, in kebab; sibling-unique. */
    default NodeName segment() { return NodeName.ofType(getClass(), "Components"); }

    /** Sub-catalogues, typed to the next level by each level's interface. */
    default List<? extends ComponentCatalogue<?>> subCatalogues() { return List.of(); }

    /** The components listed here, typed by this host. */
    default List<ComponentEntry<Self>> leaves() { return List.of(); }
}
