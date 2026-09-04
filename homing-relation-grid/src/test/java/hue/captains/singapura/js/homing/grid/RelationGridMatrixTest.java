package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 — <b>THE ACCEPTANCE SWEEP</b>. The Dish List appendix's mapping
 * table is the v1 gate: "v1 ships when every numbered row works on this
 * fixture, keyboard-only where applicable. The mapping doubles as the
 * integration-test plan — each row is one test." This is that plan, one
 * method per row, named for it.
 *
 * <p>It exists <i>as well as</i> the per-phase suites, not instead of them,
 * for three reasons the phase suites cannot cover: every row runs against
 * ONE composed grid on the real fixture (the phase suites each build their
 * own narrower one); rows <b>compose</b> — row 17 is "sort by popularity
 * <i>then</i> it ticks", row 18 is "re-sort <i>with a selection active</i>",
 * row 13 is "resize <i>during an edit</i>" — which is where integration
 * actually breaks; and the gate becomes legible: one run, one row per
 * assertion, traceable to the doc.</p>
 *
 * <p><b>22 of 23 rows.</b> Row 9's mechanism (sort as a pure i→PK remap) is
 * built and covered in the view-ops suite; its <i>header-click trigger</i> is
 * deferred to RFC 0050-ext1, so the row is out of the v1 gate by decision —
 * the only one. Row 20 is the static reading path per D9 (no interactive ARIA
 * layer). Row 7's far end — the pane actually downgrading — is the host's
 * half of the RFC 0049 contract and is verified there; the grid's obligation,
 * tested here, is to fire the request and never consume Escape.</p>
 */
class RelationGridMatrixTest {

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
                    focus: function () { this.focused = true; },
                    get firstChild() { return this.children[0] || null; }
                };
                el.getBoundingClientRect = function () {
                    return { left: el._rl || 0, right: el._rr || 100, top: 0, height: 200 };
                };
                return el;
            }
            var document = {
                head: makeEl("head"), body: makeEl("body"), listeners: {},
                createElement: function (tag) { return makeEl(tag); },
                getElementById: function (id) {
                    for (var i = 0; i < this.head.children.length; i++)
                        if (this.head.children[i].id === id) return this.head.children[i];
                    return null;
                },
                addEventListener: function (t, f) { (this.listeners[t] || (this.listeners[t] = [])).push(f); },
                removeEventListener: function (t, f) {
                    var l = this.listeners[t] || []; var i = l.indexOf(f); if (i >= 0) l.splice(i, 1);
                },
                fire: function (t, e) { (this.listeners[t] || []).slice().forEach(function (f) { f(e); }); }
            };
            var console = console || { error: function () {} };
            var CARET = '\\u258F';
            """;

    /** The Dish List, wired exactly as the demo wires it — same EffectiveTypes. */
    private static final String FIXTURE = """
            var STYLES = ['Chinese', 'French', 'English', 'German', 'USA', 'Italian'];
            function fixture(optsIn) {
                optsIn = optsIn || {};
                var data = {
                    mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5,  popularity: 71 },
                    coq:    { ingredient: 'chicken', style: 'French',  calories: 610, price: 18,   popularity: 64 },
                    fish:   { ingredient: 'cod',     style: 'English', calories: 560, price: 12,   popularity: 58 },
                    sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, price: 14,   popularity: 49 },
                    burger: { ingredient: 'beef',    style: 'USA',     calories: 780, price: 11,   popularity: 88 },
                    carbo:  { ingredient: 'pasta',   style: 'Italian', calories: 720, price: 13,   popularity: 77 }
                };
                var subs = [], updates = [], deleted = [], copies = [], released = 0, rejected = [];
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'style', 'calories', 'price', 'popularity']; },
                    // ext6 — the Relation declares its orderings; a comparator is mandatory to sort
                    columnMeta: function (c) { return { compare: ['calories', 'price', 'popularity'].indexOf(c) >= 0 ? compareNumbers : compareText }; },
                    get:     function (pk, col) { return data[pk] ? data[pk][col] : undefined; },
                    subscribe:   function (fn) { subs.push(fn); },
                    unsubscribe: function () {},
                    push: function (pk, col, v) {          // the DOMAIN feed (ticks)
                        data[pk][col] = v;
                        subs.slice().forEach(function (fn) { fn(pk, col, v); });
                    },
                    update: function (pk, col, v) {        // the single write seam
                        updates.push(pk + '.' + col + '=' + v);
                        this.push(pk, col, v);
                    },
                    deleteRows: function (pks) {
                        deleted.push(pks.join(','));
                        pks.forEach(function (pk) { delete data[pk]; });
                    }
                };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container, adapter: adapter,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    label: 'Dish list',
                    editable: optsIn.editable,
                    cellFactory: function (column, value) {
                        if (column === 'style')      return new EnumCell({ options: STYLES });
                        if (column === 'price')      return new NumberCell({
                            type: numberType('price', { min: 0 }),
                            format: function (v) { return '$' + Number(v).toFixed(2); } });
                        if (column === 'calories')   return new NumberCell({
                            type: numberType('calories', { integer: true, min: 0 }) });
                        if (column === 'popularity') return new NumberCell({
                            type: numberType('popularity', { integer: true, min: 0 }) });
                        return new TextCell();
                    },
                    onCopy: function (tsv) { copies.push(tsv); },
                    onReleaseRequested: function () { released++; },
                    onBulkEditRejected: function (r) { rejected.push(r.reason + ':' + r.names.join('|')); }
                });
                var table = container.children[0];
                function tdAt(i, j) { return table.children[2].children[i].children[j]; }
                function cellEl(i, j) { return tdAt(i, j).children[0]; }
                function text(i, j) { return cellEl(i, j).textContent; }
                function has(i, j, c) { return tdAt(i, j).className.split(' ').indexOf(c) >= 0; }
                function headers() {
                    return table.children[1].children[0].children
                            .map(function (th) { return th.textContent; });
                }
                function pks() {
                    var out = [], m = grid.viewMaps();
                    for (var i = 0; i < m.rows(); i++) out.push(m.pkAt(i));
                    return out;
                }
                function key(k, mods) {
                    mods = mods || {};
                    table.fire('keydown', { key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl,
                                            altKey: !!mods.alt, preventDefault: function () {},
                                            stopPropagation: function () { mods.stopped = true; } });
                }
                function type(s) { for (var i = 0; i < s.length; i++) key(s[i]); }
                function click(i, j, mods) {
                    mods = mods || {};
                    tdAt(i, j).fire('click', { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                function census() {
                    var m = grid.viewMaps(), cur = 0, sel = 0;
                    for (var i = 0; i < m.rows(); i++) for (var j = 0; j < m.cols(); j++) {
                        if (has(i, j, 'hgr-cursor')) cur++;
                        if (has(i, j, 'hgr-sel')) sel++;
                    }
                    return { cursors: cur, sels: sel };
                }
                return { grid: grid, adapter: adapter, data: data, updates: updates,
                         deleted: deleted, copies: copies, rejected: rejected,
                         releases: function () { return released; },
                         container: container, table: table, tdAt: tdAt, cellEl: cellEl,
                         text: text, has: has, headers: headers, pks: pks, key: key,
                         type: type, click: click, census: census };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridComparatorsModule.js", "GridHeaderDragModule.js", "GridColumnOpsModule.js", "GridLayoutModule.js",
                "GridCellsModule.js", "GridCellTypesModule.js", "StockCellsModule.js",
                "GridSelectionModule.js", "GridKeyboardModule.js", "GridEditControllerModule.js",
                "GridBulkOpsModule.js", "GridUpdateBatchModule.js", "GridBulkEditSessionModule.js", "GridViewStateModule.js",
                "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    // ── Selection (rows 1–3) ─────────────────────────────────────────────

    @Test
    void row01_clickOrArrowToACell() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var boot = f.grid.cursor() === '{"pk":"mapo","column":"ingredient"}'
                        && f.census().cursors === 1;
                    f.click(2, 1);
                    var clicked = f.grid.cursor() === '{"pk":"fish","column":"style"}'
                        && f.has(2, 1, 'hgr-cursor');
                    f.key('ArrowDown'); f.key('ArrowRight');
                    return boot && clicked
                        && f.grid.cursor() === '{"pk":"sauer","column":"calories"}'
                        && f.census().cursors === 1;
                })()"""), "row 1: shallow cursor, painted on the td, by click and by arrow");
    }

    @Test
    void row02_shiftExtendsARange() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 1);
                    f.click(2, 2, { shift: true });          // 2x2 by click
                    var byClick = f.census().sels === 4;
                    f.click(0, 0);
                    f.key('ArrowDown', { shift: true });
                    f.key('ArrowRight', { shift: true });    // 2x2 by keyboard
                    return byClick && f.census().sels === 4
                        && f.grid.cursor() === '{"pk":"mapo","column":"ingredient"}';  // anchor holds
                })()"""), "row 2: Shift extends a range from the anchor; the cursor stays");
    }

    @Test
    void row03_ctrlAddsASecondRange() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    f.click(1, 1, { shift: true });
                    f.click(4, 3, { ctrl: true });
                    return JSON.parse(f.grid.selection()).length === 2
                        && f.census().sels === 5 && f.census().cursors === 1;
                })()"""), "row 3: the range LIST — multi-range selection");
    }

    // ── Editing (rows 4–8) ───────────────────────────────────────────────

    @Test
    void row04_enterOrF2OnPriceEditsTheRawForm() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);                            // price, displays $9.50
                    f.key('F2');
                    var raw = f.cellEl(0, 3).children[0].value === '9.5';   // RAW, not display
                    f.cellEl(0, 3).children[0].value = '10.5';
                    f.key('Enter');
                    f.grid.flushNow();
                    return raw && f.text(0, 3) === '$10.50' && !f.grid.isEditing();
                })()"""), "row 4: deep upgrade — the cell mounts its own editor on the raw form");
    }

    @Test
    void row05_enterOnStyleGivesAnEnumEditor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 1);
                    f.key('Enter');
                    var sel = f.cellEl(1, 1).children[0];
                    var isSelect = sel.tagName === 'select' && sel.children.length === 6;
                    sel.value = 'Italian';
                    f.key('Enter');
                    f.grid.flushNow();
                    return isSelect && f.data.coq.style === 'Italian' && f.text(1, 1) === 'Italian';
                })()"""), "row 5: the CELL decides its editor — a select over the closed set");
    }

    @Test
    void row06_escapeMidEditCancels() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 0);
                    f.key('Enter');
                    f.cellEl(0, 0).children[0].value = 'ruined';
                    f.key('Escape');
                    return !f.grid.isEditing() && f.updates.length === 0
                        && f.text(0, 0) === 'tofu'
                        && f.census().cursors === 1;              // back to shallow, cursor intact
                })()"""), "row 6: the grid OWNS Escape while editing — cancel, back to shallow");
    }

    @Test
    void row07_idleEscapeRequestsRelease() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('Enter');                               // an edit owns Escape...
                    f.key('Escape');
                    var duringEdit = f.releases() === 0;          // ...so no release fired
                    f.key('Escape');                              // now idle
                    return duringEdit && f.releases() === 1;
                })()"""), "row 7: idle Escape asks the host to release the pane (the pane's half is RFC 0049's)");
    }

    @Test
    void row08_commitReachesTheDomainByIdentity() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(2, 0);
                    f.key('Enter');
                    f.cellEl(2, 0).children[0].value = 'haddock';
                    f.key('Enter');
                    return f.updates.join(',') === 'fish.ingredient=haddock'
                        && f.data.fish.ingredient === 'haddock';
                })()"""), "row 8: commitEdit yields the value; the grid writes adapter.update(PK, col, v)");
    }

    // ── View operations (rows 10–13) ─────────────────────────────────────

    @Test
    void row10_filterDetachesAndUnfilterReattaches() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var kept = f.cellEl(0, 0), dropped = f.cellEl(1, 0);
                    f.grid.setColumnFilter('style', 'eq', 'Chinese');
                    var filtered = f.pks().join(',') === 'mapo'
                        && kept.parentNode !== null
                        && dropped.parentNode === null;           // DETACHED, not destroyed
                    f.grid.clearFilter();
                    return filtered && f.pks().length === 6
                        && f.cellEl(1, 0) === dropped              // the same element returned
                        && dropped.parentNode !== null;
                })()"""), "row 10: filter detaches cells alive; unfilter re-attaches the same ones");
    }

    @Test
    void row11_hideCaloriesIsAColumnRemap() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.grid.hideColumn('calories');
                    var hidden = f.headers().join(',') === 'ingredient,style,price,popularity'
                        && f.grid.viewMaps().locate('mapo', 'calories') === null;
                    f.grid.showColumn('calories');
                    return hidden && f.headers().indexOf('calories') === 2;   // back in place
                })()"""), "row 11: hide is a j→columnName remap (visibility)");
    }

    @Test
    void row12_reorderPriceBeforeCalories() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var el = f.cellEl(0, 3);                      // mapo's price cell
                    f.grid.reorderColumn('price', 2);
                    return f.headers().join(',') === 'ingredient,style,price,calories,popularity'
                        && f.cellEl(0, 2) === el;                 // the SAME element moved
                })()"""), "row 12: reorder is a j→columnName remap (order); cells re-parent");
    }

    @Test
    void row13_resizeNeverCancelsAnActiveEdit() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);
                    f.key('Enter');
                    var input = f.cellEl(0, 3).children[0];
                    input.value = '99';
                    f.grid.setColumnWidth('price', 180);          // the DIVIDER RULE
                    var survived = f.grid.isEditing()
                        && f.cellEl(0, 3).children[0] === input   // same editor, same buffer
                        && input.value === '99';
                    f.key('Enter');
                    f.grid.flushNow();
                    return survived
                        && f.table.children[0].children[3].style.props['--hgr-col-w'] === '180px'
                        && f.data.mapo.price === 99;
                })()"""), "row 13: column resize is window management — it never cancels an edit");
    }

    // ── Bulk operations (rows 14–15) ─────────────────────────────────────

    @Test
    void row14_copyASelectionAsRawTsv() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);
                    f.click(1, 3, { shift: true });
                    f.key('c', { ctrl: true });
                    return f.copies.join('|') === '480\\t9.5\\n610\\t18'   // RAW, not $9.50
                        && f.text(0, 3) === '$9.50';                       // display untouched
                })()"""), "row 14: TSV over the resolved selection via getValueToCopy — raw values");
    }

    @Test
    void row15_deleteSelectedRowsHandsIdentitiesToTheDomain() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(1, 0);
                    f.click(2, 2, { shift: true });
                    var pks = f.grid.deleteSelectedRows();
                    return pks.join(',') === 'coq,fish'
                        && f.deleted.join(';') === 'coq,fish'      // "these PKs" — no (i,j)
                        && f.pks().join(',') === 'mapo,sauer,burger,carbo'
                        && f.grid.viewMaps().locate('coq', 'price') === null
                        && f.census().cursors === 1;               // selection self-healed
                })()"""), "row 15: delete materialises an IDENTITY SET for the domain");
    }

    // ── Liveness (rows 16–18) ────────────────────────────────────────────

    @Test
    void row16_popularityTicksThroughTheDirectPath() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var tbodyBefore = f.table.children[2];
                    var el = f.cellEl(0, 4);
                    f.adapter.push('mapo', 'popularity', 99);
                    f.grid.flushNow();
                    return f.text(0, 4) === '99'
                        && f.cellEl(0, 4) === el                   // same cell, no re-mint
                        && f.table.children[2] === tbodyBefore;    // no layout involvement
                })()"""), "row 16: domain → ActualCell by (PK, col) directly; no layout, no lookup");
    }

    @Test
    void row17_resortDuringLiveUpdatesAndDeferredWhileEditing() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.grid.sortBy('popularity', 'desc');
                    var sorted = f.pks().join(',') === 'burger,carbo,mapo,coq,fish,sauer';
                    f.adapter.push('sauer', 'popularity', 95);     // a tick under an active sort
                    f.grid.flushNow();
                    var tickedSafely = f.pks().join(',') === 'burger,carbo,mapo,coq,fish,sauer'
                        && f.text(5, 4) === '95';                  // value updated, order untouched
                    // now the policy half: a re-sort DEFERS while an edit is open
                    f.click(0, 0); f.key('Enter');
                    f.grid.sortBy('popularity', 'desc');
                    var deferred = f.pks().join(',') === 'burger,carbo,mapo,coq,fish,sauer';
                    f.key('Escape');                               // ends the edit → queue drains
                    return sorted && tickedSafely && deferred
                        && f.pks().join(',') === 'sauer,burger,carbo,mapo,coq,fish';
                })()"""), "row 17: live ticks are safe under a sort; an auto re-sort waits for the edit");
    }

    @Test
    void row18_resortWithASelectionActive() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(2, 3);                                 // cursor fish/price
                    f.click(4, 3, { shift: true });                // a live range
                    f.grid.sortBy('price', 'desc');
                    return JSON.parse(f.grid.selection()).length === 0        // ranges RESET
                        && f.grid.cursor() === '{"pk":"fish","column":"price"}'  // cursor by IDENTITY
                        && f.census().cursors === 1;                          // invariant never lapses
                })()"""), "row 18: D5 — remaps reset ranges; the cursor survives by identity");
    }

    // ── Confinement, AT, bulk editing (rows 19–23) ───────────────────────

    @Test
    void row19_cursorIsConfinedToTheGrid() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    for (var k = 0; k < 9; k++) { f.key('ArrowUp'); f.key('ArrowLeft'); }
                    var atOrigin = f.grid.cursor() === '{"pk":"mapo","column":"ingredient"}';
                    for (var k2 = 0; k2 < 12; k2++) { f.key('ArrowDown'); f.key('Tab'); }
                    return atOrigin
                        && f.grid.cursor() === '{"pk":"carbo","column":"popularity"}'
                        && f.census().cursors === 1;               // never escaped the widget
                })()"""), "row 19: the fractal keyboard confines the cursor to the grid");
    }

    @Test
    void row20_theStaticReadingPath() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var t = f.table;
                    var nativeSemantics = t.tagName === 'table'
                        && t.children[1].tagName === 'thead'
                        && t.children[1].children[0].children[0].tagName === 'th'
                        && t.children[2].tagName === 'tbody';
                    var named = t.getAttribute('aria-label') === 'Dish list';
                    var noGridRole = t.getAttribute('role') === null;      // D9: NOT role=grid
                    var editableSaysNothing = t.getAttribute('aria-readonly') === null;
                    var ro = fixture({ editable: false });
                    return nativeSemantics && named && noGridRole && editableSaysNothing
                        && ro.table.getAttribute('aria-readonly') === 'true';
                })()"""), "row 20: real table semantics + a name; read-only stated; no interactive ARIA (D9)");
    }

    @Test
    void row21_bulkValueReplacementAcrossASelection() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);
                    f.click(2, 3, { shift: true });                // three price cells
                    f.type('18.5');
                    var preview = f.text(0, 3) === '18.5' + CARET
                        && f.text(2, 3) === '18.5' + CARET;        // live, on every target
                    f.key('Enter');
                    f.grid.flushNow();
                    return preview
                        && f.updates.join(',') === 'mapo.price=18.5,coq.price=18.5,fish.price=18.5'
                        && f.text(1, 3) === '$18.50';              // formatting returns on commit
                })()"""), "row 21: one virtual editor, live preview, N writes (the hotel-bill gesture)");
    }

    @Test
    void row22_mixedEffectiveTypesAreRejected() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);                                 // calories …
                    f.click(1, 3, { shift: true });                // … and price: both NUMBERS
                    f.type('5');
                    return f.rejected.join(',') === 'mixed-types:calories|price'
                        && !f.grid.isEditing() && f.updates.length === 0;
                })()"""), "row 22: identity is by type NAME — number-vs-number still refuses");
    }

    @Test
    void row23_deleteClearsTheSelectedCells() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);
                    f.click(1, 3, { shift: true });
                    f.key('Delete');
                    f.grid.flushNow();
                    return f.data.mapo.calories === null && f.data.coq.price === null
                        && f.data.mapo.ingredient === 'tofu'       // outside the rect: untouched
                        && f.pks().length === 6                    // STRUCTURE untouched
                        && f.census().cursors === 1;               // selection untouched
                })()"""), "row 23: Excel's Delete clears contents — not rows, not structure");
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
