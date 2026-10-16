package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;
import java.util.function.Function;

/**
 * What rules' results come to (RFC 0067, D49): a list of problems as a result, and a list of rules applied each apart to
 * what they check, their results combined - every problem of every one kept ({@link Validation}).
 */
public record Validations() implements StatelessFunctionalObject {

    public static final Validations INSTANCE = new Validations();

    /** A list of problems as a result: valid when none. */
    public Validation of(List<String> problems) {
        return problems.isEmpty() ? Validation.Valid.INSTANCE : new Validation.Invalid(problems);
    }

    /** Every rule applied to what it checks, each apart, the results combined. */
    public <R> Validation all(List<? extends R> rules, Function<? super R, Validation> apply) {
        Validation found = Validation.Valid.INSTANCE;
        for (R rule : rules) found = found.and(apply.apply(rule));
        return found;
    }
}
