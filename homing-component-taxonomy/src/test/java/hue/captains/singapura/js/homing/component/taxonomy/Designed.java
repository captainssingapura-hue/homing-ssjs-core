package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A design branch, two virtual leaves under it, two designs of it and their concrete leaves - and, after,
 * declarations that compile and that reading refuses, one per rule a design branch holds.
 */
final class Designed {

    private Designed() {}

    record Region() implements L1_ComponentBranch<Root> {
        static final Region INSTANCE = new Region();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Frame() implements L2_ComponentBranch<Region>, DesignBranch {
        static final Frame INSTANCE = new Frame();
        @Override public Region parent() { return Region.INSTANCE; }
    }

    record Edge() implements VirtualComponent<Frame> {
        static final Edge INSTANCE = new Edge();
        @Override public Frame parent() { return Frame.INSTANCE; }
        interface Concrete<D extends Design<?, Frame>> extends ConcreteComponent<Edge, Frame, D> {}
    }

    record Mark() implements VirtualComponent<Frame> {
        static final Mark INSTANCE = new Mark();
        @Override public Frame parent() { return Frame.INSTANCE; }
        interface Concrete<D extends Design<?, Frame>> extends ConcreteComponent<Mark, Frame, D> {}
    }

    /** An ordinary component beside the branch, cast as any is. */
    record Box() implements Component<Region> {
        static final Box INSTANCE = new Box();
        @Override public Region parent() { return Region.INSTANCE; }
    }

    record Plain() implements Design<BaseScheme, Frame> {

        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        static final Plain INSTANCE = new Plain();
    }

    record Bold() implements Design<BaseScheme, Frame> {

        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        static final Bold INSTANCE = new Bold();
    }

    record PlainEdge() implements Edge.Concrete<Plain> {}

    record BoldMark() implements Mark.Concrete<Bold> {}

    /** A concrete leaf that names all three arguments itself, without its leaf's interface. */
    record DirectMark() implements ConcreteComponent<Mark, Frame, Plain> {}

    /** Its leaf's interface named raw: no design given. */
    @SuppressWarnings("rawtypes")
    record RawEdge() implements Edge.Concrete {}

    // ── refused when read ──────────────────────────────────────────────────

    /** An ordinary component under a design branch. */
    record Stray() implements Component<Frame> {
        static final Stray INSTANCE = new Stray();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }

    record Place() implements Role<RoleRoot> {
        static final Place INSTANCE = new Place();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    /** A component with a part played by a virtual leaf. */
    record Holder() implements Component<Region> {
        static final Holder INSTANCE = new Holder();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Region parent() { return Region.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Edge.INSTANCE).as(Place.INSTANCE).one()); }
    }

    /** A virtual leaf with parts. */
    record Partful() implements VirtualComponent<Frame> {
        static final Partful INSTANCE = new Partful();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Frame parent() { return Frame.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Box.INSTANCE).as(Place.INSTANCE).one()); }
    }

    /** A branch under a design branch, and a leaf under it. */
    record SubFrame() implements L3_ComponentBranch<Frame> {
        static final SubFrame INSTANCE = new SubFrame();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }

    record Deep() implements Component<SubFrame> {
        static final Deep INSTANCE = new Deep();
        @Override public SubFrame parent() { return SubFrame.INSTANCE; }
    }
}
