package hue.captains.singapura.js.homing.design;

/**
 * Layer — what an element sits on, relative to the page. The current
 * surface tokens (base, raised, recessed, inverted) generalised to every
 * target a layer shows in: its face, its ink, its edge, its shadow, its
 * corner. {@code Overlay} is the scrim and the sheet above everything.
 */
public interface Layer extends Semantic {

    record Base() implements Layer {}

    record Raised() implements Layer {}

    record Recessed() implements Layer {}

    record Inverted() implements Layer {}

    record Overlay() implements Layer {}
}
