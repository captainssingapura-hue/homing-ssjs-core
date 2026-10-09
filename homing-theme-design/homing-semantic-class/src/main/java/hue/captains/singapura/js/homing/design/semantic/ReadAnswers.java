package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A theme's answers read from text: a node, a state, a target by their tokens, and what the theme
 * says for them in braces - on the line, or over as many as it takes:
 *
 * <pre>
 * # the roots, the button's corner, a danger button's corner of its own
 * root            rest      shape-corner    { border-radius: 0; }
 * button          rest      shape-corner    { border-radius: 3px; }
 * danger-button   rest      shape-corner    { border-radius: 2px; }
 * control         disabled  effect-opacity  { opacity: .45; }
 * button          rest      size-inset      {
 *     padding-block: calc(8px * pow(1.3, var(--size)));
 *     padding-inline: calc(18px * pow(1.3, var(--size)));
 * }
 * root            rest      sound-cue
 * </pre>
 *
 * A line without braces is an answer that says nothing - a silence, which still ends the walk.
 * Blank lines are nothing, and so is a line that begins with {@code #}; a {@code #} anywhere else
 * is the body's own (a colour). Refused, with the line it starts on: a node, state or target the
 * taxonomy has not; a state the target offers no slot for; a state answered on a role; an answer
 * given twice; and a body its target cannot say ({@link AnswerBody}).
 */
public record ReadAnswers() implements StatelessFunctionalObject {

    public static final ReadAnswers INSTANCE = new ReadAnswers();

    public AnswerSet read(Taxonomy taxonomy, List<String> lines) {
        var tokens = SemanticTokens.INSTANCE;
        var out = new LinkedHashMap<Answer, String>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String stripped = line.strip();
            if (stripped.isEmpty() || stripped.startsWith("#")) continue;
            int start = i;
            int brace = line.indexOf('{');
            String head = (brace < 0 ? line : line.substring(0, brace)).strip();
            String body = "";
            if (brace >= 0) {
                var text = new StringBuilder(line.substring(brace));
                while (depth(text) > 0 && i + 1 < lines.size()) text.append('\n').append(lines.get(++i));
                if (depth(text) != 0) throw new IllegalArgumentException("line " + (start + 1) + ": its '{' is never closed");
                String block = text.toString().strip();
                if (!block.endsWith("}")) throw new IllegalArgumentException("line " + (start + 1) + ": nothing may follow the body's '}'");
                body = block.substring(1, block.length() - 1).strip();
            }
            String[] words = head.split("\\s+");
            if (words.length != 3)
                throw new IllegalArgumentException("line " + (start + 1) + ": '" + head + "' - an answer is a node, a state and a target");
            try {
                var answer = new Answer(tokens.castingOf(taxonomy, words[0]), tokens.stateOf(words[1]), tokens.targetOf(words[2]));
                Target target = answer.target();
                if (!target.states().contains(answer.state()))
                    throw new IllegalArgumentException(target.token() + " offers no " + words[1] + "; it offers "
                            + String.join(", ", target.states().stream().map(tokens::state).toList()));
                AnswerBody.INSTANCE.check(target, body);
                if (out.putIfAbsent(answer, body) != null)
                    throw new IllegalArgumentException(head + " is answered twice");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("line " + (start + 1) + ": " + e.getMessage(), e);
            }
        }
        return new AnswerSet(out);
    }

    /** How many braces are open at the end of the text, strings and comments set aside. */
    private static int depth(CharSequence text) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quote != 0) { if (ch == '\\') i++; else if (ch == quote) quote = 0; continue; }
            if (ch == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                int end = text.toString().indexOf("*/", i + 2);
                if (end < 0) return depth + 1;   // a comment still open holds the body open
                i = end + 1;
                continue;
            }
            if (ch == '"' || ch == '\'') quote = ch;
            else if (ch == '{') depth++;
            else if (ch == '}') depth--;
        }
        return depth;
    }
}
