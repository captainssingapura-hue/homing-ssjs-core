package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 Phase 6 — bulk ops + clipboard: the selection materialised to
 * identity sets, TSV copy over the active rect via getValueToCopy (RAW
 * values, batch drained first), delete handing the PK set to the adapter.
 * Dish List acceptance rows 14 and 15. The domain never sees (i, j).
 */
class RelationGridBulkOpsTest {

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
                    focusCount: 0,
                    focus: function () { this.focusCount++; },
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
            function fixture(optsIn) {
                optsIn = optsIn || {};
                var data = {
                    mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5 },
                    coq:    { ingredient: 'chicken', style: 'French',  calories: 610, price: 18  },
                    fish:   { ingredient: 'cod',     style: 'English', calories: 560, price: 12  },
                    sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, price: 14  },
                    burger: { ingredient: 'beef',    style: 'USA',     calories: 780, price: 11  },
                    carbo:  { ingredient: 'pasta',   style: 'Italian', calories: 720, price: 13  }
                };
                var subs = [], deleted = [];
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'style', 'calories', 'price']; },
                    get:     function (pk, col) { return data[pk] ? data[pk][col] : undefined; },
                    subscribe:   function (fn) { subs.push(fn); },
                    unsubscribe: function () {},
                    push: function (pk, col, v) {
                        data[pk][col] = v;
                        subs.forEach(function (fn) { fn(pk, col, v); });
                    },
                    update: function (pk, col, v) { this.push(pk, col, v); },
                    deleteRows: function (pks) {
                        deleted.push(pks.join(','));
                        pks.forEach(function (pk) { delete data[pk]; });
                    }
                };
                var copies = [];
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter,
                    // price formatted for DISPLAY — copy must still yield the raw form
                    cellFactory: function (column, value) {
                        if (column === 'price') return new NumberCell({ format: function (v) {
                            return '$' + v.toFixed(2); } });
                        return (typeof value === 'number') ? new NumberCell() : new TextCell();
                    },
                    onCopy: function (tsv) { copies.push(tsv); }
                });
                function tdAt(i, j) { return container.children[0].children[2].children[i].children[j]; }
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
                function visiblePks() {
                    var out = [], maps = grid.viewMaps();
                    for (var i = 0; i < maps.rows(); i++) out.push(maps.pkAt(i));
                    return out;
                }
                return { grid: grid, adapter: adapter, data: data, deleted: deleted,
                         copies: copies, tdAt: tdAt, key: key, click: click,
                         visiblePks: visiblePks };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridHeaderDragModule.js", "GridColumnOpsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "GridCellTypesModule.js", "StockCellsModule.js", "GridSelectionModule.js", "GridKeyboardModule.js",
                "GridEditControllerModule.js", "GridBulkOpsModule.js", "GridUpdateBatchModule.js", "GridBulkEditSessionModule.js", "GridViewStateModule.js", "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void copyActiveRangeAsRawValueTsv() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);                        // mapo/calories
                    f.click(1, 3, { shift: true });       // rect (0,2)..(1,3)
                    var tsv = f.grid.copySelection();
                    // RAW values: the display shows $9.50 but the clipboard gets 9.5
                    return tsv === '480\\t9.5\\n610\\t18'
                        && f.tdAt(0, 3).children[0].textContent === '$9.50';
                })()"""), "row 14: the active rect copies as TSV of RAW values, view order");
    }

    @Test
    void copyFallsBackToTheCursorCell() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(2, 0);                        // fish/ingredient, no range
                    return f.grid.copySelection() === 'cod';
                })()"""), "no range up: the cursor cell alone is the clipboard payload");
    }

    @Test
    void multiRangeCopyUsesTheActiveRange() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    f.click(1, 1, { shift: true });       // first rect
                    f.click(4, 1, { ctrl: true });        // second (ACTIVE) range: burger/style
                    return f.grid.copySelection() === 'USA';
                })()"""), "multi-range copy serialises the ACTIVE (last) range, Excel-style");
    }

    @Test
    void copyDrainsThePendingBatchFirst() {
        assertTrue(evalBool("""
                (() => {
                    var frames = [];
                    globalThis.requestAnimationFrame = function (fn) { frames.push(fn); };
                    var f = fixture();
                    f.click(1, 3);                        // coq/price
                    f.adapter.push('coq', 'price', 19.5); // batched, no frame fires
                    var stale = f.tdAt(1, 3).children[0].textContent === '$18.00';
                    var tsv = f.grid.copySelection();     // read-path MUST drain
                    delete globalThis.requestAnimationFrame;
                    return stale && tsv === '19.5'
                        && f.tdAt(1, 3).children[0].textContent === '$19.50';
                })()"""), "a copy never reads stale cell state: flushNow runs first");
    }

    @Test
    void ctrlCFiresTheOnCopyCallback() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(3, 1);                        // sauer/style
                    f.key('c', { ctrl: true });
                    f.key('c');                           // plain c: NOT a copy
                    return f.copies.length === 1 && f.copies[0] === 'German';
                })()"""), "Ctrl+C hands the TSV to onCopy; plain c does nothing");
    }

    @Test
    void endingAnEditRestoresGridFocus() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var table = f.grid._layout.el();
                    f.click(0, 0);
                    f.key('Enter');                       // editing: the INPUT holds focus
                    var before = table.focusCount;
                    f.key('Enter');                       // commit removes the input
                    var afterCommit = table.focusCount;
                    f.key('Enter');
                    f.key('Escape');                      // cancel too
                    return afterCommit === before + 1
                        && table.focusCount === before + 2
                        && !f.grid.isEditing();
                })()"""), "commit AND cancel re-arm native focus on the table (issue 1)");
    }

    @Test
    void deleteKeyClearsTheSelectedCellsContents() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);                        // mapo/calories
                    f.click(1, 3, { shift: true });       // rect: 4 cells
                    f.key('Delete');
                    f.grid.flushNow();
                    return f.data.mapo.calories === null && f.data.mapo.price === null
                        && f.data.coq.calories === null && f.data.coq.price === null
                        && f.data.mapo.ingredient === 'tofu'          // outside the rect: untouched
                        && f.tdAt(0, 3).children[0].textContent === ''
                        && JSON.parse(f.grid.cursor()) !== null;      // selection intact
                })()"""), "issue 2a: Delete clears the selected cells' contents, Excel-style");
    }

    @Test
    void deleteSelectedRowsHandsThePkSetToTheAdapterAndRowsDie() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    f.click(1, 0);                        // coq
                    f.click(2, 3, { shift: true });       // range spans coq+fish rows
                    var pks = g.deleteSelectedRows();
                    var cursor = JSON.parse(g.cursor());
                    return pks.join(',') === 'coq,fish'
                        && f.deleted.join(';') === 'coq,fish'            // the ADAPTER got the set
                        && f.visiblePks().join(',') === 'mapo,sauer,burger,carbo'
                        && g._cells.get('coq', 'price') === null         // cells died (the only death)
                        && g._cells.size() === 16
                        && cursor !== null                               // selection self-healed
                        && g.viewMaps().locate(cursor.pk, cursor.column) !== null
                        && JSON.parse(g.selection()).length === 0;       // dead ranges dropped
                })()"""), "row 15: delete = adapter.deleteRows(pks) + rows retire in one refresh");
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
