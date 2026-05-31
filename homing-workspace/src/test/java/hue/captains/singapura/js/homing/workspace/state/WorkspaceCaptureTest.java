package hue.captains.singapura.js.homing.workspace.state;

import hue.captains.singapura.js.homing.core.Widget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0029 cycle 2 — capture-pass tests. Programmatically constructs
 * mock {@link LiveWorkspace} instances (as records implementing the
 * interface) and asserts on the {@link WorkspaceState} the capture pass
 * produces.
 *
 * <p>The mocks are tiny — each is a one-line record that just delegates
 * to a constructor parameter. The work is in the assertions: per-axis
 * captures stay axis-scoped, composed captureState fails when invariants
 * are violated, identity flows through unchanged.</p>
 */
class WorkspaceCaptureTest {

    // ── Mocks ───────────────────────────────────────────────────────────────

    private record TestParams(String animal) implements Widget._Param {}

    private record TestLiveWidget(
            WidgetInstanceId id,
            WidgetKind       kind,
            Widget._Param    params,
            WidgetTitle      title,
            WidgetLocation   location
    ) implements LiveWidget {}

    private record TestLiveWorkspace(
            WorkspaceKind            kind,
            LayoutNode               layout,
            Collection<LiveWidget>   widgets,
            ChromeState              chrome
    ) implements LiveWorkspace {}

    private static final Instant T = Instant.parse("2026-05-26T19:00:00Z");

    private static LiveWidget testWidget(WidgetInstanceId id, WidgetLocation loc) {
        return new TestLiveWidget(id, WidgetKind.of("test"),
                new TestParams("cat"), WidgetTitle.of("Cat tab"), loc);
    }

    // ── captureLayout (axis 1) ──────────────────────────────────────────────

    @Test
    void captureLayoutPassesThroughTheLiveTree() {
        var split = new LayoutNode.Split(Orientation.HORIZONTAL, 0.5,
                new LayoutNode.Leaf(PaneId.of("a")),
                new LayoutNode.Leaf(PaneId.of("b")));
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), split, List.of(), ChromeState.defaults());
        assertSame(split, WorkspaceCapture.captureLayout(live));
    }

    // ── captureWidgets (axes 2 + 3) ─────────────────────────────────────────

    @Test
    void captureWidgetsMaterialisesTypedInstancesKeyedById() {
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        var p   = PaneId.of("p1");
        var w1  = testWidget(id1, new WidgetLocation.InPane(p, 0, true));
        var w2  = testWidget(id2, WidgetLocation.InModal.INSTANCE);
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(p),
                List.of(w1, w2), ChromeState.defaults());

        var widgets = WorkspaceCapture.captureWidgets(live);

        assertEquals(2, widgets.size());
        assertEquals(WidgetKind.of("test"),         widgets.get(id1).kind());
        assertEquals("Cat tab",                     widgets.get(id1).title().value());
        assertInstanceOf(TestParams.class,          widgets.get(id1).params());
        assertEquals("cat",                         ((TestParams) widgets.get(id1).params()).animal());
        assertEquals(new WidgetLocation.InPane(p, 0, true), widgets.get(id1).location());
        assertEquals(WidgetLocation.InModal.INSTANCE,        widgets.get(id2).location());
    }

    @Test
    void captureWidgetsRejectsDuplicateIds() {
        var dup = WidgetInstanceId.fresh();
        var p   = PaneId.of("p1");
        var w1  = testWidget(dup, new WidgetLocation.InPane(p, 0, true));
        var w2  = testWidget(dup, new WidgetLocation.InPane(p, 1, false));
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(p),
                List.of(w1, w2), ChromeState.defaults());
        var ex = assertThrows(IllegalStateException.class,
                () -> WorkspaceCapture.captureWidgets(live));
        assertTrue(ex.getMessage().contains("Duplicate widget instance id"));
    }

    @Test
    void captureWidgetsRejectsNullEntry() {
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(PaneId.of("p")),
                java.util.Arrays.asList((LiveWidget) null),
                ChromeState.defaults());
        assertThrows(NullPointerException.class,
                () -> WorkspaceCapture.captureWidgets(live));
    }

    @Test
    void captureWidgetsPreservesIterationOrder() {
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        var id3 = WidgetInstanceId.fresh();
        var p   = PaneId.of("p1");
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(p),
                List.of(
                        testWidget(id1, new WidgetLocation.InPane(p, 0, true)),
                        testWidget(id2, new WidgetLocation.InPane(p, 1, false)),
                        testWidget(id3, new WidgetLocation.InPane(p, 2, false))),
                ChromeState.defaults());
        var keys = WorkspaceCapture.captureWidgets(live).keySet().stream().toList();
        assertEquals(List.of(id1, id2, id3), keys);
    }

    // ── captureChrome (chrome) ──────────────────────────────────────────────

    @Test
    void captureChromePassesThrough() {
        var chrome = new ChromeState(ThemeName.of("forest"), true);
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(PaneId.of("p")),
                List.of(), chrome);
        assertSame(chrome, WorkspaceCapture.captureChrome(live));
    }

    // ── captureState (composition) ──────────────────────────────────────────

    @Test
    void captureStateComposesAllAxes() {
        var id = WidgetInstanceId.fresh();
        var p  = PaneId.of("p1");
        var w  = testWidget(id, new WidgetLocation.InPane(p, 0, true));
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("AnimalsPlayground"), new LayoutNode.Leaf(p),
                List.of(w), new ChromeState(ThemeName.of("forest"), false));

        var state = WorkspaceCapture.captureState(live, T);

        assertEquals(WorkspaceState.CURRENT_SCHEMA_VERSION, state.schemaVersion());
        assertEquals(WorkspaceKind.of("AnimalsPlayground"), state.workspaceKind());
        assertEquals(T,                                     state.savedAt());
        assertEquals(new LayoutNode.Leaf(p),                state.layout());
        assertEquals(1,                                     state.widgetsById().size());
        assertEquals(ThemeName.of("forest"),                state.chrome().theme());
    }

    @Test
    void captureStateOfEmptyWorkspace() {
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Empty"), new LayoutNode.Leaf(PaneId.of("p")),
                List.of(), ChromeState.defaults());
        var state = WorkspaceCapture.captureState(live, T);
        assertTrue(state.widgetsById().isEmpty());
    }

    @Test
    void captureStatePropagatesInvariantViolations() {
        // Two widgets in the modal — invariant lives in WorkspaceState's
        // compact constructor; the capture pass must surface the failure
        // rather than producing a corrupt snapshot.
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(PaneId.of("p")),
                List.of(testWidget(id1, WidgetLocation.InModal.INSTANCE),
                        testWidget(id2, WidgetLocation.InModal.INSTANCE)),
                ChromeState.defaults());
        assertThrows(IllegalArgumentException.class,
                () -> WorkspaceCapture.captureState(live, T));
    }

    @Test
    void captureStatePropagatesGhostPaneViolation() {
        var id = WidgetInstanceId.fresh();
        // Widget claims to be in pane "ghost", but layout only has "p"
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(PaneId.of("p")),
                List.of(testWidget(id, new WidgetLocation.InPane(PaneId.of("ghost"), 0, true))),
                ChromeState.defaults());
        assertThrows(IllegalArgumentException.class,
                () -> WorkspaceCapture.captureState(live, T));
    }

    @Test
    void captureStateRequiresNonNullArguments() {
        assertThrows(NullPointerException.class,
                () -> WorkspaceCapture.captureState(null, T));
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(PaneId.of("p")),
                List.of(), ChromeState.defaults());
        assertThrows(NullPointerException.class,
                () -> WorkspaceCapture.captureState(live, null));
    }

    @Test
    void captureStateIsPureFunction() {
        // Same input → equal output. The capture pass has no hidden state.
        var id = WidgetInstanceId.fresh();
        var p  = PaneId.of("p");
        var live = new TestLiveWorkspace(
                WorkspaceKind.of("Ws"), new LayoutNode.Leaf(p),
                List.of(testWidget(id, new WidgetLocation.InPane(p, 0, true))),
                ChromeState.defaults());
        var s1 = WorkspaceCapture.captureState(live, T);
        var s2 = WorkspaceCapture.captureState(live, T);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
