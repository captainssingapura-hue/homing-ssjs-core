package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The artifact a {@link JsRule} inspects — one module's <b>final served JS</b>,
 * exactly as the browser receives it, segmented by how each part was produced
 * (RFC 0044). Phase 2 renders real instances through the serving facility; the
 * value type exists now so rules are pure and unit-testable against a
 * synthesized module, with no server.
 *
 * @param moduleClass the fully-qualified class name of the served module (its identity)
 * @param type        the module's classification — selects which rules apply
 * @param prologue    generated: the injected {@code css} / {@code href} manager imports
 * @param body        authored: the {@code .js} file or the {@code SelfContent} output
 * @param epilogue    generated: the exports
 */
public record ServedModule(String moduleClass, JsModuleType type,
                           JsSource prologue, JsSource body, JsSource epilogue) {

    public ServedModule {
        Objects.requireNonNull(moduleClass, "ServedModule.moduleClass");
        Objects.requireNonNull(type,        "ServedModule.type");
        prologue = (prologue == null) ? JsSource.EMPTY : prologue;
        body     = (body     == null) ? JsSource.EMPTY : body;
        epilogue = (epilogue == null) ? JsSource.EMPTY : epilogue;
    }

    /** The whole served text — prologue + body + epilogue, in order. Empty segments
     *  contribute no line, so an absent header doesn't shift the body's line numbers. */
    public String served() {
        var parts = new ArrayList<String>(3);
        if (!prologue.isEmpty()) parts.add(prologue.text());
        if (!body.isEmpty())     parts.add(body.text());
        if (!epilogue.isEmpty()) parts.add(epilogue.text());
        return String.join("\n", parts);
    }

    /** The source segment for a region ({@link JsRegion#WHOLE} joins all three). */
    public JsSource segment(JsRegion region) {
        return switch (region) {
            case PROLOGUE -> prologue;
            case BODY     -> body;
            case EPILOGUE -> epilogue;
            case WHOLE    -> JsSource.ofText(served());
        };
    }
}
