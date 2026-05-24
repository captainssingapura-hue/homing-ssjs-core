package hue.captains.singapura.js.homing.workspace;

import java.util.Objects;

/**
 * Typed one-line summary for a {@link WidgetEntry}. Optional in spirit
 * — every entry must have one, but {@link #EMPTY} is a valid value
 * meaning "no extra blurb beyond the label."
 *
 * <p>Per RFC 0027 — typed wrapper refuses confusion with sibling
 * String-shaped display fields.</p>
 *
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public record WidgetDescription(String text) {

    public WidgetDescription {
        Objects.requireNonNull(text, "WidgetDescription.text");
    }

    /** Sentinel for "no description." Use this rather than null. */
    public static final WidgetDescription EMPTY = new WidgetDescription("");

    public static WidgetDescription of(String text) { return new WidgetDescription(text); }
}
