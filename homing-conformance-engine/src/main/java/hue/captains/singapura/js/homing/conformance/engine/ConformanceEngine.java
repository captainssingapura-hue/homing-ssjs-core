package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.JsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;

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
}
