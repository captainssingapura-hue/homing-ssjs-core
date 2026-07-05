package hue.captains.singapura.js.homing.expression.glance;

import hue.captains.singapura.js.homing.expression.DomainBoundIntent;
import hue.captains.singapura.js.homing.realm.plan.PhaseState;
import hue.captains.singapura.js.homing.realm.plan.Status;

/**
 * The phase-status Expression Intents of the <strong>Glance</strong> usage — how
 * a plan phase's status is expressed <em>at a glance</em> (a compact chip in an
 * overview), as opposed to how a progress bar or an overview row would express
 * the same status.
 *
 * <p>Each constant is a {@code (Glance usage × PhaseState)} cell: it
 * {@link #domain() binds} to the canonical {@code realm.plan.Status} dimension
 * and {@link #state() references} one canonical {@link PhaseState} value — it
 * never re-declares the value, so meaning stays anchored to the source of truth
 * and only the (glance-specific) realization is this family's own.</p>
 *
 * <p>Key: {@code glance.phase-status.<value>}.</p>
 */
public enum GlancePhaseStatusIntent implements DomainBoundIntent<Status> {

    NOT_STARTED(PhaseState.NOT_STARTED),
    IN_PROGRESS(PhaseState.IN_PROGRESS),
    BLOCKED(PhaseState.BLOCKED),
    DONE(PhaseState.DONE);

    private final PhaseState state;

    GlancePhaseStatusIntent(PhaseState state) { this.state = state; }

    /** The canonical {@link PhaseState} this glance intent references (source of truth). */
    public PhaseState state() { return state; }

    @Override public Status domain() { return Status.INSTANCE; }

    @Override public String key() { return "glance.phase-status." + slug(); }

    private String slug() { return state.name().toLowerCase().replace('_', '-'); }
}
