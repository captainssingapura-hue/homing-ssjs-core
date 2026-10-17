package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Bold;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.BoldEdge;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.BoldMark;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Borrowing;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Broken;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Doubled;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Elsewhere;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Empty;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Half;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Lost;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Mute;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Ping;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Plain;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.PlainMark;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Pong;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Raw;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Shouted;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Unschemed;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Wrong;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Box;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Edge;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Frame;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Mark;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Region;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleProblem.Rule;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Local styles read, flattened and handed to a page (RFC 0067, appendix Local Styles): each as it lists its concrete
 * leaves, complete over its branch and closed at every owned target in every style, refused otherwise; a sheet of its
 * own for each style, the same in every palette; an object that wears a leaf's classes and refuses another branch's.
 */
class LocalStylesTest extends JsModuleTestBase {

    private static final Taxonomy TAXONOMY = FramingTaxonomy.INSTANCE.read();
    private static final InStyle SKETCH = in("sketch");
    private static final InStyle DETAILED = in("detailed");
    private static final List<InStyle> STYLES = List.of(SKETCH, DETAILED);
    private static final List<LocalStyle<?, ?>> GOOD = List.of(Bold.INSTANCE, Plain.INSTANCE);

    // ── read ───────────────────────────────────────────────────────────────

    @Test
    void localStylesAreRead_eachAsItListsItself_inTheBranchsOrder() {
        var read = ReadLocalStyles.INSTANCE.read(TAXONOMY, STYLES, GOOD);
        assertEquals(List.of("bold", "plain"), read.stream().map(LocalStyleRead::token).toList(), "in the order given");
        LocalStyleRead bold = read.get(0);
        assertEquals(Bold.INSTANCE, bold.localStyle());
        assertEquals(Frame.INSTANCE, bold.branch());
        assertEquals(List.of(BoldEdge.INSTANCE, BoldMark.INSTANCE), bold.concretes(), "the branch's order, whatever the listing's");
        assertEquals(List.of(Edge.INSTANCE, Mark.INSTANCE), bold.leaves());
        assertEquals(PlainMark.INSTANCE, read.get(1).of(Mark.INSTANCE));
        assertEquals(List.of(Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE), bold.owns(Edge.INSTANCE));
    }

    @Test
    void theVirtualLeaves_stayOutOfTheLookSheet() {
        assertEquals(List.of(new Casting(Box.INSTANCE)), TAXONOMY.castings(), "a virtual leaf is never cast");
        var flat = FlattenSheet.INSTANCE.flatten(TAXONOMY, ResolvingThemes.BY_NAME.get("sketch"), "the look sheet");
        assertTrue(flat.unanswered().stream().anyMatch(u -> u.startsWith("box__")), "the box is asked as ever");
        assertFalse(flat.unanswered().stream().anyMatch(u -> u.startsWith("edge__") || u.startsWith("mark__")), "a virtual leaf is never asked");
        assertFalse(flat.css().contains("edge__") || flat.css().contains("mark__"), flat.css());
    }

    @Test
    void readAlone_noStyleIsAsked() {
        assertEquals(2, ReadLocalStyles.INSTANCE.read(TAXONOMY, List.of(), GOOD).size());
        assertEquals(1, ReadLocalStyles.INSTANCE.read(TAXONOMY, List.of(), List.of(Broken.INSTANCE)).size(), "how it draws is asked in a style");
    }

    // ── refused ────────────────────────────────────────────────────────────

    @Test
    void aLocalStyleOfABranchTheTaxonomyHasNot_isRefused() {
        assertRefused(List.of(Rule.NOT_IN_TAXONOMY), List.of(Plain.INSTANCE, ShabbyTaxonomy.Scruffy.INSTANCE));
    }

    @Test
    void aLocalStyleMissingALeafOfItsBranch_orListingNothing_isRefused() {
        assertRefused(List.of(Rule.LEAF_MISSING), List.of(Half.INSTANCE));
        assertRefused(List.of(Rule.LEAF_MISSING), List.of(Empty.INSTANCE));
    }

    @Test
    void aLocalStyleWithTwoConcreteLeavesOfOneLeaf_isRefused() {
        assertRefused(List.of(Rule.LEAF_TWICE), List.of(Doubled.INSTANCE));
    }

    @Test
    void aConcreteLeafOfAnotherLocalStyle_listedAsOnesOwn_isRefused() {
        var e = assertRefused(List.of(Rule.NAMES_ANOTHER), List.of(Plain.INSTANCE, Borrowing.INSTANCE));
        assertTrue(e.getMessage().contains("borrowing lists PlainEdge (plain), a concrete leaf of plain: a local style lists its own"), e.getMessage());
    }

    @Test
    void aConcreteLeafThatSaysNothingOfHowItIsDrawn_isRefused() {
        assertRefused(List.of(Rule.NOT_DRAWN), List.of(Mute.INSTANCE));
    }

    @Test
    void aConcreteLeafThatNamesNoLocalStyle_isRefused() {
        assertRefused(List.of(Rule.UNREADABLE, Rule.LEAF_MISSING), List.of(Raw.INSTANCE));
    }

    @Test
    void drawingsOfTargetsTheLeafDoesNotOwn_twice_orNotAtAll_areRefused() {
        assertRefused(List.of(Rule.DRAWN_TWICE, Rule.NOT_OWNED, Rule.UNDRAWN), List.of(Wrong.INSTANCE));
    }

    @Test
    void aDrawingTakenFromAConcreteLeafOfAnotherLeaf_isRefused() {
        assertRefused(List.of(Rule.AS_ASTRAY), List.of(Lost.INSTANCE));
    }

    @Test
    void drawingsTakenRoundInACircle_areRefused() {
        assertRefused(List.of(Rule.AS_ASTRAY, Rule.AS_ASTRAY), List.of(Ping.INSTANCE, Pong.INSTANCE));
    }

    @Test
    void drawingsThatCannotBeMade_areRefused() {
        assertRefused(List.of(Rule.DRAWING_REFUSED), List.of(Broken.INSTANCE));
    }

    @Test
    void twoLocalStylesWithOneName_orOneGivenTwice_areRefused() {
        assertRefused(List.of(Rule.NAME_TWICE), List.of(Plain.INSTANCE, Elsewhere.Plain.INSTANCE));
        var twice = assertRefused(List.of(Rule.NAME_TWICE), List.of(Plain.INSTANCE, Plain.INSTANCE));
        assertTrue(twice.getMessage().contains("plain is given twice"), twice.getMessage());
    }

    @Test
    void leavesThatOwnNothing_aTargetTwice_orOneNoSheetCarries_areRefused() {
        var e = assertThrows(RefusedLocalStyles.class, () -> ReadLocalStyles.INSTANCE.read(ShabbyTaxonomy.INSTANCE.read(), List.of(SKETCH),
                List.of(ShabbyTaxonomy.Scruffy.INSTANCE)));
        assertEquals(List.of(Rule.OWNS_BADLY, Rule.OWNS_BADLY, Rule.OWNS_BADLY), e.problems().stream().map(LocalStyleProblem::rule).toList(), e.getMessage());
    }

    // ── flattened ──────────────────────────────────────────────────────────

    @Test
    void aLocalStyleFlattensIntoASheetOfItsOwn_aClassATarget_itsStatesWithin_itsPaletteColoursThePalettesVariables() {
        LocalStyleRead bold = read("bold");
        var flat = FlattenLocalStyle.INSTANCE.flatten(bold, SKETCH, "Bold");
        String css = flat.css();
        assertTrue(css.contains(".bold___edge__shape-border {\n    border-width: 3px; border-style: dashed;\n"
                + "    &:hover {\n        border-width: 4px;\n    }\n}\n"), "the sketch's edge, as written, and under the hand:\n" + css);
        assertTrue(css.contains("/* from plain___edge__color-border */\n.bold___edge__color-border {\n    border-color: var(--fp-copy);\n}\n"),
                "a drawing taken from another local style's concrete leaf, saying where from; a palettable colour as its palette's variable, "
                        + "so the one sheet serves every palette:\n" + css);
        assertTrue(css.contains("/* from plain___mark__shape-outline */\n.bold___mark__shape-outline {\n"), css);
        assertTrue(css.contains(".bold___mark__color-outline {\n    outline-color: #c62828;\n}\n"), "a colour the local style pins, fixed:\n" + css);
        assertEquals(4, flat.drawn());
        assertEquals(2, flat.copied());
        assertTrue(FlattenLocalStyle.INSTANCE.flatten(bold, DETAILED, "Bold").css().contains(".bold___edge__shape-border {\n    border-width: 2px; border-style: solid;\n"),
                "drawn per style");
    }

    @Test
    void aDrawingOfNothing_hasNoRule() {
        String css = FlattenLocalStyle.INSTANCE.flatten(read("plain"), SKETCH, "Plain").css();
        assertTrue(css.contains(".plain___mark__shape-outline {"), css);
        assertFalse(css.contains(".plain___mark__color-outline"), css);
    }

    @Test
    void aConcreteLeafsClass_carriesItsLocalStyle() {
        assertEquals("plain___edge__shape-border", SemanticTokens.INSTANCE.drawn(Plain.INSTANCE, Edge.INSTANCE, Target.Shape.Border.INSTANCE));
    }

    // ── handed to a page ───────────────────────────────────────────────────

    /** The plain local style's sheet, as a jar declares it: exporting one constant, for its object to import. */
    record PlainSheet() implements DerivedCss<PlainSheet> {
        static final PlainSheet INSTANCE = new PlainSheet();
        record plain_sheet() implements Exportable._Constant<PlainSheet> {}
        @Override public ExportsOf<PlainSheet> exports() { return new ExportsOf<>(INSTANCE, List.of(new plain_sheet())); }
    }

    /** A sheet that exports nothing to be imported by. */
    record SilentSheet() implements DerivedCss<SilentSheet> {}

    @Test
    void aLocalStylesObject_wearsAndShedsALeafsClasses_andRefusesALeafOfAnotherBranch() {
        js = buildContext();
        js.eval("js", "var plain_sheet = 'the sheet';"
                + " var css = { cls: function (n) { return { name: n }; },"
                + " addClass: function (el, c) { if (!c || typeof c.name !== 'string') throw new Error('not a handle: ' + c); el.classList.add(c.name); },"
                + " removeClass: function (el, c) { if (!c || typeof c.name !== 'string') throw new Error('not a handle: ' + c); el.classList.remove(c.name); } };"
                + " function El() { var s = new Set(); this.classList = { add: function (c) { s.add(c); }, remove: function (c) { s.delete(c); } };"
                + " this.classes = function () { return Array.from(s).sort().join(' '); }; }");
        js.eval("js", String.join("\n", LocalStyleLeaves.INSTANCE.lines(TAXONOMY, Frame.INSTANCE)));
        var lines = LocalStyleObject.INSTANCE.lines(read("plain"), PlainSheet.INSTANCE);
        assertTrue(lines.get(1).startsWith("void plain_sheet;"), "its sheet, by the constant the sheet exports: " + lines.get(1));
        assertTrue(lines.stream().noneMatch(l -> l.contains("classList") || l.contains("className")),
                "generated, it keeps the standard hand-written code keeps: its classes through the css manager, never the class list");
        js.eval("js", String.join("\n", lines));
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
    void anObjectsSheet_exportsTheOneConstantItIsImportedBy() {
        var e = assertThrows(IllegalArgumentException.class, () -> LocalStyleObject.INSTANCE.lines(read("plain"), new SilentSheet()));
        assertTrue(e.getMessage().contains("SilentSheet exports 0 things: a local style's sheet exports one constant"), e.getMessage());
    }

    @Test
    void onlyALocalStyleBranch_hasLeavesForALocalStyleToWear() {
        assertThrows(IllegalArgumentException.class, () -> LocalStyleLeaves.INSTANCE.lines(TAXONOMY, Region.INSTANCE));
    }

    // ── the scheme a local style is in (D33) ────────────────────────────────

    @Test
    void aLocalStyle_isInAScheme_andDrawnOnlyInAStyleWhoseSchemeSeesIt() {
        var unseen = assertThrows(RefusedLocalStyles.class, () -> ReadLocalStyles.INSTANCE.read(TAXONOMY, STYLES, List.of(Shouted.INSTANCE)));
        assertEquals(List.of(Rule.SCHEME_UNSEEN, Rule.SCHEME_UNSEEN), unseen.problems().stream().map(LocalStyleProblem::rule).toList(), unseen.getMessage());
        assertTrue(unseen.getMessage().contains("shouted is LoudScheme's, which sketch's scheme, BaseScheme, does not see: "
                + "a local style is drawn only in a style whose scheme sees its own"), unseen.getMessage());
        var shouting = new InStyle(new ResolutionCasesTest.Shouting(), ResolvingThemes.COLOURS);
        var loud = ReadLocalStyles.INSTANCE.read(TAXONOMY, List.of(shouting), List.of(Shouted.INSTANCE)).get(0);
        assertTrue(FlattenLocalStyle.INSTANCE.flatten(loud, shouting, "Shouted").css().contains(".shouted___edge__shape-border"), "drawn where its scheme is");
        assertTrue(assertThrows(IllegalArgumentException.class, () -> FlattenLocalStyle.INSTANCE.flatten(loud, SKETCH, "Shouted")).getMessage()
                .contains("shouted is drawn only in a style whose scheme sees its own"), "and flattened nowhere else");
        assertEquals("plain", ReadLocalStyles.INSTANCE.read(TAXONOMY, List.of(shouting), List.of(Plain.INSTANCE)).get(0).token(),
                "the base's local style in a loud style, which sees the base");
        var none = assertThrows(RefusedLocalStyles.class, () -> ReadLocalStyles.INSTANCE.read(TAXONOMY, STYLES, List.of(Unschemed.INSTANCE)));
        assertEquals(List.of(Rule.NO_SCHEME), none.problems().stream().map(LocalStyleProblem::rule).toList(), none.getMessage());
        assertTrue(none.getMessage().contains("unschemed says no scheme: a local style says the scheme it is in"), none.getMessage());
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /** A style of the fixtures, over their colour tree. */
    private static InStyle in(String name) {
        Dressed theme = ResolvingThemes.BY_NAME.get(name);
        return new InStyle(theme.style(), theme.colors());
    }

    private static LocalStyleRead read(String token) {
        return ReadLocalStyles.INSTANCE.read(TAXONOMY, STYLES, GOOD).stream().filter(d -> d.token().equals(token)).findFirst().orElseThrow();
    }

    private static RefusedLocalStyles assertRefused(List<Rule> rules, List<? extends LocalStyle<?, ?>> localStyles) {
        var e = assertThrows(RefusedLocalStyles.class, () -> ReadLocalStyles.INSTANCE.read(TAXONOMY, List.of(SKETCH), localStyles));
        assertEquals(rules, e.problems().stream().map(LocalStyleProblem::rule).toList(), e.getMessage());
        return e;
    }
}
