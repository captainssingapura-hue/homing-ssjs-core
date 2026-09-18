package hue.captains.singapura.js.homing.design;

/**
 * Interaction — what an element affords or is currently doing with the
 * user. {@code Interactive} is "the user may act on this"; its states
 * (hover, active, disabled) are the template's, and a design binds the
 * motion, the cursor and the cue that make the affordance perceivable.
 * {@code Selectable} is the flat kind of it — a row, a cell, an option, a
 * tile: nothing at rest, and every state a lift; where {@code Interactive}
 * reads as pressable before it is touched, {@code Selectable} reads as one
 * of many until it is the one.
 * {@code Selected} and {@code Current} are the two kinds of "this one";
 * {@code Focus} is the ring; {@code Dragging} and {@code DropTarget} are the
 * two ends of a drag.
 */
public interface Interaction extends Semantic {

    record Interactive() implements Interaction {}

    /** A row, a cell, an option, a tile: one of many, flat until it is hovered, selected, current or highlighted. */
    record Selectable() implements Interaction {}

    record Selected() implements Interaction {}

    record Current() implements Interaction {}

    record Focus() implements Interaction {}

    record Dragging() implements Interaction {}

    /** Inert: a control that is present but cannot be acted on right now. */
    record Inert() implements Interaction {}

    record DropTarget() implements Interaction {}
}
