package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.component.ComponentTrees;
import hue.captains.singapura.js.homing.component.UiComponent;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.util.ReadContentFromResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A page's keyboard map, derived: the components its crate closure
 * catalogues say what keys they take ({@link NeedKeyboard}), and the page
 * may add shortcuts of its own, chords on the document heard by bubbling
 * when the holder leaves them. Two page shortcuts on one chord are refused.
 * A component's key shadowing a page shortcut is not an error: the holder is
 * asked first; the map shows it.
 *
 * <p>{@link #validate} holds the declared to the party: a module exporting a
 * component that declares {@code NeedKeyboard} listens to no key of its own,
 * its keys arrive by the party. {@link #undeclaredListeners} lists the served
 * modules that listen to keys without a declaration, so a gate can hold the
 * list to the components still to migrate and watch it shrink.</p>
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

    /** The map of the catalogued components of a crate closure, with no page shortcuts. */
    public static KeyboardRegistry requiredBy(List<Crate> topLevel) { return requiredBy(topLevel, List.of()); }

    /** The map of the catalogued components of a crate closure, and the page's own shortcuts. */
    public static KeyboardRegistry requiredBy(List<Crate> topLevel, List<KeyBinding> shortcuts) {
        var by = new LinkedHashMap<UiComponent<?>, List<KeyBinding>>();
        for (UiComponent<?> c : ComponentTrees.components(topLevel))
            if (c instanceof NeedKeyboard n) by.put(c, List.copyOf(n.keys()));
        return new KeyboardRegistry(by, shortcuts);
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

    /** A key listener registered in any form: {@code addEventListener("keydown", ...)}, or {@code el[f]("keydown", ...)} with the method computed. */
    private static final Pattern OWN_KEYS = Pattern.compile("[\"'](keydown|keyup|keypress)[\"']\\s*,");

    /**
     * The problems with the closure's declarations: a need on something that
     * is not a declared component, a need that names no key, and a declared
     * component whose module still listens to keys itself.
     */
    public static List<String> validate(List<Crate> topLevel) {
        var problems = new ArrayList<String>();
        for (Crate crate : ComponentTrees.closure(topLevel)) {
            for (CrateEntry e : crate.entries()) {
                EsModule<?> m = e.module();
                boolean declared = false;
                for (var x : m.exports().exports()) {
                    if (!(x instanceof NeedKeyboard n)) continue;
                    String who = crate.name() + ": " + m.getClass().getSimpleName() + "." + x.getClass().getSimpleName();
                    if (!(x instanceof UiComponent<?>) || !(m instanceof DomModule<?>)) { problems.add(who + " takes keys but is not a declared component"); continue; }
                    declared = true;
                    if (n.keys() == null || n.keys().isEmpty()) problems.add(who + " takes keys but names none");
                }
                if (declared && listensToKeys(m))
                    problems.add(crate.name() + ": " + m.getClass().getSimpleName() + " declares its keys but listens to them itself; keys come through the party");
            }
        }
        return List.copyOf(problems);
    }

    /** The served modules of the closure that listen to keys with no declaration: what is still to migrate. */
    public static List<String> undeclaredListeners(List<Crate> topLevel) {
        var out = new ArrayList<String>();
        for (Crate crate : ComponentTrees.closure(topLevel)) {
            for (CrateEntry e : crate.entries()) {
                EsModule<?> m = e.module();
                if (!listensToKeys(m)) continue;
                if (m.exports().exports().stream().noneMatch(x -> x instanceof NeedKeyboard)) out.add(m.getClass().getSimpleName());
            }
        }
        return List.copyOf(out);
    }

    /** Whether the module's own JS adds a key listener; a self-content or CSS module has none, and the steward, the party's one face to the document, is the exception by definition. */
    static boolean listensToKeys(EsModule<?> m) {
        if (m instanceof SelfContent || m instanceof CssGroup<?> || m instanceof KeyboardStewardModule) return false;
        try {
            for (String line : new ReadContentFromResources<>(m).content()) if (OWN_KEYS.matcher(line).find()) return true;
            return false;
        } catch (RuntimeException notAResource) {
            return false;
        }
    }
}
