package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.component.WidgetSlot;

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
                CrateEntry.of(HrefManager.INSTANCE),
                CrateEntry.of(PreferenceSteward.INSTANCE),
                CrateEntry.of(CssDependencyGraph.INSTANCE),
                CrateEntry.of(CssHandles.INSTANCE),
                CrateEntry.of(CssLoadProcedure.INSTANCE),
                // RFC 0066 E3 - the component base's own: the widget slot, a
                // primitive that places and removes a root and mints nothing.
                // Declared here because a Maven module has one crate, and this
                // crate is the base's until the rename sweep.
                CrateEntry.of(WidgetSlot.INSTANCE, StandardJsModuleType.PRIMITIVE));
    }
}
