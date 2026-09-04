package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Typed CSS for {@link ThemePicker} — its own group rather than more of
 * {@link hue.captains.singapura.js.homing.studio.base.css.StudioStyles}, because
 * the picker is a component with two hosts (the chrome and the themes app) and
 * its styles travel with it.
 *
 * <p>Every value is a theme token. The picker is the thing you use to change the
 * theme, so it has to look right in whichever theme you are leaving — a literal
 * palette here would be visible as the one element that did not follow.</p>
 */
public record ThemePickerStyles() implements CssGroup<ThemePickerStyles> {

    public static final ThemePickerStyles INSTANCE = new ThemePickerStyles();

    /** The header trigger — reads as chrome, so it paints on inverted tokens. */
    public record tp_btn() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            font: inherit;
            display: inline-flex;
            align-items: center;
            gap: var(--space-1);
            border: 1px solid var(--color-border);
            background: transparent;
            color: var(--color-text-on-inverted);
            cursor: pointer;
            padding: 2px var(--space-2);
            border-radius: var(--radius-sm);
            margin-left: auto;
            """; }
    }

    public record tp_btn_label() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            color: var(--color-text-on-inverted-muted);
            """; }
    }

    /** Full-viewport scrim. The one place a literal is unavoidable: a scrim
        must read as "not the page" under both light and dark themes, and no
        token means that. rgba on black works in either direction. */
    public record tp_scrim() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 9999;
            background: rgba(0, 0, 0, 0.55);
            display: none;
            align-items: flex-start;
            justify-content: center;
            padding-top: 12vh;
            font: 13px system-ui, sans-serif;
            """; }
    }

    /** Open state. The scrim defaults to display:none because a CSS display
        rule beats the `hidden` attribute — hidden alone left the modal on screen. */
    public record tp_scrim_open() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: flex;
            """; }
    }

    public record tp_panel() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            background: var(--color-surface-raised);
            color: var(--color-text-primary);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-lg);
            box-shadow: 0 18px 48px rgba(0, 0, 0, 0.45);
            min-width: 288px;
            max-width: 380px;
            max-height: 66vh;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            """; }
    }

    public record tp_head() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: var(--space-4);
            padding: var(--space-3) 14px;
            border-bottom: 1px solid var(--color-border);
            font-weight: 600;
            """; }
    }

    public record tp_close() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            font: inherit;
            background: transparent;
            border: 0;
            cursor: pointer;
            color: var(--color-text-muted);
            padding: 2px var(--space-1);
            line-height: 1;
            """; }
    }

    public record tp_tree() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            overflow-y: auto;
            padding: 6px 0 var(--space-2);
            """; }
    }

    /** Group header — a disclosure control, so it is a button, not a div. */
    public record tp_grp_head() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            font: inherit;
            width: 100%;
            text-align: left;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 6px;
            background: transparent;
            border: 0;
            color: var(--color-text-muted);
            padding: 6px 14px;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            font-size: 11px;
            font-weight: 700;
            """; }
    }

    public record tp_count() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            margin-left: auto;
            opacity: 0.65;
            font-weight: 500;
            """; }
    }

    public record tp_caret() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: inline-block;
            transition: transform 120ms;
            """; }
    }

    /** Collapsed group — set on the group wrapper, rotates the caret and hides
        the children. A class rather than a style write, so the state is a fact
        the DOM asserts rather than a pixel value written from JS. */
    public record tp_shut() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            /* marker class; see tp_kids and tp_caret co-selectors below */
            """; }
    }

    public record tp_kids() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: block;
            """; }
    }

    public record tp_kids_shut() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: none;
            """; }
    }

    public record tp_caret_shut() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            transform: rotate(-90deg);
            """; }
    }

    public record tp_thm() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            font: inherit;
            width: 100%;
            text-align: left;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 9px;
            background: transparent;
            border: 0;
            color: var(--color-text-primary);
            padding: 6px 14px 6px 26px;
            """; }
    }

    public record tp_dot() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            width: 7px;
            height: 7px;
            border-radius: 50%;
            border: 1px solid var(--color-border-emphasis);
            flex: 0 0 auto;
            """; }
    }

    /** The active theme's row. */
    public record tp_on() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            color: var(--color-text-link);
            font-weight: 600;
            """; }
    }

    public record tp_dot_on() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            background: var(--color-accent);
            border-color: var(--color-accent);
            """; }
    }

    /** Inline variant — the themes app hosts the tree in the page, not a modal. */
    public record tp_inline() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            background: var(--color-surface-raised);
            max-width: 340px;
            overflow: hidden;
            """; }
    }

    @Override
    public CssImportsFor<ThemePickerStyles> cssImports() {
        return CssImportsFor.none(this);
    }

    @Override
    public List<CssClass<ThemePickerStyles>> cssClasses() {
        return List.of(
                new tp_btn(), new tp_btn_label(),
                new tp_scrim(), new tp_scrim_open(), new tp_panel(), new tp_head(), new tp_close(),
                new tp_tree(), new tp_grp_head(), new tp_count(),
                new tp_caret(), new tp_shut(), new tp_kids(), new tp_kids_shut(), new tp_caret_shut(),
                new tp_thm(), new tp_dot(), new tp_on(), new tp_dot_on(),
                new tp_inline()
        );
    }
}
