package hue.captains.singapura.js.homing.studio.base.composed.graph;

/**
 * Thrown by {@link RigidNodeNormalizer} when a list of {@link RigidNode}s does not
 * form a single-rooted tree. The message names the specific defect — not exactly
 * one root, a node unreachable from the root, a duplicate sibling name, or a node
 * listed twice — so the boundary failure is actionable rather than a generic
 * "invalid input".
 */
public final class MalformedTreeException extends RuntimeException {
    public MalformedTreeException(String message) {
        super(message);
    }
}
