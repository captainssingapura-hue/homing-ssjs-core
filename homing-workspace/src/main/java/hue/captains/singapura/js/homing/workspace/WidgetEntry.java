package hue.captains.singapura.js.homing.workspace;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One entry in a {@link WorkspaceShell}'s widget type registry. Pairs
 * a typed {@link WorkspaceWidget} class with its picker-display
 * metadata.
 *
 * <p>Construction has two paths:</p>
 *
 * <pre>{@code
 * // Defaults — picker reads widget.title() for label, EMPTY description,
 * // DEFAULT group, Emoji.DEFAULT icon.
 * WidgetEntry.of(MyWidget.class, WidgetLabel.of("My Widget"));
 *
 * // Fluent — start from defaults, override the parts you want:
 * WidgetEntry.of(MyWidget.class, WidgetLabel.of("My Widget"))
 *     .withIcon(new WidgetIcon.Emoji("📄"))
 *     .withDescription(WidgetDescription.of("Browse documents"))
 *     .withGroup(WidgetGroup.of("Documents"));
 *
 * // Explicit — pass every field directly:
 * new WidgetEntry(MyWidget.class,
 *     WidgetLabel.of("My Widget"),
 *     new WidgetIcon.Emoji("📄"),
 *     WidgetDescription.of("Browse documents"),
 *     WidgetGroup.of("Documents"));
 * }</pre>
 *
 * <p>The {@code widgetClass} is the typed handle the framework's JS
 * code-gen uses to emit a module URL for the lazy-load registry.
 * Tied to {@link WorkspaceWidget} (not raw {@code EsModule}) so the
 * type system refuses non-widget modules at compile time.</p>
 *
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public record WidgetEntry(
    Class<? extends WorkspaceWidget<?, ?>> widgetClass,
    WidgetLabel label,
    WidgetIcon icon,
    WidgetDescription description,
    WidgetGroup group,
    Map<String, String> defaults
) {

    public WidgetEntry {
        Objects.requireNonNull(widgetClass,  "WidgetEntry.widgetClass");
        Objects.requireNonNull(label,        "WidgetEntry.label");
        Objects.requireNonNull(icon,         "WidgetEntry.icon");
        Objects.requireNonNull(description,  "WidgetEntry.description");
        Objects.requireNonNull(group,        "WidgetEntry.group");
        Objects.requireNonNull(defaults,     "WidgetEntry.defaults");
        // Defensive copy + iteration-order preservation (the picker form
        // renders fields in this order). Map.copyOf hashes its entries —
        // wrong for our use. LinkedHashMap inside unmodifiableMap keeps
        // insertion order AND refuses external mutation.
        defaults = Collections.unmodifiableMap(new LinkedHashMap<>(defaults));
    }

    /**
     * Default factory — supply the widget class + a label; everything
     * else takes its default. Use the fluent {@code with*} methods to
     * override piecewise.
     */
    public static WidgetEntry of(Class<? extends WorkspaceWidget<?, ?>> widgetClass,
                                 WidgetLabel label) {
        return new WidgetEntry(widgetClass, label,
                               WidgetIcon.Emoji.DEFAULT,
                               WidgetDescription.EMPTY,
                               WidgetGroup.DEFAULT,
                               Map.of());
    }

    public WidgetEntry withIcon(WidgetIcon icon) {
        return new WidgetEntry(widgetClass, label, icon, description, group, defaults);
    }

    public WidgetEntry withDescription(WidgetDescription description) {
        return new WidgetEntry(widgetClass, label, icon, description, group, defaults);
    }

    public WidgetEntry withGroup(WidgetGroup group) {
        return new WidgetEntry(widgetClass, label, icon, description, group, defaults);
    }

    public WidgetEntry withLabel(WidgetLabel label) {
        return new WidgetEntry(widgetClass, label, icon, description, group, defaults);
    }

    /**
     * Per-Params-field default values, keyed by record component name.
     * Two consumers:
     * <ul>
     *   <li>{@code MULTI} / {@code SINGLETON} — pre-fills the picker
     *       params form so the user sees sensible starting values.</li>
     *   <li>{@code PINNED} — supplies the spawn-time params for the
     *       auto-instantiated single instance. Required (the workspace
     *       has no other source of params at boot).</li>
     * </ul>
     *
     * <p>Stringly-typed in V0 to match the picker form's natural shape;
     * full typed Params binding lands with Mechanism 3.</p>
     */
    public WidgetEntry withDefaults(Map<String, String> defaults) {
        return new WidgetEntry(widgetClass, label, icon, description, group, defaults);
    }
}
