package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.AtMost;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Dynamic;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Fixed;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Unbounded;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Varying;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

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
 *
 * <p>Each count is static, as the owner is made and for its life, unless it is said dynamic - {@code
 * any(Cardinality.DYNAMIC)}, {@code optional(Cardinality.DYNAMIC)}: the owner makes and drops those parts while it
 * lives (RFC 0071 port, question 5).</p>
 */
public record ComponentPartDSL() implements StatelessFunctionalObject {

    public static final ComponentPartDSL INSTANCE = new ComponentPartDSL();

    /** A part played by a component - none is refused when the taxonomy is read. Not yet a slot: it needs a role. */
    public <E extends Component<?>> NeedsRole<E> part(E base) { return new NeedsRole<>(new Players.One(base)); }

    /**
     * A part played by one of several components, the owner mounting one at a time (D52): {@code DSL.part(Size.INSTANCE,
     * Aspect.INSTANCE).as(Measure.INSTANCE).one()}. Not yet a slot: it needs a role.
     */
    public NeedsRole<Component<?>> part(Component<?> first, Component<?> second, Component<?>... more) {
        var all = new java.util.ArrayList<Component<?>>();
        all.add(first);
        all.add(second);
        all.addAll(java.util.List.of(more));
        return new NeedsRole<>(new Players.AnyOf(all));
    }

    /**
     * A part played by any component under a branch - those the instance registers there, a downstream's with the
     * house's (D52): {@code DSL.anyOf(Icon.INSTANCE).as(Leading.INSTANCE).optional()}. Not yet a slot: it needs a role.
     */
    public NeedsRole<Component<?>> anyOf(ComponentBranch branch) { return new NeedsRole<>(new Players.Under(branch)); }

    /**
     * A part whose players are named, and given no role yet.
     *
     * @param players who plays it
     * @param <E>     what plays it, as the DSL was told
     */
    public record NeedsRole<E extends Component<?>>(Players players) implements ValueObject {

        /** The role it plays - none is refused when the taxonomy is read. Not yet a slot: it needs a count. */
        public NeedsCount<E> as(Role<?> role) { return new NeedsCount<>(players, role); }
    }

    /**
     * A part played by a component in a role, and not yet told how many. Each verb says how many,
     * and makes the slot; a count that is no cardinality is thrown where it is said.
     *
     * @param base the component that plays it
     * @param role the role it plays
     * @param <E>  the component that plays it
     */
    public record NeedsCount<E extends Component<?>>(Players players, Role<?> role) implements ValueObject {

        /** Exactly one, static: made with its owner, kept for its life. */
        public Slot<E> one() { return exactly(1); }

        /** Exactly one, dynamic: the owner replaces it while it lives - another player is another part. */
        public Slot<E> one(Dynamic dynamic) { return exactly(1, dynamic); }

        /** Exactly {@code n}, static. */
        public Slot<E> exactly(int n) { return slot(new Fixed(n)); }

        /** Exactly {@code n}, dynamic. */
        public Slot<E> exactly(int n, Dynamic dynamic) { return slot(new Fixed(n, dynamic)); }

        /** None, or one, static: whether it is there is decided as the owner is made. */
        public Slot<E> optional() { return atMost(1); }

        /** None, or one, dynamic: made and dropped while the owner lives. */
        public Slot<E> optional(Dynamic dynamic) { return atMost(1, dynamic); }

        /** None, up to {@code n}, static. */
        public Slot<E> atMost(int n) { return slot(new Varying(0, new AtMost(n))); }

        /** None, up to {@code n}, dynamic. */
        public Slot<E> atMost(int n, Dynamic dynamic) { return slot(new Varying(0, new AtMost(n), dynamic)); }

        /** As many as the owner holds, none included, static: how many is decided as the owner is made. */
        public Slot<E> any() { return atLeast(0); }

        /** As many as the owner holds, none included, dynamic: made and dropped while the owner lives. */
        public Slot<E> any(Dynamic dynamic) { return atLeast(0, dynamic); }

        /** {@code n} or more, static. */
        public Slot<E> atLeast(int n) { return slot(new Varying(n, Unbounded.INSTANCE)); }

        /** {@code n} or more, dynamic. */
        public Slot<E> atLeast(int n, Dynamic dynamic) { return slot(new Varying(n, Unbounded.INSTANCE, dynamic)); }

        /** From {@code m} up to {@code n}, static. */
        public Slot<E> between(int m, int n) { return slot(new Varying(m, new AtMost(n))); }

        /** From {@code m} up to {@code n}, dynamic. */
        public Slot<E> between(int m, int n, Dynamic dynamic) { return slot(new Varying(m, new AtMost(n), dynamic)); }

        private Slot<E> slot(Cardinality cardinality) { return new Slot<>(role, players, cardinality); }
    }
}
