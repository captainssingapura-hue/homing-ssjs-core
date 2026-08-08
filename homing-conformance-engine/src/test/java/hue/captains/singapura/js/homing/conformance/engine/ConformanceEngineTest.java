package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.server.CssClassManager;
import hue.captains.singapura.js.homing.server.HrefManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 6 proof — the engine renders a module through the real server path,
 * classifies it, runs the policy's rules, and collects findings. Green over a
 * real compliant module; red over a module whose served artifact carries a CDN
 * import.
 */
class ConformanceEngineTest {

    private final ConformanceEngine engine = new ConformanceEngine();

    @Test
    void realModuleRendersAndPassesEndToEnd() {
        List<Finding> findings = engine.check(CssClassManager.INSTANCE);
        assertEquals(List.of(), findings,
                () -> "CssClassManager renders + validates clean, got: " + findings);
        assertEquals(JsModuleType.CONSUMER, ModuleClassifier.classify(HrefManager.INSTANCE));
    }

    @Test
    void seededCdnImportFailsThroughTheFullEnginePath() {
        // The engine renders CdnModule's SelfContent (a CDN import) and the
        // NoCdnImportRule flags it — render → classify(CONSUMER) → run → collect.
        assertEquals(JsModuleType.CONSUMER, ModuleClassifier.classify(CdnModule.INSTANCE));
        List<Finding> findings = engine.check(CdnModule.INSTANCE);
        assertFalse(findings.isEmpty(), "a served CDN import must be flagged by the engine");
        assertTrue(findings.stream().anyMatch(f -> f.rule().value().equals("no-cdn-import")),
                () -> "expected a no-cdn-import finding, got: " + findings);
    }

    /** A test module whose served artifact contains a CDN import. */
    record CdnModule() implements DomModule<CdnModule>, SelfContent {
        static final CdnModule INSTANCE = new CdnModule();

        @Override public ImportsFor<CdnModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<CdnModule> exports() { return new ExportsOf<>(INSTANCE, List.of()); }

        @Override
        public List<String> selfContent(ModuleNameResolver nameResolver) {
            return List.of("import evil from \"https://cdn.example.com/evil.mjs\";",
                    "export const y = 1;");
        }
    }
}
