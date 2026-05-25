package hue.captains.singapura.js.homing.workspace;

import java.util.Objects;

/**
 * Typed display label for a {@link WidgetEntry}. Per RFC 0027 — verbose
 * but type-safe; refuses confusion with sibling display fields
 * ({@link WidgetIcon}, {@link WidgetDescription}, {@link WidgetGroup}).
 *
 * <p>The wrapped {@link #text()} is the rendering protocol — what the
 * picker displays on a widget tile. Empty strings are forbidden (use
 * {@link #of(String)} for a non-null check; refuses blank).</p>
 *
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public record WidgetLabel(String text) {

    public WidgetLabel {
        Objects.requireNonNull(text, "WidgetLabel.text");
        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "WidgetLabel.text must not be blank — picker tiles need a visible label");
        }
    }

    /** Convenience factory with the same blank-check guarantee. */
    public static WidgetLabel of(String text) { return new WidgetLabel(text); }
}
