package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiPanesComponents() implements C0_Components<UiPanesComponents> {

    public static final UiPanesComponents INSTANCE = new UiPanesComponents();

    @Override public String name() { return "Panes"; }
    @Override public String summary() { return "Tabs over panels, the strip of chips on its own, and picking a pane off a small map of them."; }

    @Override public List<ComponentEntry<UiPanesComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new MultiTabPaneModule.MultiTabPane()),
                ComponentEntry.of(this, new TabStripModule.TabStrip()),
                ComponentEntry.of(this, new PaneThumbsModule.PaneThumbs()),
                ComponentEntry.of(this, new AddTabModule.AddTab()));
    }
}
