package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A style-aware file read as what it says (RFC 0067, appendix Two Classes per Leaf): its rules, each rule's
 * declarations and the rules nested in it, and its at-rules, each with the line it starts on. Only as much CSS as the
 * check needs - comments dropped, strings and brackets kept whole - and what cannot be read is said, never guessed at.
 */
public record ReadCss() implements StatelessFunctionalObject {

    public static final ReadCss INSTANCE = new ReadCss();

    /** What a block holds. */
    public sealed interface Item permits Rule, Declaration, AtRule {
        /** The line it starts on, from 1. */
        int line();
    }

    /** A rule: its selector as written, trimmed, and what its block holds. */
    public record Rule(String selector, int line, List<Item> body) implements Item, ValueObject {
        public Rule { body = List.copyOf(body); }
    }

    /** A declaration: its property and its value, each trimmed. */
    public record Declaration(String property, String value, int line) implements Item, ValueObject {}

    /** An at-rule: what it says after the {@code @}, and its block - none for a statement. */
    public record AtRule(String prelude, int line, List<Item> body) implements Item, ValueObject {
        public AtRule { body = body == null ? null : List.copyOf(body); }
    }

    /**
     * A file read.
     *
     * @param items    what it holds at the top
     * @param problems what could not be read, each with its line
     */
    public record Read(List<Item> items, List<String> problems) implements ValueObject {
        public Read { items = List.copyOf(items); problems = List.copyOf(problems); }
    }

    public Read read(String css) {
        var problems = new ArrayList<String>();
        String text = withoutComments(css, problems);
        var at = new int[]{0};
        List<Item> items = block(text, at, problems, true);
        return new Read(items, problems);
    }

    /** Comments blanked, their line breaks kept, so every line is where it was. */
    private static String withoutComments(String css, List<String> problems) {
        var out = new StringBuilder(css.length());
        int i = 0;
        char quote = 0;
        while (i < css.length()) {
            char c = css.charAt(i);
            if (quote != 0) {
                out.append(c);
                if (c == '\\' && i + 1 < css.length()) out.append(css.charAt(++i));
                else if (c == quote) quote = 0;
                i++;
            } else if (c == '"' || c == '\'') {
                quote = c;
                out.append(c);
                i++;
            } else if (c == '/' && i + 1 < css.length() && css.charAt(i + 1) == '*') {
                int end = css.indexOf("*/", i + 2);
                if (end < 0) {
                    problems.add(line(css, i) + ": a comment that never ends");
                    end = css.length() - 2;
                }
                for (int j = i; j < end + 2; j++) out.append(css.charAt(j) == '\n' ? '\n' : ' ');
                i = end + 2;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    /** A block's items, up to its closing brace - or the end, at the top. */
    private static List<Item> block(String text, int[] at, List<String> problems, boolean top) {
        var items = new ArrayList<Item>();
        while (true) {
            skipSpace(text, at);
            if (at[0] >= text.length()) {
                if (!top) problems.add(line(text, text.length() - 1) + ": a block that never closes");
                return items;
            }
            if (text.charAt(at[0]) == '}') {
                if (top) {
                    problems.add(line(text, at[0]) + ": a closing brace with nothing open");
                    at[0]++;
                    continue;
                }
                at[0]++;
                return items;
            }
            int start = at[0];
            char ended = chunk(text, at);
            String said = text.substring(start, at[0]).trim();
            int line = line(text, start);
            switch (ended) {
                case '{' -> {
                    at[0]++;
                    List<Item> body = block(text, at, problems, false);
                    items.add(said.startsWith("@") ? new AtRule(said.substring(1), line, body) : new Rule(said, line, body));
                }
                case ';' -> {
                    at[0]++;
                    items.add(statement(said, line, problems));
                }
                default -> {
                    if (!said.isEmpty()) items.add(statement(said, line, problems));
                }
            }
        }
    }

    private static Item statement(String said, int line, List<String> problems) {
        if (said.startsWith("@")) return new AtRule(said.substring(1), line, null);
        int colon = colon(said);
        if (colon < 0) {
            problems.add(line + ": not a declaration: " + said);
            return new Declaration(said, "", line);
        }
        return new Declaration(said.substring(0, colon).trim(), said.substring(colon + 1).trim(), line);
    }

    /** To the next {@code ;}, {@code {} or {@code }} outside brackets and strings; the one it stopped at, or 0 at the end. */
    private static char chunk(String text, int[] at) {
        int depth = 0;
        char quote = 0;
        for (; at[0] < text.length(); at[0]++) {
            char c = text.charAt(at[0]);
            if (quote != 0) {
                if (c == '\\') at[0]++;
                else if (c == quote) quote = 0;
            } else if (c == '"' || c == '\'') quote = c;
            else if (c == '(' || c == '[') depth++;
            else if (c == ')' || c == ']') depth--;
            else if (depth <= 0 && (c == ';' || c == '{' || c == '}')) return c;
        }
        return 0;
    }

    /** The first colon outside brackets and strings. */
    private static int colon(String said) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < said.length(); i++) {
            char c = said.charAt(i);
            if (quote != 0) {
                if (c == '\\') i++;
                else if (c == quote) quote = 0;
            } else if (c == '"' || c == '\'') quote = c;
            else if (c == '(' || c == '[') depth++;
            else if (c == ')' || c == ']') depth--;
            else if (c == ':' && depth == 0) return i;
        }
        return -1;
    }

    private static void skipSpace(String text, int[] at) {
        while (at[0] < text.length() && Character.isWhitespace(text.charAt(at[0]))) at[0]++;
    }

    private static int line(String text, int index) {
        int line = 1;
        for (int i = 0; i < Math.min(index, text.length()); i++) if (text.charAt(i) == '\n') line++;
        return line;
    }
}
