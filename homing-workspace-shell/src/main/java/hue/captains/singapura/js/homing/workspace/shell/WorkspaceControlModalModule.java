package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.theme.StudioVarsJsModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule;
import hue.captains.singapura.js.homing.workspace.catalogue.WorkspaceCatalogueModule;
import hue.captains.singapura.js.homing.workspace.events.CheckpointStoreModule;
import hue.captains.singapura.js.homing.workspace.events.WorkspaceEventLogModule;

import java.util.List;

/**
 * {@code WorkspaceControlModal} — combined Phases 16+17+18. V1 ships
 * three separate widgets (floating badge + switcher modal + reset
 * controls). V2 collapses them into a single substrate surface: the
 * workspace's title in the ribbon becomes a clickable
 * {@code kind :: name} chip; clicking opens this modal containing
 * three sections:
 *
 * <ol>
 *   <li><b>Switcher</b> — lists every workspace catalogued for this
 *       kind; switch via {@code ?workspace=<uuid>} navigation; create
 *       a new named workspace via {@code ?name=Y}; delete (with
 *       confirm) via the catalogue store.</li>
 *   <li><b>Reset State</b> — clears event log + checkpoint for this
 *       workspace, then reloads. Useful during dev when the log
 *       accumulates cruft.</li>
 *   <li><b>Slow-motion Replay</b> — sets {@code ?slowmo=500} and
 *       reloads; replay walks the log at human-watchable speed
 *       (observation-only — recording stays fenced off).</li>
 * </ol>
 *
 * <p>Substrate-only — every workspace gets the same three sections.
 * Spec-extensible sections (custom DevTools per workspace) is a
 * future extension; for now ControlPlaneItem is unused.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static. Constructor
 * injection for {@code ModalCtor}, {@code window} (for navigation +
 * reload), and {@code document} (for DOM construction).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — V2 phases 16+17+18
 */
public record WorkspaceControlModalModule()
        implements DomModule<WorkspaceControlModalModule> {

    public static final WorkspaceControlModalModule INSTANCE =
            new WorkspaceControlModalModule();

    /** The {@code WorkspaceControlModal} JS class. */
    public record WorkspaceControlModal()
            implements Exportable._Class<WorkspaceControlModalModule> {}

    @Override
    public ImportsFor<WorkspaceControlModalModule> imports() {
        return ImportsFor.<WorkspaceControlModalModule>builder()
                .add(new ModuleImports<>(
                        List.of(new ModalModule.Modal()),
                        ModalModule.INSTANCE))
                // Typed color tokens — every `var(--color-X)` literal in
                // the JS body comes from these imports, not raw strings.
                // A token typo here is a compile-time / module-load error
                // rather than a paint-time silent fallback.
                .add(new ModuleImports<>(
                        List.of(
                                new StudioVarsJsModule.COLOR_SURFACE_RAISED(),
                                new StudioVarsJsModule.COLOR_SURFACE_RECESSED(),
                                new StudioVarsJsModule.COLOR_TEXT_PRIMARY(),
                                new StudioVarsJsModule.COLOR_TEXT_MUTED(),
                                new StudioVarsJsModule.COLOR_BORDER(),
                                new StudioVarsJsModule.COLOR_ACCENT_EMPHASIS()
                        ),
                        StudioVarsJsModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new WorkspaceCatalogueModule.WorkspaceCatalogueStore()),
                        WorkspaceCatalogueModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new WorkspaceEventLogModule.WorkspaceEventLog()),
                        WorkspaceEventLogModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new CheckpointStoreModule.CheckpointStore()),
                        CheckpointStoreModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceControlModalModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceControlModal()));
    }
}
