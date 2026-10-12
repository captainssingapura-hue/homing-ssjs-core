package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * A design as {@link ReadDesigns} read it: complete over its branch, a concrete leaf for every leaf of
 * the branch, in the taxonomy's order.
 *
 * @param design    the design
 * @param branch    the design branch it designs
 * @param concretes its concrete leaves, one for each leaf of the branch, in the branch's order
 */
public record DesignRead(Design<?> design, ComponentBranch branch, List<ConcreteComponent<?, ?, ?>> concretes) implements ValueObject {

    public DesignRead {
        Objects.requireNonNull(design, "DesignRead.design");
        Objects.requireNonNull(branch, "DesignRead.branch");
        concretes = List.copyOf(concretes);
    }

    /** Its name: {@code minesweeper-classic}. */
    public String token() { return design.token(); }

    /** Its concrete leaf of a leaf of its branch. */
    public ConcreteComponent<?, ?, ?> of(VirtualComponent<?> leaf) {
        for (ConcreteComponent<?, ?, ?> c : concretes) if (c.virtual().equals(leaf)) return c;
        throw new IllegalArgumentException(token() + " draws no " + leaf.token() + ": it is no leaf of " + branch.token());
    }
}
