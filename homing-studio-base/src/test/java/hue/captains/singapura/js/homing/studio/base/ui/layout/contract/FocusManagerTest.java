package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0049 — behavioural test for the per-tab {@code FocusManager}. Drives the
 * real class against a minimal DOM that models {@code inert}, focus, event
 * listeners + bubbling dispatch — enough to exercise the immutable-side
 * mechanics (enter / release / reconcile, activation-function precedence, and
 * the two widget-originated events) without a browser.
 */
class FocusManagerTest {

    private Context js;

    /** Minimal event-capable DOM + the loaded FocusManager class. */
    private static final String SCAFFOLD = """
            function makeEl(name) {
                return {
                    name: name, _children: [], parent: null, _listeners: {}, _attrs: {}, inert: false,
                    setAttribute: function (k, v) { this._attrs[k] = v; },
                    hasAttribute: function (k) { return Object.prototype.hasOwnProperty.call(this._attrs, k); },
                    appendChild: function (c) { c.parent = this; this._children.push(c); return c; },
                    contains: function (n) {
                        if (n === this) return true;
                        for (var i = 0; i < this._children.length; i++) {
                            var c = this._children[i]; if (c === n || c.contains(n)) return true;
                        }
                        return false;
                    },
                    focus: function () { document.activeElement = this; },
                    blur:  function () { if (document.activeElement === this) document.activeElement = document.body; },
                    addEventListener: function (t, fn) { (this._listeners[t] = this._listeners[t] || []).push(fn); },
                    removeEventListener: function (t, fn) {
                        var a = this._listeners[t]; if (!a) return; var i = a.indexOf(fn); if (i >= 0) a.splice(i, 1);
                    },
                    dispatchEvent: function (ev) {
                        ev.target = ev.target || this; var node = this;
                        while (node) {
                            var a = node._listeners[ev.type];
                            if (a) { var cp = a.slice(); for (var i = 0; i < cp.length; i++) { if (ev._stopped) break; cp[i].call(node, ev); } }
                            if (ev._stopped) break; node = node.parent;
                        }
                        return !ev._prevented;
                    }
                };
            }
            function makeEvent(type, props) {
                var ev = { type: type, _stopped: false, _prevented: false,
                           stopPropagation: function () { this._stopped = true; },
                           preventDefault:  function () { this._prevented = true; } };
                if (props) for (var k in props) if (Object.prototype.hasOwnProperty.call(props, k)) ev[k] = props[k];
                return ev;
            }
            globalThis.document = {};
            document.body = makeEl('body');
            document.activeElement = document.body;
            // Issue 0003 drift-watch stub — records instances; tests fire the
            // callback manually to simulate a subtree mutation batch.
            globalThis.MutationObserver = function (cb) {
                this._cb = cb; this._target = null;
                this.observe    = function (t, o) { this._target = t; };
                this.disconnect = function ()     { this._target = null; };
                MutationObserver._instances.push(this);
            };
            MutationObserver._instances = [];

            // Build a tab: content with a child input, a spy tab descriptor.
            function scenario(tabOpts) {
                document.activeElement = document.body;
                var content = makeEl('content');
                var input = makeEl('input'); content.appendChild(input);
                var log = { active: [], gaveUp: 0, intended: [] };
                var tab = {
                    setActive: function (on) { log.active.push(on); },
                    defaultActivation: tabOpts && tabOpts.defaultActivation
                        ? function () { input.focus(); log.defaultRan = true; } : undefined
                };
                var fm = new FocusManager(content, {
                    tab: tab,
                    onGiveUp: function () { log.gaveUp++; },
                    onIntendedFocus: function (fn) { log.intended.push(fn); }
                }).attach();
                return { content: content, input: input, fm: fm, tab: tab, log: log };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false).option("js.ecmascript-version", "2022").build();
        eval(readJs("/homing/js/hue/captains/singapura/js/homing/studio/base/ui/layout/FocusManagerModule.js"));
        eval(SCAFFOLD);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void contentStartsInertAndFocusable() {
        assertTrue(evalBool("var s = scenario(); s.content.inert === true && s.content._attrs.tabindex === '-1';"),
                "content must start inert and be programmatically focusable");
    }

    @Test
    void enterUnInertsSetsActiveAndFocusesDefault() {
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.enter();
                s.content.inert === false
                    && s.log.active[0] === true
                    && document.activeElement === s.content   // default landing
                    && s.fm.isDeep() === true;
                """), "enter: un-inert + setActive(true) + focus the content by default");
    }

    @Test
    void enterRunsPerRequestActivationFunction() {
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.enter(function () { s.input.focus(); });
                document.activeElement === s.input;   // request fn wins
                """), "enter(fn): the per-request activation function places focus");
    }

    @Test
    void enterUsesTabDefaultWhenNoRequest() {
        assertTrue(evalBool("""
                var s = scenario({ defaultActivation: true });
                s.fm.enter();
                s.log.defaultRan === true && document.activeElement === s.input;
                """), "enter(): the tab's registered default activation runs when no request fn");
    }

    @Test
    void releaseInertsBlursAndDeactivates() {
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.enter(function () { s.input.focus(); });
                s.fm.release();
                s.content.inert === true
                    && document.activeElement === document.body
                    && s.log.active[s.log.active.length - 1] === false
                    && s.fm.isDeep() === false;
                """), "release: re-inert + blur + setActive(false)");
    }

    @Test
    void reconcileRestoresFocusThatDrifted() {
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.enter(function () { s.input.focus(); });
                document.activeElement = document.body;   // a re-render dropped focus
                s.fm.reconcile();
                document.activeElement === s.content;
                """), "reconcile: deep + focus drifted to body → restore into the tab");
    }

    @Test
    void reconcileNoopWhenNotDeepOrFocusInside() {
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.reconcile();                                   // not deep → no-op
                var a1 = document.activeElement === document.body;
                s.fm.enter(function () { s.input.focus(); });
                s.fm.reconcile();                                   // focus already inside → no move
                var a2 = document.activeElement === s.input;
                a1 && a2;
                """), "reconcile is a no-op when not deep, or when focus is already inside");
    }

    @Test
    void giveUpFiresOnlyWhenDeep() {
        assertTrue(evalBool("""
                var s = scenario();
                s.input.dispatchEvent(makeEvent('keydown', { key: 'Escape' }));  // not deep
                var before = s.log.gaveUp;
                s.fm.enter();
                s.input.dispatchEvent(makeEvent('keydown', { key: 'Escape' }));  // deep → give up
                before === 0 && s.log.gaveUp === 1;
                """), "give-up (un-consumed Escape) fires onGiveUp only while deep");
    }

    @Test
    void driftWatchReconcilesAfterAMutationStrandsFocus() {
        // Issue 0003 — removing a focused node fires NO focus event; the
        // drift watch (a MutationObserver live only while deep) turns the
        // subtree change into a reconcile that pulls focus back.
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.enter(function () { s.input.focus(); });
                var mo = MutationObserver._instances[MutationObserver._instances.length - 1];
                var observing = mo._target === s.content;
                document.activeElement = document.body;   // a re-render dropped focus, silently
                mo._cb([]);                               // the mutation batch arrives
                observing && document.activeElement === s.content;
                """), "drift watch: a mutation while deep reconciles stranded focus back into the tab");
    }

    @Test
    void driftWatchStopsOnReleaseAndNoopsWhenFocusInside() {
        assertTrue(evalBool("""
                var s = scenario();
                s.fm.enter(function () { s.input.focus(); });
                var mo = MutationObserver._instances[MutationObserver._instances.length - 1];
                mo._cb([]);                               // focus still inside → no move
                var a1 = document.activeElement === s.input;
                s.fm.release();
                var a2 = mo._target === null;             // disconnected on release
                document.activeElement = document.body;
                mo._cb([]);                               // stale batch after release → no-op
                a1 && a2 && document.activeElement === document.body;
                """), "drift watch: no-op when focus is inside; disconnected + inert after release");
    }

    @Test
    void intendedFocusInDeliversTheActivationFunction() {
        assertTrue(evalBool("""
                var s = scenario();
                var fn = function () {};
                s.input.dispatchEvent(makeEvent('intendedFocusIn', { detail: { onGranted: fn } }));
                s.log.intended.length === 1 && s.log.intended[0] === fn;
                """), "intendedFocusIn delivers detail.onGranted to onIntendedFocus");
    }

    // ── helpers ────────────────────────────────────────────────────────────
    private void eval(String src) { js.eval("js", src); }
    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private String readJs(String path) {
        try (var in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, "missing classpath resource: " + path);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to read " + path, e); }
    }
}
