package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Box;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Edge;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Frame;
import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Mark;

import hue.captains.singapura.js.homing.design.semantic.DesigningTaxonomy.Region;
import java.util.List;

/**
 * Designs of {@link DesigningTaxonomy}'s frame. Two that are read: a plain one, and a bold one that draws
 * its edge per style, darkens it under the hand, and takes the plain edge's colour and the plain mark's ring.
 * Then designs that are refused, one rule each. None takes a component's look: what a design draws is its own.
 */
final class DesigningDesigns {

    private DesigningDesigns() {}

    private static final Target.Shape.Border BORDER = Target.Shape.Border.INSTANCE;
    private static final Target.Color.Border BORDER_COLOUR = Target.Color.Border.INSTANCE;
    private static final Target.Shape.Outline RING = Target.Shape.Outline.INSTANCE;
    private static final Target.Color.Outline RING_COLOUR = Target.Color.Outline.INSTANCE;

    // ── read ───────────────────────────────────────────────────────────────

    record Plain() implements Design<BaseScheme, Frame> { static final Plain INSTANCE = new Plain(); }

    record PlainEdge() implements Edge.Concrete<Plain>, Drawn {
        static final PlainEdge INSTANCE = new PlainEdge();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(BORDER, "border-width: 1px; border-style: solid;"),
                           new Drawing.Painted<>(BORDER_COLOUR, new Paint.With<>(new BaseColours.Copy())));
        }
    }

    record PlainMark() implements Mark.Concrete<Plain>, Drawn {
        static final PlainMark INSTANCE = new PlainMark();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(RING, "outline-width: 2px; outline-style: solid; outline-offset: -2px;"),
                           new Drawing.None<>(RING_COLOUR));
        }
    }

    // ── the scheme a design is in (D33) ────────────────────────────────────

    /** Plain's drawings, in the loud scheme: drawn only in a theme whose scheme sees the loud. */
    record Shouted() implements Design<BaseColours.LoudScheme, Frame> { static final Shouted INSTANCE = new Shouted(); }

    record ShoutedEdge() implements Edge.Concrete<Shouted>, Drawn {
        static final ShoutedEdge INSTANCE = new ShoutedEdge();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return PlainEdge.INSTANCE.drawings(theme); }
    }

    record ShoutedMark() implements Mark.Concrete<Shouted>, Drawn {
        static final ShoutedMark INSTANCE = new ShoutedMark();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return PlainMark.INSTANCE.drawings(theme); }
    }

    /** Plain's drawings, in the marker, which is no scheme. */
    record Unschemed() implements Design<ColorScheme, Frame> { static final Unschemed INSTANCE = new Unschemed(); }

    record UnschemedEdge() implements Edge.Concrete<Unschemed>, Drawn {
        static final UnschemedEdge INSTANCE = new UnschemedEdge();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return PlainEdge.INSTANCE.drawings(theme); }
    }

    record UnschemedMark() implements Mark.Concrete<Unschemed>, Drawn {
        static final UnschemedMark INSTANCE = new UnschemedMark();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return PlainMark.INSTANCE.drawings(theme); }
    }

    record Bold() implements Design<BaseScheme, Frame> { static final Bold INSTANCE = new Bold(); }

    record BoldEdge() implements Edge.Concrete<Bold>, Drawn {
        static final BoldEdge INSTANCE = new BoldEdge();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            String rest = theme.style().token().equals("sketch") ? "border-width: 3px; border-style: dashed;" : "border-width: 2px; border-style: solid;";
            return List.of(new Drawing.Own<>(BORDER, new Answer.Own<Target.Shape.Border>(rest).at(State.HOVER, "border-width: 4px;")),
                           new Drawing.As<>(BORDER_COLOUR, PlainEdge.INSTANCE));
        }
    }

    record BoldMark() implements Mark.Concrete<Bold>, Drawn {
        static final BoldMark INSTANCE = new BoldMark();
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.As<>(RING, PlainMark.INSTANCE),
                           new Drawing.Painted<>(RING_COLOUR, new Paint.With<>(new BaseColours.Alarm())));
        }
    }


    // ── refused ────────────────────────────────────────────────────────────

    /** Draws the edge only. */
    record Half() implements Design<BaseScheme, Frame> { static final Half INSTANCE = new Half(); }
    record HalfEdge() implements Edge.Concrete<Half>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }

    /** A second concrete leaf of the plain edge. */
    record PlainEdgeAgain() implements Edge.Concrete<Plain>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }

    /** A mark that says nothing of how it is drawn. */
    record Mute() implements Design<BaseScheme, Frame> { static final Mute INSTANCE = new Mute(); }
    record MuteEdge() implements Edge.Concrete<Mute>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record MuteMark() implements Mark.Concrete<Mute> {}

    /** Its leaf's interface named raw: no design. */
    @SuppressWarnings("rawtypes")
    record RawMark() implements Mark.Concrete {}

    /** Draws a ring its edge does not own, its border twice, and leaves its border's colour undrawn. */
    record Wrong() implements Design<BaseScheme, Frame> { static final Wrong INSTANCE = new Wrong(); }
    record WrongEdge() implements Edge.Concrete<Wrong>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER), new Drawing.None<>(RING));
        }
    }
    record WrongMark() implements Mark.Concrete<Wrong>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Takes its border's colour from a mark. */
    record Lost() implements Design<BaseScheme, Frame> { static final Lost INSTANCE = new Lost(); }
    record LostEdge() implements Edge.Concrete<Lost>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.None<>(BORDER), new Drawing.As<>(BORDER_COLOUR, PlainMark.INSTANCE));
        }
    }
    record LostMark() implements Mark.Concrete<Lost>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Two designs whose edges each take their border from the other's. */
    record Ping() implements Design<BaseScheme, Frame> { static final Ping INSTANCE = new Ping(); }
    record Pong() implements Design<BaseScheme, Frame> { static final Pong INSTANCE = new Pong(); }
    record PingEdge() implements Edge.Concrete<Ping>, Drawn {
        static final PingEdge INSTANCE = new PingEdge();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.As<>(BORDER, PongEdge.INSTANCE), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record PongEdge() implements Edge.Concrete<Pong>, Drawn {
        static final PongEdge INSTANCE = new PongEdge();
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.As<>(BORDER, PingEdge.INSTANCE), new Drawing.None<>(BORDER_COLOUR)); }
    }
    record PingMark() implements Mark.Concrete<Ping>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }
    record PongMark() implements Mark.Concrete<Pong>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** Draws its border with a property no border owns: refused as the drawing is made. */
    record Broken() implements Design<BaseScheme, Frame> { static final Broken INSTANCE = new Broken(); }
    record BrokenEdge() implements Edge.Concrete<Broken>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) {
            return List.of(new Drawing.Own<>(BORDER, "color: red;"), new Drawing.None<>(BORDER_COLOUR));
        }
    }
    record BrokenMark() implements Mark.Concrete<Broken>, Drawn {
        @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
    }

    /** A second design named plain, from elsewhere. */
    static final class Elsewhere {
        private Elsewhere() {}
        record Plain() implements Design<BaseScheme, Frame> { static final Plain INSTANCE = new Plain(); }
        record OtherEdge() implements Edge.Concrete<Plain>, Drawn {
            @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(BORDER), new Drawing.None<>(BORDER_COLOUR)); }
        }
        record OtherMark() implements Mark.Concrete<Plain>, Drawn {
            @Override public List<Drawing<?>> drawings(Dressed theme) { return List.of(new Drawing.None<>(RING), new Drawing.None<>(RING_COLOUR)); }
        }
    }
}
