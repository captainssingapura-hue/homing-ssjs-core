package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import java.util.Objects;

/**
 * Typed identifier for one tab in a MultiTabPane. The host supplies the
 * id when calling {@code addTab(...)}; MTP itself never mints tab ids.
 * Per the Names Are Types doctrine, no raw {@code String} crosses MTP's
 * contract boundary as a tab id.
 *
 * @param value the underlying host-supplied opaque string
 * @since RFC 0035 — MTP contract package
 */
public record TabId(String value) {

    public TabId {
        Objects.requireNonNull(value, "TabId.value");
        if (value.isEmpty()) throw new IllegalArgumentException("TabId.value: must be non-empty");
    }

    public static TabId of(String value) { return new TabId(value); }

    @Override public String toString() { return value; }
}
