package hue.captains.singapura.js.homing.design;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The two trees hold their shape: the sealed one partitions, the open one is guarded, and a pair is a value. */
class TreesTest {

    // ── the sealed tree ───────────────────────────────────────────────────

    @Test
    void everyBranchPartitionsItsProperties() {
        assertEquals(List.of(), Trees.propertyCollisions());
    }

    @Test
    void everyLeafHasAnInstance_andRestAmongItsStates_andNoCrossLeafShorthand() {
        List<Target> leaves = Target.leaves();
        assertTrue(leaves.size() >= 25, "the tree has " + leaves.size() + " leaves");
        var tokens = new HashSet<String>();
        for (Target t : leaves) {
            assertTrue(t.states().contains(State.REST), t.token() + " offers REST");
            assertTrue(tokens.add(t.token()), t.token() + " is unique");
            for (String p : t.properties())
                assertTrue(!p.equals("border") && !p.equals("background") && !p.equals("outline") && !p.equals("text-decoration") && !p.equals("font"),
                        t.token() + " owns a shorthand that crosses leaves: " + p);
            if (t.carrier() != Carrier.CSS) assertTrue(t.properties().isEmpty(), t.token() + " is not CSS and owns no property");
            assertEquals(List.of(), ((hue.captains.singapura.js.homing.core.CssGroup<?>) t).cssClasses(), t.token() + " declares no class of its own");
        }
    }

    @Test
    void targetTokensCarryTheBranch() {
        assertEquals("color-surface",    Target.Color.Surface.INSTANCE.token());
        assertEquals("motion-transform", Target.Motion.Transform.INSTANCE.token());
        assertEquals("sound-cue",        Target.Sound.Cue.INSTANCE.token());
    }

    // ── the pair ──────────────────────────────────────────────────────────

    @Test
    void aPair_isAValue_withItsTokenAndItsGroup() {
        var dc = DesignClass.of(Feedback.Danger.class, Target.Color.Surface.class);
        assertEquals("danger-color-surface", dc.cssName());
        assertEquals(Target.Color.Surface.INSTANCE, dc.group(), "the group is the target");
        assertEquals(dc, DesignClass.of(Feedback.Danger.class, Target.Color.Surface.class), "the same pair, asked twice, is one value");
        assertEquals("on-danger-color-ink", DesignClass.of(Pairing.OnDanger.class, Target.Color.Ink.class).cssName());
        assertEquals("interactive-motion-transform", DesignClass.of(Interaction.Interactive.class, Target.Motion.Transform.class).cssName());
        assertEquals("drop-target-shape-rule", DesignClass.of(Interaction.DropTarget.class, Target.Shape.Rule.class).cssName());
    }

    @Test
    void everyShippedLeaf_hasExactlyOneBranch() {
        int count = 0;
        for (Class<?> branch : List.of(Feedback.class, Emphasis.class, Layer.class, Interaction.class, Text.class, Pairing.class, Box.class, Brand.class, Structure.class)) {
            for (Class<?> leaf : branch.getDeclaredClasses()) {
                if (!leaf.isRecord()) continue;
                assertEquals(branch, Trees.branchOf(leaf), leaf.getName() + " has one branch, " + branch.getSimpleName());
                count++;
            }
        }
        assertTrue(count >= 40, "the first vocabulary has " + count + " leaves");
    }

    // ── the guards ────────────────────────────────────────────────────────

    /** Two branches on one leaf. */
    record TwoBranches() implements Feedback, Emphasis {}

    @Test
    void theGuardsRefuse() {
        var e1 = assertThrows(IllegalArgumentException.class, () -> DesignClass.of(Feedback.class.asSubclass(Semantic.class), Target.Color.Ink.class));
        assertTrue(e1.getMessage().contains("leaf"), e1.getMessage());
        var e2 = assertThrows(IllegalArgumentException.class, () -> DesignClass.of(TwoBranches.class, Target.Color.Ink.class));
        assertTrue(e2.getMessage().contains("exactly one branch"), e2.getMessage());
    }

    @Test
    void kebab() {
        assertEquals("on-danger", Trees.kebab("OnDanger"));
        assertEquals("color-surface", Trees.kebab("Color_Surface"));
        assertEquals("drop-target", Trees.kebab("DropTarget"));
    }
}
