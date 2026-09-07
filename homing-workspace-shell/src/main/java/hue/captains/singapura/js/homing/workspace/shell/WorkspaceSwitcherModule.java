package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.ui.MasterDetail;
import hue.captains.singapura.js.homing.studio.base.ui.SystemDialog;

import java.util.List;

/**
 * The workspace switcher — RFC 0057, Phase 3. A tree of kinds beside the
 * instances of the selected kind, with Cancel / Open in new tab / Open beneath,
 * built on {@link SystemDialog} and {@link MasterDetail}.
 *
 * <p><b>It replaces {@code WorkspaceControlModal}</b>, keeping its constructor
 * and its {@code open/close/toggle/isOpen/destroy} surface so the shell chrome
 * swaps one symbol. What it does not keep: the flat 160px scroller of kinds
 * (thirteen in fin-dash — the failure RFC 0055 named for ten themes), the
 * mouse-only rows with no tab stop, the {@code style.cssText} colouring, the raw
 * {@code document.createElement}, and the three hand-written navigation helpers
 * that each cleared a different subset of {@code workspace}/{@code name}/
 * {@code slowmo}. Those are now one {@code targetUrl} in the model.</p>
 *
 * <p><b>Open in new tab</b> is {@code HrefManager.openNew}, which already
 * existed beside {@code navigate} — no {@code window.open}, which
 * {@code no-raw-href} would forbid for the same reason it forbids
 * {@code window.location}.</p>
 *
 * <p><b>Nothing navigates until confirmed</b>, and Tab cycles the regions
 * while arrows move within one — see the JS header.</p>
 */
public record WorkspaceSwitcherModule() implements DomModule<WorkspaceSwitcherModule> {

    /** The class. Same constructor options and surface as the control modal it replaces. */
    public record WorkspaceSwitcher() implements Exportable._Class<WorkspaceSwitcherModule> {}

    public static final WorkspaceSwitcherModule INSTANCE = new WorkspaceSwitcherModule();

    @Override
    public ImportsFor<WorkspaceSwitcherModule> imports() {
        return ImportsFor.<WorkspaceSwitcherModule>builder()
                .add(new ModuleImports<>(List.of(new SystemDialog.openSystemDialog()),
                        SystemDialog.INSTANCE))
                .add(new ModuleImports<>(List.of(new MasterDetail.mountMasterDetail()),
                        MasterDetail.INSTANCE))
                .add(new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParty()),
                        DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new WorkspaceSwitcherModel.kindTreeData(),
                        new WorkspaceSwitcherModel.kindOfSelection(),
                        new WorkspaceSwitcherModel.pathOfKind(),
                        new WorkspaceSwitcherModel.instanceListData(),
                        new WorkspaceSwitcherModel.instanceOfSelection(),
                        new WorkspaceSwitcherModel.pathOfInstance(),
                        new WorkspaceSwitcherModel.canDelete(),
                        new WorkspaceSwitcherModel.targetUrl()
                ), WorkspaceSwitcherModel.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new WorkspaceSwitcherStyles.ws_detail(),
                        new WorkspaceSwitcherStyles.ws_head(),
                        new WorkspaceSwitcherStyles.ws_sub(),
                        new WorkspaceSwitcherStyles.ws_list(),
                        new WorkspaceSwitcherStyles.ws_list_focus(),
                        new WorkspaceSwitcherStyles.ws_note(),
                        new WorkspaceSwitcherStyles.ws_row(),
                        new WorkspaceSwitcherStyles.ws_input(),
                        new WorkspaceSwitcherStyles.ws_btn(),
                        new WorkspaceSwitcherStyles.ws_btn_danger(),
                        new WorkspaceSwitcherStyles.ws_btn_off(),
                        new WorkspaceSwitcherStyles.ws_maint()
                ), WorkspaceSwitcherStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceSwitcherModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceSwitcher()));
    }
}
