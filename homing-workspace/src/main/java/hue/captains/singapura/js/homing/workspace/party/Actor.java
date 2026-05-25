package hue.captains.singapura.js.homing.workspace.party;

/**
 * The widget-side contract for messaging participation. Widgets that
 * join any Party implement this interface; the framework invokes
 * {@link #partyDeregister()} when the widget's tab is being closed so
 * the widget can leave every Party it joined before DOM tear-down.
 *
 * <p>The full Actor concept (DOM scope, reactor map, parent Secretary)
 * is a JS-side runtime concern — the Java side just exposes the
 * deterministic deregister hook. {@code MultiTabPane}'s close-button
 * path checks for this interface on the widget and calls
 * {@link #partyDeregister()} first, then {@code onClose} for general
 * cleanup. The order guarantees the widget is messaging-quiet by the
 * time DOM tear-down begins (see RFC 0028 lifecycle section).</p>
 *
 * <h2>Helper bases</h2>
 *
 * <p>Widgets that join Parties through the framework's standard
 * registration helpers get an automatic implementation that walks the
 * registered Parties and calls {@code leave()} on each. Widgets that
 * register manually implement this directly. Either way: the consumer
 * never writes a tab-close-time deregister call — the close path
 * triggers it deterministically.</p>
 *
 * @since RFC 0028 cycle 1
 */
public interface Actor {

    /**
     * Leave every Party this Actor joined. Invoked exactly once per
     * Actor lifecycle by {@code MultiTabPane}'s close path, immediately
     * before the widget's general {@code onClose} runs.
     *
     * <p>Implementations should be idempotent — second invocations
     * should no-op rather than throw. The framework guarantees a
     * single invocation under normal flow; defensive idempotence
     * protects against ill-behaved consumer code.</p>
     */
    void partyDeregister();
}
