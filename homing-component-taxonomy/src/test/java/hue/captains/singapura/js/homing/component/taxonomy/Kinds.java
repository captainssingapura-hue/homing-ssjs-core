package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A leaf and a composite holding it (D53) - and, after, declarations of kind that reading refuses: a leaf with parts, a
 * composite with none, a component that says it is both.
 */
final class Kinds {

    private Kinds() {}

    record Shelf() implements L1_ComponentBranch<Root> {
        static final Shelf INSTANCE = new Shelf();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Spot() implements Role<RoleRoot> {
        static final Spot INSTANCE = new Spot();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Knob() implements Leaf<Shelf>, WithRoles<Spots> {
        static final Knob INSTANCE = new Knob();
        @Override public Shelf parent() { return Shelf.INSTANCE; }
        @Override public Spots roleSet() { return Spots.INSTANCE; }
    }

    record Drawer() implements Composite<Shelf> {
        static final Drawer INSTANCE = new Drawer();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Shelf parent() { return Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Knob.INSTANCE).as(Spot.INSTANCE).one()); }
    }

    // ── refused when read ──────────────────────────────────────────────────

    /** A leaf that declares a part. */
    record Burdened() implements Leaf<Shelf> {
        static final Burdened INSTANCE = new Burdened();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Shelf parent() { return Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Knob.INSTANCE).as(Spot.INSTANCE).one()); }
    }

    /** A composite that declares no part. */
    record Hollow() implements Composite<Shelf> {
        static final Hollow INSTANCE = new Hollow();
        @Override public Shelf parent() { return Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(); }
    }

    /** A component that says it is both. */
    record Twofold() implements Leaf<Shelf>, Composite<Shelf> {
        static final Twofold INSTANCE = new Twofold();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Shelf parent() { return Shelf.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Knob.INSTANCE).as(Spot.INSTANCE).one()); }
    }

    // ── the roles each component may be cast in (RFC 0067, D55) ──────────

    /** What a knob may be cast as. */
    record Spots() implements RoleSet {
        static final Spots INSTANCE = new Spots();
        @Override public List<Role<?>> roles() { return List.of(Spot.INSTANCE); }
    }
}
