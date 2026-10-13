package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.DesignProblem.Rule;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.Bold;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BoldEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BoldMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BoxedEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BoxedMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BrokenEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.BrokenMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.EchoEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.EchoMark;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.Elsewhere;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.HalfEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.HollowEdge;
import hue.captains.singapura.js.homing.design.semantic.DesigningDesigns.HollowMark;
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
    /** A theme that answers every target at the root: the box's border is answered in it, as the sketch leaves it. */
    private static final Dressed VALUED = ResolvingThemes.BY_NAME.get("valued");
    private static final List<ConcreteComponent<?, ?, ?>> TAKEN =
            List.of(BoxedEdge.INSTANCE, BoxedMark.INSTANCE, EchoEdge.INSTANCE, EchoMark.INSTANCE);
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
    void aDrawingTakenFromAVirtualLeaf_orAComponentTheTaxonomyHasNot_isRefused() {
        var e = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(TAXONOMY, List.of(VALUED), List.of(new HollowEdge(), new HollowMark())));
        assertEquals(List.of(Rule.OF_ASTRAY, Rule.OF_ASTRAY), e.problems().stream().map(DesignProblem::rule).toList(), e.getMessage());
        assertTrue(e.getMessage().contains("taken from mark, a virtual leaf"), e.getMessage());
        assertTrue(e.getMessage().contains("taken from stray, which this taxonomy has not"), e.getMessage());
    }

    @Test
    void aDrawingTakenFromAComponentTheThemeLeavesUnanswered_isRefused() {
        var e = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(TAXONOMY, List.of(SKETCH), List.of(BoxedEdge.INSTANCE, BoxedMark.INSTANCE)));
        assertEquals(List.of(Rule.OF_ASTRAY, Rule.OF_ASTRAY), e.problems().stream().map(DesignProblem::rule).toList(), e.getMessage());
        assertTrue(e.getMessage().contains("leaves unanswered at rest"), e.getMessage());
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
    void aDrawingTakenFromAComponent_isTheThemesAnswerForIt_sayingWhose() {
        var read = ReadDesigns.INSTANCE.read(TAXONOMY, List.of(VALUED), TAKEN);
        DesignRead boxed = read.stream().filter(d -> d.token().equals("boxed")).findFirst().orElseThrow();
        assertEquals(TAXONOMY, boxed.taxonomy(), "a design keeps the taxonomy it was read over");
        var flat = FlattenDesign.INSTANCE.flatten(boxed, VALUED, "Boxed");
        String css = flat.css();
        assertTrue(css.contains("/* the theme's box */\n.boxed___edge__shape-border {\n    border-width: 0; border-style: solid;\n}\n"),
                "the box's border, as the valued style answers it at the root:\n" + css);
        assertTrue(css.contains("/* the theme's box */\n.boxed___edge__color-border {\n    border-color: currentColor;\n}\n"),
                "and its colour, as the valued style colours it:\n" + css);
        assertEquals(2, flat.drawn());
        assertEquals(0, flat.copied());
        assertEquals(2, flat.delegated());
        // taken through another design's concrete leaf: copied, and still the theme's
        DesignRead echo = read.stream().filter(d -> d.token().equals("echo")).findFirst().orElseThrow();
        var echoed = FlattenDesign.INSTANCE.flatten(echo, VALUED, "Echo");
        assertTrue(echoed.css().contains("/* from boxed___edge__shape-border */\n/* the theme's box */\n.echo___edge__shape-border {\n    border-width: 0; border-style: solid;\n}\n"),
                echoed.css());
        assertEquals(1, echoed.copied());
        assertEquals(1, echoed.delegated());
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
}
