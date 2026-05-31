package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceKind;

/** Hand-written codec class for {@link WorkspaceKind} — PascalCase registry key. */
public final class WorkspaceKindJsDefinition implements DefinitionCodeGen {

    public static final WorkspaceKindJsDefinition INSTANCE = new WorkspaceKindJsDefinition();

    private WorkspaceKindJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WorkspaceKind.class) {
            throw new IllegalArgumentException(
                    "WorkspaceKindJsDefinition only handles WorkspaceKind; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class WorkspaceKind {
                constructor(value) {
                    if (typeof value !== 'string') {
                        throw new TypeError("WorkspaceKind.value: must be string");
                    }
                    if (!/^[A-Z][A-Za-z0-9]*$/.test(value)) {
                        throw new RangeError(
                            "WorkspaceKind.value '" + value + "' — PascalCase required");
                    }
                    this.value = value;
                    Object.freeze(this);
                }
                toString() { return this.value; }
            }
            """;
}
