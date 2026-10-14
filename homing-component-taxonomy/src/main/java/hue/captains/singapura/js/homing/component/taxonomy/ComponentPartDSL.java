package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.AtMost;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Fixed;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Unbounded;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Varying;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The language a component declares its parts in - a stateless functional object, kept apart from
 * what it speaks of: a role is an identity and knows nothing of it, a component only returns the
 * slots it makes. A component that has parts holds it in a field of its own, so what composes is
 * seen in its fields, and a component that holds none has no parts.
 *
 * <pre>{@code
 * public record ProfileCard() implements Component<Card> {
 *     public static final ProfileCard INSTANCE = new ProfileCard();
 *     private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
 *     @Override public Card parent() { return Card.INSTANCE; }
 *     @Override public List<Slot<?>> parts() {
 *         return List.of(DSL.part(Heading.INSTANCE).as(Title.INSTANCE).one(),
 *                        DSL.part(Caption.INSTANCE).as(Subtitle.INSTANCE).optional());
 *     }
 * }
 * }</pre>
 *
 * <p>A part is said in three steps - what plays it, the role it plays, how many - and only the
 * last makes a {@link Slot}. A part given no role is a {@link NeedsRole}, one told no count a
 * {@link NeedsCount}; neither is a slot, so neither compiles where slots are asked for, and the
 * error names what is missing.</p>
 *
 * <table>
 *   <caption>How many</caption>
 *   <tr><td>{@code one()}</td><td>{@code 1}</td></tr>
 *   <tr><td>{@code exactly(n)}</td><td>{@code n}</td></tr>
 *   <tr><td>{@code optional()}</td><td>{@code 0..1}</td></tr>
 *   <tr><td>{@code atMost(n)}</td><td>{@code 0..n}</td></tr>
 *   <tr><td>{@code any()}</td><td>{@code 0..*}</td></tr>
 *   <tr><td>{@code atLeast(n)}</td><td>{@code n..*}</td></tr>
 *   <tr><td>{@code between(m, n)}</td><td>{@code m..n}</td></tr>
 * </table>
 */
public record ComponentPartDSL() implements StatelessFunctionalObject {

    public static final ComponentPartDSL INSTANCE = new ComponentPartDSL();

    /** A part played by a component - none is refused when the taxonomy is read. Not yet a slot: it needs a role. */
    public <E extends Component<?>> NeedsRole<E> part(E base) { return new NeedsRole<>(base); }

    /**
     * A part played by a component, and given no role yet.
     *
     * @param base the component that plays it
     * @param <E>  the component that plays it
     */
    public record NeedsRole<E extends Component<?>>(E base) implements ValueObject {

        /** The role it plays - none is refused when the taxonomy is read. Not yet a slot: it needs a count. */
        public NeedsCount<E> as(Role<?> role) { return new NeedsCount<>(base, role, List.of(), List.of()); }
    }

    /**
     * A part played by a component in a role, and not yet told how many. Each verb says how many,
     * and makes the slot; a count that is no cardinality is thrown where it is said. Before it, the
     * part's own states and overrides may be said: a part is a top-level thing in state and in
     * design, with none of its own unless its slot says.
     *
     * @param base       the component that plays it
     * @param role       the role it plays
     * @param states     the part's own states, in the order listed
     * @param importance the part's own levels for some of them
     * @param <E>        the component that plays it
     */
    public record NeedsCount<E extends Component<?>>(E base, Role<?> role, List<StateType<?>> states,
                                                     List<ImportanceOverride<?>> importance) implements ValueObject {

        /** A part given a role, with no states of its own yet. */
        public NeedsCount(E base, Role<?> role) { this(base, role, List.of(), List.of()); }

        /** The part's own states, in the order listed: the order that breaks a tie of importance. */
        public NeedsCount<E> states(StateType<?>... states) {
            return new NeedsCount<>(base, role, Collections.unmodifiableList(new ArrayList<>(Arrays.asList(states))), importance);
        }

        /** The part's own levels for some of its states, in place of theirs. */
        public NeedsCount<E> importance(ImportanceOverride<?>... overrides) {
            return new NeedsCount<>(base, role, states, Collections.unmodifiableList(new ArrayList<>(Arrays.asList(overrides))));
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

        private Slot<E> slot(Cardinality cardinality) { return new Slot<>(role, base, cardinality, states, importance); }
    }
}
