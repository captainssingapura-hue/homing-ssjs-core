package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A component: a leaf of the taxonomy, and the only concrete node - what an implementation
 * realizes. Where something generic is needed, a kind declares a generic leaf for it
 * ({@code PlainCard} under {@code Card}).
 *
 * <p>A component declares its parts with it, as {@link Slot slots} said in the
 * {@link ComponentPartDSL}: each the independent component that plays it, a shared role, and how
 * many. Only the component declares its slots - composition is closed - and the owner is appended
 * when the taxonomy is read, each slot becoming a {@link Part}.</p>
 *
 * <pre>{@code
 * public record ProfileCard() implements Component<Card> {
 *     public static final ProfileCard INSTANCE = new ProfileCard();
 *     @Override public Card parent() { return Card.INSTANCE; }
 *     @Override public List<Slot<?>> parts() {
 *         final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE;
 *         return List.of(dsl.part(Heading.INSTANCE).as(Title.INSTANCE).one(),
 *                        dsl.part(Badge.INSTANCE).as(Tag.INSTANCE).any(),
 *                        dsl.part(PlainButton.INSTANCE).as(Open.INSTANCE).optional());
 *     }
 * }
 * }</pre>
 *
 * @param <P> the branch this component sits under
 */
public non-sealed interface Component<P extends Branch> extends Taxon {

    /** The branch this component sits under. */
    P parent();

    /** Its parts, each a role played by an independent component, so many times; none by default. */
    default List<Slot<?>> parts() { return List.of(); }
}
