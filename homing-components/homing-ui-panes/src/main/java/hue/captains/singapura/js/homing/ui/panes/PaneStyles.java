package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Control;
import hue.captains.singapura.js.homing.design.Icon;
import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Dragging;
import static hue.captains.singapura.js.homing.design.Interaction.DropTarget;
import static hue.captains.singapura.js.homing.design.Interaction.Focus;
import static hue.captains.singapura.js.homing.design.Interaction.Inert;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Interaction.Current;
import static hue.captains.singapura.js.homing.design.Interaction.Selectable;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Pairing.OnPrimary;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Size;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Kicker;

/**
 * The panes' classes. A pane is a base surface; its strip is raised with a
 * divider under it; a chip is selectable, and the active one is
 * {@code aria-selected} so the design shows it however it shows a chosen
 * row; the chip in flight is inert; the drop mark is a primary bar. The
 * bodies hold layout and nothing that could be a value.
 *
 * <p>Names are {@code mtp_}; the studio's pane paints {@code .hmtp-} from an
 * injected sheet, so both may be on one page while the swap is under way.</p>
 */
public record PaneStyles() implements CssGroup<PaneStyles> {

    public static final PaneStyles INSTANCE = new PaneStyles();

    /** The pane: a column of strip over content, filling its host by growing — so the host is a flex column and the pane its item. */
    public record mtp_pane() implements CssClass<PaneStyles> {
        /** Its own word — a pane — so a design can say what a pane looks like while the keys are on it: the rule gives the mark its geometry and the edge its colour. A dock draws no frame at rest; the mark is an outline, and moves nothing. */
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class),
                                                                          of(Container.Pane.class, Shape.Rule.class), of(Container.Pane.class, Color.Edge.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            flex: 1 1 auto;
            align-self: stretch;
            min-height: 0;
            position: relative;
            """;
        }
    }

    /** The strip: raised, a divider under it, scrolling sideways when the chips overflow. The chips sit on its bottom edge, with room above for a lift. */
    public record mtp_strip() implements CssClass<PaneStyles> {
        /** The axes are set here and the chips inherit them, so the bar can measure a tab by the same numbers they do. */
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--size"), new CssVar("--aspect")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class)); }
        /**
         * The tab's frame, by reference rather than worn: a bar is one tab
         * tall whether or not it holds one — a dock parted off and still
         * empty is a bar, not a line — and the height follows the size and
         * the aspect axes and the theme, because it is the tab's own measure
         * over the tab's own proportion, not a number written down here.
         */
        @Override public List<? extends Wearable> reads() { return List.of(of(Control.Tab.class, Size.Extent.class), of(Control.Tab.class, Size.Proportion.class)); }
        @Override public String body() { return """
            display: flex;
            align-items: flex-end;
            flex-shrink: 0;
            overflow: hidden;
            padding-block: 4px 0;
            min-block-size: calc(var(--control-tab-size-extent-inline-size) * pow(var(--control-tab-size-extent-inline-size-ratio), var(--size, 0))
                               / (var(--control-tab-size-proportion) * pow(var(--control-tab-size-proportion-ratio-aspect), var(--aspect, 0))));
            user-select: none;
            """;
        }
    }

    /**
     * A chip: {@code Control.Tab} to the design — a hard frame whose inline
     * measure is the design's, grown by the element's size, its block size
     * following its proportion, wide and low, widened or narrowed by the
     * element's aspect; its corners cut at the top, its rule and ring a
     * control's. Selectable.Tab for its colour — seen at rest, tinted and
     * edged as a selectable hovered, then the design's hover, press,
     * selected and focus — and Selectable for its motion. The label is
     * ellipsised within. Dragged by the pointer, so no touch scrolling on it.
     */
    /**
     * The bar of the dock being worked in, lit: the word for the one you are
     * on, across the strip and nothing else. A HINT, not an outline — the
     * rooms of a workspace are divided by the grid's lines and a second set
     * of lines around one of them says the same thing twice; a bar that has
     * come up a shade says where the work is without drawing anything at all.
     * Colour only, so nothing moves, and it sits over the strip's own raised
     * face because a design's word for the current thing outranks a layer's.
     */
    public record mtp_strip_current() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class)); }
        @Override public String body() { return ""; }
    }

    public record mtp_chip() implements CssClass<PaneStyles> {
        /** {@code Control × Color.Edge} is the ring's colour and the keys' mark: bound at those states only, so the chip's own edge is still Selectable.Tab's. */
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Tab.class, Shape.Rule.class), of(Control.Tab.class, Shape.Corner.class), of(Control.class, Color.Edge.class),
                           of(Control.Tab.class, Size.Inset.class), of(Control.Tab.class, Size.Gap.class), of(Control.Tab.class, Size.Extent.class), of(Control.Tab.class, Size.Proportion.class),
                           of(Selectable.Tab.class, Color.Surface.class), of(Selectable.Tab.class, Color.Ink.class), of(Selectable.Tab.class, Color.Edge.class),
                           of(Selectable.class, Motion.Ease.class), of(Selectable.class, Affordance.Cursor.class),
                           of(Caption.class, Type.Scale.class));
        }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Control.Tab.class, Size.Inset.class), of(Control.Tab.class, Size.Gap.class), of(Control.Tab.class, Size.Extent.class), of(Caption.class, Type.Scale.class)); }
        @Override public List<? extends Wearable> aspects() { return List.of(of(Control.Tab.class, Size.Proportion.class)); }
        /** The squeeze, set on the rail and read here; and the axes, because the height below is written in them. */
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--chip-fit"), new CssVar("--size"), new CssVar("--aspect")); }
        /**
         * The tab's frame BY REFERENCE as well as worn, and for one reason:
         * the height is then WRITTEN DOWN rather than left to the proportion.
         * {@code Size.Proportion} is an {@code aspect-ratio}, which is exactly
         * right while a tab is the width the design asked for — and exactly
         * wrong the moment a crowded row squeezes it, because a ratio given a
         * narrower width answers with a shorter tab. Eight tabs in a bar meant
         * for five came out half as tall as the bar they sat in.
         *
         * <p>So the height is the design's own number — its extent over its
         * proportion, the same expression the bar uses to be one tab tall —
         * and with both sizes given, the ratio has nothing left to drive. The
         * squeeze is then a width and only ever a width.</p>
         */
        @Override public List<? extends Wearable> reads() { return List.of(of(Control.Tab.class, Size.Extent.class), of(Control.Tab.class, Size.Proportion.class)); }
        @Override public String body() { return """
            display: inline-flex;
            align-items: center;
            box-sizing: border-box;
            overflow: hidden;
            white-space: nowrap;
            flex: none;
            max-inline-size: var(--chip-fit, none);
            block-size: calc(var(--control-tab-size-extent-inline-size) * pow(var(--control-tab-size-extent-inline-size-ratio), var(--size, 0))
                           / (var(--control-tab-size-proportion) * pow(var(--control-tab-size-proportion-ratio-aspect), var(--aspect, 0))));
            touch-action: none;
            """;
        }
    }

    /**
     * The chip lifted: the whole operation is lift and shift, so the chip the
     * cursor is on is picked up off the row, and put down again the moment
     * the work moves into its tab or leaves the bar. It rides over its
     * neighbours while it is up.
     *
     * <p>The lift is its own translate and not the design's transform: a chip
     * that is selected already wears {@code Selectable.Tab}'s transform at a
     * state, which nothing plain can out-specify. The drag lifts itself the
     * same way, along the other axis.</p>
     */
    public record mtp_chip_lifted() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Selectable.class, Motion.Ease.class)); }
        @Override public String body() { return """
            position: relative;
            z-index: 1;
            translate: 0 -4px;
            """;
        }
    }

    /**
     * The mark on a chip while the keys are inside its tab: the design's
     * picture for "within", in the chip's own ink and never in the way of the
     * hand. It is there only while the keys are in the tab — the chip says
     * *held* while the bar has them, and *lent* with this mark while what the
     * tab holds has them.
     */
    public record mtp_chip_mark() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Icon.Within.class, Type.Glyph.class), of(Primary.class, Color.Ink.class)); }
        /** The one colour, at two degrees: part of the way while the cursor is on the bar, at full while the keys are in the tab. */
        @Override public List<? extends Wearable> extents() { return List.of(of(Primary.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: none;
            flex: none;
            align-items: center;
            justify-content: center;
            inline-size: 1.1em;
            block-size: 1em;
            line-height: 1;
            font-style: normal;
            pointer-events: none;
            user-select: none;
            """;
        }
    }

    /** The mark, while the keys are in the tab. */
    public record mtp_chip_mark_on() implements CssClass<PaneStyles> {
        @Override public String body() { return "display: inline-flex;"; }
    }

    /** The label in a chip: takes the room the cross leaves, and ellipsises. */
    public record mtp_chip_label() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            """;
        }
    }

    /**
     * A chip in its seat — in the row, not in the hand: the design's lift and
     * shadow by state, hover, press and selected. Taken off while the chip is
     * in the hand or afloat, so the design's word for those is the one seen.
     */
    public record mtp_chip_seated() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Selectable.class, Motion.Transform.class), of(Selectable.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }

    /**
     * The chip in the hand: lifted over the row and under the hand along
     * it, where {@code --mtp-drag-x} put it — its own translate, so the
     * design's transform is the one for a thing being dragged, with its
     * shadow and cursor.
     */
    public record mtp_chip_dragging() implements CssClass<PaneStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--mtp-drag-x")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Dragging.class, Motion.Transform.class), of(Dragging.class, Shape.Shadow.class), of(Dragging.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            position: relative;
            z-index: 2;
            translate: var(--mtp-drag-x, 0px) 0;
            """;
        }
    }

    /** A chip stepping aside for the one in the hand: one pitch over, {@code --mtp-shift-x}, eased by the design as any move of a selectable. */
    public record mtp_chip_shifted() implements CssClass<PaneStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--mtp-shift-x")); }
        @Override public String body() { return """
            transform: translateX(var(--mtp-shift-x, 0px));
            """;
        }
    }

    /** The strip while a chip is in the hand: nothing clipped, so the lifted chip and its shadow are seen whole, over the content. */
    public record mtp_strip_loose() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            overflow: visible;
            position: relative;
            z-index: 2;
            """;
        }
    }

    /**
      * The cross on a chip that can be closed: the design's own close mark,
      * and nothing drawn around it. The word carries the picture — the design
      * writes it as content on the mark's ::before — so nothing here is one,
      * and the chip's own frame is frame enough: a ring of its own would be a
      * second box inside a box. It takes no keys either, being reached
      * through the party and not by the tab order, so it wears no ring.
      */
    public record mtp_chip_close() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Interactive.class, Affordance.Cursor.class), of(Icon.Close.class, Type.Glyph.class)); }
        @Override public String body() { return """
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font: inherit;
            line-height: 1;
            inline-size: 1.25em;
            block-size: 1em;
            padding: 0;
            background: transparent;
            border: 0;
            flex: none;
            """;
        }
    }

    /** Where a dragged chip will land: a primary bar between two chips. */
    public record mtp_drop_mark() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return """
            width: 3px;
            flex-shrink: 0;
            align-self: stretch;
            margin: 2px -1.5px;
            pointer-events: none;
            """;
        }
    }

    /** The end of the strip: the add button and the count, pushed right. */
    /**
      * THE WINDOW over the row. The strip is the whole bar — the rail, the
      * plus, the count — and this is the part of it the chips live in, and the
      * only part that moves. It takes what the bar can spare and no more
      * ({@code flex: 0 1 auto} over {@code min-inline-size: 0}), so with a few
      * tabs it is exactly as wide as they are and the plus sits against the
      * last of them, and with many it is the room that is left and the plus
      * has stopped following.
      *
      * <p>{@code overflow: hidden} rather than {@code auto}: the window is
      * moved by the strip, in whole tabs, and a scrollbar would offer a
      * second way of moving it that lands between two. Hidden still scrolls
      * when {@code scrollLeft} is set — that is the whole mechanism.</p>
      */
    public record mtp_rail() implements CssClass<PaneStyles> {
        /** The width the row is squeezed to, read by every chip in it; none until there are too many. */
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--chip-fit")); }
        @Override public String body() { return """
            display: flex;
            align-items: flex-end;
            flex: 0 1 auto;
            min-inline-size: 0;
            overflow: hidden;
            """;
        }
    }

    public record mtp_strip_tail() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            align-self: center;
            gap: 8px;
            margin-left: auto;
            padding: 0 8px;
            flex-shrink: 0;
            """;
        }
    }

    /** The add button: a control that is interactive; Control carries its focus ring. */
    public record mtp_add() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class), of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class), of(Control.class, Color.Edge.class)); }
        @Override public String body() { return """
            font: inherit;
            line-height: 1;
            padding: 3px 8px;
            """;
        }
    }

    /** The add button when the budget is spent: inert, and says so. Toggled beside {@code disabled}. */
    /**
      * The plus ON THE RAIL, where a browser keeps it: after the last chip,
      * moving along as tabs come and go, because it is about THE END OF THE
      * ROW and not about the end of the bar. Parked at the far right it reads
      * as a tool of the pane; here it reads as "and another", which is what
      * it does.
      *
      * <p>It borrows the chip's GEOMETRY and none of the chip's colour: the
      * same corner, the same inset and the same height, so it sits in the row
      * as a sibling and grows with the chips when the size is dialled — and
      * then it is drawn empty. {@code Selectable × Color.Surface} is flat at
      * rest and fills under the cursor, which is the whole of a new-tab
      * button's manner; the chip's own {@code Selectable.Tab} would be a
      * resting fill, and a plus that looks like a tab is a tab.</p>
      *
      * <p>Every one of those is a pair a design binds, so a design that wants
      * its plus boxed, or bigger, or silent until hovered, says so where it
      * says everything else.</p>
      */
    public record mtp_rail_add() implements CssClass<PaneStyles> {
        /** The axes, because its height is a tab's height and a tab's height moves with them. */
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--size"), new CssVar("--aspect")); }
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Tab.class, Shape.Corner.class),
                           of(Selectable.class, Color.Surface.class), of(Selectable.class, Motion.Ease.class), of(Selectable.class, Affordance.Cursor.class),
                           of(Muted.class, Color.Ink.class), of(Control.class, Color.Edge.class),
                           of(Icon.Add.class, Type.Glyph.class));
        }
        /**
         * The tab's frame BY REFERENCE, as the bar itself reads it: one tab
         * tall, by the tab's own measure over the tab's own proportion, at
         * whatever the size and aspect axes say. Worn it would be a tab's
         * WIDTH as well — a hundred and sixty-eight pixels of nothing — and
         * the plus is square, because a square is what a glyph wants.
         */
        @Override public List<? extends Wearable> reads() { return List.of(of(Control.Tab.class, Size.Extent.class), of(Control.Tab.class, Size.Proportion.class)); }
        @Override public String body() { return """
            display: inline-flex;
            align-items: center;
            justify-content: center;
            box-sizing: border-box;
            flex: none;
            block-size: calc(var(--control-tab-size-extent-inline-size) * pow(var(--control-tab-size-extent-inline-size-ratio), var(--size, 0))
                           / (var(--control-tab-size-proportion) * pow(var(--control-tab-size-proportion-ratio-aspect), var(--aspect, 0))));
            aspect-ratio: 1;
            margin-inline-start: 2px;
            background-color: transparent;
            border: 0;
            font: inherit;
            line-height: 1;
            """;
        }
    }
    public record mtp_add_off() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }

    /** The count: tabs used of the budget, as a kicker. */
    public record mtp_pill() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "white-space: nowrap;"; }
    }

    /** The content area: the tabs' panels stacked, one shown. */
    public record mtp_content() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            display: flex;
            flex-direction: column;
            position: relative;
            """;
        }
    }

    /** A tab's panel: the widget's root inside, scrolling on its own. Never detached; hidden when not the active one. */
    public record mtp_tab_content() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            overflow: auto;
            position: relative;
            isolation: isolate;
            """;
        }
    }

    /** A panel not shown, or the empty note while there are tabs: last in the sheet, so it wins the display. */
    public record mtp_tab_content_hidden() implements CssClass<PaneStyles> {
        @Override public String body() { return "display: none;"; }
    }



    /** What a new tab holds until it is told what to hold: the prompt over the choices, centred in the room the tab was given. */
    public record mtp_opener() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 14px;
            padding: 18px;
            min-height: 0;
            overflow: auto;
            """;
        }
    }

    /** The question, quietly: a tab that is asking is not a tab that is shouting. */
    public record mtp_opener_note() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;"; }
    }

    /** The choices, as many to a row as the room allows. */
    public record mtp_opener_grid() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 10px;
            """;
        }
    }

    /** One thing that could be opened. It wears what the strip's plus wears, since it is the same gesture at its full size. */
    public record mtp_opener_pick() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class),
                                                                          of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class),
                                                                          of(Interactive.class, Motion.Ease.class), of(Control.class, Color.Edge.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 9px 16px;
            """;
        }
    }

    // ── the little picture of the panes, and the control that adds by it ──

    /**
      * The frame of the picture: the room every pane is in, at the shape it
      * really has. The aspect is measured rather than chosen — a tall
      * workspace gets a tall thumbnail — so that the small arrangement is the
      * big one, and pointing at a box is pointing at a place.
      */
    public record mtp_thumbs() implements CssClass<PaneStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--thumbs-width"), new CssVar("--thumbs-aspect")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class), of(Container.Pane.class, Shape.Corner.class)); }
        @Override public String body() { return """
            position: relative;
            flex: 0 0 auto;
            box-sizing: border-box;
            inline-size: var(--thumbs-width, 132px);
            aspect-ratio: var(--thumbs-aspect, 1.6);
            """;
        }
    }

    /**
      * One pane, small. It wears the pane's own rule and edge because that is
      * what it is a picture OF; what the pane holds is not drawn, since a
      * thumbnail of a workspace is about places and not about contents.
      */
    public record mtp_thumb() implements CssClass<PaneStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--thumb-x"), new CssVar("--thumb-y"), new CssVar("--thumb-w"), new CssVar("--thumb-h")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Container.Pane.class, Shape.Rule.class),
                                                                          of(Container.Pane.class, Color.Edge.class), of(Interactive.class, Affordance.Cursor.class),
                                                                          of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
            position: absolute;
            inset-inline-start: var(--thumb-x, 0%);
            inset-block-start: var(--thumb-y, 0%);
            inline-size: var(--thumb-w, 100%);
            block-size: var(--thumb-h, 100%);
            box-sizing: border-box;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            padding: 0;
            font: inherit;
            """;
        }
    }

    /** The one the control is pointing at: the word for the current thing, and nothing that moves. */
    public record mtp_thumb_on() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class), of(Current.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    /** A pane that would not take another: shown, because where it is belongs to the picture, but not choosable. */
    public record mtp_thumb_off() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }

    /** The word in the box: small, quiet, and clipped rather than allowed to burst the pane it names. */
    public record mtp_thumb_label() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            padding: 0 3px;
            """;
        }
    }

    /** The control's row: the picture, the list, the button. */
    public record mtp_new() implements CssClass<PaneStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
            """;
        }
    }

    /** What to mount: a native list, dressed as the control beside it. */
    public record mtp_new_pick() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class), of(Raised.class, Color.Edge.class),
                                                                          of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
            font: inherit;
            padding: 3px 6px;
            """;
        }
    }

    /** The pane while a tab from outside is offered to it: the drop-target word, on its surface, its edge and its rule. */
    public record mtp_dock_target() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(DropTarget.class, Color.Surface.class), of(DropTarget.class, Color.Edge.class), of(DropTarget.class, Shape.Rule.class)); }
        @Override public String body() { return ""; }
    }

    /** What an empty pane says. */
    public record mtp_empty() implements CssClass<PaneStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
            flex: 1 1 auto;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 24px;
            """;
        }
    }

    @Override
    public List<CssClass<PaneStyles>> cssClasses() {
        return List.of(new mtp_pane(), new mtp_strip(), new mtp_rail(), new mtp_strip_loose(), new mtp_chip(), new mtp_chip_label(), new mtp_strip_current(), new mtp_chip_mark(), new mtp_chip_mark_on(), new mtp_chip_lifted(), new mtp_chip_seated(), new mtp_chip_dragging(), new mtp_chip_shifted(),
                       new mtp_chip_close(), new mtp_drop_mark(), new mtp_strip_tail(), new mtp_add(), new mtp_rail_add(), new mtp_add_off(),
                       new mtp_pill(), new mtp_content(), new mtp_tab_content(), new mtp_empty(), new mtp_dock_target(),
                       new mtp_opener(), new mtp_opener_note(), new mtp_opener_grid(), new mtp_opener_pick(),
                       new mtp_thumbs(), new mtp_thumb(), new mtp_thumb_on(), new mtp_thumb_off(), new mtp_thumb_label(), new mtp_new(), new mtp_new_pick(),
                       new mtp_tab_content_hidden());   // last, so hidden wins over what a panel or the empty note says of its display
    }
}
