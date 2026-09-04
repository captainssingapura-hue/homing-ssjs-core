package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.server.HrefManager;

import java.util.List;

/**
 * The theme picker — a grouped tree of the registry's themes, offered in two
 * shapes from one implementation: {@code mountThemePickerButton} for the chrome
 * (a header trigger that opens the tree in a modal) and
 * {@code mountThemePickerTree} for a host that already has room for it, which
 * is how the themes app uses it.
 *
 * <p><b>Why this module exists.</b> The picker used to be a Java text block in
 * {@code AppHtmlGetAction} — 178 lines, of which 110 were CSS and JavaScript
 * wearing a Java costume. Being string content, it was invisible to every rule
 * the framework has: it would have failed {@code use-dom-ops-party},
 * {@code no-dom-access}, {@code no-inline-style} and {@code no-literal-color}
 * on the day it was written, and nothing could say so. A component belongs in a
 * module where conformance can see it.</p>
 *
 * <p>It owns its own {@link ThemePickerStyles} group and its own DomOpsParty
 * branch, so it can be mounted anywhere without its host lending it either.</p>
 */
public record ThemePicker() implements DomModule<ThemePicker> {

    /** Header trigger + modal. Used by the studio chrome. */
    public record mountThemePickerButton() implements Exportable._Constant<ThemePicker> {}

    /** The bare tree, for a host with its own room for it. Used by the themes app. */
    public record mountThemePickerTree() implements Exportable._Constant<ThemePicker> {}

    public static final ThemePicker INSTANCE = new ThemePicker();

    @Override
    public ImportsFor<ThemePicker> imports() {
        return ImportsFor.<ThemePicker>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ThemePickerStyles.tp_btn(),
                        new ThemePickerStyles.tp_btn_label(),
                        new ThemePickerStyles.tp_scrim(),
                        new ThemePickerStyles.tp_scrim_open(),
                        new ThemePickerStyles.tp_panel(),
                        new ThemePickerStyles.tp_head(),
                        new ThemePickerStyles.tp_close(),
                        new ThemePickerStyles.tp_tree(),
                        new ThemePickerStyles.tp_grp_head(),
                        new ThemePickerStyles.tp_count(),
                        new ThemePickerStyles.tp_caret(),
                        new ThemePickerStyles.tp_shut(),
                        new ThemePickerStyles.tp_kids(),
                        new ThemePickerStyles.tp_kids_shut(),
                        new ThemePickerStyles.tp_caret_shut(),
                        new ThemePickerStyles.tp_thm(),
                        new ThemePickerStyles.tp_dot(),
                        new ThemePickerStyles.tp_on(),
                        new ThemePickerStyles.tp_dot_on(),
                        new ThemePickerStyles.tp_inline()
                ), ThemePickerStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemePicker> exports() {
        return new ExportsOf<>(INSTANCE,
                List.of(new mountThemePickerButton(), new mountThemePickerTree()));
    }
}
