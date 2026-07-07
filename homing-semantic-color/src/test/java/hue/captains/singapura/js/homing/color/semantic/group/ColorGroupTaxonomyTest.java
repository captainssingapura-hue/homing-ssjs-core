package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.WithDesc;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the ColorGroup taxonomy: the natures parent correctly through rebar's
 * ladder, and every group describes itself.
 */
class ColorGroupTaxonomyTest {

    @Test
    void l1NaturesParentToRoot() {
        assertSame(Colors.INSTANCE, Expressive.INSTANCE.parent());
        assertSame(Colors.INSTANCE, Encoding.INSTANCE.parent());
        assertSame(Colors.INSTANCE, Identity.INSTANCE.parent());
        assertSame(Colors.INSTANCE, Structural.INSTANCE.parent());
    }

    @Test
    void l2NaturesParentToTheirFamily() {
        assertSame(Expressive.INSTANCE, Affective.INSTANCE.parent());
        assertSame(Expressive.INSTANCE, Symbolic.INSTANCE.parent());
        assertSame(Encoding.INSTANCE, Categorical.INSTANCE.parent());
        assertSame(Encoding.INSTANCE, Sequential.INSTANCE.parent());
        assertSame(Encoding.INSTANCE, Diverging.INSTANCE.parent());
    }

    @Test
    void everyGroupDescribesItself() {
        List<WithDesc> groups = List.of(
                Colors.INSTANCE, Expressive.INSTANCE, Encoding.INSTANCE, Identity.INSTANCE,
                Structural.INSTANCE, Affective.INSTANCE, Symbolic.INSTANCE,
                Categorical.INSTANCE, Sequential.INSTANCE, Diverging.INSTANCE);
        for (WithDesc g : groups) {
            assertFalse(g.desc().summary().text().isBlank());
        }
    }
}
