/**
 * Generated JS codecs for the workspace's typed records.
 *
 * <h2>Module discipline — Generated and Hand-Written Live Apart</h2>
 *
 * <p>This module's {@code src/main/resources} is intentionally empty.
 * All served JS is produced by
 * {@link hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecGen}
 * at build time (Maven {@code exec} plugin in the {@code process-classes}
 * phase) into {@code target/classes/...}. Generated files are never
 * committed; the manifest in
 * {@link hue.captains.singapura.js.homing.workspace.codecs.WorkspaceStateCodecsManifest}
 * is the single source of truth.</p>
 *
 * <h2>Layering against the codec ontology</h2>
 *
 * <ul>
 *   <li>{@code homing-codec} — ontology interfaces only
 *       ({@code ObjectDefinition}, {@code TransformationFunctions},
 *       {@code FunctionsCodeGen}, {@code DefinitionCodeGen}).</li>
 *   <li>{@code homing-codec-ecma} — concrete codegens targeting
 *       ECMAScript. Reusable across any module that has typed records
 *       it wants codecs for.</li>
 *   <li>{@code homing-workspace-codecs} <i>(this module)</i> — the
 *       workspace-specific manifest and build driver. Links workspace
 *       records to ECMA codegens via the umbrella manifest.</li>
 * </ul>
 *
 * <h2>Cycle A scope</h2>
 *
 * <p>The manifest currently contains {@link
 * hue.captains.singapura.js.homing.workspace.state.PaneId} only — the
 * proof-of-pipeline slice. Subsequent cycles expand the manifest to all
 * 18 workspace records and add specialised generators (sealed sums,
 * enums, collections, Optionals, Instant). Parity tests against the
 * existing hand-written {@code WorkspaceStatePersistence.js} gate each
 * expansion.</p>
 *
 * @since RFC 0034 P2-prep — codec generation foundation
 */
package hue.captains.singapura.js.homing.workspace.codecs;
