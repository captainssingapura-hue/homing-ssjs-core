package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.component.C0_Components;
import hue.captains.singapura.js.homing.component.ComponentEntry;

import java.util.List;

/** The crate's catalogue of components: what it delivers, as components rather than as files. */
public record MpaComponents() implements C0_Components<MpaComponents> {

    public static final MpaComponents INSTANCE = new MpaComponents();

    @Override public String name() { return "Site chrome"; }
    @Override public String summary() { return "The bar over a page, its preferences button, and the theme as a preference."; }

    @Override public List<ComponentEntry<MpaComponents>> leaves() {
        return List.of(
                ComponentEntry.of(this, new MpaChromeModule.MpaChrome()),
                ComponentEntry.of(this, new PreferencesButtonModule.PreferencesButton()),
                ComponentEntry.of(this, new ThemeWidgetModule.ThemeWidget()));
    }
}
