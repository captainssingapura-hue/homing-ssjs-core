package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The workspace switcher's pure half — no DOM, no window. RFC 0057, Phase 3.
 *
 * <p>Tree data for the kinds, list data for the instances, the positional paths
 * that pre-select the current ones, and the URL a choice navigates to. The view
 * ({@link WorkspaceSwitcherModule}) owns the elements; this owns the decisions,
 * which is what makes them testable and what keeps the view under the
 * effective-line limit — the same split {@code ThemePickerModel} makes for the
 * theme picker.</p>
 *
 * <p>{@code targetUrl} is built from the current URL so every other parameter
 * survives, and it clears {@code workspace}, {@code name} and {@code slowmo}
 * before writing the choice: those three are scoped to one kind and one
 * instance, and the old modal's three navigation helpers each cleared a
 * different subset of them by hand.</p>
 */
public record WorkspaceSwitcherModel() implements DomModule<WorkspaceSwitcherModel> {

    public record kindTreeData()        implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record kindOfSelection()     implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record pathOfKind()          implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record instanceListData()    implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record instanceOfSelection() implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record pathOfInstance()      implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record canDelete()           implements Exportable._Constant<WorkspaceSwitcherModel> {}
    public record targetUrl()           implements Exportable._Constant<WorkspaceSwitcherModel> {}

    public static final WorkspaceSwitcherModel INSTANCE = new WorkspaceSwitcherModel();

    @Override
    public ImportsFor<WorkspaceSwitcherModel> imports() {
        return ImportsFor.<WorkspaceSwitcherModel>builder().build();
    }

    @Override
    public ExportsOf<WorkspaceSwitcherModel> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new kindTreeData(), new kindOfSelection(), new pathOfKind(),
                new instanceListData(), new instanceOfSelection(), new pathOfInstance(),
                new canDelete(), new targetUrl()));
    }
}
