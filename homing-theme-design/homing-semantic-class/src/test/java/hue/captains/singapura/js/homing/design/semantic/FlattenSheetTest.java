package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A theme's sheet flattened: every class an element can wear, its body copied from the layer and
 * the node that answer it - looks first, then treatments a state at a time in the cascade's order,
 * nested on the state's selector; a silence copied as nothing; a look nothing answers reported.
 */
class FlattenSheetTest {

    private static final Taxonomy TAXONOMY = ResolvingTaxonomy.INSTANCE.read();

    private static FlattenSheet.Flattened flatten(String theme) {
        return FlattenSheet.INSTANCE.flatten(TAXONOMY, ResolvingThemes.THEMES.apply(theme), theme);
    }

    @Test
    void everyWornClass_hasARule_copiedFromTheNodeThatAnswersIt() {
        var flat = flatten("valued");
        assertEquals(List.of(), flat.unanswered(), "every target answered at the root");
        assertEquals(6, TAXONOMY.castings().size(), "four components on their own, Go and Cancel - the window is the pane's own look");
        assertEquals(6 * 32, flat.looks(), "every casting on every CSS target the semantic classes answer but the glyph, which the style answers with a silence");
        assertEquals(6, flat.treatments(), "the two buttons, each disabled, hovered, and its shadow disabled");
        String css = flat.css();
        assertTrue(css.contains("/* from valued: button__shape-corner */\n.plain-button_cancel__shape-corner {\n    border-radius: 3px;\n}\n"), css);
        assertTrue(css.contains("\n.danger-button__shape-corner {\n    border-radius: 2px;\n}\n"), "answered itself: nothing to say where from");
        assertTrue(css.contains("/* from valued: danger-button__shape-corner */\n.floating-pane__shape-corner {\n    border-radius: 2px;\n}\n"),
                "a redirect copies the body of the class it resolves to");
        assertTrue(css.contains("\n.danger-button_go__color-background {\n    background-color: #c62828;\n    --background-ramp-from: transparent;\n"
                + "    --background-ramp-via: transparent;\n    --background-ramp-to: transparent;\n    --background-mark: transparent;\n}\n"),
                "the style's own, the whole set, a fixed colour as written: nothing to say where from");
        assertTrue(css.contains("/* from valued: root__color-background */\n.danger-button__color-background {\n    background-color: transparent;\n"), css);
        assertTrue(css.contains("\n.danger-button__color-box-shadow {\n    --box-shadow-cast: #1f2937;\n    --box-shadow-lit: #ffffff;\n    --box-shadow-glow: #c62828;\n}\n"),
                "a box's shadow colours, set on the element itself, each as paper gives it");
        assertTrue(css.contains("\n.danger-button__color-ink {\n    color: color-mix(in oklab, #dc2626 calc(clamp(0, var(--extent), 1) * 100%),"
                + " color-mix(in oklab, transparent calc(clamp(0, var(--extent) + 1, 1) * 100%), #16a34a));\n}\n"),
                "along the risk scale, by the degree the danger button sets on itself: safe at -1, clear at 0, danger at 1");
        assertTrue(css.contains("/* from valued: danger-button__color-ink */\n.danger-button_go__color-ink {\n"), "and as Go, the same");
        assertTrue(css.contains("\n.plain-button__color-border {\n    border-color: color-mix(in oklab, transparent 50%, #dc2626);\n}\n"),
                "the warning: the risk scale half way from clear to danger, mixed when the sheet is made");
        assertTrue(css.contains("/* from valued: root__color-ink */\n.plain-button__color-ink {\n    color: inherit;\n}\n"));
        assertTrue(css.contains("/* from valued: root__color-scrollbar */\n.plain-button__color-scrollbar {\n    scrollbar-color: inherit;\n}\n"));
        assertTrue(css.contains("@property --box-shadow-cast { syntax: \"<color>\"; inherits: false; initial-value: transparent; }\n"), "registered at the head");
        assertTrue(css.contains("@property --background-mark { syntax: \"<color>\"; inherits: false; initial-value: transparent; }\n"));
        assertTrue(css.contains("color-mix(in srgb, var(--box-shadow-cast) 12%, transparent)"), "the style's shadow reads the palette's cast");
        assertFalse(css.contains("__color-edge") || css.contains("__shape-rule") || css.contains("__color-surface"), "the old design system's leaves are worn by no semantic class");
        assertFalse(css.contains("floating-pane_window__"), "a functional casting is its component on its own: no class of its own");
        assertFalse(css.contains("__type-glyph"), "a silence is copied as nothing");
        assertTrue(css.contains("/* from valued: button__size-inset */\n.danger-button_go__size-inset {\n"
                + "    padding-block: calc(8px * pow(1.3, var(--size)));\n"
                + "    padding-inline: calc(18px * pow(1.3, var(--size)));\n}\n"), "a body over lines, as it was written");
    }

    @Test
    void treatments_nestOnTheirState_aStateAtATime_inTheCascadesOrder() {
        String css = flatten("valued").css();
        assertTrue(css.contains("/* from valued: control__effect-opacity--disabled */\n.danger-button__effect-opacity--disabled {\n"
                + "    &:disabled, &[aria-disabled=\"true\"] {\n        opacity: .45;\n    }\n}\n"), css);
        assertTrue(css.indexOf("/* ── looks ── */") < css.indexOf("/* ── hover ── */"), "looks first: a treatment outweighs a look only while its state holds");
        assertTrue(css.indexOf("/* ── hover ── */") < css.indexOf("/* ── disabled ── */"), "a later state wins: a disabled button under the hand reads disabled");
        assertFalse(css.contains("floating-pane__effect-opacity--disabled"), "a container is no control: nothing treats it");
    }

    /** Written over the valued: its own answers win over its base's precise ones, and the rest is copied from the base, saying so. */
    @Test
    void aStyleOverItsBase_ownsWhatItAnswers_andCopiesTheRest() {
        var flat = flatten("squared");
        assertEquals(List.of(), flat.unanswered(), "the base answers every root");
        String css = flat.css();
        assertTrue(css.contains("/* from squared: control__shape-corner */\n.danger-button__shape-corner {\n    border-radius: 0;\n}\n"),
                "the worn style's coarse answer outweighs its base's own for the danger button");
        assertTrue(css.contains("/* from valued: button__shape-corner */\n.plain-button__shape-corner {\n    border-radius: 3px;\n}\n"), "its base's, from the plain button up");
        assertTrue(css.contains("/* from squared: control__shape-corner */\n.floating-pane__shape-corner {\n    border-radius: 0;\n}\n"),
                "the base's redirect, resolved in the style worn");
        assertTrue(css.contains("/* from squared: button__shape-shadow */\n.plain-button__shape-shadow {\n    box-shadow: inset 0 0 0 1px;\n}\n"), css);
        assertTrue(css.contains("/* from valued: button__shape-shadow--hover */\n.plain-button__shape-shadow--hover {\n"), "the hover it leaves is its base's");
    }

    /** A glyph is drawn on ::before: its body nested there, at rest and in a state alike. */
    public record Glyphed() implements Derives<hue.captains.singapura.js.homing.design.color.ColorScheme.Base, ResolvingThemes.Valued> {
        @Override public ResolvingThemes.Valued base() { return ResolvingThemes.Valued.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(ResolvingTaxonomy.Button.INSTANCE, hue.captains.singapura.js.homing.design.Target.Type.Glyph.INSTANCE,
                    new Answer.Own<hue.captains.singapura.js.homing.design.Target.Type.Glyph>("content: \"✕\";")
                            .at(hue.captains.singapura.js.homing.design.State.PRESSED, "content: \"✔\";")));
        }
    }

    @Test
    void aTargetPaintedOnAPseudoElement_isWrittenThere() {
        String css = FlattenSheet.INSTANCE.flatten(TAXONOMY, new Dressed(ResolvingThemes.COLOURS, new Glyphed(), ResolvingColours.Paper.INSTANCE), "glyphed").css();
        assertTrue(css.contains("/* from glyphed: button__type-glyph */\n.plain-button__type-glyph {\n    &::before {\n        content: \"✕\";\n    }\n}\n"), css);
        assertTrue(css.contains(".plain-button__type-glyph--pressed {\n    &[aria-pressed=\"true\"] {\n        &::before {\n            content: \"✔\";\n        }\n    }\n}\n"),
                "in a state: the state's selector, then the pseudo-element");
    }

    @Test
    void aLookNothingAnswers_isReported_andHasNoRule() {
        var flat = flatten("sketch");
        assertTrue(flat.unanswered().contains("danger-button__color-ink"), flat.unanswered().toString());
        assertFalse(flat.css().contains(".danger-button__color-ink"));
    }
}
