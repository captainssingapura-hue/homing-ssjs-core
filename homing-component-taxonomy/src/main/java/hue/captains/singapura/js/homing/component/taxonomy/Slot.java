package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * One of a component's parts, as the component declares it: a role, who plays it - one component, one of several, or any
 * under a branch (D52) - and how many. Said in the {@link ComponentPartDSL} -
 * {@code DSL.part(Heading.INSTANCE).as(Title.INSTANCE).one()}; reading the taxonomy appends the
 * owner and makes it a {@link Part}.
 *
 * @param role        the role it plays; none is refused when the taxonomy is read
 * @param players     who plays it; nothing is refused when the taxonomy is read
 * @param cardinality how many it holds at once, whichever plays
 * @param <E>         what plays it, as the DSL was told
 */
public record Slot<E extends Component<?>>(Role<?> role, Players players, Cardinality cardinality) implements ValueObject {

    public Slot {
        Objects.requireNonNull(players, "Slot.players");
        Objects.requireNonNull(cardinality, "Slot.cardinality");
    }

    @Override public String toString() {
        return (role == null ? "no role" : role.name()) + " → " + players.say() + " " + cardinality.multiplicity();
    }
}
