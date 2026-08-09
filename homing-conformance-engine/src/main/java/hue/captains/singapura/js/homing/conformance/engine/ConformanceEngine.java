package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.JsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.JsRuleSet;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.rules.report.CrateReport;
import hue.captains.singapura.js.homing.conformance.rules.report.FindingReport;
import hue.captains.singapura.js.homing.conformance.rules.report.ModuleResult;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.JsModuleType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * RFC 0044 Phase 6 — the conformance engine: for each module, <b>render</b> the
 * complete served artifact, <b>classify</b> it, select its rule set via the
 * <b>policy</b>, <b>run</b> the rules, and <b>collect</b> the findings. Any
 * finding is a violation (the build-fail gate asserts the result is empty).
 *
 * <p>Enumeration is the Crate model: {@link #checkCrates} runs over the modules
 * of a crate set (typically a top-level crate's closure).</p>
 */
public final class ConformanceEngine {

    private final JsRulePolicy policy;
    private final ServedModuleRenderer renderer;

    /** The framework default: the fixed policy + the real server render path. */
    public ConformanceEngine() {
        this(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer());
    }

    public ConformanceEngine(JsRulePolicy policy, ServedModuleRenderer renderer) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    /** Findings for one module: render → classify → policy.rulesFor(type) → run. */
    public List<Finding> check(EsModule<?> module) {
        ServedModule served = renderer.render(module);
        return policy.rulesFor(served.type()).checkAll(served);
    }

    /** Findings across a set of modules. */
    public List<Finding> checkAll(Collection<? extends EsModule<?>> modules) {
        var all = new ArrayList<Finding>();
        for (EsModule<?> module : modules) all.addAll(check(module));
        return List.copyOf(all);
    }

    /**
     * Findings across the modules of the given crates — the enumeration path.
     * Each entry is classified with its <b>declared</b> domain role honoured
     * (falling back to structural inference), so the policy applies the right
     * rule set — a module marked {@code PURE_LOGIC} is held to no-DOM, not the
     * consumer default.
     */
    public List<Finding> checkCrates(Collection<? extends Crate> crates) {
        var all = new ArrayList<Finding>();
        for (Crate c : crates) {
            for (CrateEntry e : c.entries()) {
                var type = ModuleClassifier.classify(e);
                ServedModule served = renderer.render(e.module(), type);
                all.addAll(policy.rulesFor(type).checkAll(served));
            }
        }
        return List.copyOf(all);
    }

    /**
     * RFC 0044 Phase 8 — run the crates and <b>assemble the exportable report</b>:
     * per module, render → classify → run → {@code grader.grade}, producing a
     * {@link ModuleResult} (with graded findings); per crate, roll up into a
     * {@link CrateReport}; overall, the {@link ConformanceReport} summary. This is
     * {@link #checkCrates} enriched with grading + structure — the shape the
     * writer serializes and the studio renders.
     */
    public ConformanceRun assemble(Collection<? extends Crate> crates, FindingGrader grader) {
        var modules = new ArrayList<ModuleResult>();
        var crateReports = new ArrayList<CrateReport>();
        int totErr = 0, totWarn = 0, totModules = 0;

        for (Crate c : crates) {
            var memberIds = new ArrayList<String>();
            int cErr = 0, cWarn = 0;
            for (CrateEntry e : c.entries()) {
                JsModuleType type = ModuleClassifier.classify(e);
                ServedModule served = renderer.render(e.module(), type);
                JsRuleSet set = policy.rulesFor(type);

                var findings = new ArrayList<FindingReport>();
                int mErr = 0;
                for (GradedFinding g : grader.grade(set.checkAll(served))) {
                    findings.add(FindingReport.of(g));
                    if (g.isError()) mErr++; else cWarn++;
                }
                cErr += mErr;
                modules.add(new ModuleResult(e.moduleClass(), type, set.id().value(), mErr == 0, findings));
                memberIds.add(e.moduleClass());
            }
            var requires = c.requires().stream().map(Crate::name).toList();
            crateReports.add(new CrateReport(c.name(), requires, memberIds,
                    memberIds.size(), cErr, cWarn, cErr == 0));
            totErr += cErr;
            totWarn += cWarn;
            totModules += memberIds.size();
        }

        var summary = new ConformanceReport(ConformanceReport.SCHEMA_VERSION,
                grader.allowPreExisting(), grader.baseline().size(),
                totModules, totErr, totWarn, crateReports);
        return new ConformanceRun(summary, modules);
    }
}
