package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * The look of a system dialog — frame, title bar, body, action row, and the two
 * things that make one MODAL: the scrim and the glow.
 *
 * <p>Its own group because it travels with {@link SystemDialog}, and because
 * every consumer takes the component rather than the sheet. Nothing here is
 * the theme picker's; the picker used to carry all of this itself and it moved
 * here unchanged in intent (RFC 0057, Phase 1).</p>
 *
 * <p><b>Why this is not styling {@code Modal}.</b> {@code ModalModule} injects
 * its CSS from an unlayered {@code <style>} tag, and unlayered CSS beats
 * layered CSS outright — the picker's glow could only reach {@code outline} and
 * {@code filter}, the two properties Modal happened not to set. A dialog drawn
 * by its own typed sheet has no such competitor, so the glow here is an
 * ordinary {@code border-color} and {@code box-shadow}, and the title bar
 * inherits the page font instead of Modal's hardcoded {@code sans-serif} — which
 * is what let Turbo C's dialog chrome fall out of monospace.</p>
 *
 * <p><b>Above everything.</b> Modal sits at z-index 10000 so a detached widget
 * floats over the workspace. A system dialog owns the screen, so it sits above
 * a detached widget too: scrim at 10010, frame at 10011.</p>
 */
public record SystemDialogStyles() implements CssGroup<SystemDialogStyles> {

    public static final SystemDialogStyles INSTANCE = new SystemDialogStyles();

    /**
     * The page, switched off. Dims by FILTER, not by colour: there is no token
     * meaning "darker than whatever is there", because that is not a colour.
     * A veil mixed from {@code --color-surface-inverted} was a fine dark on
     * Carbon and the light grey menu bar on Turbo C, and washed that page out
     * instead of dimming it. {@code brightness()} dims light and dark themes
     * alike with nothing to choose per theme.
     *
     * <p>This is only half of "disabled". A scrim stops the mouse and tells the
     * eye; it does nothing about Tab, which walks straight behind it, or about a
     * screen reader, which reads through it. {@code inert} answers both, and
     * the dialog sets it. Neither half works alone.</p>
     */
    public record sd_scrim() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            position: fixed;
            inset: 0;
            z-index: 10010;
            background: transparent;
            backdrop-filter: brightness(0.45) blur(2px);
            """; }
    }

    /**
     * The frame. Fixed and centred by transform, so it needs no measuring and
     * no container-origin caveat — Modal is absolutely positioned in pixels
     * against document.body, which is why it has one. Size arrives as two
     * custom properties the dialog sets with {@code setProperty}, the route
     * RFC 0044 sanctions for a value that is DATA.
     */
    public record sd_frame() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            position: fixed;
            left: 50%;
            top: 50%;
            transform: translate(-50%, -50%);
            width: var(--sd-w, 640px);
            height: var(--sd-h, 400px);
            z-index: 10011;
            display: flex;
            flex-direction: column;
            background: var(--color-surface);
            color: var(--color-text-primary);
            border: 1px solid var(--color-border);
            border-radius: var(--radius-md);
            overflow: hidden;
            box-shadow:
                0 10px 30px color-mix(in srgb, var(--color-surface-inverted) 45%, transparent),
                0 2px 6px  color-mix(in srgb, var(--color-surface-inverted) 30%, transparent);
            outline: none;
            """; }
    }

    /**
     * Lit. Every colour is mixed from {@code --color-accent}, so a theme
     * re-lights the dialog by re-binding one token — Turbo C glows Borland
     * yellow and Carbon glows amber, with nothing here to change. Layered over
     * the depth shadow rather than replacing it: the shadow says "above the
     * page", the glow says "and this is the thing you are using".
     */
    public record sd_glow() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            border-color: color-mix(in srgb, var(--color-accent) 55%, var(--color-border));
            box-shadow:
                0 10px 30px color-mix(in srgb, var(--color-surface-inverted) 45%, transparent),
                0 0 0 1px   color-mix(in srgb, var(--color-accent) 28%, transparent),
                0 0 36px    color-mix(in srgb, var(--color-accent) 30%, transparent);
            """; }
    }

    /**
     * The title bar IS chrome, so it takes the page's chrome band rather than a
     * raised panel's ground. A standalone window's caption bar is the same KIND
     * of thing as the studio header — a band naming what is below it — and the
     * dialog reads as part of the product when the two match: navy on the
     * fin-dash terminal, EGA light grey on Turbo C, near-black on Carbon.
     *
     * <p>This is the surface that wanted the chrome band. The MultiTabPane strip
     * does NOT: a pane is a region of the page, not a window over it, and it
     * keeps its raised ground.</p>
     *
     * <p>Inherits the page font — see the class javadoc.</p>
     */
    public record sd_title() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            display: flex;
            align-items: center;
            height: 28px;
            padding: 0 var(--space-3);
            background: var(--color-surface-inverted);
            border-bottom: 1px solid var(--color-border);
            flex-shrink: 0;
            user-select: none;
            """; }
    }

    public record sd_title_label() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            flex: 1;
            font-size: 11px;
            font-weight: 600;
            letter-spacing: 0.4px;
            text-transform: uppercase;
            color: var(--color-text-on-inverted-muted);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """; }
    }

    public record sd_close() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            font: inherit;
            font-size: 16px;
            line-height: 1;
            padding: 0 var(--space-1);
            background: transparent;
            border: 0;
            color: var(--color-text-on-inverted);
            cursor: pointer;
            """; }
    }

    /**
     * The body is a COLUMN host. Content that wants to fill it (MasterDetail's
     * split is {@code flex: 1 1 auto}) fills it; content that does not, does
     * not. The action row sits under it at its own fixed height.
     */
    public record sd_body() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-height: 0;
            display: flex;
            flex-direction: column;
            overflow: auto;
            """; }
    }

    /**
     * The action row. Present only when the dialog declares actions — a tool
     * palette has nothing to confirm, and an empty bar would say otherwise.
     */
    public record sd_actions() implements CssClass<SystemDialogStyles> {
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

    /** The quiet actions. */
    public record sd_action() implements CssClass<SystemDialogStyles> {
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
    public record sd_action_primary() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            background: var(--color-accent);
            border-color: var(--color-accent);
            color: var(--color-accent-on);
            font-weight: 600;
            """; }
    }

    /** Nothing to do — the control says so instead of pretending. */
    public record sd_action_off() implements CssClass<SystemDialogStyles> {
        @Override public String body() { return """
            opacity: 0.45;
            cursor: default;
            """; }
    }

    @Override
    public CssImportsFor<SystemDialogStyles> cssImports() {
        return CssImportsFor.none(this);
    }

    @Override
    public List<CssClass<SystemDialogStyles>> cssClasses() {
        return List.of(
                new sd_scrim(), new sd_frame(), new sd_glow(),
                new sd_title(), new sd_title_label(), new sd_close(),
                new sd_body(),
                new sd_actions(), new sd_action(), new sd_action_primary(), new sd_action_off()
        );
    }
}
