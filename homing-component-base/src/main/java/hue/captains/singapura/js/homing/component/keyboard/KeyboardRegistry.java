package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.component.UiComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A page's keyboard map, derived: the components its crate closure
 * catalogues say what keys they take ({@link NeedKeyboard}), and the page
 * may add shortcuts of its own, chords on the document heard by bubbling
 * when the holder leaves them. Two page shortcuts on one chord are refused.
 * A component's key shadowing a page shortcut is not an error: the holder is
 * asked first; the map shows it.
 *
 * <p>{@link #validate} holds the declared to the party: a need is on a
 * declared component and names its keys, and no module but the steward
 * listens to keys on the document, on the window or in the capture phase. A
 * component's own listener on its own element is the native world's — a
 * slider's knob, a panel hearing its select — and allowed, declared or not;
 * {@link #undeclaredListeners} lists the served modules that listen to keys
 * without a declaration, so a gate can ask that a module which listens says
 * what for, and the page's map is whole.</p>
 */
public record KeyboardRegistry(Map<UiComponent<?>, List<KeyBinding>> byComponent, List<KeyBinding> shortcuts) {

    public KeyboardRegistry {
        byComponent = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(byComponent, "byComponent")));
        shortcuts = List.copyOf(Objects.requireNonNull(shortcuts, "shortcuts"));
        var chords = new HashMap<String, KeyBinding>();
        for (KeyBinding b : shortcuts) {
            var before = chords.putIfAbsent(b.chord(), b);
            if (before != null) throw new IllegalArgumentException("KeyboardRegistry: two page shortcuts on " + b.chord()
                + ": \"" + before.meaning() + "\" and \"" + b.meaning() + "\"");
        }
    }

    /** The components that take a chord. */
    public List<UiComponent<?>> takersOf(String chord) {
        var out = new ArrayList<UiComponent<?>>();
        byComponent.forEach((c, keys) -> { if (keys.stream().anyMatch(k -> k.chord().equals(chord))) out.add(c); });
        return List.copyOf(out);
    }

    /** The page shortcuts a component's keys shadow while it holds. */
    public List<KeyBinding> shadowedBy(UiComponent<?> component) {
        var keys = byComponent.getOrDefault(component, List.of());
        return shortcuts.stream().filter(s -> keys.stream().anyMatch(k -> k.chord().equals(s.chord()))).toList();
    }

    /** The chords the closure's components take, each with its takers, for a studio's listing. */
    public Map<String, List<UiComponent<?>>> byChord() {
        var out = new LinkedHashMap<String, List<UiComponent<?>>>();
        byComponent.forEach((c, keys) -> keys.forEach(k -> out.computeIfAbsent(k.chord(), x -> new ArrayList<>()).add(c)));
        out.replaceAll((k, v) -> List.copyOf(v));
        return Map.copyOf(out);
    }
}
