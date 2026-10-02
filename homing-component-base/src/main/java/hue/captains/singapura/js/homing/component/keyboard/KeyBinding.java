package hue.captains.singapura.js.homing.component.keyboard;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * One key a component takes, with its meaning: a line for a person, and a
 * fact for a studio and the collision check. {@code Shift+ArrowUp: ten steps up}.
 *
 * @param key       the key
 * @param modifiers the modifiers held with it; none for a plain key
 * @param meaning   what the component does with it
 */
public record KeyBinding(Key key, Set<Modifier> modifiers, String meaning) {

    public KeyBinding {
        Objects.requireNonNull(key, "KeyBinding.key");
        modifiers = modifiers == null || modifiers.isEmpty() ? Set.of() : Set.copyOf(modifiers);
        Objects.requireNonNull(meaning, "KeyBinding.meaning");
        if (meaning.isBlank()) throw new IllegalArgumentException("KeyBinding.meaning: must not be blank");
    }

    public static KeyBinding of(Key key, String meaning) { return new KeyBinding(key, Set.of(), meaning); }
    public static KeyBinding of(Key key, Modifier modifier, String meaning) { return new KeyBinding(key, EnumSet.of(modifier), meaning); }
    public static KeyBinding of(Key key, Set<Modifier> modifiers, String meaning) { return new KeyBinding(key, modifiers, meaning); }

    /** Several keys, one meaning: {@code ArrowRight} and {@code ArrowUp} both a step up. */
    public static List<KeyBinding> each(String meaning, Key... keys) {
        return Arrays.stream(keys).map(k -> of(k, meaning)).toList();
    }

    /** The chord, as a person reads it: {@code Shift+ArrowUp}; modifiers in a fixed order. */
    public String chord() {
        var mods = List.of(Modifier.CTRL, Modifier.ALT, Modifier.SHIFT, Modifier.META).stream()
            .filter(modifiers::contains).map(Modifier::label).collect(Collectors.joining("+"));
        return mods.isEmpty() ? key.label() : mods + "+" + key.label();
    }
}
