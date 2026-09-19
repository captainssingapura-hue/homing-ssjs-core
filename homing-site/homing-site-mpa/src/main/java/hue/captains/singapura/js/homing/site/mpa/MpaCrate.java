package hue.captains.singapura.js.homing.site.mpa;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;

import java.util.List;

/**
 * The MPA's served modules — the chrome, the preferences, their styles —
 * and the three framework crates they import: DomOpsParty (core-js); the
 * CSS manager, the href manager and the steward (server); the design
 * substrate's target groups that every worn word resolves through
 * (design-core). {@link StandardMpa} serves this crate beside the site's
 * own without being asked.
 */
public final class MpaCrate implements Crate {

    public static final MpaCrate INSTANCE = new MpaCrate();

    private MpaCrate() {}

    @Override public String name() { return "homing-site-mpa"; }

    @Override public List<Crate> requires() {
        return List.of(CoreJsCrate.INSTANCE, ServerCrate.INSTANCE, DesignCrate.INSTANCE);
    }

    @Override public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(MpaChrome.INSTANCE),
                CrateEntry.of(Preferences.INSTANCE),
                CrateEntry.of(MpaStyles.INSTANCE));
    }
}
