package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0049 — the workspace's <b>shallow-mode keyboard</b> layer
 * ({@code ShallowKeyboard}): the keyboard half of the
 * {@link WorkspaceFocusCoordinatorModule focus coordinator}, kept in its own
 * module per Modest File Size (one orthogonal concern per class). It is the
 * direct descendant of RFC 0048's {@code PaneFocusNav}.
 *
 * <p>Owns ONLY the shallow-mode keys — arrows move the cursor pane (the
 * shared-edge move via {@code mtp.neighbourOf}), Tab / Shift+Tab cycle the
 * shown tab <em>within</em> the cursor pane, Enter upgrades the cursor pane to
 * deep. Inert while the selection is deep (the entered widget owns the
 * keyboard); the discriminator is the coordinator's {@code mode()}, not DOM
 * focus. Constructed and disposed by the coordinator.</p>
 */
public record WorkspaceShallowKeyboardModule() implements DomModule<WorkspaceShallowKeyboardModule> {

    /** The single export — the {@code ShallowKeyboard} JS controller class. */
    public record ShallowKeyboard() implements Exportable._Constant<WorkspaceShallowKeyboardModule> {}

    public static final WorkspaceShallowKeyboardModule INSTANCE = new WorkspaceShallowKeyboardModule();

    @Override
    public ImportsFor<WorkspaceShallowKeyboardModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WorkspaceShallowKeyboardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ShallowKeyboard()));
    }
}
