package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * RFC 0063 — the DomOpsParty monitor's chrome: header, note, and the host the
 * tree is drawn into. The rows themselves are {@code TreeRenderer}'s and carry
 * its styling; a bespoke row sheet in the original demo's vocabulary was
 * built first and retired with the bespoke renderer, in review.
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

    /** The host the per-snapshot TreeRenderer draws into. */
    public record pm_tree() implements CssClass<PartyMonitorStyles> {
        @Override public String body() { return """
            flex: 1;
            min-height: 0;
            overflow: auto;
            padding: var(--space-2) var(--space-3);
            font-family: var(--font-mono);
            """; }
    }

    @Override
    public List<CssClass<PartyMonitorStyles>> cssClasses() {
        return List.of(
                new pm_root(), new pm_head(), new pm_title(), new pm_count(), new pm_btn(),
                new pm_note(), new pm_note_leaked(), new pm_tree()
        );
    }
}
