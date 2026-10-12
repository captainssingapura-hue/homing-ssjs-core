package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a concrete leaf says for one target its leaf owns (RFC 0067, appendix Virtual Leaves and Designs):
 * its own CSS, nothing, or another concrete leaf's - reuse always said. The target is the type parameter,
 * so an answer for one target cannot stand in for another's.
 *
 * @param <T> the target answered
 */
public sealed interface Drawing<T extends Target> extends ValueObject permits Drawing.Own, Drawing.None, Drawing.As {

    /** The target it answers. */
    T target();

    /**
     * Its own CSS: a body for each state it answers, at rest the look - declarations of the target's
     * properties only ({@link AnswerBody}), in states the target offers a slot for. Refused as it is
     * written, as a style's answer is.
     */
    record Own<T extends Target>(T target, Answer.Own<T> answer) implements Drawing<T> {
        public Own {
            Objects.requireNonNull(target, "Drawing.Own.target");
            Objects.requireNonNull(answer, "Drawing.Own.answer");
            answer.bodies().forEach((state, body) -> {
                if (!target.states().contains(state))
                    throw new IllegalArgumentException(target.token() + " offers no " + SemanticTokens.INSTANCE.state(state) + "; it offers "
                            + String.join(", ", target.states().stream().map(SemanticTokens.INSTANCE::state).toList()));
                try {
                    AnswerBody.INSTANCE.check(target, body);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(target.token() + " " + SemanticTokens.INSTANCE.state(state) + ": " + e.getMessage(), e);
                }
            });
        }

        /** The look alone: a body at rest. */
        public Own(T target, String rest) { this(target, new Answer.Own<>(rest)); }
    }

    /**
     * Nothing: no rule draws the target in this theme. Its class is worn all the same - what a design
     * puts on an element is the same in every theme - and says nothing.
     */
    record None<T extends Target>(T target) implements Drawing<T> {
        public None { Objects.requireNonNull(target, "Drawing.None.target"); }
    }

    /**
     * Another concrete leaf's drawing of the same target - of the same leaf, in another design - in the
     * theme worn: this design's cursor is the house's cursor.
     *
     * @param to the concrete leaf whose drawing this one is
     */
    record As<T extends Target>(T target, ConcreteComponent<?, ?, ?> to) implements Drawing<T> {
        public As {
            Objects.requireNonNull(target, "Drawing.As.target");
            Objects.requireNonNull(to, "Drawing.As.to");
        }
    }
}
