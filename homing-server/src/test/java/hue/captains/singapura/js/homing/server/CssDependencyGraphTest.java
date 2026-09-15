package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0064 — the dependency graph, evaluated as a raw script. Pins the plan:
 * the theme bundle first, priors next in arrival order, then Kahn's waves; a
 * dependency outside the requested set counts as satisfied; a cycle is
 * refused with its names; a dependency named but never declared is an
 * unknown node the manager must load by name.
 */
class CssDependencyGraphTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/server/CssDependencyGraph.js";

    private Value graph;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        graph = js.eval("js", "createCssDependencyGraph()");
    }

    private void merge(String json) { js.eval("js", "(g => g.merge(" + json + "))")
            .execute(graph); }

    private List<List<String>> plan(String... ids) {
        StringBuilder arr = new StringBuilder("[");
        for (int i = 0; i < ids.length; i++) { if (i > 0) arr.append(","); arr.append('"').append(ids[i]).append('"'); }
        arr.append("]");
        Value waves = js.eval("js", "(g => g.plan(" + arr + "))").execute(graph);
        List<List<String>> out = new ArrayList<>();
        for (long i = 0; i < waves.getArraySize(); i++) {
            Value w = waves.getArrayElement(i);
            List<String> wave = new ArrayList<>();
            for (long j = 0; j < w.getArraySize(); j++) wave.add(w.getArrayElement(j).asString());
            out.add(wave);
        }
        return out;
    }

    // ── The bundle and the priors ─────────────────────────────────────────────

    @Test
    void anEmptyPlan_isStillTheBundle() {
        assertEquals(List.of(List.of("__theme-vars"), List.of("__theme-globals")), plan());
    }

    @Test
    void priorsLoadFirst_inArrivalOrder_andInOneWave() {
        merge("{ Base: { deps: [], prior: true }, Util: { deps: [], prior: true } }");
        merge("{ Picker: { deps: [] } }");
        assertEquals(List.of(
                List.of("__theme-vars"), List.of("__theme-globals"),
                List.of("Base", "Util"),
                List.of("Picker")), plan("Picker", "Util", "Base"));
    }

    @Test
    void aPriorMayNotDeclareDependencies() {
        assertTrue(js.eval("js", """
                (g => { try { g.merge({ P: { deps: ["X"], prior: true } }); return false; }
                        catch (e) { return /prior/.test(String(e)); } })
                """).execute(graph).asBoolean());
    }

    // ── Kahn's waves ──────────────────────────────────────────────────────────

    @Test
    void aDiamond_loadsInThreeWaves_withTheMiddlePairParallel() {
        merge("{ Root: { deps: ['Left', 'Right'] }, Left: { deps: ['Base'] }, Right: { deps: ['Base'] }, Base: { deps: [] } }");
        assertEquals(List.of(
                List.of("__theme-vars"), List.of("__theme-globals"),
                List.of("Base"),
                List.of("Left", "Right"),
                List.of("Root")), plan("Root", "Left", "Right", "Base"));
    }

    @Test
    void aWaveIsOnlyNodesWithNothingPending() {
        merge("{ A: { deps: [] }, B: { deps: ['A'] }, C: { deps: ['B'] }, D: { deps: [] } }");
        // D has nothing pending → it joins A's wave; C waits for B which waits for A.
        assertEquals(List.of(
                List.of("__theme-vars"), List.of("__theme-globals"),
                List.of("A", "D"), List.of("B"), List.of("C")), plan("A", "B", "C", "D"));
    }

    @Test
    void aDependencyOutsideTheRequestedSet_countsAsSatisfied() {
        merge("{ Widget: { deps: ['Base'] }, Base: { deps: [] } }");
        // loading only Widget (Base already on the page): one wave, no Base.
        assertEquals(List.of(
                List.of("__theme-vars"), List.of("__theme-globals"),
                List.of("Widget")), plan("Widget"));
    }

    @Test
    void aCycle_isRefusedWithItsNames() {
        merge("{ A: { deps: ['B'] }, B: { deps: ['C'] }, C: { deps: ['A'] }, Ok: { deps: [] } }");
        assertTrue(js.eval("js", """
                (g => { try { g.plan(['A', 'B', 'C', 'Ok']); return ""; } catch (e) { return String(e); } })
                """).execute(graph).asString().contains("cycle among A, B, C"));
    }

    @Test
    void selfDependency_isRefusedAtMerge() {
        assertTrue(js.eval("js", """
                (g => { try { g.merge({ A: { deps: ['A'] } }); return false; } catch (e) { return /itself/.test(String(e)); } })
                """).execute(graph).asBoolean());
    }

    // ── Merging and closure ───────────────────────────────────────────────────

    @Test
    void aNamedButUndeclaredDependency_isUnknownUntilItsModuleArrives() {
        merge("{ Widget: { deps: ['Late'] } }");
        assertTrue(js.eval("js", "(g => g.has('Late'))").execute(graph).asBoolean());
        assertFalse(js.eval("js", "(g => g.known('Late'))").execute(graph).asBoolean());
        merge("{ Late: { deps: [] } }");
        assertTrue(js.eval("js", "(g => g.known('Late'))").execute(graph).asBoolean());
    }

    @Test
    void mergingTwice_unionsDependencies_andKeepsFirstArrival() {
        merge("{ A: { deps: ['X'] }, X: { deps: [] } }");
        merge("{ A: { deps: ['Y'] }, Y: { deps: [] } }");
        assertEquals(List.of("X", "Y"), js.eval("js", "(g => Array.from(g.deps('A')))").execute(graph).as(List.class));
        assertEquals("A", js.eval("js", "(g => g.snapshot()[0].id)").execute(graph).asString());
    }

    @Test
    void closure_isDependenciesFirst_thenTheNode() {
        merge("{ Root: { deps: ['Left', 'Right'] }, Left: { deps: ['Base'] }, Right: { deps: ['Base'] }, Base: { deps: [] } }");
        assertEquals(List.of("Base", "Left", "Right", "Root"),
                js.eval("js", "(g => g.closureOf(['Root']))").execute(graph).as(List.class));
    }

    @Test
    void theSnapshot_isFrozenData() {
        merge("{ A: { deps: ['B'], prior: false }, B: { deps: [] } }");
        assertTrue(js.eval("js", "(g => { const s = g.snapshot(); return Object.isFrozen(s) && Object.isFrozen(s[0]) && Object.isFrozen(s[0].deps); })")
                .execute(graph).asBoolean());
    }
}
