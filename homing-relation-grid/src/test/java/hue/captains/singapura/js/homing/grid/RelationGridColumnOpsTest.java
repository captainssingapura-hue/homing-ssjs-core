package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050-ext6 — the caret tier's header affordance, as laws over the
 * className set and the sort list (ext3's method: behaviour checked headlessly,
 * no pixels).
 *
 * <p>The gestures: a click on a sortable header cycles the free key asc → desc
 * → none and replaces any other free key; a pinned header cycles asc ↔ desc
 * only; the pin keeps a key across the next click and never sorts by itself;
 * a click that merely ended a reorder drag is swallowed, once; Alt+↑ / Alt+↓
 * sort the cursor's column and Alt+Shift+↑ pins it. The postures: a column is
 * sortable iff the Relation's meta orders it; {@code columnOps: 'none'} leaves
 * the header inert; {@code multiKey: false} drops the pin.</p>
 */
class RelationGridColumnOpsTest {

    private Context js;

    /** The usual stub, plus what a header drag needs to run headlessly: a rect
     *  on every element, a style with setProperty, a document with listeners
     *  and a body. */
    private static final String DOM_STUB = """
            function makeEl(tag) {
                return {
                    tagName: tag, id: "", className: "", textContent: "",
                    children: [], parentNode: null, listeners: {}, attrs: {},
                    style: { setProperty: function () {}, removeProperty: function () {} },
                    getBoundingClientRect: function () {
                        // Cells sit in 100px columns, so a header drag can be
                        // dropped back on its OWN column; everything else is at 0.
                        var p = this.parentNode, i = (p && p.tagName === 'tr') ? p.children.indexOf(this) : -1;
                        var left = i < 0 ? 0 : i * 100, w = i < 0 ? 0 : 100;
                        return { top: 0, left: left, right: left + w, bottom: 0, width: w, height: 0 }; },
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
            var document = makeEl("#document");
            document.head = makeEl("head");
            document.body = makeEl("body");
            document.createElement = function (tag) { return makeEl(tag); };
            document.getElementById = function (id) {
                for (var i = 0; i < this.head.children.length; i++)
                    if (this.head.children[i].id === id) return this.head.children[i];
                return null;
            };
            var console = console || { error: function () {} };
            // A flushable timer queue: the drag clears its mark "next tick".
            var timers = [];
            var setTimeout = function (fn) { timers.push(fn); return timers.length; };
            function flushTimers() { var t = timers; timers = []; t.forEach(function (fn) { fn(); }); }
            """;

    /** Five columns; 'raw' declares no comparator; the cursor boots on 'dish'. */
    private static final String FIXTURE = """
            var STYLES = ['Chinese', 'French', 'English', 'German', 'USA', 'Italian'];
            function fixture(gridOpts) {
                var data = {
                    coq:    { dish: 'Coq au Vin',   style: 'French',  price: 18,  note: 'b', raw: 1 },
                    fish:   { dish: 'fish & chips', style: 'English', price: 12,  note: 'a', raw: 2 },
                    mapo:   { dish: 'Mapo Tofu',    style: 'Chinese', price: 9.5, note: 'c', raw: 3 }
                };
                var byStyle = compareByOrder(STYLES);
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['dish', 'style', 'price', 'note', 'raw']; },
                    columnMeta: function (c) {
                        if (c === 'style') return { compare: byStyle };
                        if (c === 'price') return { compare: compareNumbers };
                        if (c === 'raw')   return { };
                        return { compare: compareText };
                    },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe: function () {}, unsubscribe: function () {}
                };
                var container = makeEl("div");
                var opts = { container: container,
                             branch: { createElement: function (n, t) { return makeEl(t); } },
                             adapter: adapter };
                Object.keys(gridOpts || {}).forEach(function (k) { opts[k] = gridOpts[k]; });
                var grid = new RelationGrid(opts);
                var table = container.children[0];
                function thAt(j) { return table.children[1].children[0].children[j]; }
                function has(el, c) { return el.className.split(' ').indexOf(c) >= 0; }
                function slotOf(j) {
                    return thAt(j).children.filter(function (c) { return c.className === 'hgr-th-ops'; })[0] || null;
                }
                function partOf(j, cls) {
                    var s = slotOf(j); if (!s) return null;
                    return s.children.filter(function (c) { return c.className === cls; })[0] || null;
                }
                function clickHeader(j) { var th = thAt(j); th.fire('click', { target: th }); }
                function clickPin(j) {
                    var pin = partOf(j, 'hgr-th-pin'); if (!pin) throw new Error('no pin on column ' + j);
                    pin.fire('click', { stopPropagation: function () {} });
                }
                function key(k, mods) {
                    mods = mods || {};
                    table.fire('keydown', { key: k, altKey: !!mods.alt, shiftKey: !!mods.shift,
                                            ctrlKey: false, preventDefault: function () {} });
                }
                function keys() {
                    return grid.viewState().sort.map(function (k) {
                        return k.column + ' ' + k.direction + (k.pinned ? ' pin' : ''); }).join(' | ');
                }
                function order() {
                    var out = [], maps = grid.viewMaps();
                    for (var i = 0; i < maps.rows(); i++) out.push(maps.pkAt(i));
                    return out.join(',');
                }
                /** Drag the header at j by dx (from its own centre) and release,
                 *  headlessly. dx under 50 drops it back on its own column: moved
                 *  past the threshold, no order change — the swallow's case. */
                function drag(j, dx) {
                    var th = thAt(j), x = j * 100 + 50;
                    th.fire('mousedown', { clientX: x });
                    document.fire('mousemove', { clientX: x + dx });
                    document.fire('mouseup', {});
                }
                return { grid: grid, table: table, thAt: thAt, has: has, slotOf: slotOf, partOf: partOf,
                         clickHeader: clickHeader, clickPin: clickPin, key: key, keys: keys,
                         order: order, drag: drag };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridComparatorsModule.js", "GridHeaderDragModule.js",
                "GridColumnOpsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "GridCellTypesModule.js", "StockCellsModule.js", "GridSelectionModule.js",
                "GridKeyboardModule.js", "GridEditControllerModule.js", "GridBulkOpsModule.js",
                "GridUpdateBatchModule.js", "GridBulkEditSessionModule.js", "GridViewStateModule.js",
                "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    // ── postures ─────────────────────────────────────────────────────────

    @Test
    void sortableHeadersWearTheCaretAndUnorderedOnesDoNot() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    var style = f.thAt(1), raw = f.thAt(4);
                    var caret = f.partOf(1, 'hgr-th-caret');
                    f.clickHeader(4);                                          // raw: no comparator
                    return f.has(style, 'hgr-sortable') && caret && caret.textContent === '\\u2195'
                        && style.getAttribute('aria-sort') === 'none'
                        && !f.has(raw, 'hgr-sortable') && f.partOf(4, 'hgr-th-caret') === null
                        && raw.getAttribute('aria-sort') === null
                        && f.keys() === '';                                    // the click did nothing
                })()"""), "sortable iff the Relation's meta orders the column — no comparator, no caret, no click");
    }

    @Test
    void columnOpsNoneLeavesTheHeaderInert() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ columnOps: 'none' });
                    f.clickHeader(1); f.key('ArrowUp', { alt: true });
                    return f.slotOf(1) === null && !f.has(f.thAt(1), 'hgr-sortable') && f.keys() === '';
                })()"""), "columnOps:'none' is today's grid: no slot, no class, no gesture");
    }

    @Test
    void multiKeyFalseDropsThePinButStillSorts() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ multiKey: false });
                    f.clickHeader(1);
                    return f.keys() === 'style asc' && f.partOf(1, 'hgr-th-pin') === null
                        && f.has(f.thAt(1), 'hgr-sorted-asc');
                })()"""), "multiKey:false — the caret without the pin");
    }

    // ── the free key ─────────────────────────────────────────────────────

    @Test
    void clickingAHeaderCyclesTheFreeKeyAscDescNone() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.clickHeader(2);
                    var a = f.keys() === 'price asc' && f.has(f.thAt(2), 'hgr-sorted-asc')
                         && f.thAt(2).getAttribute('aria-sort') === 'ascending'
                         && f.partOf(2, 'hgr-th-caret').textContent === '\\u25B2'
                         && f.order() === 'mapo,fish,coq';
                    f.clickHeader(2);
                    var d = f.keys() === 'price desc' && f.has(f.thAt(2), 'hgr-sorted-desc')
                         && !f.has(f.thAt(2), 'hgr-sorted-asc')
                         && f.thAt(2).getAttribute('aria-sort') === 'descending'
                         && f.partOf(2, 'hgr-th-caret').textContent === '\\u25BC';
                    f.clickHeader(2);
                    var n = f.keys() === '' && !f.has(f.thAt(2), 'hgr-sorted-desc')
                         && f.thAt(2).getAttribute('aria-sort') === 'none'
                         && f.order() === 'coq,fish,mapo';
                    return a && d && n;
                })()"""), "asc, desc, none — the predicates and aria-sort follow the model each time");
    }

    @Test
    void clickingAnotherHeaderReplacesTheFreeKey() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.clickHeader(1); f.clickHeader(2);
                    return f.keys() === 'price asc' && !f.has(f.thAt(1), 'hgr-sorted-asc');
                })()"""), "single-column mode for the unpinned: the next header takes over");
    }

    // ── the pin ──────────────────────────────────────────────────────────

    @Test
    void thePinKeepsAKeyAcrossTheNextClickShowsRanksAndNeverSortsByItself() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.clickHeader(1);
                    var before = f.order();
                    f.clickPin(1);
                    var pinned = f.keys() === 'style asc pin' && f.has(f.thAt(1), 'hgr-sort-pinned')
                              && f.partOf(1, 'hgr-th-pin').getAttribute('aria-pressed') === 'true'
                              && f.order() === before;                          // pinning changed no order
                    f.clickHeader(2);
                    var two = f.keys() === 'style asc pin | price asc'
                           && f.partOf(1, 'hgr-th-rank').textContent === '1'
                           && f.partOf(2, 'hgr-th-rank').textContent === '2';
                    f.clickPin(1);                                              // unpin: another key exists → gone
                    var gone = f.keys() === 'price asc' && f.partOf(2, 'hgr-th-rank') === null;
                    return pinned && two && gone;
                })()"""), "pin, then sort another: two ranked keys; unpin with another present: it leaves");
    }

    @Test
    void aPinnedHeaderCyclesAscDescOnly() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.clickHeader(1); f.clickPin(1);
                    f.clickHeader(1); var d = f.keys();
                    f.clickHeader(1); var a = f.keys();
                    f.clickHeader(1); var d2 = f.keys();
                    return d === 'style desc pin' && a === 'style asc pin' && d2 === 'style desc pin';
                })()"""), "a pinned key never reaches 'none' by clicking — the pin is the one way out");
    }

    @Test
    void pinnedImpliesSortedOnTheSameHeader() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.clickHeader(1); f.clickPin(1); f.clickHeader(2); f.clickPin(2); f.clickHeader(3);
                    var ok = true;
                    for (var j = 0; j < 5; j++) {
                        var th = f.thAt(j);
                        if (f.has(th, 'hgr-sort-pinned') && !(f.has(th, 'hgr-sorted-asc') || f.has(th, 'hgr-sorted-desc'))) ok = false;
                        if (f.has(th, 'hgr-sorted-asc') && f.has(th, 'hgr-sorted-desc')) ok = false;
                    }
                    return ok && f.keys() === 'style asc pin | price asc pin | note asc';
                })()"""), "the ext3 laws: pinned implies sorted; asc and desc exclude each other");
    }

    // ── the drag that is not a click ─────────────────────────────────────

    @Test
    void aClickThatEndedAReorderDragIsSwallowedOnceAndOnlyAtOnce() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.drag(2, 12);                       // price: past the threshold, dropped on itself
                    f.clickHeader(2);                    // the click that ended the drag: swallowed
                    var swallowed = f.keys() === '';
                    f.clickHeader(2);                    // the next one is a click again
                    var sorted = f.keys() === 'price asc';
                    f.drag(1, 12);                       // style: moved, no order change
                    f.clickHeader(3);                    // a click on ANOTHER header is a real click
                    var other = f.keys() === 'note asc';
                    f.drag(1, 12);
                    flushTimers();                       // no click followed: the mark clears next tick
                    f.clickHeader(1);
                    return swallowed && sorted && other && f.keys() === 'style asc';
                })()"""), "a drag marks its header on release; one click on it is swallowed, and only if it comes at once");
    }

    @Test
    void aClickOnTheResizeHandleNeverSorts() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), th = f.thAt(2);
                    var handle = th.children[th.children.length - 1];      // the right-edge control stays last
                    th.fire('click', { target: handle });
                    return handle.className === 'hgr-resize-handle' && f.keys() === '';
                })()"""), "the third gesture on the header: the handle is recognised and its click is not a sort");
    }

    // ── the keyboard ─────────────────────────────────────────────────────

    @Test
    void altChordsSortAndPinTheCursorColumn() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();                   // the cursor boots on (0,0): 'dish'
                    f.key('ArrowUp', { alt: true });     var a = f.keys();
                    f.key('ArrowUp', { alt: true });     var c = f.keys();    // the same chord again clears
                    f.key('ArrowDown', { alt: true });   var d = f.keys();
                    f.key('ArrowUp', { alt: true, shift: true }); var p = f.keys();
                    f.key('ArrowDown', { alt: true });   var still = f.keys();  // pinned: never clears by chord
                    f.key('ArrowUp', { alt: true, shift: true }); var un = f.keys();
                    return a === 'dish asc' && c === '' && d === 'dish desc' && p === 'dish desc pin'
                        && still === 'dish desc pin' && un === 'dish desc'
                        && f.has(f.thAt(0), 'hgr-sorted-desc');
                })()"""), "Alt+↑/↓ sort the cursor's column, again clears a free key; Alt+Shift+↑ toggles the pin");
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
