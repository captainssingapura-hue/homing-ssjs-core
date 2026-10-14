package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A part: a slot a component declares, with its owner appended when the taxonomy is read -
 * {@code Part(Confirmation, Confirm, PlainButton, 1)}. A node of the forest like any other, so a
 * design can answer it; it falls back through its base, never its owner, and goes one level
 * deep: a part's own parts are its base's.
 *
 * <p>A leaf of its own as a design sees it (RFC 0067, appendix Two Classes per Leaf): in state and in
 * design a part is a top-level component, independent of its owner and of the component that plays it.
 * It lists its own states, which its slot says, and its own overrides of their levels.</p>
 *
 * @param owner       the component that declares it
 * @param role        what it does for its owner
 * @param base        the component that plays it
 * @param cardinality how many the owner has
 * @param states      its own states, in the order its slot lists them
 * @param importance  its own levels for some of them
 * @param <O>         the owner
 * @param <E>         the base
 */
public record Part<O extends Component<?>, E extends Component<?>>(O owner, Role<?> role, E base, Cardinality cardinality,
                                                                   List<StateType<?>> states, List<ImportanceOverride<?>> importance)
        implements ComponentNode, ComponentLeaf, ValueObject {

    public Part {
        Objects.requireNonNull(owner, "Part.owner");
        Objects.requireNonNull(role, "Part.role");
        Objects.requireNonNull(base, "Part.base");
        Objects.requireNonNull(cardinality, "Part.cardinality");
        states = states == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(states));   // a null is the reader's to refuse
        importance = importance == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(importance));
    }

    /** A part with no states of its own. */
    public Part(O owner, Role<?> role, E base, Cardinality cardinality) { this(owner, role, base, cardinality, List.of(), List.of()); }

    /** The role's name: {@code confirm}. */
    @Override public NodeName name() { return role.name(); }

    /** The owner's token and the role's name: {@code confirmation-confirm}. */
    @Override public String token() { return owner.token() + "-" + name().value(); }
}
