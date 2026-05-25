/**
 * RFC 0028 — Parties (Scoped Actor Systems for Workspace Messaging).
 *
 * <p>Each workspace declares a set of {@link Party}s, one per concern.
 * Each Party is a self-contained universe with its own scoped message
 * ADT, its own tree of {@link Secretary}s (pure routing nodes) and
 * {@link Actor}s (concrete UI-doing leaves), and its own dispatcher.</p>
 *
 * <h2>Cycle 1 scope (this package)</h2>
 *
 * <p>The typed Java contracts that workspaces author against. The JS
 * runtime that executes these contracts ships in cycle 2.</p>
 *
 * <ul>
 *   <li><b>Value types</b> — {@link AgentId}, {@link Envelope},
 *       {@link Action}, {@link Step}, {@link Snapshot}. The wire and
 *       in-memory shapes of messages, dispatch results, and inspection
 *       output.</li>
 *   <li><b>Declaration types</b> — {@link Secretary}, {@link Party},
 *       {@link PartySet}. Records the workspace constructs at setup
 *       time to describe its Party topology; consumed by the (future)
 *       JS-emit writer to bootstrap the runtime.</li>
 *   <li><b>Widget contract</b> — {@link Actor}. Interface the widget
 *       Java class implements; one hook ({@code partyDeregister})
 *       invoked by {@code MultiTabPane}'s close path so the widget can
 *       leave every Party it joined before DOM teardown.</li>
 * </ul>
 *
 * <h2>What lives in JS (not this package)</h2>
 *
 * <ul>
 *   <li>Secretary behaviour functions — pure {@code (state, envelope) →
 *       Step} closures authored as ES modules; declared from Java by
 *       {@link Secretary#jsModulePath()}.</li>
 *   <li>Actor reactor maps — per-Party {@code Map<MessageClass, Handler>}
 *       passed into the JS-side {@code party.join()} at registration.</li>
 *   <li>The dispatch loop — synchronous, depth-first, per-member
 *       try/catch isolation.</li>
 *   <li>The agent registry — per-Party index of who is registered
 *       where.</li>
 * </ul>
 *
 * <h2>Lifecycle reminder</h2>
 *
 * <p>Party lifecycle is logically independent of DomOpsParty lifecycle.
 * {@code DomOpsParty.dissolve} cleans DOM only;
 * {@link Actor#partyDeregister} cleans messaging memberships. The two
 * fire together at tab close (Party deregister first, then onClose
 * dissolves DOM) but neither hooks into the other. The widget is the
 * explicit bridge.</p>
 *
 * @since RFC 0028
 */
package hue.captains.singapura.js.homing.workspace.party;
