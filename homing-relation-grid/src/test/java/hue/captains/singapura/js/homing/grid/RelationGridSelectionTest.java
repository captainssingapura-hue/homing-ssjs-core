package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 Phase 4 — selection + the shallow keyboard: the RFC 0049 pipeline
 * one level down. Intent in identity space, a TOTAL resolver, paint via the
 * layout's diff. Dish List acceptance rows 1, 2, 3, 18, 19 — keyboard-only
 * where applicable — plus the invariant property test over an event storm:
 * whenever the view is non-empty there is EXACTLY ONE cursor, it resolves
 * inside the view, and the painted classes equal the resolved answer.
 * D5 settles here (v1 lean): remaps reset ranges; the cursor survives by
 * identity, else by position clamp.
 */
class RelationGridSelectionTest {

    private Context js;

    private static final String DOM_STUB = """
            function makeEl(tag) {
                return {
                    tagName: tag, id: "", className: "", textContent: "",
                    children: [], parentNode: null, listeners: {}, attrs: {},
                    appendChild: function (c) {
                        if (c.parentNode) c.parentNode.removeChild(c);
                        c.parentNode = this; this.children.push(c); return c;
                    },
                    removeChild: function (c) {
                        var i = this.children.indexOf(c);
                        if (i >= 0) this.children.splice(i, 1);
                        c.parentNode = null; return c;
                    },
                    replaceChild: function (nu, old) {
                        var i = this.children.indexOf(old);
                        if (nu.parentNode) nu.parentNode.removeChild(nu);
                        this.children[i] = nu; old.parentNode = null;
                        nu.parentNode = this; return old;
                    },
                    addEventListener: function (t, f) {
                        (this.listeners[t] || (this.listeners[t] = [])).push(f);
                    },
                    removeEventListener: function (t, f) {
                        var l = this.listeners[t] || [];
                        var i = l.indexOf(f); if (i >= 0) l.splice(i, 1);
                    },
                    fire: function (t, e) {
                        (this.listeners[t] || []).slice().forEach(function (f) { f(e); });
                    },
                    setAttribute: function (k, v) { this.attrs[k] = String(v); },
                    getAttribute: function (k) { return (k in this.attrs) ? this.attrs[k] : null; },
                    get firstChild() { return this.children[0] || null; }
                };
            }
            var document = {
                head: makeEl("head"),
                createElement: function (tag) { return makeEl(tag); },
                getElementById: function (id) {
                    for (var i = 0; i < this.head.children.length; i++)
                        if (this.head.children[i].id === id) return this.head.children[i];
                    return null;
                }
            };
            var console = console || { error: function () {} };
            """;

    private static final String FIXTURE = """
            function fixture() {
                var data = {
                    mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5 },
                    coq:    { ingredient: 'chicken', style: 'French',  calories: 610, price: 18  },
                    fish:   { ingredient: 'cod',     style: 'English', calories: 560, price: 12  },
                    sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, price: 14  },
                    burger: { ingredient: 'beef',    style: 'USA',     calories: 780, price: 11  },
                    carbo:  { ingredient: 'pasta',   style: 'Italian', calories: 720, price: 13  }
                };
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'style', 'calories', 'price']; },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe: function () {}, unsubscribe: function () {}
                };
                var events = { cursor: [], selection: [] };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter,
                    onCursorMoved: function (pk, col) { events.cursor.push(pk + ':' + col); },
                    onSelectionChanged: function (ranges) { events.selection.push(ranges.length); }
                });
                function tdAt(i, j) { return container.children[0].children[2].children[i].children[j]; }
                function has(td, c) { return td.className.split(' ').indexOf(c) >= 0; }
                function key(k, mods) {
                    mods = mods || {};
                    container.children[0].fire('keydown', {
                        key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl,
                        preventDefault: function () {}
                    });
                }
                function click(i, j, mods) {
                    mods = mods || {};
                    tdAt(i, j).fire('click', { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                function paintCensus() {   // sweep the whole table: what is painted?
                    var maps = grid.viewMaps(), cursors = 0, sels = 0;
                    for (var i = 0; i < maps.rows(); i++) for (var j = 0; j < maps.cols(); j++) {
                        if (has(tdAt(i, j), 'hgr-cursor')) cursors++;
                        if (has(tdAt(i, j), 'hgr-sel')) sels++;
                    }
                    return { cursors: cursors, sels: sels };
                }
                return { grid: grid, data: data, events: events, tdAt: tdAt, has: has,
                         key: key, click: click, paintCensus: paintCensus };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridHeaderDragModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "GridCellTypesModule.js", "StockCellsModule.js", "GridSelectionModule.js", "GridKeyboardModule.js", "GridEditControllerModule.js", "GridBulkOpsModule.js", "GridUpdateBatchModule.js", "GridBulkEditSessionModule.js", "GridViewStateModule.js",
                "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void bootYieldsExactlyOneCursorAtOrigin() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var census = f.paintCensus();
                    return f.grid.cursor() === '{"pk":"mapo","column":"ingredient"}'
                        && census.cursors === 1 && census.sels === 1   // the cursor cell is selected
                        && f.has(f.tdAt(0, 0), 'hgr-cursor');
                })()"""), "the exactly-one invariant holds from boot: cursor at the origin");
    }

    @Test
    void arrowsMoveEdgeConfinedAndShiftExtends() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('ArrowDown'); f.key('ArrowDown'); f.key('ArrowRight');
                    var moved = f.grid.cursor() === '{"pk":"fish","column":"style"}';
                    f.key('ArrowDown', { shift: true });   // extend: cursor STAYS, focus moves
                    f.key('ArrowRight', { shift: true });
                    var census = f.paintCensus();          // rect (2,1)..(3,2) = 4 cells
                    var stillCursor = f.grid.cursor() === '{"pk":"fish","column":"style"}';
                    for (var k = 0; k < 9; k++) f.key('ArrowUp');   // confinement: clamp at 0
                    return moved && stillCursor
                        && census.cursors === 1 && census.sels === 4
                        && JSON.parse(f.grid.cursor()).pk === 'mapo';
                })()"""), "arrows move the cursor; Shift extends the range while the cursor stays");
    }

    @Test
    void clickRoutingShiftRangeCtrlMultiRange() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 1);                          // plain: cursor to coq/style
                    var plain = f.grid.cursor() === '{"pk":"coq","column":"style"}';
                    f.click(2, 2, { shift: true });         // extend: rect (1,1)..(2,2)
                    var afterShift = f.paintCensus().sels === 4;
                    f.click(4, 3, { ctrl: true });          // add a second 1x1 range
                    var ranges = JSON.parse(f.grid.selection());
                    var census = f.paintCensus();
                    return plain && afterShift
                        && ranges.length === 2
                        && census.sels === 5 && census.cursors === 1
                        && f.grid.cursor() === '{"pk":"burger","column":"price"}';
                })()"""), "click routes at the position->identity seam: plain / Shift / Ctrl");
    }

    @Test
    void d5RemapResetsRangesCursorSurvivesByIdentityThenClamp() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    f.click(2, 3);                                    // cursor fish/price
                    f.click(4, 3, { shift: true });                   // a 3x1 range
                    g.sortBy('price', 'desc');                        // REMAP
                    var afterSort = JSON.parse(g.selection()).length === 0        // ranges reset
                        && g.cursor() === '{"pk":"fish","column":"price"}'        // identity survives
                        && f.has(f.tdAt(3, 3), 'hgr-cursor');                     // ...at its NEW row
                    g.filterRows(function (pk, get) { return get('style') !== 'English'; });
                    var afterFilter = JSON.parse(g.cursor()).pk !== 'fish'        // fish left the view
                        && f.paintCensus().cursors === 1;                         // ...but SOME cursor exists (clamp)
                    return afterSort && afterFilter;
                })()"""), "D5 v1 lean: remaps reset ranges; the cursor survives by identity, else clamp");
    }

    @Test
    void ctrlASelectsTheViewAndHomeEndJumpTheRow() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('a', { ctrl: true });
                    var all = f.paintCensus().sels === 24;
                    f.key('End');
                    var atEnd = JSON.parse(f.grid.cursor()).column === 'price';
                    f.key('Home');
                    return all && atEnd
                        && JSON.parse(f.grid.cursor()).column === 'ingredient';
                })()"""), "Ctrl+A covers the view; Home/End jump within the row");
    }

    @Test
    void invariantHoldsAcrossAnEventStorm() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    var seed = 42;
                    var rnd = function (n) { seed = (seed * 1103515245 + 12345) % 2147483648; return seed % n; };
                    var ops = [
                        function () { f.key(['ArrowUp','ArrowDown','ArrowLeft','ArrowRight'][rnd(4)]); },
                        function () { f.key(['ArrowDown','ArrowRight'][rnd(2)], { shift: true }); },
                        function () { f.click(rnd(maps.rows()), rnd(maps.cols()), { ctrl: rnd(2) === 1 }); },
                        function () { g.sortBy(['price','calories',null][rnd(3)], 'asc'); },
                        function () { rnd(2) === 1
                            ? g.filterRows(function (pk, get) { return get('calories') < 700; })
                            : g.clearFilter(); },
                        function () { rnd(2) === 1 ? g.hideColumn('style') : g.showColumn('style'); },
                        function () { f.key('a', { ctrl: true }); }
                    ];
                    for (var n = 0; n < 200; n++) {
                        ops[rnd(ops.length)]();
                        if (!maps.rows() || !maps.cols()) continue;
                        var cursor = JSON.parse(g.cursor());
                        if (!cursor) return false;                          // exactly one: existence
                        var at = maps.locate(cursor.pk, cursor.column);
                        if (!at) return false;                              // cursor resolves in-view
                        var census = f.paintCensus();
                        if (census.cursors !== 1) return false;             // exactly one: paint
                        if (!f.has(f.tdAt(at.i, at.j), 'hgr-cursor')) return false;   // painted = resolved
                        if (census.sels < 1) return false;                  // the cursor cell is selected
                        var ranges = JSON.parse(g.selection());
                        for (var r = 0; r < ranges.length; r++) {           // every range resolves
                            if (!maps.locate(ranges[r].anchor.pk, ranges[r].anchor.column)) return false;
                            if (!maps.locate(ranges[r].focus.pk, ranges[r].focus.column)) return false;
                        }
                    }
                    return true;
                })()"""), "after 200 storm events the postcondition holds every single time");
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
