package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 proof — the rule model is pure and testable against a synthesized
 * {@link ServedModule} (the complete served text), with no server and no
 * rendering. Exercises a real rule ({@link NoCdnImportRule}), rule-set
 * aggregation, and policy dispatch.
 */
class RuleModelTest {

    private static ServedModule served(String cls, JsModuleType type, String... lines) {
        return new ServedModule(cls, type, JsSource.of(lines));
    }

    @Test
    void ruleFlagsACdnImport() {
        var m = served("demo.Widget", JsModuleType.CONSUMER,
                "import mermaid from \"https://cdn.jsdelivr.net/npm/mermaid@11/x.mjs\";",
                "function render(){}");
        List<Finding> findings = NoCdnImportRule.INSTANCE.check(m);
        assertEquals(1, findings.size(), () -> "expected one finding, got " + findings);
        Finding f = findings.get(0);
        assertEquals("demo.Widget", f.moduleClass());
        assertEquals(new RuleId("no-cdn-import"), f.rule());
        assertEquals(0, f.line());
    }

    @Test
    void cleanModuleAndSanctionedManagerImportAreCompliant() {
        // A relative (in-app) manager import is NOT a CDN import.
        var m = served("demo.Widget", JsModuleType.CONSUMER,
                "import { HrefManagerInstance as href } from \"/module?class=…HrefManager\";",
                "function render(){ href.set(a, '/x'); }",
                "export { mountInto };");
        assertTrue(NoCdnImportRule.INSTANCE.check(m).isEmpty(),
                "relative manager import + clean body must be compliant");
    }

    @Test
    void ruleSetAggregatesFindings() {
        var set = new JsRuleSet(new RuleSetId("consumer"), "Consumer", List.of(NoCdnImportRule.INSTANCE));
        var offending = served("demo.Bad", JsModuleType.CONSUMER,
                "import a from \"http://evil/a.mjs\";",
                "import b from \"https://evil/b.mjs\";");
        assertEquals(2, set.checkAll(offending).size());
        assertTrue(set.checkAll(served("demo.Ok", JsModuleType.CONSUMER, "const x = 1;")).isEmpty());
    }

    @Test
    void policyDispatchesByModuleType() {
        var consumerSet = new JsRuleSet(new RuleSetId("consumer"), "Consumer", List.of(NoCdnImportRule.INSTANCE));
        var exempt      = JsRuleSet.empty(new RuleSetId("bundled-external"), "Bundled external");
        JsRulePolicy policy = type -> type == JsModuleType.BUNDLED_EXTERNAL ? exempt : consumerSet;

        assertSame(consumerSet, policy.rulesFor(JsModuleType.CONSUMER));
        // A bundle is exempt — its CDN-looking imports are not policed.
        var bundle = served("vendor.Three", JsModuleType.BUNDLED_EXTERNAL,
                "import x from \"https://unpkg.com/three\";");
        assertTrue(policy.rulesFor(bundle.type()).checkAll(bundle).isEmpty());
        assertFalse(policy.rulesFor(JsModuleType.CONSUMER).checkAll(bundle).isEmpty());
    }

    @Test
    void servedModuleCarriesTheCompleteText() {
        var m = ServedModule.of("demo.X", JsModuleType.CONSUMER, "line1\nline2\nline3");
        assertEquals("line1\nline2\nline3", m.text());
        assertEquals(3, m.lines().size());
        assertSame(JsSource.EMPTY, new ServedModule("demo.Y", JsModuleType.CONSUMER, null).content());
    }
}
