package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The focus party is structure and nothing else: a leaf joins a branch, a
 * container holds one; a member's parent is the holder of the branch it is
 * in; a member moves branch and all, never under itself; names are unique
 * within a branch; leaving dissolves what one holds and reports the parent
 * one had; the tree is read whole as frozen data; every mutation is a
 * notice and nothing else is said.
 */
class FocusPartyTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/component/keyboard/FocusPartyModule.js";

    private static final String SHIM = """
        var console = { error: function (m) { throw new Error(m); } };
        var notices = [];
        var party = new FocusParty();
        party.on(function (n) { notices.push(n.kind + ":" + n.name + "@" + n.path + (n.parent ? "<" + n.parent : "")); });
        class Dock { constructor(n) { this.n = n; } }
        class Widget { constructor(n) { this.n = n; } }
        var dock = new Dock("dock"), desk = new Dock("desk"), w = new Widget("w"), c = new Widget("c");
        var dockBranch = party.root.createBranch("dock", dock);
        var deskBranch = party.root.createBranch("desk", desk);
        var mw = dockBranch.join("widget", w);
        var mc = party.root.join("card", c);
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String notices() { return eval("notices.join(' ')").asString(); }

    @Test
    void aLeafJoinsAContainerHolds_andTheParentIsTheHolderOfTheBranchOneIsIn() {
        assertEquals("/dock/widget", eval("mw.path").asString());
        assertEquals("dock", eval("mw.parent().name").asString(), "the widget's parent is the dock, which holds the branch it is in");
        assertTrue(eval("mw.parent().parent() === null").asBoolean(), "the dock is in the root: no parent");
        assertTrue(eval("mc.parent() === null").asBoolean());
        assertEquals("joined:dock@/dock joined:desk@/desk joined:widget@/dock/widget<m1 joined:card@/card", notices(), "every join a notice, with the parent's id");
        assertTrue(eval("dockBranch.owner === party.find(mw.parent().id) && dockBranch.owner.branch === dockBranch").asBoolean(), "the holder's membership owns the branch");
    }

    @Test
    void adoptMovesAMemberBranchAndAll_neverUnderItself() {
        eval("notices = []; deskBranch.adopt(mw)");
        assertEquals("/desk/widget", eval("mw.path").asString());
        assertEquals("desk", eval("mw.parent().name").asString());
        assertEquals("0,1", eval("[dockBranch.members.length, deskBranch.members.length].join()").asString());
        assertEquals("m3", eval("mw.id").asString(), "the id is stable across the move");
        assertEquals("moved:widget@/desk/widget<m2", notices());
        eval("var inner = dockBranch.createBranch('pane', { n: 'pane' }); var mp = dockBranch.members[0]");
        var ex = assertThrows(PolyglotException.class, () -> eval("inner.adopt(mp)"));
        assertTrue(ex.getMessage().contains("under itself"), ex.getMessage());
        eval("var deeper = inner.createBranch('deeper', { n: 'd' })");
        assertThrows(PolyglotException.class, () -> eval("deeper.adopt(mp)"), "not under one's own descendant either");
        eval("deskBranch.adopt(mp)");
        assertEquals("/desk/pane/deeper", eval("deeper.path").asString(), "moved with everything it holds");
    }

    @Test
    void namesAreUniqueWithinABranch_andAdoptRespectsThat() {
        assertThrows(PolyglotException.class, () -> eval("dockBranch.join('widget', {})"));
        eval("deskBranch.join('widget', {})");
        assertThrows(PolyglotException.class, () -> eval("deskBranch.adopt(mw)"), "a widget is there already");
        assertThrows(PolyglotException.class, () -> eval("party.root.join('', {})"));
        assertThrows(PolyglotException.class, () -> eval("party.root.join('x', null)"));
    }

    @Test
    void leavingDissolvesWhatOneHolds_andReportsTheParentOneHad() {
        eval("notices = []; dockBranch.owner.leave()");
        assertEquals("left:widget@/dock/widget<m1 left:dock@/dock", notices(), "the widget left first, with the dock as its parent; then the dock");
        assertTrue(eval("mw.in === null && dockBranch.owner.in === null").asBoolean());
        assertEquals("desk,card", eval("party.root.members.map(function (m) { return m.name; }).join()").asString());
        eval("notices = []; mw.leave()");
        assertEquals("", notices(), "leaving twice is nothing");
    }

    @Test
    void theTreeIsReadWholeAsFrozenData() {
        Value t = eval("party.inspect()");
        assertEquals("root", t.getMember("name").asString());
        assertEquals("dock,desk,card", eval("party.inspect().members.map(function (m) { return m.name; }).join()").asString());
        assertEquals("holder,holder,leaf", eval("party.inspect().members.map(function (m) { return m.kind; }).join()").asString());
        assertEquals("Dock", eval("party.inspect().members[0].component").asString());
        assertEquals("widget", eval("party.inspect().members[0].branch.members[0].name").asString());
        assertEquals("m1", eval("party.inspect().members[0].branch.holder").asString());
        assertTrue(eval("Object.isFrozen(party.inspect()) && Object.isFrozen(party.inspect().members) && Object.isFrozen(party.inspect().members[0].branch)").asBoolean());
        assertEquals("m3", eval("party.find('m3').id").asString());
        assertTrue(eval("party.find('nope') === null").asBoolean());
    }

    /** The walk is pre-order — a member, then the branch it holds, then the next — and follows a move; a branch walks itself. */
    @Test
    void theWalkIsPreOrder_andFollowsAMove() {
        assertEquals("dock widget desk card", eval("party.walk().map(function (m) { return m.name; }).join(' ')").asString());
        assertEquals("widget", eval("dockBranch.walk().map(function (m) { return m.name; }).join(' ')").asString(), "a branch walks its own");
        eval("var inner = deskBranch.createBranch('inner', new Dock('inner')); inner.join('deep', new Widget('deep')); deskBranch.adopt(mw)");
        assertEquals("dock desk inner deep widget card", eval("party.walk().map(function (m) { return m.name; }).join(' ')").asString(), "adopted: appended to the desk's branch, after inner and its deep");
        assertTrue(eval("party.walk()[0] === dockBranch.owner").asBoolean(), "memberships, not copies");
    }

    @Test
    void theRootIsAPartyOfItsOwn_andOffRemovesAListener() {
        assertEquals("", eval("focusParty.root.path").asString());
        assertEquals("0", eval("String(focusParty.root.members.length)").asString(), "the page's, untouched by this test's party");
        eval("var heard = 0; var off = party.on(function () { heard++; }); party.root.join('a', {}); off(); party.root.join('b', {})");
        assertEquals(1, eval("heard").asInt());
        assertFalse(eval("mw.holds(mc)").asBoolean());
        assertTrue(eval("dockBranch.owner.holds(mw)").asBoolean());
    }
}
