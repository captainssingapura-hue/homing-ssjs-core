package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.L1_WordBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Lexicon;
import hue.captains.singapura.js.homing.component.taxonomy.ReadLexicon;
import hue.captains.singapura.js.homing.component.taxonomy.Word;
import hue.captains.singapura.js.homing.component.taxonomy.WordRoot;

import java.util.List;

/**
 * A lexicon of three words and the styles that write it: Plain writes every word; Fancy, over Plain,
 * writes one itself and is given another by a module; Quoting, over Plain, writes one with a quote and
 * a backslash in it; Gappy writes one word twice, one the lexicon has not, and leaves two unwritten.
 */
public final class WritingFixtures {

    private WritingFixtures() {}

    public record Marks() implements L1_WordBranch<WordRoot> {
        public static final Marks INSTANCE = new Marks();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    public record Check() implements Word<Marks> {
        public static final Check INSTANCE = new Check();
        @Override public Marks parent() { return Marks.INSTANCE; }
    }

    public record Close() implements Word<Marks> {
        public static final Close INSTANCE = new Close();
        @Override public Marks parent() { return Marks.INSTANCE; }
    }

    public record Add() implements Word<WordRoot> {
        public static final Add INSTANCE = new Add();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    /** A word no lexicon here reads. */
    public record Stray() implements Word<WordRoot> {
        public static final Stray INSTANCE = new Stray();
        @Override public WordRoot parent() { return WordRoot.INSTANCE; }
    }

    static final Lexicon LEXICON = ReadLexicon.INSTANCE.read(List.of(Check.INSTANCE, Close.INSTANCE, Add.INSTANCE));

    public record Plain() implements DesignStyle {
        public static final Plain INSTANCE = new Plain();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Writing> writings() {
            return List.of(new Writing(Check.INSTANCE, "✓"), new Writing(Close.INSTANCE, "✕"), new Writing(Add.INSTANCE, "+"));
        }
    }

    public record Fancy() implements DesignStyle, Derives<Plain> {
        public static final Fancy INSTANCE = new Fancy();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Writing> writings() { return List.of(new Writing(Check.INSTANCE, "✔")); }
    }

    /** A module writing in Fancy: named in the tests' {@code META-INF/services}. */
    public record FancyAdds() implements WritingExtension {
        @Override public Class<? extends DesignStyle> writes() { return Fancy.class; }
        @Override public List<Writing> writings() { return List.of(new Writing(Add.INSTANCE, "⊕")); }
    }

    public record Quoting() implements DesignStyle, Derives<Plain> {
        public static final Quoting INSTANCE = new Quoting();
        @Override public Plain base() { return Plain.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Writing> writings() { return List.of(new Writing(Close.INSTANCE, "\"\\")); }
    }

    public record Gappy() implements DesignStyle {
        public static final Gappy INSTANCE = new Gappy();
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Writing> writings() {
            return List.of(new Writing(Check.INSTANCE, "✓"), new Writing(Check.INSTANCE, "✔"), new Writing(Stray.INSTANCE, "✗"));
        }
    }
}
