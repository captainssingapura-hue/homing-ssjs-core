package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * The boundary: one semantic leaf projected onto one physical leaf. A
 * component imports design classes to say what it means and where it shows —
 * {@code Feedback.Danger.Color_Surface}, {@code Interaction.Interactive.Motion_Transform}
 * — and never a value. A design fulfils each through an {@link ImplProvider}.
 *
 * <p>A design class is a stateless object and belongs to no group of its own:
 * its {@link Target} is its group, and a deployment serves one sheet per
 * target leaf. Its token is the two leaf tokens joined —
 * {@code danger-color-surface} — the semantic without its branch (a meaning may
 * be re-curated without its token changing), the target with (leaf names repeat
 * across media).</p>
 *
 * <p>The two coordinates are read from the type arguments once and cached by
 * {@link Trees}; a projection record is therefore a one-liner:
 * {@code record Color_Surface() implements DesignClass<Danger, Target.Color.Surface> {}}</p>
 *
 * @param <S> the semantic leaf
 * @param <T> the physical leaf
 */
public interface DesignClass<S extends Semantic, T extends Target> extends StatelessFunctionalObject {

    @SuppressWarnings("unchecked")
    default Class<S> semantic() { return (Class<S>) Trees.coordinates(getClass()).semantic(); }

    @SuppressWarnings("unchecked")
    default Class<T> target() { return (Class<T>) Trees.coordinates(getClass()).target(); }

    /** The target leaf's singleton — the properties, states and carrier this class is bound within. */
    default Target targetLeaf() { return Trees.targetInstance(target()); }

    /** {@code <semantic>-<branch>-<leaf>}: the class token on the element and the stem of every variable. */
    default String token() { return Trees.classToken(getClass()); }
}
