package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

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
    public record MultiTabPane() implements Exportable._Constant<MultiTabPaneModule> {}

    public static final MultiTabPaneModule INSTANCE = new MultiTabPaneModule();

    @Override
    public ImportsFor<MultiTabPaneModule> imports() {
        return ImportsFor.<MultiTabPaneModule>builder()
                .add(new ModuleImports<>(List.of(new TabStripModule.TabStrip()), TabStripModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneEventsModule.PaneEvents()), PaneEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_pane(),
                        new PaneStyles.mtp_content(),
                        new PaneStyles.mtp_tab_content(),
                        new PaneStyles.mtp_tab_content_hidden(),
                        new PaneStyles.mtp_empty()
                ), PaneStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MultiTabPaneModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MultiTabPane()));
    }
}
