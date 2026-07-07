package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.WithDesc;

/**
 * A node in the <strong>ColorGroup taxonomy</strong> — colours classified by
 * their <em>nature</em> (what kind of job the colour does). A group's nature
 * fixes the internal structure of the colours it holds and whether {@code Degree}
 * even applies.
 *
 * <p>Modelled on rebar's rigid-levels ladder: the colour-scheme analogue of
 * {@code DomainNode}. Concrete groups pair this with a rebar level
 * ({@code Root} / {@code L1} / {@code L2}); colour leaves attach beneath a group
 * (which, as an {@code Intermediate}, is a {@code Parent}).</p>
 */
public interface ColorGroup extends WithDesc {
}
