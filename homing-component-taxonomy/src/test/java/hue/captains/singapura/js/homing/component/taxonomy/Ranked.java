package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;
import java.util.Map;

/**
 * A hypothetical gear for the importance of states: five states at four levels, one of them pseudo; a knob that lists
 * them all, two of them of one level; a dial that overrides a level; and a console whose part, a knob steering it, lists
 * states of its own and overrides one of their levels. Then what is refused: a state of no level, two pseudo states that
 * are none, and a component that lists badly.
 */
final class Ranked {

    private Ranked() {}

    // ── the trees ──

    record Gear() implements L1_ComponentBranch<Root> {
        static final Gear INSTANCE = new Gear();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Acting() implements L1_RoleBranch<RoleRoot> {
        static final Acting INSTANCE = new Acting();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Steer() implements Role<Acting> {
        static final Steer INSTANCE = new Steer();
        @Override public Acting parent() { return Acting.INSTANCE; }
    }

    record Mode() implements L1_StateBranch<StateRoot> {
        static final Mode INSTANCE = new Mode();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
    }

    // ── states, a level each ──

    record Faded() implements Flag<Mode>, Importance.Senary {
        static final Faded INSTANCE = new Faded();
        @Override public Mode parent() { return Mode.INSTANCE; }
    }

    record Pointed() implements Flag<Mode>, Importance.Quinary, Pseudo {
        static final Pointed INSTANCE = new Pointed();
        @Override public Mode parent() { return Mode.INSTANCE; }
        @Override public String pseudoClass() { return ":hover"; }
    }

    record Picked() implements Flag<Mode>, Importance.Tertiary {
        static final Picked INSTANCE = new Picked();
        @Override public Mode parent() { return Mode.INSTANCE; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.TRUE, "picked"); }
    }

    record Armed() implements Flag<Mode>, Importance.Tertiary {
        static final Armed INSTANCE = new Armed();
        @Override public Mode parent() { return Mode.INSTANCE; }
    }

    record Grabbed() implements Flag<Mode>, Importance.Secondary {
        static final Grabbed INSTANCE = new Grabbed();
        @Override public Mode parent() { return Mode.INSTANCE; }
    }

    /** Listed by the console's part alone. */
    record Docked() implements Flag<Mode>, Importance.Quaternary {
        static final Docked INSTANCE = new Docked();
        @Override public Mode parent() { return Mode.INSTANCE; }
    }

    // ── components ──

    /** All five, Picked and Armed of one level, listed in that order. */
    record Knob() implements Component<Gear>, WithRoles<Steering> {
        static final Knob INSTANCE = new Knob();
        @Override public Gear parent() { return Gear.INSTANCE; }
        @Override public Steering roleSet() { return Steering.INSTANCE; }
        @Override public List<StateType<?>> states() {
            return List.of(Faded.INSTANCE, Picked.INSTANCE, Armed.INSTANCE, Pointed.INSTANCE, Grabbed.INSTANCE);
        }
    }

    /** Armed made the most important, for itself. */
    record Dial() implements Component<Gear> {
        static final Dial INSTANCE = new Dial();
        @Override public Gear parent() { return Gear.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(Armed.INSTANCE, Grabbed.INSTANCE); }
        @Override public List<ImportanceOverride<?>> importance() { return List.of(new ImportanceOverride<>(Armed.INSTANCE, Importance.Primary.class)); }
    }

    /** A knob steering it: cast as Steer, the knob still, with the knob's states. */
    record Console() implements Component<Gear> {
        static final Console INSTANCE = new Console();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Gear parent() { return Gear.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(Docked.INSTANCE); }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Knob.INSTANCE).as(Steer.INSTANCE).one()); }
    }

    // ── refused ──

    record Unranked() implements Flag<Mode> {
        static final Unranked INSTANCE = new Unranked();
        @Override public Mode parent() { return Mode.INSTANCE; }
    }

    /** A pseudo state with three values. */
    record Wide() implements StateType<Mode>, Importance.Quinary, Pseudo {
        static final Wide INSTANCE = new Wide();
        @Override public Mode parent() { return Mode.INSTANCE; }
        @Override public List<StateValue> values() { return List.of(new StateValue("low"), new StateValue("mid"), new StateValue("high")); }
        @Override public String pseudoClass() { return ":hover"; }
    }

    /** A pseudo-class without its colon. */
    record Colonless() implements Flag<Mode>, Importance.Quinary, Pseudo {
        static final Colonless INSTANCE = new Colonless();
        @Override public Mode parent() { return Mode.INSTANCE; }
        @Override public String pseudoClass() { return "active"; }
    }

    /** Armed listed twice, Picked's level overridden though it is not listed, and Armed's overridden twice. */
    record Muddled() implements Component<Gear> {
        static final Muddled INSTANCE = new Muddled();
        @Override public Gear parent() { return Gear.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(Armed.INSTANCE, Armed.INSTANCE); }
        @Override public List<ImportanceOverride<?>> importance() {
            return List.of(new ImportanceOverride<>(Picked.INSTANCE, Importance.Primary.class),
                           new ImportanceOverride<>(Armed.INSTANCE, Importance.Primary.class),
                           new ImportanceOverride<>(Armed.INSTANCE, Importance.Senary.class));
        }
    }

    // ── the roles each component may be cast in (RFC 0067, D55) ──────────

    /** What a knob may be cast as. */
    record Steering() implements RoleSet {
        static final Steering INSTANCE = new Steering();
        @Override public List<Role<?>> roles() { return List.of(Steer.INSTANCE); }
    }
}
