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
 * RFC 0029 — the end-to-end real-world test. A realistic
 * {@link WorkspaceState} (split layout, multiple widgets in different
 * panes, one in the modal, named theme) is constructed in JS, encoded
 * through the composed codec stack, persisted via
 * {@code LocalStorageStore} (mock backing), reloaded, decoded, and
 * asserted equivalent to the original — both structurally and via the
 * cross-widget invariants the {@code WorkspaceState} constructor
 * enforces on decode.
 *
 * <p>The codec stack is hand-written end-to-end. If this test passes,
 * the codec foundation can drive real workspace persistence; the
 * remaining RFC 0029 work (auto-save event wiring, Force Save ribbon
 * control) becomes scaffolding around a proven core.</p>
 */
class WorkspaceStateE2ETest {

    private Context js;

    @BeforeEach
    void setup() throws IOException {
        js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();

        // ── Load all codec definitions + functions in dependency order ──
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

        // Polymorphism registry + a test widget kind's Params codec
        loadResource("/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/WidgetParamsCodecRegistry.js");
        js.eval("js", """
                class TestPet {
                    constructor(name, age) {
                        if (typeof name !== 'string') throw new TypeError("TestPet.name: expected string");
                        if (typeof age !== 'number' || !Number.isInteger(age) || age < 0) {
                            throw new RangeError("TestPet.age: expected non-negative integer");
                        }
                        this.name = name;
                        this.age = age;
                        Object.freeze(this);
                    }
                }
                const TestPetCodec = {
                    transformTo(p) {
                        if (!(p instanceof TestPet)) throw new TypeError("TestPetCodec.transformTo: expected TestPet");
                        return { name: p.name, age: p.age };
                    },
                    transformFrom(wire) {
                        return new TestPet(wire.name, wire.age);
                    }
                };
                WidgetParamsCodecRegistry.register('test-pet', TestPetCodec);
                """);

        loadCodecPair(WidgetInstanceJsDefinition.INSTANCE, WidgetInstanceJsFunctions.INSTANCE, WidgetInstance.class);
        loadCodecPair(WorkspaceStateJsDefinition.INSTANCE, WorkspaceStateJsFunctions.INSTANCE, WorkspaceState.class);

        // LocalStorageStore + mock storage
        loadResource("/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/LocalStorageStore.js");
        js.eval("js", """
                const mockStorage = {
                    _data: new Map(),
                    get length() { return this._data.size; },
                    key(i) {
                        const keys = Array.from(this._data.keys());
                        return i >= 0 && i < keys.length ? keys[i] : null;
                    },
                    getItem(k) { return this._data.has(k) ? this._data.get(k) : null; },
                    setItem(k, v) { this._data.set(String(k), String(v)); },
                    removeItem(k) { this._data.delete(String(k)); },
                    clear() { this._data.clear(); }
                };
                const store = createLocalStorageStore(mockStorage);
                """);
    }

    @AfterEach
    void teardown() {
        if (js != null) {
            js.close();
            js = null;
        }
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

    // ── The defining real-world claim ────────────────────────────────────────

    @Test
    void realisticWorkspaceStateRoundTripsThroughCodecAndStore() {
        // Construct a state that exercises every axis:
        //   - Split layout (two leaves)
        //   - Two widgets in different panes (one active, one not)
        //   - One widget in the modal (single-modal-slot invariant exercised)
        //   - Non-default chrome
        var result = js.eval("js", """
                const idA = '11111111-2222-3333-4444-555555555555';
                const idB = '66666666-7777-8888-9999-aaaaaaaaaaaa';
                const idM = 'bbbbbbbb-cccc-dddd-eeee-ffffffffffff';

                const original = new WorkspaceState(
                    1,
                    new WorkspaceKind('AnimalsPlayground'),
                    new Date('2026-05-26T20:00:00Z'),
                    new LayoutNode.Split(
                        Orientation.HORIZONTAL, 0.5,
                        new LayoutNode.Leaf(new PaneId('left')),
                        new LayoutNode.Leaf(new PaneId('right'))),
                    new Map([
                        [new WidgetInstanceId(idA), new WidgetInstance(
                            new WidgetInstanceId(idA), new WidgetKind('test-pet'),
                            new TestPet('Whiskers', 3),
                            new WidgetTitle('Whiskers (3yo)'),
                            new WidgetLocation.InPane(new PaneId('left'), 0, true))],
                        [new WidgetInstanceId(idB), new WidgetInstance(
                            new WidgetInstanceId(idB), new WidgetKind('test-pet'),
                            new TestPet('Rex', 7),
                            new WidgetTitle('Rex (7yo)'),
                            new WidgetLocation.InPane(new PaneId('right'), 0, true))],
                        [new WidgetInstanceId(idM), new WidgetInstance(
                            new WidgetInstanceId(idM), new WidgetKind('test-pet'),
                            new TestPet('Floater', 1),
                            new WidgetTitle('Floater (in modal)'),
                            new WidgetLocation.InModal())]
                    ]),
                    new ChromeState(new ThemeName('forest'), true));

                // ENCODE → SAVE
                const wire = WorkspaceStateCodec.transformTo(original);
                store.save('AnimalsPlayground', wire);

                // LOAD → DECODE
                const loadedWire = store.load('AnimalsPlayground');
                const restored   = WorkspaceStateCodec.transformFrom(loadedWire);

                // Assertions in JS — all return as flat fields for Java-side check
                const restoredA = restored.widgetsById.get(
                    [...restored.widgetsById.keys()].find(k => k.id === idA));
                const restoredB = restored.widgetsById.get(
                    [...restored.widgetsById.keys()].find(k => k.id === idB));
                const restoredM = restored.widgetsById.get(
                    [...restored.widgetsById.keys()].find(k => k.id === idM));

                ({
                    schemaVersionEqual:   restored.schemaVersion === original.schemaVersion,
                    workspaceKindEqual:   restored.workspaceKind.value === original.workspaceKind.value,
                    savedAtEqual:         restored.savedAt.toISOString() === original.savedAt.toISOString(),

                    layoutIsSplit:        restored.layout instanceof LayoutNode.Split,
                    layoutOrientationEqual: restored.layout.orientation === original.layout.orientation,
                    layoutRatioEqual:     restored.layout.ratio === original.layout.ratio,
                    layoutLeftPaneId:     restored.layout.first.paneId.value,
                    layoutRightPaneId:    restored.layout.second.paneId.value,

                    widgetCount:          restored.widgetsById.size,
                    widgetANameMatch:     restoredA.params.name === 'Whiskers',
                    widgetAAgeMatch:      restoredA.params.age === 3,
                    widgetATitleMatch:    restoredA.title.value === 'Whiskers (3yo)',
                    widgetAInLeftPane:    restoredA.location.paneId.value === 'left',
                    widgetAIsActive:      restoredA.location.isActive === true,
                    widgetAParamsTyped:   restoredA.params instanceof TestPet,
                    widgetAPaneIdTyped:   restoredA.location.paneId instanceof PaneId,

                    widgetBInRightPane:   restoredB.location.paneId.value === 'right',
                    widgetBName:          restoredB.params.name,

                    widgetMInModal:       restoredM.location instanceof WidgetLocation.InModal,
                    widgetMName:          restoredM.params.name,

                    themeEqual:           restored.chrome.theme.value === 'forest',
                    fullscreenEqual:      restored.chrome.fullscreen === true,
                    chromeStateTyped:     restored.chrome instanceof ChromeState
                })
                """);

        // ── Top-level fields ────────────────────────────────────────────────
        assertTrue(result.getMember("schemaVersionEqual").asBoolean(),    "schemaVersion preserved");
        assertTrue(result.getMember("workspaceKindEqual").asBoolean(),    "workspaceKind value preserved");
        assertTrue(result.getMember("savedAtEqual").asBoolean(),          "savedAt timestamp preserved");

        // ── Layout (sealed + recursive) ─────────────────────────────────────
        assertTrue(result.getMember("layoutIsSplit").asBoolean(),         "Split variant identity preserved");
        assertTrue(result.getMember("layoutOrientationEqual").asBoolean(),"Orientation enum value preserved");
        assertTrue(result.getMember("layoutRatioEqual").asBoolean(),      "Split ratio preserved");
        assertEquals("left",  result.getMember("layoutLeftPaneId").asString());
        assertEquals("right", result.getMember("layoutRightPaneId").asString());

        // ── Widget A (typed identifiers + polymorphic Params + InPane) ──────
        assertEquals(3, result.getMember("widgetCount").asInt(),          "all three widgets preserved");
        assertTrue(result.getMember("widgetANameMatch").asBoolean(),      "TestPet.name preserved");
        assertTrue(result.getMember("widgetAAgeMatch").asBoolean(),       "TestPet.age preserved");
        assertTrue(result.getMember("widgetATitleMatch").asBoolean(),     "WidgetTitle preserved");
        assertTrue(result.getMember("widgetAInLeftPane").asBoolean(),     "widget A in left pane");
        assertTrue(result.getMember("widgetAIsActive").asBoolean(),       "widget A isActive flag preserved");
        assertTrue(result.getMember("widgetAParamsTyped").asBoolean(),    "params is a typed TestPet (not raw object)");
        assertTrue(result.getMember("widgetAPaneIdTyped").asBoolean(),    "location.paneId is a typed PaneId");

        // ── Widget B + C — multi-widget composition ─────────────────────────
        assertTrue(result.getMember("widgetBInRightPane").asBoolean());
        assertEquals("Rex",     result.getMember("widgetBName").asString());
        assertTrue(result.getMember("widgetMInModal").asBoolean(),        "widget M correctly routed to modal");
        assertEquals("Floater", result.getMember("widgetMName").asString());

        // ── Chrome ──────────────────────────────────────────────────────────
        assertTrue(result.getMember("themeEqual").asBoolean());
        assertTrue(result.getMember("fullscreenEqual").asBoolean());
        assertTrue(result.getMember("chromeStateTyped").asBoolean());
    }

    // ── Cross-widget invariant preservation ─────────────────────────────────

    @Test
    void corruptWireWithTwoModalWidgetsFailsLoudly() {
        // A wire form that violates the single-modal-slot invariant
        // must fail at WorkspaceState construction time on decode —
        // the load-bearing claim of the codec's validation pass.
        boolean threw = evalThrows("""
                const idA = '11111111-2222-3333-4444-555555555555';
                const idB = '66666666-7777-8888-9999-aaaaaaaaaaaa';
                const corrupt = {
                    schemaVersion: 1,
                    workspaceKind: 'Ws',
                    savedAt: '2026-05-26T20:00:00Z',
                    layout: { kind: 'Leaf', paneId: 'p' },
                    widgetsById: {
                        [idA]: { id: idA, kind: 'test-pet', params: { name: 'a', age: 1 },
                                 title: 'A', location: { kind: 'InModal' } },
                        [idB]: { id: idB, kind: 'test-pet', params: { name: 'b', age: 1 },
                                 title: 'B', location: { kind: 'InModal' } }
                    },
                    chrome: { theme: 'default', fullscreen: false }
                };
                store.save('CorruptWs', corrupt);
                WorkspaceStateCodec.transformFrom(store.load('CorruptWs'));
                """);
        assertTrue(threw, "two widgets with InModal location must fail invariant check on decode");
    }

    @Test
    void unknownWidgetKindOnDecodeFailsLoudly() {
        boolean threw = evalThrows("""
                const id = '11111111-2222-3333-4444-555555555555';
                const corrupt = {
                    schemaVersion: 1,
                    workspaceKind: 'Ws',
                    savedAt: '2026-05-26T20:00:00Z',
                    layout: { kind: 'Leaf', paneId: 'p' },
                    widgetsById: {
                        [id]: { id: id, kind: 'unknown-kind', params: {},
                                 title: 'X', location: { kind: 'InPane', paneId: 'p', tabIndex: 0, isActive: true } }
                    },
                    chrome: { theme: 'default', fullscreen: false }
                };
                store.save('UnknownKindWs', corrupt);
                WorkspaceStateCodec.transformFrom(store.load('UnknownKindWs'));
                """);
        assertTrue(threw, "unregistered widget kind must fail loudly on decode (no silent params drop)");
    }

    // ── Empty workspace (minimal valid envelope) ────────────────────────────

    @Test
    void emptyWorkspaceRoundTrips() {
        Value result = js.eval("js", """
                const empty = new WorkspaceState(
                    1,
                    new WorkspaceKind('EmptyWs'),
                    new Date('2026-05-26T20:00:00Z'),
                    new LayoutNode.Leaf(new PaneId('only-pane')),
                    new Map(),
                    new ChromeState(ThemeName.DEFAULT, false));

                store.save('EmptyWs', WorkspaceStateCodec.transformTo(empty));
                const restored = WorkspaceStateCodec.transformFrom(store.load('EmptyWs'));

                ({
                    paneId:       restored.layout.paneId.value,
                    widgetCount:  restored.widgetsById.size,
                    theme:        restored.chrome.theme.value,
                    fullscreen:   restored.chrome.fullscreen
                })
                """);
        assertEquals("only-pane", result.getMember("paneId").asString());
        assertEquals(0,           result.getMember("widgetCount").asInt());
        assertEquals("default",   result.getMember("theme").asString());
        assertFalse(result.getMember("fullscreen").asBoolean());
    }

    // ── Deeply nested layout ────────────────────────────────────────────────

    @Test
    void deeplyNestedLayoutSurvivesRoundTrip() {
        // Three-level Split tree: ((p1 | p2) / p3) — exercises codec recursion
        var result = js.eval("js", """
                const state = new WorkspaceState(
                    1,
                    new WorkspaceKind('NestedWs'),
                    new Date('2026-05-26T20:00:00Z'),
                    new LayoutNode.Split(
                        Orientation.VERTICAL, 0.7,
                        new LayoutNode.Split(
                            Orientation.HORIZONTAL, 0.3,
                            new LayoutNode.Leaf(new PaneId('p1')),
                            new LayoutNode.Leaf(new PaneId('p2'))),
                        new LayoutNode.Leaf(new PaneId('p3'))),
                    new Map(),
                    ChromeState.defaults());

                store.save('NestedWs', WorkspaceStateCodec.transformTo(state));
                const r = WorkspaceStateCodec.transformFrom(store.load('NestedWs'));

                ({
                    outerOrientation: r.layout.orientation.name,
                    outerRatio:       r.layout.ratio,
                    innerOrientation: r.layout.first.orientation.name,
                    innerRatio:       r.layout.first.ratio,
                    p1: r.layout.first.first.paneId.value,
                    p2: r.layout.first.second.paneId.value,
                    p3: r.layout.second.paneId.value
                })
                """);
        assertEquals("VERTICAL",   result.getMember("outerOrientation").asString());
        assertEquals(0.7,          result.getMember("outerRatio").asDouble());
        assertEquals("HORIZONTAL", result.getMember("innerOrientation").asString());
        assertEquals(0.3,          result.getMember("innerRatio").asDouble());
        assertEquals("p1",         result.getMember("p1").asString());
        assertEquals("p2",         result.getMember("p2").asString());
        assertEquals("p3",         result.getMember("p3").asString());
    }
}
