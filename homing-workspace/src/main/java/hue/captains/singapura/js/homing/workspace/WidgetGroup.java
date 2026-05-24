package hue.captains.singapura.js.homing.workspace;

import java.util.Objects;

/**
 * Typed grouping key for picker organisation. Widgets in the same group
 * cluster together in the picker UI. Group is free-form per workspace
 * — studios pick names that fit their domain (Documents / Tools /
 * Debug / etc).
 *
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public record WidgetGroup(String name) {

    public WidgetGroup {
        Objects.requireNonNull(name, "WidgetGroup.name");
        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "WidgetGroup.name must not be blank — picker groupings need a visible name");
        }
    }

    /** Default group when none is specified. */
    public static final WidgetGroup DEFAULT = new WidgetGroup("Widgets");

    public static WidgetGroup of(String name) { return new WidgetGroup(name); }
}
