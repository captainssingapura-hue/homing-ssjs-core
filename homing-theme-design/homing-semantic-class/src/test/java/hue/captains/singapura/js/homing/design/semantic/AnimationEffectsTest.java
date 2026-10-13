package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Ants;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Area;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Line;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Overlay;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Pad;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Plate;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Strip;
import hue.captains.singapura.js.homing.design.semantic.AnimatingTaxonomy.Sweep;
import hue.captains.singapura.js.homing.design.semantic.DesignProblem.Rule;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Animation, a target of its own (RFC 0050 · Episode 2, appendix The Copied Range): answered with an effect, whole,
 * never with a body. An effect's CSS is its own, sealed and written by the framework - its class, its still dual,
 * its keyframes once - and refused unless it is its own to write. On a component it may take nothing; on a design's
 * leaf it takes what it needs, and nothing else the leaf owns may write it.
 */
class AnimationEffectsTest extends JsModuleTestBase {

    private static final Taxonomy TAXONOMY = AnimatingTaxonomy.INSTANCE.read();
    private static final Dressed VALUED = ResolvingThemes.BY_NAME.get("valued");
    private static final Target.Animation.Effect EFFECT = Target.Animation.Effect.INSTANCE;

    // ── an effect, judged and written ──────────────────────────────────────

    @Test
    void anEffectTakesWhatItWrites_andItsCssIsWrittenForIt() {
        var effects = Effects.INSTANCE;
        assertEquals("ants", Ants.INSTANCE.token());
        assertEquals(Set.of("stroke-dasharray", "stroke-dashoffset", "stroke-width"), effects.takes(Ants.INSTANCE));
        assertEquals("""
                stroke-width: 1.5px;
                stroke-dasharray: 5 4;
                animation: effect-ants 0.6s linear infinite;
                @media (prefers-reduced-motion: reduce) {
                    stroke-width: 1.5px;
                    stroke-dasharray: 5 4;
                    animation: none;
                }""", effects.moving(Ants.INSTANCE), "the moving form, its motion by the keyframes' name, and its still dual where motion is reduced");
        assertEquals("stroke-width: 1.5px;\nstroke-dasharray: 5 4;\nanimation: none;", effects.still(Ants.INSTANCE), "the effect without any motion");
        assertEquals("@keyframes effect-ants {\n    to { stroke-dashoffset: -9; }\n}", effects.keyframes(Ants.INSTANCE));
        String sweep = effects.moving(Sweep.INSTANCE);
        assertTrue(sweep.contains("    background-image: unset;\n    background-size: unset;\n    background-repeat: unset;\n    animation: none;"),
                "what only the moving form declares is unset where motion is reduced:\n" + sweep);
        assertEquals("animation: none;", effects.still(Sweep.INSTANCE), "still, no band");
        assertEquals(effects.keyframes(Ants.INSTANCE) + "\n" + effects.keyframes(Sweep.INSTANCE),
                effects.keyframes(List.of(Ants.INSTANCE, Sweep.INSTANCE, Ants.INSTANCE, AnimationEffect.None.INSTANCE)), "each once; none for no motion");
    }

    @Test
    void noEffect_movesNothing_andTakesNothing() {
        var none = AnimationEffect.None.INSTANCE;
        assertEquals("none", none.token());
        assertFalse(none.moves());
        assertEquals(Set.of(), Effects.INSTANCE.takes(none));
        assertEquals("animation: none;", Effects.INSTANCE.moving(none));
        assertEquals("animation: none;", Effects.INSTANCE.still(none));
        assertEquals("", Effects.INSTANCE.keyframes(none));
    }

    @Test
    void anEffectWhoseCssIsNotItsOwn_isRefusedByName() {
        assertRefused(new Bad("stroke: red;", "", "", ""), "names a colour, red");
        assertRefused(new Bad("animation: spin 1s;", "", "", ""), "its motion is the framework's");
        assertRefused(new Bad("stroke-width: var(--elsewhere);", "", "", ""), "reads --elsewhere");
        assertRefused(new Bad("&:hover { stroke-width: 2px; }", "", "", ""), "declares in a block");
        assertRefused(new Bad("", "", "middle { stroke-width: 2px; }", "1s"), "is no frame");
        assertRefused(new Bad("", "", "to { stroke-width: 2px; }", ""), "frames and no timing");
        assertRefused(new Bad("", "", "to { stroke-width: 2px; }", "1s; color: red"), "a duration, an easing");
        assertRefused(new Bad("", "", "to { }", "1s"), "frames move nothing");
    }

    // ── answered by a style ────────────────────────────────────────────────

    @Test
    void anAnimationIsAnsweredWithAnEffect_neverABody() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> new Implementation<>(Root.INSTANCE, EFFECT, new Answer.Own<>("animation: none;")));
        assertTrue(e.getMessage().contains("answered with an animation effect, never with a body"), e.getMessage());
        e = assertThrows(IllegalArgumentException.class, () -> new Drawing.Own<>(EFFECT, "animation: none;"));
        assertTrue(e.getMessage().contains("drawn with an animation effect"), e.getMessage());
    }

    @Test
    void onAComponent_anEffectTakesNothing() {
        var e = assertThrows(IllegalArgumentException.class, () -> new Implementation<>(Root.INSTANCE, EFFECT, new Answer.Animated(Ants.INSTANCE)));
        assertTrue(e.getMessage().contains("takes stroke-dasharray, stroke-dashoffset, stroke-width: on a component every target's class is worn"),
                e.getMessage());
        new Implementation<>(Root.INSTANCE, EFFECT, new Answer.Animated(AnimationEffect.None.INSTANCE));
    }

    @Test
    void aStylesEffect_resolvesAtRest_asTheEffectsCss() {
        Taxonomy taxonomy = ResolvingTaxonomy.INSTANCE.read();
        Casting button = taxonomy.castings().get(0);
        var r = ResolveSemanticClass.INSTANCE.resolve(taxonomy, VALUED, button, State.REST, EFFECT);
        var a = assertInstanceOf(Resolution.Answered.class, r.outcome(), String.join("\n", r.explain()));
        assertEquals(AnimationEffect.None.INSTANCE, a.effect());
        assertEquals("animation: none;", a.body());
        assertTrue(a.className().endsWith("__animation-effect"), a.className());
        assertInstanceOf(Resolution.Silent.class, ResolveSemanticClass.INSTANCE.resolve(taxonomy, VALUED, button, State.HOVER, EFFECT).outcome(),
                "an effect is a look, at rest");
        var ink = ResolveSemanticClass.INSTANCE.resolve(taxonomy, VALUED, button, State.REST, Target.Shape.Corner.INSTANCE);
        assertNull(((Resolution.Answered) ink.outcome()).effect(), "an answer of CSS is no effect");
    }

    // ── drawn by a design ──────────────────────────────────────────────────

    record Marching() implements Design<Overlay> { static final Marching INSTANCE = new Marching(); }

    record MarchingLine() implements Line.Concrete<Marching>, Drawn {
        static final MarchingLine INSTANCE = new MarchingLine();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(Target.Color.Stroke.INSTANCE, "stroke: currentColor;"), new Drawing.Animated(Ants.INSTANCE));
        }
    }

    record MarchingArea() implements Area.Concrete<Marching>, Drawn {
        static final MarchingArea INSTANCE = new MarchingArea();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Color.Background.INSTANCE), new Drawing.Animated(Sweep.INSTANCE));
        }
    }

    record MarchingPlate() implements Plate.Concrete<Marching>, Drawn {
        static final MarchingPlate INSTANCE = new MarchingPlate();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(Target.Color.Background.INSTANCE)); }
    }

    record MarchingStrip() implements Strip.Concrete<Marching>, Drawn {
        static final MarchingStrip INSTANCE = new MarchingStrip();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Shape.Background.INSTANCE), new Drawing.Animated(Ants.INSTANCE));
        }
    }

    /** Takes its line's effect from the marching line, and its area's from the theme's pad: no effect. */
    record Borrowed() implements Design<Overlay> { static final Borrowed INSTANCE = new Borrowed(); }

    record BorrowedLine() implements Line.Concrete<Borrowed>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Color.Stroke.INSTANCE), new Drawing.As<>(EFFECT, MarchingLine.INSTANCE));
        }
    }

    record BorrowedArea() implements Area.Concrete<Borrowed>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Color.Background.INSTANCE), new Drawing.Of<>(EFFECT, Pad.INSTANCE));
        }
    }

    record BorrowedPlate() implements Plate.Concrete<Borrowed>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(Target.Color.Background.INSTANCE)); }
    }

    record BorrowedStrip() implements Strip.Concrete<Borrowed>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Shape.Background.INSTANCE), new Drawing.None<>(EFFECT));
        }
    }

    /** Sweeps its strip, whose image the strip's own background writes: refused. */
    record Clashing() implements Design<Overlay> { static final Clashing INSTANCE = new Clashing(); }

    record ClashingLine() implements Line.Concrete<Clashing>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Color.Stroke.INSTANCE), new Drawing.None<>(EFFECT));
        }
    }

    record ClashingArea() implements Area.Concrete<Clashing>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Color.Background.INSTANCE), new Drawing.None<>(EFFECT));
        }
    }

    record ClashingPlate() implements Plate.Concrete<Clashing>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(Target.Color.Background.INSTANCE)); }
    }

    record ClashingStrip() implements Strip.Concrete<Clashing>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(Target.Shape.Background.INSTANCE), new Drawing.Animated(Sweep.INSTANCE));
        }
    }

    private static final List<ConcreteComponent<?, ?, ?>> MARCHING =
            List.of(MarchingLine.INSTANCE, MarchingArea.INSTANCE, MarchingPlate.INSTANCE, MarchingStrip.INSTANCE);

    @Test
    void aLeafsEffect_isItsClass_itsStillDualBeside_andItsKeyframesOnceAtTheHead() {
        DesignRead marching = ReadDesigns.INSTANCE.read(TAXONOMY, List.of(VALUED), MARCHING).get(0);
        var flat = FlattenDesign.INSTANCE.flatten(marching, VALUED, "Marching");
        String css = flat.css();
        int ants = css.indexOf("@keyframes effect-ants {"), sweep = css.indexOf("@keyframes effect-sweep {");
        assertTrue(ants > 0 && sweep > ants, "each moving effect's keyframes, in the order first worn:\n" + css);
        assertEquals(ants, css.lastIndexOf("@keyframes effect-ants {"), "the ants, worn twice, keyframed once");
        assertTrue(ants < css.indexOf(".marching___"), "ahead of every class");
        assertTrue(css.contains("/* ants */\n.marching___line__animation-effect {\n    stroke-width: 1.5px;\n    stroke-dasharray: 5 4;\n"
                + "    animation: effect-ants 0.6s linear infinite;\n    @media (prefers-reduced-motion: reduce) {\n"), css);
        assertTrue(css.contains(".marching___line__animation-effect-still {\n    stroke-width: 1.5px;\n    stroke-dasharray: 5 4;\n    animation: none;\n}\n"), css);
        assertTrue(css.contains(".marching___area__animation-effect-still {\n    animation: none;\n}\n"), css);
        assertTrue(css.contains(".marching___line__color-stroke {\n    stroke: currentColor;\n}\n"), "the line's ink, its own");
        assertEquals(4, flat.drawn(), "the line's ink and effect, the area's effect, the strip's effect");
    }

    @Test
    void anEffectTakenFromAnotherLeaf_orFromTheTheme_isTheEffect() {
        var read = ReadDesigns.INSTANCE.read(TAXONOMY, List.of(VALUED),
                List.of(MarchingLine.INSTANCE, MarchingArea.INSTANCE, MarchingPlate.INSTANCE, MarchingStrip.INSTANCE,
                        new BorrowedLine(), new BorrowedArea(), new BorrowedPlate(), new BorrowedStrip()));
        DesignRead borrowed = read.stream().filter(d -> d.token().equals("borrowed")).findFirst().orElseThrow();
        String css = FlattenDesign.INSTANCE.flatten(borrowed, VALUED, "Borrowed").css();
        assertTrue(css.contains("/* from marching___line__animation-effect */\n/* ants */\n.borrowed___line__animation-effect {\n"), css);
        assertTrue(css.contains("/* the theme's pad */\n/* none */\n.borrowed___area__animation-effect {\n    animation: none;\n}\n"),
                "the pad's animation, as the valued style answers it at the root - no effect:\n" + css);
        assertEquals(1, css.split("@keyframes effect-ants", -1).length - 1);
    }

    @Test
    void anEffectThatTakesWhatTheLeafsOtherTargetWrites_isRefused() {
        var e = assertThrows(RefusedDesigns.class, () -> ReadDesigns.INSTANCE.read(TAXONOMY, List.of(VALUED),
                List.of(new ClashingLine(), new ClashingArea(), new ClashingPlate(), new ClashingStrip())));
        assertEquals(List.of(Rule.EFFECT_TAKES_OWNED, Rule.EFFECT_TAKES_OWNED, Rule.EFFECT_TAKES_OWNED, Rule.EFFECT_TAKES_OWNED),
                e.problems().stream().map(DesignProblem::rule).toList(), e.getMessage());
        assertTrue(e.getMessage().contains("wears sweep, which takes background-image - and strip owns shape-background, which writes it"), e.getMessage());
    }

    // ── handed to a page: the still dual swapped ────────────────────────────

    @Test
    void aDesignsObject_switchesALeafsEffectOffAndOn_bySwappingItsClassForItsStillDual() {
        DesignRead marching = ReadDesigns.INSTANCE.read(TAXONOMY, List.of(VALUED), MARCHING).get(0);
        js = buildContext();
        js.eval("js", "var marching_sheet = 'the sheet';"
                + " function El() { var s = new Set(); this.classList = { add: function (c) { s.add(c); }, remove: function (c) { s.delete(c); } };"
                + " this.classes = function () { return Array.from(s).sort().join(' '); }; }");
        js.eval("js", String.join("\n", DesignLeaves.INSTANCE.lines(TAXONOMY, Overlay.INSTANCE)));
        js.eval("js", String.join("\n", DesignObject.INSTANCE.lines(marching, "marching_sheet")));
        assertEquals("marching___line__animation-effect marching___line__color-stroke",
                js.eval("js", "var el = new El(); Marching.wear(OVERLAY.LINE, el); el.classes()").asString());
        assertEquals("marching___line__animation-effect-still marching___line__color-stroke",
                js.eval("js", "Marching.still(OVERLAY.LINE, el); el.classes()").asString(), "off: the effect's class swapped for its still dual's");
        assertEquals("marching___line__animation-effect marching___line__color-stroke",
                js.eval("js", "Marching.move(OVERLAY.LINE, el); el.classes()").asString(), "and on again");
        assertEquals("", js.eval("js", "Marching.still(OVERLAY.LINE, el); Marching.shed(OVERLAY.LINE, el); el.classes()").asString(),
                "shedding a leaf takes its still dual too");
        assertTrue(js.eval("js", "(function () { try { Marching.still(OVERLAY.PLATE, new El()); return ''; } catch (e) { return e.message; } })()")
                .asString().contains("'plate' wears no animation effect"));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /** An effect written by a test, to be refused. */
    record Bad(String moving, String still, String frames, String timing) implements AnimationEffect {
        @Override public String meaning() { return "a test's"; }
        @Override public List<Target.Color> drawnIn() { return List.of(Target.Color.Stroke.INSTANCE); }
    }

    private static void assertRefused(AnimationEffect effect, String says) {
        var e = assertThrows(IllegalArgumentException.class, () -> Effects.INSTANCE.check(effect));
        assertTrue(e.getMessage().contains(says), "expected '" + says + "' in: " + e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> new Answer.Animated(effect), "and refused as an answer");
    }
}
