package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Optional;

/** Lines read from what may not be one ({@link Line}). */
public record Lines() implements StatelessFunctionalObject {

    public static final Lines INSTANCE = new Lines();

    /**
     * Wrap a caption/title string as an optional {@link Plain} line — a
     * {@code null} or blank string becomes {@link Optional#empty()}. The single
     * place the String-authoring boundary (DSL, convenience constructors)
     * crosses into the typed line.
     */
    public Optional<Line.Plain> optionalPlain(String s) {
        return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(new Line.Plain(s));
    }
}
