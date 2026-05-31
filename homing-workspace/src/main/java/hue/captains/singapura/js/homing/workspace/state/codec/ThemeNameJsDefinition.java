package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.ThemeName;

/** Hand-written codec class for {@link ThemeName} — kebab-case theme key. */
public final class ThemeNameJsDefinition implements DefinitionCodeGen {

    public static final ThemeNameJsDefinition INSTANCE = new ThemeNameJsDefinition();

    private ThemeNameJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != ThemeName.class) {
            throw new IllegalArgumentException(
                    "ThemeNameJsDefinition only handles ThemeName; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class ThemeName {
                constructor(value) {
                    if (typeof value !== 'string') {
                        throw new TypeError("ThemeName.value: must be string");
                    }
                    if (!/^[a-z][a-z0-9-]*$/.test(value)) {
                        throw new RangeError(
                            "ThemeName.value '" + value + "' — kebab-case required");
                    }
                    this.value = value;
                    Object.freeze(this);
                }
                toString() { return this.value; }
            }
            ThemeName.DEFAULT = new ThemeName('default');
            """;
}
