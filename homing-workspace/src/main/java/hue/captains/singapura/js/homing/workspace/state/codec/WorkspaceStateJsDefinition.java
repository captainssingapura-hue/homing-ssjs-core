package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceState;

/**
 * Hand-written codec class for {@link WorkspaceState} — the envelope.
 * The JS class mirrors Java's cross-widget invariants on construct:
 * single-modal slot, single active per pane, no slot collisions, every
 * InPane.paneId resolves in the layout tree, widgetsById key matches
 * WidgetInstance.id.
 *
 * <p>{@code widgetsById} is exposed as a JS {@code Map} (preserves
 * iteration order and supports typed-key semantics); the codec
 * ({@link WorkspaceStateJsFunctions}) translates to / from a plain
 * JSON object keyed by the stringified UUID at the wire boundary.</p>
 */
public final class WorkspaceStateJsDefinition implements DefinitionCodeGen {

    public static final WorkspaceStateJsDefinition INSTANCE = new WorkspaceStateJsDefinition();

    private WorkspaceStateJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WorkspaceState.class) {
            throw new IllegalArgumentException(
                    "WorkspaceStateJsDefinition only handles WorkspaceState; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class WorkspaceState {
                constructor(schemaVersion, workspaceKind, savedAt, layout, widgetsById, chrome) {
                    if (typeof schemaVersion !== 'number' || !Number.isInteger(schemaVersion) || schemaVersion < 1) {
                        throw new RangeError(
                            "WorkspaceState.schemaVersion must be a positive integer, got " + schemaVersion);
                    }
                    if (!(workspaceKind instanceof WorkspaceKind)) {
                        throw new TypeError("WorkspaceState.workspaceKind: expected WorkspaceKind");
                    }
                    if (!(savedAt instanceof Date)) {
                        throw new TypeError("WorkspaceState.savedAt: expected Date");
                    }
                    if (!(layout instanceof LayoutNode.Leaf) && !(layout instanceof LayoutNode.Split)) {
                        throw new TypeError("WorkspaceState.layout: expected LayoutNode");
                    }
                    if (!(widgetsById instanceof Map)) {
                        throw new TypeError("WorkspaceState.widgetsById: expected Map");
                    }
                    if (!(chrome instanceof ChromeState)) {
                        throw new TypeError("WorkspaceState.chrome: expected ChromeState");
                    }

                    // Cross-widget invariants (mirror Java's compact constructor)
                    const paneIds = new Set();
                    (function collect(node) {
                        if (node instanceof LayoutNode.Leaf) {
                            paneIds.add(node.paneId.value);
                        } else {
                            collect(node.first);
                            collect(node.second);
                        }
                    })(layout);

                    let modalSeen = false;
                    const tabSlots  = new Set();    // 'paneId#tabIndex'
                    const activeByPane = new Set();
                    for (const [key, inst] of widgetsById) {
                        if (!(key instanceof WidgetInstanceId)) {
                            throw new TypeError("WorkspaceState.widgetsById key must be WidgetInstanceId");
                        }
                        if (!(inst instanceof WidgetInstance)) {
                            throw new TypeError("WorkspaceState.widgetsById value must be WidgetInstance");
                        }
                        if (key.id !== inst.id.id) {
                            throw new RangeError(
                                "WorkspaceState.widgetsById key/value id mismatch: " + key.id + " vs " + inst.id.id);
                        }
                        if (inst.location instanceof WidgetLocation.InModal) {
                            if (modalSeen) {
                                throw new RangeError("Multiple widgets in the single transit modal slot");
                            }
                            modalSeen = true;
                        } else {
                            const p = inst.location.paneId.value;
                            if (!paneIds.has(p)) {
                                throw new RangeError(
                                    "WidgetInstance references pane '" + p + "' not in layout");
                            }
                            const slot = p + '#' + inst.location.tabIndex;
                            if (tabSlots.has(slot)) {
                                throw new RangeError("Tab slot collision: " + slot);
                            }
                            tabSlots.add(slot);
                            if (inst.location.isActive) {
                                if (activeByPane.has(p)) {
                                    throw new RangeError("Multiple active tabs in pane '" + p + "'");
                                }
                                activeByPane.add(p);
                            }
                        }
                    }

                    this.schemaVersion = schemaVersion;
                    this.workspaceKind = workspaceKind;
                    this.savedAt       = savedAt;
                    this.layout        = layout;
                    this.widgetsById   = widgetsById;
                    this.chrome        = chrome;
                    Object.freeze(this);
                }
            }
            WorkspaceState.CURRENT_SCHEMA_VERSION = 1;
            """;
}
