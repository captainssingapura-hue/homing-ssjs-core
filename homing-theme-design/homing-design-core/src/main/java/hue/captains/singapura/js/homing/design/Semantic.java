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
 * <p>A leaf owns its projections: the {@link DesignClass} records that pair
 * it with the physical targets it makes sense in are declared nested inside
 * it — {@code Feedback.Danger.danger_color_surface} — so a projection exists
 * once, where the meaning lives, and a consumer can only ask for one that
 * exists. A leaf is <i>not</i> a module and <i>not</i> a group: meaning is
 * served by nothing. Its projections are served by their targets, which are
 * the groups ({@link Target}), once the leaf is registered with the
 * {@link Vocabulary}.</p>
 *
 * <p>Three guards the open side does not get from the compiler, all checked
 * by {@link Trees}: a coordinate must be a leaf (a record), a leaf must have
 * exactly one branch, and a leaf's projections are declared inside it and
 * named for their coordinates.</p>
 */
public interface Semantic extends StatelessFunctionalObject {

    /** The leaf's token: its simple name in kebab case ({@code OnDanger} → {@code on-danger}). */
    default String token() { return Trees.semanticToken(getClass()); }
}
