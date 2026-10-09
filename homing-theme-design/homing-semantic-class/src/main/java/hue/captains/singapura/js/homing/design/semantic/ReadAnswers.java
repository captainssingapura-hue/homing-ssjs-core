package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * A theme's answers read from text, one to a line: a node, a state, a target, by their tokens -
 *
 * <pre>
 * # the roots, the button family, a danger button's own corner
 * root            rest      shape-corner
 * button          rest      shape-corner
 * danger-button   rest      shape-corner
 * root            disabled  effect-opacity
 * </pre>
 *
 * Blank lines and what follows a {@code #} are nothing. A line that names no node, state or target
 * of the taxonomy, or answers a state on a role, is refused with its number.
 */
public record ReadAnswers() implements StatelessFunctionalObject {

    public static final ReadAnswers INSTANCE = new ReadAnswers();

    public AnswerSet read(Taxonomy taxonomy, List<String> lines) {
        var tokens = SemanticTokens.INSTANCE;
        var out = new LinkedHashSet<Answer>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int hash = line.indexOf('#');
            String body = (hash < 0 ? line : line.substring(0, hash)).strip();
            if (body.isEmpty()) continue;
            String[] words = body.split("\\s+");
            if (words.length != 3)
                throw new IllegalArgumentException("line " + (i + 1) + ": '" + body + "' - an answer is a node, a state and a target");
            try {
                out.add(new Answer(tokens.castingOf(taxonomy, words[0]), tokens.stateOf(words[1]), tokens.targetOf(words[2])));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("line " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return new AnswerSet(out);
    }
}
