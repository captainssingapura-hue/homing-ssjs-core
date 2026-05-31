package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspaceStatePersistenceModule;

import java.util.List;

/**
 * {@code PersistenceAttacher} — Phase 4 of the workspace-shell chrome.
 * Thin wrapper around {@code WorkspaceStatePersistence.attach}: pulls
 * {@code spec.kind} for the {@code workspaceKind} param, supplies
 * {@code window.localStorage} as the storage, returns the constructed
 * persistence layer (or {@code null} on failure, logged).
 *
 * <p>What this <i>does not</i> do — by intent in this phase:</p>
 *
 * <ul>
 *   <li>Does not call {@code tryRestore}. RFC 0030 P3 retired
 *       snapshot-based boot; restoration is event-replay (Phase 9).</li>
 *   <li>Does not call {@code create}. The persister is not used by V2
 *       yet — RFC 0030 P3 likewise retired the snapshot persister; only
 *       checkpoint writes (Phase 8) actually save state.</li>
 * </ul>
 *
 * <p>The attach is still useful: the returned layer's {@code store} is
 * what {@code ResetControls} (Phase 18) calls {@code .clear(kind)} on
 * to wipe any lingering legacy localStorage data. So Phase 4 effectively
 * makes the storage layer present and inspectable; Phases 8/9 do the
 * real save/restore via the checkpoint store.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static methods.
 * {@code attach} is sync — the wrapped function is sync — so no
 * Promise plumbing.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 4
 */
public record PersistenceAttacherModule()
        implements DomModule<PersistenceAttacherModule> {

    public static final PersistenceAttacherModule INSTANCE = new PersistenceAttacherModule();

    /** The {@code PersistenceAttacher} JS class. */
    public record PersistenceAttacher() implements Exportable._Class<PersistenceAttacherModule> {}

    @Override
    public ImportsFor<PersistenceAttacherModule> imports() {
        return ImportsFor.<PersistenceAttacherModule>builder()
                .add(new ModuleImports<>(
                        List.of(new WorkspaceStatePersistenceModule.WorkspaceStatePersistence()),
                        WorkspaceStatePersistenceModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PersistenceAttacherModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PersistenceAttacher()));
    }
}
