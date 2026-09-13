package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * RFC 0063 — the DomOpsParty monitor's sheet, in the original demo's
 * vocabulary: {@code party-node}, {@code party-depth-badge},
 * {@code party-owner-dot--leaked}. Those names already say the right things,
 * and a reader who knew {@code es-el-manager/demo/partyTree.js} should
 * recognise the tree.
 *
 * <p>Every value is a token. The widget template this replaces styled itself
 * with {@code style.cssText}; that is baselined debt, not a pattern, and a
 * monitor whose whole point is that the framework can see itself should not
 * be invisible to {@code no-inline-style}.</p>
 */
public record PartyMonitorStyles() implements CssGroup<PartyMonitorStyles> {

    public static final PartyMonitorStyles INSTANCE = new PartyMonitorStyles();

    /** The widget: header pinned, tree scrolls. */
    public record pm_root() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            font-size: 12px;
            color: var(--color-text-primary);
            """; }
    }

    public record pm_head() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: var(--space-2);
            padding: var(--space-2) var(--space-3);
            border-bottom: 1px solid var(--color-border);
            background: var(--color-surface-raised);
            """; }
    }

    public record pm_title() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            font-weight: 600;
            color: var(--color-text-title);
            """; }
    }

    /** Branch and element totals: small and muted, after the title. */
    public record pm_count() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            flex: 1;
            """; }
    }

    public record pm_btn() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            font: inherit;
            padding: 2px 10px;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            background: var(--color-surface);
            color: var(--color-text-primary);
            cursor: pointer;
            """; }
    }

    /**
     * The one line under the header that says what the monitor cannot know:
     * none collected is not no leaks. Muted when clean, emphasised when not.
     */
    public record pm_note() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            padding: var(--space-1) var(--space-3);
            color: var(--color-text-muted);
            font-style: italic;
            """; }
    }

    public record pm_note_leaked() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            color: var(--color-accent-emphasis);
            font-style: normal;
            """; }
    }

    /** The scrolling tree. */
    public record pm_tree() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            flex: 1;
            min-height: 0;
            overflow: auto;
            padding: var(--space-2) var(--space-3);
            font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
            """; }
    }

    // ── One node ────────────────────────────────────────────────────────────

    public record pm_node() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            margin: 2px 0;
            """; }
    }

    /** The row: icon, name, depth, owner, dot, badges. Clickable to fold. */
    public record pm_node_header() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: var(--space-2);
            padding: 2px var(--space-1);
            border-radius: var(--radius-md);
            cursor: pointer;
            """; }
    }

    /** An owner that has been collected: the original demo's red row. */
    public record pm_node_header_leaked() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            background: color-mix(in srgb, var(--color-accent-emphasis) 12%, transparent);
            """; }
    }

    /** The monitor's own branch: the honest proof the view is live. */
    public record pm_node_header_self() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            outline: 1px dashed var(--color-accent);
            outline-offset: 1px;
            """; }
    }

    public record pm_icon() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            width: 1em;
            text-align: center;
            color: var(--color-text-muted);
            """; }
    }

    public record pm_name() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            font-weight: 600;
            """; }
    }

    public record pm_depth_badge() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            font-size: 10px;
            padding: 0 5px;
            border-radius: 8px;
            background: var(--color-surface-recessed);
            color: var(--color-text-muted);
            """; }
    }

    /** The owner label, as captured at activate(). */
    public record pm_owner() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            """; }
    }

    public record pm_owner_dot() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            width: 8px;
            height: 8px;
            border-radius: 50%;
            display: inline-block;
            """; }
    }

    public record pm_owner_dot_alive() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            background: var(--color-accent);
            """; }
    }

    public record pm_owner_dot_leaked() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            background: var(--color-accent-emphasis);
            """; }
    }

    /** "3 el", "2 branches". */
    public record pm_badge() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            font-size: 10px;
            padding: 0 5px;
            border: 1px solid var(--color-border);
            border-radius: 8px;
            color: var(--color-text-muted);
            """; }
    }

    /** Owned elements as chips under the row. */
    public record pm_elements() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            list-style: none;
            margin: 0 0 0 1.6em;
            padding: 0;
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            """; }
    }

    public record pm_chip() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            font-size: 11px;
            padding: 0 6px;
            border-radius: 4px;
            background: var(--color-surface-recessed);
            color: var(--color-text-muted);
            """; }
    }

    /** Children, indented. */
    public record pm_branches() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            margin-left: 1.2em;
            border-left: 1px solid var(--color-border);
            padding-left: var(--space-2);
            """; }
    }

    /** A folded node's children. */
    public record pm_folded() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            display: none;
            """; }
    }

    @Override
    public CssImportsFor<PartyMonitorStyles> cssImports() {
        return CssImportsFor.none(this);
    }

    @Override
    public List<CssClass<PartyMonitorStyles>> cssClasses() {
        return List.of(
                new pm_root(), new pm_head(), new pm_title(), new pm_count(), new pm_btn(),
                new pm_note(), new pm_note_leaked(), new pm_tree(),
                new pm_node(), new pm_node_header(), new pm_node_header_leaked(), new pm_node_header_self(),
                new pm_icon(), new pm_name(), new pm_depth_badge(),
                new pm_owner(), new pm_owner_dot(), new pm_owner_dot_alive(), new pm_owner_dot_leaked(),
                new pm_badge(), new pm_elements(), new pm_chip(), new pm_branches(), new pm_folded()
        );
    }
}
