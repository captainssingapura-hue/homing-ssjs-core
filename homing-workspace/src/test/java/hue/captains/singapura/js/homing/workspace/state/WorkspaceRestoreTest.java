package hue.captains.singapura.js.homing.workspace.state;

import hue.captains.singapura.js.homing.core.Widget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0029 cycle 3 — restore-pass tests. A {@link RecordingBuilder}
 * captures the sequence of calls the algorithm issues; tests assert on
 * the call order to prove the canonical four-phase restore sequence
 * (layout → in-pane widgets → modal → chrome).
 *
 * <p>Also exercises round-trip equivalence: capture → restore against a
 * second LiveWorkspace whose state the recording builder reconstructs;
 * the second capture equals the first.</p>
 */
class WorkspaceRestoreTest {

    // ── Mocks ───────────────────────────────────────────────────────────────

    private record TestParams(String animal) implements Widget._Param {}

    /** Sealed record of every call type the algorithm can issue. */
    private sealed interface Call permits Call.Layout, Call.InPane, Call.InModal, Call.Chrome {
        record Layout(LayoutNode layout) implements Call {}
        record InPane(WidgetInstance instance, WidgetLocation.InPane location) implements Call {}
        record InModal(WidgetInstance instance, WidgetLocation.InModal location) implements Call {}
        record Chrome(ChromeState chrome) implements Call {}
    }

    private static final class RecordingBuilder implements WorkspaceBuilder {
        final List<Call> calls = new ArrayList<>();
        @Override public void resetLayout(LayoutNode layout) {
            calls.add(new Call.Layout(layout));
        }
        @Override public void openInPane(WidgetInstance instance, WidgetLocation.InPane location) {
            calls.add(new Call.InPane(instance, location));
        }
        @Override public void openInModal(WidgetInstance instance, WidgetLocation.InModal location) {
            calls.add(new Call.InModal(instance, location));
        }
        @Override public void applyChrome(ChromeState chrome) {
            calls.add(new Call.Chrome(chrome));
        }
    }

    private static WidgetInstance widgetIn(WidgetInstanceId id, PaneId pane, int idx, boolean active) {
        return new WidgetInstance(id, WidgetKind.of("test"),
                new TestParams("cat"), WidgetTitle.of("T"),
                new WidgetLocation.InPane(pane, idx, active));
    }

    private static WidgetInstance widgetInModal(WidgetInstanceId id) {
        return new WidgetInstance(id, WidgetKind.of("test"),
                new TestParams("cat"), WidgetTitle.of("T"),
                WidgetLocation.InModal.INSTANCE);
    }

    /** Build a WorkspaceState honouring widgetsById key/value id agreement. */
    private static WorkspaceState build(LayoutNode layout,
                                        List<WidgetInstance> widgets,
                                        ChromeState chrome) {
        var map = new LinkedHashMap<WidgetInstanceId, WidgetInstance>();
        for (var w : widgets) map.put(w.id(), w);
        return new WorkspaceState(WorkspaceState.CURRENT_SCHEMA_VERSION,
                WorkspaceKind.of("Ws"), Instant.parse("2026-05-26T19:00:00Z"),
                layout, map, chrome);
    }

    // ── Empty workspace ─────────────────────────────────────────────────────

    @Test
    void restoreOfEmptyWorkspaceCallsLayoutThenChrome() {
        var s = build(new LayoutNode.Leaf(PaneId.of("p")), List.of(), ChromeState.defaults());
        var b = new RecordingBuilder();
        WorkspaceRestore.restoreState(s, b);
        assertEquals(2, b.calls.size());
        assertInstanceOf(Call.Layout.class, b.calls.get(0));
        assertInstanceOf(Call.Chrome.class, b.calls.get(1));
    }

    // ── Order of operations ─────────────────────────────────────────────────

    @Test
    void canonicalOrderIsLayoutInPaneInModalChrome() {
        var p = PaneId.of("p1");
        var idA = WidgetInstanceId.fresh();
        var idM = WidgetInstanceId.fresh();
        var s = build(new LayoutNode.Leaf(p),
                List.of(widgetIn(idA, p, 0, true), widgetInModal(idM)),
                ChromeState.defaults());

        var b = new RecordingBuilder();
        WorkspaceRestore.restoreState(s, b);

        assertEquals(4, b.calls.size());
        assertInstanceOf(Call.Layout.class,  b.calls.get(0));
        assertInstanceOf(Call.InPane.class,  b.calls.get(1));
        assertInstanceOf(Call.InModal.class, b.calls.get(2));
        assertInstanceOf(Call.Chrome.class,  b.calls.get(3));
    }

    @Test
    void inPaneWidgetsSortedByPaneIdThenTabIndex() {
        var pA = PaneId.of("a-pane");
        var pB = PaneId.of("b-pane");
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        var id3 = WidgetInstanceId.fresh();
        var id4 = WidgetInstanceId.fresh();

        // Construct widgets out of order; expect them to come out sorted.
        var s = build(
                new LayoutNode.Split(Orientation.HORIZONTAL, 0.5,
                        new LayoutNode.Leaf(pA), new LayoutNode.Leaf(pB)),
                List.of(
                        widgetIn(id3, pB, 1, false),
                        widgetIn(id1, pA, 1, false),
                        widgetIn(id2, pB, 0, true),
                        widgetIn(id4, pA, 0, true)),
                ChromeState.defaults());

        var b = new RecordingBuilder();
        WorkspaceRestore.restoreState(s, b);

        // Calls 1..4 are the in-pane widgets. Sorted: (a,0), (a,1), (b,0), (b,1)
        var paneCalls = b.calls.stream()
                .filter(c -> c instanceof Call.InPane)
                .map(c -> (Call.InPane) c)
                .toList();
        assertEquals(4, paneCalls.size());
        assertEquals(id4, paneCalls.get(0).instance().id());    // (a-pane, 0, active)
        assertEquals(id1, paneCalls.get(1).instance().id());    // (a-pane, 1)
        assertEquals(id2, paneCalls.get(2).instance().id());    // (b-pane, 0, active)
        assertEquals(id3, paneCalls.get(3).instance().id());    // (b-pane, 1)
    }

    @Test
    void inPaneOpensComeBeforeInModalOpen() {
        var p = PaneId.of("p1");
        var idPane  = WidgetInstanceId.fresh();
        var idModal = WidgetInstanceId.fresh();
        var s = build(new LayoutNode.Leaf(p),
                List.of(widgetInModal(idModal), widgetIn(idPane, p, 0, true)),    // modal first in input
                ChromeState.defaults());

        var b = new RecordingBuilder();
        WorkspaceRestore.restoreState(s, b);

        // Layout, InPane(idPane), InModal(idModal), Chrome — modal sorts AFTER panes regardless of input order.
        assertInstanceOf(Call.InPane.class,  b.calls.get(1));
        assertEquals(idPane,  ((Call.InPane)  b.calls.get(1)).instance().id());
        assertInstanceOf(Call.InModal.class, b.calls.get(2));
        assertEquals(idModal, ((Call.InModal) b.calls.get(2)).instance().id());
    }

    // ── Argument validation ─────────────────────────────────────────────────

    @Test
    void requiresNonNullArguments() {
        var s = build(new LayoutNode.Leaf(PaneId.of("p")), List.of(), ChromeState.defaults());
        var b = new RecordingBuilder();
        assertThrows(NullPointerException.class, () -> WorkspaceRestore.restoreState(null, b));
        assertThrows(NullPointerException.class, () -> WorkspaceRestore.restoreState(s, null));
    }

    // ── Payload pass-through ────────────────────────────────────────────────

    @Test
    void instanceAndLocationPairAgree() {
        var p = PaneId.of("p1");
        var id = WidgetInstanceId.fresh();
        var s = build(new LayoutNode.Leaf(p),
                List.of(widgetIn(id, p, 2, true)),
                ChromeState.defaults());

        var b = new RecordingBuilder();
        WorkspaceRestore.restoreState(s, b);

        var inPaneCall = (Call.InPane) b.calls.get(1);
        assertEquals(id, inPaneCall.instance().id());
        assertEquals(inPaneCall.instance().location(), inPaneCall.location());
        assertEquals(2,    inPaneCall.location().tabIndex());
        assertTrue(inPaneCall.location().isActive());
    }

    @Test
    void chromeStatePassesThrough() {
        var chrome = new ChromeState(ThemeName.of("forest"), true);
        var s = build(new LayoutNode.Leaf(PaneId.of("p")), List.of(), chrome);
        var b = new RecordingBuilder();
        WorkspaceRestore.restoreState(s, b);
        var chromeCall = (Call.Chrome) b.calls.get(b.calls.size() - 1);
        assertSame(chrome, chromeCall.chrome());
    }

    // ── Round-trip with captureState ────────────────────────────────────────

    @Test
    void captureRestoreCaptureIsIdempotent() {
        // Construct a state; restore through a RecordingBuilder that also
        // exposes a LiveWorkspace view of itself; capture again; assert equal.
        var pA = PaneId.of("pa");
        var pB = PaneId.of("pb");
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        var idM = WidgetInstanceId.fresh();
        var original = build(
                new LayoutNode.Split(Orientation.HORIZONTAL, 0.4,
                        new LayoutNode.Leaf(pA), new LayoutNode.Leaf(pB)),
                List.of(widgetIn(id1, pA, 0, true),
                        widgetIn(id2, pB, 0, true),
                        widgetInModal(idM)),
                new ChromeState(ThemeName.of("sunset"), false));

        // Restore into a captureable mirror.
        var mirror = new MirrorBuilder();
        WorkspaceRestore.restoreState(original, mirror);

        // Re-capture from the mirror.
        var rebuilt = WorkspaceCapture.captureState(mirror.asLive(), original.savedAt());

        assertEquals(original.layout(),       rebuilt.layout());
        assertEquals(original.chrome(),       rebuilt.chrome());
        assertEquals(original.widgetsById().keySet(), rebuilt.widgetsById().keySet());
    }

    /**
     * Builder that records calls AND exposes a LiveWorkspace view of the
     * resulting state — used for capture→restore→capture idempotency tests.
     */
    private static final class MirrorBuilder implements WorkspaceBuilder {
        private LayoutNode layout;
        private ChromeState chrome;
        private final Map<WidgetInstanceId, WidgetInstance> widgets = new LinkedHashMap<>();

        @Override public void resetLayout(LayoutNode l)                                       { this.layout = l; }
        @Override public void openInPane(WidgetInstance i, WidgetLocation.InPane loc)         { widgets.put(i.id(), i); }
        @Override public void openInModal(WidgetInstance i, WidgetLocation.InModal loc)       { widgets.put(i.id(), i); }
        @Override public void applyChrome(ChromeState c)                                      { this.chrome = c; }

        LiveWorkspace asLive() {
            return new LiveWorkspace() {
                @Override public WorkspaceKind kind() { return WorkspaceKind.of("Ws"); }
                @Override public LayoutNode layout()  { return layout; }
                @Override public java.util.Collection<LiveWidget> widgets() {
                    return widgets.values().stream().map(w -> (LiveWidget) new LiveWidget() {
                        @Override public WidgetInstanceId id()    { return w.id(); }
                        @Override public WidgetKind kind()        { return w.kind(); }
                        @Override public Widget._Param params()   { return w.params(); }
                        @Override public WidgetTitle title()      { return w.title(); }
                        @Override public WidgetLocation location() { return w.location(); }
                    }).toList();
                }
                @Override public ChromeState chrome() { return chrome; }
            };
        }
    }
}
