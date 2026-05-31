package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.events.CheckpointWorkerModule;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0034 P4 — Structural + behavioural conformance test for the
 * checkpoint worker JS module.
 *
 * <p>Loads {@code CheckpointWorkerModule.js} into a GraalVM Polyglot
 * context with a minimal in-memory IDB mock and asserts the worker's
 * message contract end-to-end:</p>
 *
 * <ol>
 *   <li>Worker registers a {@code self.onmessage} handler at load time.</li>
 *   <li>Message with {@code prune: false} writes one checkpoint row and
 *       replies with {@code prunedCount: null}.</li>
 *   <li>Message with {@code prune: true} writes the checkpoint and
 *       range-deletes events with {@code seq <= lastEventSeq}, replying
 *       with the deleted count.</li>
 *   <li>Prune scope is limited to the workspace identity — events
 *       belonging to other {@code (kind, workspaceId)} pairs are
 *       untouched.</li>
 *   <li>Garbled messages (missing {@code reqId}) generate no
 *       {@code postMessage} reply.</li>
 *   <li>Failed checkpoint writes reply with {@code ok: false}.</li>
 *   <li>Failed prunes (post-successful-write) still reply with
 *       {@code ok: true} and an absent {@code prunedCount} — the
 *       checkpoint commit is the authoritative success signal.</li>
 * </ol>
 *
 * <p>This is the Diligent Primitives discipline applied to the worker
 * substrate: every state-affecting code path is exercised at build
 * time, against the same contract the production main thread relies on.
 * RFC 0033's UI-headless harness is not required — Polyglot + an in-
 * memory IDB mock is sufficient because the worker has no DOM coupling.</p>
 *
 * @since RFC 0034 P4 — checkpoint worker conformance
 */
class CheckpointWorkerConformanceTest {

    private Context js;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js")
                .allowAllAccess(true)   // needed for Java host interop in mock
                .option("js.ecmascript-version", "2022")
                .build();
        installMockEnvironment();
        loadWorker();
    }

    @AfterEach
    void teardown() {
        if (js != null) js.close();
    }

    // ─── Structural tests ────────────────────────────────────────────────

    @Test
    void workerRegistersOnMessageHandlerAtLoad() {
        assertTrue(js.eval("js", "typeof self.onmessage === 'function'").asBoolean(),
                "Worker must register self.onmessage at load");
    }

    // ─── Behavioural tests — checkpoint write ────────────────────────────

    @Test
    void writeOnlyReplyHasOkTrueAndNullPrunedCount() {
        Value reply = sendMessageAndAwaitReply("""
                {
                    reqId:        42,
                    kind:         'TestKind',
                    workspaceId:  'ws-A',
                    state:        { hello: 'world' },
                    lastEventSeq: 100,
                    prune:        false
                }
                """);
        assertEquals(42,   reply.getMember("reqId").asInt());
        assertTrue (       reply.getMember("ok").asBoolean());
        assertEquals(100,  reply.getMember("storedSeq").asInt());
        assertTrue (       reply.getMember("prunedCount").isNull(),
                "prunedCount should be null when prune=false");
    }

    @Test
    void writeOnlyPersistsOneCheckpointRow() {
        sendMessageAndAwaitReply("""
                {
                    reqId:        1,
                    kind:         'TestKind',
                    workspaceId:  'ws-A',
                    state:        { tag: 'first' },
                    lastEventSeq: 7,
                    prune:        false
                }
                """);
        int rowCount = js.eval("js", "__mockCheckpointStore.size").asInt();
        assertEquals(1, rowCount);
        assertEquals(7, js.eval("js",
                "__mockCheckpointStore.get('TestKind|ws-A').lastEventSeq").asInt());
    }

    @Test
    void rewriteSameKeyReplacesRowNotAppends() {
        sendMessageAndAwaitReply("{reqId:1,kind:'K',workspaceId:'W',state:{},lastEventSeq:5,prune:false}");
        sendMessageAndAwaitReply("{reqId:2,kind:'K',workspaceId:'W',state:{},lastEventSeq:9,prune:false}");
        assertEquals(1, js.eval("js", "__mockCheckpointStore.size").asInt(),
                "Same (kind,workspaceId) key replaces, not appends");
        assertEquals(9, js.eval("js",
                "__mockCheckpointStore.get('K|W').lastEventSeq").asInt());
    }

    // ─── Behavioural tests — prune ───────────────────────────────────────

    @Test
    void pruneTrueDeletesEventsUpToLastEventSeq() {
        seedEvents("K", "W", new int[] { 1, 2, 3, 4, 5 });
        Value reply = sendMessageAndAwaitReply("""
                {
                    reqId:        10,
                    kind:         'K',
                    workspaceId:  'W',
                    state:        {},
                    lastEventSeq: 3,
                    prune:        true
                }
                """);
        assertTrue (reply.getMember("ok").asBoolean());
        assertEquals(3, reply.getMember("prunedCount").asInt(),
                "Three events (seq 1,2,3) should be pruned");
        assertEquals(2, js.eval("js", "__mockEvents.length").asInt(),
                "Two events (seq 4,5) should remain");
    }

    @Test
    void pruneScopeIsLimitedToWorkspaceIdentity() {
        seedEvents("K", "W",     new int[] { 1, 2, 3 });
        seedEvents("K", "OTHER", new int[] { 1, 2, 3 });
        seedEvents("OTHER", "W", new int[] { 1, 2 });
        Value reply = sendMessageAndAwaitReply("""
                {
                    reqId:        20,
                    kind:         'K',
                    workspaceId:  'W',
                    state:        {},
                    lastEventSeq: 100,
                    prune:        true
                }
                """);
        assertEquals(3, reply.getMember("prunedCount").asInt(),
                "Only the K/W rows should be pruned");
        int remaining = js.eval("js", "__mockEvents.length").asInt();
        assertEquals(5, remaining,
                "K/OTHER (3 rows) + OTHER/W (2 rows) should remain untouched");
    }

    @Test
    void pruneOnEmptyStoreReturnsZero() {
        Value reply = sendMessageAndAwaitReply("""
                {
                    reqId:        30,
                    kind:         'K',
                    workspaceId:  'W',
                    state:        {},
                    lastEventSeq: 50,
                    prune:        true
                }
                """);
        assertTrue (reply.getMember("ok").asBoolean());
        assertEquals(0, reply.getMember("prunedCount").asInt());
    }

    // ─── Behavioural tests — error paths ─────────────────────────────────

    @Test
    void garbledMessageWithoutReqIdGeneratesNoReply() {
        js.eval("js", """
                __postedMessages.length = 0;
                self.onmessage({ data: { kind: 'K', workspaceId: 'W' } });
                """);
        waitForReplies(50);
        assertEquals(0, js.eval("js", "__postedMessages.length").asInt(),
                "Worker must not reply to messages missing reqId");
    }

    @Test
    void failedCheckpointWriteRepliesWithOkFalse() {
        js.eval("js", "__mockFailCheckpointPut = true;");
        Value reply = sendMessageAndAwaitReply(
                "{reqId:40,kind:'K',workspaceId:'W',state:{},lastEventSeq:1,prune:false}");
        assertEquals(40, reply.getMember("reqId").asInt());
        assertFalse(reply.getMember("ok").asBoolean());
        assertTrue (reply.hasMember("error"), "Failed write must include error message");
    }

    @Test
    void failedPruneStillRepliesOkTrueForCheckpoint() {
        seedEvents("K", "W", new int[] { 1, 2 });
        js.eval("js", "__mockFailPruneTx = true;");
        Value reply = sendMessageAndAwaitReply(
                "{reqId:50,kind:'K',workspaceId:'W',state:{},lastEventSeq:10,prune:true}");
        assertTrue (reply.getMember("ok").asBoolean(),
                "Prune failure must not mask successful checkpoint commit");
        // prunedCount stays null when prune throws (post-write).
        assertTrue (reply.getMember("prunedCount").isNull(),
                "Failed prune leaves prunedCount null");
    }

    /** Reference the Java DomModule class so dependency wiring is visible. */
    @SuppressWarnings("unused")
    private static final Class<?>[] CONTRACT_REFERENCES = {
            CheckpointWorkerModule.class
    };

    // ─── Helpers ─────────────────────────────────────────────────────────

    private void loadWorker() {
        js.eval("js", readJs(
                "/homing/js/hue/captains/singapura/js/homing/workspace/events/CheckpointWorkerModule.js"));
    }

    private Value sendMessageAndAwaitReply(String dataJsLiteral) {
        js.eval("js", "__postedMessages.length = 0;");
        js.eval("js", "self.onmessage({ data: " + dataJsLiteral + " });");
        waitForReplies(200);
        Value posted = js.eval("js", "__postedMessages");
        if (posted.getArraySize() == 0) {
            fail("Worker did not post a reply within timeout");
        }
        return posted.getArrayElement(0);
    }

    private void waitForReplies(long maxMs) {
        // The worker uses async/await + Promises; JS microtasks run via
        // Polyglot's event loop. Yield to let them complete.
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            int len = js.eval("js", "__postedMessages.length").asInt();
            if (len > 0) return;
            try { Thread.sleep(2); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); return;
            }
        }
    }

    private void seedEvents(String kind, String workspaceId, int[] seqs) {
        var sb = new StringBuilder();
        for (int seq : seqs) {
            sb.append("__mockEvents.push({ kind: '")
              .append(kind).append("', workspaceId: '")
              .append(workspaceId).append("', seq: ").append(seq).append(" });\n");
        }
        js.eval("js", sb.toString());
    }

    private String readJs(String classpathPath) {
        try (var in = getClass().getResourceAsStream(classpathPath)) {
            assertNotNull(in, "missing classpath resource: " + classpathPath);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read " + classpathPath, e);
        }
    }

    /**
     * Install the worker's "self" global, console, and a minimal in-memory
     * IDB sufficient for the worker's operations:
     * <ul>
     *   <li>One checkpoint store with composite [kind, workspaceId] key.</li>
     *   <li>One events store with the by_workspace index returning
     *       cursors over a kind/workspaceId-scoped range.</li>
     * </ul>
     * Test-only injection points ({@code __mockFailCheckpointPut},
     * {@code __mockFailPruneTx}) flip simulated failure paths.
     */
    private void installMockEnvironment() {
        js.eval("js", """
                // ── Posted-message capture ───────────────────────────────
                var __postedMessages = [];
                var self = {
                    onmessage: null,
                    postMessage: function (msg) { __postedMessages.push(msg); }
                };

                // ── Console (Polyglot has one; alias for explicitness) ──
                if (typeof console === 'undefined') {
                    console = { log: function () {}, warn: function () {} };
                }

                // ── Failure-injection flags ──────────────────────────────
                var __mockFailCheckpointPut = false;
                var __mockFailPruneTx       = false;

                // ── Mock IDBKeyRange ─────────────────────────────────────
                var IDBKeyRange = {
                    bound: function (lo, hi, loOpen, hiOpen) {
                        return { lo: lo, hi: hi, loOpen: !!loOpen, hiOpen: !!hiOpen };
                    }
                };

                // ── Mock checkpoint store: Map<'kind|workspaceId', row> ──
                var __mockCheckpointStore = new Map();

                // ── Mock events list: [{kind, workspaceId, seq}, ...] ───
                var __mockEvents = [];

                // ── Mock IDB: open() dispatches by DB name ──────────────
                var indexedDB = {
                    open: function (dbName, version) {
                        var req = {};
                        // Resolve async via setTimeout-shaped queueing.
                        Promise.resolve().then(function () {
                            try {
                                var db = (dbName === 'homing.checkpoints')
                                    ? __mockCheckpointDb()
                                    : __mockEventDb();
                                if (req.onupgradeneeded) {
                                    req.onupgradeneeded({ target: { result: db } });
                                }
                                if (req.onsuccess) {
                                    req.onsuccess({ target: { result: db } });
                                }
                            } catch (e) {
                                if (req.onerror) req.onerror({ target: { error: e } });
                            }
                        });
                        return req;
                    }
                };

                function __mockCheckpointDb() {
                    return {
                        objectStoreNames: { contains: function (n) { return n === 'checkpoints'; } },
                        createObjectStore: function () {},
                        transaction: function (name, mode) {
                            var tx = { _committed: false, oncomplete: null, onerror: null, onabort: null };
                            // Defer completion to a microtask so handlers can attach.
                            Promise.resolve().then(function () {
                                tx._committed = true;
                                if (tx.oncomplete) tx.oncomplete();
                            });
                            return Object.assign(tx, {
                                objectStore: function () {
                                    return {
                                        put: function (row) {
                                            var req = {};
                                            Promise.resolve().then(function () {
                                                if (__mockFailCheckpointPut) {
                                                    if (req.onerror) {
                                                        req.error = new Error('mock checkpoint write failure');
                                                        req.onerror();
                                                    }
                                                } else {
                                                    var key = row.kind + '|' + row.workspaceId;
                                                    __mockCheckpointStore.set(key, row);
                                                    if (req.onsuccess) req.onsuccess();
                                                }
                                            });
                                            return req;
                                        }
                                    };
                                }
                            });
                        }
                    };
                }

                function __mockEventDb() {
                    return {
                        objectStoreNames: { contains: function (n) { return n === 'events'; } },
                        transaction: function (name, mode) {
                            var tx = { oncomplete: null, onerror: null, onabort: null };
                            return Object.assign(tx, {
                                objectStore: function () {
                                    return {
                                        index: function (idxName) {
                                            return {
                                                openCursor: function (range) {
                                                    var req = {};
                                                    var kind = range.lo[0];
                                                    var ws   = range.lo[1];
                                                    var hiSeq = range.hi[2];
                                                    // Pre-collect matching indices to delete
                                                    // (avoid mutating array while iterating).
                                                    var matches = [];
                                                    for (var i = 0; i < __mockEvents.length; i++) {
                                                        var e = __mockEvents[i];
                                                        if (e.kind === kind && e.workspaceId === ws && e.seq <= hiSeq) {
                                                            matches.push(i);
                                                        }
                                                    }
                                                    var pos = 0;
                                                    function step() {
                                                        if (pos >= matches.length) {
                                                            if (req.onsuccess) req.onsuccess({ target: { result: null } });
                                                            Promise.resolve().then(function () {
                                                                if (__mockFailPruneTx) {
                                                                    if (tx.onerror) {
                                                                        tx.error = new Error('mock prune tx abort');
                                                                        tx.onerror();
                                                                    }
                                                                } else if (tx.oncomplete) {
                                                                    tx.oncomplete();
                                                                }
                                                            });
                                                            return;
                                                        }
                                                        var cur = {
                                                            value: __mockEvents[matches[pos]],
                                                            _markedForDelete: matches[pos],
                                                            delete: function () { /* recorded below */ },
                                                            continue: function () { pos++; Promise.resolve().then(step); }
                                                        };
                                                        // Capture for actual deletion after cursor traversal.
                                                        cur.delete = function () { __mockEvents[matches[pos]] = null; };
                                                        if (req.onsuccess) req.onsuccess({ target: { result: cur } });
                                                    }
                                                    // Capture original tx.oncomplete so we can run
                                                    // compaction BEFORE the worker's oncomplete (which
                                                    // resolves the prune promise). Compaction must run
                                                    // after every cursor.delete() has fired — i.e., at
                                                    // tx.oncomplete time, not during step().
                                                    var origCompleteSetter;
                                                    Object.defineProperty(tx, 'oncomplete', {
                                                        get: function () { return tx._oncomplete; },
                                                        set: function (fn) {
                                                            tx._oncomplete = function () {
                                                                __mockEvents = __mockEvents.filter(function (e) { return e !== null; });
                                                                if (fn) fn();
                                                            };
                                                        }
                                                    });
                                                    Promise.resolve().then(step);
                                                    return req;
                                                }
                                            };
                                        }
                                    };
                                }
                            });
                        }
                    };
                }
                """);
    }
}
