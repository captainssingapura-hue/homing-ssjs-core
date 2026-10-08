package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A part: a slot a component declares, with its owner appended when the taxonomy is read -
 * {@code Part(Confirmation, Confirm, PlainButton, 1)}. A node of the forest like any other, so a
 * design can answer it; it falls back through its base, never its owner, and goes one level
 * deep: a part's own parts are its base's.
 *
 * @param owner       the component that declares it
 * @param role        what it does for its owner
 * @param base        the component that plays it
 * @param cardinality how many the owner has
 * @param <O>         the owner
 * @param <E>         the base
 */
public record Part<O extends Component<?>, E extends Component<?>>(O owner, Role<?> role, E base, Cardinality cardinality)
        implements ComponentNode, ValueObject {

    public Part {
        Objects.requireNonNull(owner, "Part.owner");
        Objects.requireNonNull(role, "Part.role");
        Objects.requireNonNull(base, "Part.base");
        Objects.requireNonNull(cardinality, "Part.cardinality");
    }

    /** The role's name: {@code confirm}. */
    @Override public NodeName name() { return role.name(); }

    /** The owner's token and the role's name: {@code confirmation-confirm}. */
    @Override public String token() { return owner.token() + "-" + name().value(); }
}
