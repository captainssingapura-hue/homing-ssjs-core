package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * No JS import may target an {@code http(s)://} URL — every third-party module
 * is bundled (the CDN-free discipline). Reads the {@link JsRegion#WHOLE} served
 * text, since an import can appear in the authored body or a generated header.
 *
 * <p>The first concrete rule — it proves the model end-to-end (a pure check over
 * a {@link ServedModule}) and seeds the Phase 5 port of {@code
 * CdnFreeConformanceTest}. Functional Object: one {@code INSTANCE}.</p>
 */
public record NoCdnImportRule() implements JsRule {

    public static final NoCdnImportRule INSTANCE = new NoCdnImportRule();

    @Override public RuleId      id()     { return new RuleId("no-cdn-import"); }
    @Override public String      intent() { return "No JS import may target an http(s):// URL — bundle third-party modules instead."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("thin-html-typed-js"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> lines = module.segment(JsRegion.WHOLE).lines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("import") && (line.contains("from \"http://") || line.contains("from \"https://"))) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "CDN import — bundle it instead: " + line.trim(), JsRegion.WHOLE, i));
            }
        }
        return List.copyOf(findings);
    }
}
