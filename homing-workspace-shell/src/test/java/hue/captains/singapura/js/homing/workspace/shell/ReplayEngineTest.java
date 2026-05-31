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
 * Unit test for {@code ReplayEngine} — fold-style virtual replay.
 * Engine reads checkpoint + log, folds events into a caller-supplied
 * state, returns finalState + summary. No DOM, no handlers, no halt-ctx.
 */
class ReplayEngineTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/ReplayEngineModule.js";

    private static final String STUBS = """
            globalThis.makeRecorder = function () {
                return {
                    replayingCalls: [],
                    setReplaying(b) { this.replayingCalls.push(!!b); }
                };
            };
            globalThis.makeLog = function (rows) {
                return {
                    queryCalls: 0,
                    query() { this.queryCalls++; return Promise.resolve(rows || []); }
                };
            };
            globalThis.makeFailingLog = function (msg) {
                return {
                    query() { return Promise.reject(new Error(msg || 'idb down')); }
                };
            };
            globalThis.makeCpStore = function (row) {
                return {
                    readCalls: [],
                    read(kind, id) {
                        this.readCalls.push({ kind, id });
                        return Promise.resolve(row || null);
                    }
                };
            };
            // Simple counter-style state for fold testing.
            globalThis.freshCounter = function () { return { events: [], counter: 0 }; };
            globalThis.counterFold = function (state, event) {
                state.events.push(event.name);
                state.counter++;
                return state;
            };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    private Value freshEngine() {
        return global("ReplayEngine").newInstance();
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

    private Value makeBootOpts(Value log, Value cpStore, Value recorder) {
        Value opts = js.eval("js", """
                ({ workspaceKind: 'K', workspaceId: 'i-1',
                   initialState: freshCounter,
                   fold:         counterFold })""");
        opts.putMember("eventLog",        log);
        opts.putMember("checkpointStore", cpStore);
        opts.putMember("recorder",        recorder);
        return opts;
    }

    @Test
    void emptyLogAndNoCheckpointInvokesOnEmpty() {
        Value log      = js.eval("js", "makeLog([])");
        Value recorder = js.eval("js", "makeRecorder()");
        Value opts = makeBootOpts(log, js.eval("js", "null"), recorder);
        opts.putMember("onEmpty",
                js.eval("js", "(function (state) { state.emptyFired = true; return state; })"));

        Value summary = await(freshEngine().invokeMember("boot", opts));

        assertEquals(0,     summary.getMember("replayed").asInt());
        assertEquals(true,  summary.getMember("fellThroughToEmpty").asBoolean());
        assertEquals(false, summary.getMember("restoredFromCheckpoint").asBoolean());
        assertEquals(true,  summary.getMember("finalState").getMember("emptyFired").asBoolean());
        // Fence latched ON; caller drops it after projection (engine doesn't).
        Value rcalls = recorder.getMember("replayingCalls");
        assertEquals(1,    rcalls.getArraySize());
        assertEquals(true, rcalls.getArrayElement(0).asBoolean());
    }

    @Test
    void eventsAreFoldedIntoStateInOrder() {
        Value log = js.eval("js", """
                makeLog([
                    { seq: 1, name: 'A', payload: {} },
                    { seq: 2, name: 'B', payload: {} },
                    { seq: 3, name: 'C', payload: {} }
                ])""");
        Value recorder = js.eval("js", "makeRecorder()");
        Value summary = await(freshEngine().invokeMember("boot",
                makeBootOpts(log, js.eval("js", "null"), recorder)));
        Value state = summary.getMember("finalState");
        assertEquals(3, state.getMember("counter").asInt());
        assertEquals(3, summary.getMember("replayed").asInt());
        assertEquals(3, summary.getMember("lastSeq").asInt());
        Value events = state.getMember("events");
        assertEquals("A", events.getArrayElement(0).asString());
        assertEquals("B", events.getArrayElement(1).asString());
        assertEquals("C", events.getArrayElement(2).asString());
    }

    @Test
    void checkpointSeedsStateAndOnlyAboveCutoffEventsFold() {
        Value log = js.eval("js", """
                makeLog([
                    { seq: 1, name: 'A', payload: {} },
                    { seq: 2, name: 'B', payload: {} },
                    { seq: 3, name: 'C', payload: {} },
                    { seq: 4, name: 'D', payload: {} }
                ])""");
        Value cpStore = js.eval("js", "makeCpStore({ lastEventSeq: 2, state: { restored: true } })");
        Value recorder = js.eval("js", "makeRecorder()");

        Value opts = makeBootOpts(log, cpStore, recorder);
        opts.putMember("decodeCheckpoint",
                js.eval("js", "(function (row) { return { events: ['from-cp'], counter: 99, ...row.state }; })"));

        Value summary = await(freshEngine().invokeMember("boot", opts));

        assertEquals(true, summary.getMember("restoredFromCheckpoint").asBoolean());
        assertEquals(false, summary.getMember("fellThroughToEmpty").asBoolean());
        // Only seq > 2 events folded.
        assertEquals(2, summary.getMember("replayed").asInt());
        assertEquals(4, summary.getMember("lastSeq").asInt());
        // State seeded from checkpoint and folded over.
        Value state = summary.getMember("finalState");
        assertEquals(true, state.getMember("restored").asBoolean());
        assertEquals(101,  state.getMember("counter").asInt(),
                "99 from checkpoint + 2 folded events");
    }

    @Test
    void checkpointPresentEmptyTailReturnsCheckpointState() {
        Value log = js.eval("js", "makeLog([{ seq: 1, name: 'X', payload: {} }])");
        Value cpStore = js.eval("js", "makeCpStore({ lastEventSeq: 5, state: { ok: true } })");
        Value recorder = js.eval("js", "makeRecorder()");

        Value opts = makeBootOpts(log, cpStore, recorder);
        opts.putMember("decodeCheckpoint",
                js.eval("js", "(function (row) { return { events: [], counter: 0, ...row.state }; })"));

        Value summary = await(freshEngine().invokeMember("boot", opts));

        assertEquals(0, summary.getMember("replayed").asInt());
        assertEquals(true,  summary.getMember("restoredFromCheckpoint").asBoolean());
        assertEquals(false, summary.getMember("fellThroughToEmpty").asBoolean());
        assertEquals(true,  summary.getMember("finalState").getMember("ok").asBoolean());
    }

    @Test
    void foldThrowsAreAbsorbedAndOtherEventsContinue() {
        Value log = js.eval("js", """
                makeLog([
                    { seq: 1, name: 'A', payload: {} },
                    { seq: 2, name: 'BOOM', payload: {} },
                    { seq: 3, name: 'C', payload: {} }
                ])""");
        Value recorder = js.eval("js", "makeRecorder()");
        Value opts = makeBootOpts(log, js.eval("js", "null"), recorder);
        opts.putMember("fold",
                js.eval("js", """
                        (function (state, event) {
                            if (event.name === 'BOOM') throw new Error('handler-fail');
                            state.events.push(event.name);
                            state.counter++;
                            return state;
                        })"""));

        Value summary = await(freshEngine().invokeMember("boot", opts));
        assertEquals(3, summary.getMember("replayed").asInt(),
                "all events counted even when fold throws on one");
        assertEquals(2, summary.getMember("finalState").getMember("counter").asInt(),
                "fold throw on BOOM doesn't disturb the other two");
    }

    @Test
    void onProgressFiresPerEvent() {
        Value log = js.eval("js", """
                makeLog([
                    { seq: 1, name: 'A', payload: {} },
                    { seq: 2, name: 'B', payload: {} }
                ])""");
        Value recorder = js.eval("js", "makeRecorder()");
        Value calls = js.eval("js", "({ progress: [] })");
        Value opts = makeBootOpts(log, js.eval("js", "null"), recorder);
        opts.putMember("onProgress",
                js.eval("js", "(function (o) { return function (s) { o.progress.push(s.idx + '/' + s.total); }; })")
                        .execute(calls));

        await(freshEngine().invokeMember("boot", opts));

        Value progress = calls.getMember("progress");
        assertEquals(2, progress.getArraySize());
        assertEquals("1/2", progress.getArrayElement(0).asString());
        assertEquals("2/2", progress.getArrayElement(1).asString());
    }

    @Test
    void logQueryFailureFallsBackToEmptyOnEmpty() {
        Value log      = js.eval("js", "makeFailingLog('idb down')");
        Value recorder = js.eval("js", "makeRecorder()");
        Value calls = js.eval("js", "({ called: 0 })");
        Value opts = makeBootOpts(log, js.eval("js", "null"), recorder);
        opts.putMember("onEmpty",
                js.eval("js", "(function (o) { return function (state) { o.called++; return state; }; })")
                        .execute(calls));

        Value summary = await(freshEngine().invokeMember("boot", opts));

        assertEquals(0, summary.getMember("replayed").asInt());
        assertEquals(1, calls.getMember("called").asInt(),
                "query failure → empty queue → onEmpty fires");
        assertEquals(true, summary.getMember("fellThroughToEmpty").asBoolean());
    }

    @Test
    void noEventLogAndNoCheckpointInvokesOnEmptyWithInitialState() {
        Value recorder = js.eval("js", "makeRecorder()");
        Value calls = js.eval("js", "({ initialFired: 0, emptyFired: 0 })");
        Value opts = js.eval("js", "({ workspaceKind: 'K', workspaceId: 'i-1' })");
        opts.putMember("recorder",     recorder);
        opts.putMember("fold",         js.eval("js", "(function (s, _e) { return s; })"));
        opts.putMember("initialState", js.eval("js",
                "(function (o) { return function () { o.initialFired++; return { v: 'fresh' }; }; })")
                .execute(calls));
        opts.putMember("onEmpty",      js.eval("js",
                "(function (o) { return function (s) { o.emptyFired++; return s; }; })")
                .execute(calls));

        Value summary = await(freshEngine().invokeMember("boot", opts));
        assertNotNull(summary);
        assertEquals(0,   summary.getMember("replayed").asInt());
        assertEquals(1,   calls.getMember("initialFired").asInt());
        assertEquals(1,   calls.getMember("emptyFired").asInt());
        assertEquals("fresh", summary.getMember("finalState").getMember("v").asString());
    }

    @Test
    void decodeCheckpointReturningNullFallsBackToInitialState() {
        Value cpStore = js.eval("js", "makeCpStore({ lastEventSeq: 5, state: {} })");
        Value recorder = js.eval("js", "makeRecorder()");
        Value opts = makeBootOpts(js.eval("js", "makeLog([])"), cpStore, recorder);
        opts.putMember("decodeCheckpoint",
                js.eval("js", "(function (_row) { return null; })"));

        Value summary = await(freshEngine().invokeMember("boot", opts));
        // Decode returning null → treated as no checkpoint, onEmpty path,
        // initialState seeds, since no events and decode-null = no cp.
        assertEquals(false, summary.getMember("restoredFromCheckpoint").asBoolean());
        assertEquals(true,  summary.getMember("fellThroughToEmpty").asBoolean());
    }

    @Test
    void engineRequiresFoldAndInitialStateFunctions() {
        Value recorder = js.eval("js", "makeRecorder()");
        Value optsNoFold = js.eval("js",
                "({ workspaceKind: 'K', workspaceId: 'i-1', initialState: freshCounter })");
        optsNoFold.putMember("recorder", recorder);
        try {
            freshEngine().invokeMember("boot", optsNoFold);
            org.junit.jupiter.api.Assertions.fail("expected throw on missing fold");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("fold"),
                    "error mentions fold: " + e.getMessage());
        }
    }
}
