package hue.captains.singapura.js.homing.color.semantic;

/**
 * A <strong>semantic colour</strong> — a named meaning ({@code success},
 * {@code danger}, {@code happy}, {@code worried}, {@code relaxed}), leaning
 * generic rather than domain-specific.
 *
 * <p>A pure <em>identity</em>: it carries no colour values. Each meaning owns a
 * <em>degree scale</em> that is realised on demand by a
 * {@link SemanticColorResolver} — {@code resolve(semantic, degree) -> colour}.
 * Keeping values off the identity is what keeps a semantic colour portable and
 * realization-free (a theme supplies a different resolver, not a different
 * identity).</p>
 *
 * <p>The vocabulary is <strong>open</strong>: a framework band ships in this
 * module ({@link SemanticColors#DEFAULT_BAND}); downstream defines new meanings
 * simply by implementing this interface. A semantic colour is defined by its
 * <em>feel/function</em> and never references a domain.</p>
 */
public interface SemanticColor {

    /** Stable identity of the meaning (e.g. {@code "happy"}). */
    String name();
}
