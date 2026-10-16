package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.L1_ColorBranch;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorProblem.Rule;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Danger;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Glare;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Neon;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Night;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Paper;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Risk;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Safe;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Signals;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Button;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.PlainButton;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A scheme's colour tree read by its reader, layer by layer from the base up to its own - each layer its scheme's alone,
 * closed, adding colours - and refused where the compiler cannot refuse it; a palettable colour resolved down the
 * palettes; a palette checked whole over what its scheme sees; a colour moving along a scale only where the casting
 * varies its colour by degree.
 */
class ColorTreeTest {

    private static final ColorTree TREE = ResolvingThemes.COLOURS;

    @Test
    void theTree_isTheLayersFromTheBaseUp_eachItsSchemesAlone() {
        assertEquals(16, BaseColours.INSTANCE.read().nodes().size(), "the base's own - a hairline, the current colour at a share, the mines among them");
        assertEquals(17, TREE.nodes().size(), "the loud scheme's, its glare over the base's sixteen");
        assertEquals(new Glare(), TREE.nodes().get(16), "the base's layer first, the scheme's own last");
        assertTrue(!BaseColours.INSTANCE.read().has(new Glare()), "the base sees nothing of the scheme on top of it");
        assertEquals(BaseColours.LoudScheme.class, TREE.scheme(new Glare()));
        assertEquals(new Signals(), TREE.parent(new Glare()).orElseThrow(), "a scheme's colour, under the base's branch");
        assertTrue(TREE.parent(new Signals()).isEmpty(), "a branch at the top");
    }

    /** The selected palette, then the palettes below it. */
    @Test
    void aPalettableColour_resolvesDownThePalettes_theFirstAnswerWinning() {
        var flag = new BaseColours.Flag();
        assertEquals("#e53935", css(TREE.resolve(Paper.INSTANCE, flag)), "paper's own flag");
        assertEquals("#ff6f00", css(TREE.resolve(Night.INSTANCE, flag)), "the night's own");
        assertEquals("#e53935", css(TREE.resolve(Neon.INSTANCE, flag)), "what neon leaves, paper below it gives");
        assertEquals("#ff0055", css(TREE.resolve(Neon.INSTANCE, new Danger())), "neon's own danger, over paper's");
        assertEquals("#1f2937", css(TREE.resolve(Neon.INSTANCE, new Copy())), "what neon leaves, paper below it gives");
        assertEquals("#39ff14", css(TREE.resolve(Neon.INSTANCE, new Glare())));
        assertTrue(TREE.resolve(Paper.INSTANCE, new Glare()).isEmpty(), "paper is of the base: the loud scheme's glare is none of its");
    }

    @Test
    void aPalette_isCheckedWhole_overWhatItsSchemeSees() {
        assertEquals(List.of(), TREE.check(Paper.INSTANCE));
        assertEquals(List.of(), TREE.check(Night.INSTANCE));
        assertEquals(List.of(), TREE.check(Neon.INSTANCE), "complete over its scheme's own, the rest down the chain");
        assertEquals(List.of("unsafe leaves safe unanswered"), TREE.check(new BaseColours.Unsafe()));
        assertEquals(List.of("stranded sits on nothing, and LoudScheme is on top of BaseScheme: it sits on a palette of BaseScheme"),
                TREE.check(new BaseColours.Stranded()));
        assertEquals(List.of("unmarked implements no scheme: its type argument is an abstract class, a scheme from scratch or one on top of another"),
                TREE.check(new BaseColours.Unmarked()));
    }

    @Test
    void anInterpolatedColour_isWorkedOutWhenTheSheetIsMade_andOnlyOneTheTreeHas() {
        var nodes = new ArrayList<ColorNode<?, ?, ?>>(TREE.nodes());
        nodes.addAll(List.of(new AtSafe(), new MostlyClear()));
        var tree = ReadColors.INSTANCE.read(nodes);
        var theme = new Dressed(tree, ResolvingThemes.Valued.INSTANCE, Paper.INSTANCE);
        var render = RenderColor.INSTANCE;
        assertEquals("#16a34a", render.css(theme, new AtSafe()), "at a knot's degree, the knot's own colour");
        assertEquals("color-mix(in oklab, #16a34a 25%, transparent)", render.css(theme, new MostlyClear()), "a quarter of the way from safe, three from clear");
        var e = assertThrows(IllegalArgumentException.class, () -> render.css(ResolvingThemes.BY_NAME.get("valued"), new AtSafe()));
        assertEquals("at-safe is in no colour tree read: its scheme's layer, or one below it, lists it", e.getMessage());
    }

    /** The risk scale's first knot. */
    public record AtSafe() implements Interpolated<BaseScheme, Signals> {
        @Override public ColorScale<? super BaseScheme, ?> scale() { return new Risk(); }
        @Override public double degree() { return -1; }
    }

    /** Three quarters of the way from safe at -1 to clear at 0. */
    public record MostlyClear() implements Interpolated<BaseScheme, Signals> {
        @Override public ColorScale<? super BaseScheme, ?> scale() { return new Risk(); }
        @Override public double degree() { return -0.25; }
    }

    @Test
    void alongAScale_onlyWhereTheCastingVariesItsColourByDegree() {
        Taxonomy taxonomy = ResolvingTaxonomy.INSTANCE.read();
        var theme = new Dressed(TREE, new Swaying(), Paper.INSTANCE);
        var e = assertThrows(IllegalStateException.class, () -> ResolveSemanticClass.INSTANCE.resolve(taxonomy, theme,
                new Casting(PlainButton.INSTANCE), State.REST, Target.Color.Text.INSTANCE));
        assertTrue(e.getMessage().contains("plain-button declares no COLOUR axis: only a casting that varies its colour by degree moves along a scale"), e.getMessage());
    }

    /** Every button's ink along the risk scale - the plain button's included, which varies its colour by no degree. */
    public record Swaying() implements DesignStyle<BaseScheme> {
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Coloring<BaseScheme, ?>> colorings() {
            return List.of(new Coloring<>(Button.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Painted<>(new Paint.Along<>(new Risk()))),
                           new Coloring<>(Root.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Painted<>(new Paint.Inherited<>())));
        }
    }

    // ── refused as read ────────────────────────────────────────────────────

    public record Lost() implements L1_ColorBranch<BaseScheme> {}
    public record Astray() implements Palettable<BaseScheme, Lost> {}
    public record Danger2() implements Palettable<BaseScheme, Signals> { @Override public String token() { return "danger"; } }
    public record Coloured(int shade) implements Palettable<BaseScheme, Signals> {}
    public record Marker() implements L1_ColorBranch<ColorScheme> {}
    public record Short() implements ColorScale<BaseScheme, Signals> {
        @Override public List<Knot<BaseScheme>> knots() { return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Danger(), 1)); }
    }
    public record Falling() implements ColorScale<BaseScheme, Signals> {
        @Override public List<Knot<BaseScheme>> knots() {
            return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Danger(), 0.5), new Knot<>(new Copy(), 0.25), new Knot<>(new Danger(), 0.9));
        }
    }
    public record Off() implements Interpolated<BaseScheme, Signals> {
        @Override public ColorScale<? super BaseScheme, ?> scale() { return new Risk(); }
        @Override public double degree() { return 2; }
    }
    public record Blank() implements Fixed<BaseScheme, Signals> { @Override public ConcreteColor color() { return null; } }
    public record None() implements CurrentColor<BaseScheme, Signals> { @Override public double share() { return 0; } }

    @Test
    void whatTheCompilerCannotRefuse_isRefusedAsTheTreeIsRead_everyProblemAtOnce() {
        List<ColorNode<?, ?, ?>> own = List.of(new BaseColours.Surfaces(), new BaseColours.Words(), new Signals(), new BaseColours.Clear(),
                new Safe(), new Danger(), new Copy(), new Risk(),
                new Astray(), new Danger2(), new Coloured(1), new Marker(), new Short(), new Falling(), new Off(), new Blank(), new None());
        var e = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(own));
        assertEquals(List.of(Rule.NAME_TWICE, Rule.PARENT_MISSING, Rule.NOT_STATELESS, Rule.SCHEME_ASTRAY,
                        Rule.SCALE_BADLY, Rule.SCALE_BADLY, Rule.SCALE_BADLY, Rule.INTERPOLATED_BADLY, Rule.FIXED_BADLY, Rule.SHARE_BADLY),
                e.problems().stream().map(ColorProblem::rule).toList(), e.getMessage());
        assertTrue(e.getMessage().contains("'danger' names " + Danger.class.getName() + " and " + Danger2.class.getName()), e.getMessage());
        assertTrue(e.getMessage().contains("astray hangs under Lost, which the tree has not"), e.getMessage());
        assertTrue(e.getMessage().contains("short has 2 knots: a scale has three or more"), e.getMessage());
        assertTrue(e.getMessage().contains("falling's knots run from -1.0 to 0.9"), e.getMessage());
        assertTrue(e.getMessage().contains("falling's knot at 0.25 follows one at 0.5: its knots rise strictly"), e.getMessage());
        assertTrue(e.getMessage().contains("off is at 2.0: a degree is from -1 to 1"), e.getMessage());
        assertTrue(e.getMessage().contains("none takes 0.0 of the colour it is given: a share is more than none and at most the whole of it, 1"), e.getMessage());
        var twice = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(List.of(new Signals(), new Safe(), new Safe())));
        assertTrue(twice.getMessage().contains("safe is listed twice"), twice.getMessage());
    }

    // ── a scheme's layers ──────────────────────────────────────────────────

    /** The loud scheme with nothing of its own: it means nothing. */
    public record Quiet() implements SchemeTreeReader<BaseColours.LoudScheme> {
        @Override public List<? extends ColorNode<BaseColours.LoudScheme, ?, ?>> layer() { return List.of(); }
        @Override public Optional<SchemeTreeReader<? super BaseColours.LoudScheme>> below() { return Optional.of(BaseColours.INSTANCE); }
    }

    /** The loud scheme's glare, forgetting the base it is on top of. */
    public record Adrift() implements SchemeTreeReader<BaseColours.LoudScheme> {
        @Override public List<? extends ColorNode<BaseColours.LoudScheme, ?, ?>> layer() { return List.of(new Glare()); }
    }

    /** One that slips a base colour into the loud scheme's layer - the compiler refuses it but for a raw list. */
    public record Smuggling() implements SchemeTreeReader<BaseColours.LoudScheme> {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override public List<? extends ColorNode<BaseColours.LoudScheme, ?, ?>> layer() { return (List) List.of(new Glare(), new Copy()); }
        @Override public Optional<SchemeTreeReader<? super BaseColours.LoudScheme>> below() { return Optional.of(BaseColours.INSTANCE); }
    }

    /** One that sits on itself. */
    public record Circling() implements SchemeTreeReader<BaseColours.LoudScheme> {
        @Override public List<? extends ColorNode<BaseColours.LoudScheme, ?, ?>> layer() { return List.of(new Glare()); }
        @Override public Optional<SchemeTreeReader<? super BaseColours.LoudScheme>> below() { return Optional.of(new Circling()); }
    }

    @Test
    void aLayer_addsColoursOfItsSchemeAlone_overTheLayerOfTheSchemeBelow() {
        var empty = assertThrows(RefusedColors.class, () -> new Quiet().read());
        assertEquals(List.of("LAYER_EMPTY: Quiet adds no colour to LoudScheme: a scheme without colours of its own means nothing"),
                empty.problems().stream().map(ColorProblem::toString).toList());
        var adrift = assertThrows(RefusedColors.class, () -> new Adrift().read());
        assertTrue(adrift.getMessage().contains("LAYER_ASTRAY: Adrift reads LoudScheme, which is on top of BaseScheme, and sits on nothing"), adrift.getMessage());
        var smuggled = assertThrows(RefusedColors.class, () -> new Smuggling().read());
        assertTrue(smuggled.getMessage().contains("LAYER_ASTRAY: copy is BaseScheme's, in LoudScheme's layer: a layer holds its scheme's colours alone"), smuggled.getMessage());
        var circling = assertThrows(RefusedColors.class, () -> new Circling().read());
        assertTrue(circling.getMessage().contains("Circling sits, in the end, on itself"), circling.getMessage());
    }

    private static String css(Optional<ConcreteColor> c) { return c.orElseThrow().css(); }
}
