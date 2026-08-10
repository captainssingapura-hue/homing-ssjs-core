package hue.captains.singapura.js.homing.libs;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-libs}: its bundled third-party
 * modules ({@link HomingLibsRegistry#ALL}) plus {@link MermaidProxyModule} (an
 * external CDN proxy the registry doesn't list). The orphan check surfaced the
 * proxy — a good demonstration that the crate is the complete inventory, not
 * just the bundled-externals registry. A leaf crate: all import nothing.
 */
public final class LibsCrate implements Crate {

    public static final LibsCrate INSTANCE = new LibsCrate();

    private LibsCrate() {}

    @Override
    public String name() {
        return "homing-libs";
    }

    @Override
    public List<CrateEntry> entries() {
        var entries = new ArrayList<CrateEntry>();
        HomingLibsRegistry.ALL.forEach(m -> entries.add(CrateEntry.of(m)));
        entries.add(CrateEntry.of(MermaidProxyModule.INSTANCE));
        return List.copyOf(entries);
    }
}
