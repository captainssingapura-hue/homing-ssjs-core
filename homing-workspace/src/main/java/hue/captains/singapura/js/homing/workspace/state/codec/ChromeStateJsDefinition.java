package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.ChromeState;

/** Hand-written codec class for {@link ChromeState} — composite (ThemeName + boolean). */
public final class ChromeStateJsDefinition implements DefinitionCodeGen {

    public static final ChromeStateJsDefinition INSTANCE = new ChromeStateJsDefinition();

    private ChromeStateJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != ChromeState.class) {
            throw new IllegalArgumentException(
                    "ChromeStateJsDefinition only handles ChromeState; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            class ChromeState {
                constructor(theme, fullscreen) {
                    if (!(theme instanceof ThemeName)) {
                        throw new TypeError("ChromeState.theme: expected ThemeName");
                    }
                    if (typeof fullscreen !== 'boolean') {
                        throw new TypeError("ChromeState.fullscreen: expected boolean");
                    }
                    this.theme = theme;
                    this.fullscreen = fullscreen;
                    Object.freeze(this);
                }
            }
            ChromeState.defaults = function() {
                return new ChromeState(ThemeName.DEFAULT, false);
            };
            """;
}
