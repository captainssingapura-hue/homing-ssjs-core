package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A native control letting go is a yield (RFC 0066 E3, keyboard §17.5): an
 * Escape the control lent the keys had no use for, and the keys go from the
 * control up the focus tree - its member asked first, wouldHold(control), and
 * passed by when it has nothing designed for holding them; then its ancestors,
 * to the first that would hold; then the root's default allocation, the home;
 * then no one. The tree, over a fake DOM whose focus() and blur() move
 * document.activeElement and fire focusout and focusin as a browser does:
 *
 * <pre>
 *   root ── home                        the page's default, when named
 *        ├─ catches ── viewerA ▸ viewA  a panel that would hold, a viewer in it
 *        ├─ passes  ── viewerB ▸ viewB  a panel that would not
 *        ├─ viewerC ▸ viewC             a viewer straight at the root
 *        └─ keeper  ▸ fieldK            a member with keys of its own once its field lets go
 * </pre>
 */
class KeyboardLetGoTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/component/";

    private static final String SHIM = """
        var console = { error: function () {}, warn: function () {} };
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
        function sink(e) { if (e.kind === "Granted") events.push("Granted:" + nameOf(e.id) + ":" + e.by); else if (e.kind === "Released") events.push("Released:" + nameOf(e.id)); }
        var kt = new KeyboardSteward(fakeBranch("k"), { onEvent: sink, party: focusParty });
        var asked = [];
        // a component: holds - answers wouldHold - or not; undefined: says nothing, as a widget with nothing designed for it
        function comp(name, holds) {
            var c = { name: name, keys: [], keyDown: function (ev) { c.keys.push(ev.key); if (ev.key === "Escape") { Keys.yield(kt, c.m); return true; } return false; } };
            if (holds !== undefined) c.wouldHold = function (from) { asked.push(name + "?" + (from && from.tagName ? from.tagName : from ? from.name : "left")); return holds; };
            return c;
        }
        var page = el("DIV", document.body);
        var rootHome = el("DIV", page), rootCatch = el("DIV", page), rootPass = el("DIV", page);
        var rootA = el("DIV", rootCatch), viewA = el("CANVAS", rootA, { tabindex: "0" });
        var rootB = el("DIV", rootPass), viewB = el("CANVAS", rootB, { tabindex: "0" });
        var rootC = el("DIV", page), viewC = el("CANVAS", rootC, { tabindex: "0" });
        var rootK = el("DIV", page), fieldK = el("INPUT", rootK);
        var home = comp("home"), catches = comp("catches", true), passes = comp("passes", false), viewerA = comp("viewerA"), viewerB = comp("viewerB"), viewerC = comp("viewerC"), keeper = comp("keeper", true);
        var t = focusParty.root;
        home.m = t.join("home", home);
        var bc = t.createBranch("catches", catches), bp = t.createBranch("passes", passes);
        catches.m = bc.owner; passes.m = bp.owner;
        viewerA.m = bc.join("viewerA", viewerA); viewerB.m = bp.join("viewerB", viewerB);
        viewerC.m = t.join("viewerC", viewerC); keeper.m = t.join("keeper", keeper);
        [[rootHome, home], [rootCatch, catches], [rootPass, passes], [rootA, viewerA], [rootB, viewerB], [rootC, viewerC], [rootK, keeper]]
            .forEach(function (p) { Keys.claimOn(p[0], kt, p[1].m); });
        function nameOf(id) { var m = focusParty.find(id); return m ? m.name : id; }
        function holder() { var h = kt.holder(); return h ? nameOf(h) : "none"; }
        function at() { var m = kt.marker(); return m ? nameOf(m.id) + ":" + m.state : "none"; }
        function escapeFrom(view) { view.focus(); events = []; asked = []; return key(view, "Escape"); }
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
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String s(String src) { return eval(src).asString(); }

    /** The member says nothing - no UX designed for holding once its control lets go - so it is passed by, and the first ancestor that would hold takes the keys: one Escape. */
    @Test
    void aWrapperWithNothingDesigned_isPassedBy_andTheFirstAncestorThatWouldHoldTakesThem() {
        Value esc = eval("escapeFrom(viewA)");
        assertTrue(esc.getMember("defaultPrevented").asBoolean(), "the steward's key");
        assertEquals("catches", s("holder()"), "the panel that would hold has them");
        assertEquals("catches?CANVAS", s("asked.join()"), "asked about the control that let go; the viewer, saying nothing, not counted as holding");
        assertEquals("Granted:catches:yield", s("events.join()"));
        assertEquals("catches:held", s("at()"), "and nothing is focused");
        assertTrue(eval("document.activeElement === document.body").asBoolean());
        eval("events = []; key(document.body, 'Escape')");
        assertEquals("none", s("holder()"), "the panel's own Escape is its own: here, a yield to the root, which has no home yet");
    }

    /** A panel that would not hold is asked, and passed by too: on up to the root. */
    @Test
    void anAncestorThatWouldNot_isPassedBy_toTheRoot_noOneWithoutAHome() {
        eval("escapeFrom(viewB)");
        assertEquals("passes?CANVAS", s("asked.join()"));
        assertEquals("none", s("holder()"));
        assertEquals("Released:viewerB", s("events.join()"), "no home: no one");
        eval("escapeFrom(viewC)");
        assertEquals("Released:viewerC", s("events.join()"), "straight at the root: no one to ask");
    }

    /** A member with keys of its own once its control lets go says so, and keeps them, held: the second Escape is its own. */
    @Test
    void aMemberThatWouldHold_keepsThemHeld_andItsOwnEscapeYields() {
        eval("escapeFrom(fieldK)");
        assertEquals("keeper?INPUT", s("asked.join()"), "asked first, about its own field");
        assertEquals("keeper:held", s("at()"));
        assertEquals("", s("events.join()"), "no change of holder");
        eval("key(document.body, 'ArrowUp')");
        assertEquals("ArrowUp", s("keeper.keys.join()"), "its keys, with nothing focused");
    }

    /** The root's default allocation: named once the page is laid out, it holds at once while no one does, and every yield no one would hold goes to it. */
    @Test
    void theHome_holdsWhileNoOneDoes_andTakesWhatReachesTheRoot() {
        eval("events = []; Keys.home(kt, home.m)");
        assertEquals("Granted:home:home", s("events.join()"), "while no one holds, the home has them at once");
        eval("escapeFrom(viewC)");
        assertEquals("Granted:home:yield", s("events.join()"), "from a viewer at the root: one Escape, home");
        eval("escapeFrom(viewB)");
        assertEquals("Granted:home:yield", s("events.join()"), "past the panel that would not hold, home");
        eval("escapeFrom(viewA); key(document.body, 'Escape')");
        assertEquals("Granted:catches:yield,Granted:home:yield", s("events.join()"), "caught by the panel; the panel's own Escape, home");
        eval("events = []; kt.yield(home.m)");
        assertEquals("home", s("holder()"), "the home yielding keeps them");
        assertEquals("Released:home,Granted:home:home", s("events.join()"), "its yield reached the page, and was given straight back: granted anew, by home");
    }

    /** The home is the anchor: its own control letting go - an Escape by mistake - gives the keys straight back to it, granted anew, so it puts the focus back. */
    @Test
    void theHomesOwnControlLettingGo_isUndone_theHomeGrantedAnew() {
        eval("""
            var rootT = el("DIV", page), rowT = el("DIV", rootT, { tabindex: "0" });
            var toc = comp("toc", true); toc.granted = function (by) { toc.got = (toc.got || []).concat([by]); if (by !== "native") rowT.focus(); };
            toc.m = t.join("toc", toc);
            Keys.claimOn(rootT, kt, toc.m);
            Keys.home(kt, toc.m);
            rowT.focus(); events = []; toc.got = [];
            """);
        assertEquals("toc:lent", s("at()"));
        Value esc = eval("key(rowT, 'Escape')");
        assertTrue(esc.getMember("defaultPrevented").asBoolean(), "the steward's key");
        assertEquals("Released:toc,Granted:toc:home", s("events.join()"), "let go, then given back at once");
        assertEquals("home", s("toc.got.join()"), "granted anew, by home");
        assertEquals("toc:lent", s("at()"), "and the focus is back where its keys are");
        assertTrue(eval("document.activeElement === rowT").asBoolean());
        eval("events = []; rowT.blur(); key(document.body, 'Escape')");
        assertEquals("Released:toc,Granted:toc:home", s("events.join()"), "held with nothing focused, its own Escape: the same");
        assertEquals("toc:lent", s("at()"));
    }

    /** A home that leaves is no one's default; one already holding while it is named is not disturbed. */
    @Test
    void aHomeThatLeaves_isNoOnesDefault_andNamingOneDisturbsNoHolder() {
        eval("kt.claim(viewerC.m); events = []; Keys.home(kt, home.m)");
        assertEquals("viewerC", s("holder()"), "someone holds: naming the home moves nothing");
        assertEquals("", s("events.join()"));
        eval("home.m.leave(); escapeFrom(viewC)");
        assertTrue(eval("kt.homed() === null").asBoolean());
        assertEquals("Released:viewerC", s("events.join()"), "no home any more: no one");
    }
}
