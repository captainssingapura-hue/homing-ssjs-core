package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * What a style or a palette says for one semantic class (RFC 0067, D5): its own CSS, a redirect to
 * another node on the same root, or its base's answer. The root is the type parameter, so an
 * answer for one target cannot stand in for another's, and a redirect names only a node - the
 * target is the class's own, so a redirect across roots cannot be written at all.
 *
 * <p>Resolution looks in the worn style first, all the way up the tree, and only then in its base
 * (D6): a style's coarse answer outweighs its base's precise one. {@link FromBase} is how a style
 * takes its base's answer below one of its own - a call to the base, as {@code super} is, where a
 * {@link Redirect} is resolved in the style worn, late, as a method is.</p>
 *
 * @param <T> the target leaf: the root the class grows on
 */
public sealed interface Answer<T extends Target> extends ValueObject permits Answer.Own, Answer.Redirect, Answer.FromBase {

    /**
     * Its own CSS, a body per state it answers - declarations of the target's properties, nested
     * only on the element itself ({@link AnswerBody}). The states it does not list are not answered
     * here, and the walk goes on for them; an empty body is an answer that says nothing.
     *
     * @param bodies the body for each state answered, {@link State#REST} the look
     */
    record Own<T extends Target>(Map<State, String> bodies) implements Answer<T> {

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

    /** The base's answer for this class, from here up - though this style answers higher up itself. */
    record FromBase<T extends Target>() implements Answer<T> {}
}
