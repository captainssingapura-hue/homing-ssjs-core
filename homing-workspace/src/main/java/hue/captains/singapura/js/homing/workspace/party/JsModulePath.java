package hue.captains.singapura.js.homing.workspace.party;

import java.util.Objects;

/**
 * Typed handle for a JS module's location — the path the JS runtime
 * uses to import a Secretary's behaviour or any other workspace-owned
 * JS code referenced from a Java declaration. Wraps a {@link String} per
 * RFC 0027 (no stringly-typed parameters in public surfaces).
 *
 * <p>Path semantics are runtime-defined — typically a slash-separated
 * key the framework's module loader resolves against the workspace's
 * JS resource roots. The value must be non-blank; further validation
 * is the loader's concern.</p>
 *
 * @param value the path string; non-null, non-blank
 * @since RFC 0028 cycle 1
 */
public record JsModulePath(String value) {

    public JsModulePath {
        Objects.requireNonNull(value, "JsModulePath.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("JsModulePath.value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
