package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;

import java.util.ArrayList;
import java.util.List;

/**
 * The crate of the design substrate: the 29 target groups it serves. A
 * target exports nothing and declares no class; it is served so that a sheet
 * has an address. What a target's sheet carries is decided by what the
 * deployment's components wear, at boot, by whoever renders it.
 */
public final class DesignCrate implements Crate {

    public static final DesignCrate INSTANCE = new DesignCrate();

    private DesignCrate() {}

    @Override public String name() { return "homing-design-core"; }

    @Override
    public List<CrateEntry> entries() {
        var out = new ArrayList<CrateEntry>();
        for (Target t : Target.leaves()) out.add(CrateEntry.of((EsModule<?>) t));
        return List.copyOf(out);
    }
}
