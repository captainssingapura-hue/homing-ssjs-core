package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The root of the role catalogue: the one node nobody adds. It is its own parent, where every
 * walk up the catalogue ends; it is final, so nothing extends it, and a branch that names itself
 * as its parent - a second root - is refused when the catalogue is read. Everything under it is
 * open, at any level.
 */
public record RoleRoot() implements RoleBranch<RoleRoot> {

    public static final RoleRoot INSTANCE = new RoleRoot();

    /** Itself: the root has nothing above it. */
    @Override public RoleRoot parent() { return this; }
}
