package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * The semantic tree — <b>open</b>. What an element <i>means</i>, with no
 * word for how it looks. A branch is an interface extending this one; a
 * leaf is a record implementing exactly one branch, in whoever's crate needs
 * the meaning. The framework ships a first vocabulary ({@link Feedback},
 * {@link Emphasis}, {@link Layer}, {@link Interaction}, {@link Text},
 * {@link Pairing}, {@link Box}, {@link Brand}, {@link Structure}); a
 * product adds a leaf under an existing branch and, rarely, a branch.
 *
 * <p>A leaf is projected onto a physical target by whoever needs it, as a
 * value that pairs the two — {@code DesignClass.of(Danger.class, Color.Surface.class)}: a value a
 * component wears and a design answers. A leaf is <i>not</i> a module and
 * <i>not</i> a group: meaning is served by nothing; its projections are
 * served by their targets, which are the groups ({@link Target}).</p>
 *
 * <p>Two guards the open side does not get from the compiler, both checked
 * by {@link Trees}: a coordinate must be a leaf (a record), and a leaf must
 * have exactly one branch.</p>
 */
public interface Semantic extends StatelessFunctionalObject {

    /** The leaf's token: its simple name in kebab case ({@code OnDanger} → {@code on-danger}). */
    default String token() { return Trees.semanticToken(getClass()); }
}
