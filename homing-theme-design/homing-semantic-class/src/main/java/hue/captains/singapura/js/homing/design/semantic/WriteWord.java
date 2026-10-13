package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Lexicon;
import hue.captains.singapura.js.homing.component.taxonomy.Word;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * How the style worn writes a word: the first writing of it down the style's base chain, ownership
 * first as every answer is - the worn style's, then its base's. A style's writings are its own
 * ({@link DesignStyle#writings()}) and those the modules on the classpath give it ({@link
 * WritingExtension}), the modules' in the order of their class names. A palette writes no word: a word
 * is never coloured, it takes the ink the mark stands on.
 */
public record WriteWord() implements StatelessFunctionalObject {

    public static final WriteWord INSTANCE = new WriteWord();

    /**
     * A word as the style worn writes it.
     *
     * @param word  the word
     * @param glyph its glyph
     * @param by    the style down the chain that wrote it: the worn one, or one it is written over
     */
    public record Written(Word<?> word, String glyph, DesignStyle<?> by) implements ValueObject {}

    /** The style worn, then down its base chain. */
    public List<DesignStyle<?>> chain(DesignStyle<?> style) {
        var out = new ArrayList<DesignStyle<?>>();
        var seen = new HashSet<Class<?>>();
        for (DesignStyle<?> s = style; s != null; s = s instanceof Derives<?, ?> d ? d.base() : null) {
            if (!seen.add(s.getClass())) throw new IllegalArgumentException("the base chain of " + style.token() + " comes back to " + s.token());
            out.add(s);
        }
        return List.copyOf(out);
    }

    /** What a style writes: its own writings, then those the modules on the classpath write in it. */
    public List<Writing> writings(DesignStyle<?> style) {
        var out = new ArrayList<Writing>(style.writings());
        var extensions = new ArrayList<WritingExtension>();
        for (WritingExtension e : ServiceLoader.load(WritingExtension.class, style.getClass().getClassLoader()))
            if (e.writes() == style.getClass()) extensions.add(e);
        extensions.sort(Comparator.comparing(e -> e.getClass().getName()));
        for (WritingExtension e : extensions) out.addAll(e.writings());
        return List.copyOf(out);
    }

    /** How the style worn writes a word, if anything down its chain does. */
    public Optional<Written> write(DesignStyle<?> style, Word<?> word) {
        for (DesignStyle<?> s : chain(style))
            for (Writing w : writings(s))
                if (w.word().equals(word)) return Optional.of(new Written(word, w.glyph(), s));
        return Optional.empty();
    }

    /**
     * What a style's writings get wrong against a lexicon, down its chain: a word one style writes
     * twice, a word the lexicon has not - its module is not on the classpath, or it was never declared -
     * and a word of the lexicon nothing down the chain writes, which no mark could draw.
     */
    public List<String> problems(Lexicon lexicon, DesignStyle<?> style) {
        var out = new ArrayList<String>();
        var written = new HashSet<Word<?>>();
        for (DesignStyle<?> s : chain(style)) {
            var mine = new HashSet<Word<?>>();
            for (Writing w : writings(s)) {
                if (!mine.add(w.word())) out.add(s.token() + " writes " + w.word().token() + " twice");
                if (!lexicon.words().contains(w.word())) out.add(s.token() + " writes " + w.word().token() + ", which the lexicon has not");
                written.add(w.word());
            }
        }
        for (Word<?> w : lexicon.words())
            if (!written.contains(w)) out.add(style.token() + " writes no " + w.token() + ", nor does anything it is written over");
        return List.copyOf(out);
    }
}
