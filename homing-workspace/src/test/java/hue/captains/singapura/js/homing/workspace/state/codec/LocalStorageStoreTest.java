package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.js.homing.workspace.state.WidgetLocation;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0029 Cycle 4 real-world test — the codec foundation drives real
 * persistence through {@code LocalStorageStore}.
 *
 * <p>Two layers of tests:</p>
 *
 * <ol>
 *   <li><b>LocalStorageStore alone</b> — save / load / clear /
 *       listSavedKinds against a JS mock that satisfies the
 *       localStorage interface (length, key, getItem, setItem,
 *       removeItem, clear). Validates the store's contract
 *       independent of any codec.</li>
 *   <li><b>End-to-end with WidgetLocation codec</b> — typed
 *       {@code WidgetLocation.InPane} → encoded via {@code WidgetLocationCodec}
 *       → saved through {@code LocalStorageStore} → loaded back →
 *       decoded via the same codec → equivalent to the original.
 *       The real "real-world" claim: codec + store compose cleanly,
 *       no friction at the seam.</li>
 * </ol>
 *
 * <p>The mock storage is a small JS object — keeps the test hermetic
 * and reproducible without depending on browser globals.</p>
 */
class LocalStorageStoreTest {

    private static final String STORE_JS_PATH =
            "/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/LocalStorageStore.js";

    /** Mock storage satisfying the localStorage interface. Hermetic, no Java host access. */
    private static final String MOCK_STORAGE_JS = """
            const mockStorage = {
                _data: new Map(),
                get length() { return this._data.size; },
                key(i) {
                    const keys = Array.from(this._data.keys());
                    return i >= 0 && i < keys.length ? keys[i] : null;
                },
                getItem(k) {
                    return this._data.has(k) ? this._data.get(k) : null;
                },
                setItem(k, v) {
                    this._data.set(String(k), String(v));
                },
                removeItem(k) {
                    this._data.delete(String(k));
                },
                clear() {
                    this._data.clear();
                }
            };
            """;

    private Context js;

    @BeforeEach
    void setup() throws IOException {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
        js.eval(Source.newBuilder("js", readResource(STORE_JS_PATH), "LocalStorageStore.js")
                .buildLiteral());
        js.eval("js", MOCK_STORAGE_JS);
        js.eval("js", "const store = createLocalStorageStore(mockStorage);");
    }

    @AfterEach
    void teardown() {
        if (js != null) {
            js.close();
            js = null;
        }
    }

    // ── LocalStorageStore alone ─────────────────────────────────────────────

    @Test
    void saveAndLoadRoundTripPlainObject() {
        var result = js.eval("js", """
                store.save("AnimalsPlayground", { theme: "default", fullscreen: false });
                const loaded = store.load("AnimalsPlayground");
                ({
                    theme:      loaded.theme,
                    fullscreen: loaded.fullscreen
                })
                """);
        assertEquals("default", result.getMember("theme").asString());
        assertFalse(result.getMember("fullscreen").asBoolean());
    }

    @Test
    void loadOfUnknownKindReturnsNull() {
        var result = js.eval("js", "store.load('ghost')");
        assertTrue(result.isNull(), "unknown workspaceKind should return null, not throw");
    }

    @Test
    void clearRemovesSavedState() {
        var result = js.eval("js", """
                store.save("Foo", { x: 1 });
                store.clear("Foo");
                store.load("Foo")
                """);
        assertTrue(result.isNull(), "load after clear should return null");
    }

    @Test
    void clearIsIdempotent() {
        // Clearing a never-saved key is a no-op, no error.
        assertDoesNotThrow(() -> js.eval("js", "store.clear('never-saved');"));
    }

    @Test
    void listSavedKindsReturnsOnlyPrefixedKeys() {
        var result = js.eval("js", """
                store.save("KindA", { v: 1 });
                store.save("KindB", { v: 2 });
                // Some other tenant's data on the same storage
                mockStorage.setItem("unrelated.key", "outsider");
                JSON.stringify(store.listSavedKinds().sort())
                """);
        assertEquals("[\"KindA\",\"KindB\"]", result.asString(),
                "listSavedKinds should expose only workspace state keys, not unrelated storage entries");
    }

    @Test
    void overwritePreservesLastWrite() {
        var result = js.eval("js", """
                store.save("WS", { v: 1 });
                store.save("WS", { v: 2 });
                store.load("WS").v
                """);
        assertEquals(2, result.asInt(), "subsequent save should overwrite prior wire form");
    }

    @Test
    void saveRejectsBlankWorkspaceKind() {
        assertTrue(evalThrows("store.save('', { v: 1 });"),
                "blank workspaceKind should be rejected");
        assertTrue(evalThrows("store.save(null, { v: 1 });"),
                "null workspaceKind should be rejected");
    }

    @Test
    void saveRejectsUndefinedWire() {
        assertTrue(evalThrows("store.save('K', undefined);"),
                "undefined wire form should fail loudly (caller bug)");
    }

    @Test
    void isRemoteIsFalse() {
        var result = js.eval("js", "store.isRemote()");
        assertFalse(result.asBoolean(),
                "framework LocalStorageStore must report isRemote = false (State Belongs to the User)");
    }

    @Test
    void factoryRejectsInvalidStorage() {
        assertTrue(evalThrows("createLocalStorageStore(null);"),
                "factory must reject null storage backing");
        assertTrue(evalThrows("createLocalStorageStore({ foo: 1 });"),
                "factory must reject objects missing the localStorage interface");
    }

    // ── End-to-end through WidgetLocation codec ─────────────────────────────

    @Test
    void widgetLocationRoundTripsThroughCodecAndStore() {
        // Load the codecs (composition: WidgetLocation depends on PaneId)
        js.eval("js", PaneIdJsDefinition.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", PaneIdJsFunctions.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", WidgetLocationJsDefinition.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));
        js.eval("js", WidgetLocationJsFunctions.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));

        // The real-world scenario: typed value → codec encode → store save
        // → store load → codec decode → typed value. Assert structural
        // equivalence across the full round-trip.
        var result = js.eval("js", """
                const original = new WidgetLocation.InPane(new PaneId("p1"), 5, true);

                // Encode → save
                const wire = WidgetLocationCodec.transformTo(original);
                store.save("TestWorkspace.location", wire);

                // Load → decode
                const loadedWire = store.load("TestWorkspace.location");
                const restored   = WidgetLocationCodec.transformFrom(loadedWire);

                ({
                    paneIdEqual:    restored.paneId.value === original.paneId.value,
                    tabIndexEqual:  restored.tabIndex === original.tabIndex,
                    isActiveEqual:  restored.isActive === original.isActive,
                    isInPane:       restored instanceof WidgetLocation.InPane,
                    paneIdTyped:    restored.paneId instanceof PaneId
                })
                """);

        assertTrue(result.getMember("paneIdEqual").asBoolean(),
                "round-trip should preserve PaneId.value");
        assertTrue(result.getMember("tabIndexEqual").asBoolean(),
                "round-trip should preserve tabIndex");
        assertTrue(result.getMember("isActiveEqual").asBoolean(),
                "round-trip should preserve isActive");
        assertTrue(result.getMember("isInPane").asBoolean(),
                "round-trip should preserve variant identity (InPane)");
        assertTrue(result.getMember("paneIdTyped").asBoolean(),
                "round-trip should preserve typed identifier (PaneId, not raw string)");
    }

    @Test
    void modalVariantSurvivesStoreRoundTrip() {
        js.eval("js", PaneIdJsDefinition.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", PaneIdJsFunctions.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", WidgetLocationJsDefinition.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));
        js.eval("js", WidgetLocationJsFunctions.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));

        var result = js.eval("js", """
                const original = new WidgetLocation.InModal();
                store.save("TestWorkspace.location", WidgetLocationCodec.transformTo(original));
                const restored = WidgetLocationCodec.transformFrom(store.load("TestWorkspace.location"));
                ({
                    isInModal: restored instanceof WidgetLocation.InModal,
                    notInPane: !(restored instanceof WidgetLocation.InPane)
                })
                """);
        assertTrue(result.getMember("isInModal").asBoolean(),
                "InModal variant identity must survive the round-trip");
        assertTrue(result.getMember("notInPane").asBoolean());
    }

    @Test
    void multipleWorkspacesCoexistInStore() {
        js.eval("js", PaneIdJsDefinition.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", PaneIdJsFunctions.INSTANCE.generate(ObjectDefinition.of(PaneId.class)));
        js.eval("js", WidgetLocationJsDefinition.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));
        js.eval("js", WidgetLocationJsFunctions.INSTANCE.generate(ObjectDefinition.of(WidgetLocation.class)));

        var result = js.eval("js", """
                // Two different workspaces, two different states, same store
                store.save("WsA", WidgetLocationCodec.transformTo(
                    new WidgetLocation.InPane(new PaneId("a"), 0, true)));
                store.save("WsB", WidgetLocationCodec.transformTo(
                    new WidgetLocation.InPane(new PaneId("b"), 1, false)));

                const restoredA = WidgetLocationCodec.transformFrom(store.load("WsA"));
                const restoredB = WidgetLocationCodec.transformFrom(store.load("WsB"));
                ({
                    aPaneId:    restoredA.paneId.value,
                    bPaneId:    restoredB.paneId.value,
                    aTabIndex:  restoredA.tabIndex,
                    bTabIndex:  restoredB.tabIndex,
                    kindsCount: store.listSavedKinds().length
                })
                """);

        assertEquals("a", result.getMember("aPaneId").asString());
        assertEquals("b", result.getMember("bPaneId").asString());
        assertEquals(0, result.getMember("aTabIndex").asInt());
        assertEquals(1, result.getMember("bTabIndex").asInt());
        assertEquals(2, result.getMember("kindsCount").asInt(),
                "both workspaces should be enumerable via listSavedKinds");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean evalThrows(String snippet) {
        try {
            js.eval("js", snippet);
            return false;
        } catch (PolyglotException e) {
            return e.isGuestException();
        }
    }

    private String readResource(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) throw new IOException("classpath resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
