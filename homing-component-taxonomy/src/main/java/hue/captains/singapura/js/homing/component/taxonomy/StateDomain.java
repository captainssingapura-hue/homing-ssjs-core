package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

/**
 * What a type of state may be (RFC 0069, amended 21 Oct): its describer, in no language, so that its Java type and its
 * JavaScript type are made from it alike. One of two kinds:
 *
 * <ul>
 *   <li>a finite set of ranges - a range one named value (enabled or not, the three elevations), or the numbers between
 *       two bounds, whole ones only where it says so (a count, a position, a size, a degree);</li>
 *   <li>a value object of one kind, compared by its equality (a text).</li>
 * </ul>
 *
 * Each has its rest: the value it is when nothing has changed it. A domain is held whole as it is made: no one is made
 * that is not.
 */
public sealed interface StateDomain extends ValueObject permits StateDomain.Ranges, StateDomain.Valued {

    /** Whether a value is one this domain allows. */
    boolean allows(Value value);

    /** Its rest. */
    Value rest();

    /** One value of a type of state: a named one, a number, a text, an order of names, or a count for each name. */
    sealed interface Value extends ValueObject permits Named, Number, Text, Names, Counts {}

    /** A named value: {@code true}, {@code sunken}. */
    record Named(StateValue value) implements Value {
        public Named { Objects.requireNonNull(value, "Named.value"); }
        @Override public String toString() { return value.token(); }
    }

    /** A number: a finite one. */
    record Number(double value) implements Value {
        public Number {
            if (!Double.isFinite(value)) throw new IllegalArgumentException("a state's number is a finite one, not " + value);
        }
        @Override public String toString() { return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value); }
    }

    /** A text: any, the empty one among them. */
    record Text(String value) implements Value {
        public Text { Objects.requireNonNull(value, "Text.value"); }
        @Override public String toString() { return value; }
    }

    /** An order of names, each once, none blank - a tab order: equal to another of the same names in the same order. */
    record Names(List<String> names) implements Value {
        public Names {
            names = List.copyOf(Objects.requireNonNull(names, "Names.names"));
            if (names.stream().anyMatch(String::isBlank)) throw new IllegalArgumentException("an order of names names none blank: " + names);
            if (new java.util.HashSet<>(names).size() != names.size()) throw new IllegalArgumentException("an order of names names each once: " + names);
        }
        @Override public String toString() { return names.toString(); }
    }

    /**
     * A count for each name, from none - or none at all, for a name that is not counted: the closes asked of each tab a
     * pane holds, none for a tab that cannot be closed. Equal to another of the same names, each with the same count or
     * none; the order they are given in is kept, and means nothing.
     */
    record Counts(Map<String, OptionalInt> counts) implements Value {
        public Counts {
            Objects.requireNonNull(counts, "Counts.counts");
            for (var e : counts.entrySet()) {
                if (e.getKey() == null || e.getKey().isBlank()) throw new IllegalArgumentException("a count for each name names none blank: " + counts.keySet());
                if (e.getValue() == null) throw new IllegalArgumentException(e.getKey() + ": a count, or none - OptionalInt.empty()");
                if (e.getValue().isPresent() && e.getValue().getAsInt() < 0) throw new IllegalArgumentException(e.getKey() + ": a count is from none, not " + e.getValue().getAsInt());
            }
            counts = Collections.unmodifiableMap(new LinkedHashMap<>(counts));
        }

        /** The names whose count differs from another's - or which only one of the two has. */
        public Set<String> differing(Counts other) {
            var out = new TreeSet<String>();
            for (var e : counts.entrySet()) if (!e.getValue().equals(other.counts.get(e.getKey()))) out.add(e.getKey());
            for (String n : other.counts.keySet()) if (!counts.containsKey(n)) out.add(n);
            return out;
        }

        @Override public String toString() {
            var said = new ArrayList<String>();
            counts.forEach((n, c) -> said.add(n + "=" + (c.isPresent() ? String.valueOf(c.getAsInt()) : "none")));
            return "{" + String.join(", ", said) + "}";
        }
    }

    /** One range of a type of state's values: a named value, or a span of numbers. */
    sealed interface Range extends ValueObject permits Point, Span {
        boolean holds(Value value);
    }

    /** A range of one named value. */
    record Point(StateValue value) implements Range {
        public Point { Objects.requireNonNull(value, "Point.value"); }
        @Override public boolean holds(Value v) { return v instanceof Named n && n.value().equals(value); }
    }

    /**
     * The numbers between two bounds, both in, the least below the most; whole numbers only where it says so, its
     * bounds then whole.
     */
    record Span(double least, double most, boolean whole) implements Range {
        public Span {
            if (!Double.isFinite(least) || !Double.isFinite(most)) throw new IllegalArgumentException("a span's bounds are finite: " + least + " to " + most);
            if (!(least < most)) throw new IllegalArgumentException("a span's least is below its most: " + least + " to " + most);
            if (whole && (least != Math.rint(least) || most != Math.rint(most)))
                throw new IllegalArgumentException("a span of whole numbers has whole bounds: " + least + " to " + most);
        }
        @Override public boolean holds(Value v) {
            return v instanceof Number n && n.value() >= least && n.value() <= most && (!whole || n.value() == Math.rint(n.value()));
        }
    }

    /**
     * A finite set of ranges: at least two values among them, no named value twice, no two spans that meet; its rest a
     * value one of them holds.
     *
     * @param ranges its ranges, in the order it says them
     * @param rest   its rest
     */
    record Ranges(List<Range> ranges, Value rest) implements StateDomain {
        public Ranges {
            Objects.requireNonNull(ranges, "Ranges.ranges");
            Objects.requireNonNull(rest, "Ranges.rest");
            ranges = List.copyOf(ranges);
            var named = new HashSet<StateValue>();
            var spans = new ArrayList<Span>();
            for (Range r : ranges) switch (r) {
                case Point p -> { if (!named.add(p.value())) throw new IllegalArgumentException("a state's ranges name " + p.value() + " twice"); }
                case Span s -> {
                    for (Span t : spans) if (s.least() <= t.most() && t.least() <= s.most())
                        throw new IllegalArgumentException("a state's spans meet: " + s.least() + " to " + s.most() + " and " + t.least() + " to " + t.most());
                    spans.add(s);
                }
            }
            if (named.size() < 2 && spans.isEmpty())
                throw new IllegalArgumentException("a state is one way or another: two values at least, not " + named.size());
            if (rest instanceof Text) throw new IllegalArgumentException("a state of ranges rests at a named value or a number, not a text");
            final Value given = rest;
            if (ranges.stream().noneMatch(r -> r.holds(given))) throw new IllegalArgumentException("a state's rest is one of its values, not " + rest);
        }

        /** Its named values: only these, where it has no span - the values a class may be written for. */
        public List<StateValue> named() { return ranges.stream().filter(Point.class::isInstance).map(r -> ((Point) r).value()).toList(); }

        /** Whether it has a span of numbers. */
        public boolean continuous() { return ranges.stream().anyMatch(Span.class::isInstance); }

        @Override public boolean allows(Value value) { return value != null && ranges.stream().anyMatch(r -> r.holds(value)); }
    }

    /**
     * A value object of one kind, compared by its equality: a text, which may be any - the empty one its rest; or an order
     * of names, each once - none its rest; or a count for each name, or none for a name not counted - no name its rest.
     *
     * @param kind what its values are
     */
    record Valued(Kind kind) implements StateDomain {
        public Valued { Objects.requireNonNull(kind, "Valued.kind"); }

        /** What a value-object state's values are. */
        public enum Kind { TEXT, NAMES, COUNTS }

        @Override public boolean allows(Value value) {
            return switch (kind) { case TEXT -> value instanceof Text; case NAMES -> value instanceof Names; case COUNTS -> value instanceof Counts; };
        }

        @Override public Value rest() {
            return switch (kind) { case TEXT -> new Text(""); case NAMES -> new Names(List.of()); case COUNTS -> new Counts(Map.of()); };
        }
    }
}
