package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.Implementation;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Stray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The check of a style's files, from the Java leaves: each style's answer for every leaf and its file; every file held
 * to its leaf's class and states and to the colour tree; each kind of wrong file refused by name, alone; the precedence
 * a soft report.
 */
class CheckStyleTest {

    private static final Taxonomy TAXONOMY = StyledTaxonomy.INSTANCE.read();
    private static final ColorTree TREE = ResolvingColours.INSTANCE.read();
    private static final CheckStyle CHECK = CheckStyle.INSTANCE;
    private static final Casting TAB = new Casting(StatedTaxonomy.Tab.INSTANCE);
    private static final Casting METER = new Casting(StyledTaxonomy.Meter.INSTANCE);
    private static final String ALONG = "color-mix(in oklab, var(--fp-danger) calc(clamp(0, var(--extent), 1) * 100%), "
            + "color-mix(in oklab, var(--fixed-clear) calc(clamp(0, var(--extent) + 1, 1) * 100%), var(--fp-safe)))";

    /** A tab's file in plain refused for one thing, said by name. */
    private static void refused(String css, String says) { refused(TAB, css, says); }

    private static void refused(Casting leaf, String css, String says) {
        List<String> problems = CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, leaf, css).problems();
        assertEquals(1, problems.size(), "one problem, alone: " + problems);
        assertTrue(problems.get(0).contains(says), problems.get(0));
    }

    private static void passes(Casting leaf, String css) {
        var checked = CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, leaf, css);
        assertEquals(List.of(), checked.problems());
        assertEquals(List.of(), checked.reports());
    }

    // ── a style over every leaf ────────────────────────────────────────────

    @Test
    void aBaseLayer_andTheStylesOverIt_pass() {
        assertEquals(List.of("tab", "strip", "meter", "badge", "tab_lead"), TAXONOMY.castings().stream().map(Casting::token).toList(),
                "the extension's badge a leaf like the others, and the tab cast as Lead one for its class");
        assertEquals(List.of(), CHECK.require(TAXONOMY, TREE, List.of(Plain.INSTANCE, Ink.INSTANCE, Loudly.INSTANCE)), "no report");
        var files = StyleFiles.INSTANCE;
        assertEquals("styles/hue/captains/singapura/js/homing/design/styling/plain/tab.css", files.path(Plain.INSTANCE, TAB));
        assertEquals(List.of("badge.css", "meter.css", "strip.css", "tab.css", "tab_lead.css"), files.present(Plain.INSTANCE));
        assertEquals(Plain.INSTANCE, StyleSheet.INSTANCE.baseLayer(Loudly.INSTANCE).orElseThrow(), "loudly's absence lands on plain");
    }

    @Test
    void aBaseLayer_answersEveryLeaf_anExtensionsToo_andHasNoFileForNone() {
        assertEquals(List.of(
                "shabby is a base layer, and answers absence for meter: a base layer answers content for every leaf, an extension's too (D26)",
                "shabby answers content for badge, and has no file at styles/hue/captains/singapura/js/homing/design/styling/shabby/badge.css",
                "shabby/knob.css is no leaf's file: no leaf of the taxonomy is knob"),
                CHECK.check(TAXONOMY, TREE, Shabby.INSTANCE).problems());
    }

    @Test
    void aFileTheStyleDoesNotAnswer_isRefused() {
        assertEquals(List.of("stray/tab.css is a file stray does not answer: it answers absence for tab, so the base layer's is used - "
                + "answer content, or remove the file"), CHECK.check(TAXONOMY, TREE, Stray.INSTANCE).problems());
    }

    @Test
    void stylesCheckedTogether_areRefusedWithEveryProblemAtOnce() {
        var refused = assertThrows(IllegalArgumentException.class,
                () -> CHECK.require(TAXONOMY, TREE, List.of(Plain.INSTANCE, Shabby.INSTANCE, Stray.INSTANCE)));
        assertEquals(4, refused.getMessage().lines().count() - 1, refused.getMessage());
    }

    /** A style whose type argument is the marker, which is no scheme. */
    record Unschemed() implements DesignStyle<ColorScheme> {
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }

    @Test
    void aStyleOfNoScheme_isRefused() {
        assertEquals(List.of("unschemed requires no scheme: its type argument is the marker, a variable, or no abstract scheme"),
                CHECK.check(TAXONOMY, TREE, new Unschemed()).problems());
    }

    // ── a file's selectors: its leaf's class, its states nested in it ──────

    @Test
    void aFileRulesItsLeafsClass_once_andNothingElse() {
        refused(".styled-tab { color: var(--fp-copy); }\n.styled-strip { color: var(--fp-copy); }",
                "plain/tab.css:2: .styled-strip is not this leaf's: its file rules .styled-tab alone, its states nested in it");
        refused(".styled-tab {}\nbutton { color: var(--fp-copy); }", "button is not this leaf's");
        refused(".styled-tab {}\n.styled-tab .icon { color: var(--fp-copy); }", ".styled-tab .icon is not this leaf's");
        refused(".styled-tab {}\n.styled-tab, .styled-strip { color: var(--fp-copy); }", ".styled-tab, .styled-strip is not this leaf's");
        refused("", "plain/tab.css: no rule for .styled-tab: a leaf's file rules its own class");
        refused(".styled-tab {}\n.styled-tab {}", "plain/tab.css:2: .styled-tab is ruled twice");
        refused("color: var(--fp-copy);\n.styled-tab {}", "plain/tab.css:1: color is declared outside any rule");
        refused(".styled-tab {}\n@media print { .styled-tab { color: var(--fp-page); } }", "@media print: no at-rule is in a leaf's file");
    }

    @Test
    void aState_isDrawnOnTheLeafsOwnElement_onlyIfTheLeafListsIt() {
        refused(".styled-tab { .icon { color: var(--fp-copy); } }", ".icon inside styled-tab is no state of it");
        refused(".styled-tab { &.styled-tab--pressed { color: var(--fp-copy); } }",
                "&.styled-tab--pressed draws no state tab lists: its states are :hover, .styled-tab--disabled, .styled-tab--selected, "
                        + ".styled-tab--candidate, .styled-tab--held");
        refused(".styled-tab { &:disabled { color: var(--fp-raised); } }", "disabled, checked and invalid are application states, worn as classes (D21)");
        refused(".styled-tab { &:focus { color: var(--fp-raised); } }", "&:focus draws no state tab lists");
        refused(".styled-tab { &:hover {} &:hover {} }", "&:hover is drawn twice");
        refused(".styled-tab { &.styled-tab--selected:hover { color: var(--fp-copy); } }",
                "draws .styled-tab--selected with more: each state is drawn alone, and where two hold the leaf's precedence decides (D22)");
        refused(".styled-tab { &::before { color: var(--fp-copy); } }", "&::before draws a pseudo-element: none is in a leaf's file, for now");
        refused(".styled-tab { &.styled-tab--selected { &:hover { color: var(--fp-copy); } } }", "a state's rule holds declarations alone");
    }

    @Test
    void aCastingInARole_drawsItsComponentsStates_byItsOwnClasses() {
        Casting lead = TAXONOMY.castings().stream().filter(c -> !c.standalone()).findFirst().orElseThrow();
        refused(lead, ".styled-tab_lead { &.styled-tab--selected { color: var(--fp-danger); } }", "draws no state tab_lead lists");
        passes(lead, ".styled-tab_lead { &:hover { color: var(--fp-copy); } &.styled-tab_lead--selected { color: var(--fp-danger); } }");
    }

    @Test
    void statesOutOfTheLeafsPrecedence_areReported_notRefused() {
        var checked = CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, TAB,
                ".styled-tab {\n  &.styled-tab--selected { color: var(--fp-danger); }\n  &:hover { color: var(--fp-raised); }\n}");
        assertEquals(List.of(), checked.problems(), "a soft check (D23)");
        assertEquals(List.of("plain/tab.css:3: :hover is drawn after .styled-tab--selected, which outranks it in tab's precedence: "
                + "where both hold, the later wins, so :hover would (D22)"), checked.reports());
    }

    // ── its colours: every one a variable ──────────────────────────────────

    @Test
    void aPalettesVariable_isAPalettableColourTheStylesSchemeSees_neverAssigned_neverFallingBack() {
        refused(".styled-tab { color: var(--fp-ink); }", "--fp-ink names no palettable colour of the tree");
        refused(".styled-tab { color: var(--fp-alarm); }", "--fp-alarm names no palettable colour of the tree: alarm is a fixed colour, --fixed-alarm");
        refused(METER, ".styled-meter { color: var(--fp-glare); }", "--fp-glare: glare is Loud's, which plain's scheme, the base, does not see (D7)");
        assertTrue(CHECK.file(TAXONOMY, TREE, Loudly.INSTANCE, METER, ".styled-meter { color: var(--fp-glare); }").passes(), "loudly's scheme sees it");
        refused(".styled-tab { --fp-copy: var(--fp-page); }", "--fp-copy is the palette's, defined by its sheet");
        refused(".styled-tab { color: var(--fp-copy, var(--fp-page)); }", "var(--fp-copy, ...) falls back");
    }

    @Test
    void aColourOfTheLeafsOwn_isAssignedInItsRule_asTheTreeGivesIt() {
        passes(METER, ".styled-meter { --fixed-alarm: #C62828; color: var(--fixed-alarm); }");
        refused(METER, ".styled-meter { --fixed-alarm: #ff0000; color: var(--fixed-alarm); }", "--fixed-alarm is #c62828 by the tree, not #ff0000");
        refused(METER, ".styled-meter { color: var(--fixed-alarm); }",
                "var(--fixed-alarm) is named, and not assigned in the leaf's own rule: a colour of its own is assigned where it is used");
        refused(METER, ".styled-meter { --fixed-copy: #1f2937; }", "--fixed-copy names no fixed colour of the tree: copy is a palettable colour, var(--fp-copy)");
        refused(METER, ".styled-meter { --fixed-clear: transparent; --it-warning: color-mix(in oklab, var(--fixed-clear) 40%, var(--fp-danger)); }",
                "--it-warning is color-mix(in oklab, var(--fixed-clear) 50%, var(--fp-danger)) by the tree");
        refused(".styled-tab { &:hover { --fixed-alarm: #c62828; color: var(--fixed-alarm); } }", "--fixed-alarm is assigned in a state's rule");
        refused(METER, ".styled-meter { --fixed-alarm: #c62828; --fixed-alarm: #c62828; }", "--fixed-alarm is assigned twice");
    }

    @Test
    void aColourAlongAScale_onlyOnALeafWithTheColourAxis_itsAnchorsVariables() {
        passes(METER, ".styled-meter { --fixed-clear: transparent; --along-risk: " + ALONG + "; background-color: var(--along-risk); }");
        refused(".styled-tab { --fixed-clear: transparent; --along-risk: " + ALONG + "; }", "tab has no colour axis: only a leaf with it may (D14)");
        refused(METER, ".styled-meter { --along-risk: color-mix(in oklab, #dc2626 calc(clamp(0, var(--extent), 1) * 100%), #16a34a); }",
                "--along-risk is " + ALONG + " by the tree");
    }

    @Test
    void noLiteralColour_butCssOwnWords() {
        refused(".styled-tab { color: #fff; }", "#fff is a literal colour");
        refused(".styled-tab { color: rgb(0 0 0); }", "rgb( writes a literal colour");
        refused(".styled-tab { color: Red; }", "Red is a named colour");
        refused(".styled-tab { color: Canvas; }", "Canvas is a system colour");
        refused(".styled-tab { color: color-mix(in oklab, var(--fp-copy) 50%, transparent); }", "color-mix( makes a colour the tree does not have");
        refused(".styled-tab { color: var(--fp-copy) !important; }", "color is !important, which overrules the leaf's precedence");
        passes(TAB, ".styled-tab { color: currentColor; background: transparent; border-color: inherit; content: \"red\"; "
                + "background-image: url(red.png); }");
    }
}
