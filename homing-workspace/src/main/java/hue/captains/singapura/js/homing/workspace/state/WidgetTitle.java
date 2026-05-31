package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;

/**
 * Display label for a widget — the user-facing string shown on the
 * widget's tab. The wrapper that the {@code Names Are Types} doctrine
 * demands — marks "this is a UI label, not a registry key, not a free-text
 * body."
 *
 * <p>The cached value lives in {@link WidgetInstance#title()} and is used
 * during restore-time UI rendering (until the widget mounts and supplies
 * its current title from its own state).</p>
 *
 * <p>Free-form: any non-null string is accepted. The wrapper exists for
 * type discipline at the call site, not for grammar enforcement.</p>
 *
 * @param value the displayed label
 * @since RFC 0029 cycle 1
 */
public record WidgetTitle(String value) {

    public WidgetTitle {
        Objects.requireNonNull(value, "WidgetTitle.value");
    }

    public static WidgetTitle of(String value) {
        return new WidgetTitle(value);
    }

    @Override public String toString() { return value; }
}
