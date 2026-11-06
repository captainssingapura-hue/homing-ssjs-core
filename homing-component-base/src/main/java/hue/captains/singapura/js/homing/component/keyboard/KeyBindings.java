package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Arrays;
import java.util.List;

/** Key bindings made in numbers ({@link KeyBinding}). */
public record KeyBindings() implements StatelessFunctionalObject {

    public static final KeyBindings INSTANCE = new KeyBindings();

    /** Several keys, one meaning: {@code ArrowRight} and {@code ArrowUp} both a step up. */
    public List<KeyBinding> each(String meaning, Key... keys) {
        return Arrays.stream(keys).map(k -> new KeyBinding(k, meaning)).toList();
    }
}
