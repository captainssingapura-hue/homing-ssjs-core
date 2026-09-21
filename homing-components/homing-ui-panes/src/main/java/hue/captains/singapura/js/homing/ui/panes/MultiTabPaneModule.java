package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
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
 * {@code root, setActive?, dispose?}
 * — whose root the pane appends once and never detaches; a switch hides
 * and shows panels. The pane disposes the widget on a real close and never
 * on a detach, and never calls {@code setActive}: that is the holder's,
 * told through {@code TabActivated}.</p>
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
        /** The pane holds the keys for what is inside: the strip's on a focused chip, and the active tab's widget's, whatever they are. */
        @Override public List<KeyBinding> keys() {
            return TabStripModule.TabStrip.KEYS.stream().map(b -> new KeyBinding(b.key(), b.modifiers(), "to the strip: " + b.meaning())).toList();
        }
    }

    public static final MultiTabPaneModule INSTANCE = new MultiTabPaneModule();

    @Override
    public ImportsFor<MultiTabPaneModule> imports() {
        return ImportsFor.<MultiTabPaneModule>builder()
                .add(new ModuleImports<>(List.of(new TabStripModule.TabStrip()), TabStripModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
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
