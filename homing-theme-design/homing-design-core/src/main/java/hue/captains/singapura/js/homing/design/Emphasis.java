package hue.captains.singapura.js.homing.design;

/**
 * Emphasis — how much an element asks for attention relative to its
 * neighbours. {@code Primary} is what the current tokens call the accent;
 * {@code Muted} is what they call muted text, generalised to any target.
 */
public interface Emphasis extends Semantic {

    record Primary() implements Emphasis {}

    record Secondary() implements Emphasis {}

    record Tertiary() implements Emphasis {}

    record Muted() implements Emphasis {}
}
