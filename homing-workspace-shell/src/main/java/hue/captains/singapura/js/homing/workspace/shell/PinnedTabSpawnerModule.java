package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code PinnedTabSpawner} — Phase 14 of the workspace-shell chrome.
 * Reads {@code spec.pinnedSpawns} (a list of widget {@code simpleName}s)
 * and, for each match in {@code spec.entries}, creates a pinned tab in
 * a target slot and mounts the widget via {@link WidgetMounterModule}.
 *
 * <p>Replaces the {@code pinnedAutoSpawn() / spawnPinned()} pair V1
 * inlines at lines 506–574 + 1792–1801 of
 * {@code AnimalsPlaygroundChrome.bodyJs()}.</p>
 *
 * <p>The spec drives behaviour: only widgets named in
 * {@code spec.pinnedSpawns()} auto-mount; the rest stay available
 * through the picker. This is a spec-level concern — independent of
 * the widget class's default {@code lifecycleHint()} — so a workspace
 * can pin any widget without changing the widget itself.</p>
 *
 * <p>Functional Object: stateless class, single async method
 * {@code spawnAll(opts) → Promise<Tab[]>}. Tightly coupled to
 * {@link WidgetMounterModule} which it delegates the construct flow to;
 * coupling is one-way (PinnedTabSpawner imports WidgetMounter, not the
 * other way around).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 14
 */
public record PinnedTabSpawnerModule() implements DomModule<PinnedTabSpawnerModule> {

    public static final PinnedTabSpawnerModule INSTANCE = new PinnedTabSpawnerModule();

    /** The {@code PinnedTabSpawner} JS class. */
    public record PinnedTabSpawner() implements Exportable._Class<PinnedTabSpawnerModule> {}

    @Override
    public ImportsFor<PinnedTabSpawnerModule> imports() {
        return ImportsFor.<PinnedTabSpawnerModule>builder()
                .add(new ModuleImports<>(
                        List.of(new WidgetMounterModule.WidgetMounter()),
                        WidgetMounterModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PinnedTabSpawnerModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PinnedTabSpawner()));
    }
}
