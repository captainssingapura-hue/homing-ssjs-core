package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a concrete leaf says for one target its leaf owns (RFC 0067, appendix Virtual Leaves and Designs, and Local Styles):
 * its own CSS - or for colour, the semantic colours it is painted in - nothing, or another concrete leaf's - reuse
 * always said - and for an animation, an effect, whole. A local style never takes a component's look: what it draws is
 * its own, as a table's lines are the table's and never its cells' (the owner, 15 Oct 2026).
 * The target is the type parameter, so an answer for one target cannot stand in for another's, and no drawing
 * can name a colour's value.
 *
 * @param <T> the target answered
 */
public sealed interface Drawing<T extends Target> extends ValueObject
        permits Drawing.Own, Drawing.Painted, Drawing.None, Drawing.As, Drawing.Animated {

    /** The target it answers. */
    T target();

    /**
     * Its own CSS: a body for each state it answers, at rest the look - declarations of the target's
     * properties only ({@link AnswerBody}), in states the target offers a slot for. Refused as it is
     * written, as a style's answer is.
     */
    record Own<T extends Target.Colorless>(T target, Answer.Own<T> answer) implements Drawing<T> {
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
     * A colour target painted (RFC 0067, appendix Semantic Colours and Flavours): a case its target offers per
     * state it answers, at rest the look, in semantic colours of its local style's scheme - palettable ones follow the
     * palette worn, a fixed one pins its colour. In states the target offers a slot for.
     */
    record Painted<T extends Target.Color>(T target, Answer.Painted<?, T> answer) implements Drawing<T> {
        public Painted {
            Objects.requireNonNull(target, "Drawing.Painted.target");
            Objects.requireNonNull(answer, "Drawing.Painted.answer");
            answer.paints().keySet().forEach(state -> {
                if (!target.states().contains(state))
                    throw new IllegalArgumentException(target.token() + " offers no " + SemanticTokens.INSTANCE.state(state) + "; it offers "
                            + String.join(", ", target.states().stream().map(SemanticTokens.INSTANCE::state).toList()));
            });
        }

        /** The look alone: a paint at rest. */
        public Painted(T target, Paint<?, T> rest) { this(target, new Answer.Painted<>(rest)); }
    }

    /**
     * Nothing: no rule draws the target in this style. Its class is worn all the same - what a local style
     * puts on an element is the same in every style - and says nothing.
     */
    record None<T extends Target>(T target) implements Drawing<T> {
        public None { Objects.requireNonNull(target, "Drawing.None.target"); }
    }

    /**
     * Another concrete leaf's drawing of the same target - of the same leaf, in another local style - in the
     * style worn: this local style's cursor is the house's cursor.
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
