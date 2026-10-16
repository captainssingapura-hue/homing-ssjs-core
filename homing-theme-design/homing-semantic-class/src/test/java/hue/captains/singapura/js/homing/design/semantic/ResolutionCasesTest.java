package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Page;
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
        assertEquals(37, report.cases(), "every case read");
        assertEquals(List.of(), report.failures(), () -> String.join("\n", report.failures()));
    }

    @Test
    void theWalk_isTold_stepByStep() {
        assertEquals(List.of(
                "query    plain-button_cancel  rest  color-text",
                "theme    sketch in paper",
                "walk      1. sketch  plain-button_cancel      -",
                "          2. sketch  plain-button_committing  -",
                "          3. sketch  plain-button_doing       -",
                "          4. sketch  plain-button             -",
                "          5. sketch  button_cancel            -",
                "          6. sketch  button_committing        answered",
                "result   button_committing__color-text from sketch",
                "         asked 6 times: sketch answers at button as committing"),
                resolve("sketch", "plain-button_cancel", State.REST, "color-text").explain());
    }

    /** The worn style all the way up first, then its base; a redirect in the base resolved in the style worn. */
    @Test
    void theWalk_layerByLayer_aRedirectResolvedInTheStyleWorn() {
        assertEquals(List.of(
                "query    floating-pane  rest  shape-corner",
                "theme    squared in paper",
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
        assertTrue(refused(() -> new Implementation<>(go, corner, new Answer.Own<Target.Shape.Corner>(Map.of(State.VERTICAL, "border-radius: 2px;"))))
                .contains("danger-button as go is answered vertical: a state is a treatment"));
        assertTrue(refused(() -> new Coloring<>(go, Target.Color.Background.INSTANCE, new Answer.Painted<>(Map.of(State.DISABLED, new Paint.Ground<>(new Page())))))
                .contains("danger-button as go is answered disabled: a state is a treatment"));
        assertTrue(refused(() -> new Coloring<>(Button.INSTANCE, Target.Color.Scrollbar.INSTANCE, new Answer.Painted<>(Map.of(State.HOVER, new Paint.Inherited<>()))))
                .contains("color-scrollbar offers no hover"));
        assertTrue(refused(() -> new Coloring<>(Button.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Redirect<>(Button.INSTANCE))).contains("redirected to itself"));
        assertTrue(refused(() -> new Answer.Painted<>(Map.<State, Paint<BaseScheme, Target.Color.Text>>of())).contains("answers at least one state"));
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
     * Colour is painted, never written: a body names no colour, and a shape drawn in colour reads only the colours
     * that colour it; the old design system's leaves are answered by their successors. That a colour target takes no
     * body at all is the compiler's: its own CSS is bounded to the targets that are not colour.
     */
    @Test
    void colourRefused_aColourWritten_anotherLeafsColours_theOldLeaves() {
        var shadow = Target.Shape.Shadow.INSTANCE;
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: 0 1px 3px #111936;")))
                .contains("shape-shadow names a colour, #111936: colour is painted, never written; it is drawn in color-box-shadow's"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: inset 0 1px 0 color-mix(in srgb, white 60%, transparent);")))
                .contains("names a colour, white"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: 0 0 4px rgba(0, 0, 0, .2);")))
                .contains("names a colour, rgba("));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>("box-shadow: 0 0 4px var(--background-mark);")))
                .contains("shape-shadow reads --background-mark: it reads its degrees and color-box-shadow's colours, and nothing else"));
        assertTrue(refused(() -> new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Own<>("--corner: 2px;")))
                .contains("shape-corner does not own --corner"));
        assertTrue(refused(() -> TOKENS.targetOf("shape-rule")).contains("'shape-rule' is no target"), "the old design system's rule is gone with it");
        // what may be said: the shadow's geometry in its own colours, the element's degrees, no colour at all
        new Implementation<>(Button.INSTANCE, shadow, new Answer.Own<>(
                "box-shadow: 0 calc(2px * pow(1.2, var(--size))) 8px color-mix(in srgb, var(--box-shadow-cast) 20%, transparent), inset 0 1px 0 var(--box-shadow-lit);"));
        new Implementation<>(Button.INSTANCE, Target.Type.Face.INSTANCE, new Answer.Own<>("font-family: \"Gold Sans\", serif;"));
    }

    @Test
    void themesRefused_asTheyAreDressed() {
        var colours = ResolvingThemes.COLOURS;
        var paper = ResolvingColours.Paper.INSTANCE;
        assertTrue(refused(() -> new Dressed(colours, new Baseless(), paper)).contains("asks its base, and baseless has none"));
        assertTrue(refused(() -> new Dressed(colours, new Twice(), paper)).contains("button shape-corner is answered twice"));
        assertTrue(refused(() -> new Dressed(colours, new Ouroboros(), paper)).contains("comes back to ouroboros"));
        assertTrue(refused(() -> new Dressed(colours, new Unschemed(), new ResolvingColours.Unmarked())).contains("unschemed requires no scheme"));
        assertEquals("stranded sits on nothing, and Loud is on top of BaseScheme: it sits on a palette of BaseScheme",
                refused(() -> new Dressed(colours, new Shouting(), new ResolvingColours.Stranded())));
        assertEquals("stacked sits on neon, of Loud: a palette of Loud sits on one of BaseScheme",
                refused(() -> new Dressed(colours, new Shouting(), new ResolvingColours.Stacked())));
        var looping = new Dressed(colours, new Looping(), paper);
        var e = assertThrows(IllegalStateException.class,
                () -> ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, looping, new Casting(PlainButton.INSTANCE), State.REST, Target.Shape.Corner.INSTANCE));
        assertEquals("a redirect comes back to plain-button on shape-corner: plain-button > danger-button > plain-button", e.getMessage());
        assertEquals(List.of(), ResolvingThemes.BY_NAME.get("squared").check(TAXONOMY), "every class the fixture themes answer is one the taxonomy asks");
        assertEquals(List.of("twice-asked: plain-button_go shape-corner - no casting the taxonomy has walks through it"),
                new Dressed(colours, new TwiceAsked(), paper).check(TAXONOMY));
    }

    /** Where the two are chosen at run time: worn only when both are of one scheme - a palette above the style's refused as one below. */
    @Test
    void aStyleWornAtRunTime_onlyInAPaletteOfItsScheme() {
        var colours = ResolvingThemes.COLOURS;
        DesignStyle<?> valued = ResolvingThemes.Valued.INSTANCE, shouting = new Shouting();
        ColorPalette<?> paper = ResolvingColours.Paper.INSTANCE, neon = ResolvingColours.Neon.INSTANCE;
        assertEquals("valued in paper", Wear.INSTANCE.of(colours, valued, paper).orElseThrow().toString());
        assertEquals("shouting in neon", Wear.INSTANCE.of(colours, shouting, neon).orElseThrow().toString());
        assertTrue(Wear.INSTANCE.of(colours, valued, neon).isEmpty(), "a palette of a scheme above: its colours are ones the style cannot see");
        assertTrue(Wear.INSTANCE.of(colours, shouting, paper).isEmpty(), "a palette of the scheme below: it gives none of the style's own");
    }

    /** A style of the loud scheme: written over the valued, which is of the base, below it. */
    public record Shouting() implements Derives<ResolvingColours.Loud, ResolvingThemes.Valued> {
        @Override public ResolvingThemes.Valued base() { return ResolvingThemes.Valued.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Coloring<ResolvingColours.Loud, ?>> colorings() {
            return List.of(new Coloring<>(Button.INSTANCE, Target.Color.Outline.INSTANCE, new Answer.Painted<>(new Paint.With<>(new ResolvingColours.Glare()))));
        }
    }
    public record Unschemed() implements DesignStyle<ColorScheme> {
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }
    public record Baseless() implements DesignStyle<BaseScheme> {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.FromBase<>()));
        }
    }
    public record Twice() implements DesignStyle<BaseScheme> {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Own<>("border-radius: 2px;")),
                           new Implementation<>(Button.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Own<>("border-radius: 3px;")));
        }
    }
    public record Ouroboros() implements Derives<BaseScheme, Ouroboros> {
        @Override public Ouroboros base() { return this; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }
    public record Looping() implements DesignStyle<BaseScheme> {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(PlainButton.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Redirect<>(DangerButton.INSTANCE)),
                           new Implementation<>(DangerButton.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Redirect<>(PlainButton.INSTANCE)));
        }
    }
    public record TwiceAsked() implements DesignStyle<BaseScheme> {
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(new Casting(PlainButton.INSTANCE, Go.INSTANCE), Target.Shape.Corner.INSTANCE, new Answer.Own<>("border-radius: 1px;")));
        }
    }

    @Test
    void theCommandLine_explainsOneQuery_tabulatesMany_andSaysWhenALookIsUnanswered() {
        String taxonomy = ResolvingTaxonomy.class.getName();
        String colours = ResolvingColours.class.getName(), paper = ResolvingColours.Paper.class.getName();
        String[] sketch = {"--colors", colours, "--style", ResolvingThemes.Sketch.class.getName(), "--palette", paper};
        var one = cli(join(new String[] {"--taxonomy", taxonomy}, sketch, new String[] {"danger-button_go", "rest", "color-background"}));
        assertEquals(0, one.code(), one.err());
        assertTrue(one.out().contains("result   danger-button_go__color-background from sketch"), one.out());
        var table = cli(join(new String[] {"--taxonomy", taxonomy}, sketch, new String[] {"danger-button", "rest", "*"}));
        assertEquals(1, table.code(), "the sketch answers few targets: the rest are unanswered");
        assertTrue(table.out().contains("rest         shape-corner       danger-button__shape-corner   from sketch"), table.out());
        assertTrue(table.out().contains("rest         color-text         UNANSWERED"), table.out());
        var squared = cli("--taxonomy", taxonomy, "--colors", colours, "--style", ResolvingThemes.Squared.class.getName(), "--palette", paper,
                "plain-button", "rest", "shape-corner");
        assertTrue(squared.out().contains("result   button__shape-corner from valued"), squared.out());
        var flat = cli("--taxonomy", taxonomy, "--colors", colours, "--style", ResolvingThemes.Squared.class.getName(), "--palette", paper, "--flatten");
        assertEquals(0, flat.code(), flat.err());
        assertTrue(flat.out().contains("/* from squared: control__shape-corner */\n.danger-button__shape-corner {\n    border-radius: 0;\n}\n"), flat.out());
        var list = cli("--taxonomy", taxonomy, "--list", "roles");
        assertTrue(list.out().contains("shaping   (functional)") && list.out().contains("window   (functional)") && list.out().contains("go\n"), list.out());
        assertEquals(2, cli(join(new String[] {"--taxonomy", taxonomy}, sketch, new String[] {"danger-button"})).code(), "a node, a state and a target");
        assertEquals(2, cli("--taxonomy", taxonomy, "danger-button", "rest", "shape-corner").code(), "no colours, no style, no palette");
        assertEquals(2, cli("--taxonomy", taxonomy, "--colors", colours, "--style", paper, "--palette", paper, "button", "rest", "shape-corner").code(),
                "a palette is no style");
        var apart = cli("--taxonomy", taxonomy, "--colors", colours, "--style", ResolvingThemes.Valued.class.getName(),
                "--palette", ResolvingColours.Neon.class.getName(), "button", "rest", "shape-corner");
        assertEquals(2, apart.code());
        assertTrue(apart.err().contains("valued and neon are of different schemes"), apart.err());
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
