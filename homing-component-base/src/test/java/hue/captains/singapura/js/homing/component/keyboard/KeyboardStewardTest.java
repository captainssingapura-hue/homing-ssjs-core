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
 * evicts and the evicted is told by whom; keys from the body reach the
 * holder only, and a key the holder takes is defaulted and stopped while a
 * key it leaves travels on; a key from a focused element reaches no one,
 * whatever the key; one listener on the document, bubble phase, always;
 * leaving releases; one steward per page; the claiming convention is one
 * press, the innermost root alone, and native focus moves nothing; and the
 * walk — Tab moving a candidate over the tree in pre-order while the holder
 * stays put, Enter taking the offer up, Escape calling it off.
 */
class KeyboardStewardTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/component/";

    // Elements that know their parent, their listeners by phase, and contains();
    // fire() walks capture from the document down, then bubble back up, and
    // honours stopPropagation. focus() and blur() move document.activeElement,
    // which is the body when nothing is focused, as in a browser; a key fired
    // at an element is a key from a focused element.
    private static final String SHIM = """
        var log = [];
        var console = { error: function (m) { log.push("error:" + m); }, warn: function (m) { log.push("warn:" + m); } };
        function el(tag, parent) {
            var e = { tagName: tag, parentNode: parent || null, _cap: {}, _bub: {}, isContentEditable: false };
            e.addEventListener = function (t, f, c) { var m = c ? e._cap : e._bub; (m[t] = m[t] || []).push(f); };
            e.removeEventListener = function (t, f, c) { var m = c ? e._cap : e._bub; var a = m[t] || []; var i = a.indexOf(f); if (i >= 0) a.splice(i, 1); };
            e.contains = function (o) { for (var x = o; x; x = x.parentNode) if (x === e) return true; return false; };
            e.listeners = function (t) { return (e._cap[t] || []).length + (e._bub[t] || []).length; };
            e.focus = function () { document.activeElement = e; };
            e.blur = function () { if (document.activeElement === e) document.activeElement = document.body; };
            return e;
        }
        var document = el("#document");
        document.body = el("BODY", document);
        document.activeElement = document.body;
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
        var body = document.body, page = el("DIV", body), outer = el("DIV", page), inner = el("DIV", outer), field = el("INPUT", page);
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
        loadModule(DIR + "keyboard/KeyboardWalkModule.js");
        loadModule(DIR + "keyboard/KeyboardStewardModule.js");
        loadModule(DIR + "keyboard/KeysModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String events() { return eval("events.join(' ')").asString(); }
    private String holder() { Value v = eval("kb.holder()"); return v.isNull() ? null : v.asString(); }
    private int documentKeyListeners() { return eval("document.listeners('keydown') + document.listeners('keyup') - 1").asInt(); }  // minus the bubble probe

    @Test
    void theDocumentIsListenedToAlways_andKeysGoNowhereWithoutAHolder() {
        eval("var a = member('a')");
        assertEquals(2, documentKeyListeners(), "keydown and keyup, bubble phase, from the start");
        assertEquals("ArrowUp", eval("key(body, 'ArrowUp'); bubbled.join()").asString());
        assertEquals("", eval("a.keys.join()").asString());
        assertEquals(null, holder());
    }

    @Test
    void aClaimGrants() {
        eval("var a = member('a'); kb.claim('a')");
        assertEquals("a", holder());
        assertEquals(1, eval("a.granted").asInt());
        assertEquals("Granted:a", events());
    }

    @Test
    void aTakenKeyIsDefaultedAndStoppedALeftKeyTravelsOn() {
        eval("var a = member('a'); kb.claim('a')");
        Value taken = eval("key(body, 'ArrowUp')");
        assertTrue(taken.getMember("defaultPrevented").asBoolean());
        assertTrue(taken.getMember("stopped").asBoolean());
        Value left = eval("key(body, 'Escape')");
        assertFalse(left.getMember("defaultPrevented").asBoolean());
        assertFalse(left.getMember("stopped").asBoolean());
        assertEquals("a:ArrowUp,a:Escape", eval("a.keys.join()").asString(), "the holder saw both");
        assertEquals("Escape", eval("bubbled.join()").asString(), "only the left key reached the document's bubble probe, registered after the steward");
    }

    @Test
    void keyUpIsForwardedToo() {
        eval("var a = member('a'); kb.claim('a'); fire(body, 'keyup', { key: 'ArrowUp' })");
        assertEquals("a:up:ArrowUp", eval("a.keys.join()").asString());
    }

    @Test
    void aClaimEvictsTheHolderWhoIsToldByWhomAndHearsNoMoreKeys() {
        eval("var a = member('a'), b = member('b'); kb.claim('a'); kb.claim('b'); key(body, 'ArrowUp')");
        assertEquals("b", holder());
        assertEquals("Granted:a Taken:a:b Granted:b", events());
        assertEquals("b", eval("a.taken.join()").asString());
        assertEquals("", eval("a.keys.join()").asString(), "the evicted hears nothing");
        assertEquals("b:ArrowUp", eval("b.keys.join()").asString());
    }

    @Test
    void aClaimByTheHolderIsNothing() {
        eval("var a = member('a'); kb.claim('a'); kb.claim('a')");
        assertEquals("Granted:a", events());
        assertEquals(1, eval("a.granted").asInt());
    }

    @Test
    void aReleaseEmptiesTheField() {
        eval("var a = member('a'); kb.claim('a'); kb.release('a'); key(body, 'ArrowUp')");
        assertEquals(null, holder());
        assertEquals("Granted:a Released:a", events());
        assertEquals("null", eval("a.taken.join()").asString(), "told: taken by no one");
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
        var ex = assertThrows(PolyglotException.class, () -> eval("kb.claim('a')"));
        assertTrue(ex.getMessage().contains("no member 'a'"), ex.getMessage());
        eval("member('b')");   // b left, so it may rejoin
    }

    /** A key from a focused element — a field, a select, a button, a plain element — reaches no one, whatever the key; from the body it reaches the holder. */
    @Test
    void aKeyFromAFocusedElementReachesNoOne_whateverTheKey() {
        eval("var a = member('a'); kb.claim('a'); key(field, 'ArrowUp'); key(field, 'a'); key(field, 'ArrowUp', { ctrlKey: true }); key(field, 'Escape'); key(field, 'Enter')");
        assertEquals("", eval("a.keys.join()").asString(), "an input keeps its plain keys, its chords, its Escape and its Enter");
        eval("var sel = el('SELECT', page); key(sel, 'ArrowDown'); key(sel, 'Enter'); key(sel, 'Escape'); var btn = el('BUTTON', page); key(btn, 'Enter'); key(inner, 'ArrowUp')");
        assertEquals("", eval("a.keys.join()").asString(), "a select, a button and a plain element too");
        eval("key(body, 'ArrowUp'); key(body, 'Escape')");
        assertEquals("a:ArrowUp,a:Escape", eval("a.keys.join()").asString(), "from the body, nothing focused: the holder's");
        assertEquals("ArrowUp,a,ArrowUp,Escape,Enter,ArrowDown,Enter,Escape,Enter,ArrowUp,Escape", eval("bubbled.join()").asString(), "every key from a focused element travelled on untouched; the holder's left Escape too");
    }

    /** The convention is one press: a press in the root claims; native focus arriving in or leaving the root moves nothing; off() removes it. */
    @Test
    void theConventionClaimsOnAPress_andNativeFocusMovesNothing() {
        eval("var a = member('a', outer), b = member('b')");
        eval("fire(outer, 'pointerdown')");
        assertEquals("a", holder());
        eval("kb.claim('b'); fire(inner, 'focusin'); field.focus(); fire(field, 'focusin')");
        assertEquals("b", holder(), "the focus arriving in the root claims nothing");
        eval("kb.claim('a'); fire(outer, 'focusout', { relatedTarget: field }); fire(outer, 'focusout', { relatedTarget: null })");
        assertEquals("a", holder(), "the focus leaving the root releases nothing");
        assertEquals(1, eval("outer.listeners('pointerdown') + outer.listeners('focusin') + outer.listeners('focusout')").asInt(), "one listener: the press");
        eval("a.off(); kb.release('a'); fire(outer, 'pointerdown')");
        assertEquals(null, holder(), "off() removes the convention");
        assertEquals(0, eval("outer.listeners('pointerdown')").asInt());
    }

    /**
     * Nesting resolves on the roots enrolled with the steward: a press inside
     * an inner root is the inner member's alone — the outer stays silent,
     * never granted for it; a press in the outer outside the inner is the
     * outer's; a root forgotten by its off() is no longer inner to anyone;
     * memberAt answers the innermost.
     */
    @Test
    void nestedRootsResolveToTheInnermostAlone_theOuterNeverGrantedForItsChild() {
        eval("var o = member('o', outer), i = member('i', inner); var deep = el('SPAN', inner)");
        assertEquals("i", eval("kb.memberAt(deep)").asString());
        assertEquals("o", eval("kb.memberAt(outer)").asString());
        assertTrue(eval("kb.memberAt(field) === null").asBoolean());
        eval("fire(deep, 'pointerdown')");
        assertEquals("i", holder(), "a press deep in the inner root: the inner alone claims");
        assertEquals("Granted:i", events(), "the outer was not granted on the way");
        assertEquals(0, eval("o.granted").asInt());
        eval("fire(outer, 'pointerdown')");
        assertEquals("o", holder(), "a press in the outer root outside the inner: the outer claims");
        assertEquals("o", eval("i.taken.join()").asString(), "the inner was told by whom");
        eval("kb.claim('i'); events = []; i.off(); fire(deep, 'pointerdown')");
        assertEquals("o", holder(), "the inner root forgotten: a press in it is the outer's again");
        assertEquals("Taken:i:o Granted:o", events());
        assertEquals("o", eval("kb.memberAt(deep)").asString());
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
        assertEquals(2, documentKeyListeners(), "the new one listens");
        eval("kb2.dispose()");
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
        eval("key(body, 'ArrowUp'); key(body, 'Enter')");
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

    /**
     * The walk: Tab moves a candidate over the tree in pre-order — the holder
     * unchanged, the member offered told so — Shift+Tab walks back, Enter takes
     * the offer up and the walk is over, Escape calls it off; a walk that comes
     * home to the holder ends; a press, a claim by call, native focus arriving
     * — told by focusin, before any key — and the candidate leaving the tree
     * all withdraw the offer; with no member
     * to walk, Tab is the browser's.
     */
    @Test
    void theWalkMovesACandidate_andTheHolderStaysUntilItIsConfirmed() {
        eval("""
            kb.dispose();
            var tree = new FocusParty();
            var kt = new KeyboardSteward(fakeBranch("k"), { onEvent: sink, party: tree });
            function node(name) { return { name: name, got: [], offered: function () { this.got.push(name + ":offered"); }, withdrawn: function () { this.got.push(name + ":withdrawn"); },
                                           granted: function (by) { this.got.push(name + ":granted:" + by); }, taken: function () { this.got.push(name + ":taken"); } }; }
            var pA = tree.root.createBranch("A", node("A")), a1 = pA.join("a1", node("a1")), a2 = pA.join("a2", node("a2"));
            var pB = tree.root.createBranch("B", node("B")), b1 = pB.join("b1", node("b1"));
            var d = tree.root.join("d", node("d"));
            var offC = Keys.claimOn(inner, kt, a1);
            function who() { var h = kt.holder(); return h ? tree.find(h).name : "none"; }
            function cand() { var c = kt.candidate(); return c ? tree.find(c).name : "none"; }
            function tab(shift) { return fire(body, "keydown", { key: "Tab", shiftKey: !!shift }); }
            function key(k) { return fire(body, "keydown", { key: k }); }
            """);
        // the walk from nothing: the first member offered, and no one holds
        Value t = eval("tab()");
        assertTrue(t.getMember("defaultPrevented").asBoolean() && t.getMember("stopped").asBoolean(), "the walk's key is the walk's");
        assertEquals("A", eval("cand()").asString());
        assertEquals("none", eval("who()").asString(), "the holder has not moved");
        assertEquals("Offered:" + eval("pA.owner.id").asString(), events());
        assertEquals("A:offered", eval("pA.owner.component.got.join()").asString());
        eval("tab(); tab()");
        assertEquals("a2", eval("cand()").asString(), "A a1 a2");
        assertEquals("a1:offered,a1:withdrawn", eval("a1.component.got.join()").asString(), "offered as it passed, withdrawn as it moved on");
        eval("tab(true)");
        assertEquals("a1", eval("cand()").asString(), "Shift+Tab walks back");
        // Enter takes the offer up: the keys move, and the walk is over
        eval("events = []; key('Enter')");
        assertEquals("a1", eval("who()").asString());
        assertEquals("none", eval("cand()").asString());
        assertEquals("Withdrawn:" + eval("a1.id").asString() + " Granted:" + eval("a1.id").asString(), events(), "withdrawn, then granted");
        // Escape calls a walk off, and the holder keeps the keys
        eval("events = []; tab(); tab()");
        assertEquals("B", eval("cand()").asString(), "from the candidate, a2 then B - a1 holds and is walked past");
        eval("key('Escape')");
        assertEquals("none", eval("cand()").asString());
        assertEquals("a1", eval("who()").asString(), "the holder is untouched by a walk called off");
        // a walk that comes home to the holder ends there
        eval("kt.claim(d); tab(); tab(); tab(); tab(); tab()");
        assertEquals("b1", eval("cand()").asString(), "from d: A a1 a2 B b1");
        eval("tab()");
        assertEquals("none", eval("cand()").asString(), "and home to d: the walk is over");
        assertEquals("d", eval("who()").asString());
        // a press withdraws the offer
        eval("tab(); fire(inner, 'pointerdown')");
        assertEquals("none", eval("cand()").asString(), "a claim by any other means ends the walk");
        assertEquals("a1", eval("who()").asString());
        // native focus arriving withdraws it, before a key is pressed at all
        eval("tab(); field.focus(); fire(field, 'focusin')");
        assertEquals("none", eval("cand()").asString(), "the walk is the keyboard's alone");
        // and a key from there is the native world's
        eval("tab(); key2 = fire(field, 'keydown', { key: 'Tab' })");
        assertEquals("none", eval("cand()").asString());
        assertFalse(eval("key2.defaultPrevented").asBoolean(), "a Tab from a focused control is the browser's");
        eval("document.activeElement = document.body");
        // the candidate leaving the tree withdraws it
        eval("kt.claim(d); tab(); var was = cand()");
        assertEquals("A", eval("was").asString());
        eval("pA.owner.leave()");
        assertEquals("none", eval("cand()").asString(), "it left the tree: nothing is offered");
        // no member to walk: Tab is the browser's
        eval("offC(); kt.dispose(); var kn = new KeyboardSteward(fakeBranch('kn'), { onEvent: sink, party: new FocusParty() }); var m3 = fire(body, 'keydown', { key: 'Tab' })");
        assertFalse(eval("m3.defaultPrevented").asBoolean());
        eval("kn.dispose()");
    }

    /**
     * A container is asked about a member of its own branch — wouldOffer(m) —
     * and the walk steps over the ones it will not show; its own keys are the
     * way to them. What it shows is asked afresh at every step.
     */
    @Test
    void theWalkStepsOverAMemberItsContainerWillNotOffer() {
        eval("""
            kb.dispose();
            var tree = new FocusParty();
            var kt = new KeyboardSteward(fakeBranch("k"), { onEvent: sink, party: tree });
            var shown = "a1";
            var pA = tree.root.createBranch("A", { wouldOffer: function (m) { return m.name === shown; } });
            var a1 = pA.join("a1", {}), a2 = pA.join("a2", {});
            var d = tree.root.join("d", {});
            function cand() { var c = kt.candidate(); return c ? tree.find(c).name : "none"; }
            function tab() { return fire(body, "keydown", { key: "Tab" }); }
            """);
        eval("tab()");
        assertEquals("A", eval("cand()").asString(), "the holder of a branch is a member like any other");
        eval("tab()");
        assertEquals("a1", eval("cand()").asString(), "the one it shows");
        eval("tab()");
        assertEquals("d", eval("cand()").asString(), "a2 is behind a1: the walk steps over it");
        eval("shown = 'a2'; tab(); tab()");
        assertEquals("a2", eval("cand()").asString(), "what a container shows is asked afresh");
        eval("kt.dispose()");
    }

    @Test
    void aSinkThatThrowsIsReportedNotPropagated() {
        eval("kb.dispose(); var kb3 = new KeyboardSteward(fakeBranch('k3'), { onEvent: function () { throw new Error('boom'); } }); kb3.join('a', {}); kb3.claim('a')");
        assertEquals("a", eval("kb3.holder()").asString());
        assertTrue(eval("log.join()").asString().contains("onEvent threw on Granted"));
    }
}
