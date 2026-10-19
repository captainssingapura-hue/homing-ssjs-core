package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Casts.Holder;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Idle;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Key;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Panel;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Placed;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Press;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Pusher;
import hue.captains.singapura.js.homing.component.taxonomy.Casts.Twice;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Role sets (RFC 0067, D55): a component cast in a role with a look of its own only in one its set gives, so its castings
 * are its own; a role that only places a part asks for no set; a set's roles are the taxonomy's, cast in yet or not.
 */
class CastTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    @Test
    void aKeyPressed_andAPlainThingSeated_areRead() {
        Taxonomy t = READ.read(List.of(Panel.INSTANCE));
        assertEquals(List.of(Panel.INSTANCE, Key.INSTANCE, Casts.Plain.INSTANCE), t.components());
    }

    @Test
    void aSetsRoles_areTheTaxonomys_castInYetOrNot() {
        Taxonomy t = READ.read(List.of(Key.INSTANCE));
        assertEquals(List.of(Press.INSTANCE), t.roles(), "the key is cast in nothing yet, and may be pressed");
    }

    @Test
    void castInARoleWithALookOfItsOwn_withNoRoleSet_isRefused() {
        var e = refused(Pusher.INSTANCE, Rule.ROLE_NOT_ALLOWED);
        assertTrue(e.getMessage().contains("pusher casts plain as press, a role with a look of its own, and plain has no role set")
                || e.getMessage().contains("Pusher casts Plain as Press"), e.getMessage());
    }

    @Test
    void castInARoleItsSetDoesNotGive_isRefused() {
        var e = refused(Holder.INSTANCE, Rule.ROLE_NOT_ALLOWED);
        assertTrue(e.getMessage().contains("a role its set, Pressing, does not allow"), e.getMessage());
    }

    @Test
    void aSetNamingARoleTwice_aRoleThatOnlyPlaces_orNone_isRefused() {
        assertTrue(refused(Twice.INSTANCE, Rule.ROLE_SET_BADLY).getMessage().contains("twice"));
        assertTrue(refused(Placed.INSTANCE, Rule.ROLE_SET_BADLY).getMessage().contains("only places a part"));
        assertTrue(refused(Idle.INSTANCE, Rule.ROLE_SET_BADLY).getMessage().contains("has no roles"));
    }

    private static RefusedTaxonomy refused(Component<?> declared, Rule rule) {
        var e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(declared)));
        assertEquals(List.of(rule), e.problems().stream().map(TaxonomyProblem::rule).toList(), e.getMessage());
        return e;
    }
}
