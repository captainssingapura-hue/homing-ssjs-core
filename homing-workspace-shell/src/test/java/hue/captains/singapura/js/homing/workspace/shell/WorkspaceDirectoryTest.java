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
 * Unit test for {@code WorkspaceDirectory}. Constructor-injected
 * collaborators (catalogue store + URL source + UUID gen + clock) make
 * every test a pure data exercise — no IndexedDB, no real
 * {@code URLSearchParams}, no real {@code Date.now}.
 *
 * <p>Each test builds a fresh directory with a stub catalogue store
 * recording its calls; assertions watch both the resolved identity
 * tuple and the catalogue-side effects.</p>
 */
class WorkspaceDirectoryTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceDirectoryModule.js";

    /**
     * Stub catalogue store. Mirrors the real
     * {@code WorkspaceCatalogueStore} API (lookupByUuid, lookupByName,
     * create, touch) and records each call so tests can verify what
     * the directory forwards. Fixture lookups are seeded per-test via
     * {@code _byUuid} / {@code _byName} maps.
     *
     * <p>Provide a default global instance so
     * {@code WorkspaceDirectory.INSTANCE} (built at module load) finds
     * the factory function even though instance tests use their own
     * stubs via constructor injection.</p>
     */
    private static final String STUBS = """
            class StubCatalogue {
                constructor() {
                    this._byUuid       = new Map();
                    this._byName       = new Map();
                    this.lookupByUuidCalls = [];
                    this.lookupByNameCalls = [];
                    this.createCalls       = [];
                    this.touchCalls        = [];
                }
                seedExisting(entry) {
                    this._byUuid.set(entry.kind + ':' + entry.id, entry);
                    this._byName.set(entry.kind + ':' + entry.name, entry);
                }
                lookupByUuid(kind, id) {
                    this.lookupByUuidCalls.push({ kind, id });
                    return Promise.resolve(this._byUuid.get(kind + ':' + id) || null);
                }
                lookupByName(kind, name) {
                    this.lookupByNameCalls.push({ kind, name });
                    return Promise.resolve(this._byName.get(kind + ':' + name) || null);
                }
                create(entry) {
                    this.createCalls.push(entry);
                    this.seedExisting(entry);
                    return Promise.resolve();
                }
                touch(kind, id) {
                    this.touchCalls.push({ kind, id });
                    return Promise.resolve();
                }
            }
            // Used only by INSTANCE construction at module-load; tests
            // build their own directories with injected stubs.
            globalThis.createWorkspaceCatalogueStore = function () { return new StubCatalogue(); };
            // Helpers shared across tests.
            // Minimal URLSearchParams polyfill — enough to back makeUrl.
            // GraalVM JS doesn't ship the WHATWG URL API.
            globalThis.URLSearchParams = class {
                constructor(search) {
                    this._map = new Map();
                    if (!search) return;
                    const s = search.startsWith('?') ? search.slice(1) : search;
                    if (!s) return;
                    for (const pair of s.split('&')) {
                        const eq = pair.indexOf('=');
                        if (eq < 0) this._map.set(decodeURIComponent(pair), '');
                        else this._map.set(decodeURIComponent(pair.slice(0, eq)),
                                           decodeURIComponent(pair.slice(eq + 1)));
                    }
                }
                get(name) { return this._map.has(name) ? this._map.get(name) : null; }
            };
            globalThis.makeUrl = function (search) {
                return function () { return new URLSearchParams(search); };
            };
            globalThis.fixedUuid = function (val) { return function () { return val; }; };
            globalThis.fixedClock = function (ms)  { return function () { return ms; }; };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Construct a directory with explicit deps; defaults to empty URL +
     *  fixed UUID/clock so tests are deterministic. */
    private Value freshDirectory(Value catalogueStore, String search,
                                 String newUuid, long clockMs) {
        Value deps = js.eval("js", "({})");
        deps.putMember("catalogueStore", catalogueStore);
        deps.putMember("urlSource",      js.eval("js", "makeUrl('" + search + "')"));
        deps.putMember("uuidGen",        js.eval("js", "fixedUuid('" + newUuid + "')"));
        deps.putMember("clock",          js.eval("js", "fixedClock(" + clockMs + ")"));
        return global("WorkspaceDirectory").newInstance(deps);
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
    void placeholderUuidIsDeterministicForKind() {
        Value dir = freshDirectory(js.eval("js", "new StubCatalogue()"),
                                   "", "fresh", 1_000L);
        String a = dir.invokeMember("placeholderUuidForKind", "AnimalsPlayground").asString();
        String b = dir.invokeMember("placeholderUuidForKind", "AnimalsPlayground").asString();
        String c = dir.invokeMember("placeholderUuidForKind", "OtherKind").asString();
        assertEquals(a, b, "same kind → same UUID");
        assertNotNull(a);
        assertTrue(a.matches("[0-9a-f]{8}-7000-5000-9000-[0-9a-f]{8}0001"),
                "UUID matches expected shape: " + a);
        assertTrue(!a.equals(c), "different kinds → different UUIDs");
    }

    @Test
    void noUrlHintReturnsDefaultIdentity() {
        Value store = js.eval("js", "new StubCatalogue()");
        Value dir   = freshDirectory(store, "", "fresh", 1_000L);

        Value identity = await(dir.invokeMember("resolveIdentity", "AnimalsPlayground"));

        assertNotNull(identity);
        assertEquals("default", identity.getMember("name").asString());
        assertEquals(true,      identity.getMember("isDefault").asBoolean());
        assertEquals(false,     identity.getMember("isNew").asBoolean());
        // Placeholder UUID shape.
        assertTrue(identity.getMember("id").asString()
                .matches("[0-9a-f]{8}-7000-5000-9000-[0-9a-f]{8}0001"));
        // No catalogue lookups when there's no URL hint.
        assertEquals(0, store.getMember("lookupByUuidCalls").getArraySize());
        assertEquals(0, store.getMember("lookupByNameCalls").getArraySize());
    }

    @Test
    void workspaceParamLooksUpByUuid() {
        Value store = js.eval("js", """
                (() => {
                    const s = new StubCatalogue();
                    s.seedExisting({ kind: 'AnimalsPlayground', id: 'abc-uuid',
                                     name: 'Mine', isDefault: false });
                    return s;
                })()""");
        Value dir = freshDirectory(store, "?workspace=abc-uuid", "fresh", 1_000L);

        Value identity = await(dir.invokeMember("resolveIdentity", "AnimalsPlayground"));
        assertEquals("abc-uuid", identity.getMember("id").asString());
        assertEquals("Mine",     identity.getMember("name").asString());
        assertEquals(false,      identity.getMember("isDefault").asBoolean());
        assertEquals(false,      identity.getMember("isNew").asBoolean());
        assertEquals(1, store.getMember("lookupByUuidCalls").getArraySize());
    }

    @Test
    void workspaceParamUnknownFallsBackToDefault() {
        Value store = js.eval("js", "new StubCatalogue()");   // empty
        Value dir = freshDirectory(store, "?workspace=ghost", "fresh", 1_000L);

        Value identity = await(dir.invokeMember("resolveIdentity", "AnimalsPlayground"));
        assertEquals("default", identity.getMember("name").asString());
        assertEquals(true,      identity.getMember("isDefault").asBoolean());
        assertEquals(false,     identity.getMember("isNew").asBoolean());
    }

    @Test
    void nameParamExistingReturnsThatEntry() {
        Value store = js.eval("js", """
                (() => {
                    const s = new StubCatalogue();
                    s.seedExisting({ kind: 'K', id: 'existing-uuid',
                                     name: 'Shared', isDefault: false });
                    return s;
                })()""");
        Value dir = freshDirectory(store, "?name=Shared", "wont-use", 1_000L);
        Value identity = await(dir.invokeMember("resolveIdentity", "K"));
        assertEquals("existing-uuid", identity.getMember("id").asString());
        assertEquals("Shared",        identity.getMember("name").asString());
        assertEquals(false,           identity.getMember("isNew").asBoolean());
    }

    @Test
    void nameParamMissingMintsNewUuidIsNewTrue() {
        Value store = js.eval("js", "new StubCatalogue()");
        Value dir = freshDirectory(store, "?name=Fresh", "minted-uuid-42", 1_000L);

        Value identity = await(dir.invokeMember("resolveIdentity", "K"));
        assertEquals("minted-uuid-42", identity.getMember("id").asString());
        assertEquals("Fresh",          identity.getMember("name").asString());
        assertEquals(false,            identity.getMember("isDefault").asBoolean());
        assertEquals(true,             identity.getMember("isNew").asBoolean());
        // Note: resolveIdentity does NOT register. That's the caller's job
        // (the orchestrator chains registerInCatalogue after resolve).
        assertEquals(0, store.getMember("createCalls").getArraySize());
    }

    @Test
    void registerInCatalogueIsNewCreatesEntry() {
        Value store = js.eval("js", "new StubCatalogue()");
        Value dir = freshDirectory(store, "", "x", 7_000L);
        Value identity = js.eval("js", """
                ({ id: 'i-1', name: 'Just Made', isDefault: false, isNew: true })""");

        await(dir.invokeMember("registerInCatalogue", "K", identity));

        Value calls = store.getMember("createCalls");
        assertEquals(1, calls.getArraySize());
        Value c = calls.getArrayElement(0);
        assertEquals("K",         c.getMember("kind").asString());
        assertEquals("i-1",       c.getMember("id").asString());
        assertEquals("Just Made", c.getMember("name").asString());
        assertEquals(7_000L,      c.getMember("createdAt").asLong());
        assertEquals(7_000L,      c.getMember("lastOpenedAt").asLong());
        // isNew rows are never default — directory enforces that.
        assertEquals(false,       c.getMember("isDefault").asBoolean());
        // No touch — pure create path.
        assertEquals(0, store.getMember("touchCalls").getArraySize());
    }

    @Test
    void registerInCatalogueExistingTouches() {
        Value store = js.eval("js", """
                (() => {
                    const s = new StubCatalogue();
                    s.seedExisting({ kind: 'K', id: 'i-1', name: 'Mine',
                                     isDefault: false });
                    return s;
                })()""");
        Value dir = freshDirectory(store, "", "x", 7_000L);
        Value identity = js.eval("js", """
                ({ id: 'i-1', name: 'Mine', isDefault: false, isNew: false })""");

        await(dir.invokeMember("registerInCatalogue", "K", identity));

        assertEquals(0, store.getMember("createCalls").getArraySize());
        assertEquals(1, store.getMember("touchCalls").getArraySize());
        assertEquals("i-1", store.getMember("touchCalls").getArrayElement(0)
                                .getMember("id").asString());
    }

    @Test
    void registerInCatalogueMissingDefaultCreatesPreservingFlag() {
        // First-boot default workspace: existing=null, isNew=false,
        // isDefault=true → create row with isDefault preserved.
        Value store = js.eval("js", "new StubCatalogue()");
        Value dir = freshDirectory(store, "", "x", 8_000L);
        Value identity = js.eval("js", """
                ({ id: 'def-uuid', name: 'default', isDefault: true, isNew: false })""");

        await(dir.invokeMember("registerInCatalogue", "K", identity));

        Value calls = store.getMember("createCalls");
        assertEquals(1, calls.getArraySize());
        assertEquals(true, calls.getArrayElement(0).getMember("isDefault").asBoolean(),
                "isDefault=true preserved when first-booting the default workspace");
    }

    @Test
    void noCatalogueStoreShortCircuitsAllResolutionToDefault() {
        // Explicit null catalogueStore — directory still functions.
        Value dir = freshDirectory(js.eval("js", "null"),
                                   "?workspace=anything&name=AnyName",
                                   "x", 1_000L);
        Value identity = await(dir.invokeMember("resolveIdentity", "K"));
        assertEquals("default", identity.getMember("name").asString());
        assertEquals(true,      identity.getMember("isDefault").asBoolean());
    }

    @Test
    void noCatalogueStoreRegisterIsNoOp() {
        Value dir = freshDirectory(js.eval("js", "null"), "", "x", 1_000L);
        // Must resolve cleanly (no throw).
        Value out = await(dir.invokeMember("registerInCatalogue", "K",
                js.eval("js", "({ id: 'i', name: 'n', isDefault: false, isNew: true })")));
        // Resolved to undefined; the assertion is "no exception".
        assertTrue(out == null || out.isNull(), "register short-circuits to no-op");
    }
}
