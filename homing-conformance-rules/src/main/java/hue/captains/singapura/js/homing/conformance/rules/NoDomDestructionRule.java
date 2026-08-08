package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0044 Phase 7 — ported from {@code NoDomDestructionConformanceTest}:
 * branch-owned DOM must never be detached via a <b>wholesale wipe</b>. Flags
 * {@code .innerHTML = ""}, {@code .textContent = ""}, and zero-arg
 * {@code .replaceChildren()} in the served text (comments stripped first). A
 * module with a legitimate wipe (e.g. {@code Modal.setContent}) is allowlisted
 * with a justification rather than silencing the rule.
 */
public record NoDomDestructionRule() implements JsRule {

    public static final NoDomDestructionRule INSTANCE = new NoDomDestructionRule();

    private static final List<Pattern> WIPES = List.of(
            Pattern.compile("\\.innerHTML\\s*=\\s*([\"'`])\\s*\\1"),
            Pattern.compile("\\.textContent\\s*=\\s*([\"'`])\\s*\\1"),
            Pattern.compile("\\.replaceChildren\\s*\\(\\s*\\)"));

    @Override public RuleId      id()     { return new RuleId("no-dom-destruction"); }
    @Override public String      intent() { return "Branch-owned DOM must never be detached via a wholesale wipe (innerHTML=\"\", textContent=\"\", replaceChildren())."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("workspace-is-the-substrate"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            for (Pattern p : WIPES) {
                if (p.matcher(stripped.get(i)).find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "wholesale DOM wipe: " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
