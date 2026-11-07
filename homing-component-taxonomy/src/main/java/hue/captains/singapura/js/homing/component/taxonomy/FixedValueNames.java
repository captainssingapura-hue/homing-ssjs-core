package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Objects;

/** The name a fixed value goes by ({@link FixedValue}). */
public record FixedValueNames() implements StatelessFunctionalObject {

    public static final FixedValueNames INSTANCE = new FixedValueNames();

    /** Refuses a name that is no valid one. */
    public void check(String name, String kind) {
        Objects.requireNonNull(name, kind + ".name");
        if (!name.matches("[a-z][A-Za-z0-9]*")) throw new IllegalArgumentException(kind + ": '" + name + "' is no value's name - a lower-case letter, then letters and digits");
    }
}
