package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Fixed;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.AtMost;
import hue.captains.singapura.js.homing.component.taxonomy.Cardinality.Varying;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Doing;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Add;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Button;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Cancel;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Caption;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Card;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Committing;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Confirm;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Confirmation;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Container;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Control;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Heading;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Naming;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Open;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.PlainButton;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.PlainCard;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.ProfileCard;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Shelf;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Text;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Title;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyFinding.Sign;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reading the hypothetical {@link Sketch} house and its {@link TradingDesk} extension: what is
 * reached and how it is laid out, what every slot becomes, the roles shared and filed, what is
 * refused - every rule, every problem at once - and what is only noticed.
 */
class ReadTaxonomyTest {

    private static final ReadTaxonomy READ = new ReadTaxonomy();

    private static Taxonomy read(Component<?>... declared) { return READ.read(List.of(declared)); }

    private static Set<Rule> refused(Component<?>... declared) {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(declared));
        return e.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet());
    }

    // ── what is reached ────────────────────────────────────────────────────

    @Test
    void declaringAConfirmation_bringsWhatItIs_whatPlaysItsParts_andTheRolesTheyName() {
        Taxonomy t = read(Confirmation.INSTANCE);
        assertEquals(List.of(Confirmation.INSTANCE, Caption.INSTANCE, PlainButton.INSTANCE), t.components(),
                "the confirmation, then what plays its parts, reached through the slots");
        assertEquals(List.of(Container.INSTANCE, Text.INSTANCE, Control.INSTANCE, Button.INSTANCE), t.branches(),
                "the branches, reached through the parents, by level - every parent before its children - the root left out");
        assertEquals(List.of(Title.INSTANCE, Confirm.INSTANCE, Cancel.INSTANCE), t.roles(), "the roles its slots name, in order");
        assertEquals(List.of(Sketch.Saying.INSTANCE, Doing.INSTANCE, Naming.INSTANCE, Committing.INSTANCE), t.roleBranches(),
                "the role branches, reached through the roles, by level, the root left out");
    }

    @Test
    void eachSlotIsAPart_itsOwnerAppended() {
        Taxonomy t = read(Confirmation.INSTANCE);
        assertEquals(List.of(
                new Part<>(Confirmation.INSTANCE, Title.INSTANCE, Caption.INSTANCE, new Fixed(1)),
                new Part<>(Confirmation.INSTANCE, Confirm.INSTANCE, PlainButton.INSTANCE, new Fixed(1)),
                new Part<>(Confirmation.INSTANCE, Cancel.INSTANCE, PlainButton.INSTANCE, new Varying(0, new AtMost(1)))),
                t.partsOf(Confirmation.INSTANCE));
        assertEquals("confirm", t.partsOf(Confirmation.INSTANCE).get(1).name().value(), "a part is named by its role, not by what plays it");
    }

    @Test
    void aPartFallsBackThroughItsBase_neverItsOwner() {
        Taxonomy t = read(Confirmation.INSTANCE);
        var confirm = t.partsOf(Confirmation.INSTANCE).get(1);
        assertEquals(List.of(confirm, PlainButton.INSTANCE, Button.INSTANCE, Control.INSTANCE, Root.INSTANCE), t.fallback(confirm));
        assertFalse(t.fallback(confirm).contains(Confirmation.INSTANCE), "nothing of the confirmation's own look reaches its confirm");
        assertEquals(List.of(Confirmation.INSTANCE, Container.INSTANCE, Root.INSTANCE), t.fallback(Confirmation.INSTANCE));
        assertEquals(List.of(Root.INSTANCE), t.fallback(Root.INSTANCE));
    }

    @Test
    void tokens_aNodesOwnName_aPartsOwnersAndItsRole() {
        Taxonomy t = READ.read(Sketch.DECLARED);
        assertEquals("plain-button", t.token(PlainButton.INSTANCE));
        assertEquals(List.of("profile-card-title", "profile-card-name", "profile-card-tag", "profile-card-open"),
                t.partsOf(ProfileCard.INSTANCE).stream().map(t::token).toList());
        assertEquals(List.of("confirmation-title", "confirmation-confirm", "confirmation-cancel"),
                t.partsOf(Confirmation.INSTANCE).stream().map(t::token).toList());
    }

    @Test
    void aBranchsChildren_derived_neverListed() {
        Taxonomy t = READ.read(Sketch.DECLARED);
        assertEquals(List.of(PlainCard.INSTANCE, ProfileCard.INSTANCE, Shelf.INSTANCE), t.children(Card.INSTANCE));
        assertEquals(List.of(Control.INSTANCE, Container.INSTANCE, Text.INSTANCE, Sketch.Mark.INSTANCE), t.children(Root.INSTANCE),
                "branches first, each in reading order");
    }

    // ── roles: shared, and filed ───────────────────────────────────────────

    @Test
    void aRoleIsShared_eachOwnerSayingWhatPlaysItAndHowMany() {
        Taxonomy t = READ.read(Sketch.DECLARED);
        var titles = t.partsNaming(Title.INSTANCE);
        assertEquals(List.of(ProfileCard.INSTANCE, Confirmation.INSTANCE), titles.stream().map(Part::owner).toList(), "one role, two owners");
        assertEquals(List.of(Heading.INSTANCE, Caption.INSTANCE), titles.stream().map(Part::base).toList(), "each its own player");
        assertEquals(List.of("profile-card-title", "confirmation-title"), titles.stream().map(Part::token).toList(), "each its own part");
    }

    @Test
    void theCatalogue_branchesThenRoles_underEachBranch_atAnyDepth() {
        Taxonomy t = READ.read(Sketch.DECLARED, Sketch.CATALOGUE);
        assertEquals(List.of(Sketch.Saying.INSTANCE, Doing.INSTANCE, Sketch.Shaping.INSTANCE), t.children(RoleRoot.INSTANCE),
                "the top branches, in the order first reached; the root not its own child");
        assertEquals(List.of(Sketch.Going.INSTANCE, Committing.INSTANCE, Sketch.Managing.INSTANCE), t.children(Doing.INSTANCE));
        assertEquals(List.of(Confirm.INSTANCE, Cancel.INSTANCE), t.children(Committing.INSTANCE), "roles at the third level");
        assertEquals(List.of(Sketch.Entry.INSTANCE), t.children(Sketch.Shaping.INSTANCE), "and one at the second");
        assertTrue(t.roles().contains(Sketch.Summary.INSTANCE), "a catalogued role no component names is kept");
        assertTrue(t.roleBranches().stream().noneMatch(b -> b instanceof RoleRoot), "the root is no branch of the list: it is above them all");
        assertEquals(List.of(1, 1, 1, 2, 2, 2, 2, 2, 2), t.roleBranches().stream().map(RoleBranch::level).toList(), "each branch knows its level");
    }

    @Test
    void anExtension_aLeafUnderAHouseBranch_rolesInAHouseBranch_andABranchOfItsOwn_atAnyLevel() {
        var declared = new ArrayList<Component<?>>(Sketch.DECLARED);
        declared.add(TradingDesk.OrderTicket.INSTANCE);
        var roles = new ArrayList<Role<?>>(Sketch.CATALOGUE);
        roles.addAll(TradingDesk.CATALOGUE);
        Taxonomy t = READ.read(declared, roles);
        assertTrue(t.children(Card.INSTANCE).contains(TradingDesk.OrderTicket.INSTANCE), "classification is open: its leaf under the house's branch");
        assertTrue(t.children(Button.INSTANCE).contains(TradingDesk.TradeButton.INSTANCE), "reached through its own slots");
        assertEquals(List.of(TradingDesk.Buy.INSTANCE, TradingDesk.Sell.INSTANCE),
                t.children(Committing.INSTANCE).stream().filter(n -> n.getClass().getEnclosingClass() == TradingDesk.class).toList(),
                "its roles filed in the house's branch");
        assertTrue(t.children(Doing.INSTANCE).contains(TradingDesk.Trading.INSTANCE), "and a branch of its own, deep in the house's catalogue");
        assertEquals(List.of(TradingDesk.Side.INSTANCE), t.children(TradingDesk.Trading.INSTANCE));
        assertEquals(List.of("order-ticket-title", "order-ticket-side", "order-ticket-buy", "order-ticket-sell", "order-ticket-cancel"),
                t.partsOf(TradingDesk.OrderTicket.INSTANCE).stream().map(Part::token).toList());
    }

    @Test
    void theShapeIsInTheTypes_levelsSealed_eachRootClosed() {
        assertFalse(ComponentBranch.class.isAssignableFrom(Component.class), "a component is not a branch, so nothing can be named under it");
        assertFalse(RoleBranch.class.isAssignableFrom(Role.class), "a role is not a branch, so nothing can be filed under it");
        assertEquals(9, ComponentBranch.class.getPermittedSubclasses().length, "levels 0 to 8, and no other");
        assertEquals(9, RoleBranch.class.getPermittedSubclasses().length);
        assertEquals(List.of(Root.class), List.of(L0_ComponentBranch.class.getPermittedSubclasses()), "one root, nobody adds another");
        assertEquals(List.of(RoleRoot.class), List.of(L0_RoleBranch.class.getPermittedSubclasses()));
        assertEquals(2, Sketch.Button.INSTANCE.level());
    }

    // ── what is refused ────────────────────────────────────────────────────

    @Test
    void noParent_andNoState() {
        assertEquals(Set.of(Rule.NO_PARENT), refused(Broken.Orphan.INSTANCE));
        assertEquals(Set.of(Rule.NO_PARENT), refused(Broken.Strays.INSTANCE), "a role filed under nothing");
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Stateful.INSTANCE));
        assertEquals(List.of(Rule.NOT_STATELESS), e.problems().stream().map(TaxonomyProblem::rule).toList(), "a component with a field of its own");
        assertTrue(e.problems().get(0).says().contains("instance field 'label'"), e.problems().get(0).says());
        e = assertThrows(RefusedTaxonomy.class, () -> READ.read(List.of(), List.of(Broken.Helped.INSTANCE)));
        assertEquals(List.of(Rule.NOT_STATELESS), e.problems().stream().map(TaxonomyProblem::rule).toList(), "a role with a static helper, as jOntology refuses one");
    }

    @Test
    void aSlotPlayedByNothing_orInNoRole_orARoleNamedTwice() {
        assertEquals(Set.of(Rule.NO_BASE), refused(Broken.Vacant.INSTANCE));
        assertEquals(Set.of(Rule.NO_ROLE), refused(Broken.Nameless.INSTANCE));
        assertEquals(Set.of(Rule.ROLE_TWICE), refused(Broken.Stutter.INSTANCE));
    }

    @Test
    void aPartOfItself_isRefusedOnTheTypes_whateverTheCount() {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Mirror.INSTANCE));
        assertEquals(List.of(Rule.COMPOSITION_CYCLE), e.problems().stream().map(TaxonomyProblem::rule).toList(),
                "listed optionally, and still a cycle");
        e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Ping.INSTANCE));
        assertEquals(1, e.problems().size(), "one cycle, reported once: " + e.problems());
        assertTrue(e.problems().get(0).says().contains("Ping → Pong → Ping"), e.problems().get(0).says());
    }

    @Test
    void oneWordOneThing_tokens_roleNames_andRolesNamedAsNodes() {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Here.Badge.INSTANCE, Broken.There.Badge.INSTANCE));
        assertEquals(List.of(Rule.TOKEN_TWICE), e.problems().stream().map(TaxonomyProblem::rule).toList());
        assertTrue(e.problems().get(0).says().contains("'badge'"), e.problems().get(0).says());

        e = assertThrows(RefusedTaxonomy.class, () -> read(Confirmation.INSTANCE, Broken.Doubled.INSTANCE));
        assertEquals(List.of(Rule.ROLE_NAME_TWICE), e.problems().stream().map(TaxonomyProblem::rule).toList());
        assertTrue(e.problems().get(0).says().contains("'title'"), e.problems().get(0).says());

        e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Labelled.INSTANCE));
        assertEquals(List.of(Rule.ROLE_NAMES_A_NODE), e.problems().stream().map(TaxonomyProblem::rule).toList());
        assertTrue(e.problems().get(0).says().contains("is named as the component"), e.problems().get(0).says());
    }

    @Test
    void aCountThatIsNoCardinality_isAProblemOfTheComponentThatSaidIt() {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Nothing.INSTANCE, Broken.Lopsided.INSTANCE, Broken.Flat.INSTANCE));
        assertEquals(List.of(Rule.BAD_CARDINALITY, Rule.BAD_CARDINALITY, Rule.BAD_CARDINALITY),
                e.problems().stream().map(TaxonomyProblem::rule).toList(), "each component's own, none hiding another");
        assertEquals(List.of("Nothing: 0 is no count: a role played no times is no role",
                             "Lopsided: 3..1 is no range: the most is below the least",
                             "Flat: 2..2 is no range: say exactly(2)"),
                e.problems().stream().map(TaxonomyProblem::says).toList());
    }

    @Test
    void everyProblemAtOnce() {
        var e = assertThrows(RefusedTaxonomy.class, () -> read(Broken.Orphan.INSTANCE, Broken.Mirror.INSTANCE, Broken.Stutter.INSTANCE,
                Broken.Lopsided.INSTANCE, Broken.Labelled.INSTANCE, Broken.Doubled.INSTANCE));
        assertEquals(Set.of(Rule.NO_PARENT, Rule.COMPOSITION_CYCLE, Rule.ROLE_TWICE, Rule.BAD_CARDINALITY, Rule.ROLE_NAMES_A_NODE,
                            Rule.ROLE_NAME_TWICE),
                e.problems().stream().map(TaxonomyProblem::rule).collect(Collectors.toSet()));
    }

    // ── what is only noticed ───────────────────────────────────────────────

    @Test
    void signs_notFaults() {
        Taxonomy t = READ.read(Sketch.DECLARED, Sketch.CATALOGUE);
        var findings = t.findings();
        assertEquals(List.of(Sign.ALL_OPTIONAL, Sign.ROLE_UNNAMED, Sign.ROLE_PLAYED_VARIOUSLY),
                findings.stream().map(TaxonomyFinding::sign).sorted().toList(), findings.toString());
        assertEquals("Shelf: every part is optional - a branch missing its plain leaf?",
                findings.stream().filter(f -> f.sign() == Sign.ALL_OPTIONAL).findFirst().orElseThrow().says(),
                "not the plain card, which has no parts at all");
        assertEquals("Title is played by Heading in ProfileCard, Caption in Confirmation",
                findings.stream().filter(f -> f.sign() == Sign.ROLE_PLAYED_VARIOUSLY).findFirst().orElseThrow().says());
        assertEquals("Summary is named by no component",
                findings.stream().filter(f -> f.sign() == Sign.ROLE_UNNAMED).findFirst().orElseThrow().says());
    }
}
