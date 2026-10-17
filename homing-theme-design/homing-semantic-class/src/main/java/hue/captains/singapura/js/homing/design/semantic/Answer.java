package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * What a style says for one semantic class (RFC 0067, D5): its own CSS, or for colour the semantic
 * colours it is painted in; a redirect to another node on the same root; or its base's answer. The
 * root is the type parameter, so an answer for one target cannot stand in for another's, and a
 * redirect names only a node - the target is the class's own, so a redirect across roots cannot be
 * written at all. Its own CSS is bounded by type to the targets that are not colour, and colour is
 * painted, so no answer can name a colour's value.
 *
 * <p>Resolution looks in the worn style first, all the way up the tree, and only then in its base
 * (D6): a style's coarse answer outweighs its base's precise one. {@link FromBase} is how a style
 * takes its base's answer below one of its own - a call to the base, as {@code super} is, where a
 * {@link Redirect} is resolved in the style worn, late, as a method is. {@link Target.Animation.Effect} is
 * answered {@link Animated} - with an effect, whole - and never with its own CSS.</p>
 *
 * @param <T> the target leaf: the root the class grows on
 */
public sealed interface Answer<T extends Target> extends ValueObject
        permits Answer.Own, Answer.Painted, Answer.Redirect, Answer.FromBase, Answer.Animated {

    /**
     * Its own CSS, a body per state it answers - declarations of the target's properties, nested
     * only on the element itself ({@link AnswerBody}), naming no colour. The states it does not list
     * are not answered here, and the walk goes on for them; an empty body is an answer that says nothing.
     *
     * @param bodies the body for each state answered, {@link State#REST} the look
     */
    record Own<T extends Target.Colorless>(Map<State, String> bodies) implements Answer<T> {

        public Own {
            Objects.requireNonNull(bodies, "Own.bodies");
            if (bodies.isEmpty()) throw new IllegalArgumentException("an answer of its own answers at least one state");
            bodies = Collections.unmodifiableMap(new EnumMap<>(bodies));
        }

        /** The look alone: a body at rest. */
        public Own(String rest) { this(Map.of(State.REST, rest)); }

        /** This answer, and a body for one more state. */
        public Own<T> at(State state, String body) {
            var more = new EnumMap<State, String>(State.class);
            more.putAll(bodies);
            if (more.putIfAbsent(state, body) != null) throw new IllegalArgumentException(state + " is answered twice");
            return new Own<>(more);
        }
    }

    /**
     * A colour target painted (RFC 0067, appendix Semantic Colours and Flavours): a case its target
     * offers per state it answers, naming semantic colours its scheme can see - which colour a thing
     * uses, never what a colour is. Each palette worn resolves them when the sheet is made. The states
     * it does not list are not answered here, and the walk goes on for them.
     *
     * @param paints the paint for each state answered, {@link State#REST} the look
     * @param <S>    the scheme of the style that says it
     */
    record Painted<S extends ColorScheme, T extends Target.Color>(Map<State, Paint<S, T>> paints) implements Answer<T> {

        public Painted {
            Objects.requireNonNull(paints, "Painted.paints");
            if (paints.isEmpty()) throw new IllegalArgumentException("a painted answer answers at least one state");
            paints.values().forEach(p -> Objects.requireNonNull(p, "Painted.paints: a state painted with nothing"));
            paints = Collections.unmodifiableMap(new EnumMap<>(paints));
        }

        /** The look alone: a paint at rest. */
        public Painted(Paint<S, T> rest) { this(Map.of(State.REST, rest)); }

        /** This answer, and a paint for one more state. */
        public Painted<S, T> at(State state, Paint<S, T> paint) {
            var more = new EnumMap<State, Paint<S, T>>(State.class);
            more.putAll(paints);
            if (more.putIfAbsent(state, paint) != null) throw new IllegalArgumentException(state + " is answered twice");
            return new Painted<>(more);
        }
    }

    /**
     * The whole class as another node's on the same root - every state, never one alone - resolved
     * in the style worn: a redirect to the plain button follows whatever the worn style says of it.
     *
     * @param to the node whose class this one is
     */
    record Redirect<T extends Target>(Casting to) implements Answer<T> {
        public Redirect { Objects.requireNonNull(to, "Redirect.to"); }

        /** To a component or a branch on its own. */
        public Redirect(Taxon to) { this(new Casting(to)); }
    }

    /**
     * The base's answer for this class, from here up - though this style answers higher up itself -
     * in the states it names, or in every state when it names none. In a state it does not name
     * the walk goes on up this style: a close button bare at rest, as its base says, and lifted
     * under the hand, as its style lifts every button.
     *
     * @param only the states the base is asked in; none named, every state
     */
    record FromBase<T extends Target>(Set<State> only) implements Answer<T> {

        public FromBase {
            Objects.requireNonNull(only, "FromBase.only");
            only = only.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(only));
        }

        /** In every state. */
        public FromBase() { this(Set.of()); }

        /** Whether the base is asked in this state. */
        public boolean asks(State state) { return only.isEmpty() || only.contains(state); }
    }

    /**
     * An animation effect, whole, at rest: what {@link Target.Animation.Effect} is answered with - the effect's
     * CSS is its own, sealed ({@link Effects}), and refused here unless it is its own to write.
     *
     * @param effect the effect worn
     */
    record Animated(AnimationEffect effect) implements Answer<Target.Animation.Effect> {
        public Animated {
            Objects.requireNonNull(effect, "Animated.effect");
            Effects.INSTANCE.check(effect);
        }
    }
}
