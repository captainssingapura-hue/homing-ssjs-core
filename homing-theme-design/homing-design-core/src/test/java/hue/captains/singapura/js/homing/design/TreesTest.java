package hue.captains.singapura.js.homing.design;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The two trees hold their shape: the sealed one partitions, the open one is guarded. */
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
        }
    }

    @Test
    void targetTokensCarryTheBranch() {
        assertEquals("color-surface",    Target.Color.Surface.INSTANCE.token());
        assertEquals("motion-transform", Target.Motion.Transform.INSTANCE.token());
        assertEquals("sound-cue",        Target.Sound.Cue.INSTANCE.token());
    }

    // ── the open tree ─────────────────────────────────────────────────────

    @Test
    void aProjectionKnowsItsCoordinates_andItsToken() {
        var dc = new Feedback.Danger.danger_color_surface();
        assertEquals(Feedback.Danger.class, dc.semantic());
        assertEquals(Target.Color.Surface.class, dc.target());
        assertEquals("danger-color-surface", dc.token());
        assertEquals("on-danger-color-ink", new Pairing.OnDanger.on_danger_color_ink().token());
        assertEquals("interactive-motion-transform", new Interaction.Interactive.interactive_motion_transform().token());
    }

    @Test
    void everyShippedProjection_isDeclaredInsideItsLeaf_withOneBranch() {
        int count = 0;
        for (Class<?> branch : List.of(Feedback.class, Emphasis.class, Layer.class, Interaction.class, Text.class, Pairing.class, Box.class, Brand.class, Structure.class)) {
            for (Class<?> leaf : branch.getDeclaredClasses()) {
                if (!leaf.isRecord()) continue;
                assertEquals(branch, Trees.branchOf(leaf), leaf.getName() + " has one branch, " + branch.getSimpleName());
                for (Class<?> projection : leaf.getDeclaredClasses()) {
                    var c = Trees.coordinates(projection);
                    assertEquals(leaf, c.semantic());
                    count++;
                }
            }
        }
        assertTrue(count >= 150, "the first vocabulary projects " + count + " classes");
    }

    // ── the guards ────────────────────────────────────────────────────────

    /** A branch as a coordinate. */
    record BranchAsCoordinate() implements DesignClass<Semantic, Target.Color.Ink> {}

    /** Two branches on one leaf. */
    record TwoBranches() implements Feedback, Emphasis {
        record two_branches_color_ink() implements DesignClass<TwoBranches, Target.Color.Ink> {}
    }

    /** A leaf declared correctly, but its projection declared elsewhere. */
    record Lonely() implements Feedback {}
    record lonely_color_ink() implements DesignClass<Lonely, Target.Color.Ink> {}
    /** Named for the wrong coordinates. */
    record Misnamed() implements Feedback {
        record danger_color_ink() implements DesignClass<Misnamed, Target.Color.Ink> {}
    }

    @Test
    void theGuardsRefuse() {
        var e1 = assertThrows(IllegalArgumentException.class, () -> Trees.coordinates(BranchAsCoordinate.class));
        assertTrue(e1.getMessage().contains("leaf"), e1.getMessage());
        var e2 = assertThrows(IllegalArgumentException.class, () -> Trees.coordinates(TwoBranches.two_branches_color_ink.class));
        assertTrue(e2.getMessage().contains("exactly one branch"), e2.getMessage());
        var e3 = assertThrows(IllegalArgumentException.class, () -> Trees.coordinates(lonely_color_ink.class));
        assertTrue(e3.getMessage().contains("declared inside its semantic leaf"), e3.getMessage());
        var e4 = assertThrows(IllegalArgumentException.class, () -> Trees.coordinates(Misnamed.danger_color_ink.class));
        assertTrue(e4.getMessage().contains("expected misnamed_color_ink"), e4.getMessage());
    }

    @Test
    void theTargetIsTheGroup_derivedFromTheVocabulary() {
        Vocabulary.register(Feedback.Danger.class, Pairing.OnDanger.class);
        var surface = Target.Color.Surface.INSTANCE;
        assertTrue(surface.cssClasses().stream().anyMatch(c -> c instanceof Feedback.Danger.danger_color_surface), "the surface group carries danger's surface");
        assertTrue(surface.cssClasses().stream().noneMatch(c -> c.getClass() == Feedback.Danger.danger_color_ink.class), "and not its ink");
        assertEquals(surface, new Feedback.Danger.danger_color_surface().group(), "a design class's group is its target");
        assertEquals(Target.Color.Ink.INSTANCE, new Pairing.OnDanger.on_danger_color_ink().group());
    }

    @Test
    void kebab() {
        assertEquals("on-danger", Trees.kebab("OnDanger"));
        assertEquals("color-surface", Trees.kebab("Color_Surface"));
        assertEquals("drop-target", Trees.kebab("DropTarget"));
    }
}
