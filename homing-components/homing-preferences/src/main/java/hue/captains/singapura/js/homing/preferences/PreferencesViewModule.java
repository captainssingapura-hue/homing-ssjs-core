package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.WidgetSlotModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.ServingContextModule;

import java.util.List;

/**
 * The preferences view: a master slot on the left holding the tree widget,
 * a detail slot on the right holding one widget per node, every widget
 * loaded the first time it is needed and kept until the view is disposed.
 *
 * <p>{@code new PreferencesView(branch, host, registry)}: select, ready, dispose. A branch component.
 * The registry is a site's {@link PreferencesRegistry}; the view reads it
 * and holds what it constructs. It knows the master by the surface a tree
 * widget offers it — {@code onSelect(fn)}, {@code select(path)} — and knows
 * a detail widget by nothing at all: it constructs it with the registry's
 * params and shows its root. What a widget writes, it writes through the
 * steward; the view neither carries nor forwards a value.</p>
 */
public record PreferencesViewModule() implements DomModule<PreferencesViewModule> {

    /** The class. */
    public record PreferencesView() implements Exportable._Constant<PreferencesViewModule> {}

    public static final PreferencesViewModule INSTANCE = new PreferencesViewModule();

    @Override
    public ImportsFor<PreferencesViewModule> imports() {
        return ImportsFor.<PreferencesViewModule>builder()
                .add(new ModuleImports<>(List.of(new WidgetSlotModule.WidgetSlot()), WidgetSlotModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ServingContextModule.withServingContext()), ServingContextModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new PreferencesStyles.pv_root(),
                        new PreferencesStyles.pv_master(),
                        new PreferencesStyles.pv_detail()
                ), PreferencesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PreferencesViewModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new PreferencesView()));
    }
}
