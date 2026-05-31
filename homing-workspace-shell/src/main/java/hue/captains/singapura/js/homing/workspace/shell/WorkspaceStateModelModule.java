package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code WorkspaceStateModel} — pure in-memory model of the workspace's
 * effective state, mutated by folding event records into it. Replaces
 * the previous physical-replay design (each event triggers MTP DOM
 * mutation in sequence) with a virtual-replay design (events fold into
 * a final state, projected to MTP once at the end).
 *
 * <p>The model tracks three pieces:</p>
 *
 * <ul>
 *   <li><b>Layout tree</b> — pane structure as MTP-native shape
 *       ({@code {kind:'leaf', slotId}} or
 *       {@code {kind:'split', orientation, children:[…]}}). Default
 *       initial layout matches MTP's {@code _defaultLayout} (2×2
 *       with slots {@code tl}/{@code tr}/{@code bl}/{@code br}).</li>
 *   <li><b>Tabs by slot</b> — for each slotId, the ordered list of
 *       widget descriptors {@code {widgetInstanceUuid, widgetKind,
 *       params, title, pinned}}.</li>
 *   <li><b>Active widget UUID</b> — workspace-active tab.</li>
 * </ul>
 *
 * <p>Mutations via {@link #apply(event)} are idempotent where it makes
 * sense (spawn of an existing UUID is a no-op; close/move of a missing
 * UUID is a no-op). Split/merge mint stable synthetic slot ids
 * ({@code sp_N}) so the projection produces an MTP whose runtime
 * slotIds match the model's references.</p>
 *
 * <p>Three downstream consumers:</p>
 *
 * <ul>
 *   <li><b>Replay</b> — folder over recorded events.</li>
 *   <li><b>Checkpoint capture</b> — {@code toSnapshot()} returns the
 *       state envelope written to the checkpoint store.</li>
 *   <li><b>Checkpoint restore</b> — {@code fromSnapshot(s)} rebuilds
 *       the model so replay can resume from the snapshot's seq.</li>
 * </ul>
 *
 * <p>Explicit Substrate: per-workspace instance (NOT INSTANCE — one
 * model per shell). Pure state; no collaborators; all transitions
 * pure-function-of-(state, event).</p>
 *
 * @since post-RFC-0034 — virtual-replay restructure
 */
public record WorkspaceStateModelModule()
        implements DomModule<WorkspaceStateModelModule> {

    public static final WorkspaceStateModelModule INSTANCE = new WorkspaceStateModelModule();

    /** The {@code WorkspaceStateModel} JS class. */
    public record WorkspaceStateModel()
            implements Exportable._Class<WorkspaceStateModelModule> {}

    @Override
    public ImportsFor<WorkspaceStateModelModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WorkspaceStateModelModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceStateModel()));
    }
}
