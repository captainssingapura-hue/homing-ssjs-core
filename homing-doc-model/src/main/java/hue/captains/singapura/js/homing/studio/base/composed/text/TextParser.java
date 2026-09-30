package hue.captains.singapura.js.homing.studio.base.composed.text;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0018 / Phase 4 — strict parser for {@code TextSegment} prose
 * (the {@code .mdad+} grammar discovered by audit from existing
 * prose use).
 *
 * <p>Strict by design — every accepted form is documented; everything
 * else fails parsing with a precise line + column. The parser is the
 * conformance gate: an unparseable body never lands in a Doc.</p>
 *
 * <h3>Grammar (informal)</h3>
 * <pre>
 *   document := block+
 *   block    := para | bullets | numbered | quote
 *
 *   para     := line ('\n' line)*   (terminated by blank line or EOF)
 *   bullets  := bullets-item+
 *   bullets-item   := '- ' inline-text                 (one line per item)
 *   numbered := numbered-item+
 *   numbered-item  := digits '. ' inline-text          (one line per item)
 *   quote    := ('> ' inline-text)+                    (consecutive '>'-prefixed lines joined)
 *
 *   inline-text := (text | bold | italic | code | ref)+
 *   bold        := '**' run-without-bold-or-italic '**'
 *   italic      := '*'  run-without-bold-or-italic '*'
 *   code        := '`'  raw-text '`'                   (literal — no further parsing)
 *   ref         := '[' label-text ']' '(' '#ref:' name ')'
 *   text        := any other character runs
 * </pre>
 *
 * <h3>Strict rules</h3>
 * <ul>
 *   <li>No headings — segment {@code title} carries the section heading.</li>
 *   <li>No code blocks — promote to a future typed {@code CodeSegment}.</li>
 *   <li>No raw HTML — {@code <}, {@code >} outside the {@code > } quote
 *       prefix are literal text. (The grammar simply doesn't recognise
 *       angle-bracket markup — it's not banned, it's not a token.)</li>
 *   <li>Bold and italic do not nest each other (avoids {@code ***} ambiguity).</li>
 *   <li>Backticked code is leaf — no nested parsing inside.</li>
 *   <li>{@code [label](#ref:name)} is the only link form. Any other
 *       {@code [...](...)} is a parse error.</li>
 * </ul>
 *
 * @since RFC 0018 Phase 4
 */
public final class TextParser {

    private TextParser() {}

    /** Parse the body. Throws {@link ParseException} on any malformed input. */
    public static List<Block> parse(String body) {
        if (body == null) throw new ParseException(1, 1, "body is null");
        // Normalize line endings; strip leading/trailing blank lines but keep blanks within.
        String norm = body.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = norm.split("\n", -1);
        // Walk blocks.
        var out = new ArrayList<Block>();
        int i = 0;
        while (i < lines.length) {
            // skip blank lines
            while (i < lines.length && lines[i].isBlank()) i++;
            if (i >= lines.length) break;

            String line = lines[i];
            // Classify by leading marker.
            if (line.startsWith("- ")) {
                int[] consumed = {i};
                out.add(parseBullets(lines, consumed));
                i = consumed[0];
            } else if (matchesOrderedStart(line)) {
                int[] consumed = {i};
                out.add(parseNumbered(lines, consumed));
                i = consumed[0];
            } else if (line.startsWith("> ") || line.equals(">")) {
                int[] consumed = {i};
                out.add(parseQuote(lines, consumed));
                i = consumed[0];
            } else {
                int[] consumed = {i};
                out.add(parsePara(lines, consumed));
                i = consumed[0];
            }
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Block parsers — each returns the produced Block and advances the
    // [i] cursor (passed as a single-element array for in/out semantics).
    // -----------------------------------------------------------------------

    private static Block parsePara(String[] lines, int[] cursor) {
        int i = cursor[0];
        var buf = new StringBuilder();
        while (i < lines.length && !lines[i].isBlank()
                && !lines[i].startsWith("- ")
                && !matchesOrderedStart(lines[i])
                && !lines[i].startsWith("> ") && !lines[i].equals(">")) {
            if (buf.length() > 0) buf.append(' ');
            buf.append(lines[i].strip());
            i++;
        }
        cursor[0] = i;
        return new Block.Para(parseInlines(buf.toString(), startLineNumberOf(cursor[0], i)));
    }

    private static Block parseBullets(String[] lines, int[] cursor) {
        int i = cursor[0];
        var items = new ArrayList<List<Inline>>();
        while (i < lines.length && lines[i].startsWith("- ")) {
            String content = lines[i].substring(2);
            items.add(parseInlines(content, i + 1));
            i++;
        }
        cursor[0] = i;
        return new Block.Bullets(items);
    }

    private static Block parseNumbered(String[] lines, int[] cursor) {
        int i = cursor[0];
        var items = new ArrayList<List<Inline>>();
        while (i < lines.length && matchesOrderedStart(lines[i])) {
            // Strip "N. " prefix (any number of digits).
            int dot = lines[i].indexOf(". ");
            String content = lines[i].substring(dot + 2);
            items.add(parseInlines(content, i + 1));
            i++;
        }
        cursor[0] = i;
        return new Block.Numbered(items);
    }

    private static Block parseQuote(String[] lines, int[] cursor) {
        int i = cursor[0];
        var buf = new StringBuilder();
        while (i < lines.length && (lines[i].startsWith("> ") || lines[i].equals(">"))) {
            String content = lines[i].equals(">") ? "" : lines[i].substring(2);
            if (buf.length() > 0) buf.append(' ');
            buf.append(content);
            i++;
        }
        cursor[0] = i;
        return new Block.Quote(parseInlines(buf.toString().strip(), i));
    }

    // -----------------------------------------------------------------------
    // Inline tokenizer — single-pass; bold/italic/code/ref/text.
    // -----------------------------------------------------------------------

    private static List<Inline> parseInlines(String s, int lineNumber) {
        return parseInlinesImpl(s, lineNumber, true /* allow bold/italic */);
    }

    /** Inner — when {@code allowEmph} is false, {@code *} and {@code **}
     *  are taken as literal characters (used inside a bold/italic span to
     *  forbid nesting). */
    private static List<Inline> parseInlinesImpl(String s, int lineNumber, boolean allowEmph) {
        var out = new ArrayList<Inline>();
        var text = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            // ---- bold ----
            if (c == '*' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
                if (!allowEmph) {
                    throw new ParseException(lineNumber, i + 1,
                            "'**' (bold) cannot nest inside bold or italic — "
                                    + "the grammar forbids emphasis nesting to keep the '***' "
                                    + "ambiguity from surfacing");
                }
                int end = s.indexOf("**", i + 2);
                if (end < 0) {
                    throw new ParseException(lineNumber, i + 1,
                            "unclosed bold marker '**' starting here");
                }
                flushText(out, text);
                String inner = s.substring(i + 2, end);
                out.add(new Inline.Bold(parseInlinesImpl(inner, lineNumber, false)));
                i = end + 2;
                continue;
            }
            // ---- italic ----
            if (c == '*') {
                if (!allowEmph) {
                    throw new ParseException(lineNumber, i + 1,
                            "'*' (italic) cannot nest inside bold or italic — "
                                    + "the grammar forbids emphasis nesting");
                }
                int end = s.indexOf('*', i + 1);
                if (end < 0) {
                    throw new ParseException(lineNumber, i + 1,
                            "unclosed italic marker '*' starting here");
                }
                flushText(out, text);
                String inner = s.substring(i + 1, end);
                out.add(new Inline.Italic(parseInlinesImpl(inner, lineNumber, false)));
                i = end + 1;
                continue;
            }
            // ---- inline code ----
            if (c == '`') {
                int end = s.indexOf('`', i + 1);
                if (end < 0) {
                    throw new ParseException(lineNumber, i + 1,
                            "unclosed inline-code marker '`' starting here");
                }
                if (end == i + 1) {
                    throw new ParseException(lineNumber, i + 1,
                            "empty inline-code span '``' is not permitted — "
                                    + "and triple-backtick code fences are not part of the "
                                    + "grammar; promote code blocks to a future CodeSegment kind");
                }
                flushText(out, text);
                out.add(new Inline.Code(s.substring(i + 1, end)));
                i = end + 1;
                continue;
            }
            // ---- reference link ----
            if (c == '[') {
                int closeBr = s.indexOf(']', i + 1);
                if (closeBr < 0 || closeBr + 1 >= s.length() || s.charAt(closeBr + 1) != '(') {
                    throw new ParseException(lineNumber, i + 1,
                            "malformed reference link — expected '[label](#ref:name)'");
                }
                int closePa = s.indexOf(')', closeBr + 2);
                if (closePa < 0) {
                    throw new ParseException(lineNumber, i + 1,
                            "unclosed reference link target — expected ')'");
                }
                String label  = s.substring(i + 1, closeBr);
                String target = s.substring(closeBr + 2, closePa);
                if (!target.startsWith("#ref:")) {
                    throw new ParseException(lineNumber, closeBr + 3,
                            "reference link target must start with '#ref:'; got '" + target + "'");
                }
                String anchor = target.substring("#ref:".length());
                if (anchor.isBlank()) {
                    throw new ParseException(lineNumber, closeBr + 3,
                            "reference link target '#ref:' has empty name");
                }
                flushText(out, text);
                out.add(new Inline.Ref(label, anchor));
                i = closePa + 1;
                continue;
            }
            // ---- literal text ----
            text.append(c);
            i++;
        }
        flushText(out, text);
        return out;
    }

    private static void flushText(List<Inline> out, StringBuilder text) {
        if (text.length() > 0) {
            out.add(new Inline.Text(text.toString()));
            text.setLength(0);
        }
    }

    private static boolean matchesOrderedStart(String line) {
        // Accept any-digit-count followed by ". ".
        int j = 0;
        while (j < line.length() && Character.isDigit(line.charAt(j))) j++;
        return j > 0 && j + 1 < line.length()
                && line.charAt(j) == '.' && line.charAt(j + 1) == ' ';
    }

    private static int startLineNumberOf(int oldCursor, int newCursor) {
        return oldCursor + 1; // 1-based for error messages
    }

    // -----------------------------------------------------------------------
    // ParseException — surfaces the offending line + column so authors
    // can fix at construction time.
    // -----------------------------------------------------------------------

    public static final class ParseException extends RuntimeException {
        public final int line;
        public final int column;

        public ParseException(int line, int column, String message) {
            super("TextSegment parse error at line " + line + ", col " + column + ": " + message);
            this.line = line;
            this.column = column;
        }
    }
}
