package hue.captains.singapura.js.homing.component.taxonomy;

/** Words the reader refuses: one that says what another already does, one that means nothing, one filed nowhere. */
public final class BrokenPictures {

    private BrokenPictures() {}

    /** Named as {@link Pictures.Close} is. */
    public record Close() implements Word<WordRoot> {
        public static final Close INSTANCE = new Close();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    /** No section of its own. */
    public record Mute() implements Word<WordRoot> {
        public static final Mute INSTANCE = new Mute();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    /** Filed under nothing. */
    public record Orphan() implements Word<WordRoot> {
        public static final Orphan INSTANCE = new Orphan();
        @Override public WordRoot parent() { return null; }
    }
}
