package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0064 — the preference steward, evaluated as a raw script against a
 * shimmed {@code window} (a Map-backed localStorage) and a shimmed
 * {@code HrefManagerInstance.current()} standing in for the address.
 *
 * <p>Pins the one order of resolution — address, store, fallback — that the
 * address never writes the store, that the view cannot write, that a change
 * notification carries nothing, and that a steward without storage still
 * resolves.</p>
 */
class PreferenceStewardTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/server/PreferenceSteward.js";

    /** The browser the module expects: localStorage and the address, both fakeable. */
    private static final String SHIMS = """
            globalThis.__pageUrl = "/app?app=x";
            globalThis.HrefManagerInstance = { current: function () { return globalThis.__pageUrl; } };
            globalThis.__mem = new Map();
            globalThis.window = {
                localStorage: {
                    getItem:    function (k)    { return globalThis.__mem.has(k) ? globalThis.__mem.get(k) : null; },
                    setItem:    function (k, v) { globalThis.__mem.set(k, String(v)); },
                    removeItem: function (k)    { globalThis.__mem.delete(k); }
                },
                addEventListener: function (type, fn) { globalThis.__storageListener = fn; }
            };
            globalThis.console = { error: function () {} };
            // Enough of URLSearchParams for override(): parse and get.
            globalThis.URLSearchParams = class {
                constructor(s) { this._m = new Map(); String(s || "").split("&").filter(Boolean).forEach(p => {
                    const i = p.indexOf("="); this._m.set(decodeURIComponent(i < 0 ? p : p.slice(0, i)), decodeURIComponent(i < 0 ? "" : p.slice(i + 1))); }); }
                get(k) { return this._m.has(k) ? this._m.get(k) : null; }
            };
            """;

    private Value view;
    private Value steward;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
        loadModule(MODULE);
        view    = global("PreferenceViewInstance");
        steward = global("PreferenceStewardInstance");
    }

    private void address(String url) { js.eval("js", "globalThis.__pageUrl = " + quote(url) + ";"); }
    private static String quote(String s) { return "\"" + s.replace("\"", "\\\"") + "\""; }

    // ── Resolution order ──────────────────────────────────────────────────────

    @Test
    void nothingStoredAndSilentAddress_resolvesToTheFallback() {
        assertEquals("default", view.invokeMember("resolve", "theme", "default").asString());
        assertTrue(view.invokeMember("resolve", "theme").isNull(), "no fallback → null");
    }

    @Test
    void aStoredPick_beatsTheFallback() {
        steward.invokeMember("remember", "theme", "forest");
        assertEquals("forest", view.invokeMember("resolve", "theme", "default").asString());
        assertEquals("forest", view.invokeMember("preferred", "theme").asString());
    }

    @Test
    void theAddress_beatsTheStore_andNeverWritesIt() {
        steward.invokeMember("remember", "theme", "forest");
        address("/app?app=x&theme=carbon");
        assertEquals("carbon", view.invokeMember("override", "theme").asString());
        assertEquals("carbon", view.invokeMember("resolve", "theme", "default").asString());
        assertEquals("forest", view.invokeMember("preferred", "theme").asString(),
                "following an address is viewing, not choosing");
    }

    @Test
    void anEmptyAddressValue_isSilence() {
        address("/app?app=x&theme=");
        assertTrue(view.invokeMember("override", "theme").isNull());
    }

    @Test
    void preferencesAreIndependentByName() {
        steward.invokeMember("remember", "theme", "forest");
        assertNull(view.invokeMember("preferred", "locale").asString());
        assertEquals("forest", js.eval("js", "globalThis.__mem.get('homing.theme')").asString(),
                "stored under the documented, inspectable name");
    }

    // ── Writing ───────────────────────────────────────────────────────────────

    @Test
    void forget_andRememberingNothing_bothClear() {
        steward.invokeMember("remember", "theme", "forest");
        steward.invokeMember("forget", "theme");
        assertTrue(view.invokeMember("preferred", "theme").isNull());

        steward.invokeMember("remember", "theme", "forest");
        steward.invokeMember("remember", "theme", "");
        assertTrue(view.invokeMember("preferred", "theme").isNull());
    }

    @Test
    void theViewCannotWrite() {
        assertFalse(view.hasMember("remember"));
        assertFalse(view.hasMember("forget"));
        assertTrue(js.eval("js", "Object.isFrozen(PreferenceViewInstance)").asBoolean());
        assertTrue(js.eval("js", "Object.isFrozen(PreferenceStewardInstance)").asBoolean());
    }

    // ── Notification ──────────────────────────────────────────────────────────

    @Test
    void aChangeNotifies_withNoDetails_andUnsubscribeStops() {
        js.eval("js", """
                globalThis.__calls = [];
                globalThis.__off = PreferenceViewInstance.onChange(function () {
                    globalThis.__calls.push(arguments.length);
                });
                """);
        steward.invokeMember("remember", "theme", "forest");
        steward.invokeMember("forget", "theme");
        assertEquals(2, js.eval("js", "globalThis.__calls.length").asInt());
        assertEquals(0, js.eval("js", "globalThis.__calls[0]").asInt(), "a notification carries nothing");

        js.eval("js", "globalThis.__off();");
        steward.invokeMember("remember", "theme", "carbon");
        assertEquals(2, js.eval("js", "globalThis.__calls.length").asInt(), "unsubscribed");
    }

    @Test
    void anotherTabsPick_arrivesAsAStorageEvent_onlyForOurKeys() {
        js.eval("js", "globalThis.__n = 0; PreferenceViewInstance.onChange(function () { globalThis.__n++; });");
        js.eval("js", "globalThis.__storageListener({ key: 'homing.theme' });");
        js.eval("js", "globalThis.__storageListener({ key: 'somebody-elses' });");
        js.eval("js", "globalThis.__storageListener({ key: null });");
        assertEquals(1, js.eval("js", "globalThis.__n").asInt());
    }

    @Test
    void aFailingListener_doesNotStopTheOthers() {
        js.eval("js", """
                globalThis.__heard = false;
                PreferenceViewInstance.onChange(function () { throw new Error("boom"); });
                PreferenceViewInstance.onChange(function () { globalThis.__heard = true; });
                """);
        steward.invokeMember("remember", "theme", "forest");
        assertTrue(js.eval("js", "globalThis.__heard").asBoolean());
    }

    // ── Resilience ────────────────────────────────────────────────────────────

    @Test
    void withoutStorage_theStewardStillResolves_andStillNotifies() {
        js.eval("js", """
                Object.defineProperty(globalThis.window, "localStorage", {
                    get: function () { throw new Error("site data blocked"); }
                });
                globalThis.__n = 0; PreferenceViewInstance.onChange(function () { globalThis.__n++; });
                """);
        steward.invokeMember("remember", "theme", "forest");          // does not throw
        assertTrue(view.invokeMember("preferred", "theme").isNull());
        assertEquals("default", view.invokeMember("resolve", "theme", "default").asString());
        address("/app?app=x&theme=carbon");
        assertEquals("carbon", view.invokeMember("resolve", "theme", "default").asString());
        assertEquals(1, js.eval("js", "globalThis.__n").asInt());
    }

    @Test
    void aBadName_isRefused() {
        assertTrue(js.eval("js", """
                (function () { try { PreferenceViewInstance.preferred(""); return false; } catch (e) { return e instanceof TypeError; } })()
                """).asBoolean());
    }
}
