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
    private static final String STRIP  = "/homing/js/hue/captains/singapura/js/homing/ui/panes/TabStripModule.js";
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
            mtp_chip_dragging = "mtp_chip_dragging", mtp_chip_shifted = "mtp_chip_shifted", mtp_strip_dragging = "mtp_strip_dragging", mtp_chip_close = "mtp_chip_close", mtp_drop_mark = "mtp_drop_mark",
            mtp_strip_tail = "mtp_strip_tail", mtp_add = "mtp_add", mtp_add_off = "mtp_add_off", mtp_pill = "mtp_pill",
            mtp_content = "mtp_content", mtp_tab_content = "mtp_tab_content", mtp_tab_content_hidden = "mtp_tab_content_hidden",
            mtp_empty = "mtp_empty", mtp_dock_target = "mtp_dock_target";
        var console = { error: function (m, e) { log.push("error:" + m); } };
        var host = el("div");
        var branch = fakeBranch("page");
        function widget(key) {
            return { root: el("w-" + key), setActive: function (on) { log.push(key + ":" + (on ? "on" : "off")); },
                     dispose: function () { log.push(key + ":disposed"); } };
        }
        function tab(id, extra) { var t = { id: id, title: id.toUpperCase(), widget: widget(id) }; for (var k in (extra || {})) t[k] = extra[k]; return t; }
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
                    default: log.push("?" + ev.kind);
                } } });
        function chips() { return pane.el.children[0].children.filter(function (c) { return c.has("mtp_chip"); }).map(function (c) { return c.children[0].textContent; }).join(","); }
        function panels() { return pane.el.children[1].children.filter(function (c) { return c.has("mtp_tab_content"); }).map(function (c) { return c.children[0].tag + (c.has("mtp_tab_content_hidden") ? "-" : "+"); }).join(","); }
        function selected() { return pane.el.children[0].children.filter(function (c) { return c.getAttribute("aria-selected") === "true"; }).map(function (c) { return c.children[0].textContent; }).join(","); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(EVENTS);
        loadModule(DRAG);
        loadModule(STRIP);
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
        assertEquals("0,0", eval("[0, 1].map(function (i) { return pane.el.children[0].children[i].getAttribute('tabindex'); }).join(',')").asString(), "every chip is in the tab order, not only the active one");
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
