package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Typed CSS for {@link WidgetPickerModule}. Per RFC 0027, no raw cssText
 * strings in JS — every visual treatment earns a named CssClass the
 * picker JS references via the imported identifier.
 *
 * @since RFC 0025 Ext1b — Mechanism 2 (Widget Selector / Picker)
 */
public record WidgetPickerStyles() implements CssGroup<WidgetPickerStyles> {

    public static final WidgetPickerStyles INSTANCE = new WidgetPickerStyles();

    public record hwp_grid() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
                gap: 8px;
                padding: 8px;
                """; }
    }

    public record hwp_group_label() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                grid-column: 1 / -1;
                font: 600 11px sans-serif;
                color: var(--color-text-muted, #888);
                text-transform: uppercase;
                letter-spacing: 0.06em;
                padding: 6px 2px 2px;
                """; }
    }

    public record hwp_tile() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 4px;
                padding: 10px 6px;
                border-radius: 4px;
                cursor: pointer;
                background: var(--color-surface);
                border: 1px solid var(--color-border);
                color: var(--color-text-primary);
                transition: background 0.12s;
                """; }
    }

    public record hwp_tile_disabled() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                opacity: 0.45;
                cursor: not-allowed;
                """; }
    }

    public record hwp_tile_icon() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                font-size: 22px;
                line-height: 1;
                """; }
    }

    public record hwp_tile_label() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                font: 13px sans-serif;
                text-align: center;
                """; }
    }

    public record hwp_tile_desc() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                font: 11px sans-serif;
                color: var(--color-text-muted, #888);
                text-align: center;
                line-height: 1.2;
                """; }
    }

    public record hwp_form() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                padding: 12px;
                display: flex;
                flex-direction: column;
                gap: 8px;
                """; }
    }

    public record hwp_form_row() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 3px;
                """; }
    }

    public record hwp_form_label() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                font: 12px sans-serif;
                color: var(--color-text-muted, #888);
                """; }
    }

    public record hwp_form_input() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                padding: 5px 8px;
                border: 1px solid var(--color-border);
                background: var(--color-surface);
                color: var(--color-text-primary);
                border-radius: 3px;
                font: 13px sans-serif;
                """; }
    }

    public record hwp_form_actions() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                justify-content: flex-end;
                gap: 6px;
                margin-top: 4px;
                """; }
    }

    public record hwp_form_btn() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                padding: 5px 12px;
                border-radius: 3px;
                border: 1px solid var(--color-border);
                background: var(--color-surface);
                color: var(--color-text-primary);
                font: 13px sans-serif;
                cursor: pointer;
                """; }
    }

    public record hwp_form_btn_primary() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                background: var(--color-accent);
                color: var(--color-on-accent, #fff);
                border-color: var(--color-accent);
                """; }
    }

    @Override
    public List<CssClass<WidgetPickerStyles>> cssClasses() {
        return List.of(
                new hwp_grid(),
                new hwp_group_label(),
                new hwp_tile(),
                new hwp_tile_disabled(),
                new hwp_tile_icon(),
                new hwp_tile_label(),
                new hwp_tile_desc(),
                new hwp_form(),
                new hwp_form_row(),
                new hwp_form_label(),
                new hwp_form_input(),
                new hwp_form_actions(),
                new hwp_form_btn(),
                new hwp_form_btn_primary()
        );
    }

    @Override
    public CssImportsFor<WidgetPickerStyles> cssImports() {
        return CssImportsFor.none(this);
    }
}
