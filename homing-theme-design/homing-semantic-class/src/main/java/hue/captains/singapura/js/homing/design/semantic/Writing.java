package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Word;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A style's writing of a word of the lexicon: the glyph it draws for what the word means, where the
 * mark draws its picture ({@code Target.Type.Glyph}, its {@code ::before}). A mark is drawn in the ink
 * it stands on, so a word is never written as an emoji - which brings colours of its own and is drawn
 * otherwise on every platform - nor with what asks for one (the emoji variation selector), nor with a
 * line break or a control character. A quote or a backslash is escaped where the sheet is written.
 *
 * @param word  the word written
 * @param glyph what the style draws for it: {@code ✕}
 */
public record Writing(Word<?> word, String glyph) implements ValueObject {

    /** The variation selector that asks for a character's emoji presentation. */
    private static final int EMOJI_SELECTOR = 0xFE0F;

    public Writing {
        Objects.requireNonNull(word, "Writing.word");
        Objects.requireNonNull(glyph, "Writing.glyph");
        if (glyph.isBlank()) throw new IllegalArgumentException(word.token() + " is written as nothing");
        glyph.codePoints().forEach(cp -> {
            if (Character.isISOControl(cp))
                throw new IllegalArgumentException(word.token() + " is written with a control character: a glyph is one line of text");
            if (cp == EMOJI_SELECTOR || Character.isEmojiPresentation(cp))
                throw new IllegalArgumentException(word.token() + " is written '" + glyph + "', an emoji: it brings colours of its own - a word is"
                        + " written in the ink the mark stands on");
        });
    }
}
