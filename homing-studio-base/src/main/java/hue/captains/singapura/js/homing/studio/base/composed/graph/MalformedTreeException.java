package hue.captains.singapura.js.homing.studio.base.composed.graph;

/**
 * Thrown by {@link TreeGraphNormalizer} when a {@link TreeGraph} does not form a
 * single-rooted tree. The message names the specific defect — a dangling edge, a
 * duplicate sibling name, a node with more than one parent, a cycle, the wrong
 * number of roots, or a duplicate node id — so the boundary failure is actionable
 * rather than a generic "invalid input".
 */
public final class MalformedTreeException extends RuntimeException {
    public MalformedTreeException(String message) {
        super(message);
    }
}
