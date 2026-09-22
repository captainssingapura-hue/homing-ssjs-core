package hue.captains.singapura.js.homing.ui.focus;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The steward monitor: the keyboard steward's activeness on view, as one
 * lamp — active, the keys are the holder's, active and offering them while a
 * walk rests on a member, or dormant on what has the native focus, whose keys
 * are its own. Redrawn on every event of the steward and
 * every move of the native focus. It takes no keys.
 */
public record StewardMonitorModule() implements DomModule<StewardMonitorModule> {

    /** A branch component: {@code new StewardMonitor(branch, { host })}; {@code lamp()}, {@code refresh()}, {@code dispose()}. */
    public record StewardMonitor() implements BranchComponent<StewardMonitorModule> {
        @Override public String summary() { return "The keyboard steward's activeness as one lamp: active, the keys are the holder's, or dormant on what has the native focus."; }
    }

    public static final StewardMonitorModule INSTANCE = new StewardMonitorModule();

    @Override
    public ImportsFor<StewardMonitorModule> imports() {
        return ImportsFor.<StewardMonitorModule>builder()
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardStewardModule.KeyboardSteward(), new KeyboardStewardModule.KeyboardStewardInstance()), KeyboardStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FocusStyles.sm_lamp(),
                        new FocusStyles.sm_lamp_dormant()
                ), FocusStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StewardMonitorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new StewardMonitor()));
    }
}
