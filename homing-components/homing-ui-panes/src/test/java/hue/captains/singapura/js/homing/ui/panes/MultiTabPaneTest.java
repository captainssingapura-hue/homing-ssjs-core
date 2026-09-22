package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pane against a shimmed DOM, party and css manager: tabs are added,
 * switched, moved, removed and detached, the strip follows the state, the
 * widgets are disposed on a close and not on a detach, and every mutation
 * is reported with the studio pane's names and shapes, in order.
 */
class MultiTabPaneTest extends JsModuleTestBase {

    private static final String EVENTS = "/homing/js/hue/captains/singapura/js/homing/ui/panes/PaneEventsModule.js";
    private static final String DRAG   = "/homing/js/hue/captains/singapura/js/homing/ui/panes/TabDragModule.js";
    private static final String HAND   = "/homing/js/hue/captains/singapura/js/homing/ui/panes/TabHandModule.js";
    private static final String STRIP  = "/homing/js/hue/captains/singapura/js/homing/ui/panes/TabStripModule.js";
    private static final String KEYS   = "/homing/js/hue/captains/singapura/js/homing/ui/panes/PaneKeysModule.js";
    private static final String MENUS  = "/homing/js/hue/captains/singapura/js/homing/ui/panes/PaneMenusModule.js";
    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/ui/panes/MultiTabPaneModule.js";

    // Elements that know their children, classes and attributes; a party
    // branch that mints them; a css manager over classList; the typed class
    // names as the strings the page would resolve them to.
    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var classes = new Set(), attrs = {};
            var node = { tag: tag, children: [], parentNode: null, listeners: {},
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); },
                             toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                insertBefore: function (c, ref) { if (c.parentNode) c.parentNode.removeChild(c); var i = ref ? this.children.indexOf(ref) : -1; if (i < 0) this.children.push(c); else this.children.splice(i, 0, c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                removeAttribute: function (k) { delete attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                contains: function (c) { return c === this; },
                fire: function (t, ev) { (this.listeners[t] || []).slice().forEach(function (fn) { fn(ev || { stopPropagation: function () {}, preventDefault: function () {} }); }); },
                has: function (c) { return classes.has(c); } };
            return node;
        }
        function fakeBranch(name, deregister) {
            var kids = new Map(), names = new Set();
            return { name: name, dissolved: [],
                createElement: function (n, tag) { if (names.has(n)) throw new RangeError("name " + n + " is already in use on branch " + name); names.add(n); return el(tag); },
                createBranch: function (n) { if (kids.has(n)) throw new RangeError("branch " + n + " is already in use on " + name); var b = fakeBranch(n, function () { kids.delete(n); }); kids.set(n, b); return b; },
                dissolveBranch: function (n) { var b = kids.get(n); if (b) b.dissolve(); },
                dissolve: function () { kids.forEach(function (b) { b.dissolve(); }); this.dissolved.push(name); if (deregister) deregister(); },
                activate: function (owner) { this.owner = String(owner); } };
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); },
                    hasClass: function (e, c) { return e.classList.contains(c); },
                    size: function (e, s) { e.size = s; }, aspect: function (e, a) { e.aspect = a; } };
        var mtp_pane = "mtp_pane", mtp_strip = "mtp_strip", mtp_chip = "mtp_chip", mtp_chip_label = "mtp_chip_label",
            mtp_chip_dragging = "mtp_chip_dragging", mtp_chip_shifted = "mtp_chip_shifted", mtp_strip_loose = "mtp_strip_loose", mtp_chip_close = "mtp_chip_close", mtp_drop_mark = "mtp_drop_mark",
            mtp_chip_seated = "mtp_chip_seated",
            mtp_strip_tail = "mtp_strip_tail", mtp_add = "mtp_add", mtp_add_off = "mtp_add_off", mtp_pill = "mtp_pill",
            mtp_content = "mtp_content", mtp_tab_content = "mtp_tab_content", mtp_tab_content_hidden = "mtp_tab_content_hidden",
            mtp_empty = "mtp_empty", mtp_dock_target = "mtp_dock_target";
        var console = { error: function (m, e) { log.push("error:" + m); } };
        var host = el("div");
        var branch = fakeBranch("page");
        var kbEvents = [];
        KeyboardStewardInstance.on(function (e) { kbEvents.push(e.kind + ":" + (focusParty.find(e.id) ? focusParty.find(e.id).name : e.id)); });
        // a widget by the law: a member of the dock's branch (the pane's unless said), with activate(); its Escape yields
        function widget(key, into) {
            var w = { root: el("w-" + key), setActive: function (on) { log.push(key + ":" + (on ? "on" : "off")); },
                      activate: function () { log.push(key + ":activate"); KeyboardStewardInstance.claim(w.focus); },
                      keyDown: function (ev) { log.push(key + ":key:" + ev.key); if (ev.key === "Escape") { KeyboardStewardInstance.yield(w.focus); return true; } return ev.key === "ArrowUp"; },
                      dispose: function () { log.push(key + ":disposed"); w.focus.leave(); } };
            w.focus = (into || pane.focus).join(key, w);
            return w;
        }
        function tab(id, extra) { var t = { id: id, title: id.toUpperCase(), widget: widget(id, extra && extra.into) }; for (var k in (extra || {})) if (k !== "into") t[k] = extra[k]; return t; }
        function holder() { var h = KeyboardStewardInstance.holder(); return h ? focusParty.find(h).name : "none"; }
        var events = [];
        var paneBranch = branch.createBranch("mtp_s1");
        var pane = new MultiTabPane(paneBranch, { host: host, slotId: "s1", budget: 4,
            onEvent: function (ev) {
                events.push(ev);
                switch (ev.kind) {
                    case "AddRequested": log.push("add?" + ev.slotId); break;
                    case "TabAdded":     log.push("added:" + ev.slotId + ":" + ev.tab.id + "@" + ev.index); break;
                    case "TabRemoved":   log.push("removed:" + ev.slotId + ":" + ev.tab.id + "@" + ev.fromIndex); break;
                    case "TabMoved":     log.push("moved:" + ev.srcSlotId + ":" + ev.tab.id + "@" + ev.srcIndex + "->" + ev.destSlotId + "@" + ev.destIndex); break;
                    case "TabActivated": log.push("active:" + ev.slotId + ":" + ev.tabId); break;
                    case "TabAttached":  log.push("attached:" + ev.slotId + ":" + ev.tab.id + "@" + ev.atIndex); break;
                    case "DetachRequested": log.push("detach?" + ev.slotId + ":" + ev.tabId); break;
                    default: log.push("?" + ev.kind);
                } } });
        function chips() { return pane.el.children[0].children.filter(function (c) { return c.has("mtp_chip"); }).map(function (c) { return c.children[0].textContent; }).join(","); }
        function panels() { return pane.el.children[1].children.filter(function (c) { return c.has("mtp_tab_content"); }).map(function (c) { return c.children[0].tag + (c.has("mtp_tab_content_hidden") ? "-" : "+"); }).join(","); }
        function selected() { return pane.el.children[0].children.filter(function (c) { return c.getAttribute("aria-selected") === "true"; }).map(function (c) { return c.children[0].textContent; }).join(","); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/party/PartyModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/FocusPartyModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardSecretaryModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardEventsModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardWalkModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeyboardStewardModule.js");
        loadModule("/homing/js/hue/captains/singapura/js/homing/component/keyboard/KeysModule.js");
        loadModule(EVENTS);
        loadModule(DRAG);
        loadModule(HAND);
        loadModule(STRIP);
        loadModule(KEYS);
        loadModule(MENUS);
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }
    private String chips() { return eval("chips()").asString(); }
    private String panels() { return eval("panels()").asString(); }

    @Test
    void theFirstTabBecomesActiveAndTheStripFollowsTheState() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); pane.addTab(tab('c'))");
        assertEquals("A,B,C", chips());
        assertEquals("w-a+,w-b-,w-c-", panels());
        assertEquals("A", eval("selected()").asString());
        assertEquals("a", eval("pane.activeTab()").asString());
        assertEquals("added:s1:a@0 active:s1:a added:s1:b@1 added:s1:c@2", log());
        assertEquals("3 / 4", eval("pane.el.children[0].children.slice(-1)[0].children[1].textContent").asString());
        assertTrue(eval("pane.el.children[1].children[0].has('mtp_tab_content_hidden')").asBoolean(), "the empty line is hidden once a tab is in");
    }

    @Test
    void aSwitchShowsOnePanelAndSaysSo() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); log = []");
        eval("pane.switchTab('b')");
        assertEquals("w-a-,w-b+", panels());
        assertEquals("B", eval("selected()").asString());
        assertEquals("active:s1:b", log());
        eval("pane.switchTab('b')");
        assertEquals("active:s1:b", log(), "switching to the active tab says nothing");
        assertEquals("null,null", eval("[0, 1].map(function (i) { return String(pane.el.children[0].children[i].getAttribute('tabindex')); }).join(',')").asString(), "no chip takes native focus: the keys over the strip are the pane's");
    }

    @Test
    void aMoveReordersTheChipsAndReportsWhereTheTabWentWithThisSlotAsBothEnds() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); pane.addTab(tab('c')); log = []");
        assertTrue(eval("pane.moveTab('a', 2)").asBoolean());
        assertEquals("B,C,A", chips());
        assertEquals("b,c,a", eval("pane.tabs().join(',')").asString());
        assertEquals("moved:s1:a@0->s1@2", log());
        assertFalse(eval("pane.moveTab('a', 9)").asBoolean(), "clamped to the end, where it already is");
        assertTrue(eval("pane.moveTab('c', 0)").asBoolean());
        assertEquals("C,B,A", chips());
        assertEquals("a", eval("pane.activeTab()").asString(), "a move does not change the active tab");
        assertEquals("w-a+,w-b-,w-c-", panels(), "the panels stay where they were appended; only the chips move");
    }

    @Test
    void aCloseDisposesTheWidgetReportsThenActivatesTheNeighbour() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); pane.addTab(tab('c')); pane.switchTab('b'); log = []");
        eval("pane.removeTab('b')");
        assertEquals("A,C", chips());
        assertEquals("b:disposed removed:s1:b@1 active:s1:c", log());
        assertEquals("w-a-,w-c+", panels());
        eval("log = []; pane.removeTab('c')");
        assertEquals("c:disposed removed:s1:c@1 active:s1:a", log(), "no tab after it: the one before");
        eval("log = []; pane.removeTab('a')");
        assertEquals("a:disposed removed:s1:a@0", log());
        assertNull(eval("pane.activeTab()").asString());
        assertFalse(eval("pane.el.children[1].children[0].has('mtp_tab_content_hidden')").asBoolean(), "the empty line is back");
    }

    @Test
    void aDetachKeepsTheWidgetAliveAndSaysNothing() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); log = []");
        eval("var gone = pane.detachTab('a')");
        assertEquals("B", chips());
        assertEquals("active:s1:b", log(), "not removed, not disposed; the neighbour is activated");
        assertEquals("w-a", eval("gone.widget.root.tag").asString());
        assertNull(eval("gone.widget.root.parentNode").asString(), "its root is out of the panel, ready to be attached");
        eval("log = []; pane.attachTab(gone, 0)");
        assertEquals("A,B", chips(), "the same id back in the same pane: its chip and panel minted afresh on a branch of its own");
        assertEquals("attached:s1:a@0", log());
        eval("pane.removeTab('a'); log = []; pane.addTab(tab('a'))");
        assertEquals("B,A", chips(), "and again after a close");
    }

    @Test
    void theSizeAndAspectReachEveryChip_nowAndLater() {
        eval("pane.addTab(tab('a')); pane.size(0.5); pane.aspect(-2); pane.addTab(tab('b'))");
        assertEquals("0.5/-1 0.5/-1", eval("pane.el.children[0].children.filter(function (c) { return c.has('mtp_chip'); }).map(function (c) { return c.size + '/' + c.aspect; }).join(' ')").asString(), "clamped to −1..1; a chip made after gets them too");
        eval("pane.size(null); pane.aspect(null)");
        assertEquals("null/null", eval("var c = pane.el.children[0].children[0]; c.size + '/' + c.aspect").asString(), "null gives the design's back");
    }

    @Test
    void pinnedTabsSitFirstCannotCloseAndAreNotPassed() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('p', { pinned: true })); pane.addTab(tab('b')); log = []");
        assertEquals("P,A,B", chips());
        assertEquals("1", eval("pane.el.children[0].children[0].children.length").toString(), "no cross on the pinned chip");
        assertEquals("2", eval("pane.el.children[0].children[1].children.length").toString());
        eval("pane.moveTab('b', 0)");
        assertEquals("P,B,A", chips(), "a drop never lands before the pinned");
        assertEquals("moved:s1:b@2->s1@1", log());
    }

    @Test
    void theBudgetIsAPreconditionAndTheAddButtonFollowsIt() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); pane.addTab(tab('c'))");
        assertTrue(eval("pane.canAdd()").asBoolean());
        assertFalse(eval("pane.el.children[0].children.slice(-1)[0].children[0].has('mtp_add_off')").asBoolean());
        eval("pane.addTab(tab('d'))");
        assertFalse(eval("pane.canAdd()").asBoolean());
        assertTrue(eval("pane.el.children[0].children.slice(-1)[0].children[0].has('mtp_add_off')").asBoolean());
        assertTrue(eval("pane.el.children[0].children.slice(-1)[0].children[0].disabled").asBoolean());
        var ex = assertThrows(PolyglotException.class, () -> eval("pane.addTab(tab('e'))"));
        assertTrue(ex.getMessage().contains("budget of 4"), ex.getMessage());
        eval("log = []; pane.el.children[0].children.slice(-1)[0].children[0].fire('click')");
        assertEquals("", log(), "the add button does nothing when the budget is spent");
        eval("pane.removeTab('d'); log = []; pane.el.children[0].children.slice(-1)[0].children[0].fire('click')");
        assertEquals("add?s1", log());
        eval("pane.setAddEnabled(false)");
        assertFalse(eval("pane.canAdd()").asBoolean());
    }

    @Test
    void theCrossClosesAndAChipPressSwitches() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); log = []");
        eval("var chip = pane.el.children[0].children[1]; chip.fire('pointerdown', { button: 0, target: chip, clientX: 0, clientY: 0 })");
        assertEquals("active:s1:b", log(), "the press selects, before any release");
        eval("log = []; chip.fire('pointerdown', { button: 2, target: chip, clientX: 0, clientY: 0 }); chip.fire('click')");
        assertEquals("", log(), "a secondary button or a bare click is nothing");
        eval("log = []; pane.el.children[0].children[1].children[1].fire('click')");
        assertEquals("b:disposed removed:s1:b@1 active:s1:a", log());
    }

    /**
     * The strip's own ground — the room the chips leave — asks for the kind the
     * PAGE named, bound to the pane alone: what it offers is about where the
     * pane sits, which the pane knows nothing of. A right-click that came
     * through a chip is the chip's, never the ground's; a pane told no kind
     * leaves the ground to the browser.
     */
    @Test
    void theStripsGroundAsksForTheKindThePageNamed_boundToThePaneAlone() {
        eval("""
            var asked = [];
            var steward = { open: function (kind, object, at, opts) { asked.push(kind + "@" + at.x + "," + at.y + (object.pane === withGround ? ":pane" : ":?") + (object.tab ? ":tab" : "") + (opts.anchor === withGround.el ? ":anchored" : "")); return kind !== "refused"; } };
            var withGround = new MultiTabPane(branch.createBranch("mtp_s3"), { host: el("div"), slotId: "s3", menus: steward, stripMenu: "split" });
            withGround.addTab(tab("a"));
            var strip = withGround.el.children[0], chip = strip.children[0];
            var onGround = { target: strip, clientX: 40, clientY: 8, defaulted: false, preventDefault: function () { this.defaulted = true; } };
            strip.fire("contextmenu", onGround);
            var throughAChip = { target: chip, clientX: 5, clientY: 8, defaulted: false, preventDefault: function () { this.defaulted = true; } };
            strip.fire("contextmenu", throughAChip);
            """);
        assertEquals("split@40,8:pane:anchored", eval("asked.join(' ')").asString(), "the page's kind, at the point, the pane bound and the frame the anchor; a chip's event is not the ground's");
        assertTrue(eval("onGround.defaulted && !throughAChip.defaulted").asBoolean(), "the browser's menu is suppressed only where the steward took it");
        assertTrue(eval("withGround.menuByGround({ x: 1, y: 2 })").asBoolean(), "by call, as a key would");
        eval("withGround.dispose()");
        // a pane told no kind: the ground is the browser's
        eval("""
            var plain = new MultiTabPane(branch.createBranch("mtp_s4"), { host: el("div"), slotId: "s4", menus: steward });
            var e = { target: plain.el.children[0], clientX: 3, clientY: 4, defaulted: false, preventDefault: function () { this.defaulted = true; } };
            plain.el.children[0].fire("contextmenu", e);
            """);
        assertFalse(eval("e.defaulted").asBoolean(), "no kind named: nothing is asked and nothing is suppressed");
        assertFalse(eval("plain.menuByGround({ x: 1, y: 2 })").asBoolean());
        assertEquals("0", eval("String((plain.el.children[0].listeners.contextmenu || []).length)").asString(), "and no listener on the strip at all");
        eval("plain.dispose()");
    }

    /**
     * Given a steward, a chip asks it for the tab menu — on a right-click at the
     * point, on the ContextMenu key or Shift+F10 at the chip — bound to the pane,
     * the tab and the chip; the tab is selected first; the browser's menu is
     * suppressed only when the steward took the request. Without a steward a
     * right-click is nothing.
     */
    @Test
    void givenAStewardAChipAsksForTheTabMenu_boundToPaneTabAndChip() {
        eval("""
            var asked = [];
            var steward = { open: function (kind, object, at, opts) { asked.push(kind + ":" + object.tab.id + "@" + at.x + "," + at.y + (opts.keyboard ? ":kb" : "") + (opts.anchor === object.anchor && object.pane === withMenus ? ":bound" : "")); return object.tab.id !== "refused"; } };
            var withMenus = new MultiTabPane(branch.createBranch("mtp_s2"), { host: el("div"), slotId: "s2", menus: steward, onEvent: function (ev) { if (ev.kind === "TabActivated") log.push("active:" + ev.tabId); } });
            withMenus.addTab(tab("a")); withMenus.addTab(tab("refused")); log = [];
            var chipA = withMenus.el.children[0].children[0], chipR = withMenus.el.children[0].children[1];
            chipA.getBoundingClientRect = function () { return { left: 100, top: 10, right: 180, bottom: 40 }; };
            var ev1 = { clientX: 120, clientY: 30, defaulted: false, preventDefault: function () { this.defaulted = true; }, stopPropagation: function () {} };
            chipA.fire("contextmenu", ev1);
            var took2 = withMenus.keyDown({ key: "F10", shiftKey: true });   // while the pane holds the keys: the active tab's menu at its chip; no chip listens itself
            var ev3 = { clientX: 1, clientY: 2, defaulted: false, preventDefault: function () { this.defaulted = true; }, stopPropagation: function () {} };
            chipR.fire("contextmenu", ev3);
            """);
        assertEquals("tab:a@120,30:bound tab:a@112,38:kb:bound tab:refused@1,2:bound", eval("asked.join(' ')").asString(), "the kind, the tab, the point; the keyboard at the chip; bound to the pane, the tab and the chip");
        assertEquals("active:refused", log(), "the tab under the menu is selected first; a was active already");
        eval("withMenus.dispose()");
        assertTrue(eval("ev1.defaulted && took2 && !ev3.defaulted").asBoolean(), "the browser's menu is suppressed only when the steward took it; the key taken likewise");
        assertEquals("0", eval("String((chipA.listeners.keydown || []).length)").asString(), "no keydown listener on a chip");
        assertEquals("tab", eval("MultiTabPane.MENU").asString());
        eval("var e = { clientX: 0, clientY: 0, defaulted: false, preventDefault: function () { this.defaulted = true; } }; pane.addTab(tab('x')); pane.el.children[0].children[0].fire('contextmenu', e);");
        assertFalse(eval("e.defaulted").asBoolean(), "no steward: a right-click on this pane's chip is nothing");
    }

    /**
     * The pane is a member holding the dock's branch: a press on the frame claims
     * it; while it holds, the keys are the container's own — arrows and Home/End
     * change the active tab at once, Shift+arrows reorder by one with the tab
     * staying active, Shift+Down asks to detach, Enter has the widget activate
     * itself, Escape yields; everything else is left. Neither chip nor pane
     * listens for keys itself.
     */
    @Test
    void whileThePaneHoldsTheKeys_theyAreTheContainersOwn() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b')); pane.addTab(tab('c')); log = []; kbEvents = []");
        assertEquals("mtp_s1", eval("pane.focus.name").asString(), "the dock's branch, named after the pane's");
        assertEquals("a,b,c", eval("pane.focus.members.map(function (m) { return m.name; }).join()").asString(), "the widgets are its members");
        eval("pane.el.fire('pointerdown', { target: pane.el })");
        assertEquals("mtp_s1", eval("holder()").asString(), "a press on the frame: the pane holds");
        assertEquals("held", eval("String(pane.el.getAttribute('data-keys'))").asString(), "and says so, once, on the frame: the design answers it on the pane's own word");
        assertTrue(eval("pane.keyDown({ key: 'ArrowRight' })").asBoolean());
        assertEquals("b", eval("pane.activeTab()").asString());
        eval("pane.keyDown({ key: 'ArrowRight' }); pane.keyDown({ key: 'ArrowRight' })");
        assertEquals("c", eval("pane.activeTab()").asString(), "the end: no wrap");
        eval("pane.keyDown({ key: 'Home' })");
        assertEquals("a", eval("pane.activeTab()").asString());
        eval("pane.keyDown({ key: 'End' }); pane.keyDown({ key: 'ArrowLeft' })");
        assertEquals("b", eval("pane.activeTab()").asString());
        assertEquals("active:s1:b active:s1:c active:s1:a active:s1:c active:s1:b", log());
        eval("log = []; pane.keyDown({ key: 'ArrowLeft', shiftKey: true })");
        assertEquals("B,A,C", chips(), "Shift+Left: the active tab one slot left");
        assertEquals("moved:s1:b@1->s1@0", log());
        assertEquals("b", eval("pane.activeTab()").asString(), "still active");
        eval("log = []; pane.keyDown({ key: 'ArrowLeft', shiftKey: true })");
        assertEquals("", log(), "at the edge: nothing");
        eval("pane.keyDown({ key: 'ArrowDown', shiftKey: true })");
        assertEquals("detach?s1:b", log(), "Shift+Down asks; a holder with a desk does it");
        assertEquals("mtp_s1", eval("holder()").asString(), "the pane holds throughout");
        assertTrue(eval("pane.keyDown({ key: 'Enter' })").asBoolean());
        assertEquals("detach?s1:b b:activate", log(), "Enter: the widget activates itself");
        assertEquals("b", eval("holder()").asString(), "and holds - a claim of its own, not the pane's");
        assertEquals("null", eval("String(pane.el.getAttribute('data-keys'))").asString());
        eval("log = []; KeyboardStewardInstance._forward('KeyDown', { key: 'ArrowUp', target: null, preventDefault: function () {}, stopPropagation: function () {} })");
        assertEquals("b:key:ArrowUp", log(), "the keys are the widget's now, through the steward");
        eval("log = []; KeyboardStewardInstance.yield(pane.widgetOf('b').focus)");
        assertEquals("mtp_s1", eval("holder()").asString(), "the widget's yield: the pane catches");
        assertEquals("held", eval("String(pane.el.getAttribute('data-keys'))").asString());
        assertTrue(eval("pane.keyDown({ key: 'Escape' })").asBoolean());
        assertEquals("none", eval("holder()").asString(), "Escape: the pane yields, and nothing above holds");
        assertFalse(eval("pane.keyDown({ key: 'x' })").asBoolean(), "anything else is left");
        assertEquals("0,0", eval("[(pane.el.children[0].children[0].listeners.keydown || []).length, (pane.el.listeners.keydown || []).length].join()").asString(), "no keydown listener on a chip or the pane");
        assertEquals("1", eval("String((pane.el.children[0].listeners.mousedown || []).length)").asString(), "the strip stops the press's default");
    }

    /**
     * The walk over the focus tree: the pane is offered the keys like any other
     * member and says so on its frame; of its widgets only the tab on show is
     * offered — the others are behind it, and the pane's own arrows are the way
     * to them. Enter takes the offer up.
     */
    @Test
    void theWalkIsOfferedThePane_andOfItsWidgetsOnlyTheTabOnShow() {
        eval("""
            pane.addTab(tab('a')); pane.addTab(tab('b')); pane.addTab(tab('c')); log = [];
            function cand() { var c = KeyboardStewardInstance.candidate(); return c ? focusParty.find(c).name : 'none'; }
            function tabKey() { KeyboardStewardInstance._forward('KeyDown', { key: 'Tab', target: null, preventDefault: function () {}, stopPropagation: function () {} }); }
            function enter() { KeyboardStewardInstance._forward('KeyDown', { key: 'Enter', target: null, preventDefault: function () {}, stopPropagation: function () {} }); }
            """);
        eval("tabKey()");
        assertEquals("mtp_s1", eval("cand()").asString(), "the pane is a member like any other");
        assertEquals("candidate", eval("String(pane.el.getAttribute('data-keys'))").asString(), "and says the offer on its frame");
        eval("tabKey()");
        assertEquals("a", eval("cand()").asString(), "the tab on show");
        assertEquals("null", eval("String(pane.el.getAttribute('data-keys'))").asString(), "the offer moved on");
        eval("tabKey()");
        assertEquals("mtp_s1", eval("cand()").asString(), "b and c are behind a: the walk steps over them and comes round");
        eval("pane.switchTab('c'); log = []; tabKey()");
        assertEquals("c", eval("cand()").asString(), "what the pane shows is asked afresh");
        eval("enter()");
        assertEquals("c", eval("holder()").asString(), "Enter takes the offer up");
        assertEquals("none", eval("cand()").asString(), "and the walk is over");
    }

    /** The law: a tab's widget is a member of a dock's branch with activate(), or the tab is refused; attachTab adopts a membership from elsewhere; a closed tab's widget is out of the tree. */
    @Test
    void theLaw_aTabsWidgetIsLogicallyFocusable() {
        assertTrue(assertThrows(PolyglotException.class, () -> eval("pane.addTab({ id: 'n', title: 'N', widget: { root: el('w') } })")).getMessage().contains("not logically focusable"));
        assertTrue(assertThrows(PolyglotException.class, () -> eval("var w2 = widget('m'); delete w2.activate; pane.addTab({ id: 'm', title: 'M', widget: w2 })")).getMessage().contains("not logically focusable"));
        eval("w2.focus.leave(); var other = focusParty.root.createBranch('other', {}); pane.addTab(tab('a')); pane.attachTab(tab('e', { into: other }), 0)");
        assertEquals("a,e", eval("pane.focus.members.map(function (m) { return m.name; }).join()").asString(), "attached from another branch: adopted into this dock's");
        assertEquals("0", eval("String(other.members.length)").asString());
        eval("log = []; pane.removeTab('e')");
        assertEquals("a", eval("pane.focus.members.map(function (m) { return m.name; }).join()").asString(), "closed: out of the tree");
        eval("var w3 = widget('f'); w3.dispose = null; pane.addTab({ id: 'f', title: 'F', widget: w3 }); pane.removeTab('f')");
        assertEquals("a", eval("pane.focus.members.map(function (m) { return m.name; }).join()").asString(), "a widget that forgot to leave is left by the pane");
        eval("var d = pane.detachTab('a')");
        assertEquals("a", eval("pane.focus.members.map(function (m) { return m.name; }).join()").asString(), "detached: the membership stays until the receiver adopts it");
        eval("d.widget.dispose(); other.owner.leave(); pane.dispose()");
        assertTrue(eval("pane.focus.owner.in === null").asBoolean(), "disposed: the pane left the tree, its branch dissolved");
    }

    @Test
    void refusesADuplicateAWidgetlessTabAndAnUnknownId() {
        eval("pane.addTab(tab('a'))");
        assertTrue(assertThrows(PolyglotException.class, () -> eval("pane.addTab(tab('a'))")).getMessage().contains("already"));
        assertTrue(assertThrows(PolyglotException.class, () -> eval("pane.addTab({ id: 'x', title: 'X' })")).getMessage().contains("no widget"));
        assertTrue(assertThrows(PolyglotException.class, () -> eval("pane.switchTab('nope')")).getMessage().contains("no tab 'nope'"));
        assertEquals("1", eval("pane.count()").toString());
    }

    @Test
    void theStateIsTheStripAndDisposeTakesEverythingDown() {
        eval("pane.addTab(tab('a')); pane.addTab(tab('b', { pinned: true })); pane.switchTab('a')");
        assertEquals("{\"slotId\":\"s1\",\"activeTabId\":\"a\",\"tabs\":[{\"id\":\"b\",\"title\":\"B\",\"pinned\":true},{\"id\":\"a\",\"title\":\"A\",\"pinned\":false}]}",
                eval("JSON.stringify(pane.getState())").asString());
        eval("log = []; pane.dispose()");
        assertEquals("b:disposed a:disposed", log());
        assertEquals("0", eval("host.children.length").toString());
        assertEquals("mtp_s1", eval("paneBranch.dissolved[0]").asString(), "the branch it was given is dissolved");
        eval("pane.dispose()");
        assertEquals("b:disposed a:disposed", log(), "a second dispose is nothing");
    }
}
