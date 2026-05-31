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
 * Unit test for {@code WriteLockGuard} + {@code ReadOnlyOverlay}.
 * Constructor-injected {@code navigator.locks} stub lets each test
 * stage the lock outcome deterministically (acquired immediately,
 * missed on ifAvailable, take-over success, stolen mid-session).
 *
 * <p>Assertions watch three side channels:</p>
 *
 * <ul>
 *   <li>{@code recorder.calls} — every {@code setWriteLockHeld(b)}
 *       invocation by the guard, in order.</li>
 *   <li>{@code host.children} — overlay mount/unmount → mask + banner
 *       DOM presence.</li>
 *   <li>{@code onChangeLog} — passthrough hook fires with (held, reason).</li>
 * </ul>
 */
class WriteLockGuardTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WriteLockGuardModule.js";

    private static final String STUBS = """
            // Minimal recorder — captures setWriteLockHeld(b) calls.
            globalThis.makeRecorder = function () {
                return {
                    calls: [],
                    setWriteLockHeld(b) { this.calls.push(b); }
                };
            };
            // Minimal host element with appendChild/removeChild semantics.
            globalThis.makeHost = function () {
                return {
                    children: [],
                    appendChild(c) { this.children.push(c); c.parentNode = this; },
                    removeChild(c) {
                        this.children = this.children.filter(x => x !== c);
                        c.parentNode = null;
                    }
                };
            };
            // Stub document — just enough for ReadOnlyOverlay's mask+banner.
            globalThis.stubDocument = {
                createElement(tag) {
                    return {
                        tag,
                        children: [],
                        style: { cssText: '', flex: '' },
                        textContent: '',
                        attrs: {},
                        listeners: [],
                        parentNode: null,
                        disabled: false,
                        setAttribute(k, v) { this.attrs[k] = v; },
                        appendChild(c) { this.children.push(c); c.parentNode = this; },
                        addEventListener(t, fn, cap) { this.listeners.push({ t, fn, cap }); }
                    };
                },
                createTextNode(t) { return { tag: '#text', textContent: t }; }
            };
            // Stub navigator.locks — staged response controls request() outcome.
            //   stage('available')  → request returns a held lock object (success)
            //   stage('unavailable')→ request(ifAvailable) returns null (miss)
            //   stage('error', msg) → request rejects with new Error(msg)
            // Held locks remember their stealCallback so tests can simulate
            // the steal path mid-session.
            globalThis.makeLocksApi = function () {
                return {
                    requests: [],
                    _next:    'available',
                    _heldRelease: null,    // resolves the held promise (release)
                    _heldReject:  null,    // rejects the held promise (steal)
                    stage(kind, extra) { this._next = { kind, extra }; },
                    request(name, opts, callback) {
                        this.requests.push({ name, opts });
                        const staged = (typeof this._next === 'string')
                                     ? { kind: this._next }
                                     : this._next;
                        // Reset to 'available' for subsequent calls unless explicitly restaged.
                        this._next = 'available';
                        if (staged.kind === 'unavailable') {
                            return Promise.resolve(callback(null));
                        }
                        if (staged.kind === 'error') {
                            return Promise.reject(new Error(staged.extra || 'stub'));
                        }
                        // 'available' — invoke callback synchronously with a fake lock,
                        // then chain whatever the callback returned (a pending promise).
                        const self = this;
                        const held = new Promise(function (res, rej) {
                            self._heldRelease = res;
                            self._heldReject  = rej;
                        });
                        const ret = callback({ name });
                        // The guard returns a pending Promise from its callback to
                        // keep the lock held; we attach `held` to it so steal can
                        // surface as a rejection on the outer request promise.
                        if (ret && typeof ret.then === 'function') {
                            return held;     // override; lets tests reject (steal) it
                        }
                        return Promise.resolve();
                    },
                    simulateSteal(msg) {
                        if (this._heldReject) {
                            const err = new Error(msg || 'AbortError');
                            err.name  = 'AbortError';
                            this._heldReject(err);
                        }
                    }
                };
            };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
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

    /** Fresh guard with the given stub locks API + stub document injected. */
    private Value freshGuard(Value locksApi) {
        Value deps = js.eval("js", "({})");
        deps.putMember("navigatorLocks", locksApi);
        // Inject a stub document into the overlay class via OverlayCtor wrapper.
        Value overlayCtor = js.eval("js", """
                (function () {
                    return function OverlayWithStubDoc() {
                        return new ReadOnlyOverlay({ document: stubDocument });
                    };
                })()""");
        deps.putMember("OverlayCtor", overlayCtor);
        return global("WriteLockGuard").newInstance(deps);
    }

    private Value attachOpts(Value host, Value recorder, Value onChangeLog) {
        Value opts = js.eval("js", """
                ({ lockName: 'lk', workspaceName: 'Mine' })""");
        opts.putMember("host",     host);
        opts.putMember("recorder", recorder);
        // onChange callback pushes (held, reason) into onChangeLog.calls.
        Value cb = js.eval("js", """
                (function (log) {
                    return function (held, reason) {
                        log.calls.push({ held, reason });
                    };
                })""").execute(onChangeLog);
        opts.putMember("onChange", cb);
        return opts;
    }

    private Value newLog() { return js.eval("js", "({ calls: [] })"); }

    @Test
    void lockAcquiredAtBootFlipsRecorderTrueNoOverlay() {
        Value locks    = js.eval("js", "makeLocksApi()");
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();

        Value ctrl = await(freshGuard(locks).invokeMember("attach",
                attachOpts(host, recorder, log)));

        assertEquals(true, ctrl.invokeMember("isHeld").asBoolean());
        // Recorder flipped to true exactly once.
        Value calls = recorder.getMember("calls");
        assertEquals(1, calls.getArraySize());
        assertEquals(true, calls.getArrayElement(0).asBoolean());
        // No overlay mounted.
        assertEquals(0, host.getMember("children").getArraySize());
        // onChange fired with (true, 'ifAvailable').
        Value lc = log.getMember("calls");
        assertEquals(1, lc.getArraySize());
        assertEquals(true, lc.getArrayElement(0).getMember("held").asBoolean());
    }

    @Test
    void unavailableAtBootFlipsFalseAndMountsOverlay() {
        Value locks    = js.eval("js", "(function () { const a = makeLocksApi(); a.stage('unavailable'); return a; })()");
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();

        Value ctrl = await(freshGuard(locks).invokeMember("attach",
                attachOpts(host, recorder, log)));

        assertEquals(false, ctrl.invokeMember("isHeld").asBoolean());
        Value calls = recorder.getMember("calls");
        assertEquals(1, calls.getArraySize());
        assertEquals(false, calls.getArrayElement(0).asBoolean());
        // Mask + banner mounted (two children under host).
        assertEquals(2, host.getMember("children").getArraySize());
        assertEquals("unavailable-at-boot",
                log.getMember("calls").getArrayElement(0).getMember("reason").asString());
    }

    @Test
    void takeOverFromReadOnlyFlipsTrueAndUnmountsOverlay() {
        Value locks    = js.eval("js", "(function () { const a = makeLocksApi(); a.stage('unavailable'); return a; })()");
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();

        Value ctrl = await(freshGuard(locks).invokeMember("attach",
                attachOpts(host, recorder, log)));

        // Initial state: read-only.
        assertEquals(false, ctrl.invokeMember("isHeld").asBoolean());
        assertEquals(2, host.getMember("children").getArraySize());

        // takeOver — next request returns a held lock (default staged 'available').
        Value got = await(ctrl.invokeMember("takeOver"));
        assertEquals(true, got.asBoolean());
        assertEquals(true, ctrl.invokeMember("isHeld").asBoolean());
        // Overlay unmounted.
        assertEquals(0, host.getMember("children").getArraySize());
        // Recorder calls: [false, true].
        Value calls = recorder.getMember("calls");
        assertEquals(2, calls.getArraySize());
        assertEquals(false, calls.getArrayElement(0).asBoolean());
        assertEquals(true,  calls.getArrayElement(1).asBoolean());
        // Second request was a steal.
        Value reqs = locks.getMember("requests");
        assertEquals(2, reqs.getArraySize());
        assertEquals(true,
                reqs.getArrayElement(1).getMember("opts").getMember("steal").asBoolean());
    }

    @Test
    void noLocksApiDegradesToWriterModeSilently() {
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();

        Value ctrl = await(freshGuard(js.eval("js", "null"))
                .invokeMember("attach", attachOpts(host, recorder, log)));

        assertEquals(true, ctrl.invokeMember("isHeld").asBoolean());
        assertEquals(1, recorder.getMember("calls").getArraySize());
        assertEquals(true, recorder.getMember("calls").getArrayElement(0).asBoolean());
        assertEquals("no-locks-api",
                log.getMember("calls").getArrayElement(0).getMember("reason").asString());
    }

    @Test
    void stolenMidSessionFlipsToReadOnlyAndMountsOverlay() {
        Value locks    = js.eval("js", "makeLocksApi()");
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();

        Value ctrl = await(freshGuard(locks).invokeMember("attach",
                attachOpts(host, recorder, log)));
        assertEquals(true, ctrl.invokeMember("isHeld").asBoolean());

        // Now simulate a sibling tab stealing the lock.
        locks.invokeMember("simulateSteal", "AbortError");
        // Drain one microtask turn — easiest by awaiting a known Promise.
        await(js.eval("js", "Promise.resolve()"));

        assertEquals(false, ctrl.invokeMember("isHeld").asBoolean());
        assertEquals(2, host.getMember("children").getArraySize(),
                "overlay mounted after steal");
        Value calls = recorder.getMember("calls");
        assertEquals(2, calls.getArraySize());
        assertEquals(true,  calls.getArrayElement(0).asBoolean());
        assertEquals(false, calls.getArrayElement(1).asBoolean());
        // Last reason: 'stolen'.
        Value lc = log.getMember("calls");
        assertEquals("stolen",
                lc.getArrayElement(lc.getArraySize() - 1).getMember("reason").asString());
    }

    @Test
    void releaseFlipsToFalseAndMountsOverlay() {
        Value locks    = js.eval("js", "makeLocksApi()");
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();

        Value ctrl = await(freshGuard(locks).invokeMember("attach",
                attachOpts(host, recorder, log)));
        ctrl.invokeMember("release");

        assertEquals(false, ctrl.invokeMember("isHeld").asBoolean());
        assertEquals(2, host.getMember("children").getArraySize());
        assertTrue(recorder.getMember("calls").getArraySize() >= 2);
    }

    @Test
    void overlayMountIsIdempotent() {
        // Two consecutive unavailable transitions must not double-mount.
        // Easiest path: degrade-to-writer-no-locks-api scenario, then
        // manually call applyHeld(false) twice via release+release.
        Value locks    = js.eval("js", "(function () { const a = makeLocksApi(); a.stage('unavailable'); return a; })()");
        Value host     = js.eval("js", "makeHost()");
        Value recorder = js.eval("js", "makeRecorder()");
        Value log      = newLog();
        Value ctrl = await(freshGuard(locks).invokeMember("attach",
                attachOpts(host, recorder, log)));
        ctrl.invokeMember("release");   // already false; should be no double-mount

        assertEquals(false, ctrl.invokeMember("isHeld").asBoolean());
        assertEquals(2, host.getMember("children").getArraySize(),
                "still exactly mask+banner — no double-mount");
    }
}
