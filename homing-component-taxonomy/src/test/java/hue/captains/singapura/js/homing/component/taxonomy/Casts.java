package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * Role sets (RFC 0067, D55): a key cast in the role its set gives, a plain leaf only placed, and the castings and sets the
 * reading refuses. Each node's meaning is in {@code meanings/.../Casts.md}.
 */
final class Casts {

    private Casts() {}

    record Desk() implements L1_ComponentBranch<Root> {
        static final Desk INSTANCE = new Desk();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    /** Roles with a look of their own. */
    record Acts() implements L1_RoleBranch<RoleRoot> {
        static final Acts INSTANCE = new Acts();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Press() implements Role<Acts> {
        static final Press INSTANCE = new Press();
        @Override public Acts parent() { return Acts.INSTANCE; }
    }

    record Hold() implements Role<Acts> {
        static final Hold INSTANCE = new Hold();
        @Override public Acts parent() { return Acts.INSTANCE; }
    }

    /** Roles that only place their part. */
    record Seating() implements L1_RoleBranch<RoleRoot> {
        static final Seating INSTANCE = new Seating();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
        @Override public boolean functional() { return true; }
    }

    record Seat() implements Role<Seating> {
        static final Seat INSTANCE = new Seat();
        @Override public Seating parent() { return Seating.INSTANCE; }
    }

    // ── the sets ──

    /** What a key may be cast as. */
    record Pressing() implements RoleSet {
        static final Pressing INSTANCE = new Pressing();
        @Override public List<Role<?>> roles() { return List.of(Press.INSTANCE); }
    }

    /** A role named twice. */
    record PressedTwice() implements RoleSet {
        static final PressedTwice INSTANCE = new PressedTwice();
        @Override public List<Role<?>> roles() { return List.of(Press.INSTANCE, Press.INSTANCE); }
    }

    /** A role that only places a part: no casting. */
    record Seated() implements RoleSet {
        static final Seated INSTANCE = new Seated();
        @Override public List<Role<?>> roles() { return List.of(Seat.INSTANCE); }
    }

    /** No role at all. */
    record Nothing() implements RoleSet {
        static final Nothing INSTANCE = new Nothing();
        @Override public List<Role<?>> roles() { return List.of(); }
    }

    // ── read ──

    record Key() implements Leaf<Desk>, WithRoles<Pressing> {
        static final Key INSTANCE = new Key();
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public Pressing roleSet() { return Pressing.INSTANCE; }
    }

    record Plain() implements Leaf<Desk> {
        static final Plain INSTANCE = new Plain();
        @Override public Desk parent() { return Desk.INSTANCE; }
    }

    /** A key pressed, a plain leaf seated: each as its kind allows. */
    record Panel() implements Composite<Desk> {
        static final Panel INSTANCE = new Panel();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Key.INSTANCE).as(Press.INSTANCE).one(), DSL.part(Plain.INSTANCE).as(Seat.INSTANCE).one());
        }
    }

    // ── refused when read ──

    /** Casts the plain leaf, which has no role set, as Press. */
    record Pusher() implements Composite<Desk> {
        static final Pusher INSTANCE = new Pusher();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Plain.INSTANCE).as(Press.INSTANCE).one()); }
    }

    /** Casts the key as Hold, which its set does not give. */
    record Holder() implements Composite<Desk> {
        static final Holder INSTANCE = new Holder();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Key.INSTANCE).as(Hold.INSTANCE).one()); }
    }

    record Twice() implements Leaf<Desk>, WithRoles<PressedTwice> {
        static final Twice INSTANCE = new Twice();
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public PressedTwice roleSet() { return PressedTwice.INSTANCE; }
    }

    record Placed() implements Leaf<Desk>, WithRoles<Seated> {
        static final Placed INSTANCE = new Placed();
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public Seated roleSet() { return Seated.INSTANCE; }
    }

    record Idle() implements Leaf<Desk>, WithRoles<Nothing> {
        static final Idle INSTANCE = new Idle();
        @Override public Desk parent() { return Desk.INSTANCE; }
        @Override public Nothing roleSet() { return Nothing.INSTANCE; }
    }
}
