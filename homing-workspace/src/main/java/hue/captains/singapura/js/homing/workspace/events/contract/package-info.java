/**
 * RFC 0035 — Substrate-Free Contracts for Event Management.
 *
 * <p>The Java contract surface for the event-management domain. Every
 * record / interface in this package is substrate-free per the
 * <em>Good Design Doesn't Pick Up Its Substrate</em> meta-doctrine; any
 * implementation language (JS today, hypothetically Kotlin / C / TS
 * later) renders against the same Java contract.</p>
 *
 * <h2>Object model</h2>
 *
 * <p>Two typed identifiers, six value records, two sealed sums (12
 * variants total), four service interfaces:</p>
 *
 * <ul>
 *   <li><b>Identifiers:</b> {@link EventSeq}, {@link EventName}</li>
 *   <li><b>Value records:</b> {@link Event}, {@link EventQuery},
 *       {@link Checkpoint}, {@link Location}, {@link TabRef},
 *       {@link CadencePolicy}</li>
 *   <li><b>Sealed sums:</b> {@link WorkspaceEventPayload} (8 variants),
 *       {@link CheckpointTrigger} (4 variants)</li>
 *   <li><b>Service interfaces:</b> {@link EventLog}, {@link CheckpointStore},
 *       {@link ReplayHandler}, {@link Checkpointer}</li>
 * </ul>
 *
 * <h2>Reused identifiers from elsewhere</h2>
 *
 * <p>{@link hue.captains.singapura.js.homing.workspace.state.WorkspaceKind},
 * {@link hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId},
 * {@link hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId},
 * {@link hue.captains.singapura.js.homing.workspace.state.WidgetKind},
 * {@link hue.captains.singapura.js.homing.workspace.state.WidgetTitle},
 * {@link hue.captains.singapura.js.homing.workspace.state.PaneId},
 * {@link hue.captains.singapura.js.homing.workspace.state.Orientation}.</p>
 *
 * <h2>What's explicitly NOT here</h2>
 *
 * <ul>
 *   <li>No enums — every closed vocabulary is a sealed sum so variants
 *       can carry their own context.</li>
 *   <li>No partial-event-ordering specs — per meta-doctrine, that's a
 *       smell, fixed by composition rather than expressed in contract.</li>
 *   <li>No "method must fire X before Y" annotations — same.</li>
 *   <li>No substrate-specific concurrency primitives — the contracts
 *       are pure data + interfaces; substrates pick their own concurrency
 *       (JS: single-threaded by construction; Java: futures).</li>
 * </ul>
 *
 * @since RFC 0035 P1
 */
package hue.captains.singapura.js.homing.workspace.events.contract;
