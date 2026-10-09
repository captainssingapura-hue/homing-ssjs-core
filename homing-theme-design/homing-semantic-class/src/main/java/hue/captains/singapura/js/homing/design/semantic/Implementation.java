package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * One semantic class a style or a palette implements: a node of the taxonomy - a casting - a
 * target, and the answer, typed by the target so the two cannot part. Refused as it is written: a
 * state the target offers no slot for, a state answered on a role (a state is a treatment, common
 * to every role), a body its target cannot say, and a class redirected to itself.
 *
 * @param node   the casting answered: a component or a branch on its own, or a component in a role
 * @param target the target leaf
 * @param answer what is said for them
 * @param <T>    the target leaf
 */
public record Implementation<T extends Target>(Casting node, T target, Answer<T> answer) implements ValueObject {

    public Implementation {
        Objects.requireNonNull(node, "Implementation.node");
        Objects.requireNonNull(target, "Implementation.target");
        Objects.requireNonNull(answer, "Implementation.answer");
        var tokens = SemanticTokens.INSTANCE;
        String where = tokens.casting(node) + " " + target.token();
        if (answer instanceof Answer.Own<T> own)
            own.bodies().forEach((state, body) -> {
                if (!target.states().contains(state))
                    throw new IllegalArgumentException(where + ": " + target.token() + " offers no " + tokens.state(state) + "; it offers "
                            + String.join(", ", target.states().stream().map(tokens::state).toList()));
                if (state != State.REST && !node.standalone())
                    throw new IllegalArgumentException(where + ": " + node + " is answered " + tokens.state(state)
                            + ": a state is a treatment, common to every role, and is answered on the component on its own");
                try {
                    AnswerBody.INSTANCE.check(target, body);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(where + " " + tokens.state(state) + ": " + e.getMessage(), e);
                }
            });
        if (answer instanceof Answer.Redirect<T> r && r.to().equals(node))
            throw new IllegalArgumentException(where + " is redirected to itself");
    }

    /** A component or a branch on its own. */
    public Implementation(Taxon node, T target, Answer<T> answer) { this(new Casting(node), target, answer); }
}
