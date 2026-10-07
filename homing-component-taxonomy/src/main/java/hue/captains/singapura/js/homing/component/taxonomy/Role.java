package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A role: what a part does for the component that has it - a Title names it, a Summary says it
 * briefly, a Close closes it. Never what plays it. A role is a shared, stateless singleton,
 * filed in a family of the role catalogue, and it carries nothing else: what plays it and how
 * many are the component's to say, in a {@link Slot}, by asking the role to be played.
 *
 * <pre>{@code
 * public record Title() implements Role<Naming> {
 *     public static final Title INSTANCE = new Title();
 *     @Override public Naming family() { return Naming.INSTANCE; }
 * }
 *
 * Title.INSTANCE.playedBy(Heading.INSTANCE).one()      // a slot
 * }</pre>
 *
 * @param <F> the family it is filed in
 */
public non-sealed interface Role<F extends RoleFamily<?>> extends RoleNode {

    /** The family it is filed in. */
    F family();

    /** This role, played by a component: not yet a slot until it is told how many. */
    default <E extends Component<?>> Casting<E> playedBy(E base) { return new Casting<>(this, base); }
}
