package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.AtMost;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Fixed;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Unbounded;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Varying;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A role asked to be played by a component - {@code Title.INSTANCE.playedBy(Heading.INSTANCE)} -
 * and not yet a slot: a component's parts are slots only, so a casting that is never told how
 * many does not compile. Each verb says how many, and makes the slot.
 *
 * <table>
 *   <caption>The verbs</caption>
 *   <tr><td>{@code one()}</td><td>{@code 1}</td></tr>
 *   <tr><td>{@code exactly(n)}</td><td>{@code n}</td></tr>
 *   <tr><td>{@code optional()}</td><td>{@code 0..1}</td></tr>
 *   <tr><td>{@code atMost(n)}</td><td>{@code 0..n}</td></tr>
 *   <tr><td>{@code any()}</td><td>{@code 0..*}</td></tr>
 *   <tr><td>{@code atLeast(n)}</td><td>{@code n..*}</td></tr>
 *   <tr><td>{@code between(m, n)}</td><td>{@code m..n}</td></tr>
 * </table>
 *
 * @param role the role
 * @param base the component that plays it; none is refused when the taxonomy is read
 * @param <E>  the component that plays it
 */
public record Casting<E extends Component<?>>(Role<?> role, E base) implements ValueObject {

    public Casting {
        Objects.requireNonNull(role, "Casting.role");
    }

    /** Exactly one, from the start. */
    public Slot<E> one() { return exactly(1); }

    /** Exactly {@code n}, from the start. */
    public Slot<E> exactly(int n) { return slot(new Fixed(n)); }

    /** None, or one. */
    public Slot<E> optional() { return atMost(1); }

    /** None, up to {@code n}. */
    public Slot<E> atMost(int n) { return slot(new Varying(0, new AtMost(n))); }

    /** As many as the owner holds, none included. */
    public Slot<E> any() { return atLeast(0); }

    /** {@code n} or more. */
    public Slot<E> atLeast(int n) { return slot(new Varying(n, Unbounded.INSTANCE)); }

    /** From {@code m} up to {@code n}. */
    public Slot<E> between(int m, int n) { return slot(new Varying(m, new AtMost(n))); }

    private Slot<E> slot(Cardinality cardinality) { return new Slot<>(role, base, cardinality); }
}
