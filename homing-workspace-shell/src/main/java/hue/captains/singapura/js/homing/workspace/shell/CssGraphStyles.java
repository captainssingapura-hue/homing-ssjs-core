package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * Typed CSS for the CSS graph workbench (RFC 0064): a head with the theme
 * worn, a plan bar, and the graph as columns by wave. Theme tokens
 * throughout, so the workbench re-themes under the switch it demonstrates.
 */
public record CssGraphStyles() implements CssGroup<CssGraphStyles> {

    public static final CssGraphStyles INSTANCE = new CssGraphStyles();

    public record cg_root() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            font-size: 12px;
            color: var(--color-text-primary);
            """; }
    }

    public record cg_head() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: var(--space-2);
            padding: var(--space-2) var(--space-3);
            border-bottom: 1px solid var(--color-border);
            flex: 0 0 auto;
            """; }
    }

    public record cg_title() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            font-weight: 700;
            """; }
    }

    /** "wears: forest" — the theme the page is under, from the manager. */
    public record cg_worn() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            margin-left: auto;
            """; }
    }

    public record cg_btn() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            font: inherit;
            font-size: 11px;
            padding: 2px var(--space-2);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-surface-raised);
            color: var(--color-text-primary);
            cursor: pointer;
            """; }
    }

    /** The plan bar: a target theme, Plan, Run, and the note. */
    public record cg_bar() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: var(--space-2);
            padding: var(--space-2) var(--space-3);
            border-bottom: 1px solid var(--color-border);
            flex: 0 0 auto;
            """; }
    }

    public record cg_select() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            font: inherit;
            font-size: 11px;
            padding: 2px var(--space-1);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-surface-base);
            color: var(--color-text-primary);
            """; }
    }

    public record cg_note() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            """; }
    }

    public record cg_note_err() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            color: var(--color-danger, var(--color-accent));
            """; }
    }

    /**
     * The graph: one column per wave, scrolling sideways when wide. Sized by
     * its content when the host has no height of its own (a widget host is
     * auto-height) and filling the rest when it has — a flex-basis of 0 here
     * collapsed the columns to a scrollbar sliver in a tab.
     */
    public record cg_waves() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            gap: var(--space-3);
            padding: var(--space-3);
            overflow: auto;
            flex: 1 1 auto;
            align-items: flex-start;
            """; }
    }

    public record cg_wave() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: var(--space-2);
            min-width: 180px;
            """; }
    }

    public record cg_wave_head() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--color-text-muted);
            """; }
    }

    /** One node: the group, its badges, its dependencies, its sheets. */
    public record cg_node() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-surface-raised);
            padding: var(--space-2);
            display: flex;
            flex-direction: column;
            gap: 2px;
            """; }
    }

    public record cg_node_id() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            font-weight: 600;
            word-break: break-all;
            """; }
    }

    public record cg_badge() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: inline-block;
            font-size: 9px;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            padding: 0 4px;
            border-radius: var(--radius-sm);
            background: var(--color-surface-recessed);
            color: var(--color-text-muted);
            margin-right: 4px;
            """; }
    }

    /** A prior: loads before the graph. */
    public record cg_badge_prior() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            background: var(--color-accent);
            color: var(--color-accent-on, var(--color-surface-base));
            """; }
    }

    /** Named by a dependency, never declared: its CSS is loaded by name, without handles. */
    public record cg_badge_unknown() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            border: 1px dashed var(--color-border-emphasis, var(--color-border));
            background: transparent;
            """; }
    }

    public record cg_deps() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            font-size: 11px;
            """; }
    }

    public record cg_sheets() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            font-size: 11px;
            """; }
    }

    /** One sheet on a card's line: "✓ forest", "● forest", "… forest". */
    public record cg_sheet() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            display: inline-block;
            margin-right: var(--space-2);
            transition: color 160ms ease, opacity 160ms ease;
            """; }
    }

    /** Appended and fetching — the first thing a switch shows. */
    public record cg_sheet_pending() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            opacity: 0.55;
            font-style: italic;
            """; }
    }

    /** Landed but not yet applied — waiting for the wave, or for the flip. */
    public record cg_sheet_landed() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            color: var(--color-accent);
            """; }
    }

    /** Applied — the green tick. The one literal here is the tick's green; no theme
     *  names a success colour, and the tick must read the same under every theme. */
    public record cg_sheet_applied() implements CssClass<CssGraphStyles> {
        @Override public String body() { return """
            color: #2E7D32;
            font-weight: 600;
            """; }
    }

    @Override
    public List<CssClass<CssGraphStyles>> cssClasses() {
        return List.of(
                new cg_root(), new cg_head(), new cg_title(), new cg_worn(), new cg_btn(),
                new cg_bar(), new cg_select(), new cg_note(), new cg_note_err(),
                new cg_waves(), new cg_wave(), new cg_wave_head(),
                new cg_node(), new cg_node_id(), new cg_badge(), new cg_badge_prior(), new cg_badge_unknown(),
                new cg_deps(), new cg_sheets(), new cg_sheet(), new cg_sheet_pending(), new cg_sheet_landed(), new cg_sheet_applied()
        );
    }
}
