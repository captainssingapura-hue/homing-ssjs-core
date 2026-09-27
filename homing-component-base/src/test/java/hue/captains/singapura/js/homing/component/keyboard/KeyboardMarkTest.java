package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The marker (RFC 0066 E3, keyboard §17.5): where the focus is, one per page,
 * kept by the steward — the holder, held or lent to a native control of its
 * own, or away — and the steward the only writer of the marks. Over a fake
 * DOM whose focus() and blur() move document.activeElement and fire focusout
 * then focusin as a browser does, the body active while the focus is between
 * the two.
 */
class KeyboardMarkTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/component/";

    private static final String SHIM = """
        var log = [];
        var console = { error: function (m) { log.push("error:" + m); }, warn: function (m) { log.push("warn:" + m); } };
        function fire(target, type, props) {
            var ev = Object.assign({ type: type, target: target, defaultPrevented: false, stopped: false,
                preventDefault: function () { this.defaultPrevented = true; }, stopPropagation: function () { this.stopped = true; } }, props || {});
            var path = []; for (var x = target; x; x = x.parentNode) path.unshift(x);
            for (var i = 0; i < path.length && !ev.stopped; i++) (path[i]._cap[type] || []).slice().forEach(function (f) { if (!ev.stopped) f(ev); });
            for (var j = path.length - 1; j >= 0 && !ev.stopped; j--) (path[j]._bub[type] || []).slice().forEach(function (f) { if (!ev.stopped) f(ev); });
            return ev;
        }
        function el(tag, parent, attrs) {
            var e = { tagName: tag, parentNode: parent || null, _cap: {}, _bub: {}, isContentEditable: false, _attrs: Object.assign({}, attrs || {}) };
            e.setAttribute = function (k, v) { e._attrs[k] = String(v); };
            e.getAttribute = function (k) { return k in e._attrs ? e._attrs[k] : null; };
            e.removeAttribute = function (k) { delete e._attrs[k]; };
            e.hasAttribute = function (k) { return k in e._attrs; };
            e.addEventListener = function (t, f, c) { var m = c ? e._cap : e._bub; (m[t] = m[t] || []).push(f); };
            e.removeEventListener = function (t, f, c) { var m = c ? e._cap : e._bub; var a = m[t] || []; var i = a.indexOf(f); if (i >= 0) a.splice(i, 1); };
            e.contains = function (o) { for (var x = o; x; x = x.parentNode) if (x === e) return true; return false; };
            // as a browser: the old one loses it first — the body active meanwhile — then the new one has it
            e.focus = function () {
                var was = document.activeElement;
                if (was === e) return;
                document.activeElement = document.body;
                if (was && was !== document.body) fire(was, "focusout", { relatedTarget: e });
                document.activeElement = e;
                fire(e, "focusin", { relatedTarget: was === document.body ? null : was });
            };
            e.blur = function () {
                if (document.activeElement !== e) return;
                document.activeElement = document.body;
                fire(e, "focusout", { relatedTarget: null });
            };
            return e;
        }
        var document = el("#document");
        document.body = el("BODY", document);
        document.activeElement = document.body;
        function key(target, k) { return fire(target, "keydown", { key: k }); }
        function fakeBranch(name) { return { name: name, activate: function () {}, dissolve: function () {} }; }
        var events = [];
        function sink(e) { events.push(e.kind + ":" + e.id + (e.kind === "Granted" ? ":" + e.by : e.kind === "Marked" ? ":" + e.state : "")); }
        function marks() { return events.filter(function (e) { return e.indexOf("Marked:") === 0; }).join(" "); }
        var page = el("DIV", document.body);
        var rootA = el("DIV", page), rootB = el("DIV", page), outside = el("INPUT", page);
        var fieldA = el("INPUT", rootA), paneA = el("DIV", rootA), boxA = el("DIV", rootA, { tabindex: "-1" }), fieldB = el("INPUT", rootB);
        var kb = new KeyboardSteward(fakeBranch("keyboard"), { onEvent: sink });
        function member(id, root) {
            var m = { keys: [], granted: [] };
            kb.join(id, { keyDown: function (ev) { m.keys.push(ev.key); return true; }, granted: function (by) { m.granted.push(by); } });
            if (root) m.off = Keys.claimOn(root, kb, id);
            return m;
        }
        function keysOn(e) { return e.getAttribute("data-keys"); }
        function at() { var m = kb.marker(); return m ? m.id + ":" + m.state : "none"; }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(DIR + "party/PartyModule.js");
        loadModule(DIR + "keyboard/FocusPartyModule.js");
        loadModule(DIR + "keyboard/KeyboardSecretaryModule.js");
        loadModule(DIR + "keyboard/KeyboardEventsModule.js");
        loadModule(DIR + "keyboard/KeyboardWalkModule.js");
        loadModule(DIR + "keyboard/KeyboardShortcutsModule.js");
        loadModule(DIR + "keyboard/KeyboardChordsModule.js");
        loadModule(DIR + "keyboard/KeyboardMarkModule.js");
        loadModule(DIR + "keyboard/KeyboardStewardModule.js");
        loadModule(DIR + "keyboard/KeysModule.js");
        js.eval("js", SHIM);
        js.eval("js", "var a = member('a', rootA), b = member('b', rootB)");
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String s(String src) { Value v = eval(src); return v.isNull() ? null : v.asString(); }

    @Test
    void theBrowsersFocusArrivingInsideAMember_makesItTheHolder_lent_byNative() {
        eval("kb.claim('b'); events = []; fieldA.focus()");
        assertEquals("a", s("kb.holder()"), "the first member above the element holds");
        assertEquals("a:lent", s("at()"));
        assertTrue(eval("kb.marker().el === fieldA && kb.marker().root === rootA").asBoolean());
        assertEquals("Taken:b Granted:a:native Marked:a:lent", s("events.join(' ')"), "granted by the focus, and the marker moved once");
        assertEquals("native", s("a.granted.join()"));
        assertEquals("lent", s("keysOn(rootA)"));
        assertTrue(eval("keysOn(rootB) === null").asBoolean(), "one element on the page says where the focus is");
        assertEquals("fieldA", eval("document.activeElement === fieldA ? 'fieldA' : 'other'").asString(), "and the browser's focus is left where it arrived");
    }

    @Test
    void theFocusLeavingForNothing_leavesTheSameMemberHeld() {
        eval("fieldA.focus(); events = []; fieldA.blur()");
        assertEquals("a:held", s("at()"));
        assertEquals("held", s("keysOn(rootA)"));
        assertEquals("Marked:a:held", s("marks()"));
        eval("key(document.body, 'ArrowUp')");
        assertEquals("ArrowUp", s("a.keys.join()"), "nothing focused: the holder's keyDown");
    }

    @Test
    void aMembersOwnRoot_orSomethingOnlyAScroller_isRefused_andTheMemberHeld() {
        eval("rootA.setAttribute('tabindex', '-1'); kb.claim('b'); rootA.focus()");
        assertTrue(eval("document.activeElement === document.body").asBoolean(), "blurred: a member is never natively focused");
        assertEquals("a:held", s("at()"), "and focused logically instead");
        assertEquals("native", s("a.granted.join()"));
        eval("paneA.focus()");
        assertTrue(eval("document.activeElement === document.body").asBoolean(), "a scroller's focus nobody asked for: refused too");
        assertEquals("a:held", s("at()"));
        eval("boxA.focus()");
        assertEquals("a:lent", s("at()"), "an element with a tabindex asked for it: lent");
        assertTrue(eval("kb.check().length === 0").asBoolean(), eval("kb.check().join('; ')").toString());
    }

    @Test
    void theFocusUnderNoMember_isAway_theMarkerKept_andItResumes() {
        eval("kb.claim('a'); events = []; outside.focus()");
        assertEquals("a:away", s("at()"), "the marker stays");
        assertEquals("held", s("keysOn(rootA)"), "and so does its mark");
        assertEquals("Marked:a:away", s("marks()"));
        eval("key(outside, 'ArrowUp'); key(outside, 'x')");
        assertEquals("", s("a.keys.join()"), "away: nothing is routed");
        eval("outside.blur(); key(document.body, 'ArrowUp')");
        assertEquals("a:held", s("at()"), "back to nothing: the previously focused member resumes");
        assertEquals("ArrowUp", s("a.keys.join()"));
        eval("outside.focus(); fieldB.focus()");
        assertEquals("b:lent", s("at()"), "back inside another member: that one");
    }

    @Test
    void aClaimBlursTheFocusOutsideTheClaimer_wheverItWas() {
        eval("fieldA.focus(); kb.claim('b')");
        assertTrue(eval("document.activeElement === document.body").asBoolean(), "a claim is an intention to have the focus: made true");
        assertEquals("b:held", s("at()"));
        assertTrue(eval("keysOn(rootA) === null").asBoolean());
        eval("outside.focus(); kb.claim('a')");
        assertTrue(eval("document.activeElement === document.body").asBoolean(), "away included");
        assertEquals("a:held", s("at()"));
    }

    @Test
    void aContainerNeverClaimsForItsChild_itsChildsFocusGoes() {
        eval("var innerRoot = el('DIV', rootA), fieldI = el('INPUT', innerRoot); var i = member('i', innerRoot)");
        eval("fieldI.focus()");
        assertEquals("i:lent", s("at()"), "the innermost member above the element");
        eval("kb.claim('a')");
        assertTrue(eval("document.activeElement === document.body").asBoolean(), "the container holds, and the child's control lets go");
        assertEquals("a:held", s("at()"));
        eval("fieldA.focus(); kb.claim('a')");
        assertEquals("a:lent", s("at()"), "a focus in the claimer's own area stays");
    }

    @Test
    void aReleaseBlursTheFocusInsideTheOneThatLetGo() {
        eval("fieldA.focus(); kb.release('a')");
        assertTrue(eval("document.activeElement === document.body").asBoolean());
        assertEquals("none", s("at()"));
        assertTrue(eval("keysOn(rootA) === null && keysOn(rootB) === null").asBoolean(), "no holder, no mark");
    }

    @Test
    void theCandidateWearsItsMark_andNothingElseDoes() {
        eval("kb.claim('a'); kb.offer('b')");
        assertEquals("candidate", s("keysOn(rootB)"));
        assertEquals("held", s("keysOn(rootA)"));
        eval("kb.withdraw()");
        assertTrue(eval("keysOn(rootB) === null").asBoolean());
        eval("kb.offer('b'); kb.confirm()");
        assertEquals("held", s("keysOn(rootB)"));
        assertTrue(eval("keysOn(rootA) === null").asBoolean());
    }

    @Test
    void aRootForgotten_losesItsMark_andAnEnrolledHolderWearsItAtOnce() {
        eval("kb.claim('a'); a.off()");
        assertTrue(eval("keysOn(rootA) === null").asBoolean());
        eval("a.off = Keys.claimOn(rootA, kb, 'a')");
        assertEquals("held", s("keysOn(rootA)"), "enrolled while it holds");
    }

    @Test
    void aFocusedElementTakenOutOfThePage_isReadAgainBeforeAKey() {
        eval("fieldA.focus(); document.activeElement = document.body");   // removed from the page: no event at all
        assertFalse(eval("kb.check().length === 0").asBoolean(), "the lamp says so before anyone reads");
        eval("key(document.body, 'ArrowUp')");
        assertEquals("ArrowUp", s("a.keys.join()"), "read before routing: nothing is focused, the holder's key");
        assertEquals("a:held", s("at()"));
        assertTrue(eval("kb.check().length === 0").asBoolean());
    }

    @Test
    void anEscapeNothingTook_bringsTheLentControlBackToItsMember_heldAndOneKey() {
        eval("fieldA.addEventListener('keydown', function (ev) { if (ev.key === 'Enter') ev.preventDefault(); }); fieldA.focus()");
        Value esc = eval("key(fieldA, 'Escape')");
        assertTrue(esc.getMember("defaultPrevented").asBoolean() && esc.getMember("stopped").asBoolean(), "the steward's key");
        assertTrue(eval("document.activeElement === document.body").asBoolean(), "the control let go");
        assertEquals("a:held", s("at()"));
        eval("fieldA.focus(); fieldA.addEventListener('keydown', function (ev) { if (ev.key === 'Escape') ev.preventDefault(); })");
        eval("key(fieldA, 'Escape')");
        assertEquals("a:lent", s("at()"), "an Escape the control took is the control's");
        eval("outside.focus(); key(outside, 'Escape')");
        assertTrue(eval("document.activeElement === outside").asBoolean(), "away: none of the steward's business");
    }

    /** A page without the window's focus moves the focus and fires nothing: the steward reads it, and does not wait for an event. */
    @Test
    void anEscapeWhoseBlurFiresNothing_isReadAtOnce() {
        eval("fieldB.focus(); fieldB.blur = function () { if (document.activeElement === fieldB) document.activeElement = document.body; }");
        eval("key(fieldB, 'Escape')");
        assertEquals("b:held", s("at()"));
        assertTrue(eval("kb.check().length === 0").asBoolean(), eval("kb.check().join('; ')").toString());
    }

    @Test
    void aLinkOrAPlayerWithTheFocus_askedForIt() {
        assertTrue(eval("KeyboardMark.asked(el('A', rootA)) && KeyboardMark.asked(el('VIDEO', rootA)) && KeyboardMark.asked(el('SPAN', rootA, { contenteditable: 'true', tabindex: '0' }))").asBoolean());
        assertFalse(eval("KeyboardMark.asked(el('SECTION', rootA)) || KeyboardMark.asked(el('PRE', rootA))").asBoolean(), "a scroller the browser made focusable did not");
    }

    @Test
    void checkNamesAMarkNotTheStewards() {
        eval("kb.claim('a')");
        assertTrue(eval("kb.check().length === 0").asBoolean());
        eval("rootB.setAttribute('data-keys', 'held')");
        assertEquals("the root of b says held, not null", s("kb.check().join()"));
    }

    @Test
    void aGrantedHandlerFocusingItsOwnControl_endsLent() {
        eval("""
            var rootC = el("DIV", page), fieldC = el("INPUT", rootC);
            kb.join('c', { granted: function () { fieldC.focus(); } });
            Keys.claimOn(rootC, kb, 'c');
            fieldA.focus(); kb.claim('c');
            """);
        assertEquals("c:lent", s("at()"), "the claim blurred A's field, and C's own control took the focus");
        assertTrue(eval("document.activeElement === fieldC").asBoolean());
        assertTrue(eval("kb.check().length === 0").asBoolean(), eval("kb.check().join('; ')").toString());
    }

    /** Everyone above the marker, up to the root, told on every move of it — nearest first — with the marker; the ones it left, told it left. */
    @Test
    void everyoneAboveIsToldOnEveryMove_withTheMarker_andAgainWhenTheTreeMoves() {
        eval("""
            kb.dispose(); events = [];
            var tree = focusParty;   // the page's stationed party: the only one a steward is bound to
            var kt = new KeyboardSteward(fakeBranch("k"), { onEvent: sink, party: tree });
            var told = [];
            function box(name) { return { within: function (on, at) { told.push(name + ":" + on + ":" + (at ? at.state : "none")); } }; }
            var desk = tree.root.createBranch("desk", box("desk")), p = desk.createBranch("p", box("p")), q = desk.createBranch("q", box("q"));
            var w = p.join("w", {}), v = q.join("v", {});
            Keys.claimOn(rootA, kt, w); Keys.claimOn(rootB, kt, v);
            """);
        eval("kt.claim(w)");
        assertEquals("p:true:held,desk:true:held", s("told.join()"), "nearest first, up to the root");
        eval("told = []; fieldA.focus()");
        assertEquals("p:true:lent,desk:true:lent", s("told.join()"), "held to lent is a move: told again");
        eval("told = []; fieldB.focus()");
        assertEquals("p:false:lent,q:true:lent,desk:true:lent", s("told.join()"), "the one it left, then everyone above it now");
        eval("told = []; q.adopt(w); fieldB.blur(); told = []; p.adopt(v)");
        assertEquals("q:false:held,p:true:held,desk:true:held", s("told.join()"), "the tree moved under the marker: told again");
        eval("kt.dispose()");
    }
}
