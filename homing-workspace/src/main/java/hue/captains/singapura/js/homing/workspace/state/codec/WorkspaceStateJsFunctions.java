package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceState;

/**
 * Hand-written codec for {@link WorkspaceState} — the envelope. The
 * top-level entry point of the persistence pipeline: composes every
 * other codec in the state sub-category.
 *
 * <p>Two boundary translations:</p>
 *
 * <ul>
 *   <li>{@code savedAt} — {@link java.time.Instant} ↔ ISO-8601 string
 *       (via {@code Date.toISOString()}).</li>
 *   <li>{@code widgetsById} — {@code Map<WidgetInstanceId, WidgetInstance>}
 *       ↔ JS object keyed by the stringified UUID (JSON object keys are
 *       intrinsically strings; the codec extracts {@code id.id} on encode,
 *       reconstructs the typed key on decode).</li>
 * </ul>
 *
 * <p>The Map-with-typed-keys case is structurally the only place the
 * Names-Are-Types doctrine bends — JSON object keys are string-shaped
 * at the wire boundary. The codec re-applies typing immediately after
 * the boundary, so framework code never sees the raw string keys.</p>
 */
public final class WorkspaceStateJsFunctions implements FunctionsCodeGen {

    public static final WorkspaceStateJsFunctions INSTANCE = new WorkspaceStateJsFunctions();

    private WorkspaceStateJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WorkspaceState.class) {
            throw new IllegalArgumentException(
                    "WorkspaceStateJsFunctions only handles WorkspaceState; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const WorkspaceStateCodec = {
                transformTo(state) {
                    if (!(state instanceof WorkspaceState)) {
                        throw new TypeError("WorkspaceStateCodec.transformTo: expected WorkspaceState");
                    }
                    const widgetsByIdWire = {};
                    for (const [id, inst] of state.widgetsById) {
                        widgetsByIdWire[WidgetInstanceIdCodec.transformTo(id)] = WidgetInstanceCodec.transformTo(inst);
                    }
                    return {
                        schemaVersion: state.schemaVersion,
                        workspaceKind: WorkspaceKindCodec.transformTo(state.workspaceKind),
                        savedAt:       state.savedAt.toISOString(),
                        layout:        LayoutNodeCodec.transformTo(state.layout),
                        widgetsById:   widgetsByIdWire,
                        chrome:        ChromeStateCodec.transformTo(state.chrome)
                    };
                },
                transformFrom(wire) {
                    if (wire == null) {
                        throw new TypeError("WorkspaceStateCodec.transformFrom: wire must not be null");
                    }
                    const widgetsByIdMap = new Map();
                    for (const idStr of Object.keys(wire.widgetsById || {})) {
                        const id  = WidgetInstanceIdCodec.transformFrom(idStr);
                        const ins = WidgetInstanceCodec.transformFrom(wire.widgetsById[idStr]);
                        widgetsByIdMap.set(id, ins);
                    }
                    return new WorkspaceState(
                        wire.schemaVersion,
                        WorkspaceKindCodec.transformFrom(wire.workspaceKind),
                        new Date(wire.savedAt),
                        LayoutNodeCodec.transformFrom(wire.layout),
                        widgetsByIdMap,
                        ChromeStateCodec.transformFrom(wire.chrome));
                }
            };
            """;
}
