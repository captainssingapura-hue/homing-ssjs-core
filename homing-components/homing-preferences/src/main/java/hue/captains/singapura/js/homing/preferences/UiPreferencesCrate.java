package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;

import java.util.List;

/**
 * The preferences component's crate: the view, the field, the four widgets
 * and the styles. A site's {@link PreferencesRegistry} instance is the
 * site's to declare, in the site's crate, since its content is the site's.
 */
public final class UiPreferencesCrate implements Crate {

    public static final UiPreferencesCrate INSTANCE = new UiPreferencesCrate();

    private UiPreferencesCrate() {}

    @Override public String name() { return "homing-preferences"; }

    @Override public List<Crate> requires() {
        return List.of(ServerCrate.INSTANCE, CoreJsCrate.INSTANCE, DesignCrate.INSTANCE, UiElementsCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(PreferencesView.INSTANCE),
                CrateEntry.of(PreferenceField.INSTANCE),
                CrateEntry.of(ChoiceWidget.INSTANCE),
                CrateEntry.of(ToggleWidget.INSTANCE),
                CrateEntry.of(ScaleWidget.INSTANCE),
                CrateEntry.of(OverviewWidget.INSTANCE),
                CrateEntry.of(PreferencesStyles.INSTANCE));
    }
}
