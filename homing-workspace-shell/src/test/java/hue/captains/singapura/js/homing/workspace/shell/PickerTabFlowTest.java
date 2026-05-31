package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code PickerTabFlow}. Exercises:
 *
 * <ul>
 *   <li>{@code _filterPickable} — pure transform — directly</li>
 *   <li>{@code openInSlot} — synchronous side effects on stub MTP +
 *       stub WidgetPicker constructor (records args)</li>
 *   <li>{@code onCancel} → {@code mtp.removeTab}</li>
 * </ul>
 *
 * <p>onPick → mutate flow is largely integration territory (involves
 * dynamic import via WidgetMounter); covered by the live browser path.</p>
 *
 * <p>Style: full JS literal fixtures.</p>
 */
class PickerTabFlowTest extends JsModuleTestBase {

    private static final String STUBS = """
            class StubMtp {
                constructor() {
                    this.addedTabs       = [];
                    this.removedTabs     = [];
                    this.switchedTo      = [];
                    this.workspaceActiveTabId = null;
                    this._tabsBySlot     = new Map();
                }
                addTab(slotId, tab) {
                    if (!this._tabsBySlot.has(slotId)) {
                        this._tabsBySlot.set(slotId, { tabs: [] });
                    }
                    this._tabsBySlot.get(slotId).tabs.push(tab);
                    // Render synchronously so the picker tab's contentEl
                    // exists when picker.mountInto is called.
                    const el = { children: [], appendChild(c) { this.children.push(c); },
                                 removeChild(c) { this.children = this.children.filter(x=>x!==c); },
                                 get firstChild() { return this.children[0] || null; } };
                    tab.render(el);
                    tab._contentEl = el;
                    this.addedTabs.push({ slotId, tab });
                }
                removeTab(slotId, tabId) {
                    this.removedTabs.push({ slotId, tabId });
                    const s = this._tabsBySlot.get(slotId);
                    if (s) s.tabs = s.tabs.filter(t => t.id !== tabId);
                }
                switchTab(slotId, tabId) { this.switchedTo.push({ slotId, tabId }); }
                getWorkspaceActiveTab() { return this.workspaceActiveTabId; }
                setWorkspaceActiveTab(id) { this.workspaceActiveTabId = id; }
                getState() {
                    const tabs = {};
                    this._tabsBySlot.forEach((v, k) => {
                        tabs[k] = { tabs: v.tabs.map(t => ({ id: t.id, title: t.title })) };
                    });
                    return { tabs };
                }
            }
            class StubBranch {
                constructor(name) {
                    this.name = name;
                    this.children = [];
                }
                createBranch(name) { const c = new StubBranch(name); this.children.push(c); return c; }
                activate() {}
                dissolve() {}
            }
            globalThis.document = {
                createElement(tag) {
                    return { tag, style: { cssText: '' }, textContent: '',
                             children: [], appendChild(c) { this.children.push(c); } };
                }
            };
            // Stub WidgetPicker that records its constructor opts so tests
            // can verify entries / disabledIds + invoke onPick/onCancel.
            globalThis._lastPicker = null;
            class StubPicker {
                constructor(opts) {
                    this.opts = opts;
                    this.mountedInto = null;
                    globalThis._lastPicker = this;
                }
                mountInto(host) { this.mountedInto = host; }
            }
            globalThis.StubPicker = StubPicker;
            """;

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PickerTabFlowModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** filterPickable is an instance method now (Explicit Substrate —
     *  no static helpers). Invoke via the flow fixture. */
    @Test
    void filterPickableExcludesPinnedKinds() {
        Value flow = newPickerFlow().getMember("flow");
        Value entries = js.eval("js", """
                [
                    { simpleName: 'DocViewWidget' },
                    { simpleName: 'SpinningAnimalsWidget' },
                    { simpleName: 'MovingAnimalWidget' }
                ]""");
        Value pinned = js.eval("js", "[ 'DocViewWidget' ]");
        Value result = flow.invokeMember("filterPickable", entries, pinned);
        assertEquals(2, result.getArraySize());
        assertEquals("SpinningAnimalsWidget",
                     result.getArrayElement(0).getMember("simpleName").asString());
        assertEquals("MovingAnimalWidget",
                     result.getArrayElement(1).getMember("simpleName").asString());
    }

    @Test
    void filterPickableEmptyPinnedKeepsAll() {
        Value flow = newPickerFlow().getMember("flow");
        Value entries = js.eval("js", "[{simpleName: 'A'}, {simpleName: 'B'}]");
        Value result = flow.invokeMember("filterPickable", entries, js.eval("js", "[]"));
        assertEquals(2, result.getArraySize());
    }

    @Test
    void openInSlotAddsPickerTabAndMountsPicker() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        Value mtp  = setup.getMember("mtp");

        Value tabId = flow.invokeMember("openInSlot", "tr");

        assertEquals("picker:1", tabId.asString());
        // Tab was added to slot 'tr'.
        Value addedTabs = mtp.getMember("addedTabs");
        assertEquals(1, addedTabs.getArraySize());
        assertEquals("tr", addedTabs.getArrayElement(0).getMember("slotId").asString());
        assertEquals("picker:1",
                     addedTabs.getArrayElement(0).getMember("tab").getMember("id").asString());
        // Workspace-active set to the picker tab.
        assertEquals("picker:1", mtp.getMember("workspaceActiveTabId").asString());
        // switchTab fired too.
        Value switched = mtp.getMember("switchedTo");
        assertEquals(1, switched.getArraySize());
        assertEquals("picker:1", switched.getArrayElement(0).getMember("tabId").asString());
    }

    @Test
    void pickerReceivesFilteredEntriesAndDisabledIds() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "tl");

        Value picker = js.getBindings("js").getMember("_lastPicker");
        Value opts   = picker.getMember("opts");
        Value entries = opts.getMember("entries");
        // Spec has 2 entries, 1 pinned → 1 left.
        assertEquals(1, entries.getArraySize());
        assertEquals("Spinning",
                     entries.getArrayElement(0).getMember("simpleName").asString());
        // disabledIds is empty in this fresh setup (no singletons open).
        assertEquals(0, opts.getMember("disabledIds").getMemberKeys().size());
        // Picker was mounted into the pickerHost (not the tab's contentEl
        // directly — into a host the tab.render attaches; observable via
        // picker.mountedInto being non-null).
        assertTrue(!picker.getMember("mountedInto").isNull());
    }

    @Test
    void onCancelRemovesTheTab() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "br");

        // Trigger cancel through the picker's recorded opts.
        Value picker = js.getBindings("js").getMember("_lastPicker");
        picker.getMember("opts").getMember("onCancel").execute();

        Value removed = setup.getMember("mtp").getMember("removedTabs");
        assertEquals(1, removed.getArraySize());
        assertEquals("br",      removed.getArrayElement(0).getMember("slotId").asString());
        assertEquals("picker:1", removed.getArrayElement(0).getMember("tabId").asString());
    }

    @Test
    void inspectReportsState() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "tl");
        flow.invokeMember("openInSlot", "tr");

        Value snap = flow.invokeMember("inspect");
        assertEquals(0, snap.getMember("singletonsByKind").getMemberKeys().size());
        assertEquals(2, snap.getMember("tabsIssued").asInt());
    }

    /** Build a fresh PickerTabFlow + stub MTP. Spec: 2 entries, 1 pinned. */
    private Value newPickerFlow() {
        return js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('widgets');
                    const spec = {
                        entries: [
                            { simpleName: 'DocViewWidget', moduleUrl: '/dvw', label: 'Doc' },
                            { simpleName: 'Spinning',      moduleUrl: '/s',   label: 'Spin' }
                        ],
                        pinnedSpawns: [ 'DocViewWidget' ]
                    };
                    const stubMounter = {
                        resolveCalls: [],
                        resolve: function (e) { this.resolveCalls.push(e);
                                                return Promise.resolve({ construct: () => ({ root:{}, setActive:()=>{} }) }); },
                        mount:   function (mod, b, e) { return mod.construct(b, {}, {}); },
                        attach:  function (c, tab) { tab.controller = c; }
                    };
                    const flow = new PickerTabFlow({
                        mtp, widgetsBranch: wB, spec,
                        workspaceCtx: {}, WidgetPickerCtor: StubPicker, mounter: stubMounter
                    });
                    return { mtp, wB, spec, flow };
                })()""");
    }
}
