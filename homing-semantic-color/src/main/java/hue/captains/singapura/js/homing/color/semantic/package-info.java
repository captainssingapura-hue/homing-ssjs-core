/**
 * Semantic Colours — a generic, extensible scheme for colours keyed by semantic
 * <em>meaning</em> (success, danger, happy, worried, relaxed), each owning a
 * <em>degree scale</em>.
 *
 * <h2>The model</h2>
 * <pre>
 *   resolve : (SemanticColor, Degree) -> PhysicalColor
 * </pre>
 * <ul>
 *   <li>{@link hue.captains.singapura.js.homing.color.semantic.SemanticColor} —
 *       a pure typed identity (the meaning), open/extensible, never
 *       domain-specific.</li>
 *   <li>{@link hue.captains.singapura.js.homing.color.semantic.Degree} — the
 *       intensity, a <strong>proper rational</strong> in {@code [0, 1)}.</li>
 *   <li>{@link hue.captains.singapura.js.homing.color.semantic.PhysicalColor} —
 *       the realised colour; opaque for now.</li>
 *   <li>{@link hue.captains.singapura.js.homing.color.semantic.SemanticColorResolver}
 *       — the seam; the scale is a <em>function</em> sampled at a degree, not a
 *       stored list.</li>
 * </ul>
 *
 * <h2>Realization deferred</h2>
 * How a degree becomes a colour (anchors, interpolation, OKLCH) lives entirely
 * in a concrete resolver and is <strong>not written yet</strong>. Today only a
 * {@link hue.captains.singapura.js.homing.color.semantic.PlaceholderResolver}
 * exists, echoing the request so the seam compiles and is testable. Swapping in
 * a real (theme-specific) resolver later changes every scale at once, with no
 * call-site change — this is the colour-channel Realizer.
 *
 * <p>The default band lives in
 * {@link hue.captains.singapura.js.homing.color.semantic.band.SemanticColorBand}.</p>
 */
package hue.captains.singapura.js.homing.color.semantic;
