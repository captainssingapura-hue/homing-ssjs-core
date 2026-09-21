package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;

import java.util.Set;

/**
 * A component that needs context menus says which, on its declaration:
 * the export record of a {@code UiComponent} implements this beside its
 * shape and names the kinds it opens —
 *
 * <pre>{@code
 * public record MultiTabPane() implements BranchComponent<MultiTabPaneModule>, NeedContextMenu {
 *     @Override public Set<ContextMenuKind<?>> required() { return Set.of(TabMenu.INSTANCE); }
 * }
 * }</pre>
 *
 * <p>That is the whole of registration from the component's side. A page
 * asks nothing of its components at runtime: the kinds it must hold are
 * {@link ContextMenuRegistry#requiredBy(java.util.List) derived} from the
 * components its crate closure catalogues, and its steward is given the
 * union — so a kind is declared once, by the crate that owns the component
 * that opens it, and a site that serves the component serves the menu.
 * What a pick <i>does</i> stays the page's handler.</p>
 *
 * <p>A mixin rather than a shape: {@code UiComponent} is sealed to its two
 * shapes in the component base, and a need is orthogonal to a shape — an
 * element or a branch may have one. {@link ContextMenuRegistry#validate}
 * refuses a need on something that is not a declared, catalogued
 * component, since a need nobody can find is a menu nobody registers.</p>
 */
public interface NeedContextMenu {

    /** The kinds this component opens; never empty. */
    Set<ContextMenuKind<?>> required();
}
