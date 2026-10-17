package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical house whose marks are pictures, each a component of its own under one branch (D52): an up, a down and
 * a star. A header shows one of the up and the down as its sort mark; a row leads with any picture the house has. Each
 * node's meaning is in {@code meanings/.../Pictured.md}.
 */
final class Pictured {

    private Pictured() {}

    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;

    record Showing() implements L1_RoleBranch<RoleRoot> {
        static final Showing INSTANCE = new Showing();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Sort() implements Role<Showing> {
        static final Sort INSTANCE = new Sort();
        @Override public Showing parent() { return Showing.INSTANCE; }
    }

    record Lead() implements Role<Showing> {
        static final Lead INSTANCE = new Lead();
        @Override public Showing parent() { return Showing.INSTANCE; }
    }

    record Mark() implements L1_ComponentBranch<Root> {
        static final Mark INSTANCE = new Mark();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Picture() implements L2_ComponentBranch<Mark> {
        static final Picture INSTANCE = new Picture();
        @Override public Mark parent() { return Mark.INSTANCE; }
    }

    record Bar() implements L1_ComponentBranch<Root> {
        static final Bar INSTANCE = new Bar();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Up() implements Component<Picture> {
        static final Up INSTANCE = new Up();
        @Override public Picture parent() { return Picture.INSTANCE; }
    }

    record Down() implements Component<Picture> {
        static final Down INSTANCE = new Down();
        @Override public Picture parent() { return Picture.INSTANCE; }
    }

    record Star() implements Component<Picture> {
        static final Star INSTANCE = new Star();
        @Override public Picture parent() { return Picture.INSTANCE; }
    }

    /** One of the up and the down as its sort mark, or none: which is its own to say. */
    record Header() implements Component<Bar> {
        static final Header INSTANCE = new Header();
        @Override public Bar parent() { return Bar.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Up.INSTANCE, Down.INSTANCE).as(Sort.INSTANCE).optional()); }
    }

    /** Any picture the house has, leading it, or none. */
    record Row() implements Component<Bar> {
        static final Row INSTANCE = new Row();
        @Override public Bar parent() { return Bar.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.anyOf(Picture.INSTANCE).as(Lead.INSTANCE).optional()); }
    }
}
