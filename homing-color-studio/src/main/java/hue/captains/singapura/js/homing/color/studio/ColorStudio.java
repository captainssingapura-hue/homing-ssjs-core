package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;

/**
 * The standalone Color Studio. A near-empty {@link Studio} whose only job is to
 * host the {@code color} workspace ({@link ColorWorkspaceSpec}) in a runnable
 * server — {@code home()} anchors the framework's root/brand contract, and all
 * other studio seams inherit their empty defaults.
 */
public record ColorStudio() implements Studio<ColorCatalogue> {

    public static final ColorStudio INSTANCE = new ColorStudio();

    @Override public ColorCatalogue home() { return ColorCatalogue.INSTANCE; }

    @Override
    public StudioBrand standaloneBrand() {
        return new StudioBrand("Homing · Colours", ColorCatalogue.class);
    }
}
