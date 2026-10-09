package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.regex.Pattern;

/**
 * What an answer may say: a body that means the same under any class it is copied to. It holds
 * declarations of its target's own properties - or the element's own values, a custom property -
 * blocks nested on the element itself ({@code &:hover}, {@code &::before}, {@code &[aria-pressed="true"]})
 * and blocks for a mode ({@code @media}, {@code @supports}). It names no class, reaches into no
 * child, and sets no property its target does not own. A target that is not CSS - a sound, an
 * asset - is said nothing of here.
 */
public record AnswerBody() implements StatelessFunctionalObject {

    public static final AnswerBody INSTANCE = new AnswerBody();

    /** One selector of a nested block: the element itself, with pseudo-classes, pseudo-elements and attributes only. */
    private static final Pattern SELF = Pattern.compile("&((::?[a-zA-Z-]+(\\([^()]*\\))?)|(\\[[^\\]]*\\]))*");

    /** Refuses a body its target cannot say; says nothing of an empty one. */
    public void check(Target target, String body) {
        if (body.isBlank()) return;
        if (target.carrier() != Carrier.CSS) throw new IllegalArgumentException(target.token() + " is no CSS target: an answer for it says nothing here");
        block(target, stripComments(body), 0);
    }

    private static void block(Target target, String text, int depth) {
        var buf = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char ch = text.charAt(i);
            if (ch == '"' || ch == '\'') { int end = closing(text, i); buf.append(text, i, end + 1); i = end + 1; continue; }
            if (ch == '(') { int end = matching(text, i, '(', ')'); buf.append(text, i, end + 1); i = end + 1; continue; }
            if (ch == ';') { declaration(target, buf.toString()); buf.setLength(0); i++; continue; }
            if (ch == '{') {
                int end = matching(text, i, '{', '}');
                nested(target, buf.toString().strip(), text.substring(i + 1, end), depth);
                buf.setLength(0);
                i = end + 1;
                continue;
            }
            if (ch == '}') throw new IllegalArgumentException("a '}' closes nothing");
            buf.append(ch);
            i++;
        }
        if (!buf.toString().isBlank()) declaration(target, buf.toString());
    }

    private static void nested(Target target, String selector, String inner, int depth) {
        if (depth > 3) throw new IllegalArgumentException("nested too deep");
        if (selector.startsWith("@media") || selector.startsWith("@supports")) { block(target, inner, depth + 1); return; }
        for (String part : selector.split(",")) {
            String p = part.strip();
            if (!SELF.matcher(p).matches())
                throw new IllegalArgumentException("'" + selector + "' is not the element itself: a body nests only on & with its own states, pseudo-elements and attributes");
        }
        block(target, inner, depth + 1);
    }

    private static void declaration(Target target, String text) {
        String d = text.strip();
        if (d.isEmpty()) return;
        int colon = d.indexOf(':');
        if (colon <= 0) throw new IllegalArgumentException("'" + d + "' is no declaration");
        String property = d.substring(0, colon).strip().toLowerCase(java.util.Locale.ROOT);
        if (property.startsWith("--")) return;   // the element's own value
        if (!target.properties().contains(property))
            throw new IllegalArgumentException(target.token() + " does not own " + property + "; it owns " + String.join(", ", new java.util.TreeSet<>(target.properties())));
    }

    private static int closing(String text, int open) {
        char q = text.charAt(open);
        for (int i = open + 1; i < text.length(); i++) {
            if (text.charAt(i) == '\\') { i++; continue; }
            if (text.charAt(i) == q) return i;
        }
        throw new IllegalArgumentException("a string is never closed");
    }

    private static int matching(String text, int open, char o, char c) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"' || ch == '\'') { i = closing(text, i); continue; }
            if (ch == o) depth++;
            else if (ch == c && --depth == 0) return i;
        }
        throw new IllegalArgumentException("a '" + o + "' is never closed");
    }

    private static String stripComments(String text) { return text.replaceAll("(?s)/\\*.*?\\*/", " "); }
}
