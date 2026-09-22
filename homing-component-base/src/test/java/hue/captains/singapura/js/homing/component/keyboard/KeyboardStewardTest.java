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
 * The steward against a fake document that runs capture then bubble: the
 * invariants of the design, each as one test. One holder or none; a claim
 * evicts and the evicted is told by whom; keys reach the holder only, and a
 * key the holder takes is defaulted and stopped while a key it leaves
 * travels on; no listener while no one holds; a text field keeps its plain
 * keys; leaving releases; one steward per page; the claiming convention
 * resolves nesting to the innermost root alone; physical focus is never
 * touched.
 */
class KeyboardStewardTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/component/";

    // Elements that know their parent, their listeners by phase, and contains();
    // fire() walks capture from the document down, then bubble back up, and
    // honours stopPropagation. No element has focus(): calling it would throw.
    private static final String SHIM = """
        var log = [];
        var console = { error: function (m) { log.push("error:" + m); }, warn: function (m) { log.push("warn:" + m); } };
        function el(tag, parent) {
            var e = { tagName: tag, parentNode: parent || null, _cap: {}, _bub: {}, isContentEditable: false };
            e.addEventListener = function (t, f, c) { var m = c ? e._cap : e._bub; (m[t] = m[t] || []).push(f); };
            e.removeEventListener = function (t, f, c) { var m = c ? e._cap : e._bub; var a = m[t] || []; var i = a.indexOf(f); if (i >= 0) a.splice(i, 1); };
            e.contains = function (o) { for (var x = o; x; x = x.parentNode) if (x === e) return true; return false; };
            e.listeners = function (t) { return (e._cap[t] || []).length + (e._bub[t] || []).length; };
            return e;
        }
        var document = el("#document");
        function fire(target, type, props) {
            var ev = Object.assign({ type: type, target: target, defaultPrevented: false, stopped: false,
                preventDefault: function () { this.defaultPrevented = true; }, stopPropagation: function () { this.stopped = true; } }, props || {});
            var path = []; for (var x = target; x; x = x.parentNode) path.unshift(x);
            for (var i = 0; i < path.length && !ev.stopped; i++) (path[i]._cap[type] || []).slice().forEach(function (f) { if (!ev.stopped) f(ev); });
            for (var j = path.length - 1; j >= 0 && !ev.stopped; j--) (path[j]._bub[type] || []).slice().forEach(function (f) { if (!ev.stopped) f(ev); });
            return ev;
        }
        function key(target, k, props) { return fire(target, "keydown", Object.assign({ key: k }, props || {})); }
        function fakeBranch(name) { return { name: name, dissolved: false, activate: function () {}, dissolve: function () { this.dissolved = true; } }; }
        var events = [];
        function sink(e) { events.push(e.kind + ":" + e.id + (e.kind === "Taken" ? ":" + e.by : "")); }
        var kb = new KeyboardSteward(fakeBranch("keyboard"), { onEvent: sink });
        var page = el("DIV", document), outer = el("DIV", page), inner = el("DIV", outer), field = el("INPUT", page);
        // a member that takes arrows and leaves everything else
        function member(id, root) {
            var m = { keys: [], granted: 0, taken: [] };
            kb.join(id, {
                keyDown: function (ev) { m.keys.push(id + ":" + ev.key); return ev.key.indexOf("Arrow") === 0; },
                keyUp: function (ev) { m.keys.push(id + ":up:" + ev.key); return false; },
                granted: function () { m.granted++; },
                taken: function (by) { m.taken.push(String(by)); }
            });
            if (root) m.off = Keys.claimOn(root, kb, id);
            return m;
        }
        var bubbled = [];
        document.addEventListener("keydown", function (ev) { bubbled.push(ev.key); }, false);
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(DIR + "party/PartyModule.js");
        loadModule(DIR + "keyboard/FocusPartyModule.js");
        loadModule(DIR + "keyboard/KeyboardSecretaryModule.js");
        loadModule(DIR + "keyboard/KeyboardEventsModule.js");
        loadModule(DIR + "keyboard/KeyboardStewardModule.js");
        loadModule(DIR + "keyboard/KeysModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String events() { return eval("events.join(' ')").asString(); }
    private String holder() { Value v = eval("kb.holder()"); return v.isNull() ? null : v.asString(); }
    private int documentKeyListeners() { return eval("document.listeners('keydown') + document.listeners('keyup') - 1").asInt(); }  // minus the bubble probe

    @Test
    void noHolderMeansNoListenerAndKeysGoNowhere() {
        eval("var a = member('a')");
        assertEquals(0, documentKeyListeners(), "lazy: nothing while no one holds");
        assertEquals("ArrowUp", eval("key(page, 'ArrowUp'); bubbled.join()").asString());
        assertEquals("", eval("a.keys.join()").asString());
        assertEquals(null, holder());
    }

    @Test
    void aClaimGrantsAndTurnsTheDocumentOn() {
        eval("var a = member('a'); kb.claim('a')");
        assertEquals("a", holder());
        assertEquals(1, eval("a.granted").asInt());
        assertEquals("Granted:a", events());
        assertEquals(2, documentKeyListeners(), "keydown and keyup, capture");
    }

    @Test
    void aTakenKeyIsDefaultedAndStoppedALeftKeyTravelsOn() {
        eval("var a = member('a'); kb.claim('a')");
        Value taken = eval("key(page, 'ArrowUp')");
        assertTrue(taken.getMember("defaultPrevented").asBoolean());
        assertTrue(taken.getMember("stopped").asBoolean());
        Value left = eval("key(page, 'Escape')");
        assertFalse(left.getMember("defaultPrevented").asBoolean());
        assertFalse(left.getMember("stopped").asBoolean());
        assertEquals("a:ArrowUp,a:Escape", eval("a.keys.join()").asString(), "the holder saw both");
        assertEquals("Escape", eval("bubbled.join()").asString(), "only the left key reached the document's bubble");
    }

    @Test
    void keyUpIsForwardedToo() {
        eval("var a = member('a'); kb.claim('a'); fire(page, 'keyup', { key: 'ArrowUp' })");
        assertEquals("a:up:ArrowUp", eval("a.keys.join()").asString());
    }

    @Test
    void aClaimEvictsTheHolderWhoIsToldByWhomAndHearsNoMoreKeys() {
        eval("var a = member('a'), b = member('b'); kb.claim('a'); kb.claim('b'); key(page, 'ArrowUp')");
        assertEquals("b", holder());
        assertEquals("Granted:a Taken:a:b Granted:b", events());
        assertEquals("b", eval("a.taken.join()").asString());
        assertEquals("", eval("a.keys.join()").asString(), "the evicted hears nothing");
        assertEquals("b:ArrowUp", eval("b.keys.join()").asString());
        assertEquals(2, documentKeyListeners(), "still listening, never off in between");
    }

    @Test
    void aClaimByTheHolderIsNothing() {
        eval("var a = member('a'); kb.claim('a'); kb.claim('a')");
        assertEquals("Granted:a", events());
        assertEquals(1, eval("a.granted").asInt());
    }

    @Test
    void aReleaseEmptiesTheFieldAndTurnsTheDocumentOff() {
        eval("var a = member('a'); kb.claim('a'); kb.release('a'); key(page, 'ArrowUp')");
        assertEquals(null, holder());
        assertEquals("Granted:a Released:a", events());
        assertEquals("null", eval("a.taken.join()").asString(), "told: taken by no one");
        assertEquals(0, documentKeyListeners());
        assertEquals("", eval("a.keys.join()").asString());
    }

    @Test
    void aReleaseByAnyoneElseIsNothing() {
        eval("var a = member('a'), b = member('b'); kb.claim('a'); kb.release('b'); kb.release('zz')");
        assertEquals("a", holder());
        assertEquals("Granted:a", events());
    }

    @Test
    void leavingWhileHoldingReleasesAndAGoneMemberCannotClaim() {
        eval("var a = member('a'), b = member('b'); kb.claim('a'); kb.leave('b'); kb.leave('a')");
        assertEquals(null, holder());
        assertEquals("Granted:a Released:a", events());
        assertEquals(0, documentKeyListeners());
        var ex = assertThrows(PolyglotException.class, () -> eval("kb.claim('a')"));
        assertTrue(ex.getMessage().contains("no member 'a'"), ex.getMessage());
        eval("member('b')");   // b left, so it may rejoin
    }

    @Test
    void aTextFieldKeepsItsPlainKeysButNotChordsOrEscape() {
        eval("var a = member('a'); kb.claim('a'); key(field, 'ArrowUp'); key(field, 'a'); key(field, 'ArrowUp', { ctrlKey: true }); key(field, 'Escape'); field.type = 'range'; key(field, 'ArrowUp')");
        assertEquals("a:ArrowUp,a:Escape,a:ArrowUp", eval("a.keys.join()").asString(), "chord, Escape, and a range input's arrows");
        eval("var ta = el('TEXTAREA', page); key(ta, 'ArrowDown'); var ce = el('DIV', page); ce.isContentEditable = true; key(ce, 'ArrowDown')");
        assertEquals("a:ArrowUp,a:Escape,a:ArrowUp", eval("a.keys.join()").asString());
    }

    @Test
    void theConventionClaimsOnPressOrFocusAndReleasesWhenFocusLeaves() {
        eval("var a = member('a', outer)");
        eval("fire(outer, 'pointerdown')");
        assertEquals("a", holder());
        eval("fire(outer, 'focusout', { relatedTarget: inner })");
        assertEquals("a", holder(), "focus moving within the root keeps the keys");
        eval("fire(outer, 'focusout', { relatedTarget: field })");
        assertEquals(null, holder(), "focus leaving the root releases");
        eval("fire(inner, 'focusin')");
        assertEquals("a", holder(), "focus arriving anywhere in the root claims");
        eval("fire(outer, 'focusout', { relatedTarget: null })");
        assertEquals(null, holder(), "focus going nowhere releases");
        eval("a.off(); fire(outer, 'pointerdown')");
        assertEquals(null, holder(), "off() removes the convention");
    }

    @Test
    void theConventionKeepsTheKeysWhenAskedNotToRelease() {
        eval("var a = member('a'); Keys.claimOn(outer, kb, 'a', { release: false }); fire(outer, 'pointerdown'); fire(outer, 'focusout', { relatedTarget: field })");
        assertEquals("a", holder());
    }

    /**
     * Nesting resolves in Keys, which knows the roots under the convention:
     * a press or the focus arriving inside an inner root is the inner
     * member's alone — the outer stays silent, never granted for it; a press
     * in the outer outside the inner is the outer's; a root forgotten by its
     * off() is no longer inner to anyone.
     */
    @Test
    void nestedRootsResolveToTheInnermostAlone_theOuterNeverGrantedForItsChild() {
        eval("var o = member('o', outer), i = member('i', inner)");
        eval("fire(inner, 'pointerdown')");
        assertEquals("i", holder(), "a press in the inner root: the inner alone claims");
        assertEquals("Granted:i", events(), "the outer was not granted on the way");
        assertEquals(0, eval("o.granted").asInt());
        eval("fire(outer, 'pointerdown')");
        assertEquals("o", holder(), "a press in the outer root outside the inner: the outer claims");
        assertEquals("o", eval("i.taken.join()").asString(), "the inner was told by whom");
        eval("events = []; var deep = el('SPAN', inner); fire(deep, 'focusin')");
        assertEquals("i", holder(), "the focus arriving deep inside the inner root: the inner's, by the same rule");
        assertEquals("Taken:o:i Granted:i", events());
        eval("events = []; i.off(); fire(deep, 'pointerdown')");
        assertEquals("o", holder(), "the inner root forgotten: a press in it is the outer's again");
        assertEquals("Taken:i:o Granted:o", events());
    }

    @Test
    void onlyOneStewardPerPageUntilDisposed() {
        var ex = assertThrows(PolyglotException.class, () -> eval("new KeyboardSteward(fakeBranch('k2'), {})"));
        assertTrue(ex.getMessage().contains("one per page"), ex.getMessage());
        eval("var a = member('a'); kb.claim('a'); var br = kb._branch; kb.dispose()");
        assertEquals(0, documentKeyListeners(), "disposed: the document is let go");
        assertTrue(eval("br.dissolved").asBoolean());
        eval("var kb2 = new KeyboardSteward(fakeBranch('k2'), {})");
        assertTrue(eval("kb2.holder()").isNull());
    }

    @Test
    void aMemberNeedsAnIdAndJoinsOnce() {
        for (String bad : new String[] { "kb.join('', {})", "kb.join(3, {})", "kb.join('steward', {})" }) assertThrows(PolyglotException.class, () -> eval(bad), bad);
        eval("kb.join('a', {})");
        assertThrows(PolyglotException.class, () -> eval("kb.join('a', {})"));
    }

    @Test
    void moreThanOneListener_eachToldEach_offRemovesOne() {
        eval("var heard = []; var off = kb.on(function (e) { heard.push(e.kind); }); var a = member('a'); kb.claim('a'); off(); kb.release('a')");
        assertEquals("Granted", eval("heard.join()").asString(), "the second listener heard the grant, then was removed");
        assertEquals("Granted:a Released:a", events(), "the first listener heard both");
        assertThrows(PolyglotException.class, () -> eval("kb.on(3)"));
    }

    /**
     * A member of the focus tree: joined by its membership, its component's
     * methods the reactors; claimed by the membership; leaving the tree leaves
     * the steward, and releases if it held; the convention takes a membership.
     */
    @Test
    void aMemberOfTheFocusTreeJoinsByItsMembership_andLeavingTheTreeLeavesHere() {
        eval("""
            kb.dispose();
            var tree = new FocusParty();
            var kt = new KeyboardSteward(fakeBranch("k"), { onEvent: sink, party: tree });
            var w = { keys: [], got: [], keyDown: function (ev) { this.keys.push(ev.key); return ev.key === "ArrowUp"; }, granted: function (by) { this.got.push("granted:" + by); }, taken: function (by) { this.got.push("taken:" + by); } };
            var m = tree.root.join("w", w);   // joined the tree: joined the steward, unasked
            var off = Keys.claimOn(inner, kt, m);
            fire(inner, "pointerdown");
            """);
        assertEquals(eval("m.id").asString(), eval("kt.holder()").asString(), "the holder is the membership's id");
        assertTrue(eval("kt.has(m) && kt.has(m.id)").asBoolean());
        eval("key(inner, 'ArrowUp'); key(inner, 'Enter')");
        assertEquals("ArrowUp,Enter", eval("w.keys.join()").asString(), "the component's keyDown is the reactor");
        assertEquals("granted:claim", eval("w.got.join()").asString());
        eval("events = []; m.leave()");
        assertEquals("Released:" + eval("m.id").asString(), events(), "left the tree: left the steward, and released");
        assertFalse(eval("kt.has(m)").asBoolean());
        assertTrue(eval("kt.holder() === null").asBoolean());
        eval("off(); kt.dispose()");
    }

    /**
     * Yield, up the tree: a holder that yields hands the keys to the first
     * ancestor whose wouldHold says yes — that ancestor told granted by yield,
     * the leaf told taken by it — or, none saying yes, to no one; a yield by a
     * non-holder is nothing; a member outside the tree releases; a member
     * leaving the tree while holding yields on its way out, from the parent
     * it had.
     */
    @Test
    void aYieldGoesUpToTheFirstAncestorThatWouldHold_orToNoOne() {
        eval("""
            kb.dispose();
            var tree = new FocusParty();
            var kt = new KeyboardSteward(fakeBranch("k"), { onEvent: sink, party: tree });
            function node(name, holds) { return { name: name, got: [], asked: [], wouldHold: function (from) { this.asked.push(from ? from.name : "left"); return holds; },
                granted: function (by) { this.got.push("granted:" + by); }, taken: function (by) { this.got.push("taken:" + by); } }; }
            var pageC = node("page", false), panelC = node("panelA", true), passC = node("panelB", false), leafA = node("a1", false), leafB = node("b1", false), looseC = node("c", false);
            var page = tree.root.createBranch("page", pageC);
            var panelA = page.createBranch("panelA", panelC), panelB = page.createBranch("panelB", passC);
            var a1 = panelA.join("a1", leafA), b1 = panelB.join("b1", leafB), c = page.join("c", looseC);
            """);
        // 2. a catching parent
        eval("kt.claim(a1); events = []; var y1 = kt.yield(a1)");
        assertTrue(eval("y1").asBoolean());
        assertEquals(eval("panelA.owner.id").asString(), eval("kt.holder()").asString(), "panel A would hold: it holds");
        assertEquals("Taken:" + eval("a1.id").asString() + ":" + eval("panelA.owner.id").asString() + " Granted:" + eval("panelA.owner.id").asString(), events(), "one change of holder");
        assertEquals("granted:yield", eval("panelC.got.join()").asString(), "told by what");
        assertEquals("a1", eval("panelC.asked.join()").asString(), "asked about the one that yielded");
        assertEquals("granted:claim,taken:" + eval("panelA.owner.id").asString(), eval("leafA.got.join()").asString());
        // 1. no catching parent: panel B passes, the page passes, the root is reached
        eval("kt.claim(b1); events = []; kt.yield(b1)");
        assertTrue(eval("kt.holder() === null").asBoolean(), "no one holds");
        assertEquals("Released:" + eval("b1.id").asString(), events());
        assertEquals("b1", eval("passC.asked.join()").asString());
        assertEquals("b1", eval("pageC.asked.join()").asString(), "every ancestor asked in turn, up to the root");
        eval("kt.claim(c); events = []; kt.yield(c)");
        assertTrue(eval("kt.holder() === null").asBoolean(), "a loose leaf under a page that would not hold: no one");
        // a yield by a non-holder is nothing; a member outside the tree releases
        eval("kt.claim(a1); events = []; var y2 = kt.yield(b1)");
        assertFalse(eval("y2").asBoolean());
        assertEquals("", events());
        eval("kt.join('legacy', {}); kt.claim('legacy'); events = []; kt.yield('legacy')");
        assertEquals("Released:legacy", events());
        // leaving while holding: a yield on the way out, from the parent it had
        eval("kt.claim(a1); events = []; panelC.asked = []; a1.leave()");
        assertEquals(eval("panelA.owner.id").asString(), eval("kt.holder()").asString(), "the keys went to the parent, not to no one");
        assertEquals("left", eval("panelC.asked.join()").asString(), "asked with no one to name");
        assertEquals("granted:yield,taken:" + eval("b1.id").asString() + ",granted:left", eval("panelC.got.join()").asString(), "taken by b1 in between; granted again when a1 left");
        assertFalse(eval("kt.has(a1)").asBoolean());
        eval("kt.dispose()");
    }

    @Test
    void aSelectKeepsTheKeysThatWalkIt_butEnterConfirmsAndIsForwarded() {
        eval("var a = member('a'); kb.claim('a'); var sel = el('SELECT', page); key(sel, 'ArrowDown'); key(sel, 'Home'); key(sel, 'c'); key(sel, 'Escape'); key(sel, 'ArrowDown', { ctrlKey: true }); key(sel, 'Enter'); key(field, 'Enter')");
        assertEquals("a:Escape,a:ArrowDown,a:Enter", eval("a.keys.join()").asString(), "the arrows, Home and a letter are the list's; Escape, a chord and Enter are forwarded; an input keeps its Enter");
    }

    @Test
    void aSinkThatThrowsIsReportedNotPropagated() {
        eval("kb.dispose(); var kb3 = new KeyboardSteward(fakeBranch('k3'), { onEvent: function () { throw new Error('boom'); } }); kb3.join('a', {}); kb3.claim('a')");
        assertEquals("a", eval("kb3.holder()").asString());
        assertTrue(eval("log.join()").asString().contains("onEvent threw on Granted"));
    }
}
