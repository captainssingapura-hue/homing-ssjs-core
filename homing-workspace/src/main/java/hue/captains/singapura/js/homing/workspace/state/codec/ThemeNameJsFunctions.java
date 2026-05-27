package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.ThemeName;

/** Hand-written codec for {@link ThemeName}. */
public final class ThemeNameJsFunctions implements FunctionsCodeGen {

    public static final ThemeNameJsFunctions INSTANCE = new ThemeNameJsFunctions();

    private ThemeNameJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != ThemeName.class) {
            throw new IllegalArgumentException(
                    "ThemeNameJsFunctions only handles ThemeName; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const ThemeNameCodec = {
                transformTo(t) {
                    if (!(t instanceof ThemeName)) {
                        throw new TypeError("ThemeNameCodec.transformTo: expected ThemeName");
                    }
                    return t.value;
                },
                transformFrom(wire) {
                    return new ThemeName(wire);
                }
            };
            """;
}
