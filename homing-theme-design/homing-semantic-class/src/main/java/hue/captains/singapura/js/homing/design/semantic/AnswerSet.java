package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A theme's answers, each with what it says: the CSS body written for it - declarations of its
 * target's properties, the element's own states nested on {@code &}, the modes in {@code @media}
 * - and never a selector of its own, so the same body means the same under any class it is worn
 * as. An answer whose body is empty says nothing, and is still an answer: a silence ends the walk
 * as any answer does.
 *
 * @param bodies every answer, and its body as written; empty for a silence
 */
public record AnswerSet(Map<Answer, String> bodies) implements Answers, ValueObject {

    public AnswerSet { bodies = Collections.unmodifiableMap(new LinkedHashMap<>(bodies)); }

    @Override public boolean answers(Casting node, State state, Target target) {
        if (state != State.REST && !node.standalone()) return false;   // never answered: a state is no role's
        return bodies.containsKey(new Answer(node, state, target));
    }

    /** What the theme says for a node in a state on a target: its body; empty when it says nothing, or does not answer. */
    public String body(Casting node, State state, Target target) {
        if (state != State.REST && !node.standalone()) return "";
        return bodies.getOrDefault(new Answer(node, state, target), "");
    }
}
