package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
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
        // A clean consumer renders through the real server path and validates
        // with zero findings — render → classify(CONSUMER) → run → collect.
        assertEquals(StandardJsModuleType.CONSUMER, ModuleClassifier.classify(CleanModule.INSTANCE));
        List<Finding> findings = engine.check(CleanModule.INSTANCE);
        assertEquals(List.of(), findings,
                () -> "a clean consumer must render + validate clean, got: " + findings);
        // classify(EsModule) is the structural fallback — no crate declaration in view here.
        assertEquals(StandardJsModuleType.CONSUMER, ModuleClassifier.classify(HrefManager.INSTANCE));
    }

    @Test
    void assembleProducesAStructuredGradedReport() {
        // STRICT: no allowlist, no baseline — every finding is an error.
        ConformanceRun run = engine.assemble(List.of(new TestCrate()), FindingGrader.STRICT);

        assertEquals(1, run.summary().crates().size());
        CrateReport crate = run.summary().crates().get(0);
        assertEquals("test-crate", crate.crate());
        assertEquals(2, crate.moduleCount());
        assertEquals(2, run.modules().size());

        ModuleResult clean = moduleNamed(run, "CleanModule");
        ModuleResult cdn   = moduleNamed(run, "CdnModule");
        assertTrue(clean.pass(), "clean consumer passes");
        assertFalse(cdn.pass(), "a CDN import is a NEW violation under STRICT → fails");

        assertEquals(1, run.summary().errorCount(), "one CDN error across the crate");
        assertFalse(run.summary().pass());
    }

    private static ModuleResult moduleNamed(ConformanceRun run, String simpleName) {
        return run.modules().stream().filter(m -> m.moduleClass().contains(simpleName))
                .findFirst().orElseThrow();
    }

    /** A one-crate fixture packing the clean + CDN modules. */
    record TestCrate() implements Crate {
        @Override public String name() { return "test-crate"; }
        @Override public List<Crate> requires() { return List.of(); }
        @Override public List<CrateEntry> entries() {
            return List.of(CrateEntry.of(CleanModule.INSTANCE), CrateEntry.of(CdnModule.INSTANCE));
        }
    }

    /** A rule-clean consumer — renders through the real path with no violations. */
    record CleanModule() implements DomModule<CleanModule>, SelfContent {
        static final CleanModule INSTANCE = new CleanModule();

        @Override public ImportsFor<CleanModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<CleanModule> exports() { return new ExportsOf<>(INSTANCE, List.of()); }

        @Override
        public List<String> selfContent(ModuleNameResolver nameResolver) {
            return List.of("export const greeting = 'hello';",
                    "export function render(host) { return host; }");
        }
    }

    @Test
    void seededCdnImportFailsThroughTheFullEnginePath() {
        // The engine renders CdnModule's SelfContent (a CDN import) and the
        // NoCdnImportRule flags it — render → classify(CONSUMER) → run → collect.
        assertEquals(StandardJsModuleType.CONSUMER, ModuleClassifier.classify(CdnModule.INSTANCE));
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
