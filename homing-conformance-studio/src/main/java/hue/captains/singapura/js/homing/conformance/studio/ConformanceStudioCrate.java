package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-conformance-studio}: its four
 * workspace widgets. It {@link #requires()} {@link CoreJsCrate} because
 * {@link ModuleTreeWidget} imports {@code TreeRendererModule} (a core-js
 * module) — a genuine cross-crate edge. Declaring that one require is what
 * makes the import legal under {@code CrateDependencyRule}; omitting it (or
 * reaching it only transitively) is a layering violation, which this module's
 * test demonstrates.
 */
public final class ConformanceStudioCrate implements Crate {

    public static final ConformanceStudioCrate INSTANCE = new ConformanceStudioCrate();

    private ConformanceStudioCrate() {}

    @Override
    public String name() {
        return "homing-conformance-studio";
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(ModuleTreeWidget.INSTANCE),
                CrateEntry.of(CrateGraphWidget.INSTANCE),
                CrateEntry.of(ModuleSummaryWidget.INSTANCE),
                CrateEntry.of(ModuleContentWidget.INSTANCE),
                CrateEntry.of(CrateConformanceWidget.INSTANCE),
                CrateEntry.of(ModuleConformanceWidget.INSTANCE),
                CrateEntry.of(ConformanceReportWidget.INSTANCE),
                // The generated JS codec — headless, no DOM.
                CrateEntry.of(ReportCodecsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }

    @Override
    public List<Crate> requires() {
        // ModuleTreeWidget imports TreeRendererModule ∈ CoreJsCrate — declared directly.
        return List.of(CoreJsCrate.INSTANCE);
    }
}
