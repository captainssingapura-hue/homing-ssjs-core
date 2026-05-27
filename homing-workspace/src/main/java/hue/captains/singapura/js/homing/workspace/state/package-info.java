/**
 * RFC 0029 — Workspace State Persistence. The state envelope, the
 * polymorphic store SPI, and the schema-migration SPI.
 *
 * <h2>The envelope</h2>
 *
 * <p>{@link hue.captains.singapura.js.homing.workspace.state.WorkspaceState}
 * holds three conceptual axes in two storage maps:</p>
 *
 * <ul>
 *   <li><b>Layout</b>
 *       ({@link hue.captains.singapura.js.homing.workspace.state.LayoutNode})
 *       — the SplitPane tree shape.</li>
 *   <li><b>Widget identity + linkage</b> — each
 *       {@link hue.captains.singapura.js.homing.workspace.state.WidgetInstance}
 *       carries its own
 *       {@link hue.captains.singapura.js.homing.workspace.state.WidgetLocation}
 *       (sealed: {@code InPane} | {@code InModal}), so identity and
 *       location share one source of truth per widget.</li>
 *   <li><b>Chrome</b>
 *       ({@link hue.captains.singapura.js.homing.workspace.state.ChromeState})
 *       — small workspace-wide settings.</li>
 * </ul>
 *
 * <h2>Names Are Types</h2>
 *
 * <p>Every identifier in this package is its own typed record per the
 * {@code Names Are Types} doctrine:
 * {@link hue.captains.singapura.js.homing.workspace.state.PaneId},
 * {@link hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId},
 * {@link hue.captains.singapura.js.homing.workspace.state.WidgetKind},
 * {@link hue.captains.singapura.js.homing.workspace.state.WorkspaceKind},
 * {@link hue.captains.singapura.js.homing.workspace.state.WidgetTitle},
 * {@link hue.captains.singapura.js.homing.workspace.state.ThemeName}.
 * Widget params are typed
 * ({@code Widget._Param}), not {@code Map<String, Object>} — the type
 * carries the data structure.</p>
 *
 * <h2>I/O contracts</h2>
 *
 * <p>{@link hue.captains.singapura.js.homing.workspace.state.WorkspaceStateStore}
 * is the polymorphic persistence SPI; the framework ships local-only
 * implementations and provides the {@code isRemote()} signal for the
 * "state leaving device" banner when a downstream plugs a remote
 * store.</p>
 *
 * <p>{@link hue.captains.singapura.js.homing.workspace.state.WorkspaceStateMigration}
 * is the JSON-tree-level migration SPI for envelope schema bumps;
 * widget params evolve tolerantly under the widget's own
 * responsibility.</p>
 *
 * <p>Anchored to the {@code State Belongs to the User} doctrine —
 * local-first, export-sufficient, framework refuses to ship server-sync.</p>
 *
 * @since RFC 0029 cycle 1
 */
package hue.captains.singapura.js.homing.workspace.state;
