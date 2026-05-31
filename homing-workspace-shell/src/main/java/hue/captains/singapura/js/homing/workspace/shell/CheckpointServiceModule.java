package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.events.CheckpointStoreModule;

import java.util.List;

/**
 * {@code CheckpointService} — Phase 8 of the workspace-shell chrome.
 * Drives periodic snapshots of the live workspace state to the
 * {@code CheckpointStore}, so refresh-time replay can start from a
 * recent baseline rather than re-walking the entire event log.
 *
 * <p>Three cadence triggers, all gated by
 * {@code recorder.inspect()} (read-only / replaying → skip):</p>
 *
 * <ul>
 *   <li><b>event-count</b> — fires when
 *       {@code recorder.eventsSinceLastCheckpoint()} crosses
 *       {@code opts.eventCountM} (default 50). Wired via the recorder's
 *       {@code setOnAfterEmit} so every accepted emit re-arms the check.</li>
 *   <li><b>timer</b> — fires every {@code opts.intervalMs} (default
 *       30000ms). Skipped if the cadence counter is 0.</li>
 *   <li><b>unload</b> — {@code window.pagehide} + {@code beforeunload}
 *       best-effort flush. The IDB transaction can't be awaited at unload
 *       (browser-imposed) but typically commits before the page
 *       disappears.</li>
 * </ul>
 *
 * <p>The service does NOT know the state shape. The caller passes a
 * {@code captureState} callback that returns the wire-format object
 * ready to hand to {@code CheckpointStore.write} — typically the
 * orchestrator wires this to the live MTP layout + widget instance
 * map encoded via the codec registry.</p>
 *
 * <p>Returns a controller exposing {@code captureNow(reason)} (manual
 * trigger for DevTools / shutdown paths), {@code stop()} (tear down
 * timer + unload listeners), and {@code inspect()}.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static. Constructor
 * injection for {@code checkpointStoreFactory}, {@code timer},
 * {@code window} keeps every IO seam stubbable.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 8
 */
public record CheckpointServiceModule()
        implements DomModule<CheckpointServiceModule> {

    public static final CheckpointServiceModule INSTANCE = new CheckpointServiceModule();

    /** The {@code CheckpointService} JS class. */
    public record CheckpointService() implements Exportable._Class<CheckpointServiceModule> {}

    @Override
    public ImportsFor<CheckpointServiceModule> imports() {
        return ImportsFor.<CheckpointServiceModule>builder()
                .add(new ModuleImports<>(
                        List.of(new CheckpointStoreModule.createCheckpointStore()),
                        CheckpointStoreModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CheckpointServiceModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CheckpointService()));
    }
}
