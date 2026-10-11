package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/** A small lexicon for the tests: a branch of acts with two words in it, and a word filed under the root. */
public final class Pictures {

    private Pictures() {}

    public record Doing() implements L1_WordBranch<WordRoot> {
        public static final Doing INSTANCE = new Doing();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    public record Close() implements Word<Doing> {
        public static final Close INSTANCE = new Close();
        @Override public Doing parent() { return Doing.INSTANCE; }
    }

    public record Add() implements Word<Doing> {
        public static final Add INSTANCE = new Add();
        @Override public Doing parent() { return Doing.INSTANCE; }
    }

    public record Check() implements Word<WordRoot> {
        public static final Check INSTANCE = new Check();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    /** The words the lexicon declares itself, in its order. */
    public static final List<Word<?>> DECLARED = List.of(Close.INSTANCE, Add.INSTANCE, Check.INSTANCE);
}
