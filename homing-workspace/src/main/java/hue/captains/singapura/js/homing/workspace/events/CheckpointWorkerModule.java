package hue.captains.singapura.js.homing.workspace.events;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0034 P2 — Web Worker for checkpoint writes (LMAX consumer thread).
 *
 * <p>This DomModule exists to make the worker JS reachable via the
 * framework's classpath-resource serving — the main thread constructs
 * the worker with {@code new Worker(url, \\{ type: 'module' \\})} where
 * the URL is derived from this module's fully-qualified class name
 * (the {@code /module?class=&lt;FQN&gt;} pattern the framework uses for
 * every served ES module).</p>
 *
 * <h2>No exports</h2>
 *
 * <p>The worker is not imported by any other module — it's loaded by
 * URL into a separate JS execution context. So {@code exports()} returns
 * an empty list. The worker speaks {@code postMessage} only; its public
 * surface is the message contract, not named exports.</p>
 *
 * <h2>Message contract</h2>
 *
 * <pre>{@code
 * // Main thread → worker:
 * worker.postMessage({
 *     reqId:        Number,    // correlation id for the reply
 *     kind:         String,
 *     workspaceId:  String,
 *     state:        Object,    // already encoded by WorkspaceStateCodec
 *     lastEventSeq: Number
 * });
 *
 * // Worker → main thread:
 * { reqId, ok: true,  storedSeq: Number }
 * { reqId, ok: false, error:     String }
 * }</pre>
 *
 * <h2>Fallback</h2>
 *
 * <p>The chrome's {@code _writeCheckpointNow} retains the V1 main-thread
 * write path as a fallback. If {@code typeof Worker === 'undefined'} or
 * the worker fails to initialize, the chrome falls through to the
 * main-thread {@code CheckpointStore.write(...)} call. Discipline of
 * periodic snapshotting survives; only the off-thread guarantee is lost.</p>
 *
 * @since RFC 0034 P2
 */
public record CheckpointWorkerModule() implements DomModule<CheckpointWorkerModule> {

    public static final CheckpointWorkerModule INSTANCE = new CheckpointWorkerModule();

    @Override
    public ImportsFor<CheckpointWorkerModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<CheckpointWorkerModule> exports() {
        // Workers don't get imported — they're loaded by URL into a
        // separate execution context. No named exports needed.
        return new ExportsOf<>(INSTANCE, List.of());
    }
}
