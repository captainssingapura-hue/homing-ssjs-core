package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.component.ComponentTrees;
import hue.captains.singapura.js.homing.component.UiComponent;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.MadeByDesign;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.util.ReadContentFromResources;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/** What a tree of crates asks of the keyboard, and whether it keeps to it ({@link KeyboardRegistry}). */
public record CrateKeys() implements StatelessFunctionalObject {

    public static final CrateKeys INSTANCE = new CrateKeys();

    /** A key listener registered in any form: {@code addEventListener("keydown", ...)}, or {@code el[f]("keydown", ...)} with the method computed. */
    private static final Pattern OWN_KEYS = Pattern.compile("[\"'](keydown|keyup|keypress)[\"']\\s*,");

    /** A key listener on the document or the window, or in the capture phase: the steward's alone. */
    private static final Pattern CAPTURES = Pattern.compile("(document|window)\\s*[.\\[]|,\\s*true\\s*\\)");

    /** The map of the catalogued components of a crate closure, with no page shortcuts. */
    public KeyboardRegistry requiredBy(List<Crate> topLevel) { return requiredBy(topLevel, List.of()); }

    /** The map of the catalogued components of a crate closure, and the page's own shortcuts. */
    public KeyboardRegistry requiredBy(List<Crate> topLevel, List<KeyBinding> shortcuts) {
        var by = new LinkedHashMap<UiComponent<?>, List<KeyBinding>>();
        for (UiComponent<?> c : ComponentTrees.INSTANCE.components(topLevel))
            if (c instanceof NeedKeyboard n) by.put(c, List.copyOf(n.keys()));
        return new KeyboardRegistry(by, shortcuts);
    }

    /**
     * The problems with the closure's declarations: a need on something that
     * is not a declared component, a need that names no key, and any module
     * but the steward that captures keys on the document or the window or in
     * the capture phase. A component's listener on its own element is allowed.
     */
    public List<String> validate(List<Crate> topLevel) {
        var problems = new ArrayList<String>();
        for (Crate crate : ComponentTrees.INSTANCE.closure(topLevel)) {
            for (CrateEntry e : crate.entries()) {
                EsModule<?> m = e.module();
                for (var x : m.exports().exports()) {
                    if (!(x instanceof NeedKeyboard n)) continue;
                    String who = crate.name() + ": " + m.getClass().getSimpleName() + "." + x.getClass().getSimpleName();
                    if (!(x instanceof UiComponent<?>) || !(m instanceof DomModule<?>)) { problems.add(who + " takes keys but is not a declared component"); continue; }
                    if (n.keys() == null || n.keys().isEmpty()) problems.add(who + " takes keys but names none");
                }
                var lines = keyListenerLines(m);
                if (lines.stream().anyMatch(line -> CAPTURES.matcher(line).find()))
                    problems.add(crate.name() + ": " + m.getClass().getSimpleName() + " captures keys on the document; only the steward does");
            }
        }
        return List.copyOf(problems);
    }

    /** The served modules of the closure that listen to keys with no declaration: what is still to migrate. */
    public List<String> undeclaredListeners(List<Crate> topLevel) {
        var out = new ArrayList<String>();
        for (Crate crate : ComponentTrees.INSTANCE.closure(topLevel)) {
            for (CrateEntry e : crate.entries()) {
                EsModule<?> m = e.module();
                if (!listensToKeys(m)) continue;
                if (m.exports().exports().stream().noneMatch(x -> x instanceof NeedKeyboard)) out.add(m.getClass().getSimpleName());
            }
        }
        return List.copyOf(out);
    }

    /** Whether the module's own JS adds a key listener; a self-content, design-made or CSS module has none, and the steward, the party's one face to the document, is the exception by definition. */
    boolean listensToKeys(EsModule<?> m) { return !keyListenerLines(m).isEmpty(); }

    /** The lines of the module's own JS that register a key listener, in any form; none for the steward, a self-content, design-made or CSS module. */
    List<String> keyListenerLines(EsModule<?> m) {
        if (m instanceof SelfContent || m instanceof MadeByDesign || m instanceof CssGroup<?> || m instanceof DerivedCss<?> || m instanceof KeyboardStewardModule) return List.of();
        try {
            var out = new ArrayList<String>();
            for (String line : new ReadContentFromResources<>(m).content()) if (OWN_KEYS.matcher(line).find()) out.add(line);
            return List.copyOf(out);
        } catch (RuntimeException notAResource) {
            return List.of();
        }
    }
}
