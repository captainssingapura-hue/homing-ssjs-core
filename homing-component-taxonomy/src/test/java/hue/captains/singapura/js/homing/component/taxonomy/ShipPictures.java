package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/** A module's words, beside its code: a branch of its own, and a word filed under the lexicon's branch. */
public final class ShipPictures {

    private ShipPictures() {}

    public record Shipping() implements L1_WordBranch<WordRoot> {
        public static final Shipping INSTANCE = new Shipping();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    public record Ship() implements Word<Shipping> {
        public static final Ship INSTANCE = new Ship();
        @Override public Shipping parent() { return Shipping.INSTANCE; }
    }

    public record Moor() implements Word<Pictures.Doing> {
        public static final Moor INSTANCE = new Moor();
        @Override public Pictures.Doing parent() { return Pictures.Doing.INSTANCE; }
    }

    /** The module extending the {@link Pictures} lexicon: named in the tests' {@code META-INF/services}. */
    public record Extension() implements LexiconExtension {
        @Override public Class<? extends LexiconSource> extending() { return PicturesSource.class; }
        @Override public List<Word<?>> words() { return List.of(Ship.INSTANCE, Moor.INSTANCE); }
    }
}
