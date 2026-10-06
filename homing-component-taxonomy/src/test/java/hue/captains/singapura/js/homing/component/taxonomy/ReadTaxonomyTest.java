package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Sketch.AlternatingButton;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Button;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Caption;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Container;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Control;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.DangerButton;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Dialog;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.PlainButton;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Text;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The taxonomy as read: only components are declared - the kinds are reached through their
 * parents, the components that play roles through the roles - and each role becomes a part, its
 * owner appended, falling back through its base and never its owner. What breaks a taxonomy is
 * refused, every problem at once.
 */
class ReadTaxonomyTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    private static Taxonomy read(Component<?>... declared) { return READ.read(List.of(declared)); }

    private static Set<Rule> refused(Component<?>... declared) {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(declared));
        return e.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet());
    }

    @Test
    void declaringADialog_bringsWhatItIsAndWhatPlaysItsRoles() {
        Taxonomy t = read(Dialog.INSTANCE);
        assertEquals(List.of(Dialog.INSTANCE, PlainButton.INSTANCE, Caption.INSTANCE), t.components(),
                "the dialog, then the components that play its roles, reached through them");
        assertEquals(Set.of(Container.INSTANCE, Control.INSTANCE, Button.INSTANCE, Text.INSTANCE), Set.copyOf(t.kinds()),
                "the kinds, reached through the parents");
        assertTrue(t.kinds().indexOf(Control.INSTANCE) < t.kinds().indexOf(Button.INSTANCE), "a parent before its children");
    }

    @Test
    void eachRoleIsAPart_itsOwnerAppended() {
        Taxonomy t = read(Dialog.INSTANCE);
        assertEquals(List.of(
                new Part<>(Dialog.INSTANCE, PlainButton.INSTANCE, Dialog.Ok.INSTANCE),
                new Part<>(Dialog.INSTANCE, PlainButton.INSTANCE, Dialog.Cancel.INSTANCE),
                new Part<>(Dialog.INSTANCE, Caption.INSTANCE, Dialog.Title.INSTANCE)), t.partsOf(Dialog.INSTANCE));
        assertEquals("ok", t.parts().get(0).name().value(), "a part is named by its role, not by what plays it");
    }

    @Test
    void aPartFallsBackThroughItsBase_neverItsOwner() {
        Taxonomy t = read(Dialog.INSTANCE);
        var ok = t.partsOf(Dialog.INSTANCE).get(0);
        assertEquals(List.of(ok, PlainButton.INSTANCE, Button.INSTANCE, Control.INSTANCE, Root.INSTANCE), t.fallback(ok));
        assertFalse(t.fallback(ok).contains(Dialog.INSTANCE), "nothing of the dialog's own look reaches its OK button");
        assertEquals(List.of(Dialog.INSTANCE, Container.INSTANCE, Root.INSTANCE), t.fallback(Dialog.INSTANCE));
        assertEquals(List.of(Root.INSTANCE), t.fallback(Root.INSTANCE));
    }

    @Test
    void tokens_aNodesOwnName_aPartsOwnersAndItsRole() {
        Taxonomy t = read(Dialog.INSTANCE, AlternatingButton.INSTANCE);
        assertEquals("plain-button", t.token(PlainButton.INSTANCE));
        assertEquals("button", t.token(Button.INSTANCE));
        assertEquals(List.of("dialog-ok", "dialog-cancel", "dialog-title"),
                t.partsOf(Dialog.INSTANCE).stream().map(t::token).toList());
        assertEquals("alternating-button-label", t.token(t.partsOf(AlternatingButton.INSTANCE).get(0)));
    }

    @Test
    void aBranchsChildren_derived_neverListed() {
        Taxonomy t = read(PlainButton.INSTANCE, DangerButton.INSTANCE, AlternatingButton.INSTANCE);
        assertEquals(List.of(PlainButton.INSTANCE, DangerButton.INSTANCE, AlternatingButton.INSTANCE), t.children(Button.INSTANCE));
        assertEquals(List.of(Control.INSTANCE, Text.INSTANCE), t.children(Root.INSTANCE), "kinds first, each in reading order");
    }

    @Test
    void onlyALeafIsConcrete_byType() {
        assertFalse(Branch.class.isAssignableFrom(Component.class), "a component is not a branch, so nothing can be named under it");
        assertTrue(Branch.class.isAssignableFrom(Kind.class));
    }

    @Test
    void aRoleNotItsOwn_orListedTwice_orPlayedByNobody_isRefused() {
        assertEquals(Set.of(Rule.ROLE_NOT_ITS_OWN), refused(Broken.Thief.INSTANCE));
        assertEquals(Set.of(Rule.ROLE_TWICE), refused(Broken.Stutter.INSTANCE));
        assertEquals(Set.of(Rule.NO_BASE), refused(Broken.Vacant.INSTANCE));
    }

    @Test
    void aComponentThatIsAPartOfItself_isRefused() {
        assertEquals(Set.of(Rule.COMPOSITION_CYCLE), refused(Broken.Mirror.INSTANCE));
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Ping.INSTANCE));
        assertEquals(1, e.problems().size(), "one cycle, reported once: " + e.problems());
        assertTrue(e.problems().get(0).says().contains("Ping → Pong → Ping"), e.problems().get(0).says());
    }

    @Test
    void parentsThatComeBackOnThemselves_orNoParent_areRefused() {
        assertEquals(Set.of(Rule.PARENT_CYCLE), refused(Broken.Chick.INSTANCE));
        assertEquals(Set.of(Rule.NO_PARENT), refused(Broken.Orphan.INSTANCE));
    }

    @Test
    void twoNodesWithOneToken_areRefused_bothNamed() {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Here.Badge.INSTANCE, Broken.There.Badge.INSTANCE));
        assertEquals(List.of(Rule.TOKEN_TWICE), e.problems().stream().map(TaxonomyProblem::rule).toList());
        assertTrue(e.problems().get(0).says().contains("'badge'"), e.problems().get(0).says());
    }

    @Test
    void everyProblemAtOnce() {
        var e = assertThrows(RefusedTaxonomy.class,
                () -> read(Broken.Thief.INSTANCE, Broken.Mirror.INSTANCE, Broken.Orphan.INSTANCE));
        assertEquals(Set.of(Rule.ROLE_NOT_ITS_OWN, Rule.COMPOSITION_CYCLE, Rule.NO_PARENT),
                e.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet()));
    }
}
