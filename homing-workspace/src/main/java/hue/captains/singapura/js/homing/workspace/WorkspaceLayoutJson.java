package hue.captains.singapura.js.homing.workspace;

import java.util.List;

/**
 * Wire-shape serializer for {@link RibbonItem} / {@link FooterItem}
 * lists into JSON literals consumed by {@code WorkspaceLayout.js}.
 * Sibling of {@link WidgetEntriesJson}; same pattern of "Java types →
 * JSON literal embedded in chrome bodyJs".
 *
 * <h2>Ribbon wire shape</h2>
 *
 * <pre>{@code
 * [
 *   { "kind": "Button",    "icon": {...}, "tooltip": "...", "actionId": "..." },
 *   { "kind": "Separator" },
 *   { "kind": "Label",     "text": "..." }
 * ]
 * }</pre>
 *
 * <h2>Footer wire shape</h2>
 *
 * <pre>{@code
 * [
 *   { "kind": "Label",     "text": "..." },
 *   { "kind": "Separator" },
 *   { "kind": "Button",    "icon": {...}, "tooltip": "...", "actionId": "..." }
 * ]
 * }</pre>
 *
 * <p>Empty array on either side is a meaningful signal — empty footer
 * suppresses the DOM slot entirely; empty ribbon still renders the
 * built-in title + fullscreen toggle.</p>
 *
 * @since RFC 0025 Ext1b b.2e — workspace chrome typed model.
 */
public final class WorkspaceLayoutJson {

    private WorkspaceLayoutJson() {}

    /**
     * Quote a Java string as a JSON-escaped JS string literal — useful for
     * chrome bodyJs that inlines the workspace's title (or similar dynamic
     * text) into the emitted JS via simple string concatenation:
     *
     * <pre>{@code
     * String title = workspace.title();
     * "    var layout = new WorkspaceLayout({ title: " + WorkspaceLayoutJson.quoteString(title) + ", ... });"
     * }</pre>
     */
    public static String quoteString(String value) {
        var sb = new StringBuilder();
        WidgetEntriesJson.appendString(sb, value);
        return sb.toString();
    }

    public static String ribbonItems(List<RibbonItem> items) {
        var sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            appendRibbon(sb, items.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    public static String footerItems(List<FooterItem> items) {
        var sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            appendFooter(sb, items.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    // ── Ribbon ─────────────────────────────────────────────────────────────

    private static void appendRibbon(StringBuilder sb, RibbonItem item) {
        switch (item) {
            case RibbonItem.Button b -> {
                sb.append('{');
                appendKV(sb, "kind", "Button"); sb.append(',');
                sb.append("\"icon\":"); appendIcon(sb, b.icon()); sb.append(',');
                appendKV(sb, "tooltip",  b.tooltip());  sb.append(',');
                appendKV(sb, "actionId", b.actionId());
                sb.append('}');
            }
            case RibbonItem.Separator s -> {
                sb.append('{');
                appendKV(sb, "kind", "Separator");
                sb.append('}');
            }
            case RibbonItem.Label l -> {
                sb.append('{');
                appendKV(sb, "kind", "Label"); sb.append(',');
                appendKV(sb, "text", l.text());
                sb.append('}');
            }
            case RibbonItem.Choice c -> {
                sb.append('{');
                appendKV(sb, "kind", "Choice"); sb.append(',');
                appendKV(sb, "label", c.label()); sb.append(',');
                appendKV(sb, "actionId", c.actionId()); sb.append(',');
                sb.append("\"options\":[");
                var opts = c.options();
                for (int i = 0; i < opts.size(); i++) {
                    if (i > 0) sb.append(',');
                    sb.append('{');
                    appendKV(sb, "label", opts.get(i).label()); sb.append(',');
                    appendKV(sb, "value", opts.get(i).value());
                    sb.append('}');
                }
                sb.append(']');
                sb.append('}');
            }
        }
    }

    // ── Footer ─────────────────────────────────────────────────────────────

    private static void appendFooter(StringBuilder sb, FooterItem item) {
        switch (item) {
            case FooterItem.Label l -> {
                sb.append('{');
                appendKV(sb, "kind", "Label"); sb.append(',');
                appendKV(sb, "text", l.text());
                sb.append('}');
            }
            case FooterItem.Separator s -> {
                sb.append('{');
                appendKV(sb, "kind", "Separator");
                sb.append('}');
            }
            case FooterItem.Button b -> {
                sb.append('{');
                appendKV(sb, "kind", "Button"); sb.append(',');
                sb.append("\"icon\":"); appendIcon(sb, b.icon()); sb.append(',');
                appendKV(sb, "tooltip",  b.tooltip());  sb.append(',');
                appendKV(sb, "actionId", b.actionId());
                sb.append('}');
            }
        }
    }

    // ── Shared helpers — match WidgetEntriesJson's escaping rules. ─────────

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

    private static void appendKV(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":");
        WidgetEntriesJson.appendString(sb, value);
    }
}
