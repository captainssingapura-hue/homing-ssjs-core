package hue.captains.singapura.js.homing.ui.menu.tree;

import java.util.List;

/**
 * The root of a context menu's tree: a kind. Its class is named
 * {@code <Kind>Menu} and its kind is the rest in kebab case —
 * {@code AnimalMenu} → {@code animal} — the name a component opens it by
 * and a handler is bound to. Its children are first-level rows,
 * {@link M1_Node}s whose parent is this kind.
 *
 * <pre>{@code
 * public record AnimalMenu() implements ContextMenuKind<AnimalMenu> {
 *     public static final AnimalMenu INSTANCE = new AnimalMenu();
 *     @Override public List<? extends M1_Node<AnimalMenu, ?>> children() { return List.of(Rotate.INSTANCE, Change.INSTANCE); }
 *
 *     public record Rotate() implements M1_Node<AnimalMenu, Rotate> {
 *         public static final Rotate INSTANCE = new Rotate();
 *         @Override public AnimalMenu parent() { return AnimalMenu.INSTANCE; }
 *         @Override public String label() { return "Rotate"; }
 *         @Override public Class<? extends Icon> icon() { return Icon.Rotate.class; }
 *     }
 *     …
 * }
 * }</pre>
 *
 * @param <Self> the concrete kind's own type
 */
public non-sealed interface ContextMenuKind<Self extends ContextMenuKind<Self>> extends MenuNode<Self> {

    /** The kind: the class's simple name without its {@code Menu} suffix, in kebab case. */
    default String kind() { return MenuTrees.kindOf(getClass()); }
    /** A kind's id is its kind. */
    @Override default String id() { return kind(); }

    @Override List<? extends M1_Node<Self, ?>> children();
}
