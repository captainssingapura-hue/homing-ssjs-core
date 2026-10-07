package hue.captains.singapura.js.homing.component.taxonomy;

/** The root of the role catalogue: every family sits under it, however deep. */
public record AnyRole() implements RoleBranch {

    public static final AnyRole INSTANCE = new AnyRole();
}
