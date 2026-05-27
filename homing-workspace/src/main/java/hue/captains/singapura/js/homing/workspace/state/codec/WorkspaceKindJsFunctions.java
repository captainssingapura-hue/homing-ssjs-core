package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceKind;

/** Hand-written codec for {@link WorkspaceKind}. */
public final class WorkspaceKindJsFunctions implements FunctionsCodeGen {

    public static final WorkspaceKindJsFunctions INSTANCE = new WorkspaceKindJsFunctions();

    private WorkspaceKindJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WorkspaceKind.class) {
            throw new IllegalArgumentException(
                    "WorkspaceKindJsFunctions only handles WorkspaceKind; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const WorkspaceKindCodec = {
                transformTo(k) {
                    if (!(k instanceof WorkspaceKind)) {
                        throw new TypeError("WorkspaceKindCodec.transformTo: expected WorkspaceKind");
                    }
                    return k.value;
                },
                transformFrom(wire) {
                    return new WorkspaceKind(wire);
                }
            };
            """;
}
