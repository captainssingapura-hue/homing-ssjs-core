package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneModule;
import hue.captains.singapura.js.homing.workspace.WorkspaceLayoutModule;

import java.util.List;

/**
 * {@code WorkspaceShellChrome} — the composition root for the workspace
 * shell. Owns the boot sequence; constructs and wires together the
 * sub-classes that the present-day {@code AnimalsPlaygroundChrome.bodyJs()}
 * inlines as ~2,100 lines of JS-as-Java-strings.
 *
 * <p>This module is grown <b>strictly in parallel</b> to the existing
 * chrome. {@code AnimalsPlayground} + {@code AnimalsPlaygroundChrome}
 * are not modified. Instead, a sibling {@code AnimalsPlaygroundV2} is
 * built up that mounts via {@code mountWorkspaceShell(branch, parent,
 * spec)}; both versions ship side-by-side throughout the extraction.</p>
 *
 * <p>Why parallel-V2 instead of atomic swap:</p>
 * <ul>
 *   <li><b>Always a working reference.</b> V1 stays untouched, so
 *       behavioral regressions in V2 cannot leave the workspace
 *       broken — V1 is the ground truth at every checkpoint.</li>
 *   <li><b>Widget independence proved by reuse.</b> Both V1 and V2
 *       host the <i>same</i> widgets ({@code DocViewWidget},
 *       {@code SpinningAnimalsWidget}, {@code MovingAnimalWidget}, …)
 *       with the <i>same</i> {@code construct(branch, params,
 *       workspaceCtx)} contract. If a widget needs chrome-specific
 *       knowledge to work in V2 that V1 didn't expose, that's a
 *       coupling smell the parallel build surfaces immediately.</li>
 *   <li><b>Phase-by-phase progress is visible.</b> After each phase
 *       lands in the orchestrator (PartyBootstrap, EventEmitter, …),
 *       V2 inherits that capability; the gap between V1 and V2
 *       shrinks by one phase per turn.</li>
 * </ul>
 *
 * <p>V1 retirement is a separate later cycle — only proposed once V2
 * has reached parity and parity has been verified.</p>
 *
 * <p>Boot phases (in order — see {@code WorkspaceShellChromeModule.js}
 * for the orchestration outline). Each phase becomes its own JS
 * module/class as the extraction progresses. v1 imports
 * {@link LayoutCodecModule} only; the import list grows as classes
 * land.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public record WorkspaceShellChromeModule() implements DomModule<WorkspaceShellChromeModule> {

    public static final WorkspaceShellChromeModule INSTANCE = new WorkspaceShellChromeModule();

    /** The orchestrator class. */
    public record WorkspaceShellChrome() implements Exportable._Class<WorkspaceShellChromeModule> {}

    /** Convenience entry: {@code mountWorkspaceShell(branch, parent, spec)}. */
    public record mountWorkspaceShell() implements Exportable._Constant<WorkspaceShellChromeModule> {}

    @Override
    public ImportsFor<WorkspaceShellChromeModule> imports() {
        return ImportsFor.<WorkspaceShellChromeModule>builder()
                .add(new ModuleImports<>(List.of(
                        new LayoutCodecModule.LayoutCodec()),
                        LayoutCodecModule.INSTANCE))
                // Phase 1 — host + WorkspaceLayout mount.
                .add(new ModuleImports<>(List.of(
                        new WorkspaceLayoutModule.WorkspaceLayout()),
                        WorkspaceLayoutModule.INSTANCE))
                // MTP is universal substrate, not a spec-driven phase —
                // every workspace gets one in its content area.
                .add(new ModuleImports<>(List.of(
                        new MultiTabPaneModule.MultiTabPane()),
                        MultiTabPaneModule.INSTANCE))
                // Phase 2 — PartyBootstrap walks spec.parties and constructs
                // each Party + initial actors; result populates
                // {parties, workspaceCtx} the orchestrator threads through.
                .add(new ModuleImports<>(List.of(
                        new PartyBootstrapModule.PartyBootstrap()),
                        PartyBootstrapModule.INSTANCE))
                // Phase 15 — WidgetMounter: dynamic-import + construct +
                // controller-shape validation. Shared by Phase 14
                // (pinned spawn) and Phase 13 (picker spawn).
                .add(new ModuleImports<>(List.of(
                        new WidgetMounterModule.WidgetMounter()),
                        WidgetMounterModule.INSTANCE))
                // Phase 14 — PinnedTabSpawner: reads spec.pinnedSpawns
                // and auto-spawns each pinned widget at boot.
                .add(new ModuleImports<>(List.of(
                        new PinnedTabSpawnerModule.PinnedTabSpawner()),
                        PinnedTabSpawnerModule.INSTANCE))
                // Phase 13 — PickerTabFlow: '+' on a pane opens the
                // picker; pick → mutate-into-widget.
                .add(new ModuleImports<>(List.of(
                        new PickerTabFlowModule.PickerTabFlow()),
                        PickerTabFlowModule.INSTANCE))
                // Phase 3 — CodecRegistrar: registers identity codec
                // for every entry + overlays spec.widgetCodecs. Runs
                // early so persistence (Phase 4) finds a populated
                // registry.
                .add(new ModuleImports<>(List.of(
                        new CodecRegistrarModule.CodecRegistrar()),
                        CodecRegistrarModule.INSTANCE))
                // Phase 4 — PersistenceAttacher: thin wrapper around
                // WorkspaceStatePersistence.attach. Makes the storage
                // layer present + inspectable; ResetControls (Phase 18)
                // uses the returned layer's .store.
                .add(new ModuleImports<>(List.of(
                        new PersistenceAttacherModule.PersistenceAttacher()),
                        PersistenceAttacherModule.INSTANCE))
                // Phase 5 — WorkspaceDirectory: resolves identity from
                // URL via the catalogue + idempotent registration.
                // Downstream phases (6/8/9/16) chain on identityReady.
                .add(new ModuleImports<>(List.of(
                        new WorkspaceDirectoryModule.WorkspaceDirectory()),
                        WorkspaceDirectoryModule.INSTANCE))
                // Phase 6 — EventEmitter: attaches WorkspaceEventLog
                // scoped to (kind, workspaceId), emits SessionStarted,
                // returns a fenced EventRecorder downstream callsites
                // use to journal mutations. Phases 7/8/9 flip the
                // fences and wire the cadence callback.
                .add(new ModuleImports<>(List.of(
                        new EventEmitterModule.EventEmitter(),
                        new EventEmitterModule.EventRecorder()),
                        EventEmitterModule.INSTANCE))
                // Phase 7 — WriteLockGuard: navigator.locks-backed
                // exclusive write lock + read-only overlay (mask +
                // multi-window banner). Flips recorder fence on lock
                // state change.
                .add(new ModuleImports<>(List.of(
                        new WriteLockGuardModule.WriteLockGuard(),
                        new WriteLockGuardModule.ReadOnlyOverlay()),
                        WriteLockGuardModule.INSTANCE))
                // Phase 8 — CheckpointService: periodic snapshot to
                // CheckpointStore on three cadences (event-count, timer,
                // unload). Caller supplies captureState(); Phase 9
                // ReplayEngine reads via the same store.
                .add(new ModuleImports<>(List.of(
                        new CheckpointServiceModule.CheckpointService()),
                        CheckpointServiceModule.INSTANCE))
                // Phase 9 — ReplayEngine: read checkpoint + walk event
                // log with handler-driven semantics. Closes the refresh-
                // restore gap once handlers are wired (handlers map is
                // empty in the current cut; engine is present and
                // exercised end-to-end on every boot).
                .add(new ModuleImports<>(List.of(
                        new ReplayEngineModule.ReplayEngine()),
                        ReplayEngineModule.INSTANCE))
                // Phase 12 — TabRegistry: per-workspace widgetInstanceUuid
                // → {tab, slotId, kind} index. Spawn paths register;
                // replay handlers (Phase 9 vocabulary, pending) look up.
                .add(new ModuleImports<>(List.of(
                        new TabRegistryModule.TabRegistry()),
                        TabRegistryModule.INSTANCE))
                // Virtual-replay state model — fold target for the
                // ReplayEngine; checkpoint-snapshot source for the
                // CheckpointService.
                .add(new ModuleImports<>(List.of(
                        new WorkspaceStateModelModule.WorkspaceStateModel()),
                        WorkspaceStateModelModule.INSTANCE))
                // Phases 16+17+18 combined — WorkspaceControlModal:
                // clickable `kind :: name` header → modal with
                // switcher + reset + slow-motion replay sections.
                .add(new ModuleImports<>(List.of(
                        new WorkspaceControlModalModule.WorkspaceControlModal()),
                        WorkspaceControlModalModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceShellChromeModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new WorkspaceShellChrome(),
                new mountWorkspaceShell()));
    }
}
