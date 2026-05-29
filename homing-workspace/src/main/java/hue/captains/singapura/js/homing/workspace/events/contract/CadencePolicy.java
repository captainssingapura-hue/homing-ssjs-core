package hue.captains.singapura.js.homing.workspace.events.contract;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for the hybrid checkpoint cadence: trigger on EITHER a
 * timer tick OR an event-count threshold, whichever fires first.
 *
 * <p>Hybrid because each mode covers what the other misses:</p>
 * <ul>
 *   <li>Timer alone wastes work on idle workspaces (re-checkpointing
 *       identical state every interval).</li>
 *   <li>Event-count alone falls behind on bursty workspaces (a flurry
 *       of events triggers one capture but no follow-up if activity
 *       stops mid-burst).</li>
 * </ul>
 *
 * <p>Recommended defaults: {@code interval = 30s}, {@code
 * eventCountThreshold = 100}. Both tunable per deployment; the contract
 * just shapes the policy.</p>
 *
 * @param interval            time between checkpoint candidates (timer arm)
 * @param eventCountThreshold events emitted since last checkpoint that triggers a write
 *
 * @since RFC 0035 P1
 */
public record CadencePolicy(Duration interval, int eventCountThreshold) {

    public CadencePolicy {
        Objects.requireNonNull(interval, "CadencePolicy.interval");
        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("CadencePolicy.interval: must be positive, got " + interval);
        }
        if (eventCountThreshold < 1) {
            throw new IllegalArgumentException(
                    "CadencePolicy.eventCountThreshold: must be positive, got " + eventCountThreshold);
        }
    }

    public static final CadencePolicy DEFAULT = new CadencePolicy(Duration.ofSeconds(30), 100);
}
