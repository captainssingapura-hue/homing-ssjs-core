package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * One of a component's parts, as the component declares it: a role, the component that plays it,
 * and how many - and the part's own states and overrides of their levels, since a part is a
 * top-level thing in state and in design. Said in the {@link ComponentPartDSL} -
 * {@code DSL.part(Heading.INSTANCE).as(Title.INSTANCE).one()}; reading the taxonomy appends the
 * owner and makes it a {@link Part}.
 *
 * @param role        the role it plays; none is refused when the taxonomy is read
 * @param base        the component that plays it; none is refused when the taxonomy is read
 * @param cardinality how many
 * @param states      the part's own states, in the order listed; none unless the slot says
 * @param importance  the part's own levels for some of them
 * @param <E>         the component that plays it
 */
public record Slot<E extends Component<?>>(Role<?> role, E base, Cardinality cardinality,
                                           List<StateType<?>> states, List<ImportanceOverride<?>> importance) implements ValueObject {

    public Slot {
        Objects.requireNonNull(cardinality, "Slot.cardinality");
        states = states == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(states));   // a null is the reader's to refuse
        importance = importance == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(importance));
    }

    /** A slot whose part has no states of its own. */
    public Slot(Role<?> role, E base, Cardinality cardinality) { this(role, base, cardinality, List.of(), List.of()); }

    @Override public String toString() {
        return (role == null ? "no role" : role.name()) + " → " + (base == null ? "nothing" : base.name()) + " " + cardinality.multiplicity();
    }
}
