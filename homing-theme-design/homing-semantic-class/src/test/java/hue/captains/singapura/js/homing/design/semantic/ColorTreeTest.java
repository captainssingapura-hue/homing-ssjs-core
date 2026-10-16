package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;

import hue.captains.singapura.js.homing.design.color.ColorLeaf;
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
 * A scheme's colour tree read from its declarations, layer by layer from the base up to its own - each layer its scheme's
 * alone, closed, adding colours, each branch coming in with its leaves - and refused where the compiler cannot refuse it,
 * at run time what each says and when its jar is built what only its types tell (D49); a palettable colour resolved down the
 * palettes; a palette checked whole over what its scheme sees; a colour moving along a scale only where the casting
 * varies its colour by degree.
 */
class ColorTreeTest {

    private static final ColorTree TREE = ResolvingThemes.COLOURS;

    @Test
    void theTree_isTheLayersFromTheBaseUp_eachItsSchemesAlone() {
        assertEquals(16, BaseColours.TREE.nodes().size(), "the base's own - a hairline, the current colour at a share, the mines among them");
        assertEquals(17, TREE.nodes().size(), "the loud scheme's, its glare over the base's sixteen");
        assertEquals(new Glare(), TREE.nodes().get(16), "the base's layer first, the scheme's own last");
        assertTrue(!BaseColours.TREE.has(new Glare()), "the base sees nothing of the scheme on top of it");
        assertEquals(BaseColours.LoudScheme.INSTANCE, TREE.scheme(new Glare()));
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
        assertEquals(List.of("unmarked says no scheme: a palette says the scheme it implements"),
                TREE.check(new BaseColours.Unmarked()));
    }

    @Test
    void anInterpolatedColour_isWorkedOutWhenTheSheetIsMade_andOnlyOneTheTreeHas() {
        var nodes = new ArrayList<ColorNode<?, ?, ?>>(TREE.nodes());
        nodes.addAll(List.of(new AtSafe(), new MostlyClear()));
        var tree = ReadColors.INSTANCE.read(BaseScheme.INSTANCE, nodes);
        var theme = new Dressed(tree, ResolvingThemes.Valued.INSTANCE, Paper.INSTANCE);
        var render = RenderColor.INSTANCE;
        assertEquals("#16a34a", render.css(theme, new AtSafe()), "at a knot's degree, the knot's own colour");
        assertEquals("color-mix(in oklab, #16a34a 25%, transparent)", render.css(theme, new MostlyClear()), "a quarter of the way from safe, three from clear");
        var e = assertThrows(IllegalArgumentException.class, () -> render.css(ResolvingThemes.BY_NAME.get("valued"), new AtSafe()));
        assertEquals("at-safe is in no colour tree read: its scheme's layer, or one below it, lists it", e.getMessage());
    }

    /** The risk scale's first knot. */
    public record AtSafe() implements Interpolated<BaseScheme, Signals> {
        @Override public Signals parent() { return new Signals(); }
        @Override public ColorScale<? super BaseScheme, ?> scale() { return new Risk(); }
        @Override public double degree() { return -1; }
    }

    /** Three quarters of the way from safe at -1 to clear at 0. */
    public record MostlyClear() implements Interpolated<BaseScheme, Signals> {
        @Override public Signals parent() { return new Signals(); }
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

        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Coloring<BaseScheme, ?>> colorings() {
            return List.of(new Coloring<>(Button.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Painted<>(new Paint.Along<>(new Risk()))),
                           new Coloring<>(Root.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Painted<>(new Paint.Inherited<>())));
        }
    }

    // ── refused as read ────────────────────────────────────────────────────

    public record Lost() implements L1_ColorBranch<BaseScheme> {}
    public record Astray() implements Palettable<BaseScheme, Lost> { @Override public Lost parent() { return new Lost(); } }
    public record Danger2() implements Palettable<BaseScheme, Signals> { @Override public Signals parent() { return new Signals(); } @Override public String token() { return "danger"; } }
    public record Coloured(int shade) implements Palettable<BaseScheme, Signals> { @Override public Signals parent() { return new Signals(); } }
    public record Marker() implements L1_ColorBranch<ColorScheme> {}
    public record Inked() implements Palettable<BaseScheme, Marker> { @Override public Marker parent() { return new Marker(); } }
    public record Short() implements ColorScale<BaseScheme, Signals> {
        @Override public Signals parent() { return new Signals(); }
        @Override public List<Knot<BaseScheme>> knots() { return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Danger(), 1)); }
    }
    public record Falling() implements ColorScale<BaseScheme, Signals> {
        @Override public Signals parent() { return new Signals(); }
        @Override public List<Knot<BaseScheme>> knots() {
            return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Danger(), 0.5), new Knot<>(new Copy(), 0.25), new Knot<>(new Danger(), 0.9));
        }
    }
    public record Off() implements Interpolated<BaseScheme, Signals> {
        @Override public Signals parent() { return new Signals(); }
        @Override public ColorScale<? super BaseScheme, ?> scale() { return new Risk(); }
        @Override public double degree() { return 2; }
    }
    public record Blank() implements Fixed<BaseScheme, Signals> { @Override public Signals parent() { return new Signals(); } @Override public ConcreteColor color() { return null; } }
    public record None() implements CurrentColor<BaseScheme, Signals> { @Override public Signals parent() { return new Signals(); } @Override public double share() { return 0; } }

    @Test
    void whatTheCompilerCannotRefuse_isRefusedAsTheTreeIsRead_everyProblemAtOnce() {
        List<ColorNode<?, ?, ?>> own = List.of(new BaseColours.Clear(), new Safe(), new Danger(), new Copy(), new Risk(),
                new Danger2(), new Short(), new Falling(), new Off(), new Blank(), new None());
        var e = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(BaseScheme.INSTANCE, own));
        assertEquals(List.of(Rule.NAME_TWICE, Rule.SCALE_BADLY, Rule.SCALE_BADLY, Rule.SCALE_BADLY, Rule.INTERPOLATED_BADLY, Rule.FIXED_BADLY, Rule.SHARE_BADLY),
                e.problems().stream().map(ColorProblem::rule).toList(), e.getMessage());
        assertTrue(e.getMessage().contains("'danger' names " + Danger.class.getName() + " and " + Danger2.class.getName()), e.getMessage());
        assertTrue(e.getMessage().contains("short has 2 knots: a scale has three or more"), e.getMessage());
        assertTrue(e.getMessage().contains("falling's knots run from -1.0 to 0.9"), e.getMessage());
        assertTrue(e.getMessage().contains("falling's knot at 0.25 follows one at 0.5: its knots rise strictly"), e.getMessage());
        assertTrue(e.getMessage().contains("off is at 2.0: a degree is from -1 to 1"), e.getMessage());
        assertTrue(e.getMessage().contains("none takes 0.0 of the colour it is given: a share is more than none and at most the whole of it, 1"), e.getMessage());
        var twice = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(BaseScheme.INSTANCE, List.of(new Safe(), new Safe())));
        assertTrue(twice.getMessage().contains("safe is listed twice"), twice.getMessage());
        ColorTree astray = ReadColors.INSTANCE.read(BaseScheme.INSTANCE, List.of(new Astray()));
        assertEquals(new Lost(), astray.parent(new Astray()).orElseThrow(), "a leaf's branch comes in with it, said by the leaf: the tree never lacks a parent");
    }

    // ── a scheme's layers ──────────────────────────────────────────────────

    /** The loud scheme with nothing of its own: it means nothing. */
    public record Quiet() implements SchemeDeclaration<BaseColours.LoudScheme> {
        @Override public BaseColours.LoudScheme scheme() { return BaseColours.LoudScheme.INSTANCE; }
        @Override public List<? extends ColorLeaf<BaseColours.LoudScheme, ?, ?>> leaves() { return List.of(); }
    }

    /** One that slips colours of the base into the loud scheme's layer - the compiler refuses it but for a raw list. */
    public record Smuggling() implements SchemeDeclaration<BaseColours.LoudScheme> {
        @Override public BaseColours.LoudScheme scheme() { return BaseColours.LoudScheme.INSTANCE; }
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override public List<? extends ColorLeaf<BaseColours.LoudScheme, ?, ?>> leaves() { return (List) List.of(new Glare(), new Astray(), new Coloured(1), new Inked()); }
    }

    /** A scheme that says it is on top of itself. */
    public static class CirclingScheme extends BaseScheme {
        public static final CirclingScheme INSTANCE = new CirclingScheme();
        @Override public Optional<ColorScheme> below() { return Optional.of(INSTANCE); }
    }
    public record Glow() implements Palettable<CirclingScheme, Signals> { @Override public Signals parent() { return new Signals(); } }
    public record Circling() implements SchemeDeclaration<CirclingScheme> {
        @Override public CirclingScheme scheme() { return CirclingScheme.INSTANCE; }
        @Override public List<? extends ColorLeaf<CirclingScheme, ?, ?>> leaves() { return List.of(new Glow()); }
    }

    /** A scheme that extends the base and says it is on top of nothing; and one that holds a state. */
    public static class Mislaid extends BaseScheme { public static final Mislaid INSTANCE = new Mislaid(); }
    public static class Stateful extends BaseScheme {
        public static final Stateful INSTANCE = new Stateful();
        private final int shade = 1;
        @Override public Optional<ColorScheme> below() { return Optional.of(BaseScheme.INSTANCE); }
    }

    @Test
    void aLayer_addsColoursOfItsSchemeAlone_overTheLayerOfTheSchemeBelow() {
        var empty = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(List.of(BaseColours.INSTANCE, new Quiet())));
        assertEquals(List.of("LAYER_EMPTY: Quiet adds no colour to LoudScheme: a scheme without colours of its own means nothing"),
                empty.problems().stream().map(ColorProblem::toString).toList());
        var adrift = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(List.of(BaseColours.LoudColours.INSTANCE)));
        assertTrue(adrift.getMessage().contains("LAYER_ASTRAY: LoudColours declares LoudScheme, which is on top of BaseScheme, and sits on nothing"), adrift.getMessage());
        String unregistered = assertThrows(IllegalArgumentException.class, () -> new SchemeRegistry(List.of(BaseColours.LoudColours.INSTANCE))).getMessage();
        assertTrue(unregistered.contains("LoudScheme is on top of BaseScheme, which no registered declaration declares: a scheme's schemes below are "
                + "registered with it"), unregistered);
        String twice = assertThrows(IllegalArgumentException.class, () -> new SchemeRegistry(List.of(BaseColours.INSTANCE, BaseColours.INSTANCE))).getMessage();
        assertTrue(twice.contains("BaseScheme is declared twice"), twice);
        String circling = assertThrows(IllegalArgumentException.class, () -> new SchemeRegistry(List.of(BaseColours.INSTANCE, new Circling()))).getMessage();
        assertTrue(circling.contains("CirclingScheme's schemes below come back to CirclingScheme"), circling);
        String nothing = assertThrows(IllegalArgumentException.class, () -> new SchemeRegistry(List.of())).getMessage();
        assertTrue(nothing.contains("it registers no scheme"), nothing);
        String every = assertThrows(IllegalArgumentException.class,
                () -> new SchemeRegistry(List.of(BaseColours.INSTANCE, BaseColours.INSTANCE, new Circling(), new Quiet()))).getMessage();
        assertTrue(every.contains("BaseScheme is declared twice") && every.contains("CirclingScheme's schemes below come back")
                && every.contains("LoudScheme's tree is refused") && every.contains("LAYER_EMPTY: Quiet adds no colour to LoudScheme"), "every rule applied apart, each problem kept: " + every);
        assertEquals(new Validation.Invalid(List.of("a", "b")), new Validation.Invalid(List.of("a")).and(Validation.Valid.INSTANCE).and(new Validation.Invalid(List.of("b"))),
                "results combine, every problem kept");
    }

    @Test
    void whatOnlyADeclarationsTypesTell_isCheckedWhenItsJarIsBuilt_neverAtRunTime() {
        assertEquals(List.of(), CheckScheme.INSTANCE.check(BaseColours.INSTANCE, BaseColours.SCHEMES), "the tests' base holds");
        assertEquals(List.of(), CheckScheme.INSTANCE.check(BaseColours.LoudColours.INSTANCE, BaseColours.SCHEMES), "and the loud scheme on it");
        SchemeRegistry smuggled = new SchemeRegistry(List.of(BaseColours.INSTANCE, new Smuggling()));
        assertTrue(SchemeTrees.INSTANCE.tree(smuggled, BaseColours.LoudScheme.INSTANCE).has(new Astray()), "read at run time, where nothing reads the type that says astray is the base's");
        List<String> told = CheckScheme.INSTANCE.check(new Smuggling(), smuggled);
        assertTrue(told.contains("astray is BaseScheme's, and came into LoudScheme's layer: a branch comes in with a leaf of its own scheme, and its scheme "
                + "lists it below"), told.toString());
        assertTrue(told.contains("lost is BaseScheme's, and came into LoudScheme's layer: a branch comes in with a leaf of its own scheme, and its scheme "
                + "lists it below"), "its branch too: " + told);
        assertTrue(told.contains("coloured is a record with components: a node is a stateless record, its type its meaning"), told.toString());
        assertTrue(told.stream().anyMatch(t -> t.startsWith("marker is ColorScheme's")), "a branch of the marker: " + told);
        assertEquals(List.of("Mislaid extends BaseScheme, and says it is on top of nothing"), CheckScheme.INSTANCE.scheme(Mislaid.INSTANCE));
        assertEquals(List.of("Stateful holds shade: a scheme is stateless"), CheckScheme.INSTANCE.scheme(Stateful.INSTANCE));
        assertEquals(List.of("unmarked says no scheme"), CheckScheme.INSTANCE.says(new BaseColours.Unmarked()));
        assertEquals(List.of(), CheckScheme.INSTANCE.says(Paper.INSTANCE), "a palette saying the scheme its type names");
    }

    private static String css(Optional<ConcreteColor> c) { return c.orElseThrow().css(); }
}
