package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Add;
import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Check;
import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Close;
import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Fancy;
import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Gappy;
import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Plain;
import hue.captains.singapura.js.homing.design.semantic.WritingFixtures.Quoting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static hue.captains.singapura.js.homing.design.semantic.WritingFixtures.LEXICON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A style writes the words of a lexicon: ownership first, down its base chain, with what the modules on
 * the classpath write in it after its own; what it gets wrong is reported, never guessed at; and its sheet
 * draws each word on the mark's {@code ::before}, saying where a glyph came from when the worn style did
 * not write it. A word is written in the ink the mark stands on: never an emoji.
 */
class WritingTest {

    private static final WriteWord WRITE = WriteWord.INSTANCE;

    @Test
    void aWordIsWrittenByTheWornStyle_elseByWhatItIsWrittenOver() {
        assertEquals(Optional.of(new WriteWord.Written(Check.INSTANCE, "✔", Fancy.INSTANCE)), WRITE.write(Fancy.INSTANCE, Check.INSTANCE));
        assertEquals(Optional.of(new WriteWord.Written(Close.INSTANCE, "✕", Plain.INSTANCE)), WRITE.write(Fancy.INSTANCE, Close.INSTANCE), "its base's");
        assertEquals(Optional.empty(), WRITE.write(Gappy.INSTANCE, Close.INSTANCE), "nothing down its chain writes it");
        assertEquals(List.of(Fancy.INSTANCE, Plain.INSTANCE), WRITE.chain(Fancy.INSTANCE));
    }

    @Test
    void aModuleWritesInAStyleItDoesNotOwn_afterTheStylesOwn() {
        assertEquals(List.of(new Writing(Check.INSTANCE, "✔"), new Writing(Add.INSTANCE, "⊕")), WRITE.writings(Fancy.INSTANCE));
        assertEquals(Optional.of(new WriteWord.Written(Add.INSTANCE, "⊕", Fancy.INSTANCE)), WRITE.write(Fancy.INSTANCE, Add.INSTANCE),
                "the module's writing is the style's own: it comes before the base's");
        assertEquals("+", WRITE.write(Plain.INSTANCE, Add.INSTANCE).orElseThrow().glyph(), "and only in the style it names");
    }

    @Test
    void whatAStyleGetsWrong_isReported() {
        assertEquals(List.of(), WRITE.problems(LEXICON, Fancy.INSTANCE));
        assertEquals(List.of(), WRITE.problems(LEXICON, Plain.INSTANCE));
        assertEquals(List.of("gappy writes check twice", "gappy writes stray, which the lexicon has not",
                        "gappy writes no close, nor does anything it is written over", "gappy writes no add, nor does anything it is written over"),
                WRITE.problems(LEXICON, Gappy.INSTANCE));
    }

    @Test
    void aWordIsWrittenInTheInkItStandsOn_neverAsAnEmoji() {
        assertThrows(IllegalArgumentException.class, () -> new Writing(Check.INSTANCE, "📌"), "a pushpin, an emoji");
        assertThrows(IllegalArgumentException.class, () -> new Writing(Check.INSTANCE, "✔️"), "a check asked for as an emoji");
        assertThrows(IllegalArgumentException.class, () -> new Writing(Check.INSTANCE, " "), "nothing");
        assertThrows(IllegalArgumentException.class, () -> new Writing(Check.INSTANCE, "✔\n"), "more than a line");
        new Writing(Check.INSTANCE, "⚙");   // a gear is text unless an emoji is asked for
    }

    @Test
    void theSheet_drawsEachWordOnTheMarksBefore_sayingWhereAGlyphCameFrom() {
        var sheet = FlattenLexicon.INSTANCE.flatten(LEXICON, Fancy.INSTANCE, "fancy");
        assertTrue(sheet.css().contains(".word-check::before {\n    content: \"✔\";\n}\n"), sheet.css());
        assertTrue(sheet.css().contains("/* from plain */\n.word-close::before {\n    content: \"✕\";\n}\n"), sheet.css());
        assertTrue(sheet.css().contains(".word-add::before {\n    content: \"⊕\";\n}\n"), sheet.css());
        assertEquals(3, sheet.written());
        assertEquals(1, sheet.copied());
        assertEquals(List.of(), sheet.unwritten());
        assertEquals("word-close", SemanticTokens.INSTANCE.word(Close.INSTANCE));
    }

    @Test
    void theSheet_escapesWhatACssStringCannotHold_andReportsWhatNothingWrites() {
        assertTrue(FlattenLexicon.INSTANCE.flatten(LEXICON, Quoting.INSTANCE, "quoting").css().contains("content: \"\\\"\\\\\";"));
        assertEquals(List.of("close", "add"), FlattenLexicon.INSTANCE.flatten(LEXICON, Gappy.INSTANCE, "gappy").unwritten());
    }
}
