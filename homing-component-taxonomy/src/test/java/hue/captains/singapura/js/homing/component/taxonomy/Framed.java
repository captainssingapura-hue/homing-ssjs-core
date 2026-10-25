package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A local-style branch and two virtual leaves under it - and, after, declarations that reading refuses, one per rule a
 * local-style branch holds. A local style of it, and its concrete leaves, are the design side's: its own tests.
 */
final class Framed {

    private Framed() {}

    record Region() implements L1_ComponentBranch<Root> {
        static final Region INSTANCE = new Region();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Frame() implements L2_ComponentBranch<Region>, LocalStyleBranch {
        static final Frame INSTANCE = new Frame();
        @Override public Region parent() { return Region.INSTANCE; }
    }

    record Edge() implements VirtualComponent<Frame> {
        static final Edge INSTANCE = new Edge();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }

    record Mark() implements VirtualComponent<Frame> {
        static final Mark INSTANCE = new Mark();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }

    /** An ordinary component beside the branch, cast as any is. */
    record Box() implements Component<Region>, WithRoles<Places> {
        static final Box INSTANCE = new Box();
        @Override public Region parent() { return Region.INSTANCE; }
        @Override public Places roleSet() { return Places.INSTANCE; }
    }

    // ── refused when read ──────────────────────────────────────────────────

    /** An ordinary component under a local-style branch. */
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

    /** A branch under a local-style branch, and a leaf under it. */
    record SubFrame() implements L3_ComponentBranch<Frame> {
        static final SubFrame INSTANCE = new SubFrame();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }

    record Deep() implements Component<SubFrame> {
        static final Deep INSTANCE = new Deep();
        @Override public SubFrame parent() { return SubFrame.INSTANCE; }
    }

    // ── the roles each component may be cast in (RFC 0067, D55) ──────────

    /** What a box may be cast as. */
    record Places() implements RoleSet {
        static final Places INSTANCE = new Places();
        @Override public List<Role<?>> roles() { return List.of(Place.INSTANCE); }
    }
}
