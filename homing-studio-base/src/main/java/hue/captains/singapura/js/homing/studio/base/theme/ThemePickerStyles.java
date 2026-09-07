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
     * Column: the tree takes the room, the palette preview sits under it as a
     * fixed strip. Master/detail at picker scale — the tree says what a theme
     * is, the strip shows what it looks like.
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

    /** The swatch strip. Contiguous, so the palette reads as one object. */
    public record tp_swatches() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            display: flex;
            border-radius: var(--radius-sm);
            overflow: hidden;
            border: 1px solid var(--color-border);
            height: 34px;
            """; }
    }

    /**
     * One swatch. The colour itself is the only thing set from JS, because it
     * is data rather than design — every other property lives here.
     */
    public record tp_sw() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            flex: 1 1 0;
            min-width: 0;
            /* The colour arrives as a custom property set by JS - the route
               RFC 0044 sanctions for DATA-driven values, since setProperty is a
               method call rather than a .style.x write. Everything else about a
               swatch is design and lives here. */
            background: var(--tp-sw, transparent);
            """; }
    }

    /**
     * Inline variant — the themes app hosts the tree in the page rather than a
     * modal, so it supplies the frame the modal would otherwise have given.
     */
    public record tp_inline() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            background: var(--color-surface-raised);
            max-width: 340px;
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
                new tp_swatches(), new tp_sw()
        );
    }
}
