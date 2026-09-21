package hue.captains.singapura.js.homing.ui.menu.tree;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A node of a context menu's tree — the kind at its root, a row beneath —
 * as a class: one class, one node, its identity the class itself, its id
 * the class's simple name in kebab case. A menu is declared as a nest of
 * stateless records, each with its {@code INSTANCE}, each naming its
 * parent and listing its children; the levels are typed
 * ({@link ContextMenuKind} → {@link M1_Node} → {@link M2_Node} →
 * {@link M3_Node}), so the compiler refuses a child at the wrong depth or
 * under the wrong parent, and the third level is the last: a context menu
 * is shallow by construction.
 *
 * <p>This package imports nothing of the JS stack — the vocabulary's
 * {@code Icon} words and the JDK — so a menu declared here is validated
 * ({@link MenuTrees#validate}) and walked by any stack that renders it;
 * what a pick does is never here, it is the kind's handler at runtime.</p>
 *
 * @param <Self> the concrete node's own type (CRTP)
 */
public sealed interface MenuNode<Self extends MenuNode<Self>> extends StatelessFunctionalObject permits ContextMenuKind, MenuRow {

    /** The node's id: its class's simple name in kebab case — {@code ChangeAnimal} → {@code change-animal}, {@code S10} → {@code s10}. */
    default String id() { return MenuTrees.kebab(getClass().getSimpleName()); }

    /** The rows beneath, in order; empty at a leaf. Typed to the next level by each level's interface. */
    List<? extends MenuRow<?, ?>> children();
}
