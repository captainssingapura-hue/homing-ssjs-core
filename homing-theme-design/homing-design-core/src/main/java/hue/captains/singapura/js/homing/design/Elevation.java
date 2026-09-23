package hue.captains.singapura.js.homing.design;

/**
 * How far off its own plane a thing sits: {@link #ELEVATED}, flat, or
 * {@link #SUNKEN}. The physical register, read from one attribute whose
 * values are mutually exclusive, and answered by the design as
 * {@link State#ELEVATED} and {@link State#SUNKEN} on whatever word says
 * what the thing is. Flat is rest: a thing that says nothing sits where the
 * design rests it, so there is no default to choose and none is offered.
 *
 * <p>Not the elevation of other systems, which is an absolute ladder of
 * steps with a shadow ramp to match. This is relative and per-word:
 * elevated is as raised as <i>this word</i> goes <i>in this design</i>, and
 * a panel's and a card's need not be the same distance. It says how far
 * from rest, not how high above the page.</p>
 *
 * <p>Depth is not {@link Layer}. A layer is the plane a thing belongs to —
 * base, raised, recessed, overlay — with its own face, ink and edge. The
 * elevation is how far off <i>that</i> plane it is at this moment.</p>
 *
 * <p><b>The design answers in whatever plane it honestly uses.</b> A shadow
 * usually; two shadows swapping sides where pressed-and-raised is the
 * language; a lit rim where light is; a step of tone where a design says
 * depth by value. <b>And a design may decline.</b> One with no idiom for
 * depth binds neither state, or binds them alike, and a thing dialled in
 * that theme simply stays where it is — which is an answer, and an honest
 * one: a design should not express depth in a plane it does not otherwise
 * use merely to satisfy a register. What is not safe is a holder using both
 * ends against each other, one thing elevated and another sunken, since a
 * design with a single gesture may fold them together; a holder that must
 * read in every theme says it on a plane every design has — colour — and
 * uses the depth to reinforce.</p>
 *
 * <p><b>It means nothing by itself.</b> Nothing here links a register to
 * focus, to selection or to anything else: what an elevation stands for is
 * the app's, said in the app's own code. A workspace that wants the region
 * being worked in to come forward writes that line itself, and may write a
 * different one tomorrow.</p>
 */
public final class Elevation {

    private Elevation() {}

    /** The attribute a component writes its register on; absent is flat. */
    public static final String ATTRIBUTE = "data-elevation";

    /** Lifted off its plane, as far as the word goes in this design. */
    public static final String ELEVATED = "elevated";

    /** Pressed into its plane, as far the other way as the word goes here. */
    public static final String SUNKEN = "sunken";

    /** Where the design rests the word: the attribute is not written at all. */
    public static final String FLAT = "flat";
}
