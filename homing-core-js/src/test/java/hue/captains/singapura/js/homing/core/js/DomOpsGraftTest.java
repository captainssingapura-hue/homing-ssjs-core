package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 E3, grafting: one stationed party per page, every other party
 * mobile - made by the party of parties, working on its own, grafted into the
 * page's tree by a host through a proxy fixed to it. Within any party the
 * levels are as they were; a node at level n of a party grafted at level m
 * stands at level m + n.
 */
class DomOpsGraftTest extends JsModuleTestBase {

    private static final String BASE  = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyBaseModule.js";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyModule.js";

    /** Enough DOM for createElement + remove; removal is counted. */
    private static final String DOM = """
        globalThis.removed = 0;
        globalThis.document = {
            createElement: function (tag) {
                return { tagName: String(tag).toUpperCase(), remove: function () { globalThis.removed++; } };
            }
        };
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", DOM, "dom.js").buildLiteral());
        loadModule(BASE);
        loadModule(PARTY);
        // the page's tree: root(0) > widgets(1) > docks(2); a widget's own party: w(0) > cells(1) > c1(2), elements on each
        js.eval("js", """
                var owners = [];
                function owned(branch, label) { var o = { toString: () => label }; owners.push(o); branch.activate(o); return branch; }
                var widgets = owned(domOpsParty.createBranch('widgets'), 'host:widgets');
                var docks = owned(widgets.createBranch('docks'), 'host:docks');
                var w = owned(domOpsParties.mobile('w'), 'widget:w');
                w.createElement('root', 'div');
                var cells = owned(w.createBranch('cells'), 'widget:cells');
                var c1 = owned(cells.createBranch('c1'), 'widget:c1');
                c1.createElement('cell', 'td');
                function fails(f) { try { f(); return null; } catch (e) { return e.message; } }
                """);
    }

    private String str(String expr) { return js.eval("js", expr).asString(); }
    private int num(String expr) { return js.eval("js", expr).asInt(); }
    private boolean yes(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void thePageHasOneStationedParty_andAnyOtherIsMobile_madeByThePartyOfParties() {
        assertEquals("stationed", str("domOpsParty.kind"));
        assertTrue(yes("domOpsParties.stationed === domOpsParty && domOpsParty instanceof StationedDomOpsParty && domOpsParty instanceof DomOpsParty"));
        assertTrue(str("fails(() => new StationedDomOpsParty('another'))").contains("already"));
        assertTrue(str("fails(() => new DomOpsParty('x', 'mobile'))").contains("Stationed or Mobile"));
        assertTrue(str("fails(() => new MobileDomOpsParty(null, 'x'))").contains("domOpsParties.mobile"));
        assertEquals("mobile", str("w.kind"));
        assertTrue(yes("w instanceof MobileDomOpsParty && domOpsParties.mobiles().includes(w)"));
        assertTrue(str("fails(() => domOpsParties.mobile('w'))").contains("alive already"), "a party's name is its own");
    }

    @Test
    void aMobilePartyWorksOnItsOwn_itsLevelsAsTheyWere_andIsAStrayUntilGrafted() {
        assertEquals(2, num("c1.depth"));
        assertEquals("DomOpsPartyL2", str("c1.constructor.name"), "its level classes count from its own root");
        assertEquals(2, num("c1.level"), "not grafted: its level is its depth");
        assertFalse(yes("w.isGrafted"));
        assertEquals("w", str("domOpsParties.strays()[0].name"));
        assertFalse(yes("JSON.stringify(domOpsParty.snapshot()).includes('widget:w')"), "the page's tree does not see a stray");
    }

    @Test
    void graftedAtLevelM_aNodeAtLevelNStandsAtMPlusN() {
        js.eval("js", "var proxy = docks.graft('w-1', w);");
        assertEquals("proxy", str("proxy.kind"));
        assertEquals(3, num("proxy.level"), "a proxy in docks (level 2) is at level 3");
        assertEquals(3, num("w.level"));
        assertEquals(5, num("c1.level"), "3 + 2");
        assertEquals(2, num("c1.depth"), "its depth in its own party is as it was");
        assertTrue(yes("w.isGrafted"));
        assertEquals(0, num("domOpsParties.strays().length"));
        js.eval("js", "var more = owned(w.createBranch('more'), 'widget:more');");
        assertEquals(1, num("more.depth"), "a branch made after the graft still counts from its own root");
        assertEquals("DomOpsPartyL1", str("more.constructor.name"));
        assertEquals(4, num("more.level"));
    }

    @Test
    void thePagesSnapshotReadsThroughTheProxy_asOneTree() {
        js.eval("js", "docks.graft('w-1', w); var s = domOpsParty.snapshot(); var node = s.branches[0].branches[0].branches[0];");
        assertEquals("w-1", str("node.name"), "the party stands under the proxy's name");
        assertEquals("w", str("node.mobile"));
        assertEquals(3, num("node.depth"));
        assertEquals("root,widgets,docks,w-1", str("node.path.join()"));
        assertEquals("widget:w", str("node.owner"));
        assertEquals(1, num("node.elements.length"));
        assertEquals(5, num("node.branches[0].branches[0].depth"));
        assertEquals("root,widgets,docks,w-1,cells,c1", str("node.branches[0].branches[0].path.join()"));
        assertTrue(yes("s.mobile === null && s.branches[0].mobile === null"));
        assertEquals(5, num("c1.snapshot().depth"), "a snapshot taken inside the party is at the page's level too");
    }

    @Test
    void aGraftIsRefused_whenItCannotStand() {
        assertTrue(str("fails(() => docks.graft('x', cells))").contains("only a mobile party's root"));
        assertTrue(str("fails(() => docks.graft('x', domOpsParty))").contains("only a mobile party's root"));
        js.eval("js", "var bare = domOpsParties.mobile('bare');");
        assertTrue(str("fails(() => docks.graft('x', bare))").contains("not been activated"));
        assertTrue(str("fails(() => cells.graft('self', w))").contains("inside itself"), "not under one of its own branches");
        js.eval("js", "var inner = owned(domOpsParties.mobile('inner'), 'widget:inner'); c1.graft('in', inner);");
        assertTrue(str("fails(() => inner.graft('loop', w))").contains("inside itself"), "not under a party grafted inside it");
        js.eval("js", "docks.graft('w-1', w);");
        assertTrue(str("fails(() => widgets.graft('w-2', w))").contains("grafted already"));
        assertTrue(str("fails(() => docks.graft('w-1', owned(domOpsParties.mobile('v'), 'widget:v')))").contains("already exists"), "the proxy's name is the host's, once");
    }

    @Test
    void aProxyHoldsItsOneParty_andNothingElse() {
        js.eval("js", "var proxy = docks.graft('w-1', w);");
        assertTrue(str("fails(() => proxy.createElement('x', 'div'))").contains("proxy"));
        assertTrue(str("fails(() => proxy.createBranch('x'))").contains("proxy"));
        assertTrue(str("fails(() => proxy.graft('x', owned(domOpsParties.mobile('v'), 'widget:v')))").contains("proxy"));
        assertTrue(str("fails(() => proxy.activate({}))").contains("proxy"));
    }

    @Test
    void detached_theProxyIsDissolved_andThePartyIsAStrayAgain_whole() {
        js.eval("js", "var proxy = docks.graft('w-1', w); var back = docks.detach('w-1');");
        assertTrue(yes("back === w && !w.isGrafted && !docks.hasBranch('w-1')"));
        assertEquals(2, num("c1.level"));
        assertEquals(1, num("c1.elementCount"), "its tree is whole");
        assertEquals("w", str("domOpsParties.strays()[0].name"));
        js.eval("js", "var again = widgets.graft('w-2', w);");
        assertTrue(yes("again !== proxy"), "grafted again, it is a new proxy: a proxy is fixed to its party for its life");
        assertEquals(4, num("c1.level"), "2 + 2, where it stands now");
        assertTrue(str("fails(() => docks.detach('w-1'))").contains("no mobile party"));
    }

    @Test
    void aMobilePartyDissolved_takesItsProxyWithIt_andLeavesThePartyOfParties() {
        js.eval("js", "docks.graft('w-1', w); w.dissolve();");
        assertFalse(yes("docks.hasBranch('w-1')"));
        assertFalse(yes("domOpsParties.mobiles().includes(w)"));
        assertEquals(2, num("removed"), "its elements, both");
        assertTrue(str("fails(() => docks.graft('w-1', w))").contains("dissolved"));
    }

    @Test
    void aDissolveAbove_isAnOrderThePartyObeys() {
        js.eval("js", "docks.graft('w-1', w); widgets.dissolve();");
        assertEquals(2, num("removed"), "the grafted party's elements went with the host's branch");
        assertFalse(yes("domOpsParties.mobiles().includes(w)"));
        assertFalse(yes("w.isGrafted"));
        assertFalse(yes("domOpsParty.hasBranch('widgets')"));
    }

    @Test
    void graftsNest_andTheLevelsAddUp() {
        js.eval("js", """
                docks.graft('w-1', w);                                   // w at 3
                var inner = owned(domOpsParties.mobile('inner'), 'widget:inner');
                var deep = owned(inner.createBranch('deep'), 'widget:deep');
                cells.graft('in', inner);                                 // cells at 4, the proxy at 5
                """);
        assertEquals(5, num("inner.level"));
        assertEquals(6, num("deep.level"));
        js.eval("js", "var at = (n, name) => n.branches.find(b => b.name === name); var s = domOpsParty.snapshot();");
        assertEquals(6, num("at(at(at(at(at(at(s, 'widgets'), 'docks'), 'w-1'), 'cells'), 'in'), 'deep').depth"));
        assertEquals("inner", str("at(at(at(at(at(s, 'widgets'), 'docks'), 'w-1'), 'cells'), 'in').mobile"));
        js.eval("js", "docks.detach('w-1');");
        assertEquals(3, num("deep.level"), "w a stray at 0: cells at 1, the proxy at 2, deep at 3");
    }
}
