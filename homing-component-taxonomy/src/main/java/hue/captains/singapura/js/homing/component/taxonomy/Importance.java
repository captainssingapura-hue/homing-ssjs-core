package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * How important a state is, as a type (RFC 0067, appendix Two Classes per Leaf): six levels, {@link Primary} the most
 * important, {@link Senary} the least. When an element is in two states at once and both set one property, the more
 * important wins. Every type of state implements one level, an intrinsic property of the state, on the tree of
 * states: {@code record Selected() implements Flag<Picking>, Importance.Tertiary}. A level names itself as its type
 * argument, and a class cannot implement this interface twice with different arguments - so a state of two levels does
 * not compile; one of none is refused when the taxonomy is read. A component or a part may override a state's level for
 * itself ({@link ImportanceOverride}).
 *
 * @param <L> the level itself
 */
public sealed interface Importance<L extends Importance<L>>
        permits Importance.Primary, Importance.Secondary, Importance.Tertiary, Importance.Quaternary, Importance.Quinary, Importance.Senary {

    /** The most important. */
    non-sealed interface Primary extends Importance<Primary> {}

    non-sealed interface Secondary extends Importance<Secondary> {}

    non-sealed interface Tertiary extends Importance<Tertiary> {}

    non-sealed interface Quaternary extends Importance<Quaternary> {}

    non-sealed interface Quinary extends Importance<Quinary> {}

    /** The least important. */
    non-sealed interface Senary extends Importance<Senary> {}
}
