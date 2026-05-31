package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code CheckpointService}. Constructor-injected store
 * factory + manual timer + stub window make every cadence deterministic
 * — no real {@code setInterval}, no real IndexedDB, no real
 * {@code window} listeners.
 *
 * <p>Each test builds a fresh service + per-test stub recorder so we
 * can exercise gate semantics ({@code replaying} / {@code !writeLockHeld}
 * → skip), the three cadence triggers, and the write tally.</p>
 */
class CheckpointServiceTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/CheckpointServiceModule.js";

    private static final String STUBS = """
            // Stub store — records every write call.
            globalThis.makeStubStore = function () {
                return {
                    writes: [],
                    write(kind, id, state, seq) {
                        this.writes.push({ kind, id, state, seq });
                        return Promise.resolve();
                    }
                };
            };
            globalThis.makeFailingStore = function () {
                return {
                    writes: [],
                    write(_k, _i, _s, _q) {
                        return Promise.reject(new Error('idb full'));
                    }
                };
            };
            // Stub recorder — tests dial up the cadence counter + fence flags.
            globalThis.makeStubRecorder = function () {
                const r = {
                    _onAfterEmit: null,
                    _cadence:     0,
                    _lastSeq:     0,
                    _replaying:   false,
                    _writeLockHeld: true,
                    inspect() { return { replaying: this._replaying,
                                         writeLockHeld: this._writeLockHeld }; },
                    setOnAfterEmit(fn) { this._onAfterEmit = fn || function () {}; },
                    eventsSinceLastCheckpoint() { return this._cadence; },
                    resetCheckpointCadence()    { this._cadence = 0; },
                    lastEmittedSeq()            { return this._lastSeq; },
                    /** Simulate an emit: bumps both counters; fires onAfterEmit. */
                    simulateEmit(seq) {
                        this._cadence++;
                        this._lastSeq = seq;
                        if (this._onAfterEmit) this._onAfterEmit(seq);
                    }
                };
                return r;
            };
            // Manual timer driver — records setInterval calls, exposes
            // .tick() so tests fire the interval synchronously.
            globalThis.makeManualTimer = function () {
                return {
                    intervals: [],
                    setInterval(fn, ms) {
                        const id = this.intervals.length + 1;
                        this.intervals.push({ id, fn, ms, cleared: false });
                        return id;
                    },
                    clearInterval(id) {
                        const slot = this.intervals.find(i => i.id === id);
                        if (slot) slot.cleared = true;
                    },
                    tick(id) {
                        const slot = this.intervals.find(i => i.id === id);
                        if (slot && !slot.cleared) slot.fn();
                    }
                };
            };
            // Stub window — records addEventListener / removeEventListener.
            globalThis.makeStubWindow = function () {
                return {
                    listeners: [],
                    addEventListener(t, fn)    { this.listeners.push({ t, fn, active: true }); },
                    removeEventListener(t, fn) {
                        this.listeners.forEach(l => {
                            if (l.t === t && l.fn === fn) l.active = false;
                        });
                    },
                    /** Fire all listeners for an event name (only active ones). */
                    dispatch(t) {
                        this.listeners.filter(l => l.t === t && l.active)
                                      .forEach(l => l.fn());
                    }
                };
            };
            // Required for CheckpointService.INSTANCE module-load construction.
            globalThis.createCheckpointStore = function () { return makeStubStore(); };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    private Value freshService(Value storeFactory, Value timer, Value window) {
        Value deps = js.eval("js", "({})");
        if (!storeFactory.isNull()) deps.putMember("checkpointStoreFactory", storeFactory);
        else                        deps.putMember("checkpointStoreFactory", js.eval("js", "null"));
        deps.putMember("timer",  timer);
        deps.putMember("window", window);
        // Explicitly disable worker — tests in this class exercise the
        // main-thread path. Worker-path coverage lives in the dedicated
        // workerPathReceivesPostMessage test below.
        deps.putMember("WorkerCtor", js.eval("js", "null"));
        return global("CheckpointService").newInstance(deps);
    }

    /** Service variant that uses a stub Worker — for worker-path tests. */
    private Value freshServiceWithWorker(Value storeFactory, Value timer,
                                         Value window, Value workerCtor) {
        Value deps = js.eval("js", "({})");
        if (!storeFactory.isNull()) deps.putMember("checkpointStoreFactory", storeFactory);
        else                        deps.putMember("checkpointStoreFactory", js.eval("js", "null"));
        deps.putMember("timer",     timer);
        deps.putMember("window",    window);
        deps.putMember("WorkerCtor", workerCtor);
        deps.putMember("workerUrl",  "stub://worker");
        return global("CheckpointService").newInstance(deps);
    }

    private Value attachOpts(Value recorder, String captureExpr) {
        Value opts = js.eval("js", """
                ({ workspaceKind: 'K', workspaceId: 'i-1',
                   intervalMs: 1000, eventCountM: 5 })""");
        opts.putMember("recorder",     recorder);
        opts.putMember("captureState", js.eval("js", "(function () { return " + captureExpr + "; })"));
        return opts;
    }

    /** Drain a JS Promise to a Java value (2s timeout). */
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
    void captureNowWritesWhenGateOpen() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function () { return arguments[0]; })").execute(store);
        // Wait — factory must be a 0-arg function that returns the store.
        factory = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        recorder.putMember("_lastSeq", 42);
        Value ctrl = freshService(factory, js.eval("js", "makeManualTimer()"),
                                  js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({ a: 1 })"));

        Value seq = await(ctrl.invokeMember("captureNow", "manual"));
        assertEquals(42, seq.asInt());
        assertEquals(1, store.getMember("writes").getArraySize());
        assertEquals(42, store.getMember("writes").getArrayElement(0).getMember("seq").asInt());
        // Cadence reset after write.
        assertEquals(0, recorder.invokeMember("eventsSinceLastCheckpoint").asInt());
    }

    @Test
    void captureNowSkipsWhenReplaying() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        recorder.putMember("_replaying", true);
        Value ctrl = freshService(factory, js.eval("js", "makeManualTimer()"),
                                  js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({})"));
        Value seq = await(ctrl.invokeMember("captureNow", "x"));
        assertTrue(seq == null || seq.isNull());
        assertEquals(0, store.getMember("writes").getArraySize());
    }

    @Test
    void captureNowSkipsWhenWriteLockNotHeld() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        recorder.putMember("_writeLockHeld", false);
        Value ctrl = freshService(factory, js.eval("js", "makeManualTimer()"),
                                  js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({})"));
        await(ctrl.invokeMember("captureNow", "x"));
        assertEquals(0, store.getMember("writes").getArraySize());
    }

    @Test
    void eventCountCadenceFiresAtThreshold() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        freshService(factory, js.eval("js", "makeManualTimer()"),
                     js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({})"));
        // eventCountM = 5. First 4 emits don't trigger; 5th does.
        for (int i = 1; i <= 4; i++) recorder.invokeMember("simulateEmit", i);
        await(js.eval("js", "Promise.resolve()"));   // drain microtasks
        assertEquals(0, store.getMember("writes").getArraySize());

        recorder.invokeMember("simulateEmit", 5);
        await(js.eval("js", "Promise.resolve()"));
        assertEquals(1, store.getMember("writes").getArraySize());
        assertEquals(5, store.getMember("writes").getArrayElement(0).getMember("seq").asInt());
    }

    @Test
    void timerCadenceFiresWhenCadenceNonZero() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value timer    = js.eval("js", "makeManualTimer()");
        Value recorder = js.eval("js", "makeStubRecorder()");
        freshService(factory, timer, js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({})"));
        int intervalId = timer.getMember("intervals").getArrayElement(0).getMember("id").asInt();

        // Tick with cadence=0 → no write.
        timer.invokeMember("tick", intervalId);
        await(js.eval("js", "Promise.resolve()"));
        assertEquals(0, store.getMember("writes").getArraySize());

        // Emit, then tick → write fires.
        recorder.invokeMember("simulateEmit", 7);
        timer.invokeMember("tick", intervalId);
        await(js.eval("js", "Promise.resolve()"));
        assertEquals(1, store.getMember("writes").getArraySize());
    }

    @Test
    void unloadCadenceFiresOnPageHide() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value window   = js.eval("js", "makeStubWindow()");
        Value recorder = js.eval("js", "makeStubRecorder()");
        freshService(factory, js.eval("js", "makeManualTimer()"), window)
                .invokeMember("attach", attachOpts(recorder, "({})"));
        recorder.invokeMember("simulateEmit", 11);
        window.invokeMember("dispatch", "pagehide");
        await(js.eval("js", "Promise.resolve()"));
        assertEquals(1, store.getMember("writes").getArraySize());
        assertEquals(11, store.getMember("writes").getArrayElement(0).getMember("seq").asInt());
    }

    @Test
    void stopTearsDownTimerAndUnloadListeners() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value timer    = js.eval("js", "makeManualTimer()");
        Value window   = js.eval("js", "makeStubWindow()");
        Value recorder = js.eval("js", "makeStubRecorder()");
        Value ctrl = freshService(factory, timer, window)
                .invokeMember("attach", attachOpts(recorder, "({})"));
        ctrl.invokeMember("stop");

        // Timer cleared.
        assertEquals(true, timer.getMember("intervals").getArrayElement(0)
                                .getMember("cleared").asBoolean());
        // Listeners marked inactive.
        Value ls = window.getMember("listeners");
        for (int i = 0; i < (int) ls.getArraySize(); i++) {
            assertEquals(false, ls.getArrayElement(i).getMember("active").asBoolean());
        }
        // Subsequent captureNow is a no-op.
        await(ctrl.invokeMember("captureNow", "after-stop"));
        assertEquals(0, store.getMember("writes").getArraySize());
    }

    @Test
    void writeFailureTalliesButDoesNotThrow() {
        Value store    = js.eval("js", "makeFailingStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        Value ctrl = freshService(factory, js.eval("js", "makeManualTimer()"),
                                  js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({})"));
        Value seq = await(ctrl.invokeMember("captureNow", "x"));
        assertTrue(seq == null || seq.isNull());
        Value snap = ctrl.invokeMember("inspect");
        assertEquals(1, snap.getMember("writesAttempted").asInt());
        assertEquals(0, snap.getMember("writesOk").asInt());
        assertEquals(1, snap.getMember("writesFailed").asInt());
    }

    @Test
    void noStoreFactoryDegradesGracefully() {
        Value recorder = js.eval("js", "makeStubRecorder()");
        Value ctrl = freshService(js.eval("js", "null"),
                                  js.eval("js", "makeManualTimer()"),
                                  js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "({})"));
        Value seq = await(ctrl.invokeMember("captureNow", "x"));
        assertTrue(seq == null || seq.isNull());
        assertEquals(false, ctrl.invokeMember("inspect").getMember("hasStore").asBoolean());
    }

    @Test
    void workerPathReceivesPostMessageAndResolvesWithStoredSeq() {
        // Stub Worker — captures every postMessage and lets us reply
        // synthetically via .respond({reqId, ok, storedSeq}).
        Value workerCtor = js.eval("js", """
                (function () {
                    function StubWorker(url, opts) {
                        this.url = url;
                        this.posts = [];
                        this.onmessage = null;
                        this.onerror   = null;
                        this.terminated = false;
                    }
                    StubWorker.prototype.postMessage = function (msg) {
                        this.posts.push(msg);
                    };
                    StubWorker.prototype.terminate = function () { this.terminated = true; };
                    /** Test helper: simulate a reply for one of the posts. */
                    StubWorker.prototype.respond = function (replyData) {
                        if (this.onmessage) this.onmessage({ data: replyData });
                    };
                    globalThis._lastStubWorker = null;
                    return function (url, opts) {
                        const w = new StubWorker(url, opts);
                        globalThis._lastStubWorker = w;
                        return w;
                    };
                })()""");
        Value timer    = js.eval("js", "makeManualTimer()");
        Value window   = js.eval("js", "makeStubWindow()");
        Value recorder = js.eval("js", "makeStubRecorder()");
        recorder.putMember("_lastSeq", 42);
        Value ctrl = freshServiceWithWorker(js.eval("js", "null"),  // no store needed for worker path
                                            timer, window, workerCtor)
                .invokeMember("attach", attachOpts(recorder, "({ snap: 'data' })"));
        // Worker path needs a non-null store too (for the .store() accessor)
        // — re-do attach with a store but still using worker.
        timer    = js.eval("js", "makeManualTimer()");
        window   = js.eval("js", "makeStubWindow()");
        recorder = js.eval("js", "makeStubRecorder()");
        recorder.putMember("_lastSeq", 42);
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        ctrl = freshServiceWithWorker(factory, timer, window, workerCtor)
                .invokeMember("attach", attachOpts(recorder, "({ snap: 'data' })"));

        Value promise = ctrl.invokeMember("captureNow", "manual");
        Value worker  = js.getBindings("js").getMember("_lastStubWorker");
        Value posts   = worker.getMember("posts");
        assertEquals(1, posts.getArraySize());
        Value post = posts.getArrayElement(0);
        assertEquals("K",   post.getMember("kind").asString());
        assertEquals("i-1", post.getMember("workspaceId").asString());
        assertEquals(42,    post.getMember("lastEventSeq").asInt());

        // Worker replies with success.
        int reqId = post.getMember("reqId").asInt();
        worker.invokeMember("respond", js.eval("js", """
                ({ reqId: """ + reqId + """
                ,
                   ok: true, storedSeq: 42, prunedCount: 5 })"""));

        Value seq = await(promise);
        assertEquals(42, seq.asInt());
        Value snap = ctrl.invokeMember("inspect");
        assertEquals(1, snap.getMember("workerWrites").asInt());
        assertEquals(0, snap.getMember("mainThreadWrites").asInt());
        assertEquals(5, snap.getMember("pruned").asInt(),
                "worker-reported prunedCount accumulates into stats");
        // Main-thread store was NOT used.
        assertEquals(0, store.getMember("writes").getArraySize());
    }

    @Test
    void workerOnErrorTriggersMainThreadFallbackOnNextCapture() {
        Value workerCtor = js.eval("js", """
                (function () {
                    function StubWorker() {
                        this.posts = []; this.terminated = false;
                        this.onmessage = null; this.onerror = null;
                    }
                    StubWorker.prototype.postMessage = function (m) { this.posts.push(m); };
                    StubWorker.prototype.terminate   = function () { this.terminated = true; };
                    StubWorker.prototype.fail = function (err) {
                        if (this.onerror) this.onerror(err);
                    };
                    globalThis._lastStubWorker = null;
                    return function () {
                        const w = new StubWorker();
                        globalThis._lastStubWorker = w;
                        return w;
                    };
                })()""");
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        recorder.putMember("_lastSeq", 7);
        Value ctrl = freshServiceWithWorker(factory, js.eval("js", "makeManualTimer()"),
                                            js.eval("js", "makeStubWindow()"), workerCtor)
                .invokeMember("attach", attachOpts(recorder, "({ s: 1 })"));

        // Simulate worker erroring out.
        Value worker = js.getBindings("js").getMember("_lastStubWorker");
        worker.invokeMember("fail", js.eval("js", "({ message: 'worker oom' })"));

        // Next capture should go via main-thread.
        await(ctrl.invokeMember("captureNow", "after-error"));
        assertEquals(1, store.getMember("writes").getArraySize(),
                "main-thread store used after worker.onerror");
        Value snap = ctrl.invokeMember("inspect");
        assertEquals(false, snap.getMember("useWorker").asBoolean());
        assertEquals(1, snap.getMember("mainThreadWrites").asInt());
    }

    @Test
    void captureStateNullSkipsWrite() {
        Value store    = js.eval("js", "makeStubStore()");
        Value factory  = js.eval("js", "(function (s) { return function () { return s; }; })").execute(store);
        Value recorder = js.eval("js", "makeStubRecorder()");
        Value ctrl = freshService(factory, js.eval("js", "makeManualTimer()"),
                                  js.eval("js", "makeStubWindow()"))
                .invokeMember("attach", attachOpts(recorder, "null"));
        await(ctrl.invokeMember("captureNow", "x"));
        assertEquals(0, store.getMember("writes").getArraySize());
    }
}
