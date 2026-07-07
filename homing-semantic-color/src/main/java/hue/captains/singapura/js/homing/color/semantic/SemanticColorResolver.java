package hue.captains.singapura.js.homing.color.semantic;

import java.util.function.Function;

/**
 * The seam that realises a semantic colour's degree scale:
 * {@code resolve(semantic, degree) -> physical colour}.
 *
 * <p>The scale is a <em>function</em>, not a stored list — a resolver samples it
 * at any {@link Degree}. The <em>how</em> (anchors, interpolation, colour space)
 * lives entirely in a concrete resolver and is deferred; different resolvers
 * (per theme, or a future OKLCH implementation) change every scale at once
 * without touching a call site. This is the colour-channel Realizer.</p>
 */
public interface SemanticColorResolver {

    /** Realise {@code semantic} at {@code degree}. */
    PhysicalColor resolve(SemanticColor semantic, Degree degree);

    /**
     * Curried form — resolve a semantic once into a reusable scale
     * {@code Degree -> PhysicalColor} (sample many degrees, e.g. a gradient).
     */
    default Function<Degree, PhysicalColor> resolve(SemanticColor semantic) {
        return degree -> resolve(semantic, degree);
    }
}
