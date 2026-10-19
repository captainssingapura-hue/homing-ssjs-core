package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A component cast in roles with a look of its own (RFC 0067, D55): the set of them it may be cast in, fixed by its
 * type - {@code R} - and handed by value, so no reflection reads it. A slot casts such a component only in a role of its
 * set; a component that is no {@code WithRoles} is only ever placed - on its own, or in a role that only places it.
 * Its castings are then its own: on its own, and in each role of its set - what base assigns it, every one.
 *
 * <pre>{@code
 * public record PlainButton() implements Leaf<Button>, WithRoles<Committing> {
 *     public static final PlainButton INSTANCE = new PlainButton();
 *     @Override public Button parent() { return Button.INSTANCE; }
 *     @Override public Committing roleSet() { return Committing.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <R> its role set
 */
public interface WithRoles<R extends RoleSet> {

    /** The roles it may be cast in: its set, of its type. */
    R roleSet();
}
