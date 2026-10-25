package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * How many of a part a component has, and when that is decided: a sum of two cases for the count, each
 * {@link Static} or {@link Dynamic}.
 *
 * <ul>
 *   <li>{@link Fixed} - exactly so many: a split's seam, a dialog's window.</li>
 *   <li>{@link Varying} - within a range: the tabs of a strip, the choices of a menu.</li>
 * </ul>
 *
 * <p>The count bounds how many; the other side says when they are decided (RFC 0071 port, question 5, the owner's
 * words). <b>Static</b>, the default: the parts along the slot are made with their owner and kept for its life - how
 * many, and which player, chosen once, as it is made; a part a state shows or hides is still static. <b>Dynamic</b>: the
 * owner makes and drops them while it lives, within the count - the tabs of a strip, the steps of a trail.</p>
 *
 * <p>One way to say each count: {@code Fixed(0)} is refused - a role played no times is no role - and so is a range
 * whose least and most are equal, which is {@code Fixed}. Written as multiplicity: {@code 1}, {@code 2}, {@code 0..1},
 * {@code 1..*}.</p>
 */
public sealed interface Cardinality extends ValueObject permits Cardinality.Fixed, Cardinality.Varying {

    /** Dynamic, as the DSL says it: {@code DSL.part(Tab.INSTANCE).as(Chip.INSTANCE).any(Cardinality.DYNAMIC)}. */
    Dynamic DYNAMIC = Dynamic.INSTANCE;

    /** The fewest the owner may have: one or more makes the part required. */
    int least();

    /** Whether the owner may have this many. */
    boolean allows(int count);

    /** As multiplicity: {@code 1}, {@code 0..1}, {@code 2..12}, {@code 0..*}. */
    String multiplicity();

    /** When the parts are decided: once, as the owner is made, or while it lives. */
    StaticOrDynamic when();

    /** Whether the owner makes and drops these parts while it lives. */
    default boolean dynamic() { return when() instanceof Dynamic; }

    /** Exactly so many: {@code Fixed(1)} is {@code 1}, static. */
    record Fixed(int count, StaticOrDynamic when) implements Cardinality {
        public Fixed {
            if (count < 1) throw new BadCardinality(count + " is no count: a role played no times is no role");
            if (when == null) throw new BadCardinality(count + ", decided when? static or dynamic");
        }
        /** Exactly so many, static. */
        public Fixed(int count) { this(count, Static.INSTANCE); }
        @Override public int least() { return count; }
        @Override public boolean allows(int n) { return n == count; }
        @Override public String multiplicity() { return String.valueOf(count); }
        @Override public String toString() { return multiplicity() + when.said(); }
    }

    /** At least {@code min}, and at most what {@code max} says: {@code Varying(0, AtMost(1))} is {@code 0..1}, static. */
    record Varying(int min, Bound max, StaticOrDynamic when) implements Cardinality {
        public Varying {
            if (max == null) throw new BadCardinality(min + "..? is no range: it needs a most, or none");
            if (min < 0) throw new BadCardinality(min + ".." + max.say() + " is no range: the least is below none");
            if (max instanceof AtMost(int most) && most < min)
                throw new BadCardinality(min + ".." + most + " is no range: the most is below the least");
            if (max instanceof AtMost(int most) && most == min)
                throw new BadCardinality(min + ".." + most + " is no range: say exactly(" + most + ")");
            if (when == null) throw new BadCardinality(min + ".." + max.say() + ", decided when? static or dynamic");
        }
        /** Within a range, static. */
        public Varying(int min, Bound max) { this(min, max, Static.INSTANCE); }
        @Override public int least() { return min; }
        @Override public boolean allows(int n) { return n >= min && max.admits(n); }
        @Override public String multiplicity() { return min + ".." + max.say(); }
        @Override public String toString() { return multiplicity() + when.said(); }
    }

    /** When the parts along a slot are decided: once, as their owner is made, or while it lives. */
    sealed interface StaticOrDynamic extends ValueObject permits Static, Dynamic {
        /** How a count says it: nothing for static, the default; {@code " dynamic"}. */
        String said();
    }

    /** Decided once, as the owner is made, and kept for its life - the default. */
    record Static() implements StaticOrDynamic {
        public static final Static INSTANCE = new Static();
        @Override public String said() { return ""; }
    }

    /** Made and dropped by the owner while it lives, within the count. */
    record Dynamic() implements StaticOrDynamic {
        public static final Dynamic INSTANCE = new Dynamic();
        @Override public String said() { return " dynamic"; }
    }

    /** The most of a range: so many, or no most at all. */
    sealed interface Bound extends ValueObject permits AtMost, Unbounded {
        boolean admits(int n);
        String say();
    }

    /** No more than {@code count}: one or more. */
    record AtMost(int count) implements Bound {
        public AtMost {
            if (count < 1) throw new BadCardinality("at most " + count + " is no bound: a role played no times is no role");
        }
        @Override public boolean admits(int n) { return n <= count; }
        @Override public String say() { return String.valueOf(count); }
    }

    /** No most: as many as the owner holds. */
    record Unbounded() implements Bound {
        public static final Unbounded INSTANCE = new Unbounded();
        @Override public boolean admits(int n) { return true; }
        @Override public String say() { return "*"; }
    }
}
