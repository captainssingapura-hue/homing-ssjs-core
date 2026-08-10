package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0044 — a module's served JavaScript as the browser receives it: the
 * <b>complete, final artifact</b>. Conformance deliberately ignores how each
 * line was produced (authored {@code .js}, {@code SelfContent}, injected
 * managers, generated exports) — it validates the whole served module, so a
 * violation is caught no matter how it was introduced. The complete text MUST
 * be produced by the same code path the server serves from (see the engine's
 * renderer), so the validated bytes equal the served bytes.
 *
 * @param moduleClass the module's fully-qualified class name
 * @param type        its classification (drives which rule set applies)
 * @param content     the complete served JS
 */
public record ServedModule(String moduleClass, JsModuleType type, JsSource content) {

    public ServedModule {
        Objects.requireNonNull(moduleClass, "ServedModule.moduleClass");
        Objects.requireNonNull(type,        "ServedModule.type");
        content = (content == null) ? JsSource.EMPTY : content;
    }

    /** Build from the complete served JS text. */
    public static ServedModule of(String moduleClass, JsModuleType type, String servedText) {
        return new ServedModule(moduleClass, type, JsSource.ofText(servedText));
    }

    /** The complete served text. */
    public String text() { return content.text(); }

    /** The complete served text, line by line. */
    public List<String> lines() { return content.lines(); }
}
