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
 * The focus monitor: the page's logical-focus tree as a tree view — every
 * branch, every member, the holder of the keys marked — redrawn on every
 * notice of the party and every event of the steward. It says exactly which
 * component is in focus and under whom; a holder outside the tree, a member
 * by id, is named below it. It takes no keys; the steward's own state is the
 * {@link StewardMonitorModule.StewardMonitor}'s.
 */
public record FocusMonitorModule() implements DomModule<FocusMonitorModule> {

    /** A branch component: {@code new FocusMonitor(branch, { host })}; {@code refresh()}, {@code dispose()}. */
    public record FocusMonitor() implements BranchComponent<FocusMonitorModule> {
        @Override public String summary() { return "The logical-focus tree as a tree view, the holder of the keys marked; redrawn as the party and the steward speak."; }
    }

    public static final FocusMonitorModule INSTANCE = new FocusMonitorModule();

    @Override
    public ImportsFor<FocusMonitorModule> imports() {
        return ImportsFor.<FocusMonitorModule>builder()
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeyboardStewardModule.KeyboardSteward(), new KeyboardStewardModule.KeyboardStewardInstance()), KeyboardStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FocusStyles.fm_tree(),
                        new FocusStyles.fm_row(),
                        new FocusStyles.fm_row_holder(),
                        new FocusStyles.fm_kind(),
                        new FocusStyles.fm_name(),
                        new FocusStyles.fm_component(),
                        new FocusStyles.fm_outside()
                ), FocusStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FocusMonitorModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FocusMonitor()));
    }
}
