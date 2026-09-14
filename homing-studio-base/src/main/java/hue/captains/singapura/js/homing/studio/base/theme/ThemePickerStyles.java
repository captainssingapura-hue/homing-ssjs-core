package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Typed CSS for {@link ThemePicker} — deliberately small.
 *
 * <p>An earlier cut carried nineteen classes: a scrim, a panel, a header, a
 * close button, group headers, carets, rows, dots. All of it re-implemented two
 * things the framework already ships — {@code Modal} for the dialog and
 * {@code TreeRenderer} for the tree — and re-implemented them worse, since
 * neither the collapse behaviour nor the keyboard model came along with it.</p>
 *
 * <p>What remains is only what is genuinely the picker's own: its header
 * trigger, and the containers its tree sits in.</p>
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

    /**
     * Column: the tree takes the room, the preview pane sits beside it.
     * Master/detail at picker scale — the tree says what a theme is, the
     * frame shows what it looks like.
     */
    public record tp_body() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            /* A column: MasterDetail's split takes the space, the footer sits
               under it. The split is flex:1 1 auto, so it yields to the
               footer's fixed height rather than pushing it out of view. */
            display: flex;
            flex-direction: column;
            height: 100%;
            min-height: 0;
            """; }
    }







    public record tp_preview_name() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: baseline;
            gap: var(--space-2);
            font-weight: 700;
            font-size: 15px;
            color: var(--color-text-primary);
            margin-bottom: var(--space-2);
            """; }
    }

    /** Shown beside the name when the selected theme is the one in use. */
    public record tp_current() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            font-size: 10px;
            font-weight: 600;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            color: var(--color-accent);
            """; }
    }

    /** The inspiration line, in the pane rather than crowding the row. */
    public record tp_preview_note() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            font-size: 12px;
            line-height: 1.5;
            color: var(--color-text-muted);
            margin-bottom: var(--space-4);
            """; }
    }

    /**
     * The preview frame — the preview page, wearing the selected theme. The
     * pane is a scrolling column, so the frame takes a fixed, generous height
     * rather than trying to fill a parent that has no height to give; the page
     * inside scrolls on its own. Bordered like the swatch strip it replaces, so
     * it reads as one object in the pane.
     */
    public record tp_preview_frame() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: block;
            width: 100%;
            height: 420px;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-surface-base);
            """; }
    }

    /**
     * Inline variant — the themes app hosts the tree in the page rather than a
     * modal, so it supplies the frame the modal would otherwise have given.
     * Wide enough for the preview pane to show a page rather than a sliver:
     * the tree is a narrow column of names and the rest is the frame.
     */
    public record tp_inline() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            background: var(--color-surface-raised);
            max-width: 760px;
            overflow: hidden;
            """; }
    }

    public record tp_inline_head() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            padding: var(--space-3) 14px;
            border-bottom: 1px solid var(--color-border);
            font-weight: 600;
            color: var(--color-text-primary);
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
                new tp_body(), new tp_inline(), new tp_inline_head(),
                new tp_preview_name(), new tp_current(), new tp_preview_note(),
                new tp_preview_frame()
        );
    }
}
