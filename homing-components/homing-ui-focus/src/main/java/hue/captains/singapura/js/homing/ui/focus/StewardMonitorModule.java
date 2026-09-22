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
 * The steward monitor: the keyboard steward's activeness on view — a lamp,
 * active or dormant on what has the native focus, and the last keys the
 * steward saw with what it did with each: to the holder, taken or left;
 * native; to no one; a Tab to the member it went to. Redrawn on every key
 * the steward traces and every move of the native focus. It takes no keys.
 */
public record StewardMonitorModule() implements DomModule<StewardMonitorModule> {

    /** A branch component: {@code new StewardMonitor(branch, { host, keep? })}; {@code lamp()}, {@code lines()}, {@code refresh()}, {@code dispose()}. */
    public record StewardMonitor() implements BranchComponent<StewardMonitorModule> {
        @Override public String summary() { return "The keyboard steward's activeness: a lamp, active or dormant on what has the native focus, and the last keys with where each went."; }
    }

    public static final StewardMonitorModule INSTANCE = new StewardMonitorModule();

    @Override
    public ImportsFor<StewardMonitorModule> imports() {
        return ImportsFor.<StewardMonitorModule>builder()
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardStewardModule.KeyboardSteward(), new KeyboardStewardModule.KeyboardStewardInstance()), KeyboardStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FocusStyles.sm_monitor(),
                        new FocusStyles.sm_lamp(),
                        new FocusStyles.sm_lamp_dormant(),
                        new FocusStyles.sm_keys(),
                        new FocusStyles.sm_key(),
                        new FocusStyles.sm_key_name(),
                        new FocusStyles.sm_key_route()
                ), FocusStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StewardMonitorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new StewardMonitor()));
    }
}
