package hue.captains.singapura.js.homing.design;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The sealed tree of targets holds its shape: its branches partition their properties, and every leaf is a value. */
class TreesTest {

    @Test
    void everyBranchPartitionsItsProperties() {
        assertEquals(List.of(), Trees.propertyCollisions());
    }

    @Test
    void everyLeafHasAnInstance_andRestAmongItsStates_andNoCrossLeafShorthand() {
        List<Target> leaves = Target.leaves();
        assertEquals(36, leaves.size(), "the tree's leaves");
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

    /** A shadow and a background are drawn in their own colours, registered where CSS has no property for them. */
    @Test
    void aShadowAndABackground_areDrawnInTheirOwnColours() {
        assertEquals(List.of(Target.Color.BoxShadow.INSTANCE), Target.Shape.Shadow.INSTANCE.colouredBy());
        assertEquals(List.of(Target.Color.Background.INSTANCE), Target.Shape.Background.INSTANCE.colouredBy());
        assertEquals(List.of("@property --box-shadow-cast { syntax: \"<color>\"; inherits: false; initial-value: transparent; }",
                             "@property --box-shadow-glow { syntax: \"<color>\"; inherits: false; initial-value: transparent; }",
                             "@property --box-shadow-lit { syntax: \"<color>\"; inherits: false; initial-value: transparent; }"),
                Target.Color.BoxShadow.INSTANCE.registrations());
        assertEquals(List.of(), Target.Color.Border.INSTANCE.registrations(), "border-color is CSS's own: nothing to register");
        assertEquals("color-box-shadow", Target.Color.BoxShadow.INSTANCE.token());
        assertEquals("color-column-rule", Target.Color.ColumnRule.INSTANCE.token());
    }

    @Test
    void targetTokensCarryTheBranch() {
        assertEquals("color-background", Target.Color.Background.INSTANCE.token());
        assertEquals("motion-transform", Target.Motion.Transform.INSTANCE.token());
        assertEquals("animation-effect", Target.Animation.Effect.INSTANCE.token());
        assertEquals("none", AnimationEffect.None.INSTANCE.token(), "an effect's token, from its type");
        assertEquals("sound-cue",        Target.Sound.Cue.INSTANCE.token());
    }

    @Test
    void aTargetIsALeafOfTheSealedTree() {
        assertThrows(IllegalArgumentException.class, () -> Trees.requireTargetLeaf(Target.Color.class));
        Trees.requireTargetLeaf(Target.Color.Ink.class);
        assertEquals("on-danger", Trees.kebab("OnDanger"));
    }
}
