package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.icons.IconModule;

import java.util.List;

/**
 * The slider and its builder: a number set by a knob on a track, every part
 * a real element wearing a design word — the track sunk, the fill from the
 * detent to the value, the knob raised and focusable, with a mark on it: a
 * grip, or the word for what the slider sets — so a design draws the whole
 * of it and the browser's own slider is nowhere in it.
 */
public record SliderModule() implements DomModule<SliderModule> {

    /** A branch component, made through its builder: a label, the rail with its detent, a readout; value, setOn, size, label, dispose. */
    public record Slider() implements BranchComponent<SliderModule> {
        @Override public String summary() { return "A number set by a knob on a track: a label, the rail with its detent, a readout; the hand and the keys alike."; }
    }
    /** The builder: {@code new SliderBuilder()}; label, range or axis, value, detent, format, onInput, onChange, size set progressively; {@code build(branch)} on a sub-branch of the caller's. */
    public record SliderBuilder() implements Exportable._Constant<SliderModule> {}

    public static final SliderModule INSTANCE = new SliderModule();

    @Override
    public ImportsFor<SliderModule> imports() {
        return ImportsFor.<SliderModule>builder()
                .add(new ModuleImports<>(List.of(
                        new ElementStyles.el_slider(),
                        new ElementStyles.el_slider_label(),
                        new ElementStyles.el_slider_rail(),
                        new ElementStyles.el_slider_track(),
                        new ElementStyles.el_slider_fill(),
                        new ElementStyles.el_slider_detent(),
                        new ElementStyles.el_slider_knob(),
                        new ElementStyles.el_slider_face(),
                        new ElementStyles.el_slider_held(),
                        new ElementStyles.el_slider_readout(),
                        new ElementStyles.el_slider_off()
                ), ElementStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SliderModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Slider(), new SliderBuilder()));
    }
}
