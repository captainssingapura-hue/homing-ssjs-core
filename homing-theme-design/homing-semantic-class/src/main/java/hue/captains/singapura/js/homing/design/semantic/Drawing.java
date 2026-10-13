package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a concrete leaf says for one target its leaf owns (RFC 0067, appendix Virtual Leaves and Designs):
 * its own CSS, nothing, another concrete leaf's, or the theme's answer for a component of the taxonomy - reuse
 * always said - and for an animation, an effect, whole. The target is the type parameter, so an answer for one target cannot stand in for another's.
 *
 * @param <T> the target answered
 */
public sealed interface Drawing<T extends Target> extends ValueObject permits Drawing.Own, Drawing.None, Drawing.As, Drawing.Of, Drawing.Animated {

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
            if (target instanceof Target.Animation)
                throw new IllegalArgumentException(target.token() + " is drawn with an animation effect, whole (Drawing.Animated), never with a body");
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

    /**
     * What the theme worn answers a component of the taxonomy for the same target: at rest and in every state
     * the target offers, resolved as that component's own look and treatments are - this design's lattice is
     * a document table's data cell's rule, and follows it wherever a style redraws it. A delegation said,
     * never a copy: the design keeps no CSS of the component's, and a theme that leaves the component's
     * target unanswered at rest refuses it.
     *
     * @param from the component whose answer this drawing is: one the taxonomy has, never a virtual leaf
     */
    record Of<T extends Target>(T target, Component<?> from) implements Drawing<T> {
        public Of {
            Objects.requireNonNull(target, "Drawing.Of.target");
            Objects.requireNonNull(from, "Drawing.Of.from");
        }
    }

    /**
     * An animation effect, whole: what {@link Target.Animation.Effect} is drawn with (RFC 0050 · Episode 2,
     * appendix The Copied Range). The effect's CSS is its own, sealed ({@link Effects}); the concrete leaf's class
     * wears it, and its still dual's class is written beside it. What the effect takes, nothing else the leaf
     * owns may write.
     *
     * @param effect the effect
     */
    record Animated(AnimationEffect effect) implements Drawing<Target.Animation.Effect> {
        public Animated {
            Objects.requireNonNull(effect, "Drawing.Animated.effect");
            Effects.INSTANCE.check(effect);
        }

        @Override public Target.Animation.Effect target() { return Target.Animation.Effect.INSTANCE; }
    }
}
