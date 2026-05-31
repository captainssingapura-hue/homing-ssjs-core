package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code TabRegistry} — Phase 12 of the workspace-shell chrome. The
 * substrate-side index that maps {@code widgetInstanceUuid → {tab,
 * slotId}}. Every spawn path (pinned, picker) records into this index
 * and every event-replay handler reads from it to find the live tab
 * matching a recorded UUID.
 *
 * <p>The index lives in-memory per workspace; it's reconstructed on
 * every boot as spawn paths execute (whether from
 * {@code PinnedTabSpawner.onEmpty} or from replay handlers). No
 * persistence — the event log + checkpoint are the durable truth.</p>
 *
 * <p>This is the missing piece between event-replay and DOM mutation.
 * V1 inlines the same map as an anonymous closure; pulling it into a
 * named substrate class lets handlers in the orchestrator stay one-
 * liner thin.</p>
 *
 * <p>Explicit Substrate: per-workspace instance (not INSTANCE singleton).
 * No external collaborators — pure state container.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 12
 */
public record TabRegistryModule()
        implements DomModule<TabRegistryModule> {

    public static final TabRegistryModule INSTANCE = new TabRegistryModule();

    /** The {@code TabRegistry} JS class. */
    public record TabRegistry() implements Exportable._Class<TabRegistryModule> {}

    @Override
    public ImportsFor<TabRegistryModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<TabRegistryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabRegistry()));
    }
}
