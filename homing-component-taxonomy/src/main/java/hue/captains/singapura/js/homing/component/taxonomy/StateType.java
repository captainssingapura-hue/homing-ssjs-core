package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A type of state, a leaf of the tree of states: one respect in which a component may be one way or
 * another - Enabled (true or false), a button's count of its clicks, a splitter's position, a text box's
 * draft. The tree files the types, never their values: a component that has a type is in exactly one of
 * its values at a time, so it has one value of each type it names ({@link Component#states()}). A
 * shared, stateless singleton filed under a branch, the root's included.
 *
 * <p>Its change is its event (RFC 0069, amended 21 Oct): an event is the change of one state of one
 * component, from a value to a different one, and a component declares its states and nothing else.
 * What its values may be is its {@link #domain() domain} - a finite set of ranges, or a value object -
 * and the changes that mean something of their own are named on it ({@link #changes()}). A type whose
 * look is a class - one for each of its named values - is {@link Classed}; every other is drawn by what
 * it moves or shows, and writes no class.</p>
 *
 * <pre>{@code
 * public record Clicks() implements StateType<Committing> {
 *     public static final Clicks INSTANCE = new Clicks();
 *     @Override public Committing parent() { return Committing.INSTANCE; }
 *     @Override public StateDomain domain() { return new StateDomain.Ranges(List.of(new StateDomain.Span(0, BOUND, true)), new StateDomain.Number(0)); }
 *     @Override public List<NamedChange> changes() { return List.of(new NamedChange("click", new NamedChange.Rises())); }
 * }
 * }</pre>
 *
 * @param <P> the branch it is filed under
 */
public non-sealed interface StateType<P extends StateBranch> extends StateNode {

    /** The branch it is filed under. */
    P parent();

    /** What its values may be, and its rest: its describer, in no language. */
    StateDomain domain();

    /** Its changes that mean something of their own, each named - none by default. */
    default List<NamedChange> changes() { return List.of(); }
}
