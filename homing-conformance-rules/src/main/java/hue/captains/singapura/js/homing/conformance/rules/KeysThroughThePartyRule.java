package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Keys come through the party, and only the steward listens to the document.
 * A page has one keyboard party and one steward, and one holder of the keys
 * or none: the steward listens to {@code keydown} and {@code keyup} on the
 * document — always, in the bubble phase — and routes by state: a key with
 * nothing focused goes to the holder's {@code keyDown(ev)}; a key from a
 * focused element goes nowhere, the native world had it. So no served module
 * but the steward registers a key listener on the document, on the window,
 * or in the capture phase: a second listener there is a second responder,
 * which is the thing the party exists to rule out.
 *
 * <p>A component's key listener on its own element is allowed: that is the
 * native world — a slider's arrows on its knob, a panel hearing Enter from
 * the select inside it — and such a listener can never hear a party key,
 * since a key with nothing focused has the body as its target and passes
 * through no component's root.</p>
 *
 * <p>Read from the served text: a registration of a {@code keydown},
 * {@code keyup} or {@code keypress} listener on {@code document} or
 * {@code window}, or with the capture flag — {@code document[f]("keydown", …)},
 * {@code window.addEventListener("keyup", …)}, {@code el.addEventListener("keydown", f, true)}.
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
    @Override public String      intent() { return "No module but the keyboard steward registers a keydown, keyup or keypress listener on the document, on the window, or in the capture phase; a component listens for keys only on its own elements, and party keys come through the steward to the holder's keyDown(ev)."; }
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
            if (!CAPTURES.matcher(line).find()) continue;                                            // a component's own element: the native world's, allowed
            findings.add(new Finding(module.moduleClass(), id(), "captures keys on the document — only the keyboard steward does: " + raw.get(i).trim(), i));
        }
        return List.copyOf(findings);
    }
}
