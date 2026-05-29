package hue.captains.singapura.js.homing.workspace.events.contract;

/**
 * Apply one event to live state. Implementations are domain-specific:
 * for workspace events, the handler is the chrome's replay step that
 * dispatches to {@code spawnRestoredWidget}, {@code mt.split}, {@code
 * mt.moveTab}, etc., based on which payload variant the event carries.
 *
 * <p>Pure interface — no replay-loop machinery. {@link Checkpointer} (or
 * a domain coordinator) drives the iteration; this handler is the
 * per-event hook. Keeps the handler trivially testable: feed it a
 * synthetic event, assert the live-state mutation.</p>
 *
 * @param <P> sealed payload type for this domain
 *
 * @since RFC 0035 P1
 */
public interface ReplayHandler<P> {

    /**
     * Apply the given event to live state. Idempotent at this level:
     * re-applying the same event (e.g. checkpoint-restored widget +
     * event-log spawn for the same widgetInstanceId) is a no-op.
     */
    void apply(Event<P> event);
}
