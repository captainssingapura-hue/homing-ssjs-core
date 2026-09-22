package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.Modifier;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.menu.NeedContextMenu;
import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;

import java.util.List;
import java.util.Set;

/**
 * {@code MultiTabPane}, one pane of tabs. {@code new MultiTabPane(branch,
 * {host, …})} builds a strip of chips over one content area on the
 * sub-branch the caller made for it, appends it to the host, and is the
 * pane: {@code addTab}, {@code attachTab}, {@code removeTab},
 * {@code detachTab}, {@code switchTab}, {@code moveTab}, the readers,
 * {@code dispose}. A branch component.
 *
 * <p>A tab holds a widget by the base's contract — an instance with
 * {@code root, focus, activate(), setActive?, dispose?} — whose root the
 * pane appends once and never detaches; a switch hides and shows panels.
 * The law: a tab's widget is logically focusable — a member of the dock's
 * branch of the focus party, exposing its membership as {@code focus}, and
 * answering {@code activate()}; what it contains natively is its own,
 * encapsulated; {@code addTab} and {@code attachTab} refuse a widget that
 * is not, and {@code attachTab} adopts a membership from another dock. The
 * pane is a member holding the dock's branch; its keys, while it holds
 * them, are the container's own — the tabs walked, reordered and
 * detached, the widget entered, the pane yielded — and a chip takes no
 * native focus. The pane disposes the widget on a real close and never on
 * a detach, and never calls {@code setActive}: that is the holder's, told
 * through {@code TabActivated}.</p>
 *
 * <p>Every mutation is one {@link PaneEvent} on one sink, {@code onEvent(ev)}:
 * a frozen object tagged by kind whose fields are the record's components,
 * built by {@link PaneEventsModule}. The vocabulary is the studio pane's —
 * added, removed, moved, activated, attached, add requested — and the shape
 * is data, so an event goes into a log or a checkpoint as it is. Built fresh beside
 * {@code MultiTabPaneModule}, which stays as it is; the strip, its chips and
 * the drag that reorders are {@link TabStrip}'s. The split of panes and the
 * drag between them are rounds of their own.</p>
 */
public record MultiTabPaneModule() implements DomModule<MultiTabPaneModule> {

    /** The class. */
    public record MultiTabPane() implements BranchComponent<MultiTabPaneModule>, NeedContextMenu, NeedKeyboard {
        @Override public String summary() { return "Tabs in a strip over one panel each; a press selects, a drag reorders along the rail, a right-click offers the tab's menu."; }
        /** The tab menu: opened on a chip when the pane is given a steward ({@code menus}); the site serving the pane serves the kind. */
        @Override public Set<ContextMenuKind<?>> required() { return Set.of(TabMenu.INSTANCE); }
        /** The container's own, while the pane holds the keys: the tabs walked, reordered and detached; the widget entered; the pane yielded; the menu. */
        @Override public List<KeyBinding> keys() {
            return List.of(
                    KeyBinding.of(Key.ARROW_LEFT, "the active tab moves to the previous, at once"), KeyBinding.of(Key.ARROW_RIGHT, "the active tab moves to the next, at once"),
                    KeyBinding.of(Key.HOME, "the first tab"), KeyBinding.of(Key.END, "the last tab"),
                    KeyBinding.of(Key.ARROW_LEFT, Modifier.SHIFT, "the active tab moves one slot left along the rail, staying active"),
                    KeyBinding.of(Key.ARROW_RIGHT, Modifier.SHIFT, "the active tab moves one slot right along the rail, staying active"),
                    KeyBinding.of(Key.ARROW_DOWN, Modifier.SHIFT, "the active tab asked to detach and float: DetachRequested, for a holder with a desk"),
                    KeyBinding.of(Key.ENTER, "the active tab's widget activates itself - a claim; the pane never claims for it"),
                    KeyBinding.of(Key.ESCAPE, "the pane yields, up the tree"),
                    KeyBinding.of(Key.CONTEXT_MENU, "the active tab's menu"), KeyBinding.of(Key.F10, Modifier.SHIFT, "the active tab's menu"));
        }
    }

    public static final MultiTabPaneModule INSTANCE = new MultiTabPaneModule();

    @Override
    public ImportsFor<MultiTabPaneModule> imports() {
        return ImportsFor.<MultiTabPaneModule>builder()
                .add(new ModuleImports<>(List.of(new TabStripModule.TabStrip()), TabStripModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneKeysModule.PaneKeys()), PaneKeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneEventsModule.PaneEvents()), PaneEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_pane(),
                        new PaneStyles.mtp_content(),
                        new PaneStyles.mtp_tab_content(),
                        new PaneStyles.mtp_tab_content_hidden(),
                        new PaneStyles.mtp_empty(),
                        new PaneStyles.mtp_dock_target()
                ), PaneStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MultiTabPaneModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MultiTabPane()));
    }
}
