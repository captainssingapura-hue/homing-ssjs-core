package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

/**
 * The boundary: one semantic leaf projected onto one physical leaf. A
 * component imports design classes to say what it means and where it shows —
 * {@code Feedback.Danger.danger_color_surface},
 * {@code Interaction.Interactive.interactive_motion_transform} — and never a
 * value. A design fulfils each through an {@link ImplProvider}.
 *
 * <p>A design class is a {@link CssClass} whose group is <b>its physical
 * target</b>, not the type it is declared in: the group is virtual and
 * derived. A component imports {@code danger_color_surface} from
 * {@code Target.Color.Surface}, the surface sheet is what the manager loads,
 * and {@code css.addClass(el, danger_color_surface)} is the whole
 * consumer-side story. It has no body: a design provides one, or bindings
 * into the target's template, or silence.</p>
 *
 * <p>Its token is the two leaf tokens joined — {@code danger-color-surface} —
 * the semantic without its branch (a meaning may be re-curated without its
 * token changing), the target with (leaf names repeat across media). The
 * record is named the same with underscores, so the framework's class-name
 * derivation and the substrate's agree by construction; {@link Trees}
 * refuses a record whose name says otherwise.</p>
 *
 * <p>The two coordinates are read from the type arguments once and cached;
 * a projection record is therefore a one-liner:
 * {@code public record danger_color_surface() implements DesignClass<Danger, Target.Color.Surface> {}}</p>
 *
 * @param <S> the semantic leaf
 * @param <T> the physical leaf — the class's group
 */
public interface DesignClass<S extends Semantic, T extends Target & CssGroup<T>> extends CssClass<T> {

    @SuppressWarnings("unchecked")
    default Class<S> semantic() { return (Class<S>) Trees.coordinates(getClass()).semantic(); }

    @SuppressWarnings("unchecked")
    default Class<T> target() { return (Class<T>) Trees.coordinates(getClass()).target(); }

    /** The target leaf's singleton — the properties, states and carrier this class is bound within, and its group. */
    @SuppressWarnings("unchecked")
    default T targetLeaf() { return (T) Trees.targetInstance(target()); }

    /** The group is the target: virtual, derived, never the declaring type. */
    @Override default CssGroup<?> group() { return targetLeaf(); }

    /** {@code <semantic>-<branch>-<leaf>}: the class token on the element and the stem of every variable. */
    default String token() { return Trees.classToken(getClass()); }
}
