package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0049 — behavioural test for the {@code WorkspaceFocusCoordinator}: the
 * shell-layer owner of the deep/shallow selection. Drives the coordinator with
 * the REAL per-tab {@code FocusManager} (loaded from studio-base) over a stub
 * MTP that exposes only the renderer/access facet — proving the coordinator
 * never needs the removed RFC 0048 focus API.
 */
class WorkspaceFocusCoordinatorTest extends JsModuleTestBase {

    private static final String FM_MODULE =
            "/homing/js/hue/captains/singapura/js/homing/studio/base/ui/layout/FocusManagerModule.js";
    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceFocusCoordinatorModule.js";

    /** Event-capable DOM stub + a renderer-facet-only stub MTP with two panes. */
    private static final String STUBS = """
            function makeEl(name) {
                return {
                    name: name, _children: [], parent: null, _listeners: {}, _attrs: {}, inert: false,
                    setAttribute: function (k, v) { this._attrs[k] = v; },
                    hasAttribute: function (k) { return Object.prototype.hasOwnProperty.call(this._attrs, k); },
                    appendChild: function (c) { c.parent = this; this._children.push(c); return c; },
                    contains: function (n) {
                        if (n === this) return true;
                        for (var i = 0; i < this._children.length; i++) {
                            var c = this._children[i]; if (c === n || c.contains(n)) return true;
                        }
                        return false;
                    },
                    focus: function () { document.activeElement = this; },
                    blur:  function () { if (document.activeElement === this) document.activeElement = document.body; },
                    addEventListener: function (t, fn) { (this._listeners[t] = this._listeners[t] || []).push(fn); },
                    removeEventListener: function (t, fn) {
                        var a = this._listeners[t]; if (!a) return; var i = a.indexOf(fn); if (i >= 0) a.splice(i, 1);
                    },
                    dispatchEvent: function (ev) {
                        ev.target = ev.target || this; var node = this;
                        while (node) {
                            var a = node._listeners[ev.type];
                            if (a) { var cp = a.slice(); for (var i = 0; i < cp.length; i++) { if (ev._stopped) break; cp[i].call(node, ev); } }
                            if (ev._stopped) break; node = node.parent;
                        }
                        return !ev._prevented;
                    }
                };
            }
            function makeEvent(type, props) {
                var ev = { type: type, _stopped: false, _prevented: false,
                           stopPropagation: function () { this._stopped = true; },
                           preventDefault:  function () { this._prevented = true; } };
                if (props) for (var k in props) if (Object.prototype.hasOwnProperty.call(props, k)) ev[k] = props[k];
                return ev;
            }
            globalThis.document = {
                _listeners: {},
                addEventListener: function (t, fn) { (this._listeners[t] = this._listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) {
                    var a = this._listeners[t]; if (!a) return; var i = a.indexOf(fn); if (i >= 0) a.splice(i, 1);
                }
            };
            document.body = makeEl('body');
            document.activeElement = document.body;

            // A renderer-facet-only stub MTP: two panes, one tab each.
            function makeMtp() {
                var mtp = {
                    paints: [],
                    switched: [],
                    _tabsBySlot: new Map(),
                    _contentByTab: {},
                    addStubTab: function (slot, tab) {
                        if (!this._tabsBySlot.has(slot)) this._tabsBySlot.set(slot, { tabs: [], activeTabId: null });
                        var s = this._tabsBySlot.get(slot);
                        s.tabs.push(tab);
                        if (s.activeTabId == null) s.activeTabId = tab.id;
                        this._contentByTab[tab.id] = makeEl('content:' + tab.id);
                    },
                    getState: function () {
                        var tabs = {};
                        this._tabsBySlot.forEach(function (v, k) {
                            tabs[k] = { tabs: v.tabs.map(function (t) { return { id: t.id, title: t.title || t.id }; }),
                                        activeTabId: v.activeTabId };
                        });
                        return { layout: { kind: 'leaf', slotId: 'tl' }, tabs: tabs };
                    },
                    contentElOf: function (tabId) { return this._contentByTab[tabId] || null; },
                    paintSelection: function (slotId, mode) { this.paints.push({ slotId: slotId, mode: mode }); },
                    neighbourOf: function (slot, dir) {
                        if (slot === 'tl' && dir === 'right') return 'tr';
                        if (slot === 'tr' && dir === 'left')  return 'tl';
                        return null;
                    },
                    switchTab: function (slot, tabId) {
                        this.switched.push({ slot: slot, tabId: tabId });
                        this._tabsBySlot.get(slot).activeTabId = tabId;
                    }
                };
                return mtp;
            }

            // Standard scenario: panes tl + tr with tabs A + B (spy setActive).
            function scenario() {
                document.activeElement = document.body;
                var mtp = makeMtp();
                var log = { active: [], deepChanges: [] };
                var tabA = { id: 'A', setActive: function (on) { log.active.push(['A', on]); } };
                var tabB = { id: 'B', setActive: function (on) { log.active.push(['B', on]); } };
                mtp.addStubTab('tl', tabA);
                mtp.addStubTab('tr', tabB);
                var fc = new WorkspaceFocusCoordinator({
                    mtp: mtp,
                    onDeepChanged: function (p, n) { log.deepChanges.push([p, n]); }
                }).attach();
                return { mtp: mtp, fc: fc, log: log,
                         contentA: mtp.contentElOf('A'), contentB: mtp.contentElOf('B') };
            }
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(FM_MODULE);
        loadModule(MODULE);
    }

    @Test
    void bootsShallowOnFirstPaneAndAdoptsTabsInert() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    var first = s.mtp.paints[0];
                    return first.slotId === 'tl' && first.mode === 'shallow'
                        && s.contentA.inert === true && s.contentB.inert === true
                        && s.fc.mode() === 'shallow';
                })()""").asBoolean(), "boot: shallow cursor on first pane, every tab inert");
    }

    @Test
    void enterDeepUnInertsActivatesAndFocuses() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc.enterDeep('tl');
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return last.slotId === 'tl' && last.mode === 'deep'
                        && s.contentA.inert === false && s.contentB.inert === true
                        && document.activeElement === s.contentA
                        && s.log.active.length === 1 && s.log.active[0][0] === 'A' && s.log.active[0][1] === true
                        && s.fc.deepTabId() === 'A'
                        && s.log.deepChanges.length === 1 && s.log.deepChanges[0][1] === 'A';
                })()""").asBoolean(), "enterDeep: paint deep + un-inert + setActive(true) + focus + deep-changed");
    }

    @Test
    void selectOtherInvalidatesTheDeepSelection() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc.enterDeep('tl');
                    s.fc.onChromeInteract({ kind: 'cover-dblclick', slotId: 'tr' });   // enter B
                    return s.contentA.inert === true                       // A released
                        && s.contentB.inert === false                      // B entered
                        && s.fc.deepTabId() === 'B'
                        && s.log.active.map(x => x.join(':')).join(',') === 'A:true,A:false,B:true';
                })()""").asBoolean(), "select-other: releases the old deep tab, enters the new");
    }

    @Test
    void escapeGiveUpDowngradesToShallowSameSlot() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc.enterDeep('tl');
                    // Un-consumed Escape bubbles from inside A's content to its FM.
                    s.contentA.dispatchEvent(makeEvent('keydown', { key: 'Escape' }));
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return s.fc.mode() === 'shallow'
                        && s.contentA.inert === true
                        && last.slotId === 'tl' && last.mode === 'shallow'   // hat: shallow-select same
                        && document.activeElement === document.body
                        && s.log.deepChanges.length === 2 && s.log.deepChanges[1][1] === null;
                })()""").asBoolean(), "give-up: FM releases, coordinator shallow-selects the same pane");
    }

    @Test
    void intendedFocusInEntersDeepAndRunsOnGranted() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    var granted = false;
                    // Covered widget B requests activation with a continuation.
                    s.contentB.dispatchEvent(makeEvent('intendedFocusIn',
                        { detail: { onGranted: function () { granted = true; } } }));
                    return s.fc.deepTabId() === 'B' && s.contentB.inert === false && granted === true;
                })()""").asBoolean(), "intendedFocusIn: enters the tab's pane deep and runs onGranted after un-inert");
    }

    @Test
    void arrowMovesCursorViaNeighbourOf() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc._handleKey(makeEvent('keydown', { key: 'ArrowRight', target: document.body }));
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return last.slotId === 'tr' && last.mode === 'shallow' && s.fc.selectedSlotId() === 'tr';
                })()""").asBoolean(), "arrow: cursor moves to the neighbour pane, shallow");
    }

    @Test
    void enterKeyUpgradesTheCursorPane() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc._handleKey(makeEvent('keydown', { key: 'Enter', target: document.body }));
                    return s.fc.deepTabId() === 'A' && s.contentA.inert === false;
                })()""").asBoolean(), "Enter in shallow upgrades the cursor pane to deep");
    }

    @Test
    void removingTheDeepTabReselectsShallow() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc.enterDeep('tl');
                    // Simulate MTP removing tab A (state first, then the event).
                    var st = s.mtp._tabsBySlot.get('tl');
                    var tabA = st.tabs[0];
                    st.tabs = []; st.activeTabId = null;
                    delete s.mtp._contentByTab['A'];
                    s.fc.onTabRemoved('tl', tabA);
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return s.fc.mode() === 'shallow' && s.fc.deepTabId() === null
                        && last.mode === 'shallow'
                        && s.log.deepChanges[s.log.deepChanges.length - 1][1] === null;
                })()""").asBoolean(), "removing the deep tab drops to shallow and repaints");
    }

    @Test
    void moveOfSelectedTabDowngradesToShallowAtDestination() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc.enterDeep('tl');
                    s.fc.onTabMoved('tl', 'tr', { id: 'A' });   // positional change → downgrade
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return s.fc.mode() === 'shallow' && last.slotId === 'tr' && last.mode === 'shallow'
                        && s.contentA.inert === true;
                })()""").asBoolean(), "a positional change downgrades the selection to shallow at the new pane");
    }
}
