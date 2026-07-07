package hue.captains.singapura.js.homing.color.studio;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Secretary for the Color Studio's {@code colornav} Party — the colour
 * navigation bus. A pure {@code (state, envelope) -> Step} redirect: an incoming
 * {@code NodeSelected} (from the tree) is rebroadcast to every member as
 * {@code NavigateTo}. The {@code ColorListWidget} reacts by listing the selected
 * group's colours. Names no consumer — the tree never knows who reacts.
 */
public record ColorNavSecretaryModule() implements DomModule<ColorNavSecretaryModule> {

    /** The single export — a JS object with {@code initial} and {@code behavior}. */
    public record ColorNavSecretary() implements Exportable._Constant<ColorNavSecretaryModule> {}

    public static final ColorNavSecretaryModule INSTANCE = new ColorNavSecretaryModule();

    @Override
    public ImportsFor<ColorNavSecretaryModule> imports() {
        return ImportsFor.<ColorNavSecretaryModule>builder().build();
    }

    @Override
    public ExportsOf<ColorNavSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ColorNavSecretary()));
    }
}
