package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.Orientation;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0060 — the playbook, the shipped shapes, and the shape/allocation split.
 *
 * <p>The reason the geometry assertions exist: a playbook's ratio is <b>local to
 * its split</b>, while every description of a layout is in shares of the
 * <b>whole workspace</b>. Both readings are easy to state and easy to get wrong —
 * {@code IDE} says "editor 60%" and writes {@code 0.75}. Computing the absolute
 * rects is what stops the prose and the code drifting apart.</p>
 */
class ArrangementTest {

    /** Absolute rects per pane, walking the tree the way SplitPane does. */
    private static Map<String, double[]> rects(PaneArrangement a) {
        var out = new LinkedHashMap<String, double[]>();
        walk(a.layout(), 0, 0, 1, 1, out);
        return out;
    }

    private static void walk(LayoutNode n, double x, double y, double w, double h,
                             Map<String, double[]> out) {
        switch (n) {
            case LayoutNode.Leaf leaf -> out.put(leaf.paneId().value(), new double[]{x, y, w, h});
            case LayoutNode.Split s -> {
                boolean horiz = s.orientation() == Orientation.HORIZONTAL;
                double firstW = horiz ? w * s.ratio() : w;
                double firstH = horiz ? h : h * s.ratio();
                walk(s.first(), x, y, firstW, firstH, out);
                walk(s.second(), horiz ? x + firstW : x, horiz ? y : y + firstH,
                     horiz ? w - firstW : w, horiz ? h : h - firstH, out);
            }
        }
    }

    private static void assertRect(Map<String, double[]> r, String pane,
                                   double x, double y, double w, double h) {
        var got = r.get(pane);
        assertTrue(got != null, "no pane '" + pane + "' — have " + r.keySet());
        assertEquals(x, got[0], 1e-9, pane + ".x");
        assertEquals(y, got[1], 1e-9, pane + ".y");
        assertEquals(w, got[2], 1e-9, pane + ".w");
        assertEquals(h, got[3], 1e-9, pane + ".h");
    }

    // ── The shipped shapes ───────────────────────────────────────────────────

    @Test
    void shippedShapesHaveTheGeometryTheyClaim() {
        assertRect(rects(PaneArrangements.SINGLE), "main", 0, 0, 1, 1);

        var cols = rects(PaneArrangements.COLUMNS);
        assertRect(cols, "left",  0,   0, 0.5, 1);
        assertRect(cols, "right", 0.5, 0, 0.5, 1);

        var rows = rects(PaneArrangements.ROWS);
        assertRect(rows, "top",    0, 0,   1, 0.5);
        assertRect(rows, "bottom", 0, 0.5, 1, 0.5);

        var quad = rects(PaneArrangements.QUAD);
        assertRect(quad, "top-left",     0,   0,   0.5, 0.5);
        assertRect(quad, "top-right",    0.5, 0,   0.5, 0.5);
        assertRect(quad, "bottom-left",  0,   0.5, 0.5, 0.5);
        assertRect(quad, "bottom-right", 0.5, 0.5, 0.5, 0.5);

        // "a main pane with a 30% companion on the right"
        var side = rects(PaneArrangements.MAIN_AND_SIDE);
        assertRect(side, "main", 0,   0, 0.7, 1);
        assertRect(side, "side", 0.7, 0, 0.3, 1);

        // "a main pane with a 30% companion below"
        var out = rects(PaneArrangements.MAIN_AND_OUTPUT);
        assertRect(out, "main",   0, 0,   1, 0.7);
        assertRect(out, "output", 0, 0.7, 1, 0.3);

        // "explorer 20%, editor 60%, terminal 20%" — written as 0.80 then 0.75,
        // which is exactly why this is pinned.
        var ide = rects(PaneArrangements.IDE);
        assertRect(ide, "explorer", 0,   0,    0.2, 1);
        assertRect(ide, "editor",   0.2, 0,    0.8, 0.75);
        assertRect(ide, "terminal", 0.2, 0.75, 0.8, 0.25);

        // "three columns at 20 / 30 / 50" — written as 0.20 then 0.375.
        var tri = rects(PaneArrangements.TRIPLE_COLUMN);
        assertRect(tri, "nav",     0,   0, 0.2, 1);
        assertRect(tri, "list",    0.2, 0, 0.3, 1);
        assertRect(tri, "content", 0.5, 0, 0.5, 1);
    }

    @Test
    void aShapeIsGeometryAndNothingElse() {
        // The whole point of the split: a shipped shape has no widgets to have,
        // so it cannot carry a workspace's opinions into another workspace.
        for (PaneArrangement shape : PaneArrangements.ALL) {
            assertEquals(0, shape.empty().totalWidgets(), shape.name());
            assertTrue(shape.paneCount() >= 1, shape.name());
        }
    }

    @Test
    void theDefaultIsOnePaneTakingEverything() {
        assertEquals(1, PaneArrangements.SINGLE.paneCount());
        assertRect(rects(PaneArrangements.SINGLE), "main", 0, 0, 1, 1);
    }

    // ── The playbook ─────────────────────────────────────────────────────────

    @Test
    void splitEvenlyEqualsSplitWithRatioAtAHalf() {
        var sugar = PaneArrangement.named("a").root("m")
                .splitEvenly("m", PaneDirection.RIGHT, "n").build();
        var spelt = PaneArrangement.named("a").root("m")
                .splitWithRatio("m", PaneDirection.RIGHT, "n", 0.5).build();
        assertEquals(spelt, sugar);
    }

    @Test
    void theRatioIsTheShareTheSplitPaneKeeps() {
        // The same 0.7 means "main keeps 0.7" whichever side the new pane lands on.
        var right = rects(PaneArrangement.named("r").root("main")
                .splitWithRatio("main", PaneDirection.RIGHT, "other", 0.7).build());
        assertRect(right, "main",  0,   0, 0.7, 1);
        assertRect(right, "other", 0.7, 0, 0.3, 1);

        var left = rects(PaneArrangement.named("l").root("main")
                .splitWithRatio("main", PaneDirection.LEFT, "other", 0.7).build());
        assertRect(left, "main",  0.3, 0, 0.7, 1);
        assertRect(left, "other", 0,   0, 0.3, 1);
    }

    @Test
    void directionDecidesOrientationSoItIsNeverAuthored() {
        var h = (LayoutNode.Split) PaneArrangement.named("h").root("a")
                .splitEvenly("a", PaneDirection.RIGHT, "b").build().layout();
        var v = (LayoutNode.Split) PaneArrangement.named("v").root("a")
                .splitEvenly("a", PaneDirection.DOWN, "b").build().layout();
        assertEquals(Orientation.HORIZONTAL, h.orientation());
        assertEquals(Orientation.VERTICAL,   v.orientation());
    }

    @Test
    void splittingAnUnknownPaneIsRejected() {
        var b = PaneArrangement.named("x").root("main");
        var e = assertThrows(IllegalArgumentException.class,
                () -> b.splitEvenly("nope", PaneDirection.RIGHT, "new"));
        assertTrue(e.getMessage().contains("nope"), e.getMessage());
    }

    @Test
    void reusingAPaneNameIsRejected() {
        var b = PaneArrangement.named("x").root("main");
        assertThrows(IllegalArgumentException.class,
                () -> b.splitEvenly("main", PaneDirection.RIGHT, "main"));
    }

    @Test
    void aRatioOutsideTheOpenUnitIntervalIsRejected() {
        var b = PaneArrangement.named("x").root("main");
        assertThrows(IllegalArgumentException.class,
                () -> b.splitWithRatio("main", PaneDirection.RIGHT, "n", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> b.splitWithRatio("main", PaneDirection.RIGHT, "n", 1.0));
    }

    // ── Shape reused, allocation not ─────────────────────────────────────────

    /**
     * Widget classes exist only to be named — {@code place} reads
     * {@code getSimpleName()} and never instantiates — so abstract stubs are the
     * honest stand-in for a spec's real widgets, which live in modules this one
     * cannot depend on.
     */
    abstract static class TreeWidget   extends WorkspaceWidget<WorkspaceWidget._None, TreeWidget> {}
    abstract static class DocWidget    extends WorkspaceWidget<WorkspaceWidget._None, DocWidget> {}
    abstract static class LogWidget    extends WorkspaceWidget<WorkspaceWidget._None, LogWidget> {}

    @Test
    void oneShapeServesTwoWorkspacesWithDifferentContents() {
        var a = PaneArrangements.COLUMNS.allocate()
                .place(PaneArrangements.Columns.LEFT, TreeWidget.class).build();
        var b = PaneArrangements.COLUMNS.allocate()
                .place(PaneArrangements.Columns.LEFT, DocWidget.class).build();
        assertEquals(a.panes(), b.panes(), "the shape is shared, identically");
        assertNotEquals(a.widgets(), b.widgets(), "the allocation is not");
    }

    @Test
    void allocatingDoesNotDisturbTheShippedShape() {
        var mine = PaneArrangements.IDE.allocate()
                .place(PaneArrangements.Ide.EDITOR,   DocWidget.class)
                .place(PaneArrangements.Ide.TERMINAL, LogWidget.class)
                .build();
        assertEquals(2, mine.totalWidgets());
        assertEquals(0, PaneArrangements.IDE.empty().totalWidgets(),
                "the shipped shape has no widgets to lose");
        assertEquals(List.of(DocWidget.class), mine.widgetsIn(PaneArrangements.Ide.EDITOR));
        assertEquals(List.of(), mine.widgetsIn(PaneArrangements.Ide.EXPLORER));
        assertEquals(PaneArrangements.IDE, mine.panes(), "geometry untouched by allocation");
    }

    @Test
    void aPaneFromAnotherShapeIsRejected() {
        // The distinct type stops the pane-name mix-up the compiler cannot see:
        // both are ShapePanes, but they belong to different shapes.
        var e = assertThrows(IllegalArgumentException.class,
                () -> PaneArrangements.IDE.allocate()
                        .place(PaneArrangements.Columns.LEFT, TreeWidget.class));
        assertTrue(e.getMessage().contains("different shape"), e.getMessage());
    }

    @Test
    void aShapeRefusesToNameAPaneItDoesNotHave() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> PaneArrangements.IDE.pane("sidebar"));
        assertTrue(e.getMessage().contains("explorer"), e.getMessage());
    }

    @Test
    void panesConstantsCannotDriftFromTheirPlaybook() {
        // Each constant is built through pane(), so this asserts the set rather
        // than each name: a playbook rename breaks class init, not this test.
        assertEquals(List.of("explorer", "editor", "terminal"),
                PaneArrangements.IDE.shapePanes().stream().map(ShapePane::name).toList());
        assertEquals(PaneArrangements.IDE.name(), PaneArrangements.Ide.EDITOR.shapeName());
    }

    @Test
    void aShapePaneIsTheLiveSlotIdOnlyAtSeedTime() {
        assertEquals(new PaneId("editor"), PaneArrangements.Ide.EDITOR.asSeededPaneId());
        assertEquals("ide.editor", PaneArrangements.Ide.EDITOR.toString());
    }

    @Test
    void repeatedAllocationAppendsInMountOrder() {
        var a = PaneArrangements.SINGLE.allocate()
                .place(PaneArrangements.Single.MAIN, TreeWidget.class)
                .place(PaneArrangements.Single.MAIN, DocWidget.class).build();
        assertEquals(List.of(TreeWidget.class, DocWidget.class),
                a.widgetsIn(PaneArrangements.Single.MAIN));
    }

    @Test
    void allocateFromAnExistingArrangementKeepsWhatWasPlaced() {
        var first  = PaneArrangements.COLUMNS.allocate()
                .place(PaneArrangements.Columns.LEFT, TreeWidget.class).build();
        var second = first.allocate()
                .place(PaneArrangements.Columns.RIGHT, DocWidget.class).build();
        assertEquals(List.of(TreeWidget.class), second.widgetsIn(PaneArrangements.Columns.LEFT));
        assertEquals(List.of(DocWidget.class),  second.widgetsIn(PaneArrangements.Columns.RIGHT));
    }

    // ── The wire ─────────────────────────────────────────────────────────────

    @Test
    void theWireCarriesMtpsNativeShapeWithBothRatios() {
        String json = WorkspaceSpecJson.arrangement(
                PaneArrangements.MAIN_AND_SIDE.allocate()
                        .place(PaneArrangements.MainAndSide.MAIN, DocWidget.class).build());
        assertTrue(json.contains("\"name\":\"main-and-side\""), json);
        assertTrue(json.contains("\"kind\":\"split\""), json);
        assertTrue(json.contains("\"orientation\":\"horizontal\""), json);
        assertTrue(json.contains("\"slotId\":\"main\""), json);
        // Both children carry their own share, and they sum to 1.
        assertTrue(json.contains("\"ratio\":0.7"), json);
        assertTrue(json.contains("\"ratio\":0.3"), json);
        // The wire carries the widget's simpleName, which is what the shell mounts by.
        assertTrue(json.contains("\"widgets\":{\"main\":[\"DocWidget\"]}"), json);
    }

    @Test
    void aPaneWithNoWidgetsIsOmittedFromTheWire() {
        String json = WorkspaceSpecJson.arrangement(PaneArrangements.COLUMNS.empty());
        assertTrue(json.contains("\"widgets\":{}"), json);
    }
}
