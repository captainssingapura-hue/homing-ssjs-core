package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A role a component declares for one of its parts, and the component that plays it: a dialog's
 * {@code Ok} and {@code Cancel}, played by a plain button; a card's {@code Title}, played by a
 * caption. A role is a record nested in the component that declares it - that is its owner -
 * and its name is its own, usually not its base's: the role is what lets a design tell two
 * parts of one kind apart.
 *
 * <p>The base is always an otherwise independent component. A part is answered as what it is -
 * it falls back through its base, never through its owner - and the owner's design never
 * alters how the base behaves.</p>
 *
 * @param <E> the component that plays the role
 */
public interface Role<E extends Component<?>> extends StatelessFunctionalObject {

    /** The component that plays this role. */
    E base();

    /** {@code Ok} gives {@code ok}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
