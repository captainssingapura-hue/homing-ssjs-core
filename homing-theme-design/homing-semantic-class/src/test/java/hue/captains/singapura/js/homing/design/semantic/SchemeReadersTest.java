package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.semantic.SchemeReaders.Found;
import org.junit.jupiter.api.Test;

import java.util.List;

import static hue.captains.singapura.js.homing.design.semantic.SchemeReaders.BY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Finding a scheme's reader by its name (RFC 0067, D47): the reader of {@code <Name>Scheme} is {@code <Name>Colours},
 * beside it - in its package, or the class it is nested in - read off its declaration to read exactly that scheme.
 */
class SchemeReadersTest {

    @Test
    void aSchemesReader_isItsNameColours_besideIt_readingExactlyIt() {
        assertEquals(new Found.Reader(BaseColours.INSTANCE), BY_NAME.find(BaseScheme.class), "in its package");
        assertEquals(new Found.Reader(BaseColours.LoudColours.INSTANCE), BY_NAME.find(BaseColours.LoudScheme.class), "in the class it is nested in");
        assertEquals(new Found.Reader(new MadeColours()), BY_NAME.find(MadeScheme.class), "one with no INSTANCE made by its constructor of nothing");
    }

    /** A scheme not named for its reader. */
    abstract static class Nameless extends BaseScheme {}
    /** A scheme with no reader beside it. */
    abstract static class OrphanScheme extends BaseScheme {}
    /** A scheme whose namesake reads another. */
    abstract static class StrayScheme extends BaseScheme {}
    record StrayColours() implements SchemeTreeReader<BaseScheme> {
        @Override public List<? extends ColorNode<BaseScheme, ?, ?>> layer() { return BaseColours.INSTANCE.layer(); }
    }
    /** A scheme whose namesake reads nothing. */
    abstract static class MuteScheme extends BaseScheme {}
    record MuteColours() {}
    /** A scheme whose reader has no INSTANCE. */
    abstract static class MadeScheme extends BaseScheme {}
    record MadeColours() implements SchemeTreeReader<MadeScheme> {
        @Override public List<? extends ColorNode<MadeScheme, ?, ?>> layer() { return List.of(); }
    }

    @Test
    void refused_noScheme_oneNotSoNamed_noReaderBesideIt_oneReadingAnother_oneThatReadsNothing() {
        assertEquals(new Found.Missing("java.lang.String is no scheme: a scheme is an abstract class of ColorScheme"), BY_NAME.find(String.class));
        assertEquals(new Found.Missing("Nameless is not named <Name>Scheme: its reader is found by the scheme's name, <Name>Colours beside it"),
                BY_NAME.find(Nameless.class));
        assertEquals(new Found.Missing("OrphanScheme's reader is OrphanColours beside it, and there is none: a scheme and its colours live together"),
                BY_NAME.find(OrphanScheme.class));
        assertEquals(new Found.Missing("StrayColours reads BaseScheme, not StrayScheme: a scheme's reader reads exactly it"), BY_NAME.find(StrayScheme.class));
        assertEquals(new Found.Missing("MuteColours, beside MuteScheme, is no tree reader: a scheme's reader is <Name>Colours"), BY_NAME.find(MuteScheme.class));
    }
}
