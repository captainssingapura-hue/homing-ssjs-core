package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.State;

import java.util.Map;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Brand.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * Retro-futurism — the future as 1984 drew it. Everything is outlined in
 * neon and glows: a plate is a thin frame that bleeds light, a title is
 * wide, upright, capitalised and haloed, a selected thing burns brighter.
 * Corners are barely cut, rules are crisp, motion is quick and a little
 * stepped, like a scanline catching up.
 *
 * <p>A physique only: which neon glows is the palette's — the glow is the
 * primary surface by reference, the second glow the secondary — so any
 * palette lights up under it; {@link SynthwavePalette} is the one it is
 * worn in by default, and the one with a sun.</p>
 */
final class RetroFuturismDesign {

    private RetroFuturismDesign() {}

    static final String DISPLAY_FACE = "\"Orbitron\", \"Audiowide\", \"Bahnschrift\", \"Eurostile\", \"Impact\", sans-serif";
    static final String BODY_FACE    = "\"Rajdhani\", \"Bahnschrift\", \"Segoe UI\", system-ui, sans-serif";
    static final String EASE = "box-shadow 140ms ease-out, transform 140ms steps(3, end), background-color 120ms ease-out, border-color 120ms ease-out, color 120ms ease-out";

    // ── the palette, by reference: the neon is whatever the palette makes primary; the second neon its secondary ──
    static final String NEON_REF   = of(Primary.class, Color.Surface.class).var("background-color");
    static final String NEON2_REF  = of(Secondary.class, Color.Surface.class).var("background-color");
    static final String VOID_REF   = of(Inverted.class, Color.Surface.class).var("background-color");

    static String glow(String neon, int pct) { return "color-mix(in srgb, " + neon + " " + pct + "%, transparent)"; }
    /** A neon frame: a hairline of the light, and its bleed outward. */
    static String frame(String neon, int bleed, int pct) { return "0 0 0 1px " + glow(neon, 70) + ", 0 0 " + bleed + "px " + glow(neon, pct); }
    /** A haloed letter: the light tight around it, and wider, fainter. */
    static String halo(String neon) { return "0 0 6px " + glow(neon, 80) + ", 0 0 18px " + glow(neon, 50) + ", 0 0 40px " + glow(neon, 25); }

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers: neon frames ─────────────────────────────────────
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            one(of(Raised.class, Shape.Corner.class), "4px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), Impl.Bindings
                    .of(frame(NEON_REF, 16, 30))
                    .at(State.FOCUS, frame(NEON2_REF, 24, 50))),
            one(of(Recessed.class, Shape.Corner.class), "2px"),

            // ── text: wide, upright, capitalised, haloed ────────────────
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "700"),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "800"),
            scale(of(Display.class, Type.Scale.class), "40px", "1.15"),
            treatment(of(Display.class, Type.Treatment.class), "0.12em", "uppercase", null),
            Map.entry(of(Display.class, Type.Decoration.class), Impl.Bindings.none()
                    .at(State.REST, "text-decoration-line", "none").at(State.REST, "text-shadow", halo(NEON_REF))),
            treatment(of(Lede.class, Type.Treatment.class), "0.02em", null, null),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "700"),
            treatment(of(Kicker.class, Type.Treatment.class), "0.24em", "uppercase", null),
            treatment(of(Caption.class, Type.Treatment.class), "0.04em", null, null),
            one(of(Label.class, Type.Weight.class), "700"),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "700"),
            treatment(of(Numeral.class, Type.Treatment.class), "0.06em", null, null),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            treatment(of(House.class, Type.Treatment.class), "0.2em", "uppercase", null),
            Map.entry(of(House.class, Type.Decoration.class), Impl.Bindings.none().at(State.REST, "text-shadow", "0 0 8px " + glow(NEON_REF, 70))),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Code.class, Shape.Corner.class), "2px"),

            // ── interaction: a control burns brighter; pressed, it flickers down ──
            one(of(Interactive.class, Motion.Ease.class), EASE),
            Map.entry(of(Interactive.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.HOVER, "translateY(-2px)")
                    .at(State.ACTIVE, "translateY(1px)")
                    .at(State.SELECTED, "translateY(-2px)")
                    .at(State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Interactive.class, Shape.Shadow.class), Impl.Bindings
                    .of(frame(NEON_REF, 10, 25))
                    .at(State.HOVER, frame(NEON_REF, 24, 55))
                    .at(State.ACTIVE, frame(NEON_REF, 6, 40))
                    .at(State.SELECTED, frame(NEON_REF, 32, 70))
                    .at(State.HIGHLIGHTED, frame(NEON2_REF, 24, 55))),
            Map.entry(of(Interactive.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", "drop-shadow(0 0 12px " + glow(NEON_REF, 70) + ")")
                    .at(State.HIGHLIGHTED, "filter", "drop-shadow(0 0 10px " + glow(NEON2_REF, 60) + ")")),
            Map.entry(of(Interactive.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "1px").at(State.REST, "border-style", "solid")
                    .at(State.FOCUS, "outline-width", "1px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "3px")),
            // Selectable — a row, a cell, an option: dark until it is the one, then lit in neon
            one(of(Selectable.class, Motion.Ease.class), EASE),
            Map.entry(of(Selectable.class, Motion.Transform.class), Impl.Bindings.none()
                    .at(State.SELECTED, "translateY(-1px)")),
            Map.entry(of(Selectable.class, Shape.Shadow.class), Impl.Bindings.none()
                    .at(State.REST, "none")
                    .at(State.HOVER, frame(NEON_REF, 10, 30))
                    .at(State.SELECTED, frame(NEON_REF, 28, 70))
                    .at(State.HIGHLIGHTED, frame(NEON2_REF, 18, 55))),
            Map.entry(of(Selectable.class, Effect.Filter.class), Impl.Bindings.none()
                    .at(State.SELECTED, "filter", "drop-shadow(0 0 12px " + glow(NEON_REF, 70) + ")")
                    .at(State.HIGHLIGHTED, "filter", "drop-shadow(0 0 10px " + glow(NEON2_REF, 60) + ")")),
            Map.entry(of(Selectable.class, Shape.Rule.class), Impl.Bindings.none()
                    .at(State.REST, "border-width", "1px").at(State.REST, "border-style", "solid")
                    .at(State.FOCUS, "outline-width", "1px").at(State.FOCUS, "outline-style", "solid").at(State.FOCUS, "outline-offset", "-2px")),
            // the Selected semantic in depth — lit, for a component that says so with a class of its own
            one(of(Selected.class, Shape.Shadow.class), frame(NEON_REF, 28, 70)),
            one(of(Selected.class, Motion.Transform.class), "translateY(-1px)"),
            Map.entry(of(Selected.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "filter", "drop-shadow(0 0 12px " + glow(NEON_REF, 70) + ")")),
            one(of(Current.class, Shape.Shadow.class), "inset 2px 0 0 " + NEON2_REF + ", 0 0 12px " + glow(NEON2_REF, 35)),
            one(of(Focus.class, Shape.Shadow.class), frame(NEON2_REF, 40, 70) + ", 0 24px 64px " + glow(NEON_REF, 30)),
            one(of(Overlay.class, Shape.Shadow.class), "0 0 48px " + glow(NEON_REF, 45) + ", 0 24px 64px " + glow(VOID_REF, 60)),
            Map.entry(of(Overlay.class, Effect.Filter.class), Impl.Bindings.none().at(State.REST, "backdrop-filter", "blur(4px) contrast(1.2)")),
            one(of(Inert.class, Effect.Opacity.class), "0.4"),
            one(of(Inert.class, Affordance.Cursor.class), "not-allowed"),

            // ── structure: crisp neon rules ─────────────────────────────
            rule(of(Divider.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            rule(of(Bar.class, Shape.Rule.class), "1px 1px 1px 3px", "solid"),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),

            // ── boxes: barely cut ───────────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "3px"),
            rule(of(Control.class, Shape.Rule.class), "1px", "solid"),
            one(of(Inline.class, Shape.Corner.class), "2px"),
            rule(of(Inline.class, Shape.Rule.class), "1px", "solid"),

            // ── prose ───────────────────────────────────────────────────
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, DefaultDesign.MONO_FACE)),
            body(of(Prose.class, Type.Weight.class), "h1, h2, h3, h4, th { font-weight: 700; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h1, h2 { letter-spacing: 0.08em; text-transform: uppercase; }
                h4 { letter-spacing: 0.2em; text-transform: uppercase; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 3px; text-decoration-thickness: 1px; }\nh1 { text-shadow: " + halo(NEON_REF) + "; }\n"),
            body(of(Prose.class, Shape.Rule.class), """
                h1, h2 { border-width: 0 0 2px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 2px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), "code, pre { border-radius: 2px; }\n")
    );
}
