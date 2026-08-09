package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.NoDomDestructionRule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneDragModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.MultiTabPaneModule;
import hue.captains.singapura.js.homing.studio.base.ui.layout.SplitPaneModule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0044 Phase 9 — <b>parity</b> for the retired {@code
 * StudioBaseNoDomDestructionConformanceTest}: the engine's {@link
 * NoDomDestructionRule}, run over the same four studio-base layout primitives
 * through the real render path, reproduces the old scanner's verdicts exactly —
 * SplitPane / MultiTabPane / MultiTabPaneDrag are clean, and only Modal carries
 * the wholesale wipe ({@code this._body.innerHTML = ""}, allowlisted in the gate
 * via {@link HomingConformance}). This regression guard is what lets the old
 * scanner + its base retire: the engine subsumes them (and covers every other
 * module besides).
 */
class NoDomDestructionParityTest {

    private final ServedModuleRenderer renderer = new ServedModuleRenderer();

    private int wipes(EsModule<?> module) {
        List<Finding> f = NoDomDestructionRule.INSTANCE.check(renderer.render(module));
        return f.size();
    }

    @Test
    void engineReproducesTheOldScannerVerdicts() {
        assertEquals(0, wipes(SplitPaneModule.INSTANCE),        "SplitPane must be clean");
        assertEquals(0, wipes(MultiTabPaneModule.INSTANCE),     "MultiTabPane must be clean");
        assertEquals(0, wipes(MultiTabPaneDragModule.INSTANCE), "MultiTabPaneDrag must be clean");
        assertEquals(1, wipes(ModalModule.INSTANCE),
                "Modal carries the one wholesale wipe the old scanner allowlisted");
    }
}
