package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;

import java.util.Map;

import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Interaction.Selectable;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Structure.Detent;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Structure.Hairline;
import static hue.captains.singapura.js.homing.design.Structure.Spine;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Size;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Code;
import static hue.captains.singapura.js.homing.design.Text.Label;
import static hue.captains.singapura.js.homing.design.Text.Display;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.studio.themes.Bind.gap;
import static hue.captains.singapura.js.homing.studio.themes.Bind.glyph;
import static hue.captains.singapura.js.homing.studio.themes.Bind.inset;
import static hue.captains.singapura.js.homing.studio.themes.Bind.measure;
import static hue.captains.singapura.js.homing.studio.themes.Bind.proportion;
import static hue.captains.singapura.js.homing.studio.themes.Bind.one;
import static hue.captains.singapura.js.homing.studio.themes.Bind.rule;
import static hue.captains.singapura.js.homing.studio.themes.Bind.scale;
import static hue.captains.singapura.js.homing.studio.themes.Bind.ruleWithFocusRing;
import static hue.captains.singapura.js.homing.studio.themes.Bind.silence;
import static hue.captains.singapura.js.homing.studio.themes.Bind.withKeysMark;

/**
 * Flat-morphism — flat sheets, stacked. Nothing is moulded, nothing glows
 * and nothing is blurred: a thing is a cut sheet of card lying on a ground,
 * and the only way it says how high it lies is the hard offset it casts —
 * no blur at all, one direction for the whole page, as if a single lamp
 * stood off to the top left. The flat register's honesty about surfaces,
 * with exactly one mechanic for depth.
 *
 * <p><b>The one mechanic.</b> A cast is {@code level × }{@value #UNIT_PX}px
 * across and down, zero blur, zero spread, in the colour of what it falls
 * on. At rest a sheet lies flat and casts nothing — the hairline around it
 * is what says it is a sheet at all.</p>
 *
 * <p><b>The two registers, in two planes.</b> {@link State#ELEVATED} lifts a
 * panel two units and it casts. {@link State#SUNKEN} casts nothing, because
 * a hole casts nothing: the sheet takes the sunk face instead. So this
 * design answers the register going up in the shadow plane and the register
 * going down in the colour plane, which is the licence the register was
 * written with — each design answers in whatever plane it honestly uses.</p>
 *
 * <p><b>What it will not do.</b> No blur, so there is nothing to tune; no
 * gradient; no lift on a hover, since a sheet that jumps at the pointer is
 * not lying on anything. Corners are cut, not rounded: {@value #RADIUS}
 * everywhere, the width of a trimmed edge.</p>
 */
final class FlatMorphismDesign {

    private FlatMorphismDesign() {}

    /** One elevation unit: a level is this far across and this far down. */
    static final int UNIT_PX = 3;

    /** The cut of a corner — the same everywhere, because a trimmed edge is a trimmed edge. */
    static final String RADIUS = "3px";

    static final String BODY_FACE    = "\"IBM Plex Sans\", \"Segoe UI\", \"Helvetica Neue\", Arial, sans-serif";
    static final String DISPLAY_FACE = BODY_FACE;
    static final String MONO_FACE    = "\"IBM Plex Mono\", ui-monospace, \"Cascadia Mono\", Menlo, monospace";

    /** The ink, by reference: what a cast is made of in daylight. */
    static final String INK_REF  = of(hue.captains.singapura.js.homing.design.Layer.Inverted.class, Color.Surface.class).var("background-color");
    /** The sunk face, by reference: what a cast is made of at night, where ink over a dark room says nothing. */
    static final String SUNK_REF = of(Recessed.class, Color.Surface.class).var("background-color");

    /**
     * The cast, as a colour: the ink at a fifth in daylight, the sunk face
     * at night. Translucent on purpose, and this is the morphic part — the
     * same cast over the ground and over a sheet is two colours, because it
     * IS what lies beneath, shaded. Nothing has to know what it fell on.
     */
    static final String CAST   = "color-mix(in srgb, " + INK_REF + " 22%, transparent)";
    static final String CAST_D = "color-mix(in srgb, " + SUNK_REF + " 85%, transparent)";

    /** A cast of n units: across and down, no blur, no spread. One lamp, top left, for the whole page. */
    static String cast(int level, String colour) {
        int at = level * UNIT_PX;
        return at + "px " + at + "px 0 0 " + colour;
    }

    /** The same cast in both rooms, each room's shade of it. */
    static Map.Entry<DesignClass<?>, Impl> castAt(DesignClass<?> cls, int level) {
        return Map.entry(cls, Impl.Bindings.of(cast(level, CAST))
                .in(Mode.DARK, State.REST, cast(level, CAST_D)));
    }

    static final Map<DesignClass<?>, Impl> WORDS = Map.ofEntries(
            // a cut cross, like everything else here
            glyph(of(Icon.Close.class, Type.Glyph.class), "✕"),

            // ── the layers: the ground is flat, a sheet lies on it and casts one unit ──
            one(of(Base.class, Shape.Corner.class), "0"),
            rule(of(Base.class, Shape.Rule.class), "0", "none"),
            one(of(Raised.class, Shape.Corner.class), RADIUS),
            rule(of(Raised.class, Shape.Rule.class), "1px", "solid"),
            castAt(of(Raised.class, Shape.Shadow.class), 1),
            one(of(Recessed.class, Shape.Corner.class), RADIUS),
            rule(of(Recessed.class, Shape.Rule.class), "0", "none"),
            // what floats is not a sheet on the table but one held over it: four units, and no blur even then
            castAt(of(Overlay.class, Shape.Shadow.class), 4),

            // ── the panel: the sheet this design is about ────────────────
            one(of(Container.class, Shape.Corner.class), RADIUS),
            withKeysMark(ruleWithFocusRing(of(Container.class, Shape.Rule.class), "1px", "solid", "2px", "solid", "-2px"),
                         "2px", "solid", "2px", "1px", "dashed", "4px", "1px", "solid", "2px"),
            // Lifted, a sheet casts two units onto what it lies on. Cut into the ground instead, it casts the same two
            // units INWARD, on the near edges — which is what a hole shows of the thickness it was cut through. One
            // plane, one mechanic, read both ways; and the cast is a mix of the ink, so it is the colour of whatever it
            // falls on without being told.
            Map.entry(of(Container.Panel.class, Shape.Shadow.class), Impl.Bindings.of("none")
                    .at(State.ELEVATED, cast(2, CAST))
                    .at(State.SUNKEN, "inset " + cast(2, CAST))
                    .in(Mode.DARK, State.ELEVATED, cast(2, CAST_D))
                    .in(Mode.DARK, State.SUNKEN, "inset " + cast(2, CAST_D))),
            rule(of(Container.Panel.Head.class, Shape.Rule.class), "0 0 1px 0", "solid"),

            // ── a card is a sheet you may pick up; a pane is one cut to fit ──
            castAt(of(Container.Card.class, Shape.Shadow.class), 1),
            one(of(Container.Card.class, Shape.Corner.class), RADIUS),
            one(of(Container.Pane.class, Shape.Corner.class), RADIUS),
            one(of(Container.Pane.Floating.class, Shape.Corner.class), RADIUS),
            castAt(of(Container.Pane.Floating.class, Shape.Shadow.class), 3),

            // ── controls: cut, flat, and never lifted by a hover ─────────
            one(of(Control.class, Shape.Corner.class), RADIUS),
            one(of(Control.Button.Base.class, Shape.Corner.class), RADIUS),
            silence(of(Interactive.class, Shape.Shadow.class)),
            silence(of(Selectable.class, Shape.Shadow.class)),

            // ── the measures: tight, because a sheet is cut to its content ──
            gap(of(Container.Panel.class, Size.Gap.class), "0px", 1.25),
            inset(of(Container.Panel.Head.class, Size.Inset.class), "8px", "13px", 1.25),
            gap(of(Container.Panel.Head.class, Size.Gap.class), "9px", 1.25),
            inset(of(Container.Panel.Body.class, Size.Inset.class), "13px", "15px", 1.25),
            gap(of(Container.Panel.Body.class, Size.Gap.class), "10px", 1.25),
            inset(of(Container.Card.class, Size.Inset.class), "13px", "15px", 1.25),
            gap(of(Container.Card.class, Size.Gap.class), "8px", 1.25),
            measure(of(Container.Card.class, Size.Extent.class), "340px", 1.25),
            proportion(of(Container.Card.class, Size.Proportion.class), 2),
            inset(of(Control.Button.Base.class, Size.Inset.class), "8px", "14px", 1.25),
            gap(of(Control.Button.Base.class, Size.Gap.class), "8px", 1.25),

            // ── the lines: one hairline, everywhere, and never two weights ──
            rule(of(Divider.class, Shape.Rule.class), "0 0 1px 0", "solid"),
            rule(of(Spine.class, Shape.Rule.class), "0 0 0 1px", "solid"),
            rule(of(Hairline.class, Shape.Rule.class), "1px", "solid"),

            // ── a tab is a cut card with an accent along its foot ────────
            one(of(Control.Tab.class, Shape.Corner.class), RADIUS + " " + RADIUS + " 0 0"),
            proportion(of(Control.Tab.class, Size.Proportion.class), 6),
            inset(of(Control.Tab.class, Size.Inset.class), "7px", "11px", 1.25),

            // ── motion: a sheet is put down, not thrown; and never lifted by a hover ──
            one(of(Interactive.class, Motion.Ease.class), "background-color 100ms linear, border-color 100ms linear"),
            one(of(Selectable.class, Motion.Ease.class), "background-color 100ms linear, border-color 100ms linear"),
            one(of(Container.Panel.class, Motion.Ease.class), "box-shadow 130ms ease, background-color 130ms ease"),
            silence(of(Interactive.class, Motion.Transform.class)),
            silence(of(Selectable.class, Motion.Transform.class)),

            // ── the scale: eleven, thirteen, fifteen, and nothing between ──
            scale(of(Caption.class, Type.Scale.class), "11px", "1.5", 1.2),
            scale(of(Label.class, Type.Scale.class), "13px", "1.45", 1.2),
            scale(of(Body.class, Type.Scale.class), "15px", "1.55", 1.2),

            // ── the small parts: cut like everything else, and flat ──────
            one(of(Control.Slider.Track.class, Shape.Corner.class), "2px"),
            one(of(Control.Slider.Knob.class, Shape.Corner.class), "2px"),
            one(of(Control.Option.class, Shape.Corner.class), "2px"),
            // a dock draws no frame of its own - the law of the pane - so nothing is said about its rule here
            one(of(Overlay.class, Shape.Corner.class), RADIUS),
            rule(of(Control.Option.class, Shape.Rule.class), "1px", "solid"),
            one(of(Detent.class, Shape.Corner.class), "1px"),
            one(of(Container.Menu.class, Shape.Corner.class), RADIUS),
            rule(of(Container.Menu.class, Shape.Rule.class), "1px", "solid"),

            scale(of(Heading.class, Type.Scale.class), "15px", "1.2", 1.2),
            scale(of(Display.class, Type.Scale.class), "22px", "1.15", 1.2),
            scale(of(Code.class, Type.Scale.class), "13px", "1.5", 1.2),
            measure(of(Control.Tab.class, Size.Extent.class), "168px", 1.25),
            gap(of(Control.Tab.class, Size.Gap.class), "8px", 1.25),
            one(of(Control.Tile.class, Shape.Corner.class), RADIUS),
            inset(of(Control.Tile.class, Size.Inset.class), "12px", "10px", 1.25),
            gap(of(Control.Tile.class, Size.Gap.class), "6px", 1.25),
            measure(of(Control.Tile.class, Size.Extent.class), "128px", 1.25),
            proportion(of(Control.Tile.class, Size.Proportion.class), "1.4", 1.5),
            inset(of(Container.Menu.class, Size.Inset.class), "6px", "0px", 1.25),

            // ── the type: one grotesque, one mono, nothing display about it ──
            one(of(Body.class, Type.Face.class), BODY_FACE),
            one(of(Heading.class, Type.Face.class), DISPLAY_FACE),
            one(of(Display.class, Type.Face.class), DISPLAY_FACE),
            one(of(Code.class, Type.Face.class), MONO_FACE)
    );
}
