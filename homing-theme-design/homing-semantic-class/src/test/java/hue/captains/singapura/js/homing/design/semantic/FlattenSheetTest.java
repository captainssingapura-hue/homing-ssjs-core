package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A theme's sheet flattened: every class an element can wear, its body copied from the node that
 * answers it - looks first, then treatments a state at a time in the cascade's order, nested on the
 * state's selector; a silence copied as nothing; a look nothing answers reported. And the bodies a
 * theme may write: its target's own properties, nested only on the element itself.
 */
class FlattenSheetTest {

    private static final Taxonomy TAXONOMY = ResolvingTaxonomy.INSTANCE.read();

    @Test
    void everyWornClass_hasARule_copiedFromTheNodeThatAnswersIt() {
        var flat = FlattenSheet.INSTANCE.flatten(TAXONOMY, answers("valued.answers"), "valued.answers");
        assertEquals(List.of(), flat.unanswered(), "every target answered at the root");
        assertEquals(6, TAXONOMY.castings().size(), "four components on their own, Go and Cancel - the window is the pane's own look");
        assertEquals(6 * 27, flat.looks(), "every casting on every CSS target but the glyph, which the theme answers with a silence");
        assertEquals(6, flat.treatments(), "the two buttons, each disabled, hovered, and its shadow disabled");
        String css = flat.css();
        assertTrue(css.contains("/* from button__shape-corner */\n.plain-button_cancel__shape-corner {\n    border-radius: 3px;\n}\n"), css);
        assertTrue(css.contains("\n.danger-button__shape-corner {\n    border-radius: 2px;\n}\n"), "answered itself: nothing to say where from");
        assertTrue(css.contains("/* from root__shape-corner */\n.floating-pane__shape-corner {\n    border-radius: 0;\n}\n"), css);
        assertFalse(css.contains("floating-pane_window__"), "a functional casting is its component on its own: no class of its own");
        assertFalse(css.contains("__type-glyph"), "a silence is copied as nothing");
        assertTrue(css.contains("/* from button__size-inset */\n.danger-button_go__size-inset {\n"
                + "    padding-block: calc(8px * pow(1.3, var(--size)));\n"
                + "    padding-inline: calc(18px * pow(1.3, var(--size)));\n}\n"), "a body over lines, as it was written");
    }

    @Test
    void treatments_nestOnTheirState_aStateAtATime_inTheCascadesOrder() {
        String css = FlattenSheet.INSTANCE.flatten(TAXONOMY, answers("valued.answers"), "valued.answers").css();
        assertTrue(css.contains("/* from control__effect-opacity--disabled */\n.danger-button__effect-opacity--disabled {\n"
                + "    &:disabled, &[aria-disabled=\"true\"] {\n        opacity: .45;\n    }\n}\n"), css);
        assertTrue(css.indexOf("/* ── looks ── */") < css.indexOf("/* ── hover ── */"), "looks first: a treatment outweighs a look only while its state holds");
        assertTrue(css.indexOf("/* ── hover ── */") < css.indexOf("/* ── disabled ── */"), "a later state wins: a disabled button under the hand reads disabled");
        assertFalse(css.contains("floating-pane__effect-opacity--disabled"), "a container is no control: nothing treats it");
    }

    @Test
    void aLookNothingAnswers_isReported_andHasNoRule() {
        var flat = FlattenSheet.INSTANCE.flatten(TAXONOMY, answers("sketch.answers"), "sketch.answers");
        assertTrue(flat.unanswered().contains("danger-button__color-ink"), flat.unanswered().toString());
        assertFalse(flat.css().contains(".danger-button__color-ink"));
    }

    @Test
    void bodiesRefused_aPropertyNotTheTargets_aSelectorNotTheElement_aStateNotOffered() {
        var read = ReadAnswers.INSTANCE;
        assertTrue(refused(read, "button rest shape-corner { border-width: 1px; }").contains("shape-corner does not own border-width; it owns border-radius"));
        assertTrue(refused(read, "button rest shape-corner { & .label { border-radius: 2px; } }").contains("is not the element itself"));
        assertTrue(refused(read, "button rest shape-corner { .other { border-radius: 2px; } }").contains("is not the element itself"));
        assertTrue(refused(read, "button disabled shape-corner { border-radius: 2px; }").contains("shape-corner offers no disabled"));
        assertTrue(refused(read, "root rest sound-cue { color: red; }").contains("sound-cue is no CSS target"));
        assertTrue(refused(read, "button rest shape-corner { border-radius: 2px;").contains("its '{' is never closed"));
        assertTrue(refused(read, "button rest shape-corner { border-radius: 2px; }", "button rest shape-corner").contains("answered twice"));
        // the element's own states, pseudo-elements, attributes and modes are its own to say
        var ok = read.read(TAXONOMY, List.of("button rest color-surface {",
                "  background-color: #fff;  /* the page's white */",
                "  &::before, &[data-keys=\"held\"] { background-color: #eee; }",
                "  @media (prefers-color-scheme: dark) { background-color: #111; }",
                "  --surface: #fff;",
                "}"));
        assertEquals(1, ok.bodies().size());
    }

    private static String refused(ReadAnswers read, String... lines) {
        return assertThrows(IllegalArgumentException.class, () -> read.read(TAXONOMY, List.of(lines))).getMessage();
    }

    private static AnswerSet answers(String name) { return ReadAnswers.INSTANCE.read(TAXONOMY, lines(name)); }

    private static List<String> lines(String name) {
        try (InputStream in = FlattenSheetTest.class.getResourceAsStream("/resolution/" + name)) {
            if (in == null) throw new IllegalStateException("no resolution/" + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
