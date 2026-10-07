package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A node of the role catalogue, a rigid tree of stateless singleton records: a {@link RoleBranch}
 * at any level, or a {@link Role} at a leaf. Every node names its parent, so the catalogue is open
 * at every level - any library adds a branch or a role under any branch - but its root, the one
 * {@link RoleRoot}, which nobody adds. The catalogue organises the roles and nothing more: it has
 * no design classes, and no part falls back through it.
 */
public sealed interface RoleNode extends StatelessFunctionalObject permits RoleBranch, Role {

    /** The branch this node is filed under; the root's is itself. */
    RoleBranch<?> parent();

    /** {@code ZoomIn} gives {@code zoom-in}. */
    default NodeName name() { return NodeName.ofType(getClass(), ""); }
}
