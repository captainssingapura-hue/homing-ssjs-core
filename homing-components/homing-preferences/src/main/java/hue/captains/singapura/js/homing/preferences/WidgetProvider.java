package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.component.Widget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Which widget a node is shown by, and with what params: the thing a
 * {@link PreferenceNode} carries beside its rigid-tree shape.
 *
 * <p>The widget is named by its module; the registry resolves the module to
 * the address it is served at when it is stamped, and the view imports it
 * the first time the node is chosen — a widget's module is loaded only if
 * the widget is. The params are stamped as JSON, so a widget receives them
 * as the plain object its {@code construct} expects: strings, numbers,
 * booleans, lists and maps, or a {@link Json.Raw} fragment already written.</p>
 *
 * @param widget the widget module
 * @param params what the widget is constructed with; empty for a widget that takes none
 */
public record WidgetProvider(Widget<?, ?> widget, Map<String, Object> params) {

    public WidgetProvider {
        Objects.requireNonNull(widget, "WidgetProvider.widget");
        params = params == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(params));
    }

    public static WidgetProvider of(Widget<?, ?> widget) {
        return new WidgetProvider(widget, Map.of());
    }

    public static WidgetProvider of(Widget<?, ?> widget, Map<String, Object> params) {
        return new WidgetProvider(widget, params);
    }

    /** This provider with one more param. */
    public WidgetProvider with(String key, Object value) {
        var out = new LinkedHashMap<>(params);
        out.put(key, value);
        return new WidgetProvider(widget, out);
    }
}
