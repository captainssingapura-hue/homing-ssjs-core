package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.events.WorkspaceEventLogModule;

import java.util.List;

/**
 * {@code EventEmitter} — Phase 6 of the workspace-shell chrome. Attaches
 * a {@code WorkspaceEventLog} for {@code (workspaceKind, workspaceId)},
 * emits the {@code SessionStarted} boot event, and returns an
 * {@code EventRecorder} that downstream callsites use to journal
 * mutations.
 *
 * <p>The recorder enforces three fences before forwarding to the
 * underlying log:</p>
 *
 * <ul>
 *   <li><b>replaying</b> — flipped on by Phase 9 (ReplayEngine) so
 *       replayed actions don't pollute the log they came from.</li>
 *   <li><b>writeLockHeld</b> — flipped by Phase 7 (WriteLockGuard).
 *       A workspace open in another window only reads; emitting
 *       would race the holding window's checkpoints.</li>
 *   <li><b>log present</b> — attach can fail (no IndexedDB, storage
 *       blocked); the recorder degrades to no-op rather than throwing
 *       at every callsite.</li>
 * </ul>
 *
 * <p>The recorder also tracks the seq of the last accepted emit and a
 * counter of events since the last checkpoint — both consumed by
 * Phase 8 (CheckpointService) to trigger M-event cadence checkpoints
 * via a late-bound {@code onAfterEmit} callback.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static. Constructor
 * injection for {@code workspaceEventLog} (test-overridable) and
 * {@code clock}; per-call {@code attach} receives only data
 * (workspaceKind / workspaceId / workspaceName).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 6
 */
public record EventEmitterModule()
        implements DomModule<EventEmitterModule> {

    public static final EventEmitterModule INSTANCE = new EventEmitterModule();

    /** The {@code EventEmitter} JS class (factory: holds collaborators). */
    public record EventEmitter()  implements Exportable._Class<EventEmitterModule> {}

    /** The {@code EventRecorder} JS class (per-workspace state). */
    public record EventRecorder() implements Exportable._Class<EventEmitterModule> {}

    @Override
    public ImportsFor<EventEmitterModule> imports() {
        return ImportsFor.<EventEmitterModule>builder()
                .add(new ModuleImports<>(
                        List.of(new WorkspaceEventLogModule.WorkspaceEventLog()),
                        WorkspaceEventLogModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<EventEmitterModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new EventEmitter(), new EventRecorder()));
    }
}
