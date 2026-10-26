package hue.captains.singapura.js.homing.base.lang;

import hue.captains.singapura.tao.ontology.Stateless;

/**
 * A stateless type with exactly one instance. {@link Stateless} says any two instances of a type are the same; a
 * stateless singleton says too that there is only the one, held where anyone can name it.
 *
 * <p>Every concrete type that is one declares that instance as a field of its own:</p>
 *
 * <pre>{@code
 * public record Identity() implements StatelessSingleton {
 *     public static final Identity INSTANCE = new Identity();
 * }
 * }</pre>
 *
 * <ul>
 *   <li>named {@code INSTANCE}, and declared on the type itself - never inherited from a supertype;</li>
 *   <li>{@code static final}, and of exactly the type;</li>
 *   <li>the only instance there is: nothing else constructs one.</li>
 * </ul>
 *
 * <p>A one-constant enum, {@code enum Identity implements StatelessSingleton { INSTANCE }}, keeps the same rule.</p>
 *
 * <p>Java cannot make an interface ask for a static field, so the rule is stated here and held outside the
 * language.</p>
 *
 * <p>Only a stateless type is a singleton. A type that holds state, however immutable, has as many values as it has
 * states, and no one of them is the instance.</p>
 */
public interface StatelessSingleton extends Stateless {
}
