package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A part: a role a component declares, with its owner appended when the taxonomy is read -
 * {@code Part(Dialog, PlainButton, Ok)}. A node of the forest like any other, so a design can
 * answer it; it falls back through its base, never its owner, and goes one level deep: a part's
 * own parts are its base's.
 *
 * @param belongsTo the component that declares the role
 * @param base      the component that plays it
 * @param role      the role
 * @param <B>       the owner
 * @param <E>       the base
 */
public record Part<B extends Component<?>, E extends Component<?>>(B belongsTo, E base, Role<E> role)
        implements ComponentNode, ValueObject {

    public Part {
        Objects.requireNonNull(belongsTo, "Part.belongsTo");
        Objects.requireNonNull(base, "Part.base");
        Objects.requireNonNull(role, "Part.role");
    }

    /** The role's name: {@code ok}. */
    @Override public NodeName name() { return role.name(); }

    /** The owner's token and the role's name: {@code dialog-ok}. */
    @Override public String token() { return belongsTo.token() + "-" + name().value(); }
}
