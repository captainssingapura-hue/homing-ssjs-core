package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Button;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.DangerButton;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Go;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.PlainButton;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The resolution against its cases - {@code resolution/cases.txt}, which every resolver of semantic
 * classes runs, the Java one here and the JS one beside it: in a theme, a node, a state and a target
 * resolve to the one class written and the layer that answered it, to none, or to nothing answered.
 * Then the walk as it is told, the answers refused as they are written, and the command line.
 */
class ResolutionCasesTest {

    private static final Taxonomy TAXONOMY = ResolvingTaxonomy.INSTANCE.read();
    private static final SemanticTokens TOKENS = SemanticTokens.INSTANCE;

    @Test
    void everyCase_resolvesAsWritten() {
        var cases = ResolutionCases.INSTANCE;
        var report = cases.run(lines("cases.txt"), cases.java(TAXONOMY, ResolvingThemes.THEMES));
        assertEquals(35, report.cases(), "every case read");
        assertEquals(List.of(), report.failures(), () -> String.join("\n", report.failures()));
    }

    @Test
    void theWalk_isTold_stepByStep() {
        assertEquals(List.of(
                "query    plain-button_cancel  rest  color-ink",
                "theme    sketch in sketch-colours",
                "walk      1. sketch-colours  plain-button_cancel      -",
                "          2. sketch-colours  plain-button_committing  -",
                "          3. sketch-colours  plain-button_doing       -",
                "          4. sketch-colours  plain-button             -",
                "          5. sketch-colours  button_cancel            -",
                "          6. sketch-colours  button_committing        answered",
                "result   button_committing__color-ink from sketch-colours",
                "         asked 6 times: sketch-colours answers at button as committing"),
                resolve("sketch", "plain-button_cancel", State.REST, "color-ink").explain());
    }

    /** The worn style all the way up first, then its base; a redirect in the base resolved in the style worn. */
    @Test
    void theWalk_layerByLayer_aRedirectResolvedInTheStyleWorn() {
        assertEquals(List.of(
                "query    floating-pane  rest  shape-corner",
                "theme    squared in valued-colours",
                "walk      1. squared  floating-pane  -",
                "          2. squared  container      -",
                "          3. squared  root           -",
                "          4. valued   floating-pane  redirected to danger-button",
                "          5. squared  danger-button  -",
                "          6. squared  button         -",
                "          7. squared  control        answered",
                "result   control__shape-corner from squared",
                "         asked 7 times: squared answers at control"),
                resolve("squared", "floating-pane", State.REST, "shape-corner").explain());
        var fromBase = resolve("squared", "plain-button", State.REST, "shape-corner");
        assertEquals(List.of("squared plain-button its base's, from here", "valued plain-button -", "valued button answered"),
                fromBase.steps().stream().map(s -> s.layer() + " " + TOKENS.casting(s.node()) + " " + s.said()).toList());
        assertEquals("border-radius: 3px;", ((Resolution.Answered) fromBase.outcome()).body());
    }

    @Test
    void theNotes_sayWhyTheWalkIsTheOneItIs() {
        assertEquals(List.of("window is a functional role: the part is cast for its look as Standalone"),
                resolve("sketch", "floating-pane_window", State.REST, "shape-corner").notes());
        assertEquals(List.of("a state is a treatment, common to every role: go is set aside"),
                resolve("sketch", "danger-button_go", State.DISABLED, "effect-opacity").notes());
        assertEquals(List.of("no slot of the taxonomy casts plain-button as go: no owner can cast it so"),
                resolve("sketch", "plain-button_go", State.REST, "shape-corner").notes());
    }

    @Test
    void answersRefused_asTheyAreWritten() {
        var corner = Target.Shape.Corner.INSTANCE;
        var go = new Casting(DangerButton.INSTANCE, Go.INSTANCE);
        assertTrue(refused(() -> new Implementation<>(go, Target.Color.Background.INSTANCE, new Answer.Own<Target.Color.Background>(Map.of(State.DISABLED, ""))))
                .contains("danger-button as go is answered disabled: a state is a treatment"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, corner, new Answer.Own<Target.Shape.Corner>(Map.of(State.HOVER, "border-radius: 2px;"))))
                .contains("shape-corner offers no hover"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, corner, new Answer.FromBase<Target.Shape.Corner>(Set.of(State.HOVER))))
                .contains("asks its base hover, which shape-corner does not offer"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, corner, new Answer.Own<>("border-width: 1px;")))
                .contains("shape-corner does not own border-width; it owns border-radius"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, corner, new Answer.Own<>("& .label { border-radius: 2px; }")))
                .contains("is not the element itself"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, corner, new Answer.Own<>("border-radius: 2px; & { ")))
                .contains("is never closed"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, Target.Sound.Cue.INSTANCE, new Answer.Own<>("color: red;")))
                .contains("sound-cue is no CSS target"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, corner, new Answer.Redirect<>(Button.INSTANCE))).contains("redirected to itself"));
        assertTrue(refused(() -> new Answer.Own<Target.Shape.Corner>("border-radius: 2px;").at(State.REST, "border-radius: 3px;")).contains("answered twice"));
    }

    /**
     * A semantic colour is said whole, at the top and in every mode and state beside it; colour is
     * the palette's, so a style's shadow names none and reads only its own colours; the old design
     * system's leaves are answered by their successors.
     */
    @Test
    void colourRefused_partOfASet_aColourSaidByAStyle_anotherLeafsColours_theOldLeaves() {
        var shadow = Target.Shape.Shadow.INSTANCE;
        assertEquals("color-box-shadow is a semantic colour, said whole or not at all: --box-shadow-lit is not said",
                refused(() -> new Implementation<>(Button.INSTANCE, Target.Color.BoxShadow.INSTANCE,
                        new Answer.Own<>("--box-shadow-cast: #000; --box-shadow-glow: #f00;"))).replaceFirst("^button color-box-shadow rest: ", ""));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, Target.Color.Background.INSTANCE, new Answer.Own<>(
                "background-color: #fff; --background-ramp-from: transparent; --background-ramp-via: transparent; --background-ramp-to: transparent;"
                + " --background-mark: transparent; @media (prefers-color-scheme: dark) { background-color: #111; }")))
                .contains("not said"), "a mode says the whole set too: a dark ground under a light ramp is two answers mixed");
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, Target.Color.Outline.INSTANCE, new Answer.Own<>("@media (min-width: 1px) { outline-color: red; }")))
                .contains("is said in a block alone"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: 0 1px 3px #111936;")))
                .contains("shape-shadow names a colour, #111936: colour is the palette's; it is drawn in color-box-shadow's"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: inset 0 1px 0 color-mix(in srgb, white 60%, transparent);")))
                .contains("names a colour, white"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: 0 0 4px rgba(0, 0, 0, .2);")))
                .contains("names a colour, rgba("));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: 0 0 4px var(--background-mark);")))
                .contains("shape-shadow reads --background-mark: it reads its degrees and color-box-shadow's colours, and nothing else"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Own<>("--corner: 2px;")))
                .contains("shape-corner does not own --corner"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, Target.Color.Edge.INSTANCE, new Answer.Own<>("border-color: red;")))
                .contains("color-edge is the old design system's; the semantic classes answer color-border, color-outline, color-column-rule"));
        assertTrue(refused(() -> TOKENS.targetOf("shape-rule")).contains("'shape-rule' is the old design system's; the semantic classes answer shape-border, shape-outline"));
        // what may be said: the shadow's geometry in its own colours, the element's degrees, no colour at all
        new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>(
                "box-shadow: 0 calc(2px * pow(1.2, var(--size))) 8px color-mix(in srgb, var(--box-shadow-cast) 20%, transparent), inset 0 1px 0 var(--box-shadow-lit);"));
        new Implementation<>(Button.INSTANCE, Target.Type.Face.INSTANCE, new Answer.Own<>("font-family: \"Gold Sans\", serif;"));
    }

    @Test
    void themesRefused_asTheyAreDressed() {
        var palette = ResolvingThemes.ValuedColours.INSTANCE;
        assertTrue(refused(() -> new Dressed(new Painting(), palette)).contains("a style never answers colour: that is its palette's"));
        assertTrue(refused(() -> new Dressed(new Baseless(), palette)).contains("asks its base, and baseless has none"));
        assertTrue(refused(() -> new Dressed(new Twice(), palette)).contains("button shape-corner is answered twice"));
        assertTrue(refused(() -> new Dressed(new Ouroboros(), palette)).contains("comes back to ouroboros"));
        var looping = new Dressed(new Looping(), palette);
        var e = assertThrows(IllegalStateException.class,
                () -> ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, looping, new Casting(PlainButton.INSTANCE), State.REST, Target.Shape.Corner.INSTANCE));
        assertEquals("a redirect comes back to plain-button on shape-corner: plain-button > danger-button > plain-button", e.getMessage());
        assertEquals(List.of(), ResolvingThemes.BY_NAME.get("squared").check(TAXONOMY), "every class the fixture themes answer is one the taxonomy asks");
        assertEquals(List.of("twice-asked: plain-button_go shape-corner - no casting the taxonomy has walks through it"),
                new Dressed(new TwiceAsked(), palette).check(TAXONOMY));
    }

    public record Painting() implements DesignStyle {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(Button.INSTANCE, Target.Color.Ink.INSTANCE, new Answer.Own<>("color: red;")));
        }
    }
    public record Baseless() implements DesignStyle {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.FromBase<>()));
        }
    }
    public record Twice() implements DesignStyle {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Own<>("border-radius: 2px;")),
                           new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Own<>("border-radius: 3px;")));
        }
    }
    public record Ouroboros() implements DesignStyle, Derives<Ouroboros> {
        @Override public Ouroboros base() { return this; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }
    public record Looping() implements DesignStyle {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(PlainButton.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Redirect<>(DangerButton.INSTANCE)),
                           new Implementation<>(DangerButton.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Redirect<>(PlainButton.INSTANCE)));
        }
    }
    public record TwiceAsked() implements DesignStyle {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(new Casting(PlainButton.INSTANCE, Go.INSTANCE), Target.Shape.Corner.INSTANCE, new Answer.Own<>("border-radius: 1px;")));
        }
    }

    @Test
    void theCommandLine_explainsOneQuery_tabulatesMany_andSaysWhenALookIsUnanswered() {
        String taxonomy = ResolvingTaxonomy.class.getName();
        String[] sketch = {"--style", ResolvingThemes.Sketch.class.getName(), "--palette", ResolvingThemes.SketchColours.class.getName()};
        var one = cli(join(new String[] {"--taxonomy", taxonomy}, sketch, new String[] {"danger-button_go", "rest", "color-background"}));
        assertEquals(0, one.code(), one.err());
        assertTrue(one.out().contains("result   danger-button_go__color-background from sketch-colours"), one.out());
        var table = cli(join(new String[] {"--taxonomy", taxonomy}, sketch, new String[] {"danger-button", "rest", "*"}));
        assertEquals(1, table.code(), "the sketch answers few targets: the rest are unanswered");
        assertTrue(table.out().contains("rest         shape-corner       danger-button__shape-corner   from sketch"), table.out());
        assertTrue(table.out().contains("rest         color-ink          UNANSWERED"), table.out());
        var squared = cli("--taxonomy", taxonomy, "--style", ResolvingThemes.Squared.class.getName(), "--palette", ResolvingThemes.ValuedColours.class.getName(),
                "plain-button", "rest", "shape-corner");
        assertTrue(squared.out().contains("result   button__shape-corner from valued"), squared.out());
        var flat = cli("--taxonomy", taxonomy, "--style", ResolvingThemes.Squared.class.getName(), "--palette", ResolvingThemes.ValuedColours.class.getName(), "--flatten");
        assertEquals(0, flat.code(), flat.err());
        assertTrue(flat.out().contains("/* from squared: control__shape-corner */\n.danger-button__shape-corner {\n    border-radius: 0;\n}\n"), flat.out());
        var list = cli("--taxonomy", taxonomy, "--list", "roles");
        assertTrue(list.out().contains("shaping   (functional)") && list.out().contains("window   (functional)") && list.out().contains("go\n"), list.out());
        assertEquals(2, cli(join(new String[] {"--taxonomy", taxonomy}, sketch, new String[] {"danger-button"})).code(), "a node, a state and a target");
        assertEquals(2, cli("--taxonomy", taxonomy, "danger-button", "rest", "shape-corner").code(), "no style, no palette");
        assertEquals(2, cli("--taxonomy", taxonomy, "--style", ResolvingThemes.SketchColours.class.getName(), "--palette", ResolvingThemes.SketchColours.class.getName(),
                "button", "rest", "shape-corner").code(), "a palette is no style");
        assertEquals(2, cli("--taxonomy", "no.such.Taxonomy", "--list", "nodes").code());
    }

    private static Resolution resolve(String theme, String node, State state, String target) {
        return ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, ResolvingThemes.THEMES.apply(theme), TOKENS.castingOf(TAXONOMY, node), state, TOKENS.targetOf(target));
    }

    private static String refused(org.junit.jupiter.api.function.Executable e) {
        return assertThrows(IllegalArgumentException.class, e).getMessage();
    }

    private record Run(int code, String out, String err) {}

    private static String[] join(String[]... parts) {
        return Arrays.stream(parts).flatMap(Arrays::stream).toArray(String[]::new);
    }

    private static Run cli(String... args) {
        var out = new ByteArrayOutputStream();
        var err = new ByteArrayOutputStream();
        int code = ResolveCli.INSTANCE.run(Arrays.asList(args), new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Run(code, out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"), err.toString(StandardCharsets.UTF_8));
    }

    private static List<String> lines(String name) {
        try (InputStream in = ResolutionCasesTest.class.getResourceAsStream("/resolution/" + name)) {
            if (in == null) throw new IllegalStateException("no resolution/" + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
