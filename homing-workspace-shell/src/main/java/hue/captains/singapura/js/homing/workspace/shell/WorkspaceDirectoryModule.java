package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.catalogue.WorkspaceCatalogueModule;

import java.util.List;

/**
 * {@code WorkspaceDirectory} — Phase 5 of the workspace-shell chrome.
 * Owns workspace <em>identity</em>: turns the URL ({@code ?workspace=
 * <uuid>} or {@code ?name=Y}) into a stable {@code {id, name,
 * isDefault, isNew}} tuple by consulting the
 * {@link WorkspaceCatalogueModule catalogue store}, then idempotently
 * registers the workspace (create-or-touch).
 *
 * <p>This is the gate that downstream phases chain on:</p>
 *
 * <ul>
 *   <li>Phase 6 (EventEmitter) needs {@code workspaceId} to scope the
 *       event log to this instance.</li>
 *   <li>Phase 8 (CheckpointService) keys checkpoint rows by
 *       {@code (kind, workspaceId)}.</li>
 *   <li>Phase 9 (ReplayEngine) needs the event log positioned at the
 *       right workspace before it can replay.</li>
 *   <li>Phase 16 (WorkspaceBadge) renders the resolved name + a "·"
 *       marker for non-default workspaces.</li>
 * </ul>
 *
 * <p>Falls back to a deterministic placeholder UUID derived from
 * {@code spec.kind} when the catalogue is unreachable or the URL has
 * no hint — same kind → same UUID → "the default workspace" for that
 * kind. Multi-instance support (RFC 0031 V2) layers on top: a real
 * UUID supplants the placeholder once the user names a workspace.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static methods.
 * Constructor injection for {@code catalogueStore}, {@code urlSource},
 * {@code uuidGen}, {@code clock} keeps the dep graph traversable and
 * the tests trivial (no global stubbing needed).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 5
 */
public record WorkspaceDirectoryModule()
        implements DomModule<WorkspaceDirectoryModule> {

    public static final WorkspaceDirectoryModule INSTANCE = new WorkspaceDirectoryModule();

    /** The {@code WorkspaceDirectory} JS class. */
    public record WorkspaceDirectory() implements Exportable._Class<WorkspaceDirectoryModule> {}

    @Override
    public ImportsFor<WorkspaceDirectoryModule> imports() {
        return ImportsFor.<WorkspaceDirectoryModule>builder()
                .add(new ModuleImports<>(
                        List.of(new WorkspaceCatalogueModule.createWorkspaceCatalogueStore()),
                        WorkspaceCatalogueModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceDirectoryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceDirectory()));
    }
}
