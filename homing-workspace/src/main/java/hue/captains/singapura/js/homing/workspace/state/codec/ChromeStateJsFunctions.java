package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.ChromeState;

/** Hand-written codec for {@link ChromeState} — composes ThemeName codec for the theme field. */
public final class ChromeStateJsFunctions implements FunctionsCodeGen {

    public static final ChromeStateJsFunctions INSTANCE = new ChromeStateJsFunctions();

    private ChromeStateJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != ChromeState.class) {
            throw new IllegalArgumentException(
                    "ChromeStateJsFunctions only handles ChromeState; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const ChromeStateCodec = {
                transformTo(c) {
                    if (!(c instanceof ChromeState)) {
                        throw new TypeError("ChromeStateCodec.transformTo: expected ChromeState");
                    }
                    return {
                        theme:      ThemeNameCodec.transformTo(c.theme),
                        fullscreen: c.fullscreen
                    };
                },
                transformFrom(wire) {
                    return new ChromeState(
                        ThemeNameCodec.transformFrom(wire.theme),
                        wire.fullscreen);
                }
            };
            """;
}
