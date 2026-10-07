package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A role, a leaf of the role catalogue: what a part does for the component that has it - a Title
 * names it, a Summary says it briefly, a Close closes it. Never what plays it. A role is a shared,
 * stateless singleton filed under a branch at any level, and it is an identity and nothing more:
 * what plays it and how many are the component's to say, in the {@link ComponentPartDSL}.
 *
 * <pre>{@code
 * public record Title() implements Role<Naming> {
 *     public static final Title INSTANCE = new Title();
 *     @Override public Naming parent() { return Naming.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface Role<P extends RoleBranch<?>> extends RoleNode {

    /** The branch it is filed under. */
    @Override P parent();
}
