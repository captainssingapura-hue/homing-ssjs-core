package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-core-js}: the JS-module
 * substrate primitives this Maven module ships. A leaf crate — it requires no
 * other crate ({@code homing-core}, its only dependency, ships no JS modules).
 *
 * <p>Pilot for the Crate model: its completeness is guarded by
 * {@code OrphanCheck} (nothing served here may go undeclared) and its imports
 * by {@code CrateDependencyRule}, both exercised in this module's own tests.</p>
 */
public final class CoreJsCrate implements Crate {

    public static final CoreJsCrate INSTANCE = new CoreJsCrate();

    private CoreJsCrate() {}

    @Override
    public String name() {
        return "homing-core-js";
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(TreeRendererModule.INSTANCE),
                CrateEntry.of(DocTreeRendererModule.INSTANCE),
                CrateEntry.of(DomOpsPartyModule.INSTANCE),
                CrateEntry.of(DomOpsPartyBaseModule.INSTANCE),
                CrateEntry.of(NodeContentModule.INSTANCE));
    }
}
