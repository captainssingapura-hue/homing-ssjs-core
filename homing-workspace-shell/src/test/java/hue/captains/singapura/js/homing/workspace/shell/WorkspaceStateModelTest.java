package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code WorkspaceStateModel}. Pure state container —
 * no collaborators, no async, no DOM. Tests cover the entire event
 * vocabulary, the layout-tree transitions, the snapshot round-trip,
 * the idempotency contracts, and the path-resolution edge cases.
 */
class WorkspaceStateModelTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceStateModelModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value freshModel() {
        return global("WorkspaceStateModel").newInstance();
    }

    private Value event(String name, String payloadJs) {
        return js.eval("js",
                "({ name: '" + name + "', payload: " + payloadJs + " })");
    }

    private void applyEvent(Value model, String name, String payloadJs) {
        model.invokeMember("apply", event(name, payloadJs));
    }

    /** Helper: lookup tabs for a slot in the model. */
    private Value tabsAt(Value model, String slot) {
        return model.invokeMember("tabsBySlot").invokeMember("get", slot);
    }

    // ── Initial state ───────────────────────────────────────────────────

    @Test
    void freshModelHasDefault2x2LayoutAndEmptySlots() {
        Value m = freshModel();
        assertEquals(true, m.invokeMember("isEmpty").asBoolean());
        Value layout = m.invokeMember("layout");
        assertEquals("split", layout.getMember("kind").asString());
        Value tabs = m.invokeMember("tabsBySlot");
        for (String slot : new String[]{"tl", "tr", "bl", "br"}) {
            Value arr = tabs.invokeMember("get", slot);
            assertEquals(0, arr.getArraySize(), "slot " + slot + " should start empty");
        }
        Value active = m.invokeMember("activeUuid");
        assertTrue(active == null || active.isNull());
    }

    // ── Spawn ───────────────────────────────────────────────────────────

    @Test
    void spawnPinnedPlacesIntoDefaultSlotWhenPaneIdIsRoot() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedPinned", """
                ({ widgetInstanceId: 'doc:1', widgetKind: 'DocView',
                   title: 'Intro', to: { paneId: '_', tabIndex: 0 } })""");
        Value tabs = tabsAt(m, "tl");
        assertEquals(1, tabs.getArraySize());
        assertEquals("doc:1", tabs.getArrayElement(0)
                                  .getMember("widgetInstanceUuid").asString());
        assertEquals(true, tabs.getArrayElement(0).getMember("pinned").asBoolean());
        assertEquals(false, m.invokeMember("isEmpty").asBoolean());
    }

    @Test
    void spawnFromPickerIsNonPinned() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", """
                ({ widgetInstanceId: 'mov:1', widgetKind: 'MovingAnimal',
                   to: { paneId: '_', tabIndex: 0 } })""");
        Value tabs = tabsAt(m, "tl");
        assertEquals(false, tabs.getArrayElement(0).getMember("pinned").asBoolean());
    }

    @Test
    void spawnIsIdempotentByWidgetInstanceUuid() {
        Value m = freshModel();
        String payload = "({ widgetInstanceId: 'doc:1', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })";
        applyEvent(m, "WidgetSpawnedPinned",     payload);
        applyEvent(m, "WidgetSpawnedFromPicker", payload);
        assertEquals(1, tabsAt(m, "tl").getArraySize(),
                "duplicate spawn of same uuid is a no-op");
    }

    @Test
    void spawnPreservesTabOrderViaTabIndex() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'a', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'b', widgetKind: 'B', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'c', widgetKind: 'C', to: { paneId: '_', tabIndex: 1 } })");
        Value tabs = tabsAt(m, "tl");
        assertEquals(3, tabs.getArraySize());
        // After b is inserted at 0, then c at 1: order is [b, c, a]
        assertEquals("b", tabs.getArrayElement(0).getMember("widgetInstanceUuid").asString());
        assertEquals("c", tabs.getArrayElement(1).getMember("widgetInstanceUuid").asString());
        assertEquals("a", tabs.getArrayElement(2).getMember("widgetInstanceUuid").asString());
    }

    // ── Move ────────────────────────────────────────────────────────────

    @Test
    void tabMovedRelocatesToDestPaneAfterSplit() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'a', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })");
        // Move 'a' from tl to tr.
        applyEvent(m, "TabMoved", "({ widgetInstanceId: 'a', to: { paneId: '_1_2', tabIndex: 0 } })");
        assertEquals(0, tabsAt(m, "tl").getArraySize());
        Value tr = tabsAt(m, "tr");
        assertEquals(1, tr.getArraySize());
        assertEquals("a", tr.getArrayElement(0).getMember("widgetInstanceUuid").asString());
    }

    @Test
    void tabMovedForUnknownUuidIsNoOp() {
        Value m = freshModel();
        applyEvent(m, "TabMoved", "({ widgetInstanceId: 'ghost', to: { paneId: '_1_2', tabIndex: 0 } })");
        assertEquals(true, m.invokeMember("isEmpty").asBoolean());
    }

    // ── Close ───────────────────────────────────────────────────────────

    @Test
    void tabClosedRemovesAndClearsActiveIfMatched() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'a', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "WorkspaceActiveChanged", "({ to: { widgetInstanceId: 'a' } })");
        applyEvent(m, "TabClosed",              "({ widgetInstanceId: 'a' })");
        assertEquals(0, tabsAt(m, "tl").getArraySize());
        Value active = m.invokeMember("activeUuid");
        assertTrue(active == null || active.isNull(),
                "active uuid cleared when the active tab closes");
    }

    @Test
    void closeOfUnknownUuidIsNoOp() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'a', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "TabClosed", "({ widgetInstanceId: 'ghost' })");
        assertEquals(1, tabsAt(m, "tl").getArraySize());
    }

    // ── Spawn → close folds to empty ────────────────────────────────────

    @Test
    void spawnThenCloseFoldsToNoTab() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'a', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "TabClosed",               "({ widgetInstanceId: 'a' })");
        assertEquals(true, m.invokeMember("isEmpty").asBoolean(),
                "virtual replay collapses spawn-then-close to no DOM work");
    }

    // ── WorkspaceActiveChanged ──────────────────────────────────────────

    @Test
    void workspaceActiveChangedSetsActiveUuid() {
        Value m = freshModel();
        applyEvent(m, "WorkspaceActiveChanged", "({ to: { widgetInstanceId: 'doc:1' } })");
        assertEquals("doc:1", m.invokeMember("activeUuid").asString());
    }

    @Test
    void workspaceActiveChangedToNullIsNoOp() {
        Value m = freshModel();
        applyEvent(m, "WorkspaceActiveChanged", "({ to: { widgetInstanceId: 'a' } })");
        applyEvent(m, "WorkspaceActiveChanged", "({ to: null })");
        // Last-set value retained; null target is ignored.
        assertEquals("a", m.invokeMember("activeUuid").asString());
    }

    // ── Splits + merges ────────────────────────────────────────────────

    @Test
    void splitOnLeafReplacesItWithASplitNode() {
        // Start with a fresh model where root is already a 2x2 split. To
        // exercise split on a leaf, target one of the leaves at '_1_1'.
        Value m = freshModel();
        applyEvent(m, "SplitCreated", "({ paneId: '_1_1', orientation: 'horizontal' })");
        // After the split, the tl leaf becomes a split with two leaves;
        // one keeps slotId 'tl', one is a fresh 'sp_1'.
        Value snap = m.invokeMember("inspect");
        Value layout = snap.getMember("layout");
        // Walk down: layout (split) → children[0].pane (split) → children[0].pane (split since we split _1_1)
        Value top = layout.getMember("children").getArrayElement(0).getMember("pane");
        Value tlNow = top.getMember("children").getArrayElement(0).getMember("pane");
        assertEquals("split", tlNow.getMember("kind").asString());
        // New slot id minted: sp_1
        Value newChild = tlNow.getMember("children").getArrayElement(1).getMember("pane");
        assertEquals("leaf", newChild.getMember("kind").asString());
        assertEquals("sp_1", newChild.getMember("slotId").asString());
        // Tabs for sp_1 initialised empty
        assertEquals(0, tabsAt(m, "sp_1").getArraySize());
    }

    @Test
    void splitMintsStableIdsAcrossSplits() {
        Value m = freshModel();
        applyEvent(m, "SplitCreated", "({ paneId: '_1_1', orientation: 'horizontal' })");
        applyEvent(m, "SplitCreated", "({ paneId: '_1_2', orientation: 'horizontal' })");
        assertNotNull(m.invokeMember("tabsBySlot").invokeMember("get", "sp_1"));
        assertNotNull(m.invokeMember("tabsBySlot").invokeMember("get", "sp_2"));
    }

    @Test
    void splitRatioChangedUpdatesFirstChildRatio() {
        Value m = freshModel();
        // Default 2x2 — root is split with two children at ratio 0.5/0.5.
        applyEvent(m, "SplitRatioChanged", "({ paneId: '_', ratio: 0.3 })");
        Value layout = m.invokeMember("layout");
        Value kids = layout.getMember("children");
        assertEquals(0.3, kids.getArrayElement(0).getMember("ratio").asDouble(), 0.001);
        assertEquals(0.7, kids.getArrayElement(1).getMember("ratio").asDouble(), 0.001);
    }

    @Test
    void splitRatioChangedClampsOutOfBoundsValues() {
        Value m = freshModel();
        applyEvent(m, "SplitRatioChanged", "({ paneId: '_', ratio: 0 })");
        assertEquals(0.5, m.invokeMember("layout").getMember("children")
                            .getArrayElement(0).getMember("ratio").asDouble(), 0.001);
        applyEvent(m, "SplitRatioChanged", "({ paneId: '_', ratio: 1.0 })");
        assertEquals(0.5, m.invokeMember("layout").getMember("children")
                            .getArrayElement(0).getMember("ratio").asDouble(), 0.001);
        applyEvent(m, "SplitRatioChanged", "({ paneId: '_', ratio: 'bad' })");
        assertEquals(0.5, m.invokeMember("layout").getMember("children")
                            .getArrayElement(0).getMember("ratio").asDouble(), 0.001);
    }

    @Test
    void splitRatioChangedOnLeafPaneIsNoOp() {
        Value m = freshModel();
        // _1_1 is a leaf (tl); SplitRatioChanged should ignore.
        applyEvent(m, "SplitRatioChanged", "({ paneId: '_1_1', ratio: 0.4 })");
        // Layout unchanged.
        Value layout = m.invokeMember("layout");
        assertEquals(0.5, layout.getMember("children")
                            .getArrayElement(0).getMember("ratio").asDouble(), 0.001);
    }

    @Test
    void mergeCollapsesSplitNodeBackToFirstChildLeaf() {
        Value m = freshModel();
        applyEvent(m, "SplitCreated", "({ paneId: '_1_1', orientation: 'horizontal' })");
        // Spawn into the new pane sp_1 — but the spawn event records to.paneId='_'
        // by current emit convention, so simulate placing a tab in sp_1 via the
        // model's update path (move there from tl).
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'x', widgetKind: 'X', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'y', widgetKind: 'Y', to: { paneId: '_', tabIndex: 0 } })");
        // y went to tl. Move it to sp_1 (path _1_1_2 inside the nested split).
        applyEvent(m, "TabMoved", "({ widgetInstanceId: 'y', to: { paneId: '_1_1_2', tabIndex: 0 } })");
        assertEquals(1, tabsAt(m, "sp_1").getArraySize());
        // Merge the nested split back.
        applyEvent(m, "SplitMerged", "({ paneId: '_1_1' })");
        // After merge: kept slot is the first child ('tl'); sibling ('sp_1')
        // tabs are appended to tl.
        Value tlAfter = tabsAt(m, "tl");
        assertEquals(2, tlAfter.getArraySize(), "sp_1's tabs migrated into tl on merge");
        // sp_1 removed
        Value sp1After = m.invokeMember("tabsBySlot").invokeMember("get", "sp_1");
        assertTrue(sp1After == null || sp1After.isNull(),
                "sp_1 slot removed after merge");
    }

    // ── Unknown events silently ignored ─────────────────────────────────

    @Test
    void unknownEventNameIsSilentlyIgnored() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'a', widgetKind: 'A', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "FrobnicateBaz",           "({ irrelevant: true })");
        assertEquals(1, tabsAt(m, "tl").getArraySize());
    }

    @Test
    void applyOnFalsyEventIsNoOp() {
        Value m = freshModel();
        m.invokeMember("apply", js.eval("js", "null"));
        m.invokeMember("apply", js.eval("js", "undefined"));
        assertEquals(true, m.invokeMember("isEmpty").asBoolean());
    }

    // ── Snapshot round-trip ─────────────────────────────────────────────

    @Test
    void snapshotRoundTripPreservesEverything() {
        Value m = freshModel();
        applyEvent(m, "SplitCreated",            "({ paneId: '_1_1', orientation: 'horizontal' })");
        applyEvent(m, "WidgetSpawnedPinned",     "({ widgetInstanceId: 'doc:1', widgetKind: 'DocView', title: 'Intro', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'mov:1', widgetKind: 'MovingAnimal', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "TabMoved",                "({ widgetInstanceId: 'mov:1', to: { paneId: '_1_1_2', tabIndex: 0 } })");
        applyEvent(m, "WorkspaceActiveChanged",  "({ to: { widgetInstanceId: 'doc:1' } })");

        Value snap = m.invokeMember("toSnapshot");
        // Restore into a fresh model
        Value restored = global("WorkspaceStateModel").invokeMember("fromSnapshot", snap);

        // Spot-check restored state matches original
        assertEquals("doc:1", restored.invokeMember("activeUuid").asString());
        Value origTl = tabsAt(m, "tl");
        Value restoredTl = tabsAt(restored, "tl");
        assertEquals(origTl.getArraySize(), restoredTl.getArraySize());
        Value origSp1 = tabsAt(m, "sp_1");
        Value restoredSp1 = tabsAt(restored, "sp_1");
        assertEquals(origSp1.getArraySize(), restoredSp1.getArraySize());
        if (origSp1.getArraySize() > 0) {
            assertEquals(origSp1.getArrayElement(0).getMember("widgetInstanceUuid").asString(),
                         restoredSp1.getArrayElement(0).getMember("widgetInstanceUuid").asString());
        }
    }

    @Test
    void fromSnapshotRejectsUnknownSchemaVersion() {
        try {
            global("WorkspaceStateModel").invokeMember("fromSnapshot",
                    js.eval("js", "({ schemaVersion: 99 })"));
            org.junit.jupiter.api.Assertions.fail("should have thrown on schemaVersion 99");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("schemaVersion"),
                    "error mentions schemaVersion: " + e.getMessage());
        }
    }

    @Test
    void fromSnapshotNullReturnsFreshModel() {
        Value m = global("WorkspaceStateModel").invokeMember("fromSnapshot",
                js.eval("js", "null"));
        assertEquals(true, m.invokeMember("isEmpty").asBoolean());
    }

    // ── Fold composes ───────────────────────────────────────────────────

    @Test
    void spawnMoveCloseCycleFoldsToNoVisibleWork() {
        Value m = freshModel();
        applyEvent(m, "WidgetSpawnedFromPicker", "({ widgetInstanceId: 'x', widgetKind: 'X', to: { paneId: '_', tabIndex: 0 } })");
        applyEvent(m, "TabMoved",                "({ widgetInstanceId: 'x', to: { paneId: '_1_2', tabIndex: 0 } })");
        applyEvent(m, "TabMoved",                "({ widgetInstanceId: 'x', to: { paneId: '_2_1', tabIndex: 0 } })");
        applyEvent(m, "TabClosed",               "({ widgetInstanceId: 'x' })");
        assertEquals(true, m.invokeMember("isEmpty").asBoolean(),
                "virtual replay folds spawn-move-move-close to empty");
    }
}
