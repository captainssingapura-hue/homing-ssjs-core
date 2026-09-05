package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050-ext6 — sorting through the Relation's meta. A comparator comes
 * from {@code adapter.columnMeta(column).compare} and is MANDATORY: there is
 * no fallback to raw {@code <}, which sorted enums alphabetically, text by
 * code point and nulls wherever they fell.
 *
 * <p>The laws: numbers by value; enums by their DECLARED order; text in
 * natural order (case- and accent-folded, digit runs by value); absent values
 * last in BOTH directions; ties keep base order in both directions (desc
 * reverses the keys, never the ties); a column without a comparator refuses
 * {@code sortBy} and leaves the view untouched, and a restore naming such a
 * column drops the sort as drift rather than failing.</p>
 *
 * <p>The fixture's base order is deliberately NOT the enum order, so an
 * enum sort that "works" by coincidence cannot pass.</p>
 */
class RelationGridSortTest {

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

    /**
     * Six dishes in a base order that is NOT the style order. Columns: dish
     * (text), style (enum by declared order), price (number, one null), note
     * (text with a natural-order case, an accent tie and a null), and raw —
     * a column whose meta declares NO comparator.
     */
    private static final String FIXTURE = """
            var STYLES = ['Chinese', 'French', 'English', 'German', 'USA', 'Italian'];
            function fixture() {
                var data = {
                    coq:    { dish: 'Coq au Vin',    style: 'French',  price: 18,   note: 'item 10', raw: 1 },
                    fish:   { dish: 'fish & chips',  style: 'English', price: 12,   note: 'Éclair',  raw: 2 },
                    mapo:   { dish: 'Mapo Tofu',     style: 'Chinese', price: 9.5,  note: 'item 2',  raw: 3 },
                    burger: { dish: 'Cheeseburger',  style: 'USA',     price: 11,   note: null,      raw: 4 },
                    carbo:  { dish: 'Carbonara',     style: 'Italian', price: 13,   note: 'eclair',  raw: 5 },
                    sauer:  { dish: 'Sauerbraten',   style: 'German',  price: null, note: 'item 1',  raw: 6 }
                };
                var byStyle = compareByOrder(STYLES);
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['dish', 'style', 'price', 'note', 'raw']; },
                    columnMeta: function (c) {
                        if (c === 'style') return { compare: byStyle };
                        if (c === 'price') return { compare: compareNumbers };
                        if (c === 'raw')   return { };                     // declares NO order
                        return { compare: compareText };
                    },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe: function () {}, unsubscribe: function () {}
                };
                var container = makeEl("div");
                var grid = new RelationGrid({
                    container: container,
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter
                });
                function visiblePks() {
                    var out = [], maps = grid.viewMaps();
                    for (var i = 0; i < maps.rows(); i++) out.push(maps.pkAt(i));
                    return out.join(',');
                }
                function headerTexts() {
                    return container.children[0].children[1].children[0].children
                            .map(function (th) { return th.textContent; }).join(',');
                }
                return { grid: grid, visiblePks: visiblePks, headerTexts: headerTexts };
            }
            /** Six rows over TWO styles, with ties on price and qty and one null
             *  price — so a second key has something to decide. Base order is
             *  a..f, deliberately interleaving the styles. */
            function multi() {
                var data = {
                    a: { style: 'French',  price: 18,   qty: 2 },
                    b: { style: 'Chinese', price: 12,   qty: 5 },
                    c: { style: 'French',  price: 9,    qty: 5 },
                    d: { style: 'Chinese', price: null, qty: 1 },
                    e: { style: 'Chinese', price: 12,   qty: 3 },
                    f: { style: 'French',  price: 9,    qty: 1 }
                };
                var byStyle = compareByOrder(STYLES);
                var adapter = {
                    pks:     function () { return Object.keys(data); },
                    columns: function () { return ['style', 'price', 'qty']; },
                    columnMeta: function (c) { return { compare: c === 'style' ? byStyle : compareNumbers }; },
                    get:     function (pk, col) { return data[pk][col]; },
                    subscribe: function () {}, unsubscribe: function () {}
                };
                var grid = new RelationGrid({
                    container: makeEl("div"),
                    branch: { createElement: function (n, t) { return makeEl(t); } },
                    adapter: adapter
                });
                function order() {
                    var out = [], maps = grid.viewMaps();
                    for (var i = 0; i < maps.rows(); i++) out.push(maps.pkAt(i));
                    return out.join(',');
                }
                /** The key list as 'column dir pin' words — the shape under test. */
                function keys() {
                    return grid.viewState().sort.map(function (k) {
                        return k.column + ' ' + k.direction + (k.pinned ? ' pin' : ''); }).join(' | ');
                }
                return { grid: grid, order: order, keys: keys };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridComparatorsModule.js", "GridHeaderDragModule.js", "GridColumnOpsModule.js",
                "GridLayoutModule.js", "GridCellsModule.js", "GridCellTypesModule.js",
                "StockCellsModule.js", "GridSelectionModule.js", "GridKeyboardModule.js",
                "GridEditControllerModule.js", "GridBulkOpsModule.js", "GridUpdateBatchModule.js",
                "GridBulkEditSessionModule.js", "GridViewStateModule.js", "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    // ── the stock comparators, on their own ──────────────────────────────

    @Test
    void stockComparatorsOrderAsDocumented() {
        assertTrue(evalBool("""
                (() => {
                    var byOrder = compareByOrder(['low', 'medium', 'high']);
                    return compareNumbers(2, 10) < 0                     // by value, not '2' > '10'
                        && compareNumbers(NaN, 1) > 0 && compareNumbers(NaN, NaN) === 0
                        && compareText('item 2', 'item 10') < 0          // natural: digit runs by value
                        && compareText('Zebra', 'apple') > 0             // case folded: z after a
                        && compareText('Éclair', 'eclair') === 0         // accent folded
                        && compareText('a', 'ab') < 0                    // the shorter prefix first
                        && byOrder('high', 'low') > 0                    // declared order, not alphabet
                        && byOrder('zzz', 'high') > 0                    // outside the order: after
                        && byOrder('zzz', 'aaa') === 0;                  // outside vs outside: a tie
                })()"""), "the three stock comparators order exactly as their contract says");
    }

    // ── the laws, through the grid ───────────────────────────────────────

    @Test
    void numbersSortByValueWithAbsentValuesLastBothWays() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.sortBy('price', 'asc');
                    var asc = f.visiblePks();
                    g.sortBy('price', 'desc');
                    var desc = f.visiblePks();
                    return asc  === 'mapo,burger,fish,carbo,coq,sauer'    // 9.5 11 12 13 18, null last
                        && desc === 'coq,carbo,fish,burger,mapo,sauer';   // 18 13 12 11 9.5, null STILL last
                })()"""), "a null price is last ascending AND descending — absence is not a small value");
    }

    @Test
    void enumSortsByDeclaredOrderNotAlphabet() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.sortBy('style', 'asc');
                    var asc = f.visiblePks();
                    g.sortBy('style', 'desc');
                    var desc = f.visiblePks();
                    // declared: Chinese French English German USA Italian
                    // alphabet would give: Chinese English French German Italian USA
                    return asc  === 'mapo,coq,fish,sauer,burger,carbo'
                        && desc === 'carbo,burger,sauer,fish,coq,mapo';
                })()"""), "style sorts Chinese, French, English, German, USA, Italian — the declared order");
    }

    @Test
    void textSortsNaturallyCaseAndAccentFoldedTiesKeepBaseOrderInBothDirections() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.sortBy('note', 'asc');
                    var asc = f.visiblePks();
                    g.sortBy('note', 'desc');
                    var desc = f.visiblePks();
                    // folded: eclair(fish) = eclair(carbo) < item 1 < item 2 < item 10 ; null last.
                    // The accent tie keeps BASE order (fish before carbo) ascending AND descending:
                    // desc reverses the keys, never the ties.
                    return asc  === 'fish,carbo,sauer,mapo,coq,burger'
                        && desc === 'coq,mapo,sauer,fish,carbo,burger';
                })()"""), "natural text order; a folded tie keeps base order both ways; null last");
    }

    @Test
    void sortByWithoutAComparatorRefusesAndLeavesTheViewAlone() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var before = f.visiblePks(), msg = '';
                    try { g.sortBy('raw', 'asc'); } catch (e) { msg = String(e.message || e); }
                    return msg.indexOf("no comparator for column 'raw'") >= 0
                        && msg.indexOf('columnMeta') >= 0                // the remedy is in the message
                        && f.visiblePks() === before                     // nothing moved
                        && g.viewState().sort.length === 0;              // nothing was held either
                })()"""), "a column whose meta declares no order cannot be sorted — loudly, and harmlessly");
    }

    @Test
    void aRestoreDropsASortWhoseColumnHasNoComparatorAndKeepsTheRest() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.applyViewState({ sort: [{ column: 'raw', direction: 'asc' }],
                                       columns: { hidden: ['note'] } });
                    return g.viewState().sort.length === 0               // the sort is drift: dropped
                        && f.headerTexts() === 'dish,style,price,raw'     // the hide still applied
                        && f.visiblePks() === 'coq,fish,mapo,burger,carbo,sauer';
                })()"""), "a saved view must never break a grid: the unsortable sort is discarded, the rest lands");
    }

    @Test
    void sortNullRestoresBaseOrder() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    g.sortBy('style', 'desc');
                    g.sortBy(null);
                    return f.visiblePks() === 'coq,fish,mapo,burger,carbo,sauer'
                        && g.viewState().sort.length === 0;
                })()"""), "sortBy(null) is the base order again");
    }

    // ── multi-key: a pinned prefix and one free key ──────────────────────

    @Test
    void aPinnedKeySurvivesTheNextSortWhichOrdersWithinIt() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('style', 'asc');                // Chinese b,d,e then French a,c,f
                    var single = m.order();
                    g.pinSortKey('style', true);
                    g.sortBy('price', 'asc');                // style survives; price decides within
                    // Chinese: b(12), e(12) tie → base order; d(null) LAST within its group
                    // French:  c(9), f(9) tie → base order; a(18)
                    return single  === 'b,d,e,a,c,f'
                        && m.order() === 'b,e,d,c,f,a'
                        && m.keys()  === 'style asc pin | price asc';
                })()"""), "pin style, sort price: grouped by declared style, price within, null last per group");
    }

    @Test
    void theFreeKeyIsReplacedByTheNextSortAndPinnedKeysAreNot() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('style', 'asc'); g.pinSortKey('style', true);
                    g.sortBy('price', 'asc');
                    g.sortBy('qty', 'asc');                  // price (free) goes; style (pinned) stays
                    // Chinese by qty: d(1) e(3) b(5); French: f(1) a(2) c(5)
                    return m.order() === 'd,e,b,f,a,c'
                        && m.keys()  === 'style asc pin | qty asc';
                })()"""), "single-column mode for the unpinned: the free key is replaced, the pin survives");
    }

    @Test
    void pinningTheFreeKeyKeepsItsRankAndAThirdKeyFollows() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('style', 'asc'); g.pinSortKey('style', true);
                    g.sortBy('qty', 'asc');   g.pinSortKey('qty', true);   // it was already last
                    g.sortBy('price', 'asc');                              // a third key, free
                    return m.keys() === 'style asc pin | qty asc pin | price asc'
                        && m.order() === 'd,e,b,f,a,c';                    // qty leaves no ties for price
                })()"""), "pinning time, entry order and rank coincide because the free key is always last");
    }

    @Test
    void sortingAPinnedColumnFlipsItsDirectionInPlace() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('style', 'asc'); g.pinSortKey('style', true);
                    g.sortBy('price', 'asc');
                    g.sortBy('style', 'desc');               // in place: rank 1 kept, price still free
                    // French first: c,f,a ; then Chinese: b,e,d
                    return m.order() === 'c,f,a,b,e,d'
                        && m.keys()  === 'style desc pin | price asc';
                })()"""), "re-sorting a pinned column changes its direction where it stands — the rule we settled");
    }

    @Test
    void unpinTheOnlyKeyAndItIsFreeUnpinOneOfSeveralAndItLeaves() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('price', 'asc'); g.pinSortKey('price', true);
                    g.pinSortKey('price', false);
                    var alone = m.keys();                    // the only key: simply free again
                    g.pinSortKey('price', true);
                    g.sortBy('qty', 'asc'); g.pinSortKey('qty', true);
                    g.sortBy('style', 'asc');                // price* qty* style
                    g.pinSortKey('price', false);            // one of several: gone
                    // qty asc with style tiebreak: d,f (1): Chinese d before French f; a(2); e(3); b,c (5): b Chinese first
                    return alone === 'price asc'
                        && m.keys()  === 'qty asc pin | style asc'
                        && m.order() === 'd,f,a,e,b,c';
                })()"""), "'pinned' means survives the next click: with none pending it is just free, otherwise it goes");
    }

    @Test
    void absentValuesGoLastPerKeyNotJustOnTheFirst() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('price', 'asc'); g.pinSortKey('price', true);
                    g.sortBy('qty', 'asc');                  // price*, then qty
                    // c(9),f(9): qty f1 < c5 → f,c ; b(12),e(12): e3 < b5 → e,b ; a(18) ; d(null price) LAST whatever its qty
                    return m.order() === 'f,c,e,b,a,d';
                })()"""), "a null on the primary key is last regardless of a small secondary value");
    }

    @Test
    void removeSortKeyClosesTheRanksAndSortNullClearsPinsToo() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy('style', 'asc'); g.pinSortKey('style', true);
                    g.sortBy('qty', 'asc');   g.pinSortKey('qty', true);
                    g.sortBy('price', 'asc');
                    g.removeSortKey('qty');
                    var closed = m.keys();
                    g.sortBy(null);
                    return closed === 'style asc pin | price asc'
                        && m.keys() === '' && m.order() === 'a,b,c,d,e,f';
                })()"""), "removing a middle key keeps the others in order; sortBy(null) is the nuclear option");
    }

    @Test
    void theListSetterIsAbsoluteAndNormalisedAndTheSnapshotRoundTrips() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    g.sortBy([{ column: 'qty', direction: 'desc' }, { column: 'style', direction: 'asc' }]);
                    // qty desc: b,c (5) → style: b Chinese, c French ; e(3) ; a(2) ; d,f (1) → d, f
                    var set = m.order() === 'b,c,e,a,d,f' && m.keys() === 'qty desc pin | style asc';
                    var saved = JSON.parse(JSON.stringify(g.viewState()));
                    g.sortBy(null);
                    g.applyViewState(saved);
                    return set && m.keys() === 'qty desc pin | style asc' && m.order() === 'b,c,e,a,d,f';
                })()"""), "the setter pins every non-last key; a snapshot restores pins and ranks exactly");
    }

    @Test
    void aRestoreDropsDeadKeysKeepsTheRestAndPinsAnOlderSavesPrefix() {
        assertTrue(evalBool("""
                (() => {
                    var m = multi(), g = m.grid;
                    // an older grid's save: two keys, no pinned flags, one naming a vanished column
                    g.applyViewState({ sort: [{ column: 'style', direction: 'asc' },
                                              { column: 'gone',  direction: 'asc' },
                                              { column: 'price', direction: 'asc' }] });
                    var restored = m.keys();                 // gone dropped; style pinned as the prefix
                    g.sortBy('qty', 'asc');                  // ...so the next click keeps style
                    return restored  === 'style asc pin | price asc'
                        && m.keys()  === 'style asc pin | qty asc';
                })()"""), "restore is drift-tolerant per key and re-imposes the pinned-prefix invariant");
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
