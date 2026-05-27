package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.*;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0029 — the saving pipeline end-to-end. Demonstrates that
 * {@code triggerSave}/{@code forceSave} from the persister correctly
 * captures the live workspace view, encodes through the codec stack,
 * and persists via {@code LocalStorageStore}.
 *
 * <p>A test scheduler stands in for {@code setTimeout}/{@code clearTimeout}
 * (which aren't part of hermetic ECMAScript). The mock workspace view
 * is a small JS object whose four accessors return constructed typed
 * values — the same shape the actual workspace shell will implement
 * at integration time.</p>
 */
class PersistenceE2ETest {

    private Context js;

    @BeforeEach
    void setup() throws IOException {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();

        // Codec stack (same order as WorkspaceStateE2ETest)
        loadCodecPair(PaneIdJsDefinition.INSTANCE,           PaneIdJsFunctions.INSTANCE,           PaneId.class);
        loadCodecPair(WidgetInstanceIdJsDefinition.INSTANCE, WidgetInstanceIdJsFunctions.INSTANCE, WidgetInstanceId.class);
        loadCodecPair(WidgetKindJsDefinition.INSTANCE,       WidgetKindJsFunctions.INSTANCE,       WidgetKind.class);
        loadCodecPair(WorkspaceKindJsDefinition.INSTANCE,    WorkspaceKindJsFunctions.INSTANCE,    WorkspaceKind.class);
        loadCodecPair(WidgetTitleJsDefinition.INSTANCE,      WidgetTitleJsFunctions.INSTANCE,      WidgetTitle.class);
        loadCodecPair(ThemeNameJsDefinition.INSTANCE,        ThemeNameJsFunctions.INSTANCE,        ThemeName.class);
        loadCodecPair(OrientationJsDefinition.INSTANCE,      OrientationJsFunctions.INSTANCE,      Orientation.class);
        loadCodecPair(WidgetLocationJsDefinition.INSTANCE,   WidgetLocationJsFunctions.INSTANCE,   WidgetLocation.class);
        loadCodecPair(LayoutNodeJsDefinition.INSTANCE,       LayoutNodeJsFunctions.INSTANCE,       LayoutNode.class);
        loadCodecPair(ChromeStateJsDefinition.INSTANCE,      ChromeStateJsFunctions.INSTANCE,      ChromeState.class);

        loadResource("/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/WidgetParamsCodecRegistry.js");
        js.eval("js", """
                class TestPet {
                    constructor(name, age) {
                        this.name = name;
                        this.age = age;
                        Object.freeze(this);
                    }
                }
                WidgetParamsCodecRegistry.register('test-pet', {
                    transformTo(p)  { return { name: p.name, age: p.age }; },
                    transformFrom(w) { return new TestPet(w.name, w.age); }
                });
                """);

        loadCodecPair(WidgetInstanceJsDefinition.INSTANCE, WidgetInstanceJsFunctions.INSTANCE, WidgetInstance.class);
        loadCodecPair(WorkspaceStateJsDefinition.INSTANCE, WorkspaceStateJsFunctions.INSTANCE, WorkspaceState.class);

        // Persister + capture helpers + store
        loadResource("/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/LocalStorageStore.js");
        loadResource("/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/captureLiveWorkspace.js");
        loadResource("/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/WorkspaceStatePersister.js");

        // Test scheduler (manually advanceable) + mock storage + mock view
        js.eval("js", """
                // Test scheduler — pending callbacks live in an array; flush() runs them all.
                function createTestScheduler() {
                    const pending = [];
                    let nextTicket = 0;
                    return {
                        schedule(fn, delayMs) {
                            const ticket = ++nextTicket;
                            pending.push({ ticket, fn });
                            return ticket;
                        },
                        cancel(ticket) {
                            const idx = pending.findIndex(p => p.ticket === ticket);
                            if (idx >= 0) pending.splice(idx, 1);
                        },
                        flush() {
                            const toRun = pending.splice(0);
                            for (const { fn } of toRun) fn();
                        },
                        pendingCount() { return pending.length; }
                    };
                }
                const scheduler = createTestScheduler();

                // Mock localStorage
                const mockStorage = {
                    _data: new Map(),
                    get length() { return this._data.size; },
                    key(i) {
                        const keys = Array.from(this._data.keys());
                        return i >= 0 && i < keys.length ? keys[i] : null;
                    },
                    getItem(k)    { return this._data.has(k) ? this._data.get(k) : null; },
                    setItem(k, v) { this._data.set(String(k), String(v)); },
                    removeItem(k) { this._data.delete(String(k)); },
                    clear()       { this._data.clear(); }
                };
                const store = createLocalStorageStore(mockStorage);

                // Mock workspace view — minimal but realistic. The actual workspace
                // shell will implement the same four accessors at integration time.
                let liveAnimal = 'Whiskers';    // mutable: simulates a widget params change
                const widgetId = new WidgetInstanceId('11111111-2222-3333-4444-555555555555');
                const view = {
                    workspaceKind: () => new WorkspaceKind('AnimalsPlayground'),
                    layout:        () => new LayoutNode.Leaf(new PaneId('main')),
                    widgets:       () => [
                        new WidgetInstance(
                            widgetId,
                            new WidgetKind('test-pet'),
                            new TestPet(liveAnimal, 3),
                            new WidgetTitle(liveAnimal + ' (3yo)'),
                            new WidgetLocation.InPane(new PaneId('main'), 0, true))
                    ],
                    chrome:        () => new ChromeState(ThemeName.DEFAULT, false)
                };

                // The saveFn wires capture + encode + store.save together
                let saveCount = 0;
                const saveFn = () => {
                    const state = captureLiveWorkspace(view);
                    const wire  = WorkspaceStateCodec.transformTo(state);
                    store.save(state.workspaceKind.value, wire);
                    saveCount++;
                };

                const persister = createWorkspaceStatePersister({
                    saveFn,
                    scheduler,
                    debounceMs: 500
                });
                """);
    }

    @AfterEach
    void teardown() {
        if (js != null) {
            js.close();
            js = null;
        }
    }

    // ── Debouncing — bursts coalesce into one save ───────────────────────────

    @Test
    void triggerSaveDebouncesBurstsIntoOneSave() {
        var result = js.eval("js", """
                // Three triggers in quick succession (no scheduler.flush between)
                persister.triggerSave();
                persister.triggerSave();
                persister.triggerSave();

                const pendingBefore = scheduler.pendingCount();
                const saveCountBefore = saveCount;

                // The debounce window fires — only the last scheduled callback runs
                scheduler.flush();

                ({
                    pendingBefore:   pendingBefore,
                    saveCountBefore: saveCountBefore,
                    saveCountAfter:  saveCount,
                    pendingAfter:    scheduler.pendingCount()
                })
                """);
        assertEquals(1, result.getMember("pendingBefore").asInt(),
                "three triggers should leave exactly one pending timer (debounced)");
        assertEquals(0, result.getMember("saveCountBefore").asInt(),
                "no save should have fired yet — debounce window hasn't elapsed");
        assertEquals(1, result.getMember("saveCountAfter").asInt(),
                "exactly one save should have fired when the window elapses");
        assertEquals(0, result.getMember("pendingAfter").asInt(),
                "no pending timers after flush");
    }

    @Test
    void isPendingTracksDebounceWindow() {
        var result = js.eval("js", """
                const beforeTrigger = persister.isPending();
                persister.triggerSave();
                const afterTrigger = persister.isPending();
                scheduler.flush();
                const afterFlush = persister.isPending();
                ({ beforeTrigger, afterTrigger, afterFlush })
                """);
        assertFalse(result.getMember("beforeTrigger").asBoolean());
        assertTrue(result.getMember("afterTrigger").asBoolean(),
                "isPending should be true while debounce timer is armed");
        assertFalse(result.getMember("afterFlush").asBoolean(),
                "isPending should be false after the save fires");
    }

    // ── Force Save — bypasses the debounce ───────────────────────────────────

    @Test
    void forceSaveBypassesDebounce() {
        var result = js.eval("js", """
                persister.triggerSave();
                const pendingBefore = scheduler.pendingCount();    // 1
                const saveBefore    = saveCount;                    // 0

                persister.forceSave();
                const pendingAfter = scheduler.pendingCount();      // 0 — pending cancelled
                const saveAfter    = saveCount;                     // 1 — fired immediately
                const stillPending = persister.isPending();         // false

                ({ pendingBefore, saveBefore, pendingAfter, saveAfter, stillPending })
                """);
        assertEquals(1, result.getMember("pendingBefore").asInt());
        assertEquals(0, result.getMember("saveBefore").asInt());
        assertEquals(0, result.getMember("pendingAfter").asInt(),
                "forceSave should cancel any pending debounced save");
        assertEquals(1, result.getMember("saveAfter").asInt(),
                "forceSave should fire the save synchronously");
        assertFalse(result.getMember("stillPending").asBoolean());
    }

    @Test
    void forceSaveWithoutPriorTriggerStillSaves() {
        var result = js.eval("js", """
                persister.forceSave();
                saveCount
                """);
        assertEquals(1, result.asInt(), "forceSave should work even with no prior triggerSave");
    }

    // ── End-to-end — the saved data is the live view's current state ────────

    @Test
    void savedWireMatchesLiveViewAtSaveMoment() {
        var result = js.eval("js", """
                // Live view shows Whiskers; trigger and flush
                persister.triggerSave();
                scheduler.flush();

                const wire1 = store.load('AnimalsPlayground');
                const widgetIdStr = '11111111-2222-3333-4444-555555555555';

                ({
                    workspaceKind: wire1.workspaceKind,
                    paneId:        wire1.layout.paneId,
                    widgetName:    wire1.widgetsById[widgetIdStr].params.name,
                    widgetTitle:   wire1.widgetsById[widgetIdStr].title,
                    theme:         wire1.chrome.theme,
                    fullscreen:    wire1.chrome.fullscreen
                })
                """);
        assertEquals("AnimalsPlayground", result.getMember("workspaceKind").asString());
        assertEquals("main",              result.getMember("paneId").asString());
        assertEquals("Whiskers",          result.getMember("widgetName").asString());
        assertEquals("Whiskers (3yo)",    result.getMember("widgetTitle").asString());
        assertEquals("default",           result.getMember("theme").asString());
        assertFalse(result.getMember("fullscreen").asBoolean());
    }

    @Test
    void liveViewMutationsReflectInSubsequentSaves() {
        // Simulates: user edits a widget's params → workspace shell calls triggerSave
        // → debounce fires → store has the updated value.
        var result = js.eval("js", """
                // First save with Whiskers
                persister.triggerSave();
                scheduler.flush();
                const wire1 = store.load('AnimalsPlayground');
                const widgetIdStr = '11111111-2222-3333-4444-555555555555';
                const firstName = wire1.widgetsById[widgetIdStr].params.name;

                // User edits the live view's animal
                liveAnimal = 'Rex';

                // The workspace shell calls triggerSave on the param-change event
                persister.triggerSave();
                scheduler.flush();
                const wire2 = store.load('AnimalsPlayground');
                const secondName = wire2.widgetsById[widgetIdStr].params.name;

                ({ firstName, secondName, totalSaves: saveCount })
                """);
        assertEquals("Whiskers", result.getMember("firstName").asString());
        assertEquals("Rex",      result.getMember("secondName").asString(),
                "second save should reflect the post-mutation live view");
        assertEquals(2, result.getMember("totalSaves").asInt(),
                "exactly two saves should have fired (one per debounce window)");
    }

    @Test
    void rapidEventsCoalesceButPreserveLatestState() {
        // Many triggers in a burst, ending after a live-view mutation,
        // should result in ONE save reflecting the final state.
        var result = js.eval("js", """
                liveAnimal = 'Alpha';
                persister.triggerSave();
                liveAnimal = 'Beta';
                persister.triggerSave();
                liveAnimal = 'Charlie';
                persister.triggerSave();
                liveAnimal = 'Delta';
                persister.triggerSave();

                const pendingBeforeFlush = scheduler.pendingCount();
                scheduler.flush();

                const wire = store.load('AnimalsPlayground');
                const widgetIdStr = '11111111-2222-3333-4444-555555555555';

                ({
                    pendingBeforeFlush: pendingBeforeFlush,
                    finalName:          wire.widgetsById[widgetIdStr].params.name,
                    totalSaves:         saveCount
                })
                """);
        assertEquals(1, result.getMember("pendingBeforeFlush").asInt(),
                "four triggers should collapse to one pending timer");
        assertEquals(1, result.getMember("totalSaves").asInt(),
                "exactly one save should fire for the burst");
        assertEquals("Delta", result.getMember("finalName").asString(),
                "the save should reflect the live view at the moment of saving — the LAST mutation, not an earlier one");
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void factoryRejectsInvalidOpts() {
        assertTrue(evalThrows("createWorkspaceStatePersister();"),
                "missing opts should fail");
        assertTrue(evalThrows("createWorkspaceStatePersister({});"),
                "missing saveFn should fail");
        assertTrue(evalThrows("createWorkspaceStatePersister({ saveFn: () => {} });"),
                "missing scheduler should fail");
        assertTrue(evalThrows("createWorkspaceStatePersister({ saveFn: () => {}, scheduler: {} });"),
                "scheduler without schedule()/cancel() should fail");
    }

    @Test
    void captureLiveWorkspaceValidatesViewShape() {
        assertTrue(evalThrows("captureLiveWorkspace();"),
                "missing view should fail");
        assertTrue(evalThrows("captureLiveWorkspace({});"),
                "view missing workspaceKind() should fail");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private <T> void loadCodecPair(
            hue.captains.singapura.js.homing.codec.DefinitionCodeGen defGen,
            hue.captains.singapura.js.homing.codec.FunctionsCodeGen  fnGen,
            Class<T> type) {
        var def = ObjectDefinition.of(type);
        js.eval("js", defGen.generate(def));
        js.eval("js", fnGen.generate(def));
    }

    private void loadResource(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) throw new IOException("classpath resource not found: " + path);
            var src = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            js.eval(Source.newBuilder("js", src, path.substring(path.lastIndexOf('/') + 1))
                    .buildLiteral());
        }
    }

    private boolean evalThrows(String snippet) {
        try {
            js.eval("js", snippet);
            return false;
        } catch (PolyglotException e) {
            return e.isGuestException();
        }
    }
}
