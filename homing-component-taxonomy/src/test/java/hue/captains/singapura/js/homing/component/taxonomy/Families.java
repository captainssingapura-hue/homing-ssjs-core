package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical workshop for families of components: a cutter heading its family under the tools, which head none -
 * a saw whose edge is a blade cast as Edge; a gadget heading its family at the first level, a fuse in it two levels
 * down, through a branch that heads none; and a pebble under nothing that heads. Then what is refused: a head under the
 * gadget's head, and a component that says it heads.
 */
final class Families {

    private Families() {}

    record Tool() implements L1_ComponentBranch<Root> {
        static final Tool INSTANCE = new Tool();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Cutter() implements L2_ComponentBranch<Tool>, FamilyHead {
        static final Cutter INSTANCE = new Cutter();
        @Override public Tool parent() { return Tool.INSTANCE; }
    }

    record Gadget() implements L1_ComponentBranch<Root>, FamilyHead {
        static final Gadget INSTANCE = new Gadget();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Spare() implements L2_ComponentBranch<Gadget> {
        static final Spare INSTANCE = new Spare();
        @Override public Gadget parent() { return Gadget.INSTANCE; }
    }

    record Loose() implements L1_ComponentBranch<Root> {
        static final Loose INSTANCE = new Loose();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Cutting() implements L1_RoleBranch<RoleRoot> {
        static final Cutting INSTANCE = new Cutting();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Edge() implements Role<Cutting> {
        static final Edge INSTANCE = new Edge();
        @Override public Cutting parent() { return Cutting.INSTANCE; }
    }

    record Blade() implements Component<Cutter> {
        static final Blade INSTANCE = new Blade();
        @Override public Cutter parent() { return Cutter.INSTANCE; }
    }

    record Saw() implements Component<Cutter> {
        static final Saw INSTANCE = new Saw();
        @Override public Cutter parent() { return Cutter.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(Blade.INSTANCE).as(Edge.INSTANCE).one()); }
    }

    record Lamp() implements Component<Gadget> {
        static final Lamp INSTANCE = new Lamp();
        @Override public Gadget parent() { return Gadget.INSTANCE; }
    }

    record Fuse() implements Component<Spare> {
        static final Fuse INSTANCE = new Fuse();
        @Override public Spare parent() { return Spare.INSTANCE; }
    }

    record Pebble() implements Component<Loose> {
        static final Pebble INSTANCE = new Pebble();
        @Override public Loose parent() { return Loose.INSTANCE; }
    }

    // ── refused ──

    /** A head under the gadget's. */
    record Nested() implements L2_ComponentBranch<Gadget>, FamilyHead {
        static final Nested INSTANCE = new Nested();
        @Override public Gadget parent() { return Gadget.INSTANCE; }
    }

    record Bulb() implements Component<Nested> {
        static final Bulb INSTANCE = new Bulb();
        @Override public Nested parent() { return Nested.INSTANCE; }
    }

    /** A component that says it heads a family. */
    record Bossy() implements Component<Loose>, FamilyHead {
        static final Bossy INSTANCE = new Bossy();
        @Override public Loose parent() { return Loose.INSTANCE; }
    }
}
