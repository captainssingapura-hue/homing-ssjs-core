package hue.captains.singapura.js.homing.grid;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0050 Phase 2 — static render: two branches, one visual tree. The Dish
 * List renders through the facade over a stub adapter and a stub DOM; the
 * exit criterion is the OWNERSHIP law: layout re-renders mint fresh slots but
 * every cell keeps its element identity (one appendChild moves it), because
 * the cells branch — not the layout — owns cell life.
 */
class RelationGridStaticTest {

    private Context js;

    /** Minimal DOM + DomOpsParty-branch stubs: just what the grid family touches. */
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

    /** The Dish List (RFC 0050 Appendix B) as an in-memory RelationAdapter. */
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
                    push: function (pk, col, v) {   // the domain-side change feed
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
                return { grid: grid, adapter: adapter, container: container,
                         branch: branch, mints: function () { return branchMints; } };
            }
            """;

    @BeforeEach
    void setup() {
        js = Context.newBuilder("js").allowAllAccess(false)
                .option("js.ecmascript-version", "2022").build();
        eval(DOM_STUB);
        for (String module : new String[]{
                "GridViewMapsModule.js", "GridLayoutModule.js", "GridCellsModule.js",
                "GridCellTypesModule.js", "StockCellsModule.js", "GridSelectionModule.js", "GridKeyboardModule.js", "GridEditControllerModule.js", "GridBulkOpsModule.js", "GridBulkEditSessionModule.js", "GridViewStateModule.js",
                "RelationGridModule.js"}) {
            eval(readJs("/homing/js/hue/captains/singapura/js/homing/grid/" + module));
        }
        eval(FIXTURE);
    }

    @AfterEach
    void teardown() { if (js != null) js.close(); }

    @Test
    void dishListRendersEveryHeaderAndCellValue() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    var table = f.container.children[0];
                    var headerRow = table.children[1].children[0];
                    if (headerRow.children.length !== 4) return false;
                    if (headerRow.children[0].textContent !== 'ingredient') return false;
                    var tbody = table.children[2];
                    if (tbody.children.length !== 6) return false;
                    for (var i = 0; i < maps.rows(); i++) for (var j = 0; j < maps.cols(); j++) {
                        var td = tbody.children[i].children[j];
                        if (td.children.length !== 1) return false;          // exactly one cell placed
                        var id = maps.resolve(i, j);
                        if (td.children[0].textContent !== String(f.adapter.get(id.pk, id.column)))
                            return false;
                    }
                    return f.mints() === 24;   // 6 rows x 4 cols, minted through the branch
                })()"""), "the static Dish List must show every header and value");
    }

    @Test
    void layoutReRenderPreservesCellElementIdentity() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    var at = maps.locate('carbo', 'price');
                    var el = f.container.children[0].children[2]
                                .children[at.i].children[at.j].children[0];
                    maps.setRowView(['carbo', 'burger', 'sauer', 'fish', 'coq', 'mapo']);
                    var at2 = maps.locate('carbo', 'price');
                    var el2 = f.container.children[0].children[2]
                                .children[at2.i].children[at2.j].children[0];
                    return at.i === 5 && at2.i === 0     // the row really moved
                        && el === el2                    // ...but the cell ELEMENT is the same object
                        && f.mints() === 24;             // no cell was re-minted by the re-render
                })()"""), "slots are minted fresh; cell elements are moved, never re-created");
    }

    @Test
    void filteredCellsDetachAliveAndReturnCurrent() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    maps.setRowView(['mapo']);                        // filter to one row
                    var visibleRows = f.container.children[0].children[2].children.length;
                    f.adapter.push('coq', 'price', 21);               // update a FILTERED-OUT cell
                    maps.resetRowView();                              // unfilter
                    var at = maps.locate('coq', 'price');
                    var el = f.container.children[0].children[2]
                                .children[at.i].children[at.j].children[0];
                    return visibleRows === 1
                        && el.textContent === '21'      // the detached cell updated while hidden
                        && f.mints() === 24;            // and was never destroyed or re-minted
                })()"""), "filter detaches; detached cells update and re-place current");
    }

    @Test
    void directUpdatePathReachesTheCellWithoutARender() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    var at = maps.locate('mapo', 'price');
                    var tbody = f.container.children[0].children[2];
                    var el = tbody.children[at.i].children[at.j].children[0];
                    f.adapter.push('mapo', 'price', 10.5);
                    return el.textContent === '10.5'
                        && tbody === f.container.children[0].children[2];   // no structural re-render
                })()"""), "a domain push must land in the cell directly, no layout work");
    }

    @Test
    void defaultFactoryPicksNumberCellForNumbersTextCellOtherwise() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid;
                    var price = g._cells.get('coq', 'price').cell;
                    var style = g._cells.get('coq', 'style').cell;
                    return price instanceof NumberCell && style instanceof TextCell
                        && price.getValue() === 18                  // raw
                        && price.getValueToCopy() === '18'          // clipboard = raw as text
                        && style.getValueToCopy() === 'French';
                })()"""), "numbers get NumberCell (raw-value clipboard), the rest TextCell");
    }

    @Test
    void rowLeavingTheRelationIsTheOnlyCellDeath() {
        assertTrue(evalBool("""
                (() => {
                    var f = fixture(), g = f.grid, maps = g.viewMaps();
                    maps.setRowView(['fish', 'mapo']);        // sorted, filtered view
                    g.removeRow('mapo');                      // the row leaves the Relation
                    var tbody = f.container.children[0].children[2];
                    return g._cells.size() === 20             // 24 - 4: only mapo's cells died
                        && g._cells.get('mapo', 'price') === null
                        && maps.rowOf('mapo') === -1
                        && tbody.children.length === 1        // the view kept only fish
                        && g._cells.get('coq', 'price') !== null;   // filtered-out cells LIVE
                })()"""), "removeRow disposes exactly that row's cells; nothing else dies");
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
