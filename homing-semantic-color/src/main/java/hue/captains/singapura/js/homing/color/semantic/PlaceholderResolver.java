package hue.captains.singapura.js.homing.color.semantic;

/**
 * A stub {@link SemanticColorResolver} that proves the seam without any colour
 * math — it echoes the request as an opaque token ({@code name@n/d}). It stands
 * in until a real resolver (anchors + interpolation, per theme) is written; no
 * production code should rely on its output being a usable colour.
 */
public record PlaceholderResolver() implements SemanticColorResolver {

    public static final PlaceholderResolver INSTANCE = new PlaceholderResolver();

    @Override
    public PhysicalColor resolve(SemanticColor semantic, Degree degree) {
        return new PhysicalColor(semantic.name() + "@" + degree.numerator() + "/" + degree.denominator());
    }
}
