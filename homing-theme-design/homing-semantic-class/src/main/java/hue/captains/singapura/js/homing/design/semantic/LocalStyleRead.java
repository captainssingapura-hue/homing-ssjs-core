package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * A local style as {@link ReadLocalStyles} read it: complete over its branch, a concrete leaf for every leaf of the
 * branch, in the taxonomy's order.
 *
 * @param localStyle the local style
 * @param branch     the local-style branch it styles
 * @param concretes  its concrete leaves, one for each leaf of the branch, in the branch's order
 * @param taxonomy   the taxonomy it was read over
 */
public record LocalStyleRead(LocalStyle<?, ?> localStyle, ComponentBranch branch, List<ConcreteComponent<?, ?, ?>> concretes, Taxonomy taxonomy)
        implements ValueObject {

    public LocalStyleRead {
        Objects.requireNonNull(localStyle, "LocalStyleRead.localStyle");
        Objects.requireNonNull(branch, "LocalStyleRead.branch");
        concretes = List.copyOf(concretes);
        Objects.requireNonNull(taxonomy, "LocalStyleRead.taxonomy");
    }

    /** Its name: {@code minesweeper-classic}. */
    public String token() { return localStyle.token(); }

    /** The leaves of its branch, in the taxonomy's order. */
    public List<VirtualComponent<?>> leaves() { return concretes.stream().<VirtualComponent<?>>map(ConcreteComponent::virtual).toList(); }

    /** Its concrete leaf of a leaf of its branch. */
    public ConcreteComponent<?, ?, ?> of(VirtualComponent<?> leaf) {
        for (ConcreteComponent<?, ?, ?> c : concretes) if (c.virtual().equals(leaf)) return c;
        throw new IllegalArgumentException(token() + " draws no " + leaf.token() + ": it is no leaf of " + branch.token());
    }

    /** The targets a leaf of its branch owns, in the order it says them; none, for a leaf that says none. */
    public List<Target> owns(VirtualComponent<?> leaf) {
        if (!branch.equals(leaf.parent())) throw new IllegalArgumentException(leaf.token() + " is no leaf of " + branch.token());
        return leaf instanceof Owns o && o.owns() != null ? List.copyOf(o.owns()) : List.of();
    }
}
