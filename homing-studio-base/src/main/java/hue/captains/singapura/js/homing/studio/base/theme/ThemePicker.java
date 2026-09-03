package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule;

import java.util.List;

/**
 * The theme picker — the registry's themes as a grouped tree, offered in two
 * shapes from one implementation: {@code mountThemePickerButton} for the chrome
 * (a header trigger that opens the tree in a {@code Modal}) and
 * {@code mountThemePickerTree} for a host that already has room for it, which is
 * how the themes app takes it.
 *
 * <p><b>It borrows rather than builds.</b> The dialog is {@code Modal}, the same
 * primitive the workspace-control modal uses, so the two look like the same
 * product. The rows are {@code TreeRenderer}, the framework's shared tree — which
 * brings the keyboard model with it: ArrowUp/Down move through visible rows,
 * ArrowRight/Left expand and fold a group, Enter activates. A hand-rolled tree
 * had none of that, and would have had to grow it.</p>
 *
 * <p>The split between {@code onSelect} and {@code onActivate} matters here.
 * Arrowing through the list only browses; Enter or a double-click switches the
 * theme. Without that distinction, arrowing down the list would reload the page
 * on every keystroke.</p>
 *
 * <p>Earlier this component was a 178-line Java text block in
 * {@code AppHtmlGetAction} — 110 of those lines CSS and JavaScript wearing a Java
 * costume, invisible to every rule the framework has. A component belongs in a
 * module where conformance can see it.</p>
 */
public record ThemePicker() implements DomModule<ThemePicker> {

    /** Header trigger plus modal. Used by the studio chrome. */
    public record mountThemePickerButton() implements Exportable._Constant<ThemePicker> {}

    /** The bare tree, for a host with its own room. Used by the themes app. */
    public record mountThemePickerTree() implements Exportable._Constant<ThemePicker> {}

    public static final ThemePicker INSTANCE = new ThemePicker();

    @Override
    public ImportsFor<ThemePicker> imports() {
        return ImportsFor.<ThemePicker>builder()
                .add(new ModuleImports<>(List.of(
                        new ThemePickerModel.activeThemeSlug(),
                        new ThemePickerModel.fetchThemes(),
                        new ThemePickerModel.themeBySlug(),
                        new ThemePickerModel.themeTreeData(),
                        new ThemePickerModel.slugOfSelection(),
                        new ThemePickerModel.switchToTheme(),
                        new ThemePickerModel.rememberPickerOpen(),
                        new ThemePickerModel.pickerReopenWanted()
                ), ThemePickerModel.INSTANCE))
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ModalModule.Modal()),
                        ModalModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new ThemePickerStyles.tp_btn(),
                        new ThemePickerStyles.tp_btn_label(),
                        new ThemePickerStyles.tp_tree_host(),
                        new ThemePickerStyles.tp_body(),
                        new ThemePickerStyles.tp_preview(),
                        new ThemePickerStyles.tp_preview_name(),
                        new ThemePickerStyles.tp_swatches(),
                        new ThemePickerStyles.tp_sw(),
                        new ThemePickerStyles.tp_inline(),
                        new ThemePickerStyles.tp_inline_head()
                ), ThemePickerStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemePicker> exports() {
        return new ExportsOf<>(INSTANCE,
                List.of(new mountThemePickerButton(), new mountThemePickerTree()));
    }
}
