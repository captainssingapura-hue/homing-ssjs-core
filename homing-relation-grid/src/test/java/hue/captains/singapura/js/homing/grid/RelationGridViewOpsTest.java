package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 Phase 3 — view ops + direct updates: sort / filter / hide /
 * reorder as facade commands (held intent, recomputed views, pure remaps),
 * filter as true detach (parentNode null, state alive), base mutations
 * obeying held commands, and the rAF-batched direct path (last write per
 * cell wins). Dish List acceptance rows 9, 10, 11, 12, 16.
 */
class RelationGridViewOpsTest {

    private Context js;

    private static final String DOM_STUB = """
            function makeEl(tag) {
                return {
                    tagName: tag, id: "", className: "", textContent: "",
                    children: [], parentNode: null,
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
                    addEventListener: function () {}, removeEventListener: function () {},
                    setAttribute: function () {}, getAttribute: function () { return null; },
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

    /** The Dish List adapter, with the backing data EXPOSED for base-mutation tests. */
    private static final String FIXTURE = """
            function fixture(opts) {
                var data = {
                    mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5 },
                    coq:    { ingredient: 'chicken', style: 'French',  calories: 610, price: 18  },
                    fish:   { ingredient: 'cod',     style: 'English', calories: 560, price: 12  },
                    sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, price: 14  },
                    burger: { ingredient: 'beef',    style: 'USA',     calories: 780, price: 11  },
                    carbo:  { ingredient: 'pasta',   style: 'Italian', calories: 720, price: 13  }
                };
                var subs = [];
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'style', 'calories', 'price']; },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe:   function (fn) { subs.push(fn); },
                    unsubscribe: function (fn) { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); },
                    push: function (pk, col, v) {
                        data[pk][col] = v;
                        subs.forEach(function (fn) { fn(pk, col, v); });
                    }
                };
                var branchMints = 0;
                var branch = { createElement: function (name, tag) { branchMints++; return makeEl(tag); } };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container, branch: branch, adapter: adapter,
                    cellFactory: (opts && opts.cellFactory) || undefined
                });
                function visiblePks() {
                    var out = [], maps = grid.viewMaps();
                    for (var i = 0; i < maps.rows(); i++) out.push(maps.pkAt(i));
                    return out;
                }
                function headerTexts() {
                    return container.children[0].children[0].children[0].children
                            .map(function (th) { return th.textContent; });
                }
                function cellEl(pk, col) {
                    var e = grid._cells.get(pk, col);
                    return e ? e.el : null;
                }
                return { grid: grid, adapter: adapter, container: container, data: data,
                         visiblePks: visiblePks, headerTexts: headerTexts, cellEl: cellEl,
                         mints: function () { return branchMints; } };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "StockCellsModule.js", "GridSelectionModule.js", "GridKeyboardModule.js", "GridEditControllerModule.js", "GridBulkOpsModule.js",
                "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void sortByIsAPurePermutationBothWaysAndClears() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var el = f.cellEl('coq', 'price');
                    g.sortBy('price', 'asc');
                    var asc = f.visiblePks().join(',');
                    g.sortBy('price', 'desc');
                    var desc = f.visiblePks().join(',');
                    g.sortBy(null);
                    return asc === 'mapo,burger,fish,carbo,sauer,coq'
                        && desc === 'coq,sauer,carbo,fish,burger,mapo'
                        && f.visiblePks().join(',') === 'mapo,coq,fish,sauer,burger,carbo'
                        && f.cellEl('coq', 'price') === el      // same cell element throughout
                        && el.parentNode !== null               // ...still placed
                        && f.mints() === 24;                    // nothing created or destroyed
                })()"""), "sort is an i->PK permutation; sortBy(null) restores base order");
    }

    @Test
    void filterTrulyDetachesAndClearReattachesTheSameElements() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var kept = f.cellEl('mapo', 'price'), dropped = f.cellEl('coq', 'price');
                    g.filterRows(function (pk, get) { return get('style') === 'Chinese'; });
                    var whileFiltered = f.visiblePks().join(',') === 'mapo'
                        && kept.parentNode !== null
                        && dropped.parentNode === null;         // TRUE detach, not an orphaned subtree
                    g.clearFilter();
                    return whileFiltered
                        && f.visiblePks().length === 6
                        && f.cellEl('coq', 'price') === dropped // the SAME element came back
                        && dropped.parentNode !== null
                        && f.mints() === 24;
                })()"""), "filter detaches cells alive; clear re-attaches the same elements");
    }

    @Test
    void filterAndSortComposeAndBaseMutationsObeyThem() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.filterRows(function (pk, get) { return get('calories') < 700; });
                    g.sortBy('calories', 'desc');
                    var before = f.visiblePks().join(',');
                    f.data.salad    = { ingredient: 'greens', style: 'French', calories: 320, price: 8 };
                    f.data.tiramisu = { ingredient: 'cream',  style: 'Italian', calories: 740, price: 7 };
                    g.addRow('salad');                       // passes the filter -> sorted in
                    g.addRow('tiramisu');                    // fails the filter -> base only
                    return before === 'sauer,coq,fish,mapo'
                        && f.visiblePks().join(',') === 'sauer,coq,fish,mapo,salad'
                        && g.viewMaps().basePks().length === 8
                        && f.cellEl('tiramisu', 'price') === null;   // never placed, never minted
                })()"""), "held filter+sort recompute over every base mutation");
    }

    @Test
    void hideReorderShowAreColumnRemapsOnTheHeldOrder() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.hideColumn('calories');
                    var hidden = f.headerTexts().join(',') === 'ingredient,style,price';
                    g.reorderColumn('price', 0);
                    var reordered = f.headerTexts().join(',') === 'price,ingredient,style';
                    g.showColumn('calories');                // returns at its place in the HELD order
                    var priceEl = f.cellEl('mapo', 'price');
                    return hidden && reordered
                        && f.headerTexts().join(',') === 'price,ingredient,style,calories'
                        && priceEl.parentNode !== null
                        && f.mints() === 24;
                })()"""), "hide/show/reorder permute the j->columnName view only");
    }

    @Test
    void directUpdatesBatchPerFrameLastWriteWins() {
        assertTrue(evalBool("""
                (() => {
                    var frames = [];
                    globalThis.requestAnimationFrame = function (fn) { frames.push(fn); };
                    var updates = [];
                    var countingFactory = function (column, value) {
                        var cell = new TextCell();
                        var orig = cell.update.bind(cell);
                        cell.update = function (v) { updates.push(v); return orig(v); };
                        return cell;
                    };
                    var f = fixture({ cellFactory: countingFactory }), g = f.grid;
                    f.adapter.push('burger', 'price', 11.5);
                    f.adapter.push('burger', 'price', 12);
                    f.adapter.push('burger', 'price', 12.5);       // three pushes, one frame
                    var beforeFlush = updates.length === 0 && frames.length === 1;
                    frames.shift()();                              // the frame fires
                    delete globalThis.requestAnimationFrame;
                    return beforeFlush
                        && updates.length === 1 && updates[0] === 12.5   // last write won
                        && f.cellEl('burger', 'price').textContent === '12.5';
                })()"""), "a hot feed collapses to one cell.update per frame");
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
