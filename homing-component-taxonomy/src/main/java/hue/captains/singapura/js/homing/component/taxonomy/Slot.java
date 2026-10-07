package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * One of a component's parts, as the component declares it: a role, the component that plays it,
 * and how many. Said in the {@link ComponentPartDSL} -
 * {@code dsl.part(Heading.INSTANCE).as(Title.INSTANCE).one()}; reading the taxonomy appends the
 * owner and makes it a {@link Part}.
 *
 * @param role        the role it plays; none is refused when the taxonomy is read
 * @param base        the component that plays it; none is refused when the taxonomy is read
 * @param cardinality how many
 * @param <E>         the component that plays it
 */
public record Slot<E extends Component<?>>(Role<?> role, E base, Cardinality cardinality) implements ValueObject {

    public Slot {
        Objects.requireNonNull(cardinality, "Slot.cardinality");
    }

    @Override public String toString() {
        return (role == null ? "no role" : role.name()) + " → " + (base == null ? "nothing" : base.name()) + " " + cardinality.multiplicity();
    }
}
