package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 — the VIRTUAL bulk-edit session over EffectiveTypes: value
 * replacement with no input element. Fine-grained type identity gates
 * homogeneity ({@code price} ≠ {@code calories} though both are numbers);
 * opening chars come from the type; buffered sessions carry a caret the
 * cells render; validation fans the error paint; instantaneous types (the
 * mine-tile mode) commit on the opening keystroke. Cells stay SHALLOW
 * throughout and the table never loses native focus.
 */
class RelationGridVirtualEditTest {

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
            var CARET = '\\u258F';
            """;

    private static final String FIXTURE = """
            var STYLES = ['Chinese', 'French', 'English', 'German', 'USA', 'Italian'];
            function fixture(optsIn) {
                optsIn = optsIn || {};
                var data = {
                    mapo:  { ingredient: 'tofu',    style: 'Chinese', calories: 480, price: 9.5 },
                    coq:   { ingredient: 'chicken', style: 'French',  calories: 610, price: 18  },
                    fish:  { ingredient: 'cod',     style: 'English', calories: 560, price: 12  },
                    sauer: { ingredient: 'pork',    style: 'German',  calories: 650, price: 14  }
                };
                var subs = [], updates = [], rejected = [], bulk = [];
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
                    update: function (pk, col, v) {
                        updates.push(pk + ':' + col + '=' + v);
                        this.push(pk, col, v);
                    }
                };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter,
                    editable: optsIn.editable,
                    // FINE-GRAINED types: price and calories are both numbers
                    // but distinct EffectiveTypes — the whole point of D-typing.
                    cellFactory: optsIn.cellFactory || function (column, value) {
                        if (column === 'style')    return new EnumCell({ options: STYLES });
                        if (column === 'price')    return new NumberCell({
                            type: numberType('price', { min: 0 }),
                            format: function (v) { return '$' + v.toFixed(2); } });
                        if (column === 'calories') return new NumberCell({
                            type: numberType('calories', { integer: true, min: 0 }) });
                        return new TextCell();
                    },
                    onBulkEditRejected:  function (r) { rejected.push(r.reason + ':' + r.names.join('|')); },
                    onBulkEditCommitted: function (ids, v) { bulk.push(ids.length + '=' + v); }
                });
                function tdAt(i, j) { return container.children[0].children[2].children[i].children[j]; }
                function cellText(i, j) { return tdAt(i, j).children[0].textContent; }
                function hasClass(td, c) { return td.className.split(' ').indexOf(c) >= 0; }
                function key(k, mods) {
                    mods = mods || {};
                    container.children[0].fire('keydown', {
                        key: k, shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl,
                        preventDefault: function () {}
                    });
                }
                function type(s) { for (var i = 0; i < s.length; i++) key(s[i]); }
                function click(i, j, mods) {
                    mods = mods || {};
                    tdAt(i, j).fire('click', { shiftKey: !!mods.shift, ctrlKey: !!mods.ctrl });
                }
                return { grid: grid, adapter: adapter, data: data, updates: updates,
                         rejected: rejected, bulk: bulk, tdAt: tdAt, cellText: cellText,
                         hasClass: hasClass, key: key, type: type, click: click };
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
    void typeToReplaceFansTheCommitLiveOverTheSelection() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);                        // price column, rows 0-2
                    f.click(2, 3, { shift: true });
                    f.type('12.5');                       // opening char starts the session
                    var previews = f.cellText(0, 3) === '12.5' + CARET
                        && f.cellText(1, 3) === '12.5' + CARET
                        && f.cellText(2, 3) === '12.5' + CARET
                        && f.grid.isEditing();            // the SESSION is the editing state
                    f.key('Enter');
                    f.grid.flushNow();
                    return previews
                        && f.updates.join(',') === 'mapo:price=12.5,coq:price=12.5,fish:price=12.5'
                        && f.cellText(1, 3) === '$12.50'  // formatting returns on commit
                        && f.bulk.join(',') === '3=12.5'
                        && !f.grid.isEditing();
                })()"""), "typing replaces: live raw preview on every cell, one commit fans out");
    }

    @Test
    void caretArrowsAndBackspaceEditTheSharedBuffer() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);
                    f.click(1, 3, { shift: true });
                    f.type('15');
                    f.key('ArrowLeft');                   // caret between 1 and 5
                    f.type('2');                          // 125
                    var mid = f.cellText(0, 3) === '12' + CARET + '5';
                    f.key('Backspace');                   // back to 15
                    f.key('Home'); f.key('Delete');       // 5
                    var edited = f.cellText(1, 3) === CARET + '5';
                    f.key('Enter');
                    return mid && edited
                        && f.updates.join(',') === 'mapo:price=5,coq:price=5';
                })()"""), "the caret is real: arrows/Home/Backspace/Delete edit the buffer");
    }

    @Test
    void invalidBufferPaintsTheErrorAndEnterNoops() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);                        // calories (integer type), rows 0-1
                    f.click(1, 2, { shift: true });
                    f.type('5x');                         // not a number
                    var painted = f.hasClass(f.tdAt(0, 2), 'hgr-invalid')
                        && f.hasClass(f.tdAt(1, 2), 'hgr-invalid');
                    f.key('Enter');                       // decision 1: no-op while invalid
                    var held = f.grid.isEditing() && f.updates.length === 0;
                    f.key('Backspace');                   // '5' — valid again
                    var cleared = !f.hasClass(f.tdAt(0, 2), 'hgr-invalid');
                    f.key('Enter');
                    return painted && held && cleared
                        && f.updates.join(',') === 'mapo:calories=5,coq:calories=5';
                })()"""), "invalid fans the error paint to the whole selection; Enter waits");
    }

    @Test
    void mixedEffectiveTypesReportAnError() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 2);                        // calories + price: BOTH numbers,
                    f.click(1, 3, { shift: true });       // but DIFFERENT EffectiveTypes
                    f.type('5');
                    return f.rejected.join(',') === 'mixed-types:calories|price'
                        && !f.grid.isEditing()
                        && f.updates.length === 0;
                })()"""), "fine-grained identity: number-vs-number still rejects across types");
    }

    @Test
    void escapeCancelsRestoringEveryDisplay() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);
                    f.click(1, 3, { shift: true });
                    f.type('99');
                    f.key('Escape');
                    return f.cellText(0, 3) === '$9.50' && f.cellText(1, 3) === '$18.00'
                        && f.updates.length === 0 && !f.grid.isEditing();
                })()"""), "cancel restores the cells' own values; nothing reached the domain");
    }

    @Test
    void enumTypePrefixMatchesItsClosedSet() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 1);                        // style, rows 0-1
                    f.click(1, 1, { shift: true });
                    f.type('G');                          // unique prefix of German
                    var valid = !f.hasClass(f.tdAt(0, 1), 'hgr-invalid');
                    f.key('Enter');
                    return valid
                        && f.data.mapo.style === 'German' && f.data.coq.style === 'German';
                })()"""), "the enum type validates by unique prefix over its options");
    }

    @Test
    void instantaneousTypeCommitsOnTheOpeningKeystroke() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture({ cellFactory: function (column, value) {
                        return new TextCell({ type: instantType('tile', ['f', 'o']) });
                    } });
                    f.click(1, 1);
                    f.click(2, 2, { shift: true });       // 2x2 tiles
                    f.key('f');                           // the keystroke IS the edit
                    var flagged = f.updates.length === 4
                        && f.updates[0] === 'coq:style=f'
                        && !f.grid.isEditing();           // never went deep, no session held
                    f.key('x');                           // not an opening char: ignored
                    return flagged && f.updates.length === 4;
                })()"""), "mine-tile mode: open + commit + close in one keystroke, all shallow");
    }

    @Test
    void domainTickDuringPreviewNeverClobbersAndCancelShowsIt() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture();
                    f.click(0, 3);                        // SINGLE cell: type-to-replace works too
                    f.type('7');
                    f.adapter.push('mapo', 'price', 99);  // tick mid-session
                    f.grid.flushNow();
                    var previewHeld = f.cellText(0, 3) === '7' + CARET;
                    f.key('Escape');
                    return previewHeld
                        && f.cellText(0, 3) === '$99.00'; // cancel reveals the TICK value
                })()"""), "preview shields the tick; cancel lands on current domain truth");
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
