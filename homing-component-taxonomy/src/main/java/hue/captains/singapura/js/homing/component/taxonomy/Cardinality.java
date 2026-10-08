package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * How many of a part a component has: a sum of two cases.
 *
 * <ul>
 *   <li>{@link Fixed} - by construction: the parts are there from the start and their count never
 *       changes - a split's seam, a dialog's window.</li>
 *   <li>{@link Varying} - by reaction: the count is the owner's to change within a range - the
 *       tabs of a strip, the choices of a menu. A count chosen once, when the owner is built, is
 *       varying too: a cardinality bounds a count, and does not say when it is chosen.</li>
 * </ul>
 *
 * <p>One way to say each: {@code Fixed(0)} is refused - a role played no times is no role - and
 * so is a range whose least and most are equal, which is {@code Fixed}. Written as
 * multiplicity: {@code 1}, {@code 2}, {@code 0..1}, {@code 1..*}.</p>
 */
public sealed interface Cardinality extends ValueObject permits Cardinality.Fixed, Cardinality.Varying {

    /** The fewest the owner may have: one or more makes the part required. */
    int least();

    /** Whether the owner may have this many. */
    boolean allows(int count);

    /** As multiplicity: {@code 1}, {@code 0..1}, {@code 2..12}, {@code 0..*}. */
    String multiplicity();

    /** Exactly so many, from the start: {@code Fixed(1)} is {@code 1}. */
    record Fixed(int count) implements Cardinality {
        public Fixed {
            if (count < 1) throw new BadCardinality(count + " is no count: a role played no times is no role");
        }
        @Override public int least() { return count; }
        @Override public boolean allows(int n) { return n == count; }
        @Override public String multiplicity() { return String.valueOf(count); }
        @Override public String toString() { return multiplicity(); }
    }

    /** At least {@code min}, and at most what {@code max} says: {@code Varying(0, AtMost(1))} is {@code 0..1}. */
    record Varying(int min, Bound max) implements Cardinality {
        public Varying {
            if (max == null) throw new BadCardinality(min + "..? is no range: it needs a most, or none");
            if (min < 0) throw new BadCardinality(min + ".." + max.say() + " is no range: the least is below none");
            if (max instanceof AtMost(int most) && most < min)
                throw new BadCardinality(min + ".." + most + " is no range: the most is below the least");
            if (max instanceof AtMost(int most) && most == min)
                throw new BadCardinality(min + ".." + most + " is no range: say exactly(" + most + ")");
        }
        @Override public int least() { return min; }
        @Override public boolean allows(int n) { return n >= min && max.admits(n); }
        @Override public String multiplicity() { return min + ".." + max.say(); }
        @Override public String toString() { return multiplicity(); }
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
