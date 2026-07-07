/**
 * The <strong>ColorGroup taxonomy</strong> — colours classified by their nature,
 * modelled on rebar's rigid-levels ladder.
 *
 * <pre>
 * Colors                       (L0 — root)
 * ├── Expressive               (L1)   colour read as meaning
 * │   ├── Affective            (L2)   felt — valence x arousal landmark feelings
 * │   └── Symbolic             (L2)   learned / cultural (status, auspicious, mourning)
 * ├── Encoding                 (L1)   distinguishes / quantifies data
 * │   ├── Categorical          (L2)   unordered, distinct
 * │   ├── Sequential           (L2)   ordered magnitude (Degree)
 * │   └── Diverging            (L2)   bipolar deviation about a midpoint
 * ├── Identity                 (L1)   brand / entity recognition
 * └── Structural               (L1)   surface hierarchy / legibility
 * </pre>
 *
 * <p>Nature fixes the internal <em>structure</em> of a group's colours (2-D
 * plane / discrete set / scalar ramp / bipolar ramp) and whether {@code Degree}
 * applies. Extensible on two axes: add a new group (new nature) or new colours
 * within a group. The pre-existing {@code StudioVars} tokens are, in these
 * terms, the {@code Structural} group (+ {@code accent} = {@code Identity}).</p>
 */
package hue.captains.singapura.js.homing.color.semantic.group;
