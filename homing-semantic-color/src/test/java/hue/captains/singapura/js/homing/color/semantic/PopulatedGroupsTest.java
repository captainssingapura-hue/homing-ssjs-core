package hue.captains.singapura.js.homing.color.semantic;

import hue.captains.singapura.js.homing.color.semantic.affective.Serene;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import hue.captains.singapura.js.homing.color.semantic.group.Identity;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;
import hue.captains.singapura.js.homing.color.semantic.group.Symbolic;
import hue.captains.singapura.js.homing.color.semantic.identity.Brand;
import hue.captains.singapura.js.homing.color.semantic.structural.Surface;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Danger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Each populated group's members are {@link SemanticColor} leaves parented to
 * their group. (Encoding groups are intentionally left empty — their members are
 * ramps/sets, not named colours.)
 */
class PopulatedGroupsTest {

    @Test
    void membersParentToTheirGroup() {
        assertSame(Structural.INSTANCE, Surface.INSTANCE.parent());
        assertSame(Identity.INSTANCE, Brand.INSTANCE.parent());
        assertSame(Symbolic.INSTANCE, Danger.INSTANCE.parent());
        assertSame(Affective.INSTANCE, Serene.INSTANCE.parent());
    }

    @Test
    void membersAreNamedSemanticColors() {
        assertEquals("surface", Surface.INSTANCE.name());
        assertEquals("brand", Brand.INSTANCE.name());
        assertEquals("danger", Danger.INSTANCE.name());
    }
}
