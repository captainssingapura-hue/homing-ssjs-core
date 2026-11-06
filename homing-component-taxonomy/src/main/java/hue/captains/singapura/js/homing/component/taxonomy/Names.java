package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** How a reader says a node in a problem or a finding: by its type's simple name, or qualified where that is not enough. */
record Names() implements StatelessFunctionalObject {

    static final Names INSTANCE = new Names();

    /** {@code PlainButton}; a part as {@code Confirmation.Confirm}. */
    String of(Object o) {
        if (o == null) return "nothing";
        if (o instanceof Part<?, ?> p) return of(p.owner()) + "." + of(p.role());
        return o.getClass().getSimpleName();
    }

    /** A type without its package, nesting kept: {@code Here$Badge}. */
    String qualified(Object o) {
        if (o instanceof Part<?, ?> p) return qualified(p.owner()) + "." + p.name().value();
        String n = o.getClass().getName();
        return n.substring(n.lastIndexOf('.') + 1);
    }
}
