package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.design.Deployment.Finding;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A deployment resolves its requirement set against a design — a function — and refuses what must be refused. */
class DeploymentTest {

    // ── the pairs a danger button wears ─────────────────────────────────
    static final DesignClass<Target.Color.Surface>    DANGER_SURFACE = of(Feedback.Danger.class, Target.Color.Surface.class);
    static final DesignClass<Target.Color.Surface>    SUCCESS_SURFACE = of(Feedback.Success.class, Target.Color.Surface.class);
    static final DesignClass<Target.Color.Ink>        ON_DANGER_INK = of(Pairing.OnDanger.class, Target.Color.Ink.class);
    static final DesignClass<Target.Motion.Transform> PRESS = of(Interaction.Interactive.class, Target.Motion.Transform.class);
    static final DesignClass<Target.Motion.Ease>      EASE = of(Interaction.Interactive.class, Target.Motion.Ease.class);
    static final DesignClass<Target.Shape.Corner>     CORNER = of(Box.Control.class, Target.Shape.Corner.class);
    static final DesignClass<Target.Size.Inset>       INSET = of(Box.Control.class, Target.Size.Inset.class);
    static final DesignClass<Target.Type.Face>        FACE = of(Text.Label.class, Target.Type.Face.class);
    static final DesignClass<Target.Sound.Cue>        CUE = of(Feedback.Danger.class, Target.Sound.Cue.class);

    static final Set<DesignClass<?>> DANGER_BUTTON = Set.of(DANGER_SURFACE, ON_DANGER_INK, PRESS, EASE, CORNER, INSET, FACE);

    // ── a base design: a map ─────────────────────────────────────────────
    record Plain() implements Design {
        static final Map<DesignClass<?>, Impl> WORDS = Map.of(
                DANGER_SURFACE, Impl.Bindings.none().at(State.REST, "background-color", "#B00020").at(State.HOVER, "background-color", "#C51F31").in(Mode.DARK, State.REST, "background-color", "#FF5A6E"),
                SUCCESS_SURFACE, Impl.Bindings.none().at(State.REST, "background-color", "#0A7D3A"),
                ON_DANGER_INK, Impl.Bindings.of("#FFFFFF"),
                PRESS, Impl.Bindings.none().at(State.ACTIVE, "scale(.97)"),
                EASE, Impl.Bindings.of("transform 80ms, background-color 80ms"),
                CORNER, Impl.Bindings.of("4px"),
                INSET, Impl.Bindings.of("6px 12px"),
                FACE, Impl.Bindings.of("system-ui, sans-serif"));
        @Override public String slug() { return "plain"; }
        @Override public Impl impl(DesignClass<?> pair) { return WORDS.get(pair); }
    }

    // ── a theme over it: three opinions, the base for the rest ──────────
    record Brutal() implements Design {
        static final Map<DesignClass<?>, Impl> OWN = Map.of(
                DANGER_SURFACE, Impl.Bindings.none().at(State.REST, "background-color", "#FFE800"),
                CORNER, Impl.Bindings.of("0"),
                PRESS, Impl.Silence.css());
        @Override public String slug() { return "brutal"; }
        @Override public Impl impl(DesignClass<?> pair) {
            Impl own = OWN.get(pair);
            return own != null ? own : new Plain().impl(pair);
        }
    }

    @Test
    void aCompleteDesign_resolvesEveryRequiredPair() {
        var r = Deployment.of(DANGER_BUTTON, new Plain()).resolve();
        assertEquals(List.of(), r.findings());
        assertEquals(DANGER_BUTTON, r.impls().keySet());
    }

    @Test
    void aThemeOverABase_isCompleteThroughTheBase_andItsOwnWordWins() {
        var r = Deployment.of(DANGER_BUTTON, new Brutal()).resolve();
        assertEquals(List.of(), r.findings());
        var danger = assertInstanceOf(Impl.Bindings.class, r.impls().get(DANGER_SURFACE));
        assertEquals("#FFE800", danger.values().get(Mode.LIGHT).get(State.REST).get("background-color"));
        assertInstanceOf(Impl.Silence.class, r.impls().get(PRESS), "silence is a fulfilment");
        assertInstanceOf(Impl.Bindings.class, r.impls().get(ON_DANGER_INK), "the base answers what the theme did not");
    }

    @Test
    void aMissingPair_isNamed() {
        var r = Deployment.of(Set.of(DANGER_SURFACE, CUE), new Plain()).resolve();
        assertEquals(1, r.findings().size(), r.findings().toString());
        assertEquals(Finding.Kind.MISSING, r.findings().get(0).kind());
        assertEquals(CUE, r.findings().get(0).designClass());
    }

    // ── extension: a product's own leaf, derived from the base's semantics ──
    record Up() implements Feedback {}
    static final DesignClass<Target.Color.Surface> UP_SURFACE = of(Up.class, Target.Color.Surface.class);

    record FinDash() implements DesignExtension {
        @Override public String slug() { return "fin-dash"; }
        @Override public Impl impl(DesignClass<?> pair) {
            // Up is Success, for whichever design is wearing it: derived, and flat by the time it is returned.
            return pair.equals(UP_SURFACE) ? new Brutal().impl(SUCCESS_SURFACE) : null;
        }
    }

    @Test
    void anExtension_fulfilsItsOwnPair_byDerivation_andTheResultIsFlat() {
        var r = new Deployment(Set.of(UP_SURFACE, SUCCESS_SURFACE), new Brutal(), List.of(new FinDash())).resolve();
        assertEquals(List.of(), r.findings());
        var up = assertInstanceOf(Impl.Bindings.class, r.impls().get(UP_SURFACE));
        assertEquals("#0A7D3A", up.values().get(Mode.LIGHT).get(State.REST).get("background-color"), "Up is Success, through the base, flat");
    }

    // ── refusals ─────────────────────────────────────────────────────────
    record Twice() implements DesignExtension {
        @Override public String slug() { return "twice"; }
        @Override public Impl impl(DesignClass<?> pair) { return pair.equals(DANGER_SURFACE) ? Impl.Bindings.of("#000") : null; }
    }

    @Test
    void twoAnswersForOnePair_isRefused_notLastWins() {
        var r = new Deployment(Set.of(DANGER_SURFACE), new Plain(), List.of(new Twice())).resolve();
        assertEquals(Finding.Kind.DOUBLE, r.findings().get(0).kind(), r.findings().toString());
        assertTrue(r.findings().get(0).detail().contains("plain") && r.findings().get(0).detail().contains("twice"));
    }

    static final DesignClass<Target.Color.Ink> DANGER_INK = of(Feedback.Danger.class, Target.Color.Ink.class);

    record Wrong() implements Design {
        static final Map<DesignClass<?>, Impl> WORDS = Map.of(
                CUE, Impl.Bindings.of("beep"),                                                    // CSS impl for an audio target
                DANGER_INK, Impl.Bindings.none().at(State.REST, "background-color", "#000"),    // ink does not own background
                CORNER, Impl.Bindings.none().at(State.HOVER, "0"),                               // corner offers no hover slot
                DANGER_SURFACE, Impl.Bindings.of("#000"),                                        // SOLE on a two-property target
                SUCCESS_SURFACE, new Impl.Body("background-color: #0A7D3A;\npadding-top: 4px;\n& .st-card { color: red; }\n"
                        + "tr:nth-child(even) td { background-color: #EEE; }\n"));                  // a selector with a pseudo-class, not a `tr` property
        @Override public String slug() { return "wrong"; }
        @Override public Impl impl(DesignClass<?> pair) { return WORDS.get(pair); }
    }

    @Test
    void impls_areValidatedAgainstTheirTarget() {
        var r = Deployment.of(Set.of(CUE, DANGER_INK, CORNER, DANGER_SURFACE, SUCCESS_SURFACE), new Wrong()).resolve();
        var kinds = r.findings().stream().map(Finding::kind).toList();
        assertTrue(kinds.contains(Finding.Kind.CARRIER_MISMATCH), kinds.toString());
        assertEquals(3, kinds.stream().filter(k -> k == Finding.Kind.INVALID_BINDING).count(), r.findings().toString());
        assertEquals(2, kinds.stream().filter(k -> k == Finding.Kind.INVALID_BODY).count(), "padding-top and the nested class: " + r.findings());
    }

    // ── the requirement set is what the closure wears ────────────────────
    record Wearer() implements CssGroup<Wearer> {
        static final Wearer INSTANCE = new Wearer();
        record card() implements CssClass<Wearer> {
            @Override public List<? extends Wearable> wears() { return List.of(DANGER_SURFACE, ON_DANGER_INK); }
            @Override public String body() { return "display: flex;"; }
        }
        record plain() implements CssClass<Wearer> { @Override public String body() { return "display: block;"; } }
        @Override public List<CssClass<Wearer>> cssClasses() { return List.of(new card(), new plain()); }
    }

    @Test
    void wornBy_collectsThePairsOfAClosure() {
        assertEquals(Set.of(DANGER_SURFACE, ON_DANGER_INK), Deployment.wornBy(List.of(Wearer.INSTANCE)));
    }

    // ── two planes: a physique and a palette, composed ───────────────────
    static final DesignClass<Target.Shape.Shadow> LIFT = of(Interaction.Interactive.class, Target.Shape.Shadow.class);

    /** A physique whose shadow is "the danger surface, offset" — a colour by reference, never by value. */
    record Hard() implements Design {
        static final Map<DesignClass<?>, Impl> WORDS = Map.of(
                LIFT, Impl.Bindings.of("6px 6px 0 " + DANGER_SURFACE.var("background-color")),
                CORNER, Impl.Bindings.of("0"),
                PRESS, Impl.Bindings.none().at(State.ACTIVE, "translate(6px, 6px)"),
                EASE, Impl.Bindings.of("transform 70ms steps(2)"),
                INSET, Impl.Bindings.of("8px 16px"),
                FACE, Impl.Bindings.of("Arial Black, sans-serif"));
        @Override public String slug() { return "hard"; }
        @Override public Impl impl(DesignClass<?> pair) { return WORDS.get(pair); }
    }

    @Test
    void aDesign_isTwoPlanes_andAnyPhysiqueTakesAnyPalette() {
        Set<DesignClass<?>> required = Set.of(DANGER_SURFACE, ON_DANGER_INK, PRESS, EASE, CORNER, INSET, FACE, LIFT);
        // Hard's physique under Plain's colours: every pair answered, the shadow reads Plain's danger surface.
        var r = Deployment.of(required, Composed.of(new Hard(), new Plain())).resolve();
        assertEquals(List.of(), r.findings(), r.findings().toString());
        assertEquals("#B00020", ((Impl.Bindings) r.impls().get(DANGER_SURFACE)).values().get(Mode.LIGHT).get(State.REST).get("background-color"));
        assertEquals("0", ((Impl.Bindings) r.impls().get(CORNER)).values().get(Mode.LIGHT).get(State.REST).get(Impl.Bindings.SOLE));
        assertTrue(((Impl.Bindings) r.impls().get(LIFT)).values().get(Mode.LIGHT).get(State.REST).get(Impl.Bindings.SOLE)
                .contains("var(--danger-color-surface-background-color)"));
        // and the sheet says so: the shadow's root binding is the reference, the surface's is the value
        String root = Sheets.rootSheet(r);
        assertTrue(root.contains("--interactive-shape-shadow: 6px 6px 0 var(--danger-color-surface-background-color);"), root);
        assertTrue(root.contains("--danger-color-surface-background-color: #B00020;"), root);
        // the same physique under Brutal's colours: the same shadow, now yellow — nothing in the physique changed
        var yellow = Deployment.of(required, Composed.of(new Hard(), new Brutal())).resolve();
        assertEquals(List.of(), yellow.findings(), yellow.findings().toString());
        assertTrue(Sheets.rootSheet(yellow).contains("--danger-color-surface-background-color: #FFE800;"));
        assertEquals("hard_brutal", Composed.of(new Hard(), new Brutal()).slug());
    }

    @Test
    void aPlane_answersOnlyItsOwn() {
        assertEquals(null, Composed.physiqueOf(new Plain()).impl(DANGER_SURFACE), "a physique has no colour");
        assertEquals(null, Composed.paletteOf(new Plain()).impl(CORNER), "a palette has no shape");
        assertTrue(Composed.paletteOf(new Plain()).impl(DANGER_SURFACE) != null);
        assertTrue(Composed.physiqueOf(Composed.paletteOf(new Plain())).impl(CORNER) != null, "views compose back to the whole's plane, not to nothing");
    }

    @Test
    void aReferenceToAPairNobodyRequired_isRefused() {
        Set<DesignClass<?>> noSurface = Set.of(LIFT, CORNER, PRESS, EASE, INSET, FACE);                        // no danger surface required
        var r = Deployment.of(noSurface, Composed.of(new Hard(), new Plain())).resolve();
        var kinds = r.findings().stream().map(Finding::kind).toList();
        assertEquals(List.of(Finding.Kind.DANGLING_REFERENCE), kinds, r.findings().toString());
        assertTrue(r.findings().get(0).toString().contains("--danger-color-surface-background-color"));
    }

    @Test
    void aReference_isTyped() {
        assertEquals("var(--danger-color-surface-background-color)", DANGER_SURFACE.var("background-color"));
        assertEquals("var(--on-danger-color-ink)", ON_DANGER_INK.var());
        assertTrue(DANGER_SURFACE.onColourPlane());
        assertTrue(!LIFT.onColourPlane());
        try { DANGER_SURFACE.var(); throw new AssertionError("a two-property target must name the property"); }
        catch (IllegalArgumentException expected) { /* named */ }
        try { CORNER.var("color"); throw new AssertionError("a property the target does not own"); }
        catch (IllegalArgumentException expected) { /* refused */ }
    }
}
