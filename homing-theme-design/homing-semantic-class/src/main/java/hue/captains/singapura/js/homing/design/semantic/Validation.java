package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What a rule finds of what it checks (RFC 0067, D49): valid, or invalid with every problem it found, each said whole.
 * Applicative: results combine ({@link #and(Validation)}) keeping every problem of every rule, none stopping the rest.
 */
public sealed interface Validation extends ValueObject permits Validation.Valid, Validation.Invalid {

    /** Nothing found. */
    record Valid() implements Validation {
        public static final Valid INSTANCE = new Valid();
        @Override public List<String> problems() { return List.of(); }
    }

    /** What was found, each problem said whole: never none. */
    record Invalid(List<String> problems) implements Validation {
        public Invalid {
            problems = List.copyOf(problems);
            if (problems.isEmpty()) throw new IllegalArgumentException("an invalid result has a problem: one with none is valid");
        }
    }

    /** Every problem found; none when valid. */
    List<String> problems();

    /** This and another, every problem of both kept: valid only when both are. */
    default Validation and(Validation other) {
        if (this instanceof Valid) return other;
        if (other instanceof Valid) return this;
        var all = new ArrayList<String>(problems());
        all.addAll(other.problems());
        return new Invalid(all);
    }
}
