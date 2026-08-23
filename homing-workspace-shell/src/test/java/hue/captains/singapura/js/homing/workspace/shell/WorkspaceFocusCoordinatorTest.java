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
    private static final String KB_MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceShallowKeyboardModule.js";
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
        loadModule(KB_MODULE);
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
                    s.fc._keyboard._handleKey(makeEvent('keydown', { key: 'ArrowRight', target: document.body }));
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return last.slotId === 'tr' && last.mode === 'shallow' && s.fc.selectedSlotId() === 'tr';
                })()""").asBoolean(), "arrow: cursor moves to the neighbour pane, shallow");
    }

    @Test
    void enterKeyUpgradesTheCursorPane() {
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc._keyboard._handleKey(makeEvent('keydown', { key: 'Enter', target: document.body }));
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

    @Test
    void detachToTransportMakesTheTabDeepAndClearsPanePaint() {
        // A previously COVERED (inert) tab detaches into the transport modal:
        // it becomes the deep selection (un-inerted — no dead modal), no pane
        // wears a ring, and the modal carries the entered accent class.
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    s.fc.enterDeep('tl');                       // A is deep; B covered/inert
                    var modalEl = makeEl('modal');
                    modalEl.classList = { _c: {},
                        add: function (k) { this._c[k] = 1; },
                        remove: function (k) { delete this._c[k]; } };
                    // Simulate MTP's detach: B leaves its slot, then the report.
                    var st = s.mtp._tabsBySlot.get('tr');
                    st.tabs = []; st.activeTabId = null;
                    s.fc.onChromeInteract({ kind: 'tab-detached', slotId: 'tr', tabId: 'B', modalEl: modalEl });
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return s.fc.deepTabId() === 'B'
                        && s.contentB.inert === false            // un-inerted — modal is live
                        && s.contentA.inert === true             // prior deep released
                        && last.mode === null                    // no pane ring
                        && modalEl.classList._c['hmtp-modal-entered'] === 1;
                })()""").asBoolean(), "detach: tab becomes deep in transport, pane paint clears, modal wears accent");
    }

    @Test
    void dockKeepsTheCarriedDeepSelectionAtDestination() {
        // THE GLOW FOLLOWS: docking the transported deep tab lands still deep
        // at the destination (the dock-path onTabActivated is ignored for a
        // tab in transport), with the modal accent removed.
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    var modalEl = makeEl('modal');
                    modalEl.classList = { _c: {},
                        add: function (k) { this._c[k] = 1; },
                        remove: function (k) { delete this._c[k]; } };
                    var st = s.mtp._tabsBySlot.get('tr');
                    var tabB = st.tabs[0]; st.tabs = []; st.activeTabId = null;
                    s.fc.onChromeInteract({ kind: 'tab-detached', slotId: 'tr', tabId: 'B', modalEl: modalEl });
                    // Dock into tl: MTP re-adds the tab, then reports. Under
                    // selection resolution the report is a pure TRIGGER — the
                    // resolver finds the tab in a slot again, so the glow lands.
                    s.mtp._tabsBySlot.get('tl').tabs.push(tabB);
                    s.mtp._tabsBySlot.get('tl').activeTabId = 'B';
                    s.fc.onChromeInteract({ kind: 'tab-docked', slotId: 'tl', tabId: 'B' });
                    var last = s.mtp.paints[s.mtp.paints.length - 1];
                    return s.fc.deepTabId() === 'B'              // still deep
                        && s.contentB.inert === false            // still entered
                        && last.slotId === 'tl' && last.mode === 'deep'
                        && modalEl.classList._c['hmtp-modal-entered'] === undefined;
                })()""").asBoolean(), "dock: the carried deep selection lands deep at the destination");
    }

    @Test
    void invariantHoldsAfterEveryStepOfAnEventStorm() {
        // RFC 0049 selection resolution — the reconciler's postcondition IS the
        // invariant: exactly one selection target; at most one selected widget
        // (exactly one non-inert tab iff deep, zero iff shallow); deep ⇒ widget.
        // Run a storm of inputs in an order the stateful ledger used to get
        // wrong, asserting the postcondition after every single step.
        assertTrue(js.eval("js", """
                (() => {
                    var s = scenario();
                    function post(label) {
                        var mode = s.fc.mode();
                        var deep = s.fc.deepTabId();
                        var slot = s.fc.selectedSlotId();
                        var nonInert = 0;
                        Object.keys(s.mtp._contentByTab).forEach(function (id) {
                            if (s.mtp._contentByTab[id] && s.mtp._contentByTab[id].inert === false) nonInert++;
                        });
                        if (mode === 'deep' && (deep == null || nonInert !== 1))
                            throw new Error(label + ': deep must mean exactly one live widget');
                        if (mode === 'shallow' && nonInert !== 0)
                            throw new Error(label + ': shallow must mean zero live widgets');
                        if (mode === 'shallow' && slot == null)
                            throw new Error(label + ': there must always be a selection target');
                        return true;
                    }
                    var modalEl = makeEl('modal');
                    modalEl.classList = { _c: {},
                        add: function (k) { this._c[k] = 1; },
                        remove: function (k) { delete this._c[k]; } };
                    post('boot');
                    s.fc.enterDeep('tl');                                          post('enter tl');
                    s.fc.onChromeInteract({ kind: 'cover-click', slotId: 'tr' });  post('cover-click tr');
                    s.fc.enterDeep('tr');                                          post('enter tr');
                    // detach B mid-deep, WITHOUT reporting (the silent path):
                    var st = s.mtp._tabsBySlot.get('tr');
                    var tabB = st.tabs[0]; st.tabs = []; st.activeTabId = null;
                    // …the next unrelated trigger self-heals into transport:
                    s.fc.onSplit('tl', 'horizontal', 'x');                         post('silent detach + split trigger');
                    // dock back WITHOUT a docked report — attach trigger suffices:
                    s.mtp._tabsBySlot.get('tl').tabs.push(tabB);
                    s.mtp._tabsBySlot.get('tl').activeTabId = 'B';
                    s.fc.onTabAttached('tl', tabB);                                post('silent dock via attach');
                    if (s.fc.deepTabId() !== 'B') throw new Error('glow must follow the silent dock');
                    s.fc.releaseToShallow();                                       post('escape');
                    // Close B — the world changes first, then the event (as MTP does).
                    var st2 = s.mtp._tabsBySlot.get('tl');
                    st2.tabs = st2.tabs.filter(function (t) { return t.id !== 'B'; });
                    st2.activeTabId = 'A';
                    delete s.mtp._contentByTab['B'];
                    s.fc.onTabRemoved('tl', tabB);                                 post('close B');
                    s.fc.enterDeep('tl');                                          post('enter tl again');
                    return true;
                })()""").asBoolean(), "the invariant postcondition holds after every event, including silent paths");
    }
}
