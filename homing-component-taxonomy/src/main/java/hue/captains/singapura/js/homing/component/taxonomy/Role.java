package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A role: what a part does for the component that has it - a Title names it, a Summary says it
 * briefly, a Close closes it. Never what plays it. A role is a shared, stateless singleton, filed
 * in a family of the role catalogue, and it is an identity and nothing more: what plays it and how
 * many are the component's to say, in the {@link ComponentPartDSL}.
 *
 * <pre>{@code
 * public record Title() implements Role<Naming> {
 *     public static final Title INSTANCE = new Title();
 *     @Override public Naming family() { return Naming.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <F> the family it is filed in
 */
public non-sealed interface Role<F extends RoleFamily<?>> extends RoleNode {

    /** The family it is filed in. */
    F family();
}
