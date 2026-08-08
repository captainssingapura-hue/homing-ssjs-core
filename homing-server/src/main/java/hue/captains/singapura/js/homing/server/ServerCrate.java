package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-server}: the two runtime
 * manager modules it ships. A leaf crate — both modules import nothing, so it
 * requires no other crate.
 */
public final class ServerCrate implements Crate {

    public static final ServerCrate INSTANCE = new ServerCrate();

    private ServerCrate() {}

    @Override
    public String name() {
        return "homing-server";
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(CssClassManager.INSTANCE),
                CrateEntry.of(HrefManager.INSTANCE));
    }
}
