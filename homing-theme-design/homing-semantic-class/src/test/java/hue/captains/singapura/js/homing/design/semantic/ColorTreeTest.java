package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ColorScheme.Base;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.L1_ColorBranch;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorProblem.Rule;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Danger;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Glare;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Neon;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Night;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Paper;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Risk;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Safe;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Signals;
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
 * A colour tree read from its owner's nodes and its extensions' grafts, refused where the compiler cannot refuse it;
 * a palettable colour resolved through the two layers; a palette checked whole over what its scheme sees; a colour
 * moving along a scale only where the casting varies its colour by degree.
 */
class ColorTreeTest {

    private static final ColorTree TREE = ResolvingThemes.COLOURS;

    @Test
    void theTree_isTheOwnersNodes_thenEachExtensionsFoundOnTheClasspath() {
        assertEquals(17, TREE.nodes().size(), "fourteen of its owner's - a hairline, the current colour at a share, among them - three the mine colours graft");
        assertTrue(TREE.extension(new MineColours.Flag()).orElseThrow() instanceof MineColours);
        assertTrue(TREE.extension(new Copy()).isEmpty(), "the owner's own: no extension");
        assertEquals(ResolvingColours.Loud.class, TREE.scheme(new Glare()));
        assertEquals(new Signals(), TREE.parent(new Glare()).orElseThrow(), "a scheme's colour, under the base's branch");
        assertTrue(TREE.parent(new Signals()).isEmpty(), "a branch at the top");
    }

    /** The selected palette, then the extension's word for it; the palettes below, then the extension's last word. */
    @Test
    void aPalettableColour_resolvesInTwoLayers_theFirstAnswerWinning() {
        var flag = new MineColours.Flag();
        assertEquals("#e53935", css(TREE.resolve(Paper.INSTANCE, flag)), "paper knows no flag: the extension's word for paper");
        assertEquals("#ff6f00", css(TREE.resolve(Night.INSTANCE, flag)), "the night knows the flag: a palette's own wins over the extension's");
        assertEquals("#d32f2f", css(TREE.resolve(Neon.INSTANCE, flag)), "neither neon nor paper below it: the extension's last word");
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
        assertEquals(List.of("unsafe leaves safe unanswered"), TREE.check(new ResolvingColours.Unsafe()));
        assertEquals(List.of("stranded sits on nothing, and Loud is on top of the base: it sits on a palette of the base"),
                TREE.check(new ResolvingColours.Stranded()));
        assertEquals(List.of("unmarked implements no scheme: its type argument is an abstract class on top of the base, or one from scratch"),
                TREE.check(new ResolvingColours.Unmarked()));
    }

    @Test
    void anInterpolatedColour_isWorkedOutWhenTheSheetIsMade_andOnlyOneTheTreeHas() {
        var nodes = new ArrayList<ColorNode<?, ?, ?>>(TREE.nodes());
        nodes.addAll(List.of(new AtSafe(), new MostlyClear()));
        var tree = ReadColors.INSTANCE.read(nodes, List.of());
        var theme = new Dressed(tree, ResolvingThemes.Valued.INSTANCE, Paper.INSTANCE);
        var render = RenderColor.INSTANCE;
        assertEquals("#16a34a", render.css(theme, new AtSafe()), "at a knot's degree, the knot's own colour");
        assertEquals("color-mix(in oklab, #16a34a 25%, transparent)", render.css(theme, new MostlyClear()), "a quarter of the way from safe, three from clear");
        var e = assertThrows(IllegalArgumentException.class, () -> render.css(ResolvingThemes.BY_NAME.get("valued"), new AtSafe()));
        assertEquals("at-safe is in no colour tree read: its owner or an extension lists it", e.getMessage());
    }

    /** The risk scale's first knot. */
    public record AtSafe() implements Interpolated<Base, Signals> {
        @Override public ColorScale<? super Base, ?> scale() { return new Risk(); }
        @Override public double degree() { return -1; }
    }

    /** Three quarters of the way from safe at -1 to clear at 0. */
    public record MostlyClear() implements Interpolated<Base, Signals> {
        @Override public ColorScale<? super Base, ?> scale() { return new Risk(); }
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
    public record Swaying() implements DesignStyle<Base> {
        @Override public List<Implementation<?>> implementations() { return List.of(); }
        @Override public List<Coloring<Base, ?>> colorings() {
            return List.of(new Coloring<>(Button.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Painted<>(new Paint.Along<>(new Risk()))),
                           new Coloring<>(Root.INSTANCE, Target.Color.Text.INSTANCE, new Answer.Painted<>(new Paint.Inherited<>())));
        }
    }

    // ── refused as read ────────────────────────────────────────────────────

    public record Lost() implements L1_ColorBranch<Base> {}
    public record Astray() implements Palettable<Base, Lost> {}
    public record Danger2() implements Palettable<Base, Signals> { @Override public String token() { return "danger"; } }
    public record Coloured(int shade) implements Palettable<Base, Signals> {}
    public record Marker() implements L1_ColorBranch<ColorScheme> {}
    public record Short() implements ColorScale<Base, Signals> {
        @Override public List<Knot<Base>> knots() { return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Danger(), 1)); }
    }
    public record Falling() implements ColorScale<Base, Signals> {
        @Override public List<Knot<Base>> knots() {
            return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Danger(), 0.5), new Knot<>(new Copy(), 0.25), new Knot<>(new Danger(), 0.9));
        }
    }
    public record Off() implements Interpolated<Base, Signals> {
        @Override public ColorScale<? super Base, ?> scale() { return new Risk(); }
        @Override public double degree() { return 2; }
    }
    public record Blank() implements Fixed<Base, Signals> { @Override public ConcreteColor color() { return null; } }
    public record None() implements CurrentColor<Base, Signals> { @Override public double share() { return 0; } }
    public record Graft() implements ColorExtension {
        @Override public Class<? extends ColorSource> extending() { return ResolvingColours.class; }
        @Override public List<ColorNode<?, ?, ?>> nodes() { return List.of(new Lost(), new Astray()); }
    }

    @Test
    void whatTheCompilerCannotRefuse_isRefusedAsTheTreeIsRead_everyProblemAtOnce() {
        List<ColorNode<?, ?, ?>> own = List.of(new ResolvingColours.Surfaces(), new ResolvingColours.Words(), new Signals(), new ResolvingColours.Clear(),
                new Safe(), new Danger(), new Copy(), new Risk(),
                new Astray(), new Danger2(), new Coloured(1), new Marker(), new Short(), new Falling(), new Off(), new Blank(), new None());
        var e = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(own, List.of()));
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
        var twice = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(List.of(new Signals(), new Safe()), List.of(new Graft(), new Graft())));
        assertTrue(twice.getMessage().contains("lost is listed twice by Graft and by Graft"), twice.getMessage());
        var lastWord = assertThrows(RefusedColors.class, () -> ReadColors.INSTANCE.read(List.of(), List.of(new Graft())));
        assertEquals(List.of("LAST_WORD_MISSING: Graft grafts 1 palettable colour and has no base sub-palette: its last word answers every one"),
                lastWord.problems().stream().map(ColorProblem::toString).toList());
    }

    private static String css(Optional<ConcreteColor> c) { return c.orElseThrow().css(); }
}
