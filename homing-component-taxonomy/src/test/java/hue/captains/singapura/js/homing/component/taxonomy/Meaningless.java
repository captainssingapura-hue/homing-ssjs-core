package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * Hypothetical components whose meanings reading refuses: one with no section, one with an empty
 * section, and two that mean the same - their file is {@code meanings/.../Meaningless.md}.
 */
final class Meaningless {

    private Meaningless() {}

    /** No section is written for it. */
    record Silent() implements Component<Sketch.Text> {
        static final Silent INSTANCE = new Silent();
        @Override public Sketch.Text parent() { return Sketch.Text.INSTANCE; }
    }

    /** Its section says nothing. */
    record Blank() implements Component<Sketch.Text> {
        static final Blank INSTANCE = new Blank();
        @Override public Sketch.Text parent() { return Sketch.Text.INSTANCE; }
    }

    /** Means what Twain means. */
    record Twin() implements Component<Sketch.Text> {
        static final Twin INSTANCE = new Twin();
        @Override public Sketch.Text parent() { return Sketch.Text.INSTANCE; }
    }

    /** Means what Twin means. */
    record Twain() implements Component<Sketch.Text> {
        static final Twain INSTANCE = new Twain();
        @Override public Sketch.Text parent() { return Sketch.Text.INSTANCE; }
    }
}
