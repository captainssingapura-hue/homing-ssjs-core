package hue.captains.singapura.js.homing.design.styling;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** A file read as what it says: rules nested in rules, declarations, at-rules, each at its line; what cannot be read, said. */
class ReadCssTest {

    private static final ReadCss READ = ReadCss.INSTANCE;

    @Test
    void rulesNestedInRules_eachItemAtItsLine_commentsDropped() {
        var read = READ.read("/* a comment\n   over two lines */\n.a {\n  color: x;\n  &:hover { color: y }\n}\n");
        assertEquals(List.of(), read.problems());
        var a = (ReadCss.Rule) read.items().get(0);
        assertEquals(".a", a.selector());
        assertEquals(3, a.line(), "the comment's lines kept");
        assertEquals(new ReadCss.Declaration("color", "x", 4), a.body().get(0));
        var hover = (ReadCss.Rule) a.body().get(1);
        assertEquals("&:hover", hover.selector());
        assertEquals(List.of(new ReadCss.Declaration("color", "y", 5)), hover.body(), "the last declaration needs no semicolon");
    }

    @Test
    void stringsAndBrackets_keptWhole() {
        var read = READ.read(".a { content: \"}{;/*\"; background: url(a;b.png); }");
        assertEquals(List.of(), read.problems());
        var body = ((ReadCss.Rule) read.items().get(0)).body();
        assertEquals(new ReadCss.Declaration("content", "\"}{;/*\"", 1), body.get(0));
        assertEquals(new ReadCss.Declaration("background", "url(a;b.png)", 1), body.get(1));
    }

    @Test
    void atRules_withABlock_andAsAStatement() {
        var read = READ.read("@media print { .a { color: x; } }\n@import \"b.css\";");
        var media = (ReadCss.AtRule) read.items().get(0);
        assertEquals("media print", media.prelude());
        assertEquals(1, media.body().size());
        var imported = (ReadCss.AtRule) read.items().get(1);
        assertEquals(2, imported.line());
        assertNull(imported.body(), "a statement has no block");
    }

    @Test
    void whatCannotBeRead_isSaid() {
        assertEquals(List.of("1: a block that never closes"), READ.read(".a { color: x;").problems());
        assertEquals(List.of("1: a closing brace with nothing open"), READ.read("}").problems());
        assertEquals(List.of("2: a comment that never ends"), READ.read(".a {}\n/* and so").problems());
        assertEquals(List.of("1: not a declaration: color"), READ.read(".a { color }").problems());
    }
}
