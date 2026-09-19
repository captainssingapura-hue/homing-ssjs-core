package hue.captains.singapura.js.homing.preferences;

import java.util.List;
import java.util.Map;

/**
 * The smallest JSON writer the registry needs: strings, numbers, booleans,
 * null, lists, maps with string keys, and a {@link Raw} fragment already
 * written — the rigid tree's canonical JSON, say.
 */
final class Json {

    private Json() {}

    /** A fragment that is already JSON and is written as it is. */
    record Raw(String json) {}

    static String write(Object v) {
        var sb = new StringBuilder();
        write(v, sb);
        return sb.toString();
    }

    static void write(Object v, StringBuilder sb) {
        switch (v) {
            case null -> sb.append("null");
            case Raw r -> sb.append(r.json());
            case String s -> string(s, sb);
            case Boolean b -> sb.append(b);
            case Number n -> sb.append(n);
            case Map<?, ?> m -> {
                sb.append('{');
                boolean first = true;
                for (var e : m.entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    string(String.valueOf(e.getKey()), sb);
                    sb.append(':');
                    write(e.getValue(), sb);
                }
                sb.append('}');
            }
            case List<?> l -> {
                sb.append('[');
                boolean first = true;
                for (var e : l) {
                    if (!first) sb.append(',');
                    first = false;
                    write(e, sb);
                }
                sb.append(']');
            }
            default -> string(String.valueOf(v), sb);
        }
    }

    static void string(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<'  -> sb.append("\\u003c");   // never closes a script, wherever this lands
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }
}
