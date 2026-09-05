package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 — the VIEWPORT FOLLOW: a keyboard move that leaves the visible
 * band has to bring the band with it. The cursor was already correct before
 * this suite existed; it was simply being painted somewhere nobody could see,
 * which reads as a frozen grid.
 *
 * <p>The DOM stub here is geometric — a deterministic scrollport, 20px rows,
 * 100px columns, a 24px header band pinned to the top edge — so the follow is
 * checked as arithmetic rather than as "something scrolled". That matters most
 * for the sticky header: the naive fix (native scrollIntoView) parks an
 * upward-moving cursor UNDERNEATH the band, which looks like the cursor
 * vanished, and is the reason the inset exists.</p>
 *
 * <p>What the follow deliberately does NOT do is covered just as closely:
 * Ctrl+A and view remaps move nothing, and a grid the keyboard has left moves
 * nothing — a background grid that yanks the page out from under someone is a
 * worse defect than the one being fixed here.</p>
 */
class RelationGridRevealTest {

    private Context js;

    /**
     * The DOM stub, geometry included. One scrollport (PORT, or the window
     * when PORT is null) sitting at the viewport origin; a td's rect follows
     * from its row/column index, and the thead's is pinned to the port's top
     * edge the way position:sticky pins it.
     */
    private static final String DOM_STUB = """
            var ROW_H = 20, COL_W = 100, HEAD_H = 24;
            var PORT = null;                    // the element scrollport, or null
            var WIN = { x: 0, y: 0 };           // the window's scroll offsets

            function scrollY() { return PORT ? PORT.scrollTop  : WIN.y; }
            function scrollX() { return PORT ? PORT.scrollLeft : WIN.x; }
            function indexIn(el) {
                var p = el.parentNode;
                return p ? p.children.indexOf(el) : -1;
            }
            function rectOf(el) {
                if (el.tagName === 'thead') {          // STICKY: pinned to the top edge
                    return { top: 0, bottom: HEAD_H, left: 0, right: COL_W,
                             width: COL_W, height: HEAD_H };
                }
                if (el.tagName === 'td') {
                    var j = indexIn(el), i = indexIn(el.parentNode);
                    var t = HEAD_H + i * ROW_H - scrollY(), l = j * COL_W - scrollX();
                    return { top: t, bottom: t + ROW_H, left: l, right: l + COL_W,
                             width: COL_W, height: ROW_H };
                }
                return { top: 0, bottom: el.clientHeight, left: 0, right: el.clientWidth,
                         width: el.clientWidth, height: el.clientHeight };
            }

            function makeEl(tag) {
                return {
                    tagName: tag, id: "", className: "", textContent: "",
                    children: [], parentNode: null, listeners: {}, attrs: {},
                    scrollTop: 0, scrollLeft: 0, clientTop: 0, clientLeft: 0,
                    clientWidth: 0, clientHeight: 0, scrollWidth: 0, scrollHeight: 0,
                    getBoundingClientRect: function () { return rectOf(this); },
                    contains: function (o) {
                        while (o) { if (o === this) return true; o = o.parentNode; }
                        return false;
                    },
                    focus: function () { document.activeElement = this; },
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
                activeElement: null,
                createElement: function (tag) { return makeEl(tag); },
                getElementById: function (id) {
                    for (var i = 0; i < this.head.children.length; i++)
                        if (this.head.children[i].id === id) return this.head.children[i];
                    return null;
                }
            };
            // No getComputedStyle here: the overflow measurement then stands on
            // its own, which is the fallback branch a headless host takes.
            var window = {
                innerWidth: 250, innerHeight: 124,
                scrollBy: function (dx, dy) { WIN.x += dx; WIN.y += dy; }
            };
            var console = console || { error: function () {} };
            """;

    /**
     * 40 rows x 6 columns behind a port five rows tall and two and a half
     * columns wide, so every edge is a few keystrokes away. {@code opts.window}
     * drops the element scrollport so the window becomes the one of last
     * resort; {@code opts.sticky} turns the frozen header band off.
     */
    private static final String FIXTURE = """
            function fixture(opts) {
                opts = opts || {};
                var data = {}, pks = [];
                var COLS = ['a', 'b', 'c', 'd', 'e', 'f'];
                for (var r = 0; r < 40; r++) {
                    var pk = 'r' + r; pks.push(pk); data[pk] = {};
                    COLS.forEach(function (c) { data[pk][c] = c + r; });
                }
                var adapter = {
                    pks:     function () { return pks; },
                    columns: function () { return COLS; },
                    // ext6 — the Relation declares its orderings; a comparator is mandatory to sort
                    columnMeta: function (c) { return { compare: compareText }; },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe: function () {}, unsubscribe: function () {}
                };
                var container = makeEl("div");
                if (!opts.window) {                     // the element scrollport
                    container.clientHeight = 124;       // HEAD_H + 5 rows
                    container.clientWidth  = 250;       // 2.5 columns
                    container.scrollHeight = 24 + 40 * 20;
                    container.scrollWidth  = 6 * 100;
                    PORT = container;
                } else {
                    PORT = null;                        // the window takes over
                }
                WIN = { x: 0, y: 0 };
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter,
                    header: { sticky: opts.sticky !== false }
                });
                var table = container.children[0];
                document.activeElement = table;         // the keyboard is in the grid
                function tdAt(i, j) {
                    var body = table.children[table.children.length - 1];
                    return body.children[i].children[j];
                }
                function key(k, mods) {
                    mods = mods || {};
                    table.fire('keydown', {
                        key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl,
                        altKey: !!mods.alt, preventDefault: function () {}
                    });
                }
                function at() { return { y: scrollY(), x: scrollX() }; }
                /** Is this slot wholly inside the port, header band excluded? */
                function shows(i, j) {
                    var r = tdAt(i, j).getBoundingClientRect();
                    var top = (opts.sticky === false) ? 0 : 24;
                    var h = opts.window ? window.innerHeight : container.clientHeight;
                    var w = opts.window ? window.innerWidth  : container.clientWidth;
                    return r.top >= top && r.bottom <= h && r.left >= 0 && r.right <= w;
                }
                function cursorIJ() {
                    var c = JSON.parse(grid.cursor());
                    return grid.viewMaps().locate(c.pk, c.column);
                }
                return { grid: grid, container: container, table: table, tdAt: tdAt,
                         key: key, at: at, shows: shows, cursorIJ: cursorIJ };
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
                "GridBulkOpsModule.js", "GridUpdateBatchModule.js", "GridBulkEditSessionModule.js",
                "GridViewStateModule.js", "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    // ── the defect ───────────────────────────────────────────────────────

    @Test
    void arrowingBelowTheFoldBringsTheViewportAlong() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    for (var k = 0; k < 12; k++) f.key('ArrowDown');   // row 12, well past row 4
                    var ij = f.cursorIJ();
                    return ij.i === 12 && f.at().y > 0 && f.shows(12, 0);
                })()"""), "the cursor that walks past the fold drags the scrollport with it");
    }

    @Test
    void theFollowIsTheLeastMovementThatWorks() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    for (var k = 0; k < 5; k++) f.key('ArrowDown');    // rows 0-4 show; 5 is one past
                    var oneRow = f.at().y === 20;                      // exactly one row, not a jump
                    f.key('ArrowDown');
                    return oneRow && f.at().y === 40;
                })()"""), "stepping one row past the edge scrolls exactly one row");
    }

    @Test
    void nothingMovesWhileTheCursorAlreadyShows() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('ArrowDown'); f.key('ArrowDown'); f.key('ArrowRight');
                    return f.at().y === 0 && f.at().x === 0;           // rows 0-4 / cols 0-1 visible
                })()"""), "a move inside the visible band scrolls nothing at all");
    }

    // ── the sticky header, which is where the naive fix fails ────────────

    @Test
    void theStickyBandNeverEclipsesTheCursorOnTheWayBackUp() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    for (var k = 0; k < 20; k++) f.key('ArrowDown');
                    for (var u = 0; u < 19; u++) f.key('ArrowUp');     // back to row 1
                    var r = f.tdAt(1, 0).getBoundingClientRect();
                    // Flush BELOW the 24px band, not underneath it: scrollIntoView
                    // would land this at 0 and hide the cursor behind the header.
                    return f.cursorIJ().i === 1 && r.top === 24 && f.shows(1, 0);
                })()"""), "the upward follow stops at the header's lower edge, never under it");
    }

    @Test
    void withoutAStickyBandTheTopEdgeIsTheTopEdge() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ sticky: false });
                    for (var k = 0; k < 20; k++) f.key('ArrowDown');
                    for (var u = 0; u < 19; u++) f.key('ArrowUp');
                    return f.tdAt(1, 0).getBoundingClientRect().top === 0;
                })()"""), "no band, no inset: an unfrozen header leaves the whole port usable");
    }

    // ── the other axis ───────────────────────────────────────────────────

    @Test
    void tabbingPastTheRightEdgeFollowsSideways() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('Tab'); f.key('Tab');                        // to column 2, half off
                    var moved = f.at().x === 50 && f.shows(0, 2);
                    f.key('End');                                      // to the last column
                    return moved && f.at().x === 350 && f.shows(0, 5);
                })()"""), "the horizontal follow tracks Tab and End across the right edge");
    }

    // ── extend follows the edge the user is dragging ─────────────────────

    @Test
    void shiftExtendFollowsTheRangeFocusNotTheStationaryCursor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    for (var k = 0; k < 12; k++) f.key('ArrowDown', { shift: true });
                    // The cursor never left row 0 — the FOCUS is what the user is
                    // dragging, so the focus is what the viewport chases.
                    return f.cursorIJ().i === 0 && f.at().y > 0 && f.shows(12, 0);
                })()"""), "a shift-extend follows the range's moving edge, not the parked cursor");
    }

    // ── and what must NOT move ───────────────────────────────────────────

    @Test
    void selectAllAndRemapsLeaveTheViewportWhereItWas() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    for (var k = 0; k < 12; k++) f.key('ArrowDown');
                    var parked = f.at().y;
                    f.key('a', { ctrl: true });                        // Ctrl+A: no destination
                    var afterAll = f.at().y === parked;
                    f.grid.sortBy('a', 'desc');                        // a remap is not a journey
                    return afterAll && f.at().y === parked;
                })()"""), "Ctrl+A and a remap are not places the user asked to be taken");
    }

    @Test
    void aGridTheKeyboardHasLeftDoesNotMoveThePage() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    document.activeElement = makeEl('input');          // focus is elsewhere
                    for (var k = 0; k < 12; k++) f.key('ArrowDown');
                    return f.cursorIJ().i === 12 && f.at().y === 0;    // cursor moved, port did not
                })()"""), "a background grid never yanks the scrollport out from under someone");
    }

    @Test
    void aProgrammaticSelectCellFollowsRegardless() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    document.activeElement = makeEl('input');          // focus is elsewhere
                    f.grid.selectCell('r30', 'a');                     // ...but this one MEANT it
                    return f.at().y > 0 && f.shows(30, 0);
                })()"""), "selectCell is a deliberate destination, so it follows unfocused too");
    }

    // ── the scrollport of last resort ────────────────────────────────────

    @Test
    void theWindowScrollsWhenNoAncestorWill() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ window: true });                 // no element scrollport
                    for (var k = 0; k < 12; k++) f.key('ArrowDown');
                    return WIN.y > 0 && f.shows(12, 0);
                })()"""), "with nothing else scrolling, the window carries the follow");
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
