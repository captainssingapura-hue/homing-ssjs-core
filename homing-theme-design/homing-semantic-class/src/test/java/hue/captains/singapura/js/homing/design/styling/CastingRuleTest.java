package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A casting's CSS held as each implementation's is (RFC 0067, appendices Two Classes per Leaf and The Style Jars): its
 * marks in form, then - filled in for its casting - its rule held to the casting's class and states, each pseudo-element
 * and reader's setting allowed by name (D29), its keyframes, and every colour held to the tree and the style's scheme;
 * each kind of wrong CSS refused by name, alone; the precedence a soft report.
 */
class CastingRuleTest {

    private static final Taxonomy TAXONOMY = StyledTaxonomy.INSTANCE.read();
    private static final ColorTree TREE = ResolvingColours.LoudColours.INSTANCE.read();
    private static final Casting TAB = new Casting(StatedTaxonomy.Tab.INSTANCE);
    private static final Casting METER = new Casting(StyledTaxonomy.Meter.INSTANCE);
    private static final Casting LEAD = TAXONOMY.castings().stream().filter(c -> c.token().equals("tab_lead")).findFirst().orElseThrow();
    private static final String ALONG = "color-mix(in oklab, var(--fp-danger) calc(clamp(0, var(--extent), 1) * 100%), "
            + "color-mix(in oklab, var(--fixed-clear) calc(clamp(0, var(--extent) + 1, 1) * 100%), var(--fp-safe)))";

    private static CheckDeclaration.CssChecked check(DesignStyle<?> style, Casting casting, String css) {
        return CheckDeclaration.css(TAXONOMY, TREE, style, casting, "Its.css", css);
    }

    private static void refused(String css, String says) { refused(TAB, css, says); }

    private static void refused(Casting casting, String css, String says) {
        List<String> problems = check(Plain.INSTANCE, casting, css).problems();
        assertEquals(1, problems.size(), "one problem, alone: " + problems);
        assertTrue(problems.get(0).contains(says), problems.get(0));
    }

    private static void passes(Casting casting, String css) {
        var checked = check(Plain.INSTANCE, casting, css);
        assertEquals(List.of(), checked.problems());
        assertEquals(List.of(), checked.reports());
    }

    // ── its one rule, marked ───────────────────────────────────────────────

    @Test
    void itsCss_isItsCastingsOneRule_andKeyframesOfItsOwn_nothingElse() {
        passes(TAB, "$casting { color: var(--fp-copy); }");
        refused("$casting {}\nbutton { color: var(--fp-copy); }", "button is no mark of the casting");
        refused("$casting {}\n$casting {}", "$casting is ruled twice");
        refused("color: var(--fp-copy);\n$casting {}", "color is declared outside $casting");
        refused("$casting {}\n@media print { $casting { color: var(--fp-page); } }", "@media print: an implementation's CSS holds no at-rule at its top");
        refused("", "marks no $casting rule");
    }

    // ── its states: drawn on its own element, only those it lists ─────────

    @Test
    void aState_isDrawnOnTheCastingsOwnElement_onlyIfItListsIt() {
        refused("$casting { .icon { color: var(--fp-copy); } }", ".icon names a class: an implementation's CSS names none");
        refused("$casting { span { color: var(--fp-copy); } }", "span inside styled-tab is no state of it");
        refused("$casting { &$pressed { color: var(--fp-copy); } }",
                "&.styled-tab--pressed draws no state tab lists: its states are :hover, .styled-tab--disabled, .styled-tab--selected, "
                        + ".styled-tab--candidate, .styled-tab--held");
        refused("$casting { &:disabled { color: var(--fp-raised); } }", "disabled, checked and invalid are application states, worn as classes (D21)");
        refused("$casting { &:focus { color: var(--fp-raised); } }", "&:focus draws no state tab lists");
        refused("$casting { &:hover {} &:hover {} }", "&:hover is drawn twice");
        refused("$casting { &$selected:hover { color: var(--fp-copy); } }", "a state is marked &$word alone");
        refused("$casting { &::first-line { color: var(--fp-copy); } }", "draws ::first-line, a pseudo-element the check does not allow by name");
        refused("$casting { &$selected { &:hover { color: var(--fp-copy); } } }", "a state's rule holds declarations alone");
    }

    @Test
    void aCastingInARole_drawsItsStates_underItsOwnClass_neverTheComponents() {
        passes(LEAD, "$casting { &:hover { color: var(--fp-copy); } &$selected { color: var(--fp-danger); } }");
        refused(LEAD, "$casting { &.styled-tab--selected { color: var(--fp-danger); } }", "names a class");
    }

    @Test
    void statesOutOfTheCastingsPrecedence_areReported_notRefused() {
        var checked = check(Plain.INSTANCE, TAB, "$casting {\n  &$selected { color: var(--fp-danger); }\n  &:hover { color: var(--fp-raised); }\n}");
        assertEquals(List.of(), checked.problems(), "a soft check (D23)");
        assertEquals(List.of("Its.css:3: :hover is drawn after .styled-tab--selected, which outranks it in tab's precedence: "
                + "where both hold, the later wins, so :hover would (D22)"), checked.reports());
    }

    // ── what it may hold besides, allowed by name (D29) ───────────────────

    @Test
    void aPseudoElementOfItsOwnElement_byName_onceAndHoldingDeclarationsAlone() {
        passes(TAB, "$casting { color: var(--fp-copy); &::placeholder { color: var(--fp-raised); } &::before { content: \"\"; background-color: var(--fp-safe); } }");
        refused("$casting { &::placeholder {} &::placeholder {} }", "&::placeholder is drawn twice");
        refused("$casting { &::placeholder { color: #999; } }", "#999 is a literal colour");
        refused("$casting { &::before { --fixed-alarm: #c62828; color: var(--fixed-alarm); } }", "--fixed-alarm is assigned in a pseudo-element's rule");
        refused("$casting { &::before { &:hover { color: var(--fp-copy); } } }", "a pseudo-element's rule holds declarations alone");
    }

    @Test
    void anAtMediaForAReadersSetting_nestedInItsRule_onceEach() {
        passes(TAB, "$casting { transition: color 1s; @media (prefers-reduced-motion: reduce) { transition: none; &:hover { color: var(--fp-copy); } } }");
        refused("$casting { @media print { color: var(--fp-copy); } }", "@media print is no reader's setting the check allows");
        refused("$casting { @media (prefers-contrast: more) {} @media (prefers-contrast: more) {} }", "@media (prefers-contrast: more) is answered twice");
        refused("$casting { @media (prefers-contrast: more) { @media (prefers-reduced-motion: reduce) {} } }", "a reader's setting is answered in the casting's rule");
        refused("$casting { @supports (display: grid) {} }", "a casting's rule holds no at-rule but an @media for a reader's setting");
        refused("$casting { @media (prefers-contrast: more) { --fixed-alarm: #c62828; } }", "--fixed-alarm is assigned in an @media of it");
    }

    @Test
    void itsKeyframes_namedForIt_heldToTheColours() {
        passes(TAB, "$casting { animation: $pulse 1s; }\n@keyframes $pulse { from { opacity: 1; } 50% { color: var(--fp-copy); } to { opacity: .5; } }");
        refused("$casting {}\n@keyframes $pulse { from { color: red; } }", "red is a named colour");
        refused("$casting {}\n@keyframes $pulse { .x { opacity: 1; } }", ".x is no keyframe");
        refused("$casting {}\n@keyframes $pulse {}\n@keyframes $pulse {}", "@keyframes $pulse is written twice");
        refused("$casting {}\n@keyframes spin { from { opacity: 1; } }", "@keyframes spin: an implementation's CSS holds no at-rule at its top");
    }

    // ── its colours: every one a variable ──────────────────────────────────

    @Test
    void aPalettesVariable_isAPalettableColourTheStylesSchemeSees_neverAssigned_neverFallingBack() {
        refused("$casting { color: var(--fp-ink); }", "--fp-ink names no palettable colour of the tree");
        refused("$casting { color: var(--fp-alarm); }", "--fp-alarm names no palettable colour of the tree: alarm is a fixed colour, --fixed-alarm");
        refused(METER, "$casting { color: var(--fp-glare); }", "--fp-glare: glare is Loud's, which plain's scheme, BaseScheme, does not see (D7)");
        assertEquals(List.of(), check(Loudly.INSTANCE, METER, "$casting { color: var(--fp-glare); }").problems(), "loudly's scheme sees it");
        refused("$casting { --fp-copy: var(--fp-page); }", "--fp-copy is the palette's, defined by its sheet");
        refused("$casting { color: var(--fp-copy, var(--fp-page)); }", "var(--fp-copy, ...) falls back");
    }

    @Test
    void aColourOfItsOwn_isAssignedInItsRule_asTheTreeGivesIt() {
        passes(METER, "$casting { --fixed-alarm: #C62828; color: var(--fixed-alarm); }");
        refused(METER, "$casting { --fixed-alarm: #ff0000; color: var(--fixed-alarm); }", "--fixed-alarm is #c62828 by the tree, not #ff0000");
        refused(METER, "$casting { color: var(--fixed-alarm); }",
                "var(--fixed-alarm) is named, and not assigned in the leaf's own rule: a colour of its own is assigned where it is used");
        refused(METER, "$casting { --fixed-copy: #1f2937; }", "--fixed-copy names no fixed colour of the tree: copy is a palettable colour, var(--fp-copy)");
        refused(METER, "$casting { --fixed-clear: transparent; --it-warning: color-mix(in oklab, var(--fixed-clear) 40%, var(--fp-danger)); }",
                "--it-warning is color-mix(in oklab, var(--fixed-clear) 50%, var(--fp-danger)) by the tree");
        refused("$casting { &:hover { --fixed-alarm: #c62828; color: var(--fixed-alarm); } }", "--fixed-alarm is assigned in a state's rule");
        refused(METER, "$casting { --fixed-alarm: #c62828; --fixed-alarm: #c62828; }", "--fixed-alarm is assigned twice");
    }

    @Test
    void theCurrentColourAtAShare_isAssignedInItsRule_asTheTreeGivesIt() {
        passes(TAB, "$casting { --current-hairline: color-mix(in oklab, currentColor 30%, transparent); border-color: var(--current-hairline); }");
        refused("$casting { --current-hairline: color-mix(in oklab, currentColor 60%, transparent); }",
                "--current-hairline is color-mix(in oklab, currentColor 30%, transparent) by the tree");
        refused("$casting { border-color: color-mix(in oklab, currentColor 30%, transparent); }", "color-mix( makes a colour the tree does not have");
        refused("$casting { --current-copy: currentColor; }", "--current-copy names no share of the current colour of the tree: copy is a palettable colour, var(--fp-copy)");
        refused("$casting { color: var(--fp-hairline); }", "--fp-hairline names no palettable colour of the tree: hairline is the current colour at a share, --current-hairline");
        passes(TAB, "$casting { color: currentColor; border-color: currentColor; }");   // the whole of it, CSS's own (D6)
    }

    @Test
    void aColourAlongAScale_onlyOnACastingWithTheColourAxis_itsAnchorsVariables() {
        passes(METER, "$casting { --fixed-clear: transparent; --along-risk: " + ALONG + "; background-color: var(--along-risk); }");
        refused("$casting { --fixed-clear: transparent; --along-risk: " + ALONG + "; }", "tab has no colour axis: only a leaf with it may (D14)");
        refused(METER, "$casting { --along-risk: color-mix(in oklab, #dc2626 calc(clamp(0, var(--extent), 1) * 100%), #16a34a); }",
                "--along-risk is " + ALONG + " by the tree");
    }

    /** A spectrum of the style's own: the tree's safe and danger its anchors, the turn at degrees of its own (D41). */
    record Swing() implements ColorScale<BaseScheme, ResolvingColours.Signals> {
        @Override public List<Knot<BaseScheme>> knots() {
            return List.of(new Knot<>(new ResolvingColours.Safe(), -1), new Knot<>(new ResolvingColours.Safe(), 0.4),
                           new Knot<>(new ResolvingColours.Danger(), 0.6), new Knot<>(new ResolvingColours.Danger(), 1));
        }
    }

    @Test
    void aSpectrumOfItsOwn_isReadInPlaceOfTheTrees_itsAnchorsTheTreesColours() {
        String swing = LocalColours.INSTANCE.along(new Swing());
        String css = "$casting { --along-swing: " + swing + "; background-color: var(--along-swing); }";
        var own = CheckDeclaration.css(TAXONOMY, TREE, Plain.INSTANCE, METER, "Its.css", css, List.of(new Swing()));
        assertEquals(List.of(), own.problems(), "its own spectrum, as it writes it");
        assertTrue(own.uses().containsKey("swing"), "used, so imported: the both-ways check sees it");
        refused(METER, css, "--along-swing names no colour scale of the tree");
        var drifted = CheckDeclaration.css(TAXONOMY, TREE, Plain.INSTANCE, METER, "Its.css",
                "$casting { --along-swing: " + LocalColours.INSTANCE.along(new ResolvingColours.Risk()) + "; background-color: var(--along-swing); }",
                List.of(new Swing()));
        assertTrue(drifted.problems().stream().anyMatch(p -> p.contains("by its own spectrum")), drifted.problems().toString());
    }

    @Test
    void noLiteralColour_butCssOwnWords() {
        refused("$casting { color: #fff; }", "#fff is a literal colour");
        refused("$casting { color: rgb(0 0 0); }", "rgb( writes a literal colour");
        refused("$casting { color: Red; }", "Red is a named colour");
        refused("$casting { color: Canvas; }", "Canvas is a system colour");
        refused("$casting { color: color-mix(in oklab, var(--fp-copy) 50%, transparent); }", "color-mix( makes a colour the tree does not have");
        refused("$casting { color: var(--fp-copy) !important; }", "color is !important, which overrules the leaf's precedence");
        passes(TAB, "$casting { color: currentColor; background: transparent; border-color: inherit; content: \"red\"; "
                + "background-image: url(red.png); }");
    }
}
