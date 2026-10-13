package hue.captains.singapura.js.homing.design;

import java.util.List;

/**
 * An animation effect (RFC 0050 · Episode 2, appendix The Copied Range): what moves, and how it reads, as a
 * whole - the dashes of a line march along it, a band of tint sweeps across a box. What {@link
 * Target.Animation.Effect} is answered with: a design names the effect and the colours it is drawn in, and
 * never writes a keyframe, a timing or a property the effect needs. How it is made in CSS is the effect's own,
 * sealed here, and the framework writes it.
 *
 * <ul>
 *   <li><b>One effect for each variant, with no parameters:</b> ants that march faster are another effect. Its
 *       type is its identity, its token its name: {@code marching-ants}.</li>
 *   <li><b>Its colours are borrowed:</b> read from the colour targets it is {@link #drawnIn() drawn in}, the
 *       ones the element it is worn on is coloured by - never said.</li>
 *   <li><b>Its still dual</b> is the same effect without any motion, a class of its own: what shows where motion
 *       is reduced, and what an element is given when the effect is switched off - a class swapped, nothing
 *       more.</li>
 *   <li><b>It takes the properties it needs:</b> every one its declarations and frames write. On the element
 *       it is worn on, no other answer may write them.</li>
 * </ul>
 *
 * Its declarations are the element's own - no class, no child - and read only the element's degrees and the
 * registered colours of the targets it is drawn in.
 */
public interface AnimationEffect {

    /** The effect's token, from its type: {@code marching-ants}. */
    default String token() { return Trees.kebab(getClass().getSimpleName()); }

    /** What a reader sees: {@code a dashed line whose dashes travel along it}. */
    String meaning();

    /** The colour targets it is drawn in: their colours are read, never said. */
    List<Target.Color> drawnIn();

    /** What it declares while it moves, besides its motion: {@code stroke-width: 1.5px; stroke-dasharray: 5 4;}. */
    String moving();

    /** What its still dual declares: the effect without any motion - {@code stroke-dasharray: 5 4;}, or nothing. */
    String still();

    /** Its keyframes' frames: {@code to { stroke-dashoffset: -9; }} - none for an effect that never moves. */
    String frames();

    /** How its frames run - duration, easing, count, direction: {@code 0.6s linear infinite}; none for an effect that never moves. */
    String timing();

    /** Whether it moves at all. */
    default boolean moves() { return !frames().isBlank(); }

    /** No effect: nothing moves, and nothing is taken. What an element shows when it is answered no animation. */
    record None() implements AnimationEffect {
        public static final None INSTANCE = new None();
        @Override public String meaning() { return "nothing moves"; }
        @Override public List<Target.Color> drawnIn() { return List.of(); }
        @Override public String moving() { return ""; }
        @Override public String still() { return ""; }
        @Override public String frames() { return ""; }
        @Override public String timing() { return ""; }
    }
}
