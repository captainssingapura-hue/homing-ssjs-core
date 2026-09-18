package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;

import java.util.Map;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Brand.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * The house physique — what the studio looks like out of the box, off the
 * colour plane: shape, type, motion, depth, said as bindings over the design
 * classes its components wear. Its colours are {@link SeedPalette#HOUSE}; a
 * shadow names them by reference and carries none. Every value here was a
 * token or a literal in a class body until the components stopped painting.
 */
final class DefaultDesign {

    private DefaultDesign() {}

    // The house colours live in SeedPalette.HOUSE — this is the physique: shape, type, motion, depth.──────────────────────────

    static final String DISPLAY_FACE = StudioFonts.DISPLAY;
    static final String BODY_FACE    = StudioFonts.BODY;
    static final String MONO_FACE    = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace";

    // ── the palette, by reference: a shadow is a colour the palette owns, offset ──
    static final String INK_REF      = of(Heading.class, Color.Ink.class).var();
    static final String PRIMARY_REF  = of(Primary.class, Color.Surface.class).var("background-color");
    static final String INVERTED_REF = of(Inverted.class, Color.Surface.class).var("background-color");

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers ──────────────────────────────────────────────────
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            one(of(Raised.class, Shape.Corner.class), "4px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings
                    .of("0 1px 3px color-mix(in srgb, " + INK_REF + " 4%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "0 0 0 3px color-mix(in srgb, " + PRIMARY_REF + " 18%, transparent)")),
            one(of(Recessed.class, Shape.Corner.class), "6px"),

            // ── text ────────────────────────────────────────────────────
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "700"),
            scale(of(Heading.class, Type.Scale.class), "18px", "1.25"),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "700"),
            scale(of(Display.class, Type.Scale.class), "44px", "1.1"),
            treatment(of(Display.class, Type.Treatment.class), "-0.5px", null, null),
            scale(of(Lede.class, Type.Scale.class), "17px", "1.55"),
            treatment(of(Lede.class, Type.Treatment.class), null, null, "italic"),
            one(of(Kicker.class, Type.Face.class), DISPLAY_FACE),
            one(of(Kicker.class, Type.Weight.class), "700"),
            scale(of(Kicker.class, Type.Scale.class), "11px", "1.4"),
            treatment(of(Kicker.class, Type.Treatment.class), "2px", "uppercase", null),
            scale(of(Caption.class, Type.Scale.class), "13px", "1.5"),
            treatment(of(Caption.class, Type.Treatment.class), null, null, "italic"),
            scale(of(Label.class, Type.Scale.class), "14px", "1.5"),
            one(of(Label.class, Type.Weight.class), "600"),
            one(of(Numeral.class, Type.Face.class), DISPLAY_FACE),
            one(of(Numeral.class, Type.Weight.class), "700"),
            scale(of(Numeral.class, Type.Scale.class), "28px", "1"),
            treatment(of(Numeral.class, Type.Treatment.class), null, null, "italic"),
            one(of(House.class, Type.Face.class), DISPLAY_FACE),
            scale(of(House.class, Type.Scale.class), "22px", "1"),
            treatment(of(House.class, Type.Treatment.class), null, null, "italic"),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Link.class, Motion.Ease.class), "color 140ms ease, border-color 140ms ease"),
            one(of(Code.class, Type.Face.class), MONO_FACE),
            one(of(Code.class, Shape.Corner.class), "3px"),

            // ── emphasis ────────────────────────────────────────────────
            one(of(Primary.class, Motion.Ease.class), "width 280ms ease"),
            one(of(Muted.class, Effect.Opacity.class), "0.62"),
            decoration(of(Muted.class, Type.Decoration.class), "line-through"),

            // ── pairings ────────────────────────────────────────────────

            // ── feedback ────────────────────────────────────────────────

            // ── interaction ─────────────────────────────────────────────
            one(of(Interactive.class, Motion.Ease.class), "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease"),
            // depth is a state: hover lifts, selected and highlighted stay lifted; a row lifts by filter, since a row paints no shadow
            Map.entry(of(Interactive.class, Motion.Transform.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "translateY(-2px)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "translateY(-2px)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Interactive.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings
                    .of("0 1px 3px color-mix(in srgb, " + INK_REF + " 4%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "0 6px 16px color-mix(in srgb, " + INK_REF + " 12%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "0 8px 20px color-mix(in srgb, " + INK_REF + " 18%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "0 4px 12px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent)")),
            Map.entry(of(Interactive.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "filter", "drop-shadow(0 6px 10px color-mix(in srgb, " + INK_REF + " 18%, transparent))")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "filter", "drop-shadow(0 3px 8px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent))")),
            one(of(Interactive.class, Affordance.Cursor.class), "pointer"),
            // a hairline that the palette colours only in a state; a 2px ring, inset, on focus
            Map.entry(of(Interactive.class, Shape.Rule.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-width", "1px")
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-width", "2px")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-offset", "-2px")),
            // Selectable — a row, a cell, an option: flat at rest, the same lifts as Interactive in its states
            one(of(Selectable.class, Motion.Ease.class), "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease, background-color 160ms ease"),
            one(of(Selectable.class, Affordance.Cursor.class), "pointer"),
            Map.entry(of(Selectable.class, Motion.Transform.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "translateY(-1px)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "translateY(-2px)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "translateY(-1px)")),
            Map.entry(of(Selectable.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "none")
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "0 4px 12px color-mix(in srgb, " + INK_REF + " 10%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "0 8px 20px color-mix(in srgb, " + INK_REF + " 18%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "0 4px 12px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent)")),
            Map.entry(of(Selectable.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.SELECTED, "filter", "drop-shadow(0 6px 10px color-mix(in srgb, " + INK_REF + " 18%, transparent))")
                    .at(hue.captains.singapura.js.homing.design.State.HIGHLIGHTED, "filter", "drop-shadow(0 3px 8px color-mix(in srgb, " + PRIMARY_REF + " 35%, transparent))")),
            Map.entry(of(Selectable.class, Shape.Rule.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-width", "1px")
                    .at(hue.captains.singapura.js.homing.design.State.REST, "border-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-width", "2px")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-style", "solid")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "outline-offset", "-2px")),
            // the Selected semantic in depth — for a component that says "lifted" with a class of its own
            one(of(Selected.class, Shape.Shadow.class), "0 8px 20px color-mix(in srgb, " + INK_REF + " 18%, transparent)"),
            one(of(Selected.class, Motion.Transform.class), "translateY(-2px)"),
            Map.entry(of(Selected.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.REST, "filter", "drop-shadow(0 6px 10px color-mix(in srgb, " + INK_REF + " 18%, transparent))")),
            one(of(Current.class, Shape.Shadow.class), "inset 3px 0 0 color-mix(in srgb, " + PRIMARY_REF + " 60%, transparent)"),

            // ── structure ───────────────────────────────────────────────
            rule(of(Divider.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            rule(of(Bar.class, Shape.Rule.class), "1px 1px 1px 4px", "solid"),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),
            one(of(Overlay.class, Shape.Shadow.class), "0 10px 30px color-mix(in srgb, " + INVERTED_REF + " 45%, transparent), 0 2px 6px color-mix(in srgb, " + INVERTED_REF + " 30%, transparent)"),
            Map.entry(of(Overlay.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none().at(hue.captains.singapura.js.homing.design.State.REST, "backdrop-filter", "brightness(0.45) blur(2px)")),
            one(of(Focus.class, Shape.Shadow.class), "0 10px 30px color-mix(in srgb, " + INVERTED_REF + " 45%, transparent), 0 0 0 1px color-mix(in srgb, " + PRIMARY_REF + " 28%, transparent), 0 0 36px color-mix(in srgb, " + PRIMARY_REF + " 30%, transparent)"),
            one(of(Inert.class, Effect.Opacity.class), "0.45"),
            one(of(Inert.class, Affordance.Cursor.class), "default"),

            // ── boxes ───────────────────────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "3px"),
            rule(of(Control.class, Shape.Rule.class), "1.5px", "solid"),
            one(of(Inline.class, Shape.Corner.class), "2px"),
            rule(of(Inline.class, Shape.Rule.class), "1px", "solid"),

            // ── prose: the document's elements, set by the design ───────
            body(of(Prose.class, Type.Face.class), """
                h1, h2, h3, h4 { font-family: %s; }
                code, pre { font-family: %s; }
                """.formatted(DISPLAY_FACE, MONO_FACE)),
            body(of(Prose.class, Type.Scale.class), """
                font-size: 16px;
                line-height: 1.7;
                h1, h2, h3, h4 { line-height: 1.25; }
                h1 { font-size: 32px; }
                h2 { font-size: 24px; }
                h3 { font-size: 19px; }
                h4 { font-size: 16px; }
                code { font-size: 0.92em; }
                pre { font-size: 13px; line-height: 1.5; }
                pre code { font-size: inherit; }
                table { font-size: 14px; }
                """),
            body(of(Prose.class, Type.Weight.class), "th { font-weight: 700; }\n"),
            body(of(Prose.class, Type.Treatment.class), """
                h4 { letter-spacing: 1px; text-transform: uppercase; }
                blockquote { font-style: italic; }
                """),
            body(of(Prose.class, Type.Decoration.class), "a { text-decoration-line: underline; text-underline-offset: 2px; }\n"),
            body(of(Prose.class, Shape.Rule.class), """
                h1 { border-width: 0 0 2px 0; border-style: solid; }
                blockquote { border-width: 0 0 0 3px; border-style: solid; }
                th, td { border-width: 0 0 1px 0; border-style: solid; }
                th { border-width: 0; }
                hr { border-width: 1px 0 0 0; border-style: solid; }
                """),
            body(of(Prose.class, Shape.Corner.class), """
                code { border-radius: 3px; }
                pre { border-radius: 4px; }
                """)
    );
}
