/**
 * The <strong>Affective</strong> group's landmark feelings — {@link SemanticColor}s
 * that are also leaves under
 * {@link hue.captains.singapura.js.homing.color.semantic.group.Affective}.
 *
 * <p>Each is a landmark position on the valence x arousal plane:</p>
 * <pre>
 *                    high arousal
 *                         |
 *            Tense        |      Cheerful
 *       (low-V, high-A)   |   (high-V, high-A)
 *       ------------------+------------------ valence -&gt;
 *            Somber       |      Serene
 *       (low-V, low-A)    |   (high-V, low-A)
 *                    Neutral (centre)
 *                         |
 *                    low arousal
 * </pre>
 *
 * <p>The functional statuses are a <em>projection</em> of this space, not its
 * basis: {@code danger} ≈ Tense, {@code success} ≈ Serene/Content,
 * {@code warning} ≈ the uneasy region. {@code Degree} moves along the arousal
 * axis. Physical colour is deferred to a resolver.</p>
 */
package hue.captains.singapura.js.homing.color.semantic.affective;
