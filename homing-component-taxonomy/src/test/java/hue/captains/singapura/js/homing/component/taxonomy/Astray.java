package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A hypothetical component whose own meaning is written, in a file reading refuses: words before
 * its first section, a section twice, and a section for no node - {@code meanings/.../Astray.md}.
 */
final class Astray {

    private Astray() {}

    /** Meant, in a file astray. */
    record Lost() implements Component<Sketch.Text> {
        static final Lost INSTANCE = new Lost();
        @Override public Sketch.Text parent() { return Sketch.Text.INSTANCE; }
    }
}
