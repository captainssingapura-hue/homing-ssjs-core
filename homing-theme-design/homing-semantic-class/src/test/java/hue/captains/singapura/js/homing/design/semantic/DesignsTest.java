package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.DesignProblem.Rule;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.Bold;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BoldEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BoldMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BrokenEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BrokenMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.Elsewhere;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.HalfEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.LostEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.LostMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.MuteEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.MuteMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PingEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PingMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.Plain;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PlainEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PlainEdgeAgain;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PlainMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PongEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.PongMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.RawMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.ShoutedEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.ShoutedMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.UnschemedEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.UnschemedMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.WrongEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.WrongMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Box;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Edge;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Frame;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Mark;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Region;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Designs read, flattened and handed to a page (RFC 0067, appendix Virtual Leaves and Designs): gathered by the
 * design each concrete leaf names, complete over the branch and closed at every owned target in every theme,
 * refused otherwise; a sheet of their own; an object that wears a leaf's classes and refuses another branch's.
 */
class DesignsTest extends JsModuleTestBase {

    private static final Taxonomy TAXONOMY = DesigningTaxonomy.INSTANCE.read();
    private static final Dressed SKETCH = ResolvingThemes.BY_NAME.get("sketch");
    private static final Dressed DETAILED = ResolvingThemes.BY_NAME.get("detailed");
    private static final List<Dressed> THEMES = List.of(SKETCH, DETAILED);
    private static final List<ConcreteComponent<?, ?, ?>> GOOD =
            List.of(BoldMark.INSTANCE, PlainEdge.INSTANCE, BoldEdge.INSTANCE, PlainMark.INSTANCE);

    // ── read ───────────────────────────────────────────────────────────────

    @Test
    void designsAreGatheredByTheDesignEachConcreteLeafNames_inTheBranchsOrder() {
        var designs = ReadDesigns.INSTANCE.read(TAXONOMY, THEMES, GOOD);
        assertEquals(List.of("bold", "plain"), designs.stream().map(DesignRead::token).toList(), "in the order a concrete leaf of each is met");
        DesignRead bold = designs.get(0);
        assertEquals(Bold.INSTANCE, bold.design());
        assertEquals(Frame.INSTANCE, bold.branch());
        assertEquals(List.of(BoldEdge.INSTANCE, BoldMark.INSTANCE), bold.concretes(), "the branch's order, whatever the listing's");
        assertEquals(PlainMark.INSTANCE, designs.get(1).of(Mark.INSTANCE));
    }

    @Test
    void theVirtualLeaves_stayOutOfTheLookSheet() {
        assertEquals(List.of(new Casting(Box.INSTANCE)), TAXONOMY.castings(), "a virtual leaf is never cast");
        var flat = FlattenSheet.INSTANCE.flatten(TAXONOMY, SKETCH, "the look sheet");
        assertTrue(flat.unanswered().stream().anyMatch(u -> u.startsWith("box__")), "the box is asked as ever");
        assertFalse(flat.unanswered().stream().anyMatch(u -> u.startsWith("edge__") || u.startsWith("mark__")), "a virtual leaf is never asked");
        assertFalse(flat.css().contains("edge__") || flat.css().contains("mark__"), flat.css());
    }

    @Test
    void aConcreteLeafOfAnotherTaxonomysBranch_isPassedOver() {
        var mixed = new ArrayList<ConcreteComponent<?, ?, ?>>(GOOD);
        mixed.add(new ShabbyTaxonomy.ScruffyBare());
        assertEquals(2, ReadDesigns.INSTANCE.read(TAXONOMY, THEMES, mixed).size());
    }

    // ── refused ────────────────────────────────────────────────────────────

    @Test
    void aDesignMissingALeafOfItsBranch_isRefused() {
        assertRefused(List.of(Rule.LEAF_MISSING), List.of(new HalfEdge()));
    }

    @Test
    void aDesignWithTwoConcreteLeavesOfOneLeaf_isRefused() {
        assertRefused(List.of(Rule.LEAF_TWICE), with(GOOD, new PlainEdgeAgain()));
    }

    @Test
    void aConcreteLeafThatSaysNothingOfHowItIsDrawn_isRefused() {
        assertRefused(List.of(Rule.NOT_DRAWN), List.of(new MuteEdge(), new MuteMark()));
    }

    @Test
    void aConcreteLeafThatNamesNoDesign_isRefused() {
        assertRefused(List.of(Rule.UNREADABLE), with(GOOD, new RawMark()));
    }

    @Test
    void drawingsOfTargetsTheLeafDoesNotOwn_twice_orNotAtAll_areRefused() {
        assertRefused(List.of(Rule.DRAWN_TWICE, Rule.NOT_OWNED, Rule.UNDRAWN), List.of(new WrongEdge(), new WrongMark()));
    }

    @Test
    void aDrawingTakenFromAConcreteLeafOfAnotherLeaf_isRefused() {
        assertRefused(List.of(Rule.AS_ASTRAY), List.of(new LostEdge(), new LostMark()));
    }

    @Test
    void drawingsTakenRoundInACircle_areRefused() {
        assertRefused(List.of(Rule.AS_ASTRAY, Rule.AS_ASTRAY), List.of(PingEdge.INSTANCE, PongEdge.INSTANCE, new PingMark(), new PongMark()));
    }

    @Test
    void drawingsThatCannotBeMade_areRefused() {
        assertRefused(List.of(Rule.DRAWING_REFUSED), List.of(new BrokenEdge(), new BrokenMark()));
    }

    @Test
    void twoDesignsWithOneName_areRefused() {
        assertRefused(List.of(Rule.NAME_TWICE), List.of(PlainEdge.INSTANCE, PlainMark.INSTANCE, new Elsewhere.OtherEdge(), new Elsewhere.OtherMark()));
    }

    @Test
    void leavesThatOwnNothing_aTargetTwice_orOneNoSheetCarries_areRefused() {
        var e = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(ShabbyTaxonomy.INSTANCE.read(), List.of(SKETCH),
                List.of(new ShabbyTaxonomy.ScruffyBare(), new ShabbyTaxonomy.ScruffyDoubled(), new ShabbyTaxonomy.ScruffySounding())));
        assertEquals(List.of(Rule.OWNS_BADLY, Rule.OWNS_BADLY, Rule.OWNS_BADLY), e.problems().stream().map(DesignProblem::rule).toList(), e.getMessage());
    }

    // ── flattened ──────────────────────────────────────────────────────────

    @Test
    void aDesignFlattensIntoASheetOfItsOwn_aClassATarget_itsStatesWithin() {
        DesignRead bold = read("bold");
        var flat = FlattenDesign.INSTANCE.flatten(bold, SKETCH, "Bold");
        String css = flat.css();
        assertTrue(css.contains(".bold___edge__shape-border {\n    border-width: 3px; border-style: dashed;\n"
                + "    &:hover {\n        border-width: 4px;\n    }\n}\n"), "the sketch's edge, as written, and under the hand:\n" + css);
        assertTrue(css.contains("/* from plain___edge__color-border */\n.bold___edge__color-border {\n    border-color: #1f2937;\n}\n"),
                "a drawing taken from another design's concrete leaf, saying where from, its colour as paper gives it:\n" + css);
        assertTrue(css.contains("/* from plain___mark__shape-outline */\n.bold___mark__shape-outline {\n"), css);
        assertTrue(css.contains(".bold___mark__color-outline {\n    outline-color: #c62828;\n}\n"), "a colour the design pins, fixed:\n" + css);
        var night = new Dressed(ResolvingThemes.COLOURS, ResolvingThemes.Sketch.INSTANCE, ResolvingColours.Night.INSTANCE);
        String inTheNight = FlattenDesign.INSTANCE.flatten(bold, night, "Bold").css();
        assertTrue(inTheNight.contains(".bold___edge__color-border {\n    border-color: oklch(0.93 0.01 260);\n}\n"), "a palettable colour follows the palette worn:\n" + inTheNight);
        assertTrue(inTheNight.contains(".bold___mark__color-outline {\n    outline-color: #c62828;\n}\n"), "a fixed one does not");
        assertEquals(4, flat.drawn());
        assertEquals(2, flat.copied());
        assertTrue(FlattenDesign.INSTANCE.flatten(bold, DETAILED, "Bold").css().contains(".bold___edge__shape-border {\n    border-width: 2px; border-style: solid;\n"),
                "drawn per style");
    }

    @Test
    void aDrawingOfNothing_hasNoRule() {
        String css = FlattenDesign.INSTANCE.flatten(read("plain"), SKETCH, "Plain").css();
        assertTrue(css.contains(".plain___mark__shape-outline {"), css);
        assertFalse(css.contains(".plain___mark__color-outline"), css);
    }

    @Test
    void aConcreteLeafsClass_carriesItsDesign() {
        assertEquals("plain___edge__shape-border", SemanticTokens.INSTANCE.drawn(Plain.INSTANCE, Edge.INSTANCE, Target.Shape.Border.INSTANCE));
    }

    // ── handed to a page ───────────────────────────────────────────────────

    @Test
    void aDesignsObject_wearsAndShedsALeafsClasses_andRefusesALeafOfAnotherBranch() {
        js = buildContext();
        js.eval("js", "var plain_sheet = 'the sheet';"
                + " function El() { var s = new Set(); this.classList = { add: function (c) { s.add(c); }, remove: function (c) { s.delete(c); } };"
                + " this.classes = function () { return Array.from(s).sort().join(' '); }; }");
        js.eval("js", String.join("\n", DesignLeaves.INSTANCE.lines(TAXONOMY, Frame.INSTANCE)));
        js.eval("js", String.join("\n", DesignObject.INSTANCE.lines(read("plain"), "plain_sheet")));
        assertEquals("EDGE,MARK", js.eval("js", "Object.keys(FRAME).join(',')").asString(), "every leaf of the branch, in its order");
        assertEquals("frame plain plain true", js.eval("js", "[Plain.branch, Plain.name, String(Plain), Object.isFrozen(Plain)].join(' ')").asString());
        assertEquals("plain___edge__color-border plain___edge__shape-border",
                js.eval("js", "var el = new El(); Plain.wear(FRAME.EDGE, el); el.classes()").asString(), "a class for each target the leaf owns");
        assertEquals("", js.eval("js", "Plain.shed(FRAME.EDGE, el); el.classes()").asString());
        assertTrue(js.eval("js", "(function () { try { Plain.wear({ branch: 'other', token: 'edge' }, new El()); return ''; } catch (e) { return e.message; } })()")
                .asString().contains("a leaf of frame is asked"), "a leaf of another branch is refused");
        assertTrue(js.eval("js", "(function () { try { Plain.wear({ branch: 'frame', token: 'nope' }, new El()); return ''; } catch (e) { return e.message; } })()")
                .asString().contains("frame has no leaf 'nope'"));
    }

    @Test
    void onlyADesignBranch_hasLeavesForADesignToWear() {
        assertThrows(IllegalArgumentException.class, () -> DesignLeaves.INSTANCE.lines(TAXONOMY, Region.INSTANCE));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static DesignRead read(String token) {
        return ReadDesigns.INSTANCE.read(TAXONOMY, THEMES, GOOD).stream().filter(d -> d.token().equals(token)).findFirst().orElseThrow();
    }

    private static void assertRefused(List<Rule> rules, List<? extends ConcreteComponent<?, ?, ?>> concretes) {
        var e = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(TAXONOMY, List.of(SKETCH), concretes));
        assertEquals(rules, e.problems().stream().map(DesignProblem::rule).toList(), e.getMessage());
    }

    private static List<ConcreteComponent<?, ?, ?>> with(List<ConcreteComponent<?, ?, ?>> base, ConcreteComponent<?, ?, ?> more) {
        var out = new ArrayList<>(base);
        out.add(more);
        return out;
    }
    // ── the scheme a design is in (D33) ────────────────────────────────────

    /** A theme of the loud scheme, on top of the base: shouting, in neon. */
    private static final Dressed SHOUTING = new Dressed(ResolvingThemes.COLOURS, new ResolutionCasesTest.Shouting(), ResolvingColours.Neon.INSTANCE);

    @Test
    void aDesign_isInAScheme_andDrawnOnlyInAThemeWhoseSchemeSeesIt() {
        var unseen = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(TAXONOMY, THEMES, List.of(ShoutedEdge.INSTANCE, ShoutedMark.INSTANCE)));
        assertEquals(List.of(Rule.SCHEME_UNSEEN, Rule.SCHEME_UNSEEN), unseen.problems().stream().map(DesignProblem::rule).toList(), unseen.getMessage());
        assertTrue(unseen.getMessage().contains("shouted is Loud's, which ") && unseen.getMessage().contains("'s scheme, the base, does not see: "
                + "a design is drawn only in a theme whose scheme sees its own"), unseen.getMessage());
        var loud = ReadDesigns.INSTANCE.read(TAXONOMY, List.of(SHOUTING), List.of(ShoutedEdge.INSTANCE, ShoutedMark.INSTANCE)).get(0);
        assertTrue(FlattenDesign.INSTANCE.flatten(loud, SHOUTING, "Shouted").css().contains(".shouted___edge__shape-border"), "drawn where its scheme is");
        assertTrue(assertThrows(IllegalArgumentException.class, () -> FlattenDesign.INSTANCE.flatten(loud, SKETCH, "Shouted")).getMessage()
                .contains("shouted is drawn only in a theme whose scheme sees its own"), "and flattened nowhere else");
        assertEquals("plain", ReadDesigns.INSTANCE.read(TAXONOMY, List.of(SHOUTING), List.of(PlainEdge.INSTANCE, PlainMark.INSTANCE)).get(0).token(),
                "the base's design in a loud theme, which sees the base");
        var none = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(TAXONOMY, THEMES, List.of(UnschemedEdge.INSTANCE, UnschemedMark.INSTANCE)));
        assertEquals(List.of(Rule.NO_SCHEME), none.problems().stream().map(DesignProblem::rule).toList(), none.getMessage());
        assertTrue(none.getMessage().contains("unschemed is in no scheme: its first type argument is an abstract class"), none.getMessage());
    }
}
