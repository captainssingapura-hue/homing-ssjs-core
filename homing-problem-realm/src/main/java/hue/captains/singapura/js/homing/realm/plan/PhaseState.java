package hue.captains.singapura.js.homing.realm.plan;

/**
 * The canonical value set of the plan-phase {@link Status} dimension — the
 * conceptual source of truth for what phase statuses exist.
 *
 * <p>These are <em>concepts</em>, not display intents and not physical form.
 * Usage-layer Expression Intents reference a {@code PhaseState}; the value is
 * fixed here once, its <em>realization</em> varies per usage. Each state carries
 * genuine business meaning that a generic {@code success/error} bucket erases —
 * {@code BLOCKED} is a <em>wait</em>, not a failure; {@code DONE} is phase
 * completion specifically, not a generic "good".</p>
 */
public enum PhaseState {

    /** Queued; work not begun. */
    NOT_STARTED("Queued; work not begun."),
    /** Actively underway. */
    IN_PROGRESS("Actively underway."),
    /** Stalled, awaiting an unblock — a wait, not a failure. */
    BLOCKED("Stalled; awaiting an unblock — a wait, not a failure."),
    /** Completed. */
    DONE("Completed.");

    private final String blurb;

    PhaseState(String blurb) { this.blurb = blurb; }

    /** One-line conceptual meaning of this state. */
    public String blurb() { return blurb; }
}
