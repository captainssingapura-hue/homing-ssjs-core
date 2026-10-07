package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A branch of the role catalogue, at any level: it groups the roles and branches filed under it,
 * named for what they do for their owner - saying something of it, letting the user act on it,
 * shaping its room. A branch names its parent, so any library adds a branch under any branch,
 * the root's level included. It organises and nothing more.
 *
 * <pre>{@code
 * public record Saying() implements RoleBranch<RoleRoot> {
 *     public static final Saying INSTANCE = new Saying();
 *     @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
 * }
 * public record Naming() implements RoleBranch<Saying> {
 *     public static final Naming INSTANCE = new Naming();
 *     @Override public Saying parent() { return Saying.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface RoleBranch<P extends RoleBranch<?>> extends RoleNode {

    /** The branch it is filed under; only the root is its own. */
    @Override P parent();
}
