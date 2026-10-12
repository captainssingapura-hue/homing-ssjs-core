package hue.captains.singapura.js.homing.design;

import java.util.List;

/**
 * The targets a virtual leaf owns (RFC 0067, appendix Virtual Leaves and Designs): what each of its
 * concrete leaves answers, every one of them, and all that is ever worn for it. Design language, which
 * the taxonomy never holds, so it is a second declaration on the leaf's own record, beside what the leaf
 * is:
 *
 * <pre>{@code
 * public record CursorBorder() implements VirtualComponent<GridFrame>, Owns {
 *     @Override public List<Target> owns() { return List.of(Target.Shape.Outline.INSTANCE, Target.Color.Outline.INSTANCE); }
 * }
 * }</pre>
 *
 * <p>Leaves worn together on one element own different targets, or two designs' classes would contend for
 * one property.</p>
 */
public interface Owns {

    /** The targets it owns, at least one. */
    List<Target> owns();
}
