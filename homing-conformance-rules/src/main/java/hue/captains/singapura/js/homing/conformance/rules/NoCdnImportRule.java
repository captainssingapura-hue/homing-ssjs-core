package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0044 — the seed rule: no JS {@code import} may target an {@code http(s)://}
 * URL. Third-party libraries must be bundled ({@code BundledExternalModule}), so
 * the served graph is CDN-free and deterministic. Scans the complete served text
 * — a CDN import anywhere is a violation, however it got there.
 */
public record NoCdnImportRule() implements JsRule {

    public static final NoCdnImportRule INSTANCE = new NoCdnImportRule();

    @Override public RuleId      id()     { return new RuleId("no-cdn-import"); }
    @Override public String      intent() { return "No JS import may target an http(s):// URL — bundle third-party modules instead."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("thin-html-typed-js"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> lines = module.lines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("import") && (line.contains("from \"http://") || line.contains("from \"https://"))) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "CDN import — bundle it instead: " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
