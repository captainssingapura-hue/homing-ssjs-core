package hue.captains.singapura.js.homing.design;

/**
 * Icon — what a mark means, never what it looks like. A menu row, a button,
 * a tab names an intent — close, detach, rotate — and the design says what
 * is drawn for it through {@link Target.Type.Glyph}: a symbol, an emoji, an
 * SVG, its own lettering. A component that shows an icon wears
 * {@code (Icon.X, Type.Glyph)} on the element that is the mark, and nothing
 * in the component is a picture.
 *
 * <p>The words are intents, so they are few and shared across the UI: the
 * same {@code Close} closes a tab, a pane and a dialog, and a design draws
 * it once. A new intent is a new word here and a binding in the designs —
 * the gate holds every design to every word a component wears.</p>
 */
public interface Icon extends Semantic {

    /** This one is chosen, on, done. */
    record Check() implements Icon {}
    /** There is more beneath: a row that opens its children, a node that expands. */
    record Disclose() implements Icon {}
    /** Close it: a tab, a pane, a dialog. */
    record Close() implements Icon {}
    /** Detach it: a tab from its dock, to float. */
    record Detach() implements Icon {}
    /** Turn it a step. */
    record Rotate() implements Icon {}
    /** Mirror it. */
    record Flip() implements Icon {}
    /** One more. */
    record Add() implements Icon {}
    /** One fewer. */
    record Remove() implements Icon {}
    /** Back to the start. */
    record Reset() implements Icon {}
    /** Keep it where it is. */
    record Pin() implements Icon {}
    /** The settings behind it. */
    record Settings() implements Icon {}
    /** Take hold of it: the mark on a knob, a handle, a thing the hand moves. */
    record Grip() implements Icon {}
    /** Bigger or smaller: the size axis. */
    record Size() implements Icon {}
    /** Wider or taller: the aspect axis. */
    record Aspect() implements Icon {}
    /** More or less of the meaning: the extent axis. */
    record Extent() implements Icon {}
    /** How much of it: a level, a volume, a gain. */
    record Level() implements Icon {}
    /** Columns: the room parted side by side, and a new one beside what is here. */
    record Column() implements Icon {}
    /** Rows: the room parted one over another, and a new one under what is here. */
    record Row() implements Icon {}
    /** Two rooms become one: this one goes and its room is given away. */
    record Merge() implements Icon {}
    /** You are in here: the work is going on inside this one, not on the bar that names it. */
    record Within() implements Icon {}
}
