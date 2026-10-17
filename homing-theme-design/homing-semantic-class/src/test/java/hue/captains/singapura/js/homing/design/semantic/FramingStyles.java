package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Edge;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Frame;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Mark;

import java.util.List;

/**
 * Local styles of {@link FramingTaxonomy}'s frame, each listing its concrete leaves. Two that are read: a plain one, and
 * a bold one that draws its edge per style, darkens it under the hand, and takes the plain edge's colour and the plain
 * mark's ring. Then local styles that are refused, one rule each. None takes a component's look: what a local style
 * draws is its own.
 */
final class FramingStyles {

    private FramingStyles() {}

    private static final Target.Shape.Border BORDER = Target.Shape.Border.INSTANCE;
    private static final Target.Color.Border BORDER_COLOUR = Target.Color.Border.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    // ── read ───────────────────────────────────────────────────────────────

    record Plain() implements LocalStyle<BaseScheme, Frame> {
        static final Plain INSTANCE = new Plain();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(PlainEdge.INSTANCE, PlainMark.INSTANCE); }
    }

    record PlainEdge() implements Edge.Concrete<Plain>, Drawn {
        static final PlainEdge INSTANCE = new PlainEdge();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(BORDER, "border-width: 1px; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Paint.With<>(new BaseColours.Copy())));
        }
    }

    record PlainMark() implements Mark.Concrete<Plain>, Drawn {
        static final PlainMark INSTANCE = new PlainMark();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -2px;"),
                           new Drawing.None<>(RING_COLOUR));
        }
    }

    /** Lists its mark before its edge: read in the branch's order all the same. */
    record Bold() implements LocalStyle<BaseScheme, Frame> {
        static final Bold INSTANCE = new Bold();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(BoldMark.INSTANCE, BoldEdge.INSTANCE); }
    }

    record BoldEdge() implements Edge.Concrete<Bold>, Drawn {
        static final BoldEdge INSTANCE = new BoldEdge();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            String rest = style.token().equals("sketch") ? "border-width: 3px; border-style: dashed;" : "border-width: 2px; border-style: solid;";
            return List.of(new Drawing.Own<>(BORDER, new Answer.Own<Target.Shape.Border>(rest).at(State.HOVER, "border-width: 4px;")),
                           new Drawing.As<>(BORDER_COLOUR, PlainEdge.INSTANCE));
        }
    }

    record BoldMark() implements Mark.Concrete<Bold>, Drawn {
        static final BoldMark INSTANCE = new BoldMark();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.As<>(RING, PlainMark.INSTANCE),
                           new Drawing.Painted<>(RING_COLOUR, new Paint.With<>(new BaseColours.Alarm())));
        }
    }

    // ── the scheme a local style is in (D33) ───────────────────────────────

    /** Plain's drawings, in the loud scheme: drawn only in a style whose scheme sees the loud. */
    record Shouted() implements LocalStyle<BaseColours.LoudScheme, Frame> {
        static final Shouted INSTANCE = new Shouted();
        @Override public BaseColours.LoudScheme scheme() { return BaseColours.LoudScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new ShoutedEdge(), new ShoutedMark()); }
    }

    record ShoutedEdge() implements Edge.Concrete<Shouted>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return PlainEdge.INSTANCE.drawings(style); }
    }

    record ShoutedMark() implements Mark.Concrete<Shouted>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return PlainMark.INSTANCE.drawings(style); }
    }

    /** Plain's drawings, in the marker, which is no scheme. */
    record Unschemed() implements LocalStyle<ColorScheme, Frame> {
        static final Unschemed INSTANCE = new Unschemed();
        @Override public ColorScheme scheme() { return null; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new UnschemedEdge(), new UnschemedMark()); }
    }

    record UnschemedEdge() implements Edge.Concrete<Unschemed>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return PlainEdge.INSTANCE.drawings(style); }
    }

    record UnschemedMark() implements Mark.Concrete<Unschemed>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return PlainMark.INSTANCE.drawings(style); }
    }

    // ── refused ────────────────────────────────────────────────────────────

    /** Draws the edge only. */
    record Half() implements LocalStyle<BaseScheme, Frame> {
        static final Half INSTANCE = new Half();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new HalfEdge()); }
    }
    record HalfEdge() implements Edge.Concrete<Half>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }

    /** Lists nothing at all. */
    record Empty() implements LocalStyle<BaseScheme, Frame> {
        static final Empty INSTANCE = new Empty();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(); }
    }

    /** Two concrete leaves of its edge. */
    record Doubled() implements LocalStyle<BaseScheme, Frame> {
        static final Doubled INSTANCE = new Doubled();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new DoubledEdge(), new DoubledEdgeAgain(), new DoubledMark()); }
    }
    record DoubledEdge() implements Edge.Concrete<Doubled>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record DoubledEdgeAgain() implements Edge.Concrete<Doubled>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record DoubledMark() implements Mark.Concrete<Doubled>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Lists the plain edge, a concrete leaf of another local style, as its own. */
    record Borrowing() implements LocalStyle<BaseScheme, Frame> {
        static final Borrowing INSTANCE = new Borrowing();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(PlainEdge.INSTANCE, new BorrowingMark()); }
    }
    record BorrowingMark() implements Mark.Concrete<Borrowing>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** A mark that says nothing of how it is drawn. */
    record Mute() implements LocalStyle<BaseScheme, Frame> {
        static final Mute INSTANCE = new Mute();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new MuteEdge(), new MuteMark()); }
    }
    record MuteEdge() implements Edge.Concrete<Mute>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record MuteMark() implements Mark.Concrete<Mute> {}

    /** Its mark's interface named raw: no local style read off it. */
    record Raw() implements LocalStyle<BaseScheme, Frame> {
        static final Raw INSTANCE = new Raw();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @SuppressWarnings("unchecked")
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new RawEdge(), new RawMark()); }
    }
    record RawEdge() implements Edge.Concrete<Raw>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }
    @SuppressWarnings("rawtypes")
    record RawMark() implements Mark.Concrete {}

    /** Draws a ring its edge does not own, its border twice, and leaves its border's colour undrawn. */
    record Wrong() implements LocalStyle<BaseScheme, Frame> {
        static final Wrong INSTANCE = new Wrong();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new WrongEdge(), new WrongMark()); }
    }
    record WrongEdge() implements Edge.Concrete<Wrong>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER), new Drawing.None<>(RING));
        }
    }
    record WrongMark() implements Mark.Concrete<Wrong>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Takes its border's colour from a mark. */
    record Lost() implements LocalStyle<BaseScheme, Frame> {
        static final Lost INSTANCE = new Lost();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new LostEdge(), new LostMark()); }
    }
    record LostEdge() implements Edge.Concrete<Lost>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.None<>(BORDER), new Drawing.As<>(BORDER_COLOUR, PlainMark.INSTANCE));
        }
    }
    record LostMark() implements Mark.Concrete<Lost>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Two local styles whose edges each take their border from the other's. */
    record Ping() implements LocalStyle<BaseScheme, Frame> {
        static final Ping INSTANCE = new Ping();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(PingEdge.INSTANCE, new PingMark()); }
    }
    record Pong() implements LocalStyle<BaseScheme, Frame> {
        static final Pong INSTANCE = new Pong();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(PongEdge.INSTANCE, new PongMark()); }
    }
    record PingEdge() implements Edge.Concrete<Ping>, Drawn {
        static final PingEdge INSTANCE = new PingEdge();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.As<>(BORDER, PongEdge.INSTANCE), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record PongEdge() implements Edge.Concrete<Pong>, Drawn {
        static final PongEdge INSTANCE = new PongEdge();
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.As<>(BORDER, PingEdge.INSTANCE), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record PingMark() implements Mark.Concrete<Ping>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }
    record PongMark() implements Mark.Concrete<Pong>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Draws its border with a property no border owns: refused as the drawing is made. */
    record Broken() implements LocalStyle<BaseScheme, Frame> {
        static final Broken INSTANCE = new Broken();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new BrokenEdge(), new BrokenMark()); }
    }
    record BrokenEdge() implements Edge.Concrete<Broken>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) {
            return List.of(new Drawing.Own<>(BORDER, "color: red;"), new Drawing.None<>(BORDER_COLOUR));
        }
    }
    record BrokenMark() implements Mark.Concrete<Broken>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** A second local style named plain, from elsewhere. */
    static final class Elsewhere {
        private Elsewhere() {}
        record Plain() implements LocalStyle<BaseScheme, Frame> {
            static final Plain INSTANCE = new Plain();
            @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
            @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new OtherEdge(), new OtherMark()); }
        }
        record OtherEdge() implements Edge.Concrete<Plain>, Drawn {
            @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
        }
        record OtherMark() implements Mark.Concrete<Plain>, Drawn {
            @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
        }
    }
}
