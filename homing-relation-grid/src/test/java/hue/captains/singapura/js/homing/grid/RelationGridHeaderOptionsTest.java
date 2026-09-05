package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 — HEADER DISPLAY OPTIONS: the downstream chooses whether the
 * band renders at all, whether it freezes while the body scrolls, whether a
 * copied block leads with column labels, and what those labels say. A
 * headerless grid builds NO thead — no CSS trick needed downstream (which
 * is exactly what Minesweeper used to do).
 */
class RelationGridHeaderOptionsTest {

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
                    header: optsIn.header,
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
                function tdAt(i, j) { return table.children[table.children.length - 1].children[i].children[j]; }
                function click(i, j, mods) {
                    mods = mods || {};
                    tdAt(i, j).fire('click', { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                return { grid: grid, adapter: adapter, data: data, container: container,
                         table: table, colWidth: colWidth, headerTexts: headerTexts,
                         visiblePks: visiblePks, key: key, thAt: thAt,
                         tdAt: tdAt, click: click };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridHeaderDragModule.js", "GridColumnOpsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "GridCellTypesModule.js", "StockCellsModule.js", "GridSelectionModule.js",
                "GridKeyboardModule.js", "GridEditControllerModule.js", "GridBulkOpsModule.js", "GridUpdateBatchModule.js",
                "GridBulkEditSessionModule.js", "GridViewStateModule.js", "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }


    @Test
    void headerCanBeTurnedOffEntirely() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ header: { show: false } });
                    var kids = f.table.children.map(function (c) { return c.tagName; });
                    // colgroup + tbody only: NO thead is built (not display:none)
                    return kids.join(',') === 'colgroup,tbody'
                        && f.table.children[1].children.length === 6      // rows still render
                        && f.table.className.indexOf('hgr-sticky-head') < 0;
                })()"""), "header.show=false builds no thead at all — the CSS trick is retired");
    }

    @Test
    void aHeaderlessGridStillSelectsEditsAndSizes() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ header: { show: false } });
                    var td = f.table.children[1].children[0].children[0];
                    td.fire('click', {});
                    var selects = f.grid.cursor() === '{"pk":"mapo","column":"ingredient"}';
                    f.key('ArrowDown');
                    var moves = JSON.parse(f.grid.cursor()).pk === 'coq';
                    f.grid.setColumnWidth('price', 150);                  // programmatic path
                    f.key('ArrowRight', { alt: true });                   // keyboard path
                    return selects && moves
                        && f.table.children[0].children[3].style.props['--hgr-col-w'] === '150px'
                        && f.table.children[0].children.length === 4;     // cols exist regardless
                })()"""), "no header ≠ no grid: selection, editing and sizing all still work");
    }

    @Test
    void stickinessIsOptional() {
        assertTrue(evalBool("""
                (() => {
                    var on  = fixture();                                   // default
                    var off = fixture({ header: { sticky: false } });
                    return on.table.className.indexOf('hgr-sticky-head') >= 0
                        && off.table.className.indexOf('hgr-sticky-head') < 0
                        && off.headerTexts().length === 4;                 // still rendered
                })()"""), "header.sticky=false renders the band without freezing it");
    }

    @Test
    void labelsRenameForDisplayOnlyNeverForIdentity() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ header: { labels: { ingredient: 'Main Ingredient',
                                                          price: 'Price (USD)' } } });
                    var shown = f.headerTexts().join(',') === 'Main Ingredient,style,calories,Price (USD)';
                    f.grid.hideColumn('price');                            // identity unchanged
                    var stillIdentity = f.headerTexts().join(',') === 'Main Ingredient,style,calories';
                    f.grid.showColumn('price');
                    return shown && stillIdentity
                        && f.grid.viewMaps().locate('mapo', 'price') !== null
                        && JSON.parse(JSON.stringify(f.grid.viewState())).columns.order.indexOf('price') >= 0;
                })()"""), "labels are display text; commands and state keep speaking columnName");
    }

    @Test
    void copyCanLeadWithTheColumnLabels() {
        assertTrue(evalBool("""
                (() => {
                    var plain = fixture();
                    plain.click(0, 2); plain.click(1, 3, { shift: true });
                    var withoutHead = plain.grid.copySelection();
                    var f = fixture({ header: { includeInCopy: true,
                                                labels: { price: 'Price (USD)' } } });
                    f.click(0, 2); f.click(1, 3, { shift: true });
                    var TAB = String.fromCharCode(9), NL = String.fromCharCode(10);
                    return withoutHead === '480' + TAB + '9.5' + NL + '610' + TAB + '18'
                        && f.grid.copySelection() ===
                           'calories' + TAB + 'Price (USD)' + NL +
                           '480' + TAB + '9.5' + NL + '610' + TAB + '18';
                })()"""), "header.includeInCopy leads the block with the SELECTED columns' labels");
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
