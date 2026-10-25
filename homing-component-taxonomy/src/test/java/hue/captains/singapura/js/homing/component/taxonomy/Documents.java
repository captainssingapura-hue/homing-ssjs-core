package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical page: a sheet branch the document wears, heading its family; its ground a leaf of it, CSS alone; a frame
 * branch for what stands on the page, a plain thing under it. Then what is refused: a busy ground with rooms, values and
 * an axis; a mat that plays a part of a box, and has the role set to; a component that says the document wears it.
 */
final class Documents {

    private Documents() {}

    record Sheet() implements L1_ComponentBranch<Root>, WornByTheDocument, FamilyHead {
        static final Sheet INSTANCE = new Sheet();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Frame() implements L1_ComponentBranch<Root> {
        static final Frame INSTANCE = new Frame();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Ground() implements Leaf<Sheet> {
        static final Ground INSTANCE = new Ground();
        @Override public Sheet parent() { return Sheet.INSTANCE; }
    }

    record Plain() implements Leaf<Frame> {
        static final Plain INSTANCE = new Plain();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }

    // ── refused ──

    /** A ground with what an element has. */
    record Busy() implements Leaf<Sheet> {
        static final Busy INSTANCE = new Busy();
        @Override public Sheet parent() { return Sheet.INSTANCE; }
        @Override public List<Room> rooms() { return List.of(new Room("edge")); }
        @Override public List<FixedValue> fixedValues() { return List.of(new FixedValue.YesNo("dark", false, false)); }
        @Override public List<ExtentAxis> extents() { return List.of(ExtentAxis.COLOUR); }
    }

    record Framing() implements L1_RoleBranch<RoleRoot> {
        static final Framing INSTANCE = new Framing();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Floor() implements Role<Framing> {
        static final Floor INSTANCE = new Floor();
        @Override public Framing parent() { return Framing.INSTANCE; }
    }

    /** What a mat may be cast as. */
    record Floors() implements RoleSet {
        static final Floors INSTANCE = new Floors();
        @Override public List<Role<?>> roles() { return List.of(Floor.INSTANCE); }
    }

    /** A leaf the document wears, cast as a box's floor. */
    record Mat() implements Leaf<Sheet>, WithRoles<Floors> {
        static final Mat INSTANCE = new Mat();
        @Override public Sheet parent() { return Sheet.INSTANCE; }
        @Override public Floors roleSet() { return Floors.INSTANCE; }
    }

    record Box() implements Composite<Frame> {
        static final Box INSTANCE = new Box();
        @Override public Frame parent() { return Frame.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(Mat.INSTANCE).as(Floor.INSTANCE).one()); }
    }

    /** A component that says the document wears it. */
    record Bossy() implements Leaf<Frame>, WornByTheDocument {
        static final Bossy INSTANCE = new Bossy();
        @Override public Frame parent() { return Frame.INSTANCE; }
    }
}
