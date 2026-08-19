package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import java.util.Optional;

/**
 * The substrate-free contract for MultiTabPane. The JS implementation
 * ({@code MultiTabPaneModule.js}) conforms structurally — class name,
 * public method set, callback option names — and the conformance test
 * gates the match at build time per the Diligent Primitives doctrine.
 *
 * <p>This interface is documentation in code, not a runtime polymorphism
 * hook. The Java framework does not instantiate MultiTabPane; the JS
 * class is the implementation that runs in the browser. The interface
 * names what the JS class MUST expose; the conformance test asserts the
 * JS class DOES expose it.</p>
 *
 * <h2>Known V1 compromise</h2>
 *
 * <p>{@code paneIdOf / slotIdOfPaneId} use {@code String} for the
 * structural path argument, not the typed {@code PaneId} from
 * {@code homing-workspace/state/PaneId.java}. The dependency direction
 * doesn't permit referencing workspace types from studio-base; a follow-up
 * cycle moves the structural-path identifier into a shared lower module
 * so this contract can adopt it. The conformance test gates method
 * names + arity; the argument-type tightening is a separate follow-up.
 * Same applies to {@code split(orientation)} — {@code String} for now.</p>
 *
 * @since RFC 0035 — first application of the meta-doctrine beyond
 *        event management; second domain to gain a typed Java contract.
 */
public interface MultiTabPaneContract {

    // ─── State-affecting public methods ──────────────────────────────────

    void addTab(SlotId slotId, TabDescriptor tab);
    void removeTab(SlotId slotId, TabId tabId);
    void moveTab(SlotId srcSlotId, TabId tabId, SlotId destSlotId, int destIndex);
    void attachTab(SlotId slotId, TabDescriptor tab, int index);
    void switchTab(SlotId slotId, TabId tabId);
    void setWorkspaceActiveTab(Optional<TabId> tabId);
    void split(SlotId slotId, String orientation);   // V1 compromise — String
    void merge(SlotId slotId);

    // RFC 0048 — modal keyboard pane navigation. Shallow: a pane cursor moves
    // with the arrows (selectPane / focusPane), Tab cycles tabs within it
    // (cycleTabInPane). Deep: the cursor pane is entered (enterDeep) and released
    // (releaseToShallow). "deep" is derived from the workspace-active tab.
    void selectPane(SlotId slotId);
    void focusPane(String direction);   // 'left' | 'right' | 'up' | 'down' — V1 String
    void cycleTabInPane(int delta);
    void enterDeep(SlotId slotId);
    void releaseToShallow();

    // ─── Read-only public methods ────────────────────────────────────────

    Optional<TabId>  getWorkspaceActiveTab();
    String           paneIdOf(SlotId slotId);             // V1 compromise — String
    int              tabIndexOf(SlotId slotId, TabId tabId);
    Optional<SlotId> slotIdOfPaneId(String paneId);       // V1 compromise — String

    // RFC 0047 — the global tab budget as one shared pool. Replaces the
    // per-pane depth-rationed capacityOf(slotId): the limit is a workspace
    // total, so these predicates are pane-independent.
    int              totalTabs();      // tabs across every slot
    int              budget();         // the conserved workspace ceiling
    boolean          atCapacity();     // pool exhausted — no pane may add
    boolean          canAdd();         // room for one more, anywhere

    // RFC 0048 — focus-navigation read model.
    String           mode();           // 'shallow' | 'deep'
    Optional<SlotId> selectedSlot();   // the shallow-mode pane cursor

    /**
     * Name of the JS class implementing this contract. Used by the
     * conformance test to look up the right class in the loaded JS.
     */
    String JS_CLASS_NAME = "MultiTabPane";

    /**
     * Names of the eight typed callback option fields, in the order
     * matching {@link MtpMutationEvent}'s variants. Used by the
     * conformance test to assert every callback option is reachable
     * on the constructed instance via the opts bag.
     */
    String[] CALLBACK_OPTION_NAMES = {
            "onTabActivated",
            "onTabAdded",
            "onTabRemoved",
            "onTabMoved",
            "onTabAttached",
            "onWorkspaceActiveChanged",
            "onSplit",
            "onMerge"
    };
}
