package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
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
 * The check of a style's files, from the Java leaves: each style's answer for every casting and its rule in its family's
 * files; every family file holding its castings' rules, each once, in the family's order; every rule held to its leaf's
 * class and states and to the colour tree; each kind of wrong file refused by name, alone; the precedence a soft report.
 */
class CheckStyleTest {

    private static final Taxonomy TAXONOMY = StyledTaxonomy.INSTANCE.read();
    private static final ColorTree TREE = ResolvingColours.INSTANCE.read();
    private static final CheckStyle CHECK = CheckStyle.INSTANCE;
    private static final Casting TAB = new Casting(StatedTaxonomy.Tab.INSTANCE);
    private static final Casting METER = new Casting(StyledTaxonomy.Meter.INSTANCE);
    private static final String ALONG = "color-mix(in oklab, var(--fp-danger) calc(clamp(0, var(--extent), 1) * 100%), "
            + "color-mix(in oklab, var(--fixed-clear) calc(clamp(0, var(--extent) + 1, 1) * 100%), var(--fp-safe)))";

    /** A casting's family's own file: control.css. */
    private static String fileOf(Casting leaf) { return StyleFiles.INSTANCE.file(TAXONOMY.head(leaf.cast()).orElseThrow()); }

    /** The controls' file in plain refused for one thing, said by name. */
    private static void refused(String css, String says) { refused(TAB, css, says); }

    private static void refused(Casting leaf, String css, String says) {
        List<String> problems = CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, fileOf(leaf), css).problems();
        assertEquals(1, problems.size(), "one problem, alone: " + problems);
        assertTrue(problems.get(0).contains(says), problems.get(0));
    }

    private static void passes(Casting leaf, String css) {
        var checked = CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, fileOf(leaf), css);
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
        assertEquals("styles/hue/captains/singapura/js/homing/design/styling/plain/control.css", files.path(Plain.INSTANCE, fileOf(TAB)));
        assertEquals(List.of("control.css", "control.extension.css", "gauge.css"), files.present(Plain.INSTANCE),
                "a file per family, and the extension's own file of the controls, for the badge it adds");
        assertEquals(List.of("control.css", "control.extension.css", "gauge.css"), files.written(TAXONOMY, Plain.INSTANCE).values().stream()
                .map(StyleFiles.Written::file).distinct().toList());
        assertEquals(Plain.INSTANCE, StyleSheet.INSTANCE.baseLayer(Loudly.INSTANCE).orElseThrow(), "loudly's absence lands on plain");
    }

    @Test
    void aBaseLayer_answersEveryLeaf_anExtensionsToo_andHasNoFileForNone() {
        assertEquals(List.of(
                "shabby/knob.css is no family's file: a style's file is <family>.css, or <family>.<module>.css, and the families are control, gauge",
                "shabby is a base layer, and answers absence for meter: a base layer answers content for every leaf, an extension's too (D26)",
                "shabby answers content for badge, and no file of control's rules it: shabby/control.css, or a module's"),
                CHECK.check(TAXONOMY, TREE, Shabby.INSTANCE).problems());
    }

    @Test
    void aFileTheStyleDoesNotAnswer_isRefused() {
        assertEquals(List.of("stray/control.css:2: stray rules tab, which it does not answer: it answers absence, so the base layer's is used - "
                + "answer content, or remove the rule"), CHECK.check(TAXONOMY, TREE, Stray.INSTANCE).problems());
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

    /** A branch that heads no family, and a pebble under it. */
    record Loose() implements L1_ComponentBranch<Root> {
        static final Loose INSTANCE = new Loose();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Pebble() implements Component<Loose> {
        static final Pebble INSTANCE = new Pebble();
        @Override public Loose parent() { return Loose.INSTANCE; }
    }

    @Test
    void aCastingOfNoFamily_hasNoFile_andIsRefused() {
        var loose = new ReadTaxonomy().read(List.of(Pebble.INSTANCE));
        var problems = CHECK.check(loose, TREE, Plain.INSTANCE).problems();
        assertTrue(problems.contains("pebble is of no family: no branch above pebble heads one, so no file of plain can rule it (D30)"), problems.toString());
        assertTrue(problems.contains("plain/control.css is no family's file: a style's file is <family>.css, or <family>.<module>.css, "
                + "and the families are none - no branch heads one"), problems.toString());
    }

    // ── a family's file: its castings' classes, each once, in its order ────

    @Test
    void aFamilysFile_rulesItsCastings_eachOnce_inTheFamilysOrder_andNothingElse() {
        passes(TAB, ".styled-tab { color: var(--fp-copy); }\n.styled-strip { color: var(--fp-copy); }");
        assertEquals(List.of("tab", "tab_lead", "strip", "badge"), TAXONOMY.family(StatedTaxonomy.Control.INSTANCE).stream().map(Casting::token).toList(),
                "each component on its own, then in each role: the tab as Lead beside the tab");
        refused(".styled-tab {}\n.styled-meter { color: var(--fp-copy); }", "plain/control.css:2: .styled-meter is gauge's, ruled in gauge's files, not control's");
        refused(".styled-tab {}\nbutton { color: var(--fp-copy); }", "button is no casting's rule");
        refused(".styled-tab {}\n.styled-tab .icon { color: var(--fp-copy); }", ".styled-tab .icon is no casting's rule");
        refused(".styled-tab {}\n.styled-tab, .styled-strip { color: var(--fp-copy); }", ".styled-tab, .styled-strip is no casting's rule");
        refused("", "plain/control.css: rules none of control's castings");
        refused(".styled-tab {}\n.styled-tab {}", "plain/control.css:2: .styled-tab is ruled twice: first at plain/control.css:1");
        refused(".styled-strip {}\n.styled-tab {}", "plain/control.css:2: .styled-tab is ruled after .styled-strip, which comes after it in "
                + "control's order: tab, tab_lead, strip, badge");
        refused("color: var(--fp-copy);\n.styled-tab {}", "plain/control.css:1: color is declared outside any rule");
        refused(".styled-tab {}\n@media print { .styled-tab { color: var(--fp-page); } }", "@media print: a family's file holds no at-rule but @keyframes named after a casting it rules");
        assertEquals(List.of("plain/knob.css is no family's file"), CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, "knob.css", ".styled-tab {}").problems());
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
        refused(".styled-tab { &::first-line { color: var(--fp-copy); } }", "draws ::first-line, a pseudo-element the check does not allow by name");
        refused(".styled-tab { &.styled-tab--selected { &:hover { color: var(--fp-copy); } } }", "a state's rule holds declarations alone");
    }

    // ── what a file may hold besides, allowed by name (D29) ───────────────

    @Test
    void aPseudoElementOfTheLeafsOwnElement_byName_onceAndHoldingDeclarationsAlone() {
        passes(TAB, ".styled-tab { color: var(--fp-copy); &::placeholder { color: var(--fp-raised); } &::before { content: \"\"; background-color: var(--fp-safe); } }");
        refused(".styled-tab { &::placeholder {} &::placeholder {} }", "&::placeholder is drawn twice");
        refused(".styled-tab { &::placeholder { color: #999; } }", "#999 is a literal colour");
        refused(".styled-tab { &::before { --fixed-alarm: #c62828; color: var(--fixed-alarm); } }", "--fixed-alarm is assigned in a pseudo-element's rule");
        refused(".styled-tab { &::before { &:hover { color: var(--fp-copy); } } }", "a pseudo-element's rule holds declarations alone");
    }

    @Test
    void anAtMediaForAReadersSetting_nestedInTheCastingsRule_onceEach() {
        passes(TAB, ".styled-tab { transition: color 1s; @media (prefers-reduced-motion: reduce) { transition: none; &:hover { color: var(--fp-copy); } } }");
        refused(".styled-tab { @media print { color: var(--fp-copy); } }", "@media print is no reader's setting the check allows");
        refused(".styled-tab { @media (prefers-contrast: more) {} @media (prefers-contrast: more) {} }", "@media (prefers-contrast: more) is answered twice");
        refused(".styled-tab { @media (prefers-contrast: more) { @media (prefers-reduced-motion: reduce) {} } }", "a reader's setting is answered in the casting's rule");
        refused(".styled-tab { @supports (display: grid) {} }", "a casting's rule holds no at-rule but an @media for a reader's setting");
        refused(".styled-tab { @media (prefers-contrast: more) { --fixed-alarm: #c62828; } }", "--fixed-alarm is assigned in an @media of it");
    }

    @Test
    void anAtKeyframes_namedAfterACastingTheFileRules_itsKeyframesHeldToTheColours() {
        passes(TAB, ".styled-tab { animation: tab-pulse 1s; }\n@keyframes tab-pulse { from { opacity: 1; } 50% { color: var(--fp-copy); } to { opacity: .5; } }");
        refused(".styled-tab {}\n@keyframes spin { from { opacity: 1; } }", "@keyframes spin is named after none of control's castings");
        refused(".styled-tab {}\n@keyframes strip-slide { from { opacity: 1; } }", "@keyframes strip-slide is strip's, which this file does not rule");
        refused(".styled-tab {}\n@keyframes tab-pulse { from { color: red; } }", "red is a named colour");
        refused(".styled-tab {}\n@keyframes tab-pulse { .x { opacity: 1; } }", ".x is no keyframe");
        refused(".styled-tab {}\n@keyframes tab-pulse {}\n@keyframes tab-pulse {}", "@keyframes tab-pulse is written twice");
        assertEquals("tab_lead", StyleFiles.INSTANCE.namedAfter("tab_lead-glow", TAXONOMY.castings()).orElseThrow().token(), "the longest token it begins with");
    }

    @Test
    void aCastingInARole_drawsItsComponentsStates_byItsOwnClasses() {
        Casting lead = TAXONOMY.castings().stream().filter(c -> !c.standalone()).findFirst().orElseThrow();
        refused(lead, ".styled-tab_lead { &.styled-tab--selected { color: var(--fp-danger); } }", "draws no state tab_lead lists");
        passes(lead, ".styled-tab_lead { &:hover { color: var(--fp-copy); } &.styled-tab_lead--selected { color: var(--fp-danger); } }");
    }

    @Test
    void statesOutOfTheLeafsPrecedence_areReported_notRefused() {
        var checked = CHECK.file(TAXONOMY, TREE, Plain.INSTANCE, fileOf(TAB),
                ".styled-tab {\n  &.styled-tab--selected { color: var(--fp-danger); }\n  &:hover { color: var(--fp-raised); }\n}");
        assertEquals(List.of(), checked.problems(), "a soft check (D23)");
        assertEquals(List.of("plain/control.css:3: :hover is drawn after .styled-tab--selected, which outranks it in tab's precedence: "
                + "where both hold, the later wins, so :hover would (D22)"), checked.reports());
    }

    // ── its colours: every one a variable ──────────────────────────────────

    @Test
    void aPalettesVariable_isAPalettableColourTheStylesSchemeSees_neverAssigned_neverFallingBack() {
        refused(".styled-tab { color: var(--fp-ink); }", "--fp-ink names no palettable colour of the tree");
        refused(".styled-tab { color: var(--fp-alarm); }", "--fp-alarm names no palettable colour of the tree: alarm is a fixed colour, --fixed-alarm");
        refused(METER, ".styled-meter { color: var(--fp-glare); }", "--fp-glare: glare is Loud's, which plain's scheme, the base, does not see (D7)");
        assertTrue(CHECK.file(TAXONOMY, TREE, Loudly.INSTANCE, fileOf(METER), ".styled-meter { color: var(--fp-glare); }").passes(), "loudly's scheme sees it");
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
