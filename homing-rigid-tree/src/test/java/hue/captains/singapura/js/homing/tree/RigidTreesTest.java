package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.js.homing.tree.dims.NameValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RigidTreesTest {

    private static NormalizedNode node(TreeLevel level, String label, NormalizedNode... kids) {
        return new NormalizedNode(level,
                Map.of(DisplayLabel.INSTANCE, new NameValue(label)), List.of(kids));
    }

    /** root L0 → child L1 → grandchild L2. */
    private static NormalizedNode threeDeep() {
        return node(TreeLevel.L0.INSTANCE, "root",
                node(TreeLevel.L1.INSTANCE, "child",
                        node(TreeLevel.L2.INSTANCE, "grandchild")));
    }

    // ── TreeLevel arithmetic ────────────────────────────────────────────────

    @Test
    void levelArithmetic() {
        assertEquals(8, TreeLevel.atDepth(5).shifted(3).depth());
        assertEquals(TreeLevel.L18.INSTANCE, TreeLevel.atDepth(18));
        assertTrue(TreeLevel.L18.INSTANCE.below().isEmpty(), "L18 is the cap");
        assertTrue(TreeLevel.L0.INSTANCE.above().isEmpty(),  "L0 is the root");
        assertEquals(TreeLevel.L3.INSTANCE, TreeLevel.L4.INSTANCE.above().orElseThrow());
        assertEquals(TreeLevel.L5.INSTANCE, TreeLevel.L4.INSTANCE.below().orElseThrow());
    }

    @Test
    void atDepthAndShiftRejectOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> TreeLevel.atDepth(19));
        assertThrows(IllegalArgumentException.class, () -> TreeLevel.atDepth(-1));
        assertThrows(IllegalArgumentException.class, () -> TreeLevel.L18.INSTANCE.shifted(1));
    }

    // ── Graft: pure recursive level shift ───────────────────────────────────

    @Test
    void shiftReLevelsEveryNodeByTheDelta() {
        NormalizedNode shifted = RigidTrees.shift(threeDeep(), 2);
        assertEquals(2, shifted.level().depth());
        assertEquals(3, shifted.children().get(0).level().depth());
        assertEquals(4, shifted.children().get(0).children().get(0).level().depth());
    }

    @Test
    void shiftByZeroIsIdentity() {
        NormalizedNode n = threeDeep();
        assertSame(n, RigidTrees.shift(n, 0));
    }

    @Test
    void shiftedToReRootsAtTarget() {
        NormalizedNode shifted = RigidTrees.shiftedTo(threeDeep(), TreeLevel.L5.INSTANCE);
        assertEquals(5, shifted.level().depth());
        assertEquals(7, shifted.children().get(0).children().get(0).level().depth());
    }

    @Test
    void graftUnderLandsOneBelowTheHost() {
        // host at L2 → grafted sub-tree root at L3, descendants L4, L5.
        NormalizedNode grafted = RigidTrees.graftUnder(threeDeep(), TreeLevel.L2.INSTANCE);
        assertEquals(3, grafted.level().depth());
        assertEquals(5, grafted.children().get(0).children().get(0).level().depth());
    }

    @Test
    void graftUnderTheCapIsAHardError() {
        assertThrows(IllegalArgumentException.class,
                () -> RigidTrees.graftUnder(threeDeep(), TreeLevel.L18.INSTANCE));
    }

    @Test
    void shiftPreservesDimensions() {
        NormalizedNode shifted = RigidTrees.shift(threeDeep(), 4);
        assertEquals("root", ((NameValue) shifted.dimensions().get(DisplayLabel.INSTANCE)).text());
    }
}
