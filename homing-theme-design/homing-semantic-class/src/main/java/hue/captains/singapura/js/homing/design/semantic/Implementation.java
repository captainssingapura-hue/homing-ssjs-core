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
 * target kept only for the old design system, a state the target offers no slot for, a state
 * answered on a role (a state is a treatment, common to every role), a body its target cannot say
 * ({@link AnswerBody}), a class redirected to itself, an animation answered with a body rather than an effect,
 * and an effect that takes a property: on a component every target's class is worn, so what the effect takes
 * another answer writes - only a design's virtual leaf, which owns what it owns, may wear such an effect.
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
        if (answer instanceof Answer.FromBase<T> f)
            f.only().forEach(state -> {
                if (!target.states().contains(state))
                    throw new IllegalArgumentException(where + ": asks its base " + tokens.state(state) + ", which " + target.token()
                            + " does not offer; it offers " + String.join(", ", target.states().stream().map(tokens::state).toList()));
            });
        if (answer instanceof Answer.Redirect<T> r && r.to().equals(node))
            throw new IllegalArgumentException(where + " is redirected to itself");
        if (target instanceof Target.Animation && answer instanceof Answer.Own<T>)
            throw new IllegalArgumentException(where + " is answered with a body: an animation is answered with an effect, whole (Answer.Animated)");
        if (answer instanceof Answer.Animated a && !Effects.INSTANCE.takes(a.effect()).isEmpty())
            throw new IllegalArgumentException(where + " is " + a.effect().token() + ", which takes " + String.join(", ", Effects.INSTANCE.takes(a.effect()))
                    + ": on a component every target's class is worn, and another answer writes what the effect takes - only a design's"
                    + " virtual leaf, which owns what it owns, may wear it");
    }

    /** A component or a branch on its own. */
    public Implementation(Taxon node, T target, Answer<T> answer) { this(new Casting(node), target, answer); }
}
