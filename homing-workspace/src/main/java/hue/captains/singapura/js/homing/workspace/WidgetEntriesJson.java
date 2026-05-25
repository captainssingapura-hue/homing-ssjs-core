package hue.captains.singapura.js.homing.workspace;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.List;

/**
 * Serializer for the {@link WidgetEntry} registry into the JSON
 * literal consumed by {@code WidgetPickerModule.js}. Lives in its
 * own class for testability — the JSON shape is the wire contract
 * between Java and JS, so a focused unit-test target keeps the
 * contract auditable.
 *
 * <h2>Wire shape</h2>
 *
 * <pre>{@code
 * [
 *   {
 *     "simpleName"   : "MyWidget",
 *     "moduleUrl"    : "/module?class=com.example.MyWidget",
 *     "label"        : "My Widget",
 *     "icon"         : { "kind":"emoji", "value":"📦" },
 *     "description"  : "Browse documents",
 *     "group"        : "Documents",
 *     "lifecycleHint": "MULTI",
 *     "paramsFields" : [ { "name":"docId", "type":"String" } ]
 *   },
 *   ...
 * ]
 * }</pre>
 *
 * <p><b>Instance retrieval</b>. The picker needs each widget's
 * {@code title()}, {@code lifecycleHint()}, {@code paramsType()} —
 * methods on the instance, not the class. We resolve the instance
 * via the documented {@code public static final INSTANCE} convention
 * (per the WorkspaceWidget javadoc template). A missing or
 * inaccessible {@code INSTANCE} throws at boot with a clear message
 * — the framework refuses to ship a registry it can't introspect.</p>
 *
 * <p><b>Module URL</b>. Hard-coded to the framework's stable
 * {@code /module?class=<canonicalName>} convention (per
 * {@code QueryParamResolver}). Workspace lives downstream of the
 * server module but the URL grammar is published API — bake it in.</p>
 *
 * @since RFC 0025 Ext1b — Mechanism 2 (Widget Selector / Picker)
 */
public final class WidgetEntriesJson {

    private WidgetEntriesJson() {}

    /** Build the JSON array literal (without surrounding {@code <script>} tag). */
    public static String of(List<WidgetEntry> entries) {
        var sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append(',');
            appendEntry(sb, entries.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private static void appendEntry(StringBuilder sb, WidgetEntry entry) {
        Class<? extends WorkspaceWidget<?, ?>> cls = entry.widgetClass();
        WorkspaceWidget<?, ?> instance = instanceOf(cls);

        sb.append('{');
        appendKV(sb, "simpleName",  cls.getSimpleName());                   sb.append(',');
        appendKV(sb, "moduleUrl",   "/module?class=" + cls.getCanonicalName()); sb.append(',');
        appendKV(sb, "label",       entry.label().text());                  sb.append(',');
        sb.append("\"icon\":");      appendIcon(sb, entry.icon());          sb.append(',');
        appendKV(sb, "description", entry.description().text());            sb.append(',');
        appendKV(sb, "group",       entry.group().name());                  sb.append(',');
        appendKV(sb, "lifecycleHint", instance.lifecycleHint().name());     sb.append(',');
        sb.append("\"paramsFields\":");
        appendParamsFields(sb, instance.paramsType());                       sb.append(',');
        sb.append("\"defaults\":");
        appendDefaults(sb, entry.defaults());
        sb.append('}');
    }

    private static void appendDefaults(StringBuilder sb, java.util.Map<String, String> defaults) {
        sb.append('{');
        boolean first = true;
        for (var e : defaults.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            appendKV(sb, e.getKey(), e.getValue());
        }
        sb.append('}');
    }

    private static void appendIcon(StringBuilder sb, WidgetIcon icon) {
        switch (icon) {
            case WidgetIcon.Emoji e -> {
                sb.append('{');
                appendKV(sb, "kind",  "emoji"); sb.append(',');
                appendKV(sb, "value", e.glyph());
                sb.append('}');
            }
            case WidgetIcon.Svg s -> {
                sb.append('{');
                appendKV(sb, "kind",  "svg"); sb.append(',');
                appendKV(sb, "value", s.ref().toString());
                sb.append('}');
            }
        }
    }

    private static void appendParamsFields(StringBuilder sb, Class<?> paramsType) {
        sb.append('[');
        if (!WorkspaceWidget._None.class.equals(paramsType) && paramsType.isRecord()) {
            RecordComponent[] comps = paramsType.getRecordComponents();
            for (int i = 0; i < comps.length; i++) {
                if (i > 0) sb.append(',');
                sb.append('{');
                appendKV(sb, "name", comps[i].getName()); sb.append(',');
                appendKV(sb, "type", comps[i].getType().getSimpleName());
                sb.append('}');
            }
        }
        sb.append(']');
    }

    private static void appendKV(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":");
        appendString(sb, value);
    }

    /** RFC 8259 string escaping — minimum set the JSON spec requires. */
    static void appendString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else          sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    /**
     * Resolve a {@link WorkspaceWidget} instance for a registered class
     * via the {@code public static final INSTANCE} convention. The
     * picker needs each widget's title, lifecycle hint, and params
     * type — all instance methods. The framework refuses any widget
     * class missing this convention at boot, with a clear error.
     */
    static WorkspaceWidget<?, ?> instanceOf(Class<? extends WorkspaceWidget<?, ?>> cls) {
        try {
            Field f = cls.getField("INSTANCE");
            Object v = f.get(null);
            if (v instanceof WorkspaceWidget<?, ?> w) return w;
            throw new IllegalStateException(
                    cls.getName() + ".INSTANCE is not a WorkspaceWidget");
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException(
                    cls.getName() + " is missing a public static final INSTANCE field. "
                  + "WorkspaceWidget subclasses must expose their singleton instance "
                  + "so the workspace can introspect title/lifecycleHint/paramsType "
                  + "at boot. (RFC 0025 Ext1b — picker registry contract.)", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(
                    cls.getName() + ".INSTANCE is not accessible (make it public)", ex);
        }
    }
}
