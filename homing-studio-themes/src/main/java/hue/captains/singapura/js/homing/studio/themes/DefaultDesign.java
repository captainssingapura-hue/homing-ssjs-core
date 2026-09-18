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
 * The house design — what the studio looks like out of the box, said as
 * bindings over the design classes its components wear. Every value here was
 * a token or a literal in a class body until the components stopped painting;
 * now the design owns it, light and dark, and no component knows.
 */
final class DefaultDesign {

    private DefaultDesign() {}

    // ── the house's colours, light / dark ──────────────────────────────
    static final String SURFACE = "#FAFBFD",  SURFACE_D = "#0F1320";
    static final String RAISED  = "#FFFFFF",  RAISED_D  = "#1A1F36";
    static final String RECESSED = "#F1F4F9", RECESSED_D = "#232943";
    static final String INVERTED = "#111936";
    static final String TEXT = "#3B4A6B",     TEXT_D = "#E2E8F0";
    static final String MUTED = "#64748B",    MUTED_D = "#94A3B8";
    static final String ON_INVERTED = "#FFFFFF", ON_INVERTED_D = "#E2E8F0";
    static final String ON_INVERTED_MUTED = "#CADCFC", ON_INVERTED_MUTED_D = "#B8C9F2";
    static final String NAVY = "#1E2761",     NAVY_D = "#8FA3D8";
    static final String AMBER = "#C8921E",    AMBER_D = "#E0A833";
    static final String BORDER = "#E2E8F0",   BORDER_D = "#2D3454";
    static final String GOLD = "#F4B942";
    static final String ON_GOLD = "#111936";

    static final String DISPLAY_FACE = StudioFonts.DISPLAY;
    static final String BODY_FACE    = StudioFonts.BODY;
    static final String MONO_FACE    = "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace";

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // ── layers ──────────────────────────────────────────────────
            surface(of(Base.class, Color.Surface.class), SURFACE, SURFACE_D),
            one(of(Base.class, Color.Scrollbar.class), MUTED + " " + SURFACE, MUTED_D + " " + SURFACE_D),
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Base.class, Shape.Corner.class), "0"),
            surface(of(Raised.class, Color.Surface.class), RAISED, RAISED_D),
            edgeFocus(of(Raised.class, Color.Edge.class), BORDER, GOLD),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            one(of(Raised.class, Shape.Corner.class), "4px"),
            Map.entry(of(Raised.class, Shape.Shadow.class), hue.captains.singapura.js.homing.design.Impl.Bindings
                    .of("0 1px 3px color-mix(in srgb, " + NAVY + " 4%, transparent)")
                    .at(hue.captains.singapura.js.homing.design.State.FOCUS, "0 0 0 3px color-mix(in srgb, " + GOLD + " 18%, transparent)")),
            surface(of(Recessed.class, Color.Surface.class), RECESSED, RECESSED_D),
            one(of(Recessed.class, Shape.Corner.class), "6px"),
            surface(of(Inverted.class, Color.Surface.class), INVERTED),

            // ── text ────────────────────────────────────────────────────
            one(of(Body.class, Color.Ink.class), TEXT, TEXT_D),
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Color.Ink.class), NAVY, NAVY_D),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Heading.class, Type.Weight.class), "700"),
            scale(of(Heading.class, Type.Scale.class), "18px", "1.25"),
            one(of(Display.class, Color.Ink.class), NAVY, NAVY_D),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Weight.class), "700"),
            scale(of(Display.class, Type.Scale.class), "44px", "1.1"),
            treatment(of(Display.class, Type.Treatment.class), "-0.5px", null, null),
            one(of(Lede.class, Color.Ink.class), MUTED, MUTED_D),
            scale(of(Lede.class, Type.Scale.class), "17px", "1.55"),
            treatment(of(Lede.class, Type.Treatment.class), null, null, "italic"),
            one(of(Kicker.class, Color.Ink.class), AMBER, AMBER_D),
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
            states(of(Link.class, Color.Ink.class), MUTED, NAVY),
            decoration(of(Link.class, Type.Decoration.class), "none"),
            one(of(Link.class, Motion.Ease.class), "color 140ms ease, border-color 140ms ease"),
            one(of(Code.class, Type.Face.class), MONO_FACE),
            surface(of(Code.class, Color.Surface.class), RECESSED, RECESSED_D),
            one(of(Code.class, Color.Ink.class), NAVY, NAVY_D),
            one(of(Code.class, Shape.Corner.class), "3px"),

            // ── emphasis ────────────────────────────────────────────────
            surface(of(Primary.class, Color.Surface.class), GOLD),
            one(of(Primary.class, Color.Ink.class), GOLD),
            one(of(Primary.class, Motion.Ease.class), "width 280ms ease"),
            one(of(OnPrimary.class, Color.Ink.class), ON_GOLD),
            surface(of(Secondary.class, Color.Surface.class), AMBER, AMBER_D),
            surface(of(Tertiary.class, Color.Surface.class), BORDER, BORDER_D),
            one(of(Muted.class, Color.Ink.class), MUTED, MUTED_D),
            edge(of(Muted.class, Color.Edge.class), MUTED, MUTED_D),
            one(of(Muted.class, Effect.Opacity.class), "0.62"),
            decoration(of(Muted.class, Type.Decoration.class), "line-through"),

            // ── pairings ────────────────────────────────────────────────
            one(of(OnInverted.class, Color.Ink.class), ON_INVERTED, ON_INVERTED_D),
            states(of(OnInvertedMuted.class, Color.Ink.class), ON_INVERTED_MUTED, GOLD),

            // ── feedback ────────────────────────────────────────────────
            surface(of(Danger.class, Color.Surface.class), "rgba(220, 38, 38, 0.10)"),
            one(of(Danger.class, Color.Ink.class), "#7F1D1D", "#FCA5A5"),
            edge(of(Danger.class, Color.Edge.class), "rgba(220, 38, 38, 0.35) rgba(220, 38, 38, 0.35) rgba(220, 38, 38, 0.35) #DC2626"),
            surface(of(Success.class, Color.Surface.class), "rgba(34, 139, 34, 0.12)"),
            one(of(Success.class, Color.Ink.class), "#1B5E20", "#86EFAC"),
            edge(of(Success.class, Color.Edge.class), "rgba(34, 139, 34, 0.35)"),
            surface(of(Warning.class, Color.Surface.class), "rgba(202, 138, 4, 0.12)"),
            one(of(Warning.class, Color.Ink.class), "#713F12", "#FDE68A"),
            edge(of(Warning.class, Color.Edge.class), "rgba(202, 138, 4, 0.35)"),

            // ── interaction ─────────────────────────────────────────────
            one(of(Interactive.class, Motion.Ease.class), "transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease"),
            Map.entry(of(Interactive.class, Motion.Transform.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none()
                    .at(hue.captains.singapura.js.homing.design.State.HOVER, "translateY(-2px)")),
            states(of(Interactive.class, Shape.Shadow.class),
                    "0 1px 3px color-mix(in srgb, " + NAVY + " 4%, transparent)",
                    "0 6px 16px color-mix(in srgb, " + NAVY + " 12%, transparent)"),
            one(of(Interactive.class, Affordance.Cursor.class), "pointer"),
            surface(of(Interactive.class, Color.Surface.class), "color-mix(in srgb, " + GOLD + " 15%, transparent)"),
            edge(of(Interactive.class, Color.Edge.class), BORDER, BORDER_D),
            surface(of(Selected.class, Color.Surface.class), INVERTED),
            states(of(Selected.class, Color.Ink.class), ON_INVERTED, GOLD),
            edge(of(Selected.class, Color.Edge.class), INVERTED),
            surface(of(Current.class, Color.Surface.class), "color-mix(in srgb, " + GOLD + " 7%, transparent)"),
            one(of(Current.class, Color.Ink.class), NAVY, NAVY_D),
            edge(of(Current.class, Color.Edge.class), GOLD),
            one(of(Current.class, Shape.Shadow.class), "inset 3px 0 0 color-mix(in srgb, " + GOLD + " 60%, transparent)"),

            // ── structure ───────────────────────────────────────────────
            edge(of(Divider.class, Color.Edge.class), GOLD),
            rule(of(Divider.class, Shape.Rule.class), "0 0 2px 0", "solid"),
            edge(of(Hairline.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Hairline.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            edge(of(Cap.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Cap.class, Shape.Rule.class), "1px 0 0 0", "solid"),
            edge(of(Spine.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            edgeHover(of(Marker.class, Color.Edge.class), "transparent", GOLD),
            rule(of(Marker.class, Shape.Rule.class), "0 0 0 2px", "solid"),
            edgeHover(of(Bar.class, Color.Edge.class), BORDER + " " + BORDER + " " + BORDER + " " + GOLD, BORDER + " " + BORDER + " " + BORDER + " " + AMBER),
            rule(of(Bar.class, Shape.Rule.class), "1px 1px 1px 4px", "solid"),

            edge(of(Rail.class, Color.Edge.class), BORDER, BORDER_D),
            rule(of(Rail.class, Shape.Rule.class), "0 1px 0 0", "solid"),
            edge(of(OnInverted.class, Color.Edge.class), BORDER, BORDER_D),
            edge(of(Primary.class, Color.Edge.class), GOLD),
            one(of(Overlay.class, Shape.Shadow.class), "0 10px 30px color-mix(in srgb, " + INVERTED + " 45%, transparent), 0 2px 6px color-mix(in srgb, " + INVERTED + " 30%, transparent)"),
            Map.entry(of(Overlay.class, Effect.Filter.class), hue.captains.singapura.js.homing.design.Impl.Bindings.none().at(hue.captains.singapura.js.homing.design.State.REST, "backdrop-filter", "brightness(0.45) blur(2px)")),
            edge(of(Focus.class, Color.Edge.class), "color-mix(in srgb, " + GOLD + " 55%, " + BORDER + ")"),
            one(of(Focus.class, Shape.Shadow.class), "0 10px 30px color-mix(in srgb, " + INVERTED + " 45%, transparent), 0 0 0 1px color-mix(in srgb, " + GOLD + " 28%, transparent), 0 0 36px color-mix(in srgb, " + GOLD + " 30%, transparent)"),
            one(of(Inert.class, Effect.Opacity.class), "0.45"),
            one(of(Inert.class, Affordance.Cursor.class), "default"),

            // ── boxes ───────────────────────────────────────────────────
            one(of(Control.class, Shape.Corner.class), "3px"),
            rule(of(Control.class, Shape.Rule.class), "1.5px", "solid"),
            one(of(Inline.class, Shape.Corner.class), "2px"),
            rule(of(Inline.class, Shape.Rule.class), "1px", "solid"),

            // ── prose: the document's elements, set by the design ───────
            body(of(Prose.class, Color.Ink.class), """
                color: %s;
                h1, h2, h3 { color: %s; }
                h4 { color: %s; }
                a { color: %s; }
                a:hover { color: %s; }
                blockquote { color: %s; }
                code { color: %s; }
                pre { color: %s; }
                pre code { color: inherit; }
                th { color: %s; }
                """.formatted(TEXT, NAVY, AMBER, AMBER, NAVY, MUTED, NAVY, ON_INVERTED_MUTED, ON_INVERTED)),
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
            body(of(Prose.class, Color.Surface.class), """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(RECESSED, INVERTED, INVERTED, RECESSED)),
            body(of(Prose.class, Color.Edge.class), """
                h1 { border-color: %s; }
                blockquote { border-color: %s; }
                th, td { border-color: %s; }
                hr { border-color: %s; }
                """.formatted(GOLD, GOLD, BORDER, BORDER)),
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
