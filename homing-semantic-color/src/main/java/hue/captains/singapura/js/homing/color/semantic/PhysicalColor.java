package hue.captains.singapura.js.homing.color.semantic;

/**
 * The realised colour a {@link SemanticColorResolver} produces for a
 * {@code (SemanticColor, Degree)} pair.
 *
 * <p><strong>Opaque for now — realization is deferred.</strong> The
 * {@link #value()} eventually holds a concrete colour (e.g. a CSS colour string
 * from an OKLCH interpolation); nothing in this module interprets it. It exists
 * so the {@code resolve} seam has a return type while the colour math is
 * unwritten.</p>
 */
public record PhysicalColor(String value) {
}
