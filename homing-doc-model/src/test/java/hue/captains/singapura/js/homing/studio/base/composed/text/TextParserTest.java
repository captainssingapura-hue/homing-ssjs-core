package hue.captains.singapura.js.homing.studio.base.composed.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0018 / Phase 4 — pins the {@code .mdad+} grammar definition by
 * exhaustive positive + negative cases. The grammar is the conformance
 * gate; this test is what stops grammar regressions during future
 * extensions (CodeSegment, etc.).
 */
class TextParserTest {

    // -----------------------------------------------------------------------
    // T0 — paragraphs
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T0: a single paragraph parses to one Para block")
    void singlePara() {
        var blocks = TextParser.parse("Hello world.");
        assertEquals(1, blocks.size());
        var p = assertInstanceOf(Block.Para.class, blocks.get(0));
        assertEquals(1, p.inlines().size());
        assertEquals(new Inline.Text("Hello world."), p.inlines().get(0));
    }

    @Test
    @DisplayName("T0: blank-line-separated paragraphs become separate Para blocks")
    void multiPara() {
        var blocks = TextParser.parse("First paragraph.\n\nSecond paragraph.");
        assertEquals(2, blocks.size());
        assertInstanceOf(Block.Para.class, blocks.get(0));
        assertInstanceOf(Block.Para.class, blocks.get(1));
    }

    @Test
    @DisplayName("T0: soft-wrapped paragraph lines join with a space")
    void softWrap() {
        var blocks = TextParser.parse("Line one\nline two\nline three.");
        var p = assertInstanceOf(Block.Para.class, blocks.get(0));
        assertEquals(new Inline.Text("Line one line two line three."), p.inlines().get(0));
    }

    // -----------------------------------------------------------------------
    // T1 — inline emphasis (bold, italic, code)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T1: bold tokenizes as Inline.Bold")
    void inlineBold() {
        var blocks = TextParser.parse("This is **bold** text.");
        var p = (Block.Para) blocks.get(0);
        assertEquals(3, p.inlines().size());
        assertInstanceOf(Inline.Text.class, p.inlines().get(0));
        var b = assertInstanceOf(Inline.Bold.class, p.inlines().get(1));
        assertEquals(new Inline.Text("bold"), b.inlines().get(0));
        assertInstanceOf(Inline.Text.class, p.inlines().get(2));
    }

    @Test
    @DisplayName("T1: italic tokenizes as Inline.Italic")
    void inlineItalic() {
        var blocks = TextParser.parse("An *emphasised* word.");
        var p = (Block.Para) blocks.get(0);
        var i = assertInstanceOf(Inline.Italic.class, p.inlines().get(1));
        assertEquals(new Inline.Text("emphasised"), i.inlines().get(0));
    }

    @Test
    @DisplayName("T1: inline code is a leaf; backticks contents are literal")
    void inlineCode() {
        var blocks = TextParser.parse("Run `mvn install` first.");
        var p = (Block.Para) blocks.get(0);
        var c = assertInstanceOf(Inline.Code.class, p.inlines().get(1));
        assertEquals("mvn install", c.text());
    }

    @Test
    @DisplayName("T1: code contents do not undergo further parsing")
    void codeContentsLiteral() {
        var blocks = TextParser.parse("Try `**not-bold**` inside.");
        var p = (Block.Para) blocks.get(0);
        var c = assertInstanceOf(Inline.Code.class, p.inlines().get(1));
        assertEquals("**not-bold**", c.text());
    }

    @Test
    @DisplayName("T1: unclosed bold marker throws ParseException")
    void unclosedBold() {
        var e = assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("This **never closes"));
        assertTrue(e.getMessage().contains("unclosed bold"));
    }

    @Test
    @DisplayName("T1: unclosed italic marker throws ParseException")
    void unclosedItalic() {
        assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("This *never closes"));
    }

    @Test
    @DisplayName("T1: unclosed code marker throws ParseException")
    void unclosedCode() {
        assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("This `never closes"));
    }

    @Test
    @DisplayName("T1: bold inside italic — and vice versa — is forbidden")
    void noEmphNesting() {
        // `***both***` would silently absorb as Bold(Text("*both*")) under
        // a permissive grammar — exactly the kind of ambiguity the audit
        // pushed us to refuse. The parser surfaces it explicitly.
        var e = assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("Try ***both*** at once."));
        assertTrue(e.getMessage().toLowerCase().contains("nest"));
    }

    // -----------------------------------------------------------------------
    // T2 — lists
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T2: bullet list with three items")
    void bulletList() {
        var blocks = TextParser.parse("- one\n- two\n- three");
        assertEquals(1, blocks.size());
        var ul = assertInstanceOf(Block.Bullets.class, blocks.get(0));
        assertEquals(3, ul.items().size());
        assertEquals(new Inline.Text("one"), ul.items().get(0).get(0));
    }

    @Test
    @DisplayName("T2: numbered list with three items")
    void numberedList() {
        var blocks = TextParser.parse("1. one\n2. two\n3. three");
        var ol = assertInstanceOf(Block.Numbered.class, blocks.get(0));
        assertEquals(3, ol.items().size());
    }

    @Test
    @DisplayName("T2: list items may contain inline emphasis")
    void listItemEmph() {
        var blocks = TextParser.parse("- **bold** item\n- *italic* item");
        var ul = (Block.Bullets) blocks.get(0);
        assertInstanceOf(Inline.Bold.class, ul.items().get(0).get(0));
        assertInstanceOf(Inline.Italic.class, ul.items().get(1).get(0));
    }

    // -----------------------------------------------------------------------
    // T3 — blockquote
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T3: blockquote joins consecutive '> ' lines into one Quote block")
    void blockquote() {
        var blocks = TextParser.parse("> Line one\n> Line two\n> Line three");
        var q = assertInstanceOf(Block.Quote.class, blocks.get(0));
        assertEquals(new Inline.Text("Line one Line two Line three"), q.inlines().get(0));
    }

    @Test
    @DisplayName("T3: blockquote inlines tokenize emphasis")
    void blockquoteEmph() {
        var blocks = TextParser.parse("> *quoted italic*");
        var q = (Block.Quote) blocks.get(0);
        assertInstanceOf(Inline.Italic.class, q.inlines().get(0));
    }

    // -----------------------------------------------------------------------
    // T4 — typed references
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T4: [label](#ref:name) tokenizes as Inline.Ref")
    void inlineRef() {
        var blocks = TextParser.parse("See [the RFC](#ref:rfc-19) for context.");
        var p = (Block.Para) blocks.get(0);
        var r = assertInstanceOf(Inline.Ref.class, p.inlines().get(1));
        assertEquals("the RFC", r.label());
        assertEquals("rfc-19", r.anchor());
    }

    @Test
    @DisplayName("T4: non-#ref: link target is a parse error")
    void nonRefLink() {
        var e = assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("Bad [link](https://example.com)."));
        assertTrue(e.getMessage().contains("#ref:"));
    }

    @Test
    @DisplayName("T4: empty #ref: anchor is a parse error")
    void emptyRefAnchor() {
        assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("Bad [link](#ref:)."));
    }

    @Test
    @DisplayName("T4: malformed link (missing close paren) is a parse error")
    void malformedLink() {
        assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("Bad [link](#ref:foo no-close"));
    }

    // -----------------------------------------------------------------------
    // What's NOT in the grammar — proves the discipline.
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Disallowed: ATX heading inside body becomes a literal '#' paragraph (not a heading)")
    void noHeadings() {
        // The grammar simply doesn't recognise '#' as a heading marker.
        // It comes through as literal text — but the author would notice
        // immediately because there's no rendered heading.
        var blocks = TextParser.parse("# Not a heading");
        var p = assertInstanceOf(Block.Para.class, blocks.get(0));
        assertEquals(new Inline.Text("# Not a heading"), p.inlines().get(0));
    }

    @Test
    @DisplayName("Disallowed: raw HTML angle brackets are literal text, not tokens")
    void noRawHtml() {
        // Angle brackets aren't recognised — they're literal characters.
        var blocks = TextParser.parse("Literal <div>content</div> appears as text.");
        var p = (Block.Para) blocks.get(0);
        assertInstanceOf(Inline.Text.class, p.inlines().get(0));
        assertTrue(((Inline.Text) p.inlines().get(0)).text().contains("<div>"));
    }

    @Test
    @DisplayName("Disallowed: triple-backtick fences are rejected as empty inline-code")
    void noCodeBlocks() {
        // The grammar doesn't recognise ``` as a code-block fence —
        // promote code blocks to a future typed CodeSegment. The
        // tokenizer sees the first two backticks as an empty inline-code
        // span and refuses it with a clear error.
        var e = assertThrows(TextParser.ParseException.class,
                () -> TextParser.parse("```\nnot-code\n```"));
        assertTrue(e.getMessage().toLowerCase().contains("code"));
    }

    // -----------------------------------------------------------------------
    // End-to-end — composed body with every grammar feature
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("End-to-end: a body using every T0..T4 feature parses cleanly")
    void everyFeature() {
        var body = """
                Opening paragraph with **bold**, *italic*, `code`, and [a ref](#ref:doc-tcv).

                A second paragraph that soft-wraps
                across two source lines.

                - bullet one
                - bullet two with **bold**

                1. numbered one
                2. numbered two with `code`

                > A quoted line with *emphasis*.
                """;
        List<Block> blocks = TextParser.parse(body);
        assertEquals(5, blocks.size());
        assertInstanceOf(Block.Para.class,     blocks.get(0));
        assertInstanceOf(Block.Para.class,     blocks.get(1));
        assertInstanceOf(Block.Bullets.class,  blocks.get(2));
        assertInstanceOf(Block.Numbered.class, blocks.get(3));
        assertInstanceOf(Block.Quote.class,    blocks.get(4));
    }
}
