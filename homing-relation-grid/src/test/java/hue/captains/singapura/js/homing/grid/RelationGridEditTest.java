package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 Phase 5 — deep edit + actions + citizenship. Dish List rows 4, 5,
 * 6, 7, 8, 13, 17 keyboard-only: Enter/F2 begins, Enter commits TO THE
 * ADAPTER, Escape cancels, at most one editing cell, arrows stay with the
 * editor while deep, onAction on editing-disabled grids, idle Escape requests
 * release (RFC 0049 citizenship), and D7 — a re-sort during an edit defers
 * until the edit ends.
 */
class RelationGridEditTest {

    private Context js;

    private static final String DOM_STUB = """
            function makeEl(tag) {
                return {
                    tagName: tag, id: "", className: "", textContent: "",
                    children: [], parentNode: null, listeners: {}, attrs: {}, value: "",
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
            var STYLES = ['Chinese', 'French', 'English', 'German', 'USA', 'Italian'];
            function fixture(optsIn) {
                optsIn = optsIn || {};
                var data = {
                    mapo:   { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5 },
                    coq:    { ingredient: 'chicken', style: 'French',  calories: 610, price: 18  },
                    fish:   { ingredient: 'cod',     style: 'English', calories: 560, price: 12  },
                    sauer:  { ingredient: 'pork',    style: 'German',  calories: 650, price: 14  }
                };
                var subs = [], updates = [];
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['ingredient', 'style', 'calories', 'price']; },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe:   function (fn) { subs.push(fn); },
                    unsubscribe: function () {},
                    push: function (pk, col, v) {
                        data[pk][col] = v;
                        subs.forEach(function (fn) { fn(pk, col, v); });
                    },
                    update: function (pk, col, v) {        // the commit target — echoes back
                        updates.push(pk + ':' + col + '=' + v);
                        this.push(pk, col, v);
                    }
                };
                var events = { started: [], committed: [], actions: [], releases: 0 };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter,
                    editable: optsIn.editable,
                    cellFactory: function (column, value) {
                        if (column === 'style') return new EnumCell({ options: STYLES });
                        return (typeof value === 'number') ? new NumberCell() : new TextCell();
                    },
                    onAction:      function (k, pk, col) { events.actions.push(k + '@' + pk + ':' + col); },
                    onEditStarted: function (pk, col) { events.started.push(pk + ':' + col); },
                    onEditCommitted: function (pk, col, v) { events.committed.push(pk + ':' + col + '=' + v); },
                    onReleaseRequested: function () { events.releases++; }
                });
                function tdAt(i, j) { return container.children[0].children[1].children[i].children[j]; }
                function cellDiv(i, j) { return tdAt(i, j).children[0]; }
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
                return { grid: grid, adapter: adapter, data: data, updates: updates,
                         events: events, tdAt: tdAt, cellDiv: cellDiv, key: key,
                         click: click, visiblePks: visiblePks };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "StockCellsModule.js", "GridSelectionModule.js", "GridKeyboardModule.js",
                "GridEditControllerModule.js", "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void enterBeginsEditAndCommitWritesTheAdapter() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('Enter');                              // edit at the boot cursor (mapo/ingredient)
                    var editing = f.grid.isEditing()
                        && f.cellDiv(0, 0).children[0].tagName === 'input'
                        && f.events.started.join(',') === 'mapo:ingredient';
                    f.cellDiv(0, 0).children[0].value = 'bean curd';
                    f.key('Enter');                              // commit
                    f.grid.flushNow();                           // drain the echo push
                    return editing
                        && !f.grid.isEditing()
                        && f.updates.join(',') === 'mapo:ingredient=bean curd'
                        && f.events.committed.join(',') === 'mapo:ingredient=bean curd'
                        && f.data.mapo.ingredient === 'bean curd'
                        && f.cellDiv(0, 0).textContent === 'bean curd';
                })()"""), "Enter begins; Enter commits through the ADAPTER, which echoes back");
    }

    @Test
    void escapeCancelsRestoringTheOldValue() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('Enter');
                    f.cellDiv(0, 0).children[0].value = 'ruined';
                    f.key('Escape');
                    return !f.grid.isEditing()
                        && f.updates.length === 0                 // nothing reached the adapter
                        && f.cellDiv(0, 0).textContent === 'tofu'
                        && f.cellDiv(0, 0).children.length === 0; // the editor is gone
                })()"""), "Escape cancels: no adapter write, the old value re-renders");
    }

    @Test
    void atMostOneEditingCellAndArrowsStayWithTheEditor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('Enter');                               // editing mapo/ingredient
                    var before = f.grid.cursor();
                    f.key('ArrowDown'); f.key('ArrowRight');      // deep: shallow keys are BLOCKED
                    var cursorHeld = f.grid.cursor() === before && f.grid.isEditing();
                    f.cellDiv(0, 0).children[0].value = 'silken tofu';
                    f.click(2, 0);                                // clicking away COMMITS, then selects
                    var committed = f.updates.join(',') === 'mapo:ingredient=silken tofu'
                        && !f.grid.isEditing()
                        && f.grid.cursor() === '{"pk":"fish","column":"ingredient"}';
                    f.key('ArrowDown');                           // shallow again: arrows work
                    return cursorHeld && committed
                        && f.grid.cursor() === '{"pk":"sauer","column":"ingredient"}';
                })()"""), "deep => at most one editing cell; arrows belong to the editor while deep");
    }

    @Test
    void enumCellEditsThroughASelectOverTheClosedSet() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 1);                                // cursor mapo/style (EnumCell)
                    f.key('F2');                                  // F2 begins too
                    var sel = f.cellDiv(0, 1).children[0];
                    var isSelect = sel.tagName === 'select' && sel.children.length === 6;
                    sel.value = 'Italian';
                    f.key('Enter');
                    f.grid.flushNow();
                    return isSelect
                        && f.updates.join(',') === 'mapo:style=Italian'
                        && f.data.mapo.style === 'Italian'
                        && f.cellDiv(0, 1).textContent === 'Italian';
                })()"""), "the enum editor is a select over the closed set; commit writes the adapter");
    }

    @Test
    void d7ViewOpsDeferWhileEditingAndDrainAfter() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    f.click(1, 3);                                // coq/price (NumberCell)
                    f.key('Enter');
                    g.sortBy('price', 'asc');                     // D7: DEFERRED
                    g.hideColumn('calories');                     // D7: DEFERRED
                    var whileEditing = f.visiblePks().join(',') === 'mapo,coq,fish,sauer'
                        && g.viewMaps().cols() === 4
                        && g.isEditing();
                    f.cellDiv(1, 3).children[0].value = '16';
                    f.key('Enter');                               // commit -> the queue drains
                    f.grid.flushNow();
                    return whileEditing
                        && f.updates.join(',') === 'coq:price=16'
                        && f.visiblePks().join(',') === 'mapo,fish,sauer,coq'   // sorted with the NEW price
                        && g.viewMaps().cols() === 3;                            // hide applied too
                })()"""), "a re-sort during an edit waits; it lands after commit, seeing the new value");
    }

    @Test
    void actionGridDispatchesInsteadOfEditingAndIdleEscapeReleases() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ editable: false });
                    f.key('Enter');                               // NOT an edit — an ACTION
                    f.key('f');                                   // a letter action
                    var noEditor = !f.grid.isEditing() && f.cellDiv(0, 0).children.length === 0;
                    f.key('Escape');                              // idle Escape -> release request
                    var editable = fixture();
                    editable.key('Escape');                       // same on an editable grid, idle
                    return noEditor
                        && f.events.actions.join(',') === 'Enter@mapo:ingredient,f@mapo:ingredient'
                        && f.events.releases === 1
                        && editable.events.releases === 1;
                })()"""), "editing-disabled grids dispatch onAction; idle Escape asks the host to release");
    }

    @Test
    void directUpdateNeverClobbersAnOpenEditor() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.key('Enter');                               // editing mapo/ingredient
                    var input = f.cellDiv(0, 0).children[0];
                    input.value = 'half typed';
                    f.adapter.push('mapo', 'ingredient', 'from-the-domain');
                    f.grid.flushNow();
                    var editorIntact = f.grid.isEditing()
                        && f.cellDiv(0, 0).children[0] === input
                        && input.value === 'half typed';
                    f.key('Escape');                              // cancel: the DOMAIN value shows
                    return editorIntact
                        && f.cellDiv(0, 0).textContent === 'from-the-domain';
                })()"""), "the direct path updates state, not the editor; cancel reveals the domain value");
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
