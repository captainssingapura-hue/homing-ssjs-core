package hue.captains.singapura.js.homing.site.demo.gallery;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.site.mpa.MpaCrate;

import java.util.List;

/** The gallery's served modules: two apps and their styles, on the MPA's crate. */
public final class GalleryCrate implements Crate {

    public static final GalleryCrate INSTANCE = new GalleryCrate();

    private GalleryCrate() {}

    @Override public String name() { return "homing-site-demo-gallery"; }

    @Override public List<Crate> requires() { return List.of(MpaCrate.INSTANCE); }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WelcomeApp.INSTANCE),
                CrateEntry.of(CounterApp.INSTANCE),
                CrateEntry.of(GalleryStyles.INSTANCE));
    }
}
