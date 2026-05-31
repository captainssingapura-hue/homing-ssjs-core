package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code ReplayEngine} — Phase 9 of the workspace-shell chrome. The
 * piece that finally closes the refresh-restore gap. On boot:
 *
 * <ol>
 *   <li>Read latest checkpoint (if any) from {@code CheckpointStore}.
 *       Caller's {@code decodeCheckpoint(row)} returns the decoded
 *       state object (or null on schema-mismatch / decode failure).</li>
 *   <li>Query the {@code WorkspaceEventLog}.</li>
 *   <li>Fence the recorder via {@code recorder.setReplaying(true)} so
 *       handlers' own mutations don't journal new events into the log
 *       they came from.</li>
 *   <li>Walk events in seq order, calling
 *       {@code handlers[eventName](payload, ctx)} for each. Events whose
 *       name isn't in {@code handlers} are silently skipped (the engine
 *       doesn't enforce a vocabulary).</li>
 *   <li>After the walk, fire {@code restoreFromCheckpoint(cpState)} once
 *       — orchestrator spawns checkpoint-only widgets here (post-event-
 *       pruning, those are the only ones not already covered by replayed
 *       spawn events).</li>
 *   <li>Empty log + no checkpoint → {@code onEmpty()} fallback
 *       (orchestrator typically delegates to {@code PinnedTabSpawner}).</li>
 *   <li>Production mode drops the fence on completion so user actions
 *       resume recording; slow-motion mode leaves it on (observation-
 *       only by design).</li>
 * </ol>
 *
 * <p>Per the substrate split: the engine owns <em>sequencing + fencing
 * + filtering</em>; the orchestrator owns the per-event-name semantics
 * (MTP calls, mounter spawns, etc.). This keeps the substrate ignorant
 * of the workspace's event vocabulary — new spec-driven events just
 * need a new handler entry.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static. Constructor
 * injection for {@code clock} and {@code scheduler} (so tests can drive
 * the step pump deterministically).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 9
 */
public record ReplayEngineModule()
        implements DomModule<ReplayEngineModule> {

    public static final ReplayEngineModule INSTANCE = new ReplayEngineModule();

    /** The {@code ReplayEngine} JS class. */
    public record ReplayEngine() implements Exportable._Class<ReplayEngineModule> {}

    @Override
    public ImportsFor<ReplayEngineModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<ReplayEngineModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ReplayEngine()));
    }
}
