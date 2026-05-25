package hue.captains.singapura.js.homing.workspace;

/**
 * Per-widget hint that drives the workspace's open/close semantics.
 * Declared on each {@link WorkspaceWidget} via {@code lifecycleHint()};
 * read by the workspace controller when the user invokes "open" via
 * the picker and by the workspace shell at boot.
 *
 * <ul>
 *   <li>{@link #MULTI} — default. Every open creates a fresh instance.
 *       Two instances of the same widget type can coexist with different
 *       params. Closing a tab dissolves its widget branch. The picker
 *       tile is always enabled.</li>
 *   <li>{@link #SINGLETON} — at most one instance per workspace. Opening
 *       when one already exists dismisses the picker and focuses the
 *       existing instance (activating its tab) instead of creating a new
 *       one. The picker tile is greyed out when an instance is live,
 *       with a tooltip indicating "focus existing".</li>
 *   <li>{@link #PINNED} — auto-instantiated at workspace boot into a
 *       default pane; the tab's close affordance is disabled. Suitable
 *       for permanent fixtures like a status bar, navigator, or
 *       always-on inspector. <b>Hidden from the picker</b> — the user
 *       neither adds nor removes a PINNED widget. Capacity bookkeeping
 *       still counts PINNED tabs against the 16-tab budget.</li>
 * </ul>
 *
 * <p>The hint is advisory at the contract level — workspace
 * implementations honour it, downstream consumers (e.g. a future
 * mini-workspace wrapper) may treat it differently if their model
 * doesn't support all variants.</p>
 *
 * <p><b>Ordinal stability</b>: enum order is pinned ({@code MULTI=0},
 * {@code SINGLETON=1}, {@code PINNED=2}) so JS code-gen can key off
 * {@code ordinal()} when emitting the registry table. New variants
 * append; never reorder.</p>
 *
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry); semantic
 *        of the third variant amended in Ext1b (Mechanism 2 — Picker)
 *        from "transient inspector" to "auto-spawned permanent fixture"
 *        per design call 2026-05-23.
 */
public enum LifecycleHint {
    MULTI,
    SINGLETON,
    PINNED
}
