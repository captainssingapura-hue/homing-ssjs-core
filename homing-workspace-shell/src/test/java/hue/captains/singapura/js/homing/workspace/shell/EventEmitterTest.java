package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code EventEmitter} + {@code EventRecorder}. Constructor-
 * injected event-log dep + clock makes every test a pure exercise — no
 * IndexedDB, no real {@code WorkspaceEventLog}, no real {@code Date.now}.
 *
 * <p>Two layers under test:</p>
 *
 * <ul>
 *   <li>EventEmitter.attach — produces a recorder; emits SessionStarted
 *       directly to the underlying log; survives attach failures.</li>
 *   <li>EventRecorder.emit — fenced by replaying / writeLockHeld / log
 *       presence; updates seq + cadence counters; invokes onAfterEmit.</li>
 * </ul>
 */
class EventEmitterTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/EventEmitterModule.js";

    /**
     * Stub event log + WorkspaceEventLog facade. The facade's static
     * {@code attach} returns a fresh stub log per call recording its
     * emits and the assigned seqs. Lets tests verify both the facade
     * forward (the SessionStarted boot event) and recorder.emit ↔ log
     * forwarding under all the fence permutations.
     *
     * <p>Provide globals so {@code EventEmitter.INSTANCE} construction
     * at module load doesn't blow up — tests build their own emitter
     * via constructor injection.</p>
     */
    private static final String STUBS = """
            globalThis.makeStubLog = function () {
                return {
                    emits:    [],     // [{name, payload, seqAssigned}]
                    _nextSeq: 0,
                    emit(name, payload) {
                        const seq = ++this._nextSeq;
                        this.emits.push({ name, payload, seqAssigned: seq });
                        return Promise.resolve(seq);
                    }
                };
            };
            globalThis.makeStubFacade = function () {
                return {
                    attachCalls: [],
                    lastLog:     null,
                    attach(opts) {
                        this.attachCalls.push(opts);
                        this.lastLog = makeStubLog();
                        return this.lastLog;
                    }
                };
            };
            globalThis.makeThrowingFacade = function () {
                return {
                    attach() { throw new Error('attach exploded'); }
                };
            };
            // Required for EventEmitter.INSTANCE module-load construction.
            globalThis.WorkspaceEventLog = { attach: function () { return makeStubLog(); } };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Fresh EventEmitter with the given stub facade injected. */
    private Value freshEmitter(Value facade, long clockMs) {
        Value deps = js.eval("js", "({})");
        deps.putMember("workspaceEventLog", facade);
        deps.putMember("clock",             js.eval("js", "(function () { return " + clockMs + "; })"));
        return global("EventEmitter").newInstance(deps);
    }

    /** Drain a JS Promise to a Java value. */
    private Value await(Value promise) {
        CompletableFuture<Value> fut = new CompletableFuture<>();
        promise.invokeMember("then",
                (org.graalvm.polyglot.proxy.ProxyExecutable) args -> {
                    fut.complete(args.length > 0 ? args[0] : null);
                    return null;
                });
        promise.invokeMember("catch",
                (org.graalvm.polyglot.proxy.ProxyExecutable) args -> {
                    fut.completeExceptionally(new RuntimeException(
                            args.length > 0 ? args[0].toString() : "rejected"));
                    return null;
                });
        try { return fut.get(2, TimeUnit.SECONDS); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void attachForwardsKindAndIdAndEmitsSessionStarted() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value emitter = freshEmitter(facade, 12_345L);
        Value opts = js.eval("js", """
                ({ workspaceKind: 'AP', workspaceId: 'i-1',
                   workspaceName: 'Mine' })""");

        Value recorder = emitter.invokeMember("attach", opts);

        Value attachCalls = facade.getMember("attachCalls");
        assertEquals(1, attachCalls.getArraySize());
        assertEquals("AP",  attachCalls.getArrayElement(0).getMember("workspaceKind").asString());
        assertEquals("i-1", attachCalls.getArrayElement(0).getMember("workspaceId").asString());

        Value emits = facade.getMember("lastLog").getMember("emits");
        assertEquals(1, emits.getArraySize());
        Value boot = emits.getArrayElement(0);
        assertEquals("SessionStarted", boot.getMember("name").asString());
        assertEquals("Mine",      boot.getMember("payload").getMember("workspaceName").asString());
        assertEquals(12_345L,     boot.getMember("payload").getMember("startedAt").asLong());
        assertEquals(false,       boot.getMember("payload").getMember("restored").asBoolean());
        // SessionStarted bypasses recorder.emit — _lastEmittedSeq stays 0.
        assertEquals(0, recorder.invokeMember("lastEmittedSeq").asInt());
        assertEquals(0, recorder.invokeMember("eventsSinceLastCheckpoint").asInt());
    }

    @Test
    void attachFailureReturnsRecorderWithNullLog() {
        Value emitter = freshEmitter(js.eval("js", "makeThrowingFacade()"), 1_000L);
        Value recorder = emitter.invokeMember("attach", js.eval("js", """
                ({ workspaceKind: 'AP', workspaceId: 'i', workspaceName: 'n' })"""));
        Value snap = recorder.invokeMember("inspect");
        assertEquals(false, snap.getMember("hasLog").asBoolean(),
                "attach failure → recorder has no log");
        // emit short-circuits to null.
        Value seq = await(recorder.invokeMember("emit", "X", js.eval("js", "({})")));
        assertTrue(seq == null || seq.isNull());
    }

    @Test
    void emitForwardsToLogAndUpdatesSeq() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value recorder = freshEmitter(facade, 1_000L).invokeMember("attach",
                js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i', workspaceName: 'n' })"));

        Value seq = await(recorder.invokeMember("emit", "TabAdded",
                js.eval("js", "({ slotId: 'tl', tabId: 'docview:1' })")));
        assertEquals(2, seq.asInt(),
                "log assigned seq=2 (SessionStarted got seq=1)");
        assertEquals(2, recorder.invokeMember("lastEmittedSeq").asInt());
        assertEquals(1, recorder.invokeMember("eventsSinceLastCheckpoint").asInt());

        Value emits = facade.getMember("lastLog").getMember("emits");
        assertEquals(2, emits.getArraySize());
        assertEquals("TabAdded", emits.getArrayElement(1).getMember("name").asString());
    }

    @Test
    void replayingFenceDropsEmits() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value recorder = freshEmitter(facade, 1_000L).invokeMember("attach",
                js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i', workspaceName: 'n' })"));
        recorder.invokeMember("setReplaying", true);

        Value seq = await(recorder.invokeMember("emit", "X", js.eval("js", "({})")));
        assertTrue(seq == null || seq.isNull());
        // Underlying log saw only SessionStarted (the bypass).
        assertEquals(1, facade.getMember("lastLog").getMember("emits").getArraySize());

        // Flip back and re-emit.
        recorder.invokeMember("setReplaying", false);
        await(recorder.invokeMember("emit", "Y", js.eval("js", "({})")));
        assertEquals(2, facade.getMember("lastLog").getMember("emits").getArraySize());
    }

    @Test
    void writeLockFenceDropsEmits() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value recorder = freshEmitter(facade, 1_000L).invokeMember("attach",
                js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i', workspaceName: 'n' })"));
        recorder.invokeMember("setWriteLockHeld", false);

        Value seq = await(recorder.invokeMember("emit", "X", js.eval("js", "({})")));
        assertTrue(seq == null || seq.isNull());
        assertEquals(1, facade.getMember("lastLog").getMember("emits").getArraySize(),
                "only SessionStarted got through");
        // Cadence counter unchanged.
        assertEquals(0, recorder.invokeMember("eventsSinceLastCheckpoint").asInt());
    }

    @Test
    void onAfterEmitFiresPerAcceptedEmit() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value recorder = freshEmitter(facade, 1_000L).invokeMember("attach",
                js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i', workspaceName: 'n' })"));
        Value observer = js.eval("js", """
                ({ seqs: [], cb: function (seq) { this.seqs.push(seq); } })""");
        recorder.invokeMember("setOnAfterEmit",
                js.eval("js", "(function () { const o = arguments[0]; return function (seq) { o.cb(seq); }; })")
                        .execute(observer));

        await(recorder.invokeMember("emit", "A", js.eval("js", "({})")));
        await(recorder.invokeMember("emit", "B", js.eval("js", "({})")));

        Value seqs = observer.getMember("seqs");
        assertEquals(2, seqs.getArraySize());
        assertEquals(2, seqs.getArrayElement(0).asInt());
        assertEquals(3, seqs.getArrayElement(1).asInt());
    }

    @Test
    void resetCheckpointCadenceClearsCounterKeepsSeq() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value recorder = freshEmitter(facade, 1_000L).invokeMember("attach",
                js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i', workspaceName: 'n' })"));
        await(recorder.invokeMember("emit", "A", js.eval("js", "({})")));
        await(recorder.invokeMember("emit", "B", js.eval("js", "({})")));
        assertEquals(2, recorder.invokeMember("eventsSinceLastCheckpoint").asInt());

        recorder.invokeMember("resetCheckpointCadence");
        assertEquals(0, recorder.invokeMember("eventsSinceLastCheckpoint").asInt());
        // Seq is the high-water mark; reset doesn't touch it.
        assertEquals(3, recorder.invokeMember("lastEmittedSeq").asInt());
    }

    @Test
    void onAfterEmitThrowDoesNotBlockSuccessfulEmit() {
        Value facade = js.eval("js", "makeStubFacade()");
        Value recorder = freshEmitter(facade, 1_000L).invokeMember("attach",
                js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i', workspaceName: 'n' })"));
        recorder.invokeMember("setOnAfterEmit",
                js.eval("js", "(function (seq) { throw new Error('cadence boom'); })"));

        Value seq = await(recorder.invokeMember("emit", "X", js.eval("js", "({})")));
        assertNotNull(seq);
        assertEquals(2, seq.asInt(),
                "emit returns the seq even when onAfterEmit throws");
    }
}
