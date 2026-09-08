package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.Orientation;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0060 — the playbook, and what the shipped arrangements actually come out as.
 *
 * <p>The reason this test exists: a playbook's ratio is <b>local to its split</b>,
 * while every description of a layout is in shares of the <b>whole workspace</b>.
 * Those two readings are easy to state and easy to get wrong — {@code IDE} says
 * "editor 60%" and writes {@code 0.75}. {@link #shippedArrangementsHaveTheGeometryTheyClaim}
 * computes the absolute rects so the prose and the code cannot drift.</p>
 */
class ArrangementTest {

    /** Absolute (x, w) or (y, h) per pane, by walking the tree the way SplitPane does. */
    private static Map<String, double[]> rects(Arrangement a) {
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

    // ── The shipped set ──────────────────────────────────────────────────────

    @Test
    void shippedArrangementsHaveTheGeometryTheyClaim() {
        assertRect(rects(Arrangements.SINGLE), "main", 0, 0, 1, 1);

        var cols = rects(Arrangements.COLUMNS);
        assertRect(cols, "left",  0,   0, 0.5, 1);
        assertRect(cols, "right", 0.5, 0, 0.5, 1);

        var rows = rects(Arrangements.ROWS);
        assertRect(rows, "top",    0, 0,   1, 0.5);
        assertRect(rows, "bottom", 0, 0.5, 1, 0.5);

        var quad = rects(Arrangements.QUAD);
        assertRect(quad, "top-left",     0,   0,   0.5, 0.5);
        assertRect(quad, "top-right",    0.5, 0,   0.5, 0.5);
        assertRect(quad, "bottom-left",  0,   0.5, 0.5, 0.5);
        assertRect(quad, "bottom-right", 0.5, 0.5, 0.5, 0.5);

        // "a main pane with a 30% companion on the right"
        var side = rects(Arrangements.MAIN_AND_SIDE);
        assertRect(side, "main", 0,   0, 0.7, 1);
        assertRect(side, "side", 0.7, 0, 0.3, 1);

        // "a main pane with a 30% companion below"
        var out = rects(Arrangements.MAIN_AND_OUTPUT);
        assertRect(out, "main",   0, 0,   1, 0.7);
        assertRect(out, "output", 0, 0.7, 1, 0.3);

        // "explorer 20%, editor 60%, terminal 20% beneath the editor" — written as
        // 0.80 then 0.75, which is the whole point of pinning it.
        var ide = rects(Arrangements.IDE);
        assertRect(ide, "explorer", 0,   0,   0.2, 1);
        assertRect(ide, "editor",   0.2, 0,   0.8, 0.75);
        assertRect(ide, "terminal", 0.2, 0.75, 0.8, 0.25);

        // "three columns at 20 / 30 / 50" — written as 0.20 then 0.375.
        var tri = rects(Arrangements.TRIPLE_COLUMN);
        assertRect(tri, "nav",     0,   0, 0.2, 1);
        assertRect(tri, "list",    0.2, 0, 0.3, 1);
        assertRect(tri, "content", 0.5, 0, 0.5, 1);
    }

    @Test
    void everyShippedArrangementIsPureGeometry() {
        // D7 — widgets are the consumer's business, so a shipped design binds none.
        for (Arrangement a : Arrangements.ALL) {
            assertEquals(0, a.totalWidgets(), a.name() + " must ship without widgets");
        }
    }

    @Test
    void theDefaultIsOnePaneTakingEverything() {
        assertEquals(1, Arrangements.SINGLE.panes().size());
        assertRect(rects(Arrangements.SINGLE), "main", 0, 0, 1, 1);
    }

    // ── The playbook ─────────────────────────────────────────────────────────

    @Test
    void splitEvenlyEqualsSplitWithRatioAtAHalf() {
        var sugar = Arrangement.named("a").root("m").splitEvenly("m", PaneDirection.RIGHT, "n").build();
        var spelt = Arrangement.named("a").root("m").splitWithRatio("m", PaneDirection.RIGHT, "n", 0.5).build();
        assertEquals(spelt, sugar);
    }

    @Test
    void theRatioIsTheShareTheSplitPaneKeeps() {
        // The same 0.7 means "main keeps 0.7" whichever side the new pane lands on.
        var right = rects(Arrangement.named("r").root("main")
                .splitWithRatio("main", PaneDirection.RIGHT, "other", 0.7).build());
        assertRect(right, "main", 0, 0, 0.7, 1);

        var left = rects(Arrangement.named("l").root("main")
                .splitWithRatio("main", PaneDirection.LEFT, "other", 0.7).build());
        assertRect(left,  "main",  0.3, 0, 0.7, 1);
        assertRect(left,  "other", 0,   0, 0.3, 1);
    }

    @Test
    void directionDecidesOrientationSoItIsNeverAuthored() {
        var h = (LayoutNode.Split) Arrangement.named("h").root("a")
                .splitEvenly("a", PaneDirection.RIGHT, "b").build().layout();
        var v = (LayoutNode.Split) Arrangement.named("v").root("a")
                .splitEvenly("a", PaneDirection.DOWN, "b").build().layout();
        assertEquals(Orientation.HORIZONTAL, h.orientation());
        assertEquals(Orientation.VERTICAL,   v.orientation());
    }

    @Test
    void splittingAnUnknownPaneIsRejected() {
        var b = Arrangement.named("x").root("main");
        var e = assertThrows(IllegalArgumentException.class,
                () -> b.splitEvenly("nope", PaneDirection.RIGHT, "new"));
        assertTrue(e.getMessage().contains("nope"), e.getMessage());
    }

    @Test
    void reusingAPaneNameIsRejected() {
        var b = Arrangement.named("x").root("main");
        assertThrows(IllegalArgumentException.class,
                () -> b.splitEvenly("main", PaneDirection.RIGHT, "main"));
    }

    @Test
    void aRatioOutsideTheOpenUnitIntervalIsRejected() {
        var b = Arrangement.named("x").root("main");
        assertThrows(IllegalArgumentException.class,
                () -> b.splitWithRatio("main", PaneDirection.RIGHT, "n", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> b.splitWithRatio("main", PaneDirection.RIGHT, "n", 1.0));
    }

    // ── Reuse ────────────────────────────────────────────────────────────────

    @Test
    void withBindsWidgetsWithoutDisturbingTheShippedDesign() {
        var mine = Arrangements.IDE.with("editor", "DocViewWidget").with("terminal", "LogWidget");
        assertEquals(2, mine.totalWidgets());
        assertEquals(0, Arrangements.IDE.totalWidgets(), "the shipped constant must not be mutated");
        assertEquals(java.util.List.of("DocViewWidget"), mine.widgetsIn(new PaneId("editor")));
        assertEquals(java.util.List.of(), mine.widgetsIn(new PaneId("explorer")));
        assertEquals(Arrangements.IDE.layout(), mine.layout(), "geometry is untouched by binding");
    }

    @Test
    void twoWorkspacesCanShareOneDesignWithDifferentContents() {
        var a = Arrangements.COLUMNS.with("left", "AWidget");
        var b = Arrangements.COLUMNS.with("left", "BWidget");
        assertEquals(a.layout(), b.layout());
        assertNotEquals(a.widgets(), b.widgets());
    }

    @Test
    void bindingToAnUnknownPaneNamesThePanesThatExist() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> Arrangements.IDE.with("sidebar", "W"));
        assertTrue(e.getMessage().contains("explorer"), e.getMessage());
    }

    @Test
    void repeatedBindingAppendsInMountOrder() {
        var a = Arrangements.SINGLE.with("main", "First").with("main", "Second");
        assertEquals(java.util.List.of("First", "Second"), a.widgetsIn(new PaneId("main")));
    }

    // ── The wire ─────────────────────────────────────────────────────────────

    @Test
    void theWireCarriesMtpsNativeShapeWithBothRatios() {
        String json = WorkspaceSpecJson.arrangement(
                Arrangements.MAIN_AND_SIDE.with("main", "DocViewWidget"));
        assertTrue(json.contains("\"name\":\"main-and-side\""), json);
        assertTrue(json.contains("\"kind\":\"split\""), json);
        assertTrue(json.contains("\"orientation\":\"horizontal\""), json);
        assertTrue(json.contains("\"slotId\":\"main\""), json);
        // Both children carry their own share, and they sum to 1.
        assertTrue(json.contains("\"ratio\":0.7"), json);
        assertTrue(json.contains("\"ratio\":0.3"), json);
        assertTrue(json.contains("\"widgets\":{\"main\":[\"DocViewWidget\"]}"), json);
    }

    @Test
    void aPaneWithNoWidgetsIsOmittedFromTheWire() {
        String json = WorkspaceSpecJson.arrangement(Arrangements.COLUMNS);
        assertTrue(json.contains("\"widgets\":{}"), json);
    }
}
