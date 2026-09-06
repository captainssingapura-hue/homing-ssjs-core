package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * The switcher's detail pane — what the right-hand side of the dialog draws
 * about the selected kind. The frame, scrim, glow and action row are
 * {@code SystemDialogStyles}; the split and the kind tree are
 * {@code MasterDetailStyles}. This sheet is only what neither of those knows:
 * a heading, a list host, a create row, and a maintenance row.
 *
 * <p>Every value is a token. The old control modal painted itself with
 * {@code style.cssText} and colour constants, which no rule could see; this is
 * the same surface with its colours where {@code no-literal-color} can read
 * them and its elements where {@code use-dom-ops-party} can count them.</p>
 */
public record WorkspaceSwitcherStyles() implements CssGroup<WorkspaceSwitcherStyles> {

    public static final WorkspaceSwitcherStyles INSTANCE = new WorkspaceSwitcherStyles();

    /** The pane as a column: heading, list (which takes the space), then the rows. */
    public record ws_detail() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: var(--space-2);
            height: 100%;
            min-height: 0;
            """; }
    }

    public record ws_head() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            font-size: 16px;
            font-weight: 600;
            color: var(--color-text-title);
            line-height: 1.3;
            """; }
    }

    /** Kind id and the "current" marker — small, muted, one line. */
    public record ws_sub() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            font-size: 11px;
            color: var(--color-text-muted);
            letter-spacing: 0.3px;
            """; }
    }

    /**
     * The instance list. A focus stop in its own right — Tab lands here from the
     * kind tree — so it carries a tabindex and shows a ring while it has the
     * keyboard, the same way MasterDetail's nav does.
     */
    public record ws_list() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            overflow-y: auto;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-surface-recessed);
            padding: var(--space-1) 0;
            outline: none;
            """; }
    }

    public record ws_list_focus() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String pseudoState() { return ":focus"; }
        @Override public String body() { return """
            border-color: color-mix(in srgb, var(--color-accent) 60%, var(--color-border));
            """; }
    }

    public record ws_note() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            font-size: 11px;
            color: var(--color-text-muted);
            """; }
    }

    /** A row of controls — the create row, and the maintenance row. */
    public record ws_row() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            gap: var(--space-2);
            flex-wrap: wrap;
            """; }
    }

    public record ws_input() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            flex: 1 1 160px;
            min-width: 0;
            font: inherit;
            font-size: 13px;
            padding: var(--space-1) var(--space-2);
            background: var(--color-surface-recessed);
            color: var(--color-text-primary);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            """; }
    }

    /** A quiet in-pane button. The dialog's own verbs live in its action row. */
    public record ws_btn() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            font: inherit;
            font-size: 12px;
            padding: var(--space-1) var(--space-3);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-surface);
            color: var(--color-text-primary);
            cursor: pointer;
            """; }
    }

    /** Destructive: the accent as a border rather than a fill, so it warns without shouting. */
    public record ws_btn_danger() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            border-color: color-mix(in srgb, var(--color-accent-emphasis) 60%, var(--color-border));
            color: var(--color-accent-emphasis);
            """; }
    }

    public record ws_btn_off() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            opacity: 0.45;
            cursor: default;
            """; }
    }

    /** Maintenance sits under a rule, apart from selection — it acts on what is open, not on what is chosen. */
    public record ws_maint() implements CssClass<WorkspaceSwitcherStyles> {
        @Override public String body() { return """
            padding-top: var(--space-2);
            border-top: 1px solid var(--color-border);
            """; }
    }

    @Override
    public CssImportsFor<WorkspaceSwitcherStyles> cssImports() {
        return CssImportsFor.none(this);
    }

    @Override
    public List<CssClass<WorkspaceSwitcherStyles>> cssClasses() {
        return List.of(
                new ws_detail(), new ws_head(), new ws_sub(),
                new ws_list(), new ws_list_focus(), new ws_note(),
                new ws_row(), new ws_input(),
                new ws_btn(), new ws_btn_danger(), new ws_btn_off(),
                new ws_maint()
        );
    }
}
