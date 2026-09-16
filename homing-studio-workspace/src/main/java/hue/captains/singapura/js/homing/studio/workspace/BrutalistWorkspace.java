package hue.captains.singapura.js.homing.studio.workspace;

import hue.captains.singapura.js.homing.core.CssBlock;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.studio.base.theme.HomingBrutalist;
import hue.captains.singapura.js.homing.workspace.shell.CssGraphStyles;
import hue.captains.singapura.js.homing.workspace.shell.PartyMonitorStyles;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSwitcherStyles;

/**
 * RFC 0066 — Brutalist's word on the workspace's classes: the shell's buttons
 * and input, the CSS-graph workbench's and the party monitor's buttons. These
 * four rules used to sit in the theme's overlay in {@code homing-studio-base},
 * naming {@code .ws-btn} by string — a module studio-base cannot see. Here they
 * are impls for the groups that own those classes, in the module that sees
 * both the theme and the shell, registered by the starter's fixtures. The
 * dependency the string overlay hid is now a pom edge.
 *
 * <p>The blocks reuse the theme's own constants ({@code BUTTON}, {@code INERT})
 * so the workspace's buttons press exactly like the studio's.</p>
 */
public final class BrutalistWorkspace {

    private BrutalistWorkspace() {}

    public record Switcher() implements CssGroupImpl<WorkspaceSwitcherStyles, HomingBrutalist> {
        public static final Switcher INSTANCE = new Switcher();
        @Override public WorkspaceSwitcherStyles group() { return WorkspaceSwitcherStyles.INSTANCE; }
        @Override public HomingBrutalist theme() { return HomingBrutalist.INSTANCE; }

        public CssBlock<WorkspaceSwitcherStyles.ws_btn> ws_btn() { return CssBlock.of(HomingBrutalist.BUTTON); }
        public CssBlock<WorkspaceSwitcherStyles.ws_btn_danger> ws_btn_danger() { return CssBlock.of("""
                background: var(--color-text-link-hover);
                color: var(--bru-paper);
                &:hover { background: var(--bru-ink); color: var(--bru-paper); }
                """); }
        public CssBlock<WorkspaceSwitcherStyles.ws_btn_off> ws_btn_off() { return CssBlock.of(HomingBrutalist.INERT); }
        /** The shell's input inverts to yellow on focus, like the studio's search. */
        public CssBlock<WorkspaceSwitcherStyles.ws_input> ws_input() { return CssBlock.of("""
                font-weight: 600;
                color: var(--bru-ink);
                background: var(--bru-paper);
                border: var(--bru-rule);
                border-radius: 0;
                box-shadow: inset 5px 5px 0 color-mix(in srgb, var(--bru-ink) 9%, transparent);
                transition: none;
                &:focus {
                    outline: none;
                    background: var(--color-accent);
                    border-color: var(--bru-ink);
                    box-shadow: var(--bru-shadow-md);
                }
                &::placeholder { color: var(--color-text-muted); font-weight: 500; }
                """); }
    }

    public record Graph() implements CssGroupImpl<CssGraphStyles, HomingBrutalist> {
        public static final Graph INSTANCE = new Graph();
        @Override public CssGraphStyles group() { return CssGraphStyles.INSTANCE; }
        @Override public HomingBrutalist theme() { return HomingBrutalist.INSTANCE; }
        public CssBlock<CssGraphStyles.cg_btn> cg_btn() { return CssBlock.of(HomingBrutalist.BUTTON); }
    }

    public record Monitor() implements CssGroupImpl<PartyMonitorStyles, HomingBrutalist> {
        public static final Monitor INSTANCE = new Monitor();
        @Override public PartyMonitorStyles group() { return PartyMonitorStyles.INSTANCE; }
        @Override public HomingBrutalist theme() { return HomingBrutalist.INSTANCE; }
        public CssBlock<PartyMonitorStyles.pm_btn> pm_btn() { return CssBlock.of(HomingBrutalist.BUTTON); }
    }
}
