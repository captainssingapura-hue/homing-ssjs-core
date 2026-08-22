package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050-ext2 — column sizing + the ViewState round-trip. Widths key on
 * columnName and so survive hide/show/reorder; declarative filters
 * ({column, op, operand}) compose with AND and serialize; the raw predicate
 * is ephemeral; snapshot()/applyViewState() round-trips through JSON with
 * drift tolerance; the staged header drag commits on release and Escape
 * abandons; Alt+arrows resize the cursor's column.
 */
class RelationGridViewStateTest {

    private Context js;

    private static final String DOM_STUB = """
            function makeEl(tag) {
                var el = {
                    tagName: tag, id: "", className: "", textContent: "",
                    children: [], parentNode: null, listeners: {}, attrs: {}, value: "",
                    style: { props: {},
                             setProperty: function (k, v) { this.props[k] = v; },
                             removeProperty: function (k) { delete this.props[k]; } },
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
                    focus: function () {},
                    get firstChild() { return this.children[0] || null; }
                };
                // th/table rects so the edge hot-zone logic runs headlessly
                el.getBoundingClientRect = function () {
                    return { left: el._rl || 0, right: el._rr || 100,
                             top: 0, height: 200 };
                };
                return el;
            }
            var document = {
                head: makeEl("head"),
                body: makeEl("body"),
                listeners: {},
                createElement: function (tag) { return makeEl(tag); },
                getElementById: function (id) {
                    for (var i = 0; i < this.head.children.length; i++)
                        if (this.head.children[i].id === id) return this.head.children[i];
                    return null;
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
                }
            };
            var console = console || { error: function () {} };
            """;

    private static final String FIXTURE = """
            var STYLES = ['Chinese', 'French', 'English', 'German', 'USA', 'Italian'];
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
                    get:     function (pk, col) { return data[pk] ? data[pk][col] : undefined; },
                    subscribe: function () {}, unsubscribe: function () {},
                    update: function (pk, col, v) { data[pk][col] = v; }
                };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter
                });
                var table = container.children[0];
                function colWidth(j) {
                    return table.children[0].children[j].style.props['--hgr-col-w'] || null;
                }
                function headerTexts() {
                    return table.children[1].children[0].children
                            .map(function (th) { return th.textContent; });
                }
                function visiblePks() {
                    var out = [], maps = grid.viewMaps();
                    for (var i = 0; i < maps.rows(); i++) out.push(maps.pkAt(i));
                    return out;
                }
                function key(k, mods) {
                    mods = mods || {};
                    table.fire('keydown', {
                        key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl, altKey: !!mods.alt,
                        preventDefault: function () {}
                    });
                }
                function thAt(j) { return table.children[1].children[0].children[j]; }
                return { grid: grid, adapter: adapter, data: data, container: container,
                         table: table, colWidth: colWidth, headerTexts: headerTexts,
                         visiblePks: visiblePks, key: key, thAt: thAt };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "GridCellTypesModule.js", "StockCellsModule.js", "GridSelectionModule.js",
                "GridKeyboardModule.js", "GridEditControllerModule.js", "GridBulkOpsModule.js",
                "GridBulkEditSessionModule.js", "GridViewStateModule.js", "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void widthKeysOnIdentityAndSurvivesReorderAndHide() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.setColumnWidth('price', 150);
                    var atStart = f.colWidth(3) === '150px';           // price is j=3
                    g.reorderColumn('price', 0);
                    var afterReorder = f.colWidth(0) === '150px'       // width FOLLOWED the column
                        && f.colWidth(3) === null;
                    g.hideColumn('style'); g.showColumn('style');
                    return atStart && afterReorder
                        && f.colWidth(0) === '150px'
                        && f.table.className.indexOf('hgr-fixed') >= 0;
                })()"""), "width rides columnName through reorder and hide/show");
    }

    @Test
    void widthClampsToTheMinimum() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.setColumnWidth('price', 3);                      // below MIN
                    return f.colWidth(3) === '40px';
                })()"""), "a column can never be dragged to unrecoverable zero");
    }

    @Test
    void declarativeFiltersComposeWithAndAndClearPerColumn() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.setColumnFilter('calories', 'lt', 700);
                    g.setColumnFilter('style', 'oneOf', ['Chinese', 'German', 'USA']);
                    var both = f.visiblePks().join(',') === 'mapo,sauer';
                    g.clearColumnFilter('style');
                    var one = f.visiblePks().join(',') === 'mapo,coq,fish,sauer';
                    g.clearFilter();
                    return both && one && f.visiblePks().length === 6;
                })()"""), "criteria AND-compose; clearing one leaves the others");
    }

    @Test
    void viewStateRoundTripsThroughJson() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.reorderColumn('price', 0);
                    g.hideColumn('calories');
                    g.setColumnWidth('price', 160);
                    g.sortBy('price', 'desc');
                    g.setColumnFilter('calories', 'lt', 700);
                    var vs = JSON.parse(JSON.stringify(g.viewState()));   // the wire trip
                    var f2 = fixture();
                    f2.grid.applyViewState(vs);
                    return f2.headerTexts().join(',') === 'price,ingredient,style'
                        && f2.colWidth(0) === '160px'
                        && f2.visiblePks().join(',') === 'coq,sauer,fish,mapo'
                        && JSON.stringify(f2.grid.viewState()) === JSON.stringify(vs);
                })()"""), "arrangement out, JSON, arrangement back — identical");
    }

    @Test
    void rawPredicateIsEphemeralButCriteriaPersist() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.setColumnFilter('calories', 'lt', 700);
                    g.filterRows(function (pk, get) { return get('style') !== 'German'; });
                    var combined = f.visiblePks().join(',') === 'mapo,coq,fish';   // criteria AND raw
                    var vs = g.viewState();
                    var f2 = fixture();
                    f2.grid.applyViewState(vs);
                    return combined
                        && vs.filters.length === 1                                 // raw NOT saved
                        && f2.visiblePks().join(',') === 'mapo,coq,fish,sauer';    // criteria only
                })()"""), "the raw predicate works and vanishes; criteria round-trip");
    }

    @Test
    void restoreIsDriftTolerant() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.applyViewState({
                        columns: { order: ['price', 'GHOST', 'style'],
                                   widths: { GHOST: 200, price: 150 },
                                   hidden: ['GHOST', 'calories'] },
                        sort: [{ column: 'VANISHED', direction: 'asc' }],
                        filters: [{ column: 'GHOST', op: 'eq', operand: 1 },
                                  { column: 'style', op: 'weird-op', operand: 1 },
                                  { column: 'calories', op: 'lt', operand: 700 }]
                    });
                    // unknowns dropped, missing appended, bad sort/ops discarded
                    return f.headerTexts().join(',') === 'price,style,ingredient'
                        && f.colWidth(0) === '150px'
                        && f.visiblePks().join(',') === 'mapo,coq,fish,sauer'
                        && f.grid.viewState().filters.length === 1;
                })()"""), "a stale saved view never breaks a grid whose Relation moved on");
    }

    @Test
    void stagedDragCommitsOnReleaseAndEscapeAbandons() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var th = f.thAt(3);                          // price header
                    th._rl = 300; th._rr = 400;                  // rect: width 100, edge at 400
                    th.fire('mousedown', { clientX: 398, preventDefault: function () {} });
                    document.fire('mousemove', { clientX: 448 });
                    document.fire('mouseup', {});                // commit: 100 + 50
                    var committed = f.colWidth(3) === '150px';
                    th.fire('mousedown', { clientX: 399, preventDefault: function () {} });
                    document.fire('mousemove', { clientX: 500 });
                    document.fire('keydown', { key: 'Escape' }); // ABANDON
                    document.fire('mouseup', {});                // stale mouseup: listeners gone
                    var abandoned = f.colWidth(3) === '150px';
                    th.fire('mousedown', { clientX: 350, preventDefault: function () {} });
                    document.fire('mouseup', {});
                    return committed && abandoned
                        && f.colWidth(3) === '150px';            // mid-header press: no drag at all
                })()"""), "staged: release commits once; Escape abandons; only the edge arms");
    }

    @Test
    void altArrowsResizeTheCursorsColumn() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    f.key('ArrowRight');                          // cursor to style (j=1)
                    f.key('ArrowRight', { alt: true });
                    f.key('ArrowRight', { alt: true });
                    var grew = f.colWidth(1) === '140px';         // 120 default + 2*10
                    f.key('ArrowLeft', { alt: true });
                    var back = f.colWidth(1) === '130px';
                    return grew && back
                        && JSON.parse(f.grid.cursor()).column === 'style';   // cursor DID NOT move
                })()"""), "Alt+arrows are the pointer-free resize path, cursor unmoved");
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
