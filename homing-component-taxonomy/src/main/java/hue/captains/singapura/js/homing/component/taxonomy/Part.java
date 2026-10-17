package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * A part: a slot a component declares, with its owner appended when the taxonomy is read -
 * {@code Part(Confirmation, Confirm, [PlainButton], 1)}. A node of the forest like any other, so a
 * design can answer it; it falls back through what plays it, never its owner, and goes one level
 * deep: a part's own parts are its players'. Several may play it - one of several, or any under a branch, as
 * the taxonomy has them (D52) - the owner mounting one at a time, as many at once as its cardinality says.
 *
 * @param owner       the component that declares it
 * @param role        what it does for its owner
 * @param bases       the components that may play it, in the order declared: one, or several
 * @param cardinality how many the owner has at once
 * @param <O>         the owner
 * @param <E>         what plays it
 */
public record Part<O extends Component<?>, E extends Component<?>>(O owner, Role<?> role, List<E> bases, Cardinality cardinality)
        implements ComponentNode, ValueObject {

    public Part {
        Objects.requireNonNull(owner, "Part.owner");
        Objects.requireNonNull(role, "Part.role");
        Objects.requireNonNull(bases, "Part.bases");
        Objects.requireNonNull(cardinality, "Part.cardinality");
        bases = List.copyOf(bases);
        if (bases.isEmpty()) throw new IllegalArgumentException(owner.name().value() + "." + role.name().value() + " is played by nothing");
    }

    /** Whether one of several plays it: the owner chooses which. */
    public boolean alternatives() { return bases.size() > 1; }

    /** The role's name: {@code confirm}. */
    @Override public NodeName name() { return role.name(); }

    /** The owner's token and the role's name: {@code confirmation-confirm}. */
    @Override public String token() { return owner.token() + "-" + name().value(); }
}
