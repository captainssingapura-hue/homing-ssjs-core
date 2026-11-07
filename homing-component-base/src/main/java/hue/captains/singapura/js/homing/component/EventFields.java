package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Objects;

/** The checks the fields of the page's events share. */
public record EventFields() implements StatelessFunctionalObject {

    public static final EventFields INSTANCE = new EventFields();

    /** An id that is there and not empty. */
    public String id(String id, String what) {
        Objects.requireNonNull(id, what);
        if (id.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
        return id;
    }

    /** A number above nothing. */
    public int positive(int v, String what) {
        if (v <= 0) throw new IllegalArgumentException(what + ": must be positive");
        return v;
    }
}
