package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Kinds.Burdened;
import hue.captains.singapura.js.homing.component.taxonomy.Kinds.Drawer;
import hue.captains.singapura.js.homing.component.taxonomy.Kinds.Hollow;
import hue.captains.singapura.js.homing.component.taxonomy.Kinds.Knob;
import hue.captains.singapura.js.homing.component.taxonomy.Kinds.Twofold;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A component's kind (D53): a leaf declares no parts, a composite at least one, and none is both. */
class KindTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    @Test
    void aCompositeHoldingALeaf_isRead() {
        Taxonomy t = READ.read(List.of(Drawer.INSTANCE));
        assertEquals(List.of(Drawer.INSTANCE, Knob.INSTANCE), t.components());
    }

    @Test
    void aLeafWithParts_isRefused() { assertRefused(Burdened.INSTANCE); }

    @Test
    void aCompositeWithNone_isRefused() { assertRefused(Hollow.INSTANCE); }

    @Test
    void aComponentOfBothKinds_isRefused() { assertRefused(Twofold.INSTANCE); }

    private static void assertRefused(Component<?> declared) {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(declared)));
        assertEquals(List.of(Rule.KIND_BADLY), e.problems().stream().map(TaxonomyProblem::rule).toList(), e.getMessage());
    }
}
