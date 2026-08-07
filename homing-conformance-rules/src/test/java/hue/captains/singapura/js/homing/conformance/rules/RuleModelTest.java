package hue.captains.singapura.js.homing.conformance.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 proof — the rule model is pure and testable against a synthesized
 * {@link ServedModule}, with no server and no rendering. Exercises a real rule
 * ({@link NoCdnImportRule}), rule-set aggregation, and policy dispatch.
 */
class RuleModelTest {

    /** A served module with the given body lines (empty prologue/epilogue). */
    private static ServedModule bodyModule(String cls, JsModuleType type, String... bodyLines) {
        return new ServedModule(cls, type, JsSource.EMPTY, JsSource.of(bodyLines), JsSource.EMPTY);
    }

    @Test
    void ruleFlagsACdnImportInTheBody() {
        var m = bodyModule("demo.Widget", JsModuleType.CONSUMER,
                "import mermaid from \"https://cdn.jsdelivr.net/npm/mermaid@11/x.mjs\";",
                "function render(){}");
        List<Finding> findings = NoCdnImportRule.INSTANCE.check(m);
        assertEquals(1, findings.size(), () -> "expected one finding, got " + findings);
        Finding f = findings.get(0);
        assertEquals("demo.Widget", f.moduleClass());
        assertEquals(new RuleId("no-cdn-import"), f.rule());
        assertEquals(JsRegion.WHOLE, f.region());
        assertEquals(0, f.line());
    }

    @Test
    void cleanModuleAndSanctionedManagerImportAreCompliant() {
        // A relative manager import in the (generated) prologue is NOT a CDN import.
        var m = new ServedModule("demo.Widget", JsModuleType.CONSUMER,
                JsSource.of("import { HrefManagerInstance as href } from \"/module?class=…HrefManager\";"),
                JsSource.of("function render(){ href.set(a, '/x'); }"),
                JsSource.of("export { mountInto };"));
        assertTrue(NoCdnImportRule.INSTANCE.check(m).isEmpty(),
                "relative manager import + clean body must be compliant");
    }

    @Test
    void ruleSetAggregatesFindings() {
        var set = new JsRuleSet(new RuleSetId("consumer"), "Consumer", List.of(NoCdnImportRule.INSTANCE));
        var offending = bodyModule("demo.Bad", JsModuleType.CONSUMER,
                "import a from \"http://evil/a.mjs\";",
                "import b from \"https://evil/b.mjs\";");
        assertEquals(2, set.checkAll(offending).size());
        assertTrue(set.checkAll(bodyModule("demo.Ok", JsModuleType.CONSUMER, "const x = 1;")).isEmpty());
    }

    @Test
    void policyDispatchesByModuleType() {
        var consumerSet = new JsRuleSet(new RuleSetId("consumer"), "Consumer", List.of(NoCdnImportRule.INSTANCE));
        var exempt      = JsRuleSet.empty(new RuleSetId("bundled-external"), "Bundled external");
        JsRulePolicy policy = type -> type == JsModuleType.BUNDLED_EXTERNAL ? exempt : consumerSet;

        assertSame(consumerSet, policy.rulesFor(JsModuleType.CONSUMER));
        // A bundle is exempt — its CDN-looking imports are not policed.
        var bundle = bodyModule("vendor.Three", JsModuleType.BUNDLED_EXTERNAL,
                "import x from \"https://unpkg.com/three\";");
        assertTrue(policy.rulesFor(bundle.type()).checkAll(bundle).isEmpty());
        assertFalse(policy.rulesFor(JsModuleType.CONSUMER).checkAll(bundle).isEmpty());
    }

    @Test
    void servedConcatenatesSegmentsInOrder() {
        var m = new ServedModule("demo.X", JsModuleType.CONSUMER,
                JsSource.of("PROLOGUE"), JsSource.of("BODY"), JsSource.of("EPILOGUE"));
        assertEquals("PROLOGUE\nBODY\nEPILOGUE", m.served());
        // null segments default to the empty source.
        var defaulted = new ServedModule("demo.Y", JsModuleType.CONSUMER, null, JsSource.of("b"), null);
        assertSame(JsSource.EMPTY, defaulted.prologue());
        assertSame(JsSource.EMPTY, defaulted.epilogue());
    }
}
