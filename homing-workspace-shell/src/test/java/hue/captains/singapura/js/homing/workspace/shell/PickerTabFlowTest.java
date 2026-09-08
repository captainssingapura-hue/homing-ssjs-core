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
 *   <li>every declared entry reaches the picker (RFC 0060 D20)</li>
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

    /**
     * RFC 0060 D20 — the picker offers every declared widget. It used to filter
     * out {@code spec.pinnedSpawns}, which meant closing an auto-spawned tab made
     * that widget unreachable for good. Seeding is now the arrangement's job and
     * says nothing about pickability; whether a second copy may be opened is the
     * widget's own {@code lifecycleHint()} to declare.
     */
    @Test
    void pickerOffersEveryDeclaredEntryIncludingSeededOnes() {
        Value setup = newPickerFlow();
        setup.getMember("flow").invokeMember("openInSlot", "tl");

        Value offered = js.eval("js", "globalThis._lastPicker.opts.entries");
        assertEquals(2, offered.getArraySize(),
                     "both declared entries reach the picker");
        assertEquals("DocViewWidget",
                     offered.getArrayElement(0).getMember("simpleName").asString(),
                     "the seeded widget is still offered — closing its tab must not "
                   + "make it unreachable");
        assertEquals("Spinning",
                     offered.getArrayElement(1).getMember("simpleName").asString());
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
        // RFC 0049 — the deep-select went through the focus coordinator.
        Value entered = setup.getMember("focus").getMember("enteredDeep");
        assertEquals(1, entered.getArraySize());
        assertEquals("tr", entered.getArrayElement(0).asString());
        // switchTab fired too.
        Value switched = mtp.getMember("switchedTo");
        assertEquals(1, switched.getArraySize());
        assertEquals("picker:1", switched.getArrayElement(0).getMember("tabId").asString());
    }

    @Test
    void pickerReceivesEveryEntryAndNoDisabledIds() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "tl");

        Value picker = js.getBindings("js").getMember("_lastPicker");
        Value opts   = picker.getMember("opts");
        // Both declared entries — nothing is filtered any more (D20).
        assertEquals(2, opts.getMember("entries").getArraySize());
        // disabledIds is empty in this fresh setup (no singletons open). This,
        // not the removed filter, is how a kind gets held to one instance.
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

    /** Build a fresh PickerTabFlow + stub MTP. Spec: 2 declared entries. */
    private Value newPickerFlow() {
        return js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('widgets');
                    const spec = {
                        entries: [
                            { simpleName: 'DocViewWidget', moduleUrl: '/dvw', label: 'Doc' },
                            { simpleName: 'Spinning',      moduleUrl: '/s',   label: 'Spin' }
                        ]
                    };
                    const stubMounter = {
                        resolveCalls: [],
                        resolve: function (e) { this.resolveCalls.push(e);
                                                return Promise.resolve({ construct: () => ({ root:{}, setActive:()=>{} }) }); },
                        mount:   function (mod, b, e) { return mod.construct(b, {}, {}); },
                        attach:  function (c, tab) { tab.controller = c; }
                    };
                    // RFC 0049 — deep-selects go through the focus coordinator;
                    // a spy stands in for it.
                    const focus = {
                        enteredDeep: [],
                        enterDeep: function (slotId) { this.enteredDeep.push(slotId); },
                        deepTabId: function () { return null; }
                    };
                    const flow = new PickerTabFlow({
                        mtp, focus, widgetsBranch: wB, spec,
                        workspaceCtx: {}, WidgetPickerCtor: StubPicker, mounter: stubMounter
                    });
                    return { mtp, wB, spec, flow, focus };
                })()""");
    }
}
