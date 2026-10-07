package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the role catalogue, levelled as the taxonomy is: a {@link RoleBranch} at its level, or
 * a {@link Role} at a leaf. Each is a stateless singleton record - held to it by jOntology when the
 * catalogue is read. The catalogue organises the roles and nothing more: it has no design classes,
 * and no part falls back through it.
 */
public sealed interface RoleNode extends StatelessFunctionalObject permits RoleBranch, Role {

    /** {@code ZoomIn} gives {@code zoom-in}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
