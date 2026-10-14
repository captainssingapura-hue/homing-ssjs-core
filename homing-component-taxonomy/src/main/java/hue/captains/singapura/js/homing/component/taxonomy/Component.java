package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A component: a leaf of the taxonomy, and the only concrete node - what an implementation
 * realizes. It sits under a branch at any level, the root's included. Where something generic is
 * needed, a branch declares a generic leaf for it ({@code PlainCard} under {@code Card}).
 *
 * <p>A component declares its parts with it, as {@link Slot slots} said in the
 * {@link ComponentPartDSL}: each the independent component that plays it, a shared role, and how
 * many. Only the component declares its slots - composition is closed - and the owner is appended
 * when the taxonomy is read, each slot becoming a {@link Part}.</p>
 *
 * <pre>{@code
 * public record ProfileCard() implements Component<Card> {          // Card: a branch, at level 2
 *     public static final ProfileCard INSTANCE = new ProfileCard();
 *     private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
 *     @Override public Card parent() { return Card.INSTANCE; }
 *     @Override public List<Slot<?>> parts() {
 *         return List.of(DSL.part(Heading.INSTANCE).as(Title.INSTANCE).one(),
 *                        DSL.part(Badge.INSTANCE).as(Tag.INSTANCE).any(),
 *                        DSL.part(PlainButton.INSTANCE).as(Open.INSTANCE).optional());
 *     }
 * }
 * }</pre>
 *
 * @param <P> the branch this component sits under, at any level
 */
public non-sealed interface Component<P extends ComponentBranch> extends Taxon, ComponentLeaf {

    /** The branch this component sits under. */
    P parent();

    /** Its parts, each a role played by an independent component, so many times; none by default. */
    default List<Slot<?>> parts() { return List.of(); }

    /**
     * The states it has, from the tree of states: being enabled, being on. Each has its change, an
     * event it offers by having the state. None by default.
     */
    @Override default List<StateType<?>> states() { return List.of(); }

    /** Its own levels for some of its states, in place of theirs (RFC 0067, appendix Two Classes per Leaf); none by default. */
    @Override default List<ImportanceOverride<?>> importance() { return List.of(); }

    /**
     * The events it offers besides its states' changes, from the tree of events: a click. None by
     * default.
     */
    default List<EventType<?>> events() { return List.of(); }
}
