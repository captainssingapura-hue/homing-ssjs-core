package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.PaletteProvision;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/** Core's gate: its crates on the new stack, by {@link CoreConformance}, every module of their closure. */
class CoreSelfConformanceTest extends SelfGate {

    @Override Collection<Crate> closure() { return CoreConformance.closure(); }
    @Override FindingGrader grader() { return CoreConformance.grader(ALLOW_PRE_EXISTING); }
    @Override List<PaletteProvision<?, ?>> provisions() { return CoreConformance.provisions(); }
    @Override Predicate<String> owns() { return m -> true; }
    @Override String baselineFile() { return "core-conformance-baseline.txt"; }
}
