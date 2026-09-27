package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 E3, grafting the focus party: one stationed party per page, the
 * one the steward is bound to; every other party mobile - made by the party
 * of parties, forwarding only, and grafted by a host through a proxy fixed to
 * it. The proxy is transparent; attaching and detaching tell the page of the
 * whole party; a dissolve takes the proxy, and a dissolve above takes the party.
 */
class FocusGraftTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/component/keyboard/FocusPartyModule.js";

    private static final String SHIM = """
        var console = { error: function (m) { throw new Error(m); } };
        var notices = [];
        focusParty.on(function (n) { notices.push(n.kind + ":" + n.name + "@" + n.path + (n.parent ? "<" + n.parent : "")); });
        class Box { constructor(n) { this.n = n; } }
        var dock = focusParty.root.createBranch("dock", new Box("dock"));
        var mine = focusParties.mobile("w");
        var grid = mine.root.createBranch("grid", new Box("grid"));
        var cell = grid.join("cell", new Box("cell"));
        notices = [];
        function fails(f) { try { f(); return null; } catch (e) { return e.message; } }
        function names(ms) { return ms.map(function (m) { return m.name; }).join(" "); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String str(String src) { return eval(src).asString(); }
    private String notices() { return str("notices.join(' ')"); }

    @Test
    void thePageHasOneStationedParty_andAnyOtherIsMobile_madeByThePartyOfParties() {
        assertEquals("stationed", str("focusParty.kind"));
        assertTrue(eval("focusParties.stationed === focusParty && focusParty instanceof StationedFocusParty").asBoolean());
        assertTrue(str("fails(() => new MobileFocusParty(null, 'x'))").contains("focusParties.mobile"));
        assertEquals("mobile", str("mine.kind"));
        assertTrue(str("fails(() => focusParties.mobile('w'))").contains("alive already"));
        assertTrue(str("fails(() => mine.on(function () {}))").contains("only forwards"));
    }

    @Test
    void aMobilePartyWorksOnItsOwn_andSaysNothing_whileAStray() {
        eval("grid.join('more', new Box('more'))");
        assertEquals("", notices(), "a stray says nothing to anyone");
        assertEquals("/grid/cell", str("cell.path"));
        assertTrue(eval("mine.stationed() === null && !mine.isGrafted").asBoolean());
        assertEquals("w", str("focusParties.strays()[0].name"));
        assertTrue(eval("focusParty.find(cell.id) === null").asBoolean(), "the page does not know it");
    }

    @Test
    void grafted_theProxyJoins_andTheWholePartyIsToldJoined_throughIt() {
        eval("var proxy = dock.graft('w-1', mine)");
        String dockId = str("dock.owner.id"), proxyId = str("proxy.id"), gridId = str("grid.owner.id");
        assertEquals("joined:w-1@/dock/w-1<" + dockId + " joined:grid@/dock/w-1/grid<" + proxyId + " joined:cell@/dock/w-1/grid/cell<" + gridId, notices());
        assertEquals("proxy", str("proxy.kind"));
        assertTrue(eval("cell.parent().parent() === proxy && proxy.parent() === dock.owner").asBoolean(), "the tree runs through the proxy");
        assertTrue(eval("focusParty.find(cell.id) === cell && mine.stationed() === focusParty").asBoolean());
        assertEquals("dock grid cell", str("names(focusParty.walk())"), "the proxy is not walked; its party is");
        assertEquals("proxy", str("focusParty.inspect().members[0].branch.members[0].kind"));
        assertEquals("w", str("focusParty.inspect().members[0].branch.members[0].mobile"));
        assertEquals(0, eval("focusParties.strays().length").asInt());
        eval("notices = []; grid.join('late', new Box('late'))");
        assertEquals("joined:late@/dock/w-1/grid/late<" + gridId, notices(), "what mutates in a grafted party is said on up");
    }

    @Test
    void aProxyAsksTheHolderAboveItWhetherItsPartyIsOffered() {
        eval("dock.owner.component.wouldOffer = function (m) { return m.name !== 'w-1'; }; var proxy = dock.graft('w-1', mine)");
        assertFalse(eval("grid.owner.in.owner.component.wouldOffer(grid.owner)").asBoolean(), "the proxy's face asks the dock about the proxy");
        eval("dock.owner.component.wouldOffer = function () { return true; }");
        assertTrue(eval("proxy.component.wouldOffer(grid.owner)").asBoolean());
    }

    @Test
    void aGraftIsRefused_whenItCannotStand() {
        assertTrue(str("fails(() => dock.graft('x', focusParty))").contains("only a mobile party"));
        assertTrue(str("fails(() => grid.graft('self', mine))").contains("inside itself"));
        eval("var inner = focusParties.mobile('inner'); grid.graft('in', inner)");
        assertTrue(str("fails(() => inner.root.graft('loop', mine))").contains("inside itself"), "nor under a party grafted inside it");
        eval("dock.graft('w-1', mine)");
        assertTrue(str("fails(() => focusParty.root.graft('w-2', mine))").contains("grafted already"));
        assertTrue(str("fails(() => dock.graft('w-1', focusParties.mobile('v')))").contains("already a member"));
        assertTrue(str("fails(() => dock.adopt(cell))").contains("adopted within its party"), "another party's member is grafted, not adopted");
    }

    @Test
    void detached_itsMembersAreToldLeft_theProxyIsGone_andThePartyIsWhole() {
        eval("var proxy = dock.graft('w-1', mine); notices = []; var back = dock.detach('w-1')");
        String dockId = str("dock.owner.id"), proxyId = str("proxy.id"), gridId = str("grid.owner.id");
        assertEquals("left:cell@/dock/w-1/grid/cell<" + gridId + " left:grid@/dock/w-1/grid<" + proxyId + " left:w-1@/dock/w-1<" + dockId, notices(), "leaves first, then the proxy");
        assertTrue(eval("back === mine && !mine.isGrafted && dock.members.length === 0").asBoolean());
        assertEquals("/grid/cell", str("cell.path"), "whole, and where it stands in its own party");
        assertEquals("w", str("focusParties.strays()[0].name"));
        eval("var again = focusParty.root.graft('w-2', mine)");
        assertTrue(eval("again !== proxy && focusParty.find(cell.id) === cell").asBoolean(), "grafted again: a new proxy, the same members");
        assertTrue(str("fails(() => dock.detach('w-1'))").contains("no mobile party"));
    }

    @Test
    void aMobilePartyDissolved_takesItsProxy_andLeavesThePartyOfParties() {
        eval("dock.graft('w-1', mine); notices = []; mine.dissolve()");
        assertTrue(notices().endsWith("left:w-1@/dock/w-1<" + str("dock.owner.id")), notices());
        assertTrue(notices().contains("left:cell@/dock/w-1/grid/cell"), "its members said gone to the page, while it was still grafted");
        assertEquals(0, eval("dock.members.length").asInt());
        assertFalse(eval("focusParties.mobiles().includes(mine)").asBoolean());
        assertTrue(str("fails(() => dock.graft('w-1', mine))").contains("dissolved"));
    }

    @Test
    void aBranchDissolvedAbove_isAnOrderThePartyObeys() {
        eval("dock.graft('w-1', mine); dock.owner.leave()");
        assertFalse(eval("focusParties.mobiles().includes(mine)").asBoolean());
        assertEquals(0, eval("mine.root.members.length").asInt());
        assertEquals(0, eval("focusParty.root.members.length").asInt());
    }

    @Test
    void graftsNest_andTheTreeRunsThroughEach() {
        eval("dock.graft('w-1', mine); var inner = focusParties.mobile('inner'); var deep = inner.root.join('deep', new Box('deep')); grid.graft('in', inner)");
        assertEquals("/dock/w-1/grid/in/deep", str("deep.path"));
        assertTrue(eval("inner.stationed() === focusParty && focusParty.find(deep.id) === deep").asBoolean());
        assertEquals("dock grid cell deep", str("names(focusParty.walk())"));
        eval("dock.detach('w-1')");
        assertTrue(eval("inner.stationed() === null && inner.isGrafted").asBoolean(), "grafted into a stray: it reaches no page");
    }
}
