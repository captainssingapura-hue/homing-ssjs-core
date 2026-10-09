package hue.captains.singapura.js.homing.component.taxonomy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A casting's lineage, as the appendix orders it: its role's lineage at its component, then the
 * same at each of the component's parents, ending at the root on its own. Standalone is the role
 * catalogue's root, and no role is functional unless a branch it is filed under says so.
 */
class CastingTest {

    private static final Taxonomy SKETCH = new ReadTaxonomy().read(List.of(Sketch.Confirmation.INSTANCE), List.of());

    @Test
    void theLineage_rolesAtTheComponent_thenEachParent_endingAtTheRootOnItsOwn() {
        var go = new Casting(Sketch.PlainButton.INSTANCE, Sketch.Confirm.INSTANCE);
        assertEquals(List.of("plain-button as confirm", "plain-button as committing", "plain-button as doing", "plain-button",
                             "button as confirm", "button as committing", "button as doing", "button",
                             "control as confirm", "control as committing", "control as doing", "control",
                             "root as confirm", "root as committing", "root as doing", "root"),
                go.lineage(SKETCH).stream().map(Casting::toString).toList());
    }

    @Test
    void onItsOwn_theComponentsLineageAlone() {
        var alone = new Casting(Sketch.PlainButton.INSTANCE);
        assertTrue(alone.standalone());
        assertEquals(List.of("plain-button", "button", "control", "root"), alone.lineage(SKETCH).stream().map(Casting::toString).toList());
        assertEquals(alone, new Casting(Sketch.PlainButton.INSTANCE, Sketch.Confirm.INSTANCE).alone());
    }

    @Test
    void noRoleIsFunctional_unlessABranchSaysSo() {
        assertFalse(new Casting(Sketch.PlainButton.INSTANCE, Sketch.Confirm.INSTANCE).functional());
        assertEquals(List.of(Sketch.Confirm.INSTANCE, Sketch.Committing.INSTANCE, Sketch.Doing.INSTANCE, RoleRoot.INSTANCE),
                new Casting(Sketch.PlainButton.INSTANCE, Sketch.Confirm.INSTANCE).roleLineage());
    }
}
