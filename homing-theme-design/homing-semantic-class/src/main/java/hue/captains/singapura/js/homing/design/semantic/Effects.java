package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * An animation effect's CSS, judged and written (RFC 0050 · Episode 2, appendix The Copied Range). A design
 * names an effect; what it is in CSS is the effect's, sealed, and written here - once in a sheet:
 *
 * <ul>
 *   <li><b>the class it is worn by:</b> its moving declarations and its motion - the effect's keyframes, by
 *       their own name, at its timing - and, where motion is reduced, its still dual: the still declarations,
 *       what only the moving form declares unset, and no motion;</li>
 *   <li><b>its still dual's class:</b> the effect without any motion - what an element is given when the effect
 *       is switched off, a class swapped and nothing more;</li>
 *   <li><b>its keyframes,</b> named for the effect, {@code effect-marching-ants}, written once in a sheet.</li>
 * </ul>
 *
 * An effect is refused unless its CSS is its own to write: flat declarations of the element itself, none of the
 * motion's own properties - its motion is written from its frames and timing - reading only its degrees and the
 * registered colours it is drawn in, naming no colour; frames that are frames; frames and timing together or
 * not at all.
 */
public record Effects() implements StatelessFunctionalObject {

    public static final Effects INSTANCE = new Effects();

    /** A timing is words and numbers: a duration, an easing, a count, a direction - no declaration, no block, no variable. */
    private static final Pattern NOT_TIMING = Pattern.compile("[;:{}]|var\\(");

    /** The properties an effect takes - every one its declarations and frames write; refused if its CSS is not its own to write. */
    public Set<String> takes(AnimationEffect effect) {
        Objects.requireNonNull(effect, "the effect");
        String who = "the effect " + effect.token();
        var rights = new AnswerBody.Rights(who, null, effect.drawnIn());
        var body = AnswerBody.INSTANCE;
        var taken = new TreeSet<String>();
        taken.addAll(body.declarations(rights, effect.moving()));
        taken.addAll(body.declarations(rights, effect.still()));
        Set<String> moved = body.frames(rights, effect.frames());
        if (effect.frames().isBlank() != effect.timing().isBlank())
            throw new IllegalArgumentException(who + " has " + (effect.frames().isBlank() ? "a timing and no frames" : "frames and no timing")
                    + ": they come together, or the effect never moves");
        if (effect.moves() && moved.isEmpty()) throw new IllegalArgumentException(who + "'s frames move nothing");
        if (NOT_TIMING.matcher(effect.timing()).find())
            throw new IllegalArgumentException(who + "'s timing is '" + effect.timing().strip() + "': a duration, an easing, a count and a direction - "
                    + "no declaration, no block, no variable");
        taken.addAll(moved);
        return Collections.unmodifiableSet(taken);
    }

    /** Refuses an effect whose CSS is not its own to write. */
    public void check(AnimationEffect effect) { takes(effect); }

    /** The keyframes' name: {@code effect-marching-ants}. */
    public String name(AnimationEffect effect) { return SemanticTokens.INSTANCE.effect(effect); }

    /** The body of the class an effect is worn by: its moving declarations and its motion, and its still dual where motion is reduced. */
    public String moving(AnimationEffect effect) {
        check(effect);
        var lines = new ArrayList<>(declarations(effect.moving()));
        if (!effect.moves()) {
            lines.add("animation: none;");
            return String.join("\n", lines);
        }
        lines.add("animation: " + name(effect) + " " + oneLine(effect.timing()) + ";");
        var reduced = new ArrayList<>(declarations(effect.still()));
        var still = properties(effect.still());
        for (String p : properties(effect.moving())) if (!still.contains(p)) reduced.add(p + ": unset;");
        reduced.add("animation: none;");
        lines.add("@media (prefers-reduced-motion: reduce) {");
        for (String r : reduced) lines.add("    " + r);
        lines.add("}");
        return String.join("\n", lines);
    }

    /** The body of an effect's still dual's class: the effect without any motion. */
    public String still(AnimationEffect effect) {
        check(effect);
        var lines = new ArrayList<>(declarations(effect.still()));
        lines.add("animation: none;");
        return String.join("\n", lines);
    }

    /** The effect's keyframes, written once in a sheet; nothing for an effect that never moves. */
    public String keyframes(AnimationEffect effect) {
        check(effect);
        if (!effect.moves()) return "";
        var lines = new ArrayList<String>();
        lines.add("@keyframes " + name(effect) + " {");
        effect.frames().strip().lines().map(String::strip).filter(l -> !l.isEmpty()).forEach(l -> lines.add("    " + l));
        lines.add("}");
        return String.join("\n", lines);
    }

    /** The keyframes of every effect given that moves, each once, in the order first given. */
    public String keyframes(Iterable<AnimationEffect> effects) {
        var seen = new LinkedHashSet<String>();
        var out = new ArrayList<String>();
        for (AnimationEffect e : effects)
            if (e.moves() && seen.add(name(e))) out.add(keyframes(e));
        return String.join("\n", out);
    }

    /** Flat declarations, one to a line, each closed by its semicolon. */
    private static List<String> declarations(String text) {
        var out = new ArrayList<String>();
        var buf = new StringBuilder();
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quote != 0) { buf.append(c); if (c == quote) quote = 0; continue; }
            if (c == '"' || c == '\'') { quote = c; buf.append(c); continue; }
            if (c == '(') depth++;
            if (c == ')') depth--;
            if (c == ';' && depth == 0) { add(out, buf); continue; }
            buf.append(c);
        }
        add(out, buf);
        return out;
    }

    private static void add(List<String> out, StringBuilder buf) {
        String d = buf.toString().strip().replaceAll("\\s+", " ");
        buf.setLength(0);
        if (!d.isEmpty()) out.add(d + ";");
    }

    /** The properties flat declarations declare. */
    private static Set<String> properties(String text) {
        var out = new LinkedHashSet<String>();
        for (String d : declarations(text)) out.add(d.substring(0, d.indexOf(':')).strip().toLowerCase(java.util.Locale.ROOT));
        return out;
    }

    private static String oneLine(String text) { return text.strip().replaceAll("\\s+", " "); }
}
