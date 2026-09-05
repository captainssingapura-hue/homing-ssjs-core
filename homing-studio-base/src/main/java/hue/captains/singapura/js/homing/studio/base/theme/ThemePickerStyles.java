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

    /**
     * The page, switched off. z-index 9999 — one below Modal's 10000, so it
     * covers everything except the dialog it belongs to.
     *
     * <p>This is only half of "disabled". A scrim stops the mouse and tells the
     * eye; it does nothing about Tab, which walks straight behind it, or about
     * a screen reader, which reads through it. {@code inert} answers both, and
     * the picker sets it — see {@code ThemePicker.js}. Neither half works
     * alone: inert without a scrim is invisible, a scrim without inert is a
     * picture of modality rather than the thing.</p>
     */
    public record tp_scrim() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 9999;
            /* No colour at all — a FILTER on whatever is behind. The first cut
               mixed a veil out of --color-surface-inverted, which is a fine
               dark on Carbon and is the LIGHT GREY MENU BAR on Turbo C, so it
               washed that page out instead of dimming it. There is no token
               meaning "darker than whatever is there", because that is not a
               colour; brightness() is the operation, and it dims a light theme
               and a dark theme alike with nothing to choose per theme. */
            background: transparent;
            backdrop-filter: brightness(0.45) blur(2px);
            """; }
    }

    /**
     * The glow, applied to the Modal's own root. The picker owns the decision
     * that ITS dialog is lit, so the class is the picker's and rides on
     * {@code modal.el} — rather than Modal growing an option, which would mean
     * editing a module that is entirely conformance-baselined and already
     * within forty lines of the effective-line limit.
     *
     * <p>Every colour is mixed from {@code --color-accent}, so a theme re-lights
     * the dialog by re-binding one token — Turbo C glows Borland yellow and
     * Carbon glows amber, with nothing here to change.</p>
     */
    public record tp_glow() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            /* outline and filter, NOT border-color and box-shadow. Modal sets
               those two from an unlayered <style> tag it injects itself, and
               unlayered CSS beats layered CSS outright — specificity and source
               order never enter into it. These two properties Modal leaves
               alone, so they are the ones available without !important.
               outline also draws outside the border box, so the panel's
               overflow:hidden cannot clip it. */
            outline: 1px solid color-mix(in srgb, var(--color-accent) 60%, transparent);
            outline-offset: -1px;
            filter:
                drop-shadow(0 0 10px color-mix(in srgb, var(--color-accent) 42%, transparent))
                drop-shadow(0 0 28px color-mix(in srgb, var(--color-accent) 26%, transparent));
            """; }
    }

    /**
     * The action row. Buttons live here rather than in the Modal chrome because
     * they are this dialog's vocabulary, not every dialog's — a tool palette
     * has nothing to confirm.
     */
    public record tp_actions() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            flex: 0 0 auto;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: var(--space-2);
            padding: var(--space-2) var(--space-3);
            border-top: 1px solid var(--color-border);
            background: var(--color-surface-raised);
            """; }
    }

    /** The quiet actions — Cancel, and Apply until something is selected. */
    public record tp_action() implements CssClass<ThemePickerStyles> {
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

    /**
     * The confirming action. Takes the accent as its GROUND, not its text, so
     * the primary button is the one place in the dialog reading as a filled
     * shape — which is what makes it findable without a hierarchy of sizes.
     */
    public record tp_action_primary() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            background: var(--color-accent);
            border-color: var(--color-accent);
            color: var(--color-accent-on);
            font-weight: 600;
            """; }
    }

    /**
     * Nothing to do. Applying the theme already in use would spend a page load
     * to arrive where you are, so the control says so instead of pretending.
     */
    public record tp_action_off() implements CssClass<ThemePickerStyles> {
        @Override public String body() { return """
            opacity: 0.45;
            cursor: default;
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
                new tp_body(), new tp_scrim(), new tp_glow(), new tp_actions(), new tp_action(),
                new tp_action_primary(), new tp_action_off(), new tp_inline(), new tp_inline_head(),
                new tp_preview_name(), new tp_current(), new tp_preview_note(),
                new tp_swatches(), new tp_sw()
        );
    }
}
