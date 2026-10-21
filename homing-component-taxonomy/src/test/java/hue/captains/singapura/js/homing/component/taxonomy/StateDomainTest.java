package hue.captains.singapura.js.homing.component.taxonomy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a type of state may be, and its named changes (RFC 0069, amended 21 Oct): a domain - a finite set of ranges, or
 * a value object - held whole as it is made; a named change held to fit its type's domain when the tree is read; a
 * classed type a set of named values.
 */
class StateDomainTest {

    private static final StateDomain.Span COUNT = new StateDomain.Span(0, 1_000_000, true);

    /** A button's count of its clicks: whole numbers from none, its click a rise. */
    record Clicks() implements StateType<StateRoot> {
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public StateDomain domain() { return new StateDomain.Ranges(List.of(COUNT), new StateDomain.Number(0)); }
        @Override public List<NamedChange> changes() { return List.of(new NamedChange("click", new NamedChange.Rises())); }
    }

    /** A held state, up or down, its press and its release; drawn by nothing of its own. */
    record Held() implements StateType<StateRoot> {
        static final StateValue UP = new StateValue("up"), DOWN = new StateValue("down");
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public StateDomain domain() { return new StateDomain.Ranges(List.of(new StateDomain.Point(UP), new StateDomain.Point(DOWN)), new StateDomain.Named(UP)); }
        @Override public List<NamedChange> changes() {
            return List.of(new NamedChange("press", new NamedChange.Between(UP, DOWN)), new NamedChange("release", new NamedChange.Between(DOWN, UP)));
        }
    }

    /** A change that does not fit: a rise of named values, and a change from a value it does not name. */
    record Unfit() implements StateType<StateRoot> {
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public StateDomain domain() { return new Held().domain(); }
        @Override public List<NamedChange> changes() {
            return List.of(new NamedChange("rise", new NamedChange.Rises()), new NamedChange("off", new NamedChange.Between(new StateValue("off"), Held.UP)),
                    new NamedChange("rise", new NamedChange.Falls()));
        }
    }

    /** A classed type whose domain is a span: a class is for a named value. */
    record ClassedSpan() implements Classed<StateRoot> {
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(StateValue.FALSE, StateValue.TRUE); }
        @Override public StateDomain domain() { return new StateDomain.Ranges(List.of(COUNT), new StateDomain.Number(0)); }
    }

    /** A draft text: a value object, its rest the empty one. */
    record Draft() implements StateType<StateRoot> {
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
        @Override public StateDomain domain() { return new StateDomain.Valued(StateDomain.Valued.Kind.TEXT); }
    }

    @Test
    void aDomainOfRanges_isHeldWholeAsItIsMade() {
        var held = new Held().domain();
        assertTrue(held.allows(new StateDomain.Named(Held.UP)) && !held.allows(new StateDomain.Named(StateValue.TRUE)) && !held.allows(new StateDomain.Number(1)));
        var count = new Clicks().domain();
        assertTrue(count.allows(new StateDomain.Number(3)) && !count.allows(new StateDomain.Number(2.5)) && !count.allows(new StateDomain.Number(-1)));
        assertEquals(new StateDomain.Number(0), count.rest());
        var mixed = new StateDomain.Ranges(List.of(new StateDomain.Point(new StateValue("auto")), new StateDomain.Span(0, 100, false)), new StateDomain.Named(new StateValue("auto")));
        assertTrue(mixed.allows(new StateDomain.Number(42.5)) && mixed.allows(new StateDomain.Named(new StateValue("auto"))), "named and continuous, together");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Ranges(List.of(new StateDomain.Point(Held.UP)), new StateDomain.Named(Held.UP)), "one value: no state");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Ranges(List.of(new StateDomain.Point(Held.UP), new StateDomain.Point(Held.UP)), new StateDomain.Named(Held.UP)), "a value twice");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Ranges(List.of(COUNT), new StateDomain.Number(-1)), "a rest it does not hold");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Ranges(List.of(new StateDomain.Span(0, 10, false), new StateDomain.Span(5, 20, false)), new StateDomain.Number(0)), "two spans that meet");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Span(0.5, 3, true), "whole numbers, whole bounds");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Span(3, 3, false), "its least below its most");
        assertThrows(IllegalArgumentException.class, () -> new StateDomain.Number(Double.NaN), "a finite number");
    }

    @Test
    void aValueObjectDomain_isComparedByEquality_itsRestTheEmptyText() {
        var draft = new Draft().domain();
        assertTrue(draft.allows(new StateDomain.Text("hello")) && !draft.allows(new StateDomain.Number(1)));
        assertEquals(new StateDomain.Text(""), draft.rest());
        assertEquals(new StateDomain.Text("a"), new StateDomain.Text("a"), "two texts alike are one value");
    }

    @Test
    void aClassedType_isASetOfNamedValues_itsRestTheFirst() {
        var lit = Signals.Lit.INSTANCE.domain();
        assertTrue(lit instanceof StateDomain.Ranges r && !r.continuous() && r.named().equals(Signals.Lit.INSTANCE.values()));
        assertEquals(new StateDomain.Named(Signals.Lit.INSTANCE.values().get(0)), lit.rest());
    }

    @Test
    void aChangeIsNamedFittingItsDomain_andIsBetweenTwoDifferentValues() {
        assertThrows(IllegalArgumentException.class, () -> new NamedChange.Between(Held.UP, Held.UP), "a change is between two different values");
        assertThrows(IllegalArgumentException.class, () -> new NamedChange("Click", new NamedChange.Rises()), "named in lower case");
        assertEquals(List.of(), TreeChecks.INSTANCE.badChanges(List.of(new Clicks(), new Held(), new Draft(), Signals.Lit.INSTANCE)), "each fits");
        List<TaxonomyProblem> unfit = TreeChecks.INSTANCE.badChanges(List.of(new Unfit()));
        assertEquals(4, unfit.size(), "a rise of named values, a value it does not name, a name twice, and that one a fall of named values - " + unfit);
        assertTrue(unfit.stream().allMatch(p -> p.rule() == TaxonomyProblem.Rule.BAD_STATE_CHANGE));
        assertTrue(unfit.stream().anyMatch(p -> p.says().contains("'rise' is a rise, and it has no span")), unfit.toString());
        assertTrue(unfit.stream().anyMatch(p -> p.says().contains("from off to up, and it names no such values")), unfit.toString());
        assertTrue(unfit.stream().anyMatch(p -> p.says().contains("'rise' twice")), unfit.toString());
        List<TaxonomyProblem> spanned = TreeChecks.INSTANCE.badChanges(List.of(new ClassedSpan()));
        assertTrue(spanned.size() == 1 && spanned.getFirst().rule() == TaxonomyProblem.Rule.BAD_STATE_DOMAIN, spanned.toString());
        assertFalse(TreeChecks.INSTANCE.levels(List.of(new Clicks())).stream().anyMatch(p -> p.rule() == TaxonomyProblem.Rule.STATE_UNRANKED),
                "a type with no class needs no level: a level orders the classes a leaf wears");
    }
}
