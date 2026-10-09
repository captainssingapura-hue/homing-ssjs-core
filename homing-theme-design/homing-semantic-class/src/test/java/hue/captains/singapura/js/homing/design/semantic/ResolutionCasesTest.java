package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The resolution against its cases - {@code resolution/cases.txt}, which every resolver of semantic
 * classes runs, the Java one here and the JS one beside it: under a theme's answers, a node, a
 * state and a target resolve to the one class written, to none, or to nothing answered. Then the
 * walk as it is told, the answers refused, and the command line.
 */
class ResolutionCasesTest {

    private static final Taxonomy TAXONOMY = ResolvingTaxonomy.INSTANCE.read();

    @Test
    void everyCase_resolvesAsWritten() {
        var cases = ResolutionCases.INSTANCE;
        var report = cases.run(lines("cases.txt"), cases.java(TAXONOMY, ResolutionCasesTest::lines));
        assertEquals(22, report.cases(), "every case read");
        assertEquals(List.of(), report.failures(), () -> String.join("\n", report.failures()));
    }

    @Test
    void theWalk_isTold_stepByStep() {
        var tokens = SemanticTokens.INSTANCE;
        Resolution r = ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, answers("sketch.answers"),
                tokens.castingOf(TAXONOMY, "plain-button_cancel"), State.REST, tokens.targetOf("color-ink"));
        assertEquals(List.of(
                "query    plain-button_cancel  rest  color-ink",
                "walk      1. plain-button_cancel      -",
                "          2. plain-button_committing  -",
                "          3. plain-button_doing       -",
                "          4. plain-button             -",
                "          5. button_cancel            -",
                "          6. button_committing        answered",
                "result   button_committing__color-ink",
                "         the nearest the theme answers: button as committing, 5 steps up"), r.explain());
    }

    @Test
    void theNotes_sayWhyTheWalkIsTheOneItIs() {
        var tokens = SemanticTokens.INSTANCE;
        var answers = answers("sketch.answers");
        Resolution window = ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, answers, tokens.castingOf(TAXONOMY, "floating-pane_window"),
                State.REST, tokens.targetOf("shape-corner"));
        assertEquals(List.of("window is a functional role: the part is cast for its look as Standalone"), window.notes());
        Resolution disabled = ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, answers, tokens.castingOf(TAXONOMY, "danger-button_go"),
                State.DISABLED, tokens.targetOf("effect-opacity"));
        assertEquals(List.of("a state is a treatment, common to every role: go is set aside"), disabled.notes());
        Resolution stray = ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, answers, tokens.castingOf(TAXONOMY, "plain-button_go"),
                State.REST, tokens.targetOf("shape-corner"));
        assertEquals(List.of("no slot of the taxonomy casts plain-button as go: no owner can cast it so"), stray.notes());
    }

    @Test
    void answersRefused_aStateOnARole_andWordsTheTaxonomyHasNot() {
        var reader = ReadAnswers.INSTANCE;
        var e = assertThrows(IllegalArgumentException.class, () -> reader.read(TAXONOMY, List.of("danger-button_go disabled color-surface")));
        assertTrue(e.getMessage().startsWith("line 1: danger-button as go is answered disabled: a state is a treatment"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> reader.read(TAXONOMY, List.of("", "toggle-button rest color-surface")), "no such component");
        assertThrows(IllegalArgumentException.class, () -> reader.read(TAXONOMY, List.of("button rest colour-surface")), "no such target");
        assertThrows(IllegalArgumentException.class, () -> reader.read(TAXONOMY, List.of("button pressed color-surface")), "no such state");
        assertThrows(IllegalArgumentException.class, () -> reader.read(TAXONOMY, List.of("button rest")), "three words");
    }

    @Test
    void theCommandLine_explainsOneQuery_tabulatesMany_andSaysWhenALookIsUnanswered() {
        String file = path("sketch.answers");
        var one = cli("--taxonomy", ResolvingTaxonomy.class.getName(), "--answers", file, "danger-button_go", "rest", "color-surface");
        assertEquals(0, one.code());
        assertTrue(one.out().contains("result   danger-button_go__color-surface"), one.out());
        var table = cli("--taxonomy", ResolvingTaxonomy.class.getName(), "--answers", file, "danger-button", "rest", "*");
        assertEquals(1, table.code(), "the sketch answers few targets: the rest are unanswered");
        assertTrue(table.out().contains("rest         shape-corner       danger-button__shape-corner   (0 up)"), table.out());
        assertTrue(table.out().contains("rest         color-ink          UNANSWERED"), table.out());
        var list = cli("--taxonomy", ResolvingTaxonomy.class.getName(), "--list", "roles");
        assertTrue(list.out().contains("shaping   (functional)") && list.out().contains("window   (functional)") && list.out().contains("go\n"), list.out());
        assertEquals(2, cli("--taxonomy", ResolvingTaxonomy.class.getName(), "--answers", file, "danger-button").code(), "a node, a state and a target");
        assertEquals(2, cli("--taxonomy", "no.such.Taxonomy", "--list", "nodes").code());
    }

    private record Run(int code, String out, String err) {}

    private static Run cli(String... args) {
        var out = new ByteArrayOutputStream();
        var err = new ByteArrayOutputStream();
        int code = ResolveCli.INSTANCE.run(Arrays.asList(args), new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Run(code, out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"), err.toString(StandardCharsets.UTF_8));
    }

    private static AnswerSet answers(String name) { return ReadAnswers.INSTANCE.read(TAXONOMY, lines(name)); }

    private static List<String> lines(String name) {
        try (InputStream in = ResolutionCasesTest.class.getResourceAsStream("/resolution/" + name)) {
            if (in == null) throw new IllegalStateException("no resolution/" + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String path(String name) {
        try {
            return Path.of(ResolutionCasesTest.class.getResource("/resolution/" + name).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
