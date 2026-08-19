package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0048 — the workspace's keyboard pane-navigation layer. A small,
 * single-purpose controller ({@code PaneFocusNav}) that the shell composes over
 * the {@link hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneModule
 * MultiTabPane}, kept in its own module (Modest File Size / one orthogonal
 * concern per class) exactly like {@code MultiTabPaneDragModule} sits beside the
 * pane.
 *
 * <p>It owns only the <b>shallow-mode</b> keyboard: arrow keys drive
 * {@code focusPane(dir)} (the shared-edge cursor move), Tab / Shift+Tab drive
 * {@code cycleTabInPane(±1)} (tabs within the pane, never leaving it), and Enter
 * drives {@code enterDeep()}. In deep mode it is inert — the entered widget owns
 * the keyboard — the discriminator being {@code mtp.mode()}, not DOM focus. The
 * deep→shallow release is a separate concern (the forthcoming FocusRelease
 * module), so this class stays about one thing.</p>
 *
 * <p>Dependency-free at the framework level; constructed by the shell with the
 * live {@code mtp} and the workspace's content element (for event scoping), and
 * disposed on teardown.</p>
 */
public record PaneFocusNavModule() implements DomModule<PaneFocusNavModule> {

    /** The single export — the {@code PaneFocusNav} JS controller class. */
    public record PaneFocusNav() implements Exportable._Constant<PaneFocusNavModule> {}

    public static final PaneFocusNavModule INSTANCE = new PaneFocusNavModule();

    @Override
    public ImportsFor<PaneFocusNavModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<PaneFocusNavModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PaneFocusNav()));
    }
}
