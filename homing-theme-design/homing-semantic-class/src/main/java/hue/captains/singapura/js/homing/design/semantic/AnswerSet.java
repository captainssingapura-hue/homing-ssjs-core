package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.LinkedHashSet;
import java.util.Set;

/** A theme's answers as a set: what it answers, and nothing of how. */
public record AnswerSet(Set<Answer> answers) implements Answers, ValueObject {

    public AnswerSet { answers = java.util.Collections.unmodifiableSet(new LinkedHashSet<>(answers)); }

    @Override public boolean answers(Casting node, State state, Target target) {
        if (state != State.REST && !node.standalone()) return false;   // never answered: a state is no role's
        return answers.contains(new Answer(node, state, target));
    }
}
