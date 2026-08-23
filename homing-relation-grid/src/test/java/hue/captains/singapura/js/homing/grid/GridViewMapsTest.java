package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 Phase 1 — the view-map laws, unit-tested without a browser
 * (GridViewMaps is pure logic; this is the module-map boundary made testable).
 *
 * <p>Laws: locate∘resolve is the identity on the visible view; a permutation
 * preserves membership; a subset filter drops locate to null without touching
 * the base; hidden columns resolve to -1; remaps reject unknowns and
 * duplicates; base removal falls out of every view; resets restore the base.</p>
 */
class GridViewMapsTest {

    private Context js;

    private static final String FIXTURE = """
            function fixture() {
                var events = [];
                var m = new GridViewMaps({
                    pks:     ['mapo', 'coq', 'fish', 'sauer', 'burger', 'carbo'],
                    columns: ['ingredient', 'style', 'calories', 'price'],
                    onViewChanged: function (kind) { events.push(kind); }
                });
                return { m: m, events: events };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/GridViewMapsModule.js"));
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void locateResolveRoundTripsOverTheWholeView() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), m = f.m;
                    for (var i = 0; i < m.rows(); i++) for (var j = 0; j < m.cols(); j++) {
                        var id = m.resolve(i, j);
                        var back = m.locate(id.pk, id.column);
                        if (back.i !== i || back.j !== j) return false;
                    }
                    return m.rows() === 6 && m.cols() === 4;
                })()"""), "locate(resolve(i,j)) must be (i,j) for every visible cell");
    }

    @Test
    void sortIsAPermutationPreservingMembership() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), m = f.m;
                    m.setRowView(['carbo', 'burger', 'sauer', 'fish', 'coq', 'mapo']);   // reverse
                    return m.rows() === 6
                        && m.pkAt(0) === 'carbo' && m.rowOf('mapo') === 5
                        && m.resolve(0, 1).pk === 'carbo'
                        && f.events.join(',') === 'rows';
                })()"""), "a permutation reorders without gaining or losing rows");
    }

    @Test
    void filterIsASubsetDroppingLocateNotBase() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), m = f.m;
                    m.setRowView(['mapo']);                       // filter: style = Chinese
                    return m.rows() === 1
                        && m.locate('coq', 'price') === null      // filtered out of the VIEW
                        && m.rowOf('coq') === -1
                        && m.basePks().length === 6               // ...but still EXISTS
                        && (m.resetRowView(), m.rows() === 6);    // unfilter restores
                })()"""), "filter narrows the view only; the base Relation is untouched");
    }

    @Test
    void hideAndReorderAreColumnRemaps() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), m = f.m;
                    m.setColumnView(['price', 'ingredient', 'style']);   // hide calories, price first
                    return m.cols() === 3
                        && m.columnAt(0) === 'price'
                        && m.colOf('calories') === -1                    // hidden
                        && m.locate('mapo', 'calories') === null
                        && (m.resetColumnView(), m.colOf('calories') === 2);
                })()"""), "hide = omit from the column view; reorder = permute it");
    }

    @Test
    void remapsRejectUnknownsAndDuplicates() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), m = f.m;
                    var threw = 0;
                    try { m.setRowView(['mapo', 'nope']); } catch (e) { threw++; }
                    try { m.setRowView(['mapo', 'mapo']); } catch (e) { threw++; }
                    try { m.setColumnView(['price', 'ghost']); } catch (e) { threw++; }
                    return threw === 3 && m.rows() === 6;   // failed remaps leave the view intact
                })()"""), "the view can only show what exists, once");
    }

    @Test
    void baseMutationsFlowIntoEveryView() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), m = f.m;
                    m.setRowView(['fish', 'mapo']);       // a filtered, sorted view
                    m.removePk('mapo');                   // the row leaves the Relation
                    var goneEverywhere = m.rowOf('mapo') === -1 && m.basePks().indexOf('mapo') < 0;
                    m.addPk('tiramisu');                  // a row enters — appended to base + view
                    return goneEverywhere
                        && m.rows() === 2 && m.pkAt(1) === 'tiramisu'
                        && m.basePks().length === 6;
                })()"""), "base add/remove reach both the base and the live view");
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private void eval(String src) { js.eval("js", src); }
    private boolean evalBool(String expr) { return js.eval("js", expr).asBoolean(); }
    private String readJs(String path) {
        try (var in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, "missing classpath resource: " + path);
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to read " + path, e); }
    }
}
