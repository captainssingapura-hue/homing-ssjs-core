package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.C1_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record UiPreferencesComponents() implements C0_Components<UiPreferencesComponents> {

    public static final UiPreferencesComponents INSTANCE = new UiPreferencesComponents();

    @Override public String name() { return "Preferences"; }
    @Override public String summary() { return "The preferences view, the field every setting shares, and the widgets that show a setting."; }

    @Override public List<? extends C1_Components<UiPreferencesComponents, ?>> subCatalogues() { return List.of(PreferenceWidgetsComponents.INSTANCE); }

    @Override public List<ComponentEntry<UiPreferencesComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new PreferencesViewModule.PreferencesView()),
                ComponentEntry.of(this, new PreferenceFieldModule.PreferenceField()));
    }

    /** One per kind of setting: a choice, a scale, a toggle, a list's master, an overview. */
    public record PreferenceWidgetsComponents() implements C1_Components<UiPreferencesComponents, PreferenceWidgetsComponents> {
        public static final PreferenceWidgetsComponents INSTANCE = new PreferenceWidgetsComponents();
        @Override public UiPreferencesComponents parent() { return UiPreferencesComponents.INSTANCE; }
        @Override public String name() { return "Widgets"; }
        @Override public String summary() { return "One per kind of setting: a choice, a scale, a toggle, a list's master, an overview."; }
        @Override public List<ComponentEntry<PreferenceWidgetsComponents>> leaves() {
            return List.of(
                    ComponentEntry.of(this, new ChoiceWidgetModule.ChoiceWidget()),
                    ComponentEntry.of(this, new ScaleWidgetModule.ScaleWidget()),
                    ComponentEntry.of(this, new ToggleWidgetModule.ToggleWidget()),
                    ComponentEntry.of(this, new ListMasterWidgetModule.ListMasterWidget()),
                    ComponentEntry.of(this, new OverviewWidgetModule.OverviewWidget()));
        }
    }
}
