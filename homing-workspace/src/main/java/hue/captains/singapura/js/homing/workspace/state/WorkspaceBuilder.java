package hue.captains.singapura.js.homing.workspace.state;

/**
 * The mutation-side counterpart to {@link LiveWorkspace} —
 * {@link WorkspaceRestore} calls these methods, in order, to rebuild a
 * live workspace from a {@link WorkspaceState} snapshot. The interface
 * is the contract; the actual live implementation lives in JS (Cycle 4
 * and beyond), and mock implementations exercise the algorithm in
 * Java tests.
 *
 * <p>Method dispatch on the {@link WidgetLocation} sealed ADT keeps the
 * pane-case and the modal-case visibly distinct at the builder boundary
 * — exactly the "slight coupling" mentioned in RFC 0029 §three-axes,
 * named here so live implementations can't fudge it: the modal slot is
 * a separate method, not a magic value of paneId.</p>
 *
 * @since RFC 0029 cycle 3
 */
public interface WorkspaceBuilder {

    /**
     * Rebuild the layout tree to match the captured shape. Implementations
     * may either tear down the existing layout and rebuild, or diff-apply
     * — the restore algorithm makes no assumption.
     */
    void resetLayout(LayoutNode layout);

    /**
     * Construct a widget instance and attach it to a pane's tab strip.
     * The {@code instance.location()} is guaranteed to equal the
     * {@code location} parameter — they're passed separately so the
     * builder can pattern-match without re-casting.
     */
    void openInPane(WidgetInstance instance, WidgetLocation.InPane location);

    /**
     * Construct a widget instance and place it in the workspace's single
     * transit modal slot. Called at most once per restore (the single-modal
     * invariant is enforced by {@link WorkspaceState} at capture time).
     */
    void openInModal(WidgetInstance instance, WidgetLocation.InModal location);

    /**
     * Apply chrome settings (theme, fullscreen). Called last in the
     * restore sequence so theme switches and fullscreen toggles don't
     * thrash widget construction.
     */
    void applyChrome(ChromeState chrome);
}
