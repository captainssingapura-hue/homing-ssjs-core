package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keys come through the party. A page has one keyboard party and one
 * steward, and one holder of the keys or none: the steward captures
 * {@code keydown} and {@code keyup} on the document while someone holds and
 * asks the holder first, by its {@code key(ev)}; a key the holder takes stops
 * there, a key it leaves travels on as it would. So no served module
 * registers a key listener of its own — not on its element, not on the
 * document, not in the capture phase: a component that takes keys is a
 * member, or is handed them by the member that holds it. Two listeners on
 * one key are two components responding, which is the thing the party
 * exists to rule out.
 *
 * <p>Read from the served text: any registration of a {@code keydown},
 * {@code keyup} or {@code keypress} listener, in whatever form —
 * {@code el.addEventListener("keydown", …)}, {@code document[f]("keydown", …)},
 * {@code el.onkeydown = …}. The message says which of the two it is, since
 * they are paid differently: a document or capture-phase listener is the
 * steward's job, an element's own listener is a {@code key(ev)} to write.
 * One exemption, by name: the steward, the party's one face to the document.</p>
 */
public record KeysThroughThePartyRule() implements JsRule {

    public static final KeysThroughThePartyRule INSTANCE = new KeysThroughThePartyRule();

    /** The keyboard steward: the one module that listens to the document, by definition. */
    public static final Set<String> STEWARDS = Set.of("hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule");

    /** A key listener registered in any form: the event name as a string argument, or the {@code onkey…} property assigned. */
    private static final Pattern LISTENER = Pattern.compile("[\"'](?:keydown|keyup|keypress)[\"']\\s*,|\\.onkey(?:down|up|press)\\s*=[^=]");

    /** On the document or the window, or with the capture flag: the steward's alone. */
    private static final Pattern CAPTURES = Pattern.compile("\\b(?:document|window)\\s*[.\\[]|,\\s*true\\s*\\)");

    @Override public RuleId      id()     { return new RuleId("keys-through-the-party"); }
    @Override public String      intent() { return "A module registers no keydown, keyup or keypress listener; keys come through the keyboard party, to the holder's key(ev), and only the steward listens to the document."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("keyboard-party"); }

    @Override
    public List<Finding> check(ServedModule module) {
        if (STEWARDS.contains(module.moduleClass())) return List.of();
        List<String> raw = module.lines();
        List<String> code = JsText.stripComments(raw);
        var findings = new ArrayList<Finding>();
        for (int i = 0; i < code.size(); i++) {
            String line = code.get(i);
            if (!LISTENER.matcher(line).find() || line.contains("removeEventListener")) continue;   // the removal is the pair of an addition already found
            String what = CAPTURES.matcher(line).find()
                    ? "captures keys on the document — only the keyboard steward does: "
                    : "listens for keys itself — keys come through the party, to key(ev): ";
            findings.add(new Finding(module.moduleClass(), id(), what + raw.get(i).trim(), i));
        }
        return List.copyOf(findings);
    }
}
