package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.WidgetPickerModule;

import java.util.List;

/**
 * {@code PickerTabFlow} — Phase 13 of the workspace-shell chrome. The
 * {@code +} affordance on every pane strip funnels here: open an empty
 * picker tab, mount a {@code WidgetPicker} into it, and on pick mutate
 * the same tab in place into the chosen widget (so workspace-active
 * stays put — "control never left the tab").
 *
 * <p>Stateful per-workspace instance (carries singleton tracking +
 * the picker-tab counter); shares {@link WidgetMounterModule} with
 * {@link PinnedTabSpawnerModule} for the actual construct flow.</p>
 *
 * <p>Replaces V1's {@code openPickerInNewTab} +
 * {@code mutatePickerTabIntoWidget} (~140 lines inline) with substrate
 * a workspace gets for free.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 13
 */
public record PickerTabFlowModule() implements DomModule<PickerTabFlowModule> {

    public static final PickerTabFlowModule INSTANCE = new PickerTabFlowModule();

    /** The {@code PickerTabFlow} JS class. */
    public record PickerTabFlow() implements Exportable._Class<PickerTabFlowModule> {}

    @Override
    public ImportsFor<PickerTabFlowModule> imports() {
        return ImportsFor.<PickerTabFlowModule>builder()
                .add(new ModuleImports<>(
                        List.of(new WidgetMounterModule.WidgetMounter()),
                        WidgetMounterModule.INSTANCE))
                .add(new ModuleImports<>(
                        List.of(new WidgetPickerModule.WidgetPicker()),
                        WidgetPickerModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PickerTabFlowModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PickerTabFlow()));
    }
}
