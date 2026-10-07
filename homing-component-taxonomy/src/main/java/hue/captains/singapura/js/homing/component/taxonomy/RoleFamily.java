package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A family of the role catalogue, named for what its roles do for their owner - naming it,
 * telling more about it, acting on it. A family names its parent, so any repository may add a
 * family, or a role under any family. It organises and nothing more.
 *
 * <pre>{@code
 * public record Naming() implements RoleFamily<AnyRole> {
 *     public static final Naming INSTANCE = new Naming();
 *     @Override public AnyRole parent() { return AnyRole.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> the branch this family sits under
 */
public non-sealed interface RoleFamily<P extends RoleBranch> extends RoleBranch {

    /** The branch this family sits under. */
    P parent();
}
