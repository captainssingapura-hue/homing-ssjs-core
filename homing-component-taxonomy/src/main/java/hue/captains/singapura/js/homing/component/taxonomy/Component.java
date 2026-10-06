package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A component: a leaf of the taxonomy, and the only concrete node - what an implementation
 * realizes. Where something generic is needed, a kind declares a generic leaf for it
 * ({@code GenericBox} under {@code Box}).
 *
 * <p>A component declares its parts as {@link Role roles}: each a record nested in the
 * component, naming the role and the independent component that plays it. Only the component
 * declares its roles - composition is closed - and the owner is appended when the taxonomy is
 * read, each role becoming a {@link Part}.</p>
 *
 * <pre>{@code
 * public record Dialog() implements Component<Container> {
 *     public static final Dialog INSTANCE = new Dialog();
 *     @Override public Container parent() { return Container.INSTANCE; }
 *     @Override public List<Role<?>> roles() { return List.of(Ok.INSTANCE, Cancel.INSTANCE); }
 *
 *     public record Ok() implements Role<PlainButton> {
 *         public static final Ok INSTANCE = new Ok();
 *         @Override public PlainButton base() { return PlainButton.INSTANCE; }
 *     }
 *     …
 * }
 * }</pre>
 *
 * @param <P> the branch this component sits under
 */
public non-sealed interface Component<P extends Branch> extends Taxon {

    /** The branch this component sits under. */
    P parent();

    /** The roles this component declares for its parts, each nested in it; none by default. */
    default List<Role<?>> roles() { return List.of(); }
}
