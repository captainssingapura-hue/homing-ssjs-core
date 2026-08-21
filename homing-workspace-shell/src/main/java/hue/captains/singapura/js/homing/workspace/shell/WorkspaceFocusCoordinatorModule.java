package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.ui.layout.FocusManagerModule;

import java.util.List;

/**
 * RFC 0049 — the workspace's <b>focus coordinator</b>, generalising RFC 0048's
 * {@code PaneFocusNav}. {@code SplitPane} and {@code MultiTabPane} are
 * focus-agnostic primitives; this shell-layer controller owns the deep/shallow
 * focus <em>logic</em>:
 *
 * <ul>
 *   <li>the ONE selection {@code { slotId, tabId, deep }} (exclusivity);</li>
 *   <li>the per-tab {@link FocusManagerModule FocusManager}s — created on MTP's
 *       tab-content elements as tabs are added, disposed with them;</li>
 *   <li>click routing — MTP reports cover clicks ({@code onChromeInteract});
 *       the coordinator decides (single = select shallow, double = deep);</li>
 *   <li>the shallow keyboard — arrows drive the cursor via
 *       {@code mtp.neighbourOf}, Tab cycles tabs within the pane, Enter
 *       upgrades to deep; inert while deep (the widget owns the keyboard);</li>
 *   <li>the unified release — the tab's FM does the immutable work
 *       (inert / blur / setActive(false)); the coordinator's follow-up is its
 *       intent: give-up → shallow-select the same tab, select-other →
 *       continue, reposition / removal → shallow-select at the new place.</li>
 * </ul>
 *
 * <p>MTP is driven purely through its renderer facet
 * ({@code paintSelection} / {@code setAddEnabled}) and access facet
 * ({@code contentElOf} / {@code neighbourOf} / {@code getState}); the chrome
 * fans MTP's structural events into the coordinator's {@code on*} handlers.
 * Constructed by the shell with the live {@code mtp} and the workspace content
 * element (for key scoping); disposed on teardown.</p>
 */
public record WorkspaceFocusCoordinatorModule() implements DomModule<WorkspaceFocusCoordinatorModule> {

    /** The single export — the {@code WorkspaceFocusCoordinator} JS controller class. */
    public record WorkspaceFocusCoordinator() implements Exportable._Constant<WorkspaceFocusCoordinatorModule> {}

    public static final WorkspaceFocusCoordinatorModule INSTANCE = new WorkspaceFocusCoordinatorModule();

    @Override
    public ImportsFor<WorkspaceFocusCoordinatorModule> imports() {
        return ImportsFor.<WorkspaceFocusCoordinatorModule>builder()
                // The per-tab FocusManager primitive (studio-base) — the
                // coordinator instantiates one per tab on its content element.
                .add(new ModuleImports<>(
                        List.of(new FocusManagerModule.FocusManager()),
                        FocusManagerModule.INSTANCE))
                // The shallow-mode keyboard — the coordinator's keyboard half,
                // split per Modest File Size; composed in attach().
                .add(new ModuleImports<>(
                        List.of(new WorkspaceShallowKeyboardModule.ShallowKeyboard()),
                        WorkspaceShallowKeyboardModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceFocusCoordinatorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceFocusCoordinator()));
    }
}
